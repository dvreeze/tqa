/*
 * Copyright 2011-2017 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import scala.collection.immutable.ArraySeq
import scala.collection.immutable.ListMap
import scala.collection.parallel.CollectionConverters._
import scala.io.Codec
import scala.util.Using
import scala.util.chaining._

import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyBaseFactoryFromRemoteZip.convertZipEntryNameUsingForwardSlash
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyBaseFactoryFromRemoteZip.MyUriResolver
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.ThreadSafeSaxonDocumentBuilder
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor
import org.xml.sax.InputSource

/**
 * TaxonomyBase factory from a remote (or local) taxonomy package ZIP file. The ZIP does not have to be
 * a taxonomy package with META-INF/taxonomyPackage.xml file, but it does need to have a META-INF/catalog.xml
 * file. Moreover, this catalog.xml file must be invertible, so that there is only one original URI per mapped URI!
 * The catalog must also be consistent with the document URIs found during DTS discovery. Otherwise the loading
 * of the documents whose URIs were found during DTS discovery will probably fail!
 *
 * Another thing to keep in mind is that each XML file in the ZIP stream will be parsed, even if most of them
 * are not part of the DTS. Indeed, DTS discovery is done once all XML documents have been loaded. This may be
 * unrealistic if the ZIP stream contains far too many ZIP entries, for example for unused taxonomy versions or
 * parts of the taxonomy that fall outside of the DTSes we are interested in!
 *
 * So if the catalog is not invertible or the ZIP contains far more documents than required, this TaxonomyBase
 * factory is not usable.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBaseFactoryFromRemoteZip(
    val createZipInputStream: () => ZipInputStream,
    val transformDocument: SaxonDocument => BackingDocumentApi) {

  private val catalogZipEntryName = "META-INF/catalog.xml" // Unix style, with forward slash

  def withTransformDocuemnt(
      newTransformDocument: SaxonDocument => BackingDocumentApi): TaxonomyBaseFactoryFromRemoteZip = {
    new TaxonomyBaseFactoryFromRemoteZip(createZipInputStream, newTransformDocument)
  }

  /**
   * Loads a taxonomy as TaxonomyBase, from the given entrypoint URIs. This method calls method readAllXmlDocuments,
   * and then calls the other overloaded loadDts method (which does not need the ZIP input stream anymore).
   */
  def loadDts(entrypointUris: Set[URI]): TaxonomyBase = {
    val xmlByteArrays: ListMap[String, ArraySeq[Byte]] = readAllXmlDocuments()

    loadDts(entrypointUris, xmlByteArrays)
  }

  /**
   * Reads all XML documents in the ZIP stream into memory, not as parsed DOM trees, but as immutable byte arrays.
   * More precisely, the result is a Map from ZIP entry names (following the ZipEntry.getName format on Unix) to immutable
   * ArraySeq collections of bytes. Again, the ZIP entry names are assumed to use Unix-style (file component) separators.
   *
   * After having this result, other code can safely turn this collection into a parallel collection of parsed XML documents,
   * and it can also first grab the catalog.xml content and use it for computing the (original) URIs of the other documents.
   *
   * Admittedly, reading all XML files in the ZIP stream into memory may have quite a memory footprint, which some time
   * later is GC'ed. On the other hand, code that exploits parallelism with the result of this function as input can be
   * quite simple to reason about (note the immutable byte arrays), without introducing any Futures and later block on them.
   * Moreover, if we cannot even temporarily fill all files into byte arrays, are we sure we have enough memory for whatever
   * the program does with the loaded taxonomy or taxonomies?
   */
  def readAllXmlDocuments(): ListMap[String, ArraySeq[Byte]] = {
    Using.resource(createZipInputStream()) { zis =>
      Iterator
        .continually(zis.getNextEntry())
        .takeWhile(_ != null)
        .filterNot(zipEntry => zipEntry.isDirectory || isZipEntryNameOfNonXmlFile(zipEntry.getName))
        .map { zipEntry =>
          zipEntry.getName.pipe(convertZipEntryNameUsingForwardSlash) -> readZipEntry(zis).tap(_ => zis.closeEntry())
        }
        .to(ListMap)
    }
  }

  /**
   * Loads a taxonomy as TaxonomyBase, from the given entrypoint URIs. This is the method that calls all the other
   * methods of this class, except for method readAllXmlDocuments (and of course the overloaded loadDts method).
   *
   * The 2nd parameter is the Map from ZIP entry names to immutable byte arrays.
   * The ZIP entry names are assumed to use Unix-style (file component) separators.
   */
  def loadDts(entrypointUris: Set[URI], xmlByteArrays: ListMap[String, ArraySeq[Byte]]): TaxonomyBase = {
    val catalog: SimpleCatalog = locateAndParseCatalog(xmlByteArrays)

    val docs: IndexedSeq[SaxonDocument] =
      parseAllTaxonomyDocuments(xmlByteArrays, catalog).ensuring(_.forall(_.uriOption.nonEmpty))

    // If the catalog is not invertible, it is likely that DTS discovery will fail!
    // In any case, if DTS discovery below finds URIs that are not within the document collection, an exception is thrown!
    val dtsUris: Set[URI] = findDtsUris(entrypointUris, docs)

    val docsInDts: IndexedSeq[BackingDocumentApi] =
      docs.filter(d => dtsUris.contains(d.uriOption.get)).map(transformDocument)

    val taxonomyBase: TaxonomyBase = docsInDts.map(TaxonomyDocument.build).pipe(TaxonomyBase.build)
    taxonomyBase
  }

  /**
   * Finds the "META-INF/catalog.xml" file in the file data collection, throws an exception if not found, and parses
   * the catalog data by calling method "parseCatalog". The file data collection uses as Map keys the ZIP entry
   * names, using Unix-style (file component) separators.
   */
  def locateAndParseCatalog(fileDataCollection: ListMap[String, ArraySeq[Byte]]): SimpleCatalog = {
    val fileData: ArraySeq[Byte] =
      fileDataCollection.getOrElse(catalogZipEntryName, sys.error(s"Missing META-INF/catalog.xml file"))
    parseCatalog(fileData)
  }

  /**
   * Parses the catalog file data (as immutable byte array) into a SimpleCatalog. The returned catalog has baseUri
   * the relative URI "META-INF/catalog.xml".
   */
  def parseCatalog(fileData: ArraySeq[Byte]): SimpleCatalog = {
    val docParser = DocumentParserUsingStax.newInstance()
    // A relative document URI, which is allowed for indexed/simple documents!
    val docUri: URI = URI.create(catalogZipEntryName)
    val catalogRootElem: indexed.Elem =
      indexed.Elem(docUri, docParser.parse(new ByteArrayInputStream(fileData.toArray)).documentElement)

    SimpleCatalog.fromElem(catalogRootElem)
  }

  /**
   * Parses all (taxonomy) documents, without knowing the DTS yet. It is required that the passed XML catalog
   * is invertible, or else this method does not work. After calling this function all documents are there in order to compute
   * the DTS and create the taxonomy (from a subset of those documents).
   *
   * The first parameter is the Map from ZIP entry names to immutable byte arrays.
   * The ZIP entry names are assumed to use Unix-style (file component) separators.
   *
   * Implementation note: this function parses many documents in parallel, for speed.
   */
  def parseAllTaxonomyDocuments(
      fileDataCollection: ListMap[String, ArraySeq[Byte]],
      catalog: SimpleCatalog): IndexedSeq[SaxonDocument] = {
    val reverseCatalog: SimpleCatalog = catalog.reverse

    val processor: Processor = new Processor(false) // Always a new Processor needed?

    val uriResolver: URI => InputSource = new MyUriResolver(fileDataCollection, catalog)
    val docBuilder: ThreadSafeSaxonDocumentBuilder = ThreadSafeSaxonDocumentBuilder(processor, uriResolver)

    val taxoFileDataCollection: ListMap[String, ArraySeq[Byte]] =
      fileDataCollection.filterNot(kv => isZipEntryNameInMetaInf(kv._1) || isZipEntryNameOfNonXmlFile(kv._1))

    // Parallel parsing
    taxoFileDataCollection.toSeq.par
      .map {
        case (zipEntryName, fileData) =>
          val localUri: URI = URI.create(zipEntryName.pipe(convertZipEntryNameUsingForwardSlash))
          val originalUri: URI = reverseCatalog.getMappedUri(localUri)

          // Here we ensure that roundtripping from local URIs to original URIs and back to local URIs return
          // the same local URIs. This ensures that the catalog and reverse catalog are the inverse catalogs of
          // each other. This does not ensure that the original URIs match the ones found in the DTS.
          require(
            catalog.getMappedUri(originalUri) == localUri,
            s"URI roundtripping failed for (local) document URI '$localUri'"
          )

          docBuilder.build(originalUri)
      }
      .seq
      .toIndexedSeq
      .sortBy(_.uriOption.map(_.toString).getOrElse(""))
  }

  /**
   * Finds all URIs in the DTS given the entrypoint URIs passed. A superset of the DTS as document collection
   * is passed as second parameter. If the result URI set is not a subset of the URIs of the passed document
   * collection (as 2nd parameter), an exception is thrown.
   */
  def findDtsUris(entrypointUris: Set[URI], allTaxoDocs: Seq[SaxonDocument]): Set[URI] = {
    val allDocDependencies: Seq[DocDependencyList] = allTaxoDocs.map { doc =>
      DocDependencyDiscovery.findDocDependencyList(doc)
    }

    val docDependenciesMap: Map[URI, DocDependencyList] =
      allDocDependencies.groupBy(_.docUri).view.mapValues(_.head).toMap

    val dtsDiscovery: DtsDiscovery = new DtsDiscovery(docDependenciesMap)

    // Finds the DTS, and checks if the DTS is indeed inside the document collection (which further validates the (reverse) catalog)
    dtsDiscovery.findDts(entrypointUris).tap(uris => ensureWithinDocumentCollection(uris, allTaxoDocs))
  }

  private def ensureWithinDocumentCollection(docUris: Set[URI], allTaxoDocs: Seq[SaxonDocument]): Unit = {
    val allDocUris: Set[URI] = allTaxoDocs.flatMap(_.uriOption).toSet

    val outsideDocUris: Set[URI] = docUris.filterNot(allDocUris)
    require(
      outsideDocUris.isEmpty,
      s"Not all URIs are inside the document set (at most 10 shown): ${outsideDocUris.take(10).mkString(", ")}")
  }

  private def readZipEntry(zis: ZipInputStream): ArraySeq[Byte] = {
    val bos = new ByteArrayOutputStream()
    val buffer = Array.ofDim[Byte](bufferSize)
    Iterator.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(len => bos.write(buffer, 0, len))
    ArraySeq.unsafeWrapArray(bos.toByteArray) // The original mutable array does not escape
  }

  private def isZipEntryNameOfNonXmlFile(zipEntryName: String): Boolean = !isZipEntryNameOfXmlFile(zipEntryName)

  private def isZipEntryNameOfXmlFile(zipEntryName: String): Boolean = {
    zipEntryName.endsWith(".xml") || zipEntryName.endsWith(".xsd")
  }

  private def isZipEntryNameInMetaInf(zipEntryName: String): Boolean = zipEntryName.startsWith("META-INF/")

  private val bufferSize = 4096
}

object TaxonomyBaseFactoryFromRemoteZip {

  def apply(createZipInputStream: () => ZipInputStream): TaxonomyBaseFactoryFromRemoteZip = {
    new TaxonomyBaseFactoryFromRemoteZip(createZipInputStream, identity)
  }

  def from(createInputStream: () => InputStream): TaxonomyBaseFactoryFromRemoteZip = {
    apply(() => new ZipInputStream(createInputStream(), Codec.UTF8.charSet)) // Assuming UTF-8
  }

  /**
   * URI resolver from a file data collection and a catalog. The file data collection is a Map from Unix-style
   * ZIP entry names (using the forward slash as file component separator) to immutable byte arrays.
   */
  private class MyUriResolver(val fileDataCollection: ListMap[String, ArraySeq[Byte]], val catalog: SimpleCatalog)
      extends (URI => InputSource) {

    def apply(uri: URI): InputSource = {
      val localUri: URI = catalog.getMappedUri(uri)
      val zipEntryName: String = localUri.toString // Uses forward slash as file component separator

      val bytes: Array[Byte] = fileDataCollection
        .getOrElse(zipEntryName, sys.error(s"Could not find byte array for URI '$uri' (local URI '$localUri')"))
        .toArray
      val bis: ByteArrayInputStream = new ByteArrayInputStream(bytes)

      new InputSource(bis)
    }
  }

  private def convertZipEntryNameUsingForwardSlash(zipEntryName: String): String = {
    zipEntryName.replace("\\", "/")
  }
}
