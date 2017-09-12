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

package eu.cdevreeze.tqa.extension.formula.taxonomymodel

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.AspectModel
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.model.VariableSetVariableOrParameter
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Formula query API test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class FormulaQueryApiTest extends FunSuite {

  test("testQueryExistenceAssertions") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[FormulaQueryApiTest].getResource("/taxonomies/www.nltaxonomie.nl/nt11/kvk/20170419/validation/kvk-balance-sheet-banks-for.xml").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val taxoBase = TaxonomyBase.build(taxoRootElems)
    val taxo = BasicTaxonomy.build(taxoBase, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

    val formulaTaxo = BasicFormulaTaxonomy.build(taxo)

    val variableSetConverter = new VariableSetConverter(formulaTaxo)

    val guessedScope = taxo.guessedScope

    import guessedScope._

    assertResult(1) {
      formulaTaxo.findAllExistenceAssertions.size
    }

    val domExistenceAssertion = formulaTaxo.findAllExistenceAssertions.head

    val existenceAssertion: model.ExistenceAssertion =
      variableSetConverter.convertExistenceAssertion(domExistenceAssertion).toOption.get

    assertResult(AspectModel.DimensionalAspectModel) {
      existenceAssertion.aspectModel
    }
    assertResult(true) {
      existenceAssertion.implicitFiltering
    }

    val varSetVariablesOrParameters = existenceAssertion.variableSetVariablesOrParameters

    assertResult(1) {
      varSetVariablesOrParameters.size
    }

    assertResult(true) {
      varSetVariablesOrParameters match {
        case Seq(
          varSetVarOrPar @ VariableSetVariableOrParameter(
            _,
            _,
            _,
            _)) if varSetVarOrPar.priority >= 0 => true
        case _ => false
      }
    }

    // TODO Name must be stored, and must be:
    // QName("varArc_BalanceSheetBanks_MsgAdimExistence1_BalanceSheetBeforeAfterAppropriationResults").res

    // TODO Proceed with querying and testing
  }

  test("testQueryValueAssertions") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[FormulaQueryApiTest].getResource("/taxonomies/www.nltaxonomie.nl/nt11/kvk/20170419/validation/kvk-balance-sheet-banks-for.xml").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val taxoBase = TaxonomyBase.build(taxoRootElems)
    val taxo = BasicTaxonomy.build(taxoBase, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

    val formulaTaxo = BasicFormulaTaxonomy.build(taxo)

    val variableSetConverter = new VariableSetConverter(formulaTaxo)

    // val guessedScope = taxo.guessedScope

    // import guessedScope._

    assertResult(17) {
      formulaTaxo.findAllValueAssertions.size
    }
    assertResult(formulaTaxo.findAllValueAssertions.size) {
      formulaTaxo.findAllValueAssertions.
        flatMap(va => variableSetConverter.convertValueAssertion(va).toOption).size
    }

    // TODO Proceed with querying and testing
  }
}
