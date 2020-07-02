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

import java.util.zip.ZipFile

import scala.collection.immutable

/**
 * URI resolvers specifically for taxonomy packages
 * (see https://www.xbrl.org/Specification/taxonomy-package/REC-2016-04-19/taxonomy-package-REC-2016-04-19.html).
 *
 * @author Chris de Vreeze
 */
object TaxonomyPackageUriResolvers {

  import UriResolvers.UriResolver

  def forTaxonomyPackages(taxonomyPackageZipFiles: immutable.IndexedSeq[ZipFile]): UriResolver = {
    require(taxonomyPackageZipFiles.nonEmpty, s"Expected at least one taxonomy package ZIP file, but did not get any")

    val partialUriResolvers: immutable.IndexedSeq[PartialUriResolvers.PartialUriResolver] = taxonomyPackageZipFiles.map { zipFile =>
      TaxonomyPackagePartialUriResolvers.forTaxonomyPackage(zipFile)
    }
    UriResolvers.fromPartialUriResolversWithoutFallback(partialUriResolvers)
  }

  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile): UriResolver = {
    forTaxonomyPackages(immutable.IndexedSeq(taxonomyPackageZipFile))
  }
}
