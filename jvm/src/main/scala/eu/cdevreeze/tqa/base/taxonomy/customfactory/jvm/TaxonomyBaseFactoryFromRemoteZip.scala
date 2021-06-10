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
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import scala.collection.immutable.ArraySeq
import scala.collection.immutable.ListMap
import scala.collection.parallel.CollectionConverters._
import scala.util.Using
import scala.util.chaining._

import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyBaseFactoryFromRemoteZip.MyUriResolver
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.ThreadSafeSaxonDocumentBuilder
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor
import org.xml.sax.InputSource

/**
 * TaxonomyBase factory from a remote (or local) taxonomy package ZIP file. The ZIP does not have to be
 * a taxonomy package with META-INF/taxonomyPackage.xml file, but it does need to have a META-INF/catalog.xml
 * file. Moreover, this catalog.xml file must be invertible, so that there is only one original URI per mapped URI!
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBaseFactoryFromRemoteZip(val createZipInputStream: () => ZipInputStream) {

  private val catalogZipEntryName = "META-INF/catalog.xml"

  /**
   * Loads a taxonomy as TaxonomyBase, from the given entrypoint URIs. This is the method that calls all the other
   * methods of this class.
   */
  def loadDts(entrypointUris: Set[URI]): TaxonomyBase = {
    val xmlByteArrays: ListMap[String, ArraySeq[Byte]] = readAllXmlDocuments()

    val catalog: SimpleCatalog = locateAndParseCatalog(xmlByteArrays)

    val docs: IndexedSeq[SaxonDocument] =
      parseAllTaxonomyDocuments(xmlByteArrays, catalog).ensuring(_.forall(_.uriOption.nonEmpty))

    // If the catalog is not invertible, it is likely that DTS discovery will fail!
    val dtsUris: Set[URI] = findDtsUris(entrypointUris, docs)

    val docsInDts: IndexedSeq[SaxonDocument] = docs.filter(d => dtsUris.contains(d.uriOption.get))
    val taxonomyBase: TaxonomyBase = docsInDts.map(TaxonomyDocument.build).pipe(TaxonomyBase.build)
    taxonomyBase
  }

  /**
   * Reads all XML documents in the ZIP stream into memory, not as parsed DOM trees, but as immutable byte arrays.
   * More precisely, the result is a Map from ZIP entry names (following the ZipEntry.getName format) to immutable
   * ArraySeq collections of bytes.
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
          zipEntry.getName -> readZipEntry(zis).tap(_ => zis.closeEntry())
        }
        .to(ListMap)
    }
  }

  /**
   * Finds the "META-INF/catalog.xml" file in the file data collection, throws an exception if not found, and parses
   * the catalog data by calling method "parseCatalog".
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
          val localUri: URI = URI.create(zipEntryName) // TODO ???
          val originalUri: URI = reverseCatalog.getMappedUri(localUri)

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
   * is passed as second parameter.
   */
  def findDtsUris(entrypointUris: Set[URI], allTaxoDocs: Seq[SaxonDocument]): Set[URI] = {
    val allDocDependencies: Seq[DocDependencyList] = allTaxoDocs.map { doc =>
      DocDependencyDiscovery.findDocDependencyList(doc)
    }

    val docDependenciesMap: Map[URI, DocDependencyList] =
      allDocDependencies.groupBy(_.docUri).view.mapValues(_.head).toMap

    val dtsDiscovery: DtsDiscovery = new DtsDiscovery(docDependenciesMap)

    dtsDiscovery.findDts(entrypointUris)
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
    new TaxonomyBaseFactoryFromRemoteZip(createZipInputStream)
  }

  def from(createInputStream: () => InputStream): TaxonomyBaseFactoryFromRemoteZip = {
    apply(() => new ZipInputStream(createInputStream()))
  }

  private class MyUriResolver(val fileDataCollection: ListMap[String, ArraySeq[Byte]], val catalog: SimpleCatalog)
      extends (URI => InputSource) {

    def apply(uri: URI): InputSource = {
      val localUri: URI = catalog.getMappedUri(uri)
      val zipEntryName: String = localUri.toString // TODO ???

      val bytes: Array[Byte] = fileDataCollection
        .getOrElse(zipEntryName, sys.error(s"Could not find byte array for URI '$uri' (local URI '$localUri')"))
        .toArray
      val bis: ByteArrayInputStream = new ByteArrayInputStream(bytes)

      new InputSource(bis)
    }
  }
}
