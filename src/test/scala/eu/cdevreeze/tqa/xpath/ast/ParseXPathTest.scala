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

import XPathExpressions.FunctionCall
import XPathExpressions.IfExpr
import XPathExpressions.SimpleNameTest

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
