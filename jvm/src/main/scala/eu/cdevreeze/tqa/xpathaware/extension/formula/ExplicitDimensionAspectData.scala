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

package eu.cdevreeze.tqa.xpathaware.extension.formula

import eu.cdevreeze.tqa.extension.formula.dom.ExplicitDimensionAspect
import eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.xpath.XPathEvaluator

/**
 * Wrapper around an ExplicitDimensionAspect, which can extract the relevant data by evaluating XPath where needed.
 *
 * @author Chris de Vreeze
 */
final class ExplicitDimensionAspectData(val dimensionAspect: ExplicitDimensionAspect) {

  /**
   * Returns the dimension as EName, by calling `dimensionAspect.dimension`.
   */
  def dimensionName: EName = dimensionAspect.dimension

  // Below, make sure that the passed XPathEvaluator knows about the needed namespace bindings in the XPath expressions.

  def memberOption(implicit xpathEvaluator: XPathEvaluator): Option[EName] = {
    dimensionAspect.memberElemOption.map(_.qnameValueOrExpr).
      map(valueOrExpr => ENameValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))
  }
}
