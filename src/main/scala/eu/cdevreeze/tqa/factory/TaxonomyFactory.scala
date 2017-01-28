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

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.RelationshipFactory
import eu.cdevreeze.tqa.taxonomy.BasicTaxonomy

/**
 * Fluent interface for bootstrapping a taxonomy. The type system helps in getting bootstrapping right.
 *
 * @author Chris de Vreeze
 */
object TaxonomyFactory {

  def withDocumentBuilder(documentBuilder: DocumentBuilder): HasDocumentBuilder = {
    new HasDocumentBuilder(documentBuilder)
  }

  final class HasDocumentBuilder(val documentBuilder: DocumentBuilder) {

    def withDocumentCollector(documentCollector: DocumentCollector): HasDocumentCollector = {
      new HasDocumentCollector(documentBuilder, documentCollector)
    }
  }

  final class HasDocumentCollector(
      val documentBuilder: DocumentBuilder,
      val documentCollector: DocumentCollector) {

    // TODO We miss caching of root elements now. DTS discovery may repeatedly parse the same documents.

    def withRelationshipsFactory(relationshipFactory: RelationshipFactory): Builder = {
      new Builder(documentBuilder, documentCollector, SubstitutionGroupMap.Empty, relationshipFactory, (_ => true))
    }

    def withStrictRelationshipsFactory: Builder = {
      withRelationshipsFactory(DefaultRelationshipFactory.StrictInstance)
    }

    def withLenientRelationshipsFactory: Builder = {
      withRelationshipsFactory(DefaultRelationshipFactory.LenientInstance)
    }
  }

  final class Builder(
      val documentBuilder: DocumentBuilder,
      val documentCollector: DocumentCollector,
      val substitutionGroupMap: SubstitutionGroupMap,
      val relationshipFactory: RelationshipFactory,
      val arcFilter: XLinkArc => Boolean) {

    def withSubstitutionGroupMap(newSubstitutionGroupMap: SubstitutionGroupMap): Builder = {
      new Builder(documentBuilder, documentCollector, newSubstitutionGroupMap, relationshipFactory, arcFilter)
    }

    def withArcFilter(newArcFilter: XLinkArc => Boolean): Builder = {
      new Builder(documentBuilder, documentCollector, substitutionGroupMap, relationshipFactory, newArcFilter)
    }

    def build(): BasicTaxonomy = {
      val taxoRootElems = documentCollector.collectTaxonomyRootElems(documentBuilder)

      val taxonomyBase = TaxonomyBase.build(taxoRootElems)

      BasicTaxonomy.build(taxonomyBase, substitutionGroupMap, relationshipFactory, arcFilter)
    }
  }
}
