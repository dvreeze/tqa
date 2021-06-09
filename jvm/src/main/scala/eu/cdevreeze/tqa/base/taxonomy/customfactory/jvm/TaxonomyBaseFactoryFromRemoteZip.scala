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
import scala.util.Using
import scala.util.chaining._

import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor

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
   * Loads a taxonomy as TaxonomyBase, from the given entrypoint URIs.
   *
   * Implementation note: this method first calls method parseCatalog, then method findDtsUris, and
   * finally does the real work by calling method createTaxonomyBase.
   */
  def loadDts(entrypointUris: Set[URI]): TaxonomyBase = {
    val catalog: SimpleCatalog = parseCatalog()
    val docs: IndexedSeq[SaxonDocument] = loadAllDocuments(catalog).ensuring(_.forall(_.uriOption.nonEmpty))

    // TODO Check roundtripping of document URIs.

    val dtsUris: Set[URI] = findDtsUris(entrypointUris, docs)

    val docsInDts: IndexedSeq[SaxonDocument] = docs.filter(d => dtsUris.contains(d.uriOption.get))
    val taxonomyBase: TaxonomyBase = docsInDts.map(TaxonomyDocument.build).pipe(TaxonomyBase.build)
    taxonomyBase
  }

  /**
   * Parses the catalog, which is the first step in the workflow.
   */
  def parseCatalog(): SimpleCatalog = {
    val catalogBytes: ArraySeq[Byte] =
      Using
        .resource(createZipInputStream()) { zis =>
          Iterator
            .continually(zis.getNextEntry())
            .takeWhile(_ != null)
            .map { zipEntry =>
              if (zipEntry.getName == catalogZipEntryName) {
                assert(!zipEntry.isDirectory)
                Some(readZipEntry(zis))
              } else {
                None
              }.tap(_ => zis.closeEntry())
            }
            .collectFirst { case Some(bytes) => bytes }
            .getOrElse(sys.error(s"Missing ZIP entry '$catalogZipEntryName'"))
        }

    val docParser = DocumentParserUsingStax.newInstance()
    // A relative document URI, which is allowed for indexed/simple documents!
    val docUri: URI = URI.create(catalogZipEntryName)
    val catalogRootElem: indexed.Elem =
      indexed.Elem(docUri, docParser.parse(new ByteArrayInputStream(catalogBytes.toArray)).documentElement)

    SimpleCatalog.fromElem(catalogRootElem)
  }

  /**
   * Loads all (taxonomy) documents, without knowing the DTS yet. It is required that the passed XML catalog
   * is invertible, or else this method does not work. This is step 2 in the workflow. After this step
   * all documents there in order to compute the DTS and create the taxonomy (from a document subset).
   */
  def loadAllDocuments(catalog: SimpleCatalog): IndexedSeq[SaxonDocument] = {
    val reverseCatalog: SimpleCatalog = catalog.reverse

    val processor: Processor = new Processor(false) // Always a new Processor needed?

    val docs: IndexedSeq[SaxonDocument] =
      Using.resource(createZipInputStream()) { zis =>
        val docBuilder: ZipDocumentBuilder = new ZipDocumentBuilder(zis, processor, reverseCatalog)

        Iterator
          .continually(zis.getNextEntry())
          .takeWhile(_ != null)
          .flatMap { zipEntry =>
            if (zipEntry.isDirectory || zipEntry.getName().startsWith("META-INF/")) {
              None
            } else {
              val originalUri: URI = reverseCatalog.getMappedUri(URI.create(zipEntry.getName)) // TODO ???

              // Only sequential processing?
              Some(docBuilder.build(originalUri)).tap(_ => zis.closeEntry())
            }
          }
          .toIndexedSeq
      }

    docs
  }

  /**
   * Finds all URIs in the DTS given the entrypoint URIs passed. A superset of the DTS as document collection
   * is passed as second parameter.
   */
  def findDtsUris(entrypointUris: Set[URI], allDocs: Seq[SaxonDocument]): Set[URI] = {
    val allDocDependencies: Seq[DocDependencyList] = allDocs.map { doc =>
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
    bos.toByteArray.to(ArraySeq)
  }

  private val bufferSize = 4096
}

object TaxonomyBaseFactoryFromRemoteZip {

  def apply(createZipInputStream: () => ZipInputStream): TaxonomyBaseFactoryFromRemoteZip = {
    new TaxonomyBaseFactoryFromRemoteZip(createZipInputStream)
  }

  def from(createInputStream: () => InputStream): TaxonomyBaseFactoryFromRemoteZip = {
    apply(() => new ZipInputStream(createInputStream()))
  }
}
