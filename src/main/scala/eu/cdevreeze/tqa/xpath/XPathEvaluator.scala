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

package eu.cdevreeze.tqa.xpath

import scala.collection.immutable

import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.yaidom.core.EName

/**
 * A very simple XPath evaluator abstraction. It has no knowledge about static and dynamic contexts (other than the
 * optional context item), etc. It also has no knowledge about specific implementations, such as Saxon. Moreover,
 * it has no knowledge about XPath versions.
 *
 * An XPath evaluator is needed as context when querying formula and table link content where XPath expressions are used.
 *
 * @author Chris de Vreeze
 */
trait XPathEvaluator {

  import XPathEvaluator.NodeOrAtomResult

  /**
   * XPath expression. Typically (but not necessarily) a "compiled" one.
   */
  type XPathExpression

  /**
   * The DOM node type in (DOM) evaluation results.
   */
  type Node

  def evaluateAsString(expr: XPathExpression, contextItemOption: Option[Any]): String

  def evaluateAsNode(expr: XPathExpression, contextItemOption: Option[Any]): Node

  def evaluateAsNodeSeq(expr: XPathExpression, contextItemOption: Option[Any]): immutable.IndexedSeq[NodeOrAtomResult]

  def evaluateAsBigDecimal(expr: XPathExpression, contextItemOption: Option[Any]): BigDecimal

  def evaluateAsBoolean(expr: XPathExpression, contextItemOption: Option[Any]): Boolean

  def evaluateAsEName(expr: XPathExpression, contextItemOption: Option[Any]): EName

  /**
   * Creates an XPathExpression from the given expression string. Typically (but not necessarily) "compiles" the XPath string.
   * Make sure to pass only XPath strings for which all needed namespace bindings are known to the XPath evaluator.
   */
  def toXPathExpression(xPathString: String): XPathExpression
}

object XPathEvaluator {

  sealed trait NodeOrAtomResult

  final class NodeResult[N](node: N) extends NodeOrAtomResult

  final class AtomResult(value: String) extends NodeOrAtomResult
}
