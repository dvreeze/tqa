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

package eu.cdevreeze.tqa.xpath.ast

import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import XPathExpressions.AdditionOp
import XPathExpressions.ExprSingle
import XPathExpressions.FunctionCall
import XPathExpressions.GeneralComp
import XPathExpressions.IfExpr
import XPathExpressions.IntegerLiteral
import XPathExpressions.SimpleNameTest
import XPathExpressions.StringLiteral
import XPathExpressions.ValueComp
import XPathExpressions.VarRef
import XPathExpressions.UnaryOp

/**
 * XPath parsing test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class ParseXPathTest extends FunSuite {

  import fastparse.all.Parsed

  import XPathParser.xpathExpr

  test("testParseSlash") {
    val exprString = "/"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)
  }

  test("testParseDoubleSlash") {
    val exprString = "//"

    val parseResult = xpathExpr.parse(exprString)

    assertFailure(parseResult)
  }

  test("testParseSimplePathExpr") {
    val exprString = "/p:a//p:b/p:c//p:d/p:e"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    val simpleNameTests = parseResult.get.value.findAllElemsOfType(classTag[SimpleNameTest])

    assertResult(List(
      QNameAsEQName("p:a"),
      QNameAsEQName("p:b"),
      QNameAsEQName("p:c"),
      QNameAsEQName("p:d"),
      QNameAsEQName("p:e"))) {

      simpleNameTests.map(e => e.name)
    }
  }

  test("testParseSimplePathExprWithError") {
    val exprString = "/p:a//p:b/p:c//p:d/p:e{"

    val parseResult = xpathExpr.parse(exprString)

    assertFailure(parseResult)
  }

  test("testParseIfExprWithFunctionCalls") {
    // From the NL taxonomy (NT12)

    val exprString =
      "if(xff:has-fallback-value(xs:QName('varArc_BalanceSheetVertical_MsgPrecondValueConceptAndNoExistenceConcept1_ResultForTheYear'))) " +
        "then true() else not(count($varArc_BalanceSheetVertical_MsgPrecondValueConceptAndNoExistenceConcept1_ResultForTheYear) ge 1)"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[IfExpr]).size
    }

    assertResult(5) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[StringLiteral]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[IntegerLiteral]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[ValueComp.Ge.type]).size
    }

    assertResult(List(QNameAsEQName("varArc_BalanceSheetVertical_MsgPrecondValueConceptAndNoExistenceConcept1_ResultForTheYear"))) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).map(_.varName)
    }
  }

  test("testParseSummation") {
    // From the NL taxonomy (NT12)

    val exprString =
      "$varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSumOfMembersOnAbstract1_Abstract_SumOfMembers =  " +
        "sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSumOfMembersOnAbstract1_Abstract_ChildrenMember) "

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[GeneralComp.Eq.type]).size
    }

    assertResult(2) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).size
    }

    assertResult(List(
      QNameAsEQName("varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSumOfMembersOnAbstract1_Abstract_SumOfMembers"),
      QNameAsEQName("varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSumOfMembersOnAbstract1_Abstract_ChildrenMember"))) {

      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).map(_.varName)
    }
  }

  test("testParseLargeSummation") {
    // From the NL taxonomy (NT12)

    val exprString =
      "+ sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesIssue)" +
        "  + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesPurchase)" +
        " + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesGranting)" +
        " + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesSale)" +
        " + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesWithdrawal)" +
        " + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesDistributionAsDividend)" +
        " + sum($varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesOtherMovements)" +
        " =  $varArc_NotesShareCapitalStatementOfChanges_MsgSeparateSevenVariables1_ShareCapitalNumberSharesMovement "

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[GeneralComp.Eq.type]).size
    }

    assertResult(8) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }

    assertResult(7) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).size
    }

    assertResult(true) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).map(_.varName).
        forall(nm => nm.toString.startsWith("varArc_NotesShareCapitalStatementOfChanges"))
    }
  }

  test("testParseIfExprWithFunctionCallsAndStringLiterals") {
    // From the NL taxonomy (NT12)

    val exprString =
      "xfi:fact-has-explicit-dimension-value($varArc_DocumentInformation_MsgPrecondExistenceMemberAspect3_AllItems," +
        "xs:QName('venj-bw2-dim:FinancialStatementsTypeAxis'),xs:QName('venj-bw2-dm:SeparateMember'))"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(3) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }

    assertResult(2) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[StringLiteral]).size
    }

    assertResult(List(QNameAsEQName("varArc_DocumentInformation_MsgPrecondExistenceMemberAspect3_AllItems"))) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).map(_.varName)
    }
  }

  test("testParseComplexIf") {
    // From the NL taxonomy (NT12)

    val exprString =
      "if(xfi:is-instant-period(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)))then " +
        "((xfi:period-instant(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "xs:dateTime($FinancialReportingPeriodPrePreviousEndDateParam)+ xs:dayTimeDuration('PT24H'))or " +
        "(xfi:period-instant(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "xs:dateTime($FinancialReportingPeriodPreviousEndDateParam)+ xs:dayTimeDuration('PT24H'))or " +
        "(xfi:period-instant(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "xs:dateTime($FinancialReportingPeriodCurrentEndDateParam)+ xs:dayTimeDuration('PT24H')))else " +
        "(if(xfi:is-start-end-period(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)))then " +
        "(((xfi:period-end(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "(xs:dateTime($FinancialReportingPeriodPreviousEndDateParam)+ xs:dayTimeDuration('PT24H'))) and " +
        "(xfi:period-start(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "(xs:dateTime($FinancialReportingPeriodPreviousStartDateParam))))or " +
        "((xfi:period-end(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "(xs:dateTime($FinancialReportingPeriodCurrentEndDateParam)+ xs:dayTimeDuration('PT24H'))) and " +
        "(xfi:period-start(xfi:period($varArc_DocumentInformation_MsgContextDatesParamPPEtCE1_AllItems)) = " +
        "(xs:dateTime($FinancialReportingPeriodCurrentStartDateParam)))))else (false()))"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(2) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[IfExpr]).size
    }

    assertResult(Set(
      QNameAsEQName("xfi:is-instant-period"),
      QNameAsEQName("xfi:period"),
      QNameAsEQName("xfi:period-instant"),
      QNameAsEQName("xs:dateTime"),
      QNameAsEQName("xs:dayTimeDuration"),
      QNameAsEQName("xfi:is-start-end-period"),
      QNameAsEQName("xfi:period-end"),
      QNameAsEQName("xfi:period-start"),
      QNameAsEQName("false"))) {

      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).map(_.functionName).toSet
    }

    assertResult(16) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }
  }

  test("testParseExprWithUnaryOp") {
    // From the NL taxonomy (NT12)

    val exprString =
      " $AuditorsFees = - sum($varArc_NotesAuditorsFeesBreakdown_MsgSeparateSumOfChildrenParentDebitDimensionFilter1_ChildrenOfAuditorsFeesCredit)+ " +
        "sum($varArc_NotesAuditorsFeesBreakdown_MsgSeparateSumOfChildrenParentDebitDimensionFilter1_ChildrenOfAuditorsFeesDebit)"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(3) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[VarRef]).size
    }

    assertResult(2) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[FunctionCall]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[UnaryOp.Minus.type]).size
    }

    assertResult(1) {
      parseResult.get.value.findAllElemsOrSelfOfType(classTag[AdditionOp.Plus.type]).size
    }
  }

  test("testParseExprWithStringLiteral") {
    // From the NL taxonomy (NT12)

    val exprString =
      "xfi:identifier-scheme(xfi:identifier($varArc_EntityInformation_MsgEqualToIdentifierScheme1_AllItems)) eq 'http://www.kvk.nl/kvk-id'"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    assertResult(1) {
      parseResult.get.value.findAllElemsOfType(classTag[ValueComp.Eq.type]).size
    }

    assertResult(Set(QNameAsEQName("varArc_EntityInformation_MsgEqualToIdentifierScheme1_AllItems"))) {
      parseResult.get.value.findAllElemsOfType(classTag[VarRef]).map(_.varName).toSet
    }

    assertResult(Set(QNameAsEQName("xfi:identifier"), QNameAsEQName("xfi:identifier-scheme"))) {
      parseResult.get.value.findAllElemsOfType(classTag[FunctionCall]).map(_.functionName).toSet
    }

    assertResult(Set("http://www.kvk.nl/kvk-id")) {
      parseResult.get.value.findAllElemsOfType(classTag[StringLiteral]).map(_.value).toSet
    }
  }

  test("testMultipleExprSingles") {
    val exprString = "/p:a/p:b[@xlink:type = 'arc'], if (//p:c) then //p:c else //p:d"

    val parseResult = xpathExpr.parse(exprString)

    assertSuccess(parseResult)

    val topmostExprSingles = parseResult.get.value.findAllTopmostElemsOrSelfOfType(classTag[ExprSingle])

    assertResult(2) {
      topmostExprSingles.size
    }

    assertResult(Set(QNameAsEQName("p:a"), QNameAsEQName("p:b"), QNameAsEQName("xlink:type"))) {
      topmostExprSingles(0).findAllElemsOfType(classTag[SimpleNameTest]).map(_.name).toSet
    }

    assertResult(Set(QNameAsEQName("p:c"), QNameAsEQName("p:d"))) {
      topmostExprSingles(1).findAllElemsOfType(classTag[SimpleNameTest]).map(_.name).toSet
    }
  }

  private def assertSuccess(parseResult: Parsed[_]): Unit = {
    assertResult(true) {
      parseResult.fold(
        (parser, pos, extra) => false,
        (expr, pos) => true)
    }
  }

  private def assertFailure(parseResult: Parsed[_]): Unit = {
    assertResult(false) {
      parseResult.fold(
        (parser, pos, extra) => false,
        (expr, pos) => true)
    }
  }
}
