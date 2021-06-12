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

package eu.cdevreeze.tqa.base.taxonomybuilder

import java.net.URI

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomyFactory
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder

/**
 * Fluent interface for bootstrapping a taxonomy. The type system helps in getting bootstrapping right,
 * if we start with the TaxonomyBuilder companion object.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBuilder(
    val documentBuilder: DocumentBuilder,
    val documentCollector: DocumentCollector,
    val extraSubstitutionGroupMap: SubstitutionGroupMap,
    val relationshipFactory: RelationshipFactory,
    val arcFilter: XLinkArc => Boolean) extends BasicTaxonomyFactory {

  def withExtraSubstitutionGroupMap(newExtraSubstitutionGroupMap: SubstitutionGroupMap): TaxonomyBuilder = {
    new TaxonomyBuilder(
      documentBuilder,
      documentCollector,
      newExtraSubstitutionGroupMap,
      relationshipFactory,
      arcFilter)
  }

  def withRelationshipFactory(newRelationshipFactory: RelationshipFactory): TaxonomyBuilder = {
    new TaxonomyBuilder(
      documentBuilder,
      documentCollector,
      extraSubstitutionGroupMap,
      newRelationshipFactory,
      arcFilter)
  }

  def withArcFilter(newArcFilter: XLinkArc => Boolean): TaxonomyBuilder = {
    new TaxonomyBuilder(
      documentBuilder,
      documentCollector,
      extraSubstitutionGroupMap,
      relationshipFactory,
      newArcFilter)
  }

  /**
   * Builds a `BasicTaxonomy`, passing the entry point URIs to the document collector of this taxonomy builder.
   */
  def build(entryPointUris: Set[URI]): BasicTaxonomy = {
    val taxoDocs = documentCollector.collectTaxonomyDocuments(entryPointUris, documentBuilder)

    val taxonomyBase = TaxonomyBase.build(taxoDocs)

    BasicTaxonomy.build(taxonomyBase, extraSubstitutionGroupMap, relationshipFactory, arcFilter)
  }
}

/**
 * Fluent type-safe API to create a TaxonomyBuilder. Note that typically it makes sense to create a TaxonomyBuilder once,
 * and keep it, using it to create taxonomies for different entrypoints. Hence, creating a TaxonomyBuilder is typically
 * rather easy, given a DocumentBuilder. Mostly it is the creation of the DocumentBuilder that can be challenging, due to
 * the URI resolution strategy to pass, and possibly also due to the use of a document cache.
 */
object TaxonomyBuilder {

  def withDocumentBuilder(documentBuilder: DocumentBuilder): HasDocumentBuilder = {
    // Consider (re-)using a DocumentBuilder that is a CachingDocumentBuilder, for obvious reasons.

    new HasDocumentBuilder(documentBuilder)
  }

  final class HasDocumentBuilder(val documentBuilder: DocumentBuilder) {

    def withDocumentCollector(documentCollector: DocumentCollector): HasDocumentCollector = {
      new HasDocumentCollector(documentBuilder, documentCollector)
    }

    def withDefaultDtsCollector: HasDocumentCollector = {
      withDocumentCollector(DefaultDtsCollector.acceptingEmptyUriSet(false))
    }
  }

  final class HasDocumentCollector(val documentBuilder: DocumentBuilder, val documentCollector: DocumentCollector) {

    def withRelationshipFactory(relationshipFactory: RelationshipFactory): TaxonomyBuilder = {
      new TaxonomyBuilder(
        documentBuilder,
        documentCollector,
        SubstitutionGroupMap.Empty,
        relationshipFactory,
        (_ => true))
    }

    def withStrictRelationshipFactory: TaxonomyBuilder = {
      withRelationshipFactory(DefaultRelationshipFactory.StrictInstance)
    }

    def withLenientRelationshipFactory: TaxonomyBuilder = {
      withRelationshipFactory(DefaultRelationshipFactory.LenientInstance)
    }
  }
}
