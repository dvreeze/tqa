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

package eu.cdevreeze.tqa.xpathaware

import eu.cdevreeze.tqa.StringExpr
import eu.cdevreeze.tqa.StringValue
import eu.cdevreeze.tqa.StringValueOrExpr
import eu.cdevreeze.tqa.xpath.XPathEvaluator

/**
 * XPath-aware evaluator of a StringValueOrExpr. XPath evaluation is performed without any context item.
 * Make sure to use an XPathEvaluator that knows about the needed namespace bindings in the XPath expressions.
 *
 * @author Chris de Vreeze
 */
object StringValueOrExprEvaluator extends ValueOrExprEvaluator[String] {

  type ValueOrExprType = StringValueOrExpr

  /**
   * Returns the String result of the StringValueOrExpr. If a StringExpr is passed, it is first "compiled"
   * before XPath evaluation.
   */
  override def evaluate(valueOrExpr: StringValueOrExpr)(implicit xpathEvaluator: XPathEvaluator): String = valueOrExpr match {
    case v: StringValue =>
      v.value
    case e: StringExpr =>
      xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(e.expr.xpathExpression), None)
  }
}
