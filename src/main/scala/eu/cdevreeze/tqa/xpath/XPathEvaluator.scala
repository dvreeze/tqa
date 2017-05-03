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

import scala.reflect.ClassTag

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

  /**
   * XPath expression. Typically (but not necessarily) a "compiled" one.
   */
  type XPathExpression

  /**
   * Returns the supported return types. This must include String and EName.
   */
  def supportedReturnTypes: Set[ClassTag[_]]

  def evaluate[A](expr: XPathExpression, contextItemOption: Option[Any], classTag: ClassTag[A]): A

  /**
   * Returns `evaluate(expr, contextItemOption, classTag[String])`.
   */
  def evaluateAsString(expr: XPathExpression, contextItemOption: Option[Any]): String

  /**
   * Returns `evaluate(expr, contextItemOption, classTag[EName])`.
   */
  def evaluateAsEName(expr: XPathExpression, contextItemOption: Option[Any]): EName

  /**
   * Creates an XPathExpression from the given ScopedXPathString. Typically (but not necessarily) "compiles" the XPath string.
   */
  def toXPathExpression(scopedXPathString: ScopedXPathString): XPathExpression
}
