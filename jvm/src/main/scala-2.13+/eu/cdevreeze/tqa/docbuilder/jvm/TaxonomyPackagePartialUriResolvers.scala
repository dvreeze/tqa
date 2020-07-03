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
      .find(entry => toRelativeUri(entry, dummyDirectory).toString.endsWith("/META-INF/catalog.xml"))
      .getOrElse(sys.error(s"No META-INF/catalog.xml found in taxonomy package ZIP file ${zipFile.getName}"))

    val catalogEntryRelativeUri: URI = toRelativeUri(catalogEntry, dummyDirectory).ensuring(!_.isAbsolute)

    val docParser = DocumentParserUsingStax.newInstance()
    val catalogRootElem: indexed.Elem =
      indexed.Elem(docParser.parse(zipFile.getInputStream(catalogEntry)).documentElement)

    SimpleCatalog.fromElem(catalogRootElem).copy(xmlBaseAttributeOption = Some(catalogEntryRelativeUri))
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
