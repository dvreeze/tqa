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

package eu.cdevreeze.tqa

import eu.cdevreeze.yaidom.core.Scope

/**
 * An XPath expression in a Scope. Typically this object originates from an XPath expression
 * in an XBRL formula or table linkbase, where the Scope is taken from the XML element scope.
 *
 * It is likely that the real Scope used to evaluate the XPath expression is not exactly the same
 * Scope as stored in this object. For example, the default namespace for evaluating the XPath expression
 * should probably not be the one stored in this object, if any.
 *
 * This class is in the "tqa" root package and not in the "xpath" sub-package. The reason is that this class
 * represents data that requires no XPath evaluator whatsoever, whereas the "xpath" sub-package offers an XPath
 * evaluator abstraction. As a consequence a dependency on the "xpath" sub-package is only needed where XPath
 * evaluation is done.
 *
 * @author Chris de Vreeze
 */
final case class ScopedXPathString(val xpathExpression: String, val scope: Scope) {

  /**
   * Returns a copy in which the default namespace is not used.
   */
  def withoutDefaultNamespace: ScopedXPathString = copy(scope = scope.withoutDefaultNamespace)
}
