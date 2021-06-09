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

import java.net.URI
import java.util.zip.ZipInputStream

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy

/**
 * BasicTaxonomy factory from a remote (or local) taxonomy package ZIP file. The ZIP does not have to be
 * a taxonomy package with META-INF/taxonomyPackage.xml file, but it does need to have a META-INF/catalog.xml
 * file.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyFactoryFromRemoteZip(
    val createZipInputStream: () => ZipInputStream,
    val extraSubstitutionGroupMap: SubstitutionGroupMap,
    val relationshipFactory: RelationshipFactory,
    val arcFilter: XLinkArc => Boolean) {

  val taxonomyBaseFactory: TaxonomyBaseFactoryFromRemoteZip = TaxonomyBaseFactoryFromRemoteZip(createZipInputStream)

  // TODO Functional copies (like builder pattern)

  /**
   * Builds a `BasicTaxonomy` from the data available to this taxonomy factory, as well as the passed entrypoint URIs.
   */
  def build(entryPointUris: Set[URI]): BasicTaxonomy = {
    val taxonomyBase: TaxonomyBase = taxonomyBaseFactory.loadDts(entryPointUris)

    BasicTaxonomy.build(taxonomyBase, extraSubstitutionGroupMap, relationshipFactory, arcFilter)
  }
}

object TaxonomyFactoryFromRemoteZip {

  def apply(
      createZipInputStream: () => ZipInputStream,
      extraSubstitutionGroupMap: SubstitutionGroupMap,
      relationshipFactory: RelationshipFactory,
      arcFilter: XLinkArc => Boolean): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(createZipInputStream, extraSubstitutionGroupMap, relationshipFactory, arcFilter)
  }
}
