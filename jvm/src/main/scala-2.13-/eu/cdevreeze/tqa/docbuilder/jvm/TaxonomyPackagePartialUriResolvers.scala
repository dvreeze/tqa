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

package eu.cdevreeze.tqa.docbuilder.jvm

import java.io.File
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import org.xml.sax.InputSource

import scala.collection.JavaConverters._

/**
 * Partial URI resolvers specifically for a taxonomy package
 * (see https://www.xbrl.org/Specification/taxonomy-package/REC-2016-04-19/taxonomy-package-REC-2016-04-19.html).
 *
 * @author Chris de Vreeze
 */
object TaxonomyPackagePartialUriResolvers {

  import PartialUriConverters.PartialUriConverter
  import PartialUriResolvers.PartialUriResolver

  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile): PartialUriResolver = {
    val zipEntriesByRelativeUri: Map[URI, ZipEntry] = computeZipEntryMap(taxonomyPackageZipFile)

    val catalog: SimpleCatalog = parseCatalog(taxonomyPackageZipFile, zipEntriesByRelativeUri)
    val partialUriConverter: PartialUriConverter = PartialUriConverters.fromCatalog(catalog)

    def resolveUri(uri: URI): Option[InputSource] = {
      val mappedUriOption = partialUriConverter(uri)

      mappedUriOption.map { mappedUri =>
        require(!mappedUri.isAbsolute, s"Cannot resolve absolute URI '$mappedUri'")

        val optionalZipEntry: Option[ZipEntry] = zipEntriesByRelativeUri.get(mappedUri)

        require(
          optionalZipEntry.isDefined,
          s"Missing ZIP entry in ZIP file $taxonomyPackageZipFile with URI $mappedUri")

        val is = taxonomyPackageZipFile.getInputStream(optionalZipEntry.get)

        new InputSource(is)
      }
    }

    resolveUri
  }

  private def parseCatalog(zipFile: ZipFile, zipEntriesByRelativeUri: Map[URI, ZipEntry]): SimpleCatalog = {
    val catalogEntryRelativeUri: URI = zipEntriesByRelativeUri
      .find { case (uri, _) => uri.toString.endsWith("/META-INF/catalog.xml") }
      .map(_._1)
      .getOrElse(sys.error(s"No META-INF/catalog.xml found in taxonomy package ZIP file ${zipFile.getName}"))

    val catalogEntry: ZipEntry = zipEntriesByRelativeUri.apply(catalogEntryRelativeUri)

    val docParser = DocumentParserUsingStax.newInstance()
    val catalogRootElem: indexed.Elem =
      indexed.Elem(docParser.parse(zipFile.getInputStream(catalogEntry)).documentElement)

    SimpleCatalog.fromElem(catalogRootElem).copy(xmlBaseAttributeOption = Some(catalogEntryRelativeUri))
  }

  private def computeZipEntryMap(zipFile: ZipFile): Map[URI, ZipEntry] = {
    val zipEntries = zipFile.entries().asScala.toIndexedSeq

    val zipFileParent = dummyDirectory

    zipEntries.map(e => toRelativeUri(e, zipFileParent) -> e).toMap
  }

  private def toRelativeUri(zipEntry: ZipEntry, zipFileParent: File): URI = {
    val adaptedZipEntryUri = new File(zipFileParent, zipEntry.getName).toURI
    val zipFileParentUri = URI.create(returnWithTrailingSlash(zipFileParent.toURI))
    val relativeZipEntryUri = zipFileParentUri.relativize(adaptedZipEntryUri)
    require(!relativeZipEntryUri.isAbsolute, s"Not a relative URI: $relativeZipEntryUri")

    relativeZipEntryUri
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }

  private val dummyDirectory = new File("/dummyRoot")
}
