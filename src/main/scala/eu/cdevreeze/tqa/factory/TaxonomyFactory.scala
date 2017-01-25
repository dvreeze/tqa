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

package eu.cdevreeze.tqa.factory

import java.net.URI

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.BackingElemBuilder
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.relationship.DefaultRelationshipsFactory
import eu.cdevreeze.tqa.relationship.RelationshipsFactory
import eu.cdevreeze.tqa.taxonomy.BasicTaxonomy

/**
 * Fluent interface for bootstrapping a taxonomy. The type system helps in getting bootstrapping right.
 *
 * @author Chris de Vreeze
 */
object TaxonomyFactory {

  def withBackingElemBuilder(backingElemBuilder: BackingElemBuilder): HasBackingElemBuilder = {
    new HasBackingElemBuilder(backingElemBuilder)
  }

  final class HasBackingElemBuilder(val backingElemBuilder: BackingElemBuilder) {

    def withRootElemCollector(rootElemCollector: TaxonomyRootElemCollector): HasTaxonomyRootElemCollector = {
      new HasTaxonomyRootElemCollector(backingElemBuilder, rootElemCollector)
    }
  }

  final class HasTaxonomyRootElemCollector(
      val backingElemBuilder: BackingElemBuilder,
      val rootElemCollector: TaxonomyRootElemCollector) {

    // TODO We miss caching of root elements now. DTS discovery may repeatedly parse the same documents.

    def withRelationshipsFactory(relationshipsFactory: RelationshipsFactory): Builder = {
      new Builder(backingElemBuilder, rootElemCollector, SubstitutionGroupMap.Empty, relationshipsFactory, (_ => true))
    }

    def withStrictRelationshipsFactory: Builder = {
      withRelationshipsFactory(DefaultRelationshipsFactory.StrictInstance)
    }

    def withLenientRelationshipsFactory: Builder = {
      withRelationshipsFactory(DefaultRelationshipsFactory.LenientInstance)
    }
  }

  final class Builder(
      val backingElemBuilder: BackingElemBuilder,
      val rootElemCollector: TaxonomyRootElemCollector,
      val substitutionGroupMap: SubstitutionGroupMap,
      val relationshipsFactory: RelationshipsFactory,
      val arcFilter: XLinkArc => Boolean) {

    def withSubstitutionGroupMap(newSubstitutionGroupMap: SubstitutionGroupMap): Builder = {
      new Builder(backingElemBuilder, rootElemCollector, newSubstitutionGroupMap, relationshipsFactory, arcFilter)
    }

    def withArcFilter(newArcFilter: XLinkArc => Boolean): Builder = {
      new Builder(backingElemBuilder, rootElemCollector, substitutionGroupMap, relationshipsFactory, newArcFilter)
    }

    def build(): BasicTaxonomy = {
      val taxoRootElems = rootElemCollector.collectRootElems(backingElemBuilder)

      val underlyingTaxo = Taxonomy.build(taxoRootElems)

      BasicTaxonomy.build(underlyingTaxo, substitutionGroupMap, relationshipsFactory, arcFilter)
    }
  }
}
