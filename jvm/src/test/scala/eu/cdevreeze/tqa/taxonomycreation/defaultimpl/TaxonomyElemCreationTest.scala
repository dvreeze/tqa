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

package eu.cdevreeze.tqa.taxonomycreation.defaultimpl

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Dimensional instance validation test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyElemCreationTest extends FunSuite {

  test("testCreateConceptDeclaration") {
    val scope = taxonomy.guessedScope ++ Scope.from("ex" -> "http://www.example.com")
    val taxoElemCreator = new DefaultTaxonomyElemCreator(taxonomy, scope)

    import scope._

    val conceptDecl: ConceptDeclaration = taxoElemCreator.createConceptDeclaration(
      QName("ex:Assets").res,
      Some(QName("xbrli:stringItemType").res),
      Some(QName("xbrli:item").res),
      Map())

    assertResult(QName("ex:Assets").res) {
      conceptDecl.targetEName
    }

    assertResult(Some(QName("xbrli:stringItemType").res)) {
      conceptDecl.globalElementDeclaration.typeOption
    }

    assertResult(Some(QName("xbrli:item").res)) {
      conceptDecl.substitutionGroupOption
    }
  }

  private val taxonomy: BasicTaxonomy = {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[TaxonomyElemCreationTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_def.xml").toURI,
      classOf[TaxonomyElemCreationTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = TaxonomyBase.build(taxoDocs)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

    richTaxo.ensuring(_.findAllGlobalElementDeclarations.size > 1600)
  }
}
