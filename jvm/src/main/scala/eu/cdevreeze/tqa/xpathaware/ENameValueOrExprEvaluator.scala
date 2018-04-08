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

import eu.cdevreeze.tqa.ENameExpr
import eu.cdevreeze.tqa.ENameValue
import eu.cdevreeze.tqa.ENameValueOrExpr
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.xpath.XPathEvaluator

/**
 * XPath-aware evaluator of a ENameValueOrExpr. XPath evaluation is performed without any context item.
 * Make sure to use an XPathEvaluator that knows about the needed namespace bindings in the XPath expressions.
 *
 * @author Chris de Vreeze
 */
object ENameValueOrExprEvaluator extends ValueOrExprEvaluator[EName] {

  type ValueOrExprType = ENameValueOrExpr

  /**
   * Returns the EName result of the ENameValueOrExpr. If a ENameExpr is passed, it is first "compiled"
   * before XPath evaluation.
   */
  override def evaluate(valueOrExpr: ENameValueOrExpr)(implicit xpathEvaluator: XPathEvaluator, scope: Scope): EName = valueOrExpr match {
    case v: ENameValue =>
      v.value
    case e: ENameExpr =>
      // Cheating?

      val localNameExprString = s"local-name-from-QName(${e.expr.xpathExpression})"
      val namespaceUriExprString = s"namespace-uri-from-QName(${e.expr.xpathExpression})"

      val localName =
        xpathEvaluator.evaluateAsString(xpathEvaluator.makeXPathExpression(localNameExprString), None)
      val namespaceUri =
        xpathEvaluator.evaluateAsString(xpathEvaluator.makeXPathExpression(namespaceUriExprString), None)

      if (namespaceUri.isEmpty) EName(localName) else EName(namespaceUri, localName)
  }
}
