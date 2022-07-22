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

import scala.jdk.CollectionConverters._

/**
 * Partial URI resolvers specifically for a taxonomy package
 * (see https://www.xbrl.org/Specification/taxonomy-package/REC-2016-04-19/taxonomy-package-REC-2016-04-19.html).
 *
 * @author Chris de Vreeze
 */
object TaxonomyPackagePartialUriResolvers {

  import PartialUriResolvers.PartialUriResolver

  /**
   * Creates a PartialUriResolver from a ZIP file (as ZipFile instance) containing a taxonomy package with XML catalog.
   * Consider managing the ZipFile resource with the scala.util.Using API (for Scala 2.13, with ports to Scala 2.12 available as well).
   */
  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile): PartialUriResolver = {
    val catalog: SimpleCatalog = parseCatalog(taxonomyPackageZipFile)

    PartialUriResolvers.forZipFileUsingCatalog(taxonomyPackageZipFile, catalog)
  }

  private def parseCatalog(zipFile: ZipFile): SimpleCatalog = {
    val catalogEntry: ZipEntry = zipFile
      .entries()
      .asScala
      .find(entry => entry.getName.endsWith("catalog.xml"))
      .getOrElse(sys.error(s"No META-INF/catalog.xml found in taxonomy package ZIP file ${zipFile.getName}"))

    val catalogEntryRelativeUri: URI = toRelativeUri(catalogEntry).ensuring(!_.isAbsolute)

    val docParser = DocumentParserUsingStax.newInstance()
    val docUri: URI = catalogEntryRelativeUri // A relative document URI, which is allowed for indexed/simple documents!
    val catalogRootElem: indexed.Elem =
      indexed.Elem(docUri, docParser.parse(zipFile.getInputStream(catalogEntry)).documentElement)

    SimpleCatalog.fromElem(catalogRootElem).ensuring(_.docUriOption.contains(docUri))
  }

  /**
   * Using method "URI.relativize", turns the ZIP entry's location into a relative URI, relative to the
   * "root" directory inside the ZIP file.
   */
  private def toRelativeUri(zipEntry: ZipEntry): URI = {
    // Fake constant ZIP file parent URI.
    val dummyZipFileParentDirectory = new File("/dummyRoot")

    val adaptedZipEntryUri = new File(dummyZipFileParentDirectory, zipEntry.getName).toURI
    val zipFileParentUri = URI.create(returnWithTrailingSlash(dummyZipFileParentDirectory.toURI))

    // Having an adapted ZIP entry URI relative to the ZIP file parent URI, we can use the latter to
    // "relativize" the former, getting a relative URI as result.
    val relativeZipEntryUri = zipFileParentUri.relativize(adaptedZipEntryUri)
    require(!relativeZipEntryUri.isAbsolute, s"Not a relative URI: $relativeZipEntryUri")

    relativeZipEntryUri
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }
}
