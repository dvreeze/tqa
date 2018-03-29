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

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Dimensional instance validation test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyElemCreationTest extends FunSuite {

  test("testCreateConceptDeclaration") {
    val taxoElemCreator = new DefaultTaxonomyElemCreator(taxonomy)

    val scope = taxonomy.guessedScope ++ Scope.from("ex" -> "http://www.example.com")
    import scope._

    val conceptDecl: ConceptDeclaration =
      taxoElemCreator.createConceptDeclaration(
        QName("ex:Assets").res,
        Some(QName("xbrli:stringItemType").res),
        Some(QName("xbrli:item").res),
        Map(
          ENames.AbstractEName -> "false",
          ENames.IdEName -> "ex_Assets",
          ENames.NillableEName -> "false",
          ENames.XbrliBalanceEName -> "credit",
          ENames.XbrliPeriodTypeEName -> "duration"),
        scope.filterKeys(Set("ex", "xbrli")))

    assertResult(QName("ex:Assets").res) {
      conceptDecl.targetEName
    }

    assertResult(Some(QName("xbrli:stringItemType").res)) {
      conceptDecl.globalElementDeclaration.typeOption
    }

    assertResult(Some(QName("xbrli:item").res)) {
      conceptDecl.substitutionGroupOption
    }

    assertResult(Some(PeriodType.Duration)) {
      conceptDecl.globalElementDeclaration.periodTypeOption
    }
  }

  private val taxoBuilder: TaxonomyBuilder = {
    val docParser = DocumentParserUsingStax.newInstance()

    val documentBuilder =
      new IndexedDocumentBuilder(docParser, UriResolvers.fromUriConverter(UriConverters.identity))

    TaxonomyBuilder
      .withDocumentBuilder(documentBuilder)
      .withDocumentCollector(TrivialDocumentCollector)
      .withLenientRelationshipFactory
  }

  private val taxonomy: BasicTaxonomy = {
    val docUris = Set(
      classOf[TaxonomyElemCreationTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_def.xml").toURI,
      classOf[TaxonomyElemCreationTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val taxo = taxoBuilder.build(docUris)

    taxo.ensuring(_.findAllGlobalElementDeclarations.size > 1600)
  }
}
