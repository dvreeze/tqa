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

import eu.cdevreeze.tqa.ENameValue
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.aspect.AspectModel
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.model.CommonRelationshipAttributes
import eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter
import eu.cdevreeze.tqa.extension.formula.model.DimensionFilterMember
import eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter
import eu.cdevreeze.tqa.extension.formula.model.FactVariable
import eu.cdevreeze.tqa.extension.formula.model.VariableFilter
import eu.cdevreeze.tqa.extension.formula.model.VariableSetVariableOrParameter
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
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

    assertResult(1) {
      formulaTaxo.findAllExistenceAssertions.size
    }

    val domExistenceAssertion = formulaTaxo.findAllExistenceAssertions.head

    val existenceAssertion: model.ExistenceAssertion =
      variableSetConverter.tryToConvertExistenceAssertion(domExistenceAssertion).get

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

    val Elr = "urn:kvk:linkrole:balance-sheet-banks"

    val varSetVarOrPar @ VariableSetVariableOrParameter(
      CommonRelationshipAttributes(elr, order, priority, use),
      ename,
      factVariable @ FactVariable(_, _, _, _, variableFilters)) = varSetVariablesOrParameters.head

    assertResult(Elr) {
      elr
    }
    assertResult(EName(None, "varArc_BalanceSheetBanks_MsgAdimExistence1_BalanceSheetBeforeAfterAppropriationResults")) {
      ename
    }
    assertResult(BigDecimal(1)) {
      order
    }
    assertResult(0) {
      priority
    }
    assertResult(Use.Optional) {
      use
    }

    assertResult(false) {
      factVariable.bindAsSequence
    }
    assertResult(Some("0")) {
      factVariable.fallbackValueExprOption.map(_.xpathExpression)
    }
    assertResult(Some(false)) {
      factVariable.matchesOption
    }
    assertResult(Some(false)) {
      factVariable.nilsOption
    }

    assertResult(1) {
      variableFilters.size
    }

    val Seq(
      variableFilter @ VariableFilter(
        CommonRelationshipAttributes(elr2, order2, priority2, use2),
        complement2,
        cover2,
        filter2 @ ConceptNameFilter(conceptNamesOrExprs))) = variableFilters

    assertResult(Elr) {
      elr2
    }
    assertResult(false) {
      complement2
    }
    assertResult(true) {
      cover2
    }
    assertResult(BigDecimal(1)) {
      order2
    }
    assertResult(0) {
      priority2
    }
    assertResult(Use.Optional) {
      use2
    }

    val guessedScope = taxo.guessedScope

    import guessedScope._

    assertResult(1) {
      conceptNamesOrExprs.size
    }
    assertResult(Seq(ENameValue(QName("venj-bw2-i:BalanceSheetBeforeAfterAppropriationResults").res))) {
      conceptNamesOrExprs
    }
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

    assertResult(17) {
      formulaTaxo.findAllValueAssertions.size
    }
    assertResult(formulaTaxo.findAllValueAssertions.size) {
      formulaTaxo.findAllValueAssertions.
        flatMap(va => variableSetConverter.tryToConvertValueAssertion(va).toOption).size
    }

    val domValueAssertion =
      formulaTaxo.findAllValueAssertions.find(_.underlyingResource.idOption.contains("valueAssertion_BalanceSheetBanks_MsgSeparateSumOfChildrenParentDebit1")).get

    val valueAssertion: model.ValueAssertion =
      variableSetConverter.tryToConvertValueAssertion(domValueAssertion).get

    assertResult(AspectModel.DimensionalAspectModel) {
      valueAssertion.aspectModel
    }
    assertResult(true) {
      valueAssertion.implicitFiltering
    }

    assertResult(" $Assets = - sum($varArc_BalanceSheetBanks_MsgSeparateSumOfChildrenParentDebit1_ChildrenOfAssetsCredit)+ sum($varArc_BalanceSheetBanks_MsgSeparateSumOfChildrenParentDebit1_ChildrenOfAssetsDebit)") {
      valueAssertion.testExpr.xpathExpression
    }

    assertResult(Nil) {
      valueAssertion.variableSetPreconditions
    }
    assertResult(Nil) {
      valueAssertion.variableSetFilters
    }

    assertResult(3) {
      valueAssertion.variableSetVariablesOrParameters.size
    }

    val Elr = "urn:kvk:linkrole:balance-sheet-banks"

    val varSetVarOrPar @ VariableSetVariableOrParameter(
      CommonRelationshipAttributes(elr, order, priority, use),
      ename,
      factVariable @ FactVariable(_, _, _, _, variableFilters)) = valueAssertion.variableSetVariablesOrParameters.head

    assertResult(Elr) {
      elr
    }
    assertResult(EName(None, "Assets")) {
      ename
    }
    assertResult(BigDecimal(1)) {
      order
    }
    assertResult(0) {
      priority
    }
    assertResult(Use.Optional) {
      use
    }

    assertResult(false) {
      factVariable.bindAsSequence
    }
    assertResult(None) {
      factVariable.fallbackValueExprOption.map(_.xpathExpression)
    }
    assertResult(Some(false)) {
      factVariable.matchesOption
    }
    assertResult(Some(false)) {
      factVariable.nilsOption
    }

    assertResult(3) {
      variableFilters.size
    }

    assertResult(Set(false)) {
      variableFilters.map(_.complement).toSet
    }
    assertResult(Set(true)) {
      variableFilters.map(_.cover).toSet
    }
    assertResult(Set(BigDecimal(1), BigDecimal(2))) {
      variableFilters.map(_.commonAttributes.order).toSet
    }

    val explicitDimensionFilter @ ExplicitDimensionFilter(dim, mems) =
      variableFilters.filter(_.commonAttributes.order == BigDecimal(2)).head.filter

    val guessedScope = taxo.guessedScope

    import guessedScope._

    assertResult(ENameValue(QName("venj-bw2-dim:FinancialStatementsTypeAxis").res)) {
      dim
    }
    assertResult(List(DimensionFilterMember(
      ENameValue(QName("venj-bw2-dm:SeparateMember").res), None, None, None))) {

      mems
    }
  }
}
