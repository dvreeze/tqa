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

package eu.cdevreeze.tqa.extension.formula.dom

import eu.cdevreeze.tqa.base.dom.AnyTaxonomyElem
import eu.cdevreeze.tqa.base.dom.NonStandardResource
import eu.cdevreeze.tqa.xlink.XLinkResource

/**
 * XLink resource in a formula or table (!) link. This trait is needed for modeling table relationships
 * in the "table extension".
 *
 * @author Chris de Vreeze
 */
trait FormulaOrTableResource extends AnyTaxonomyElem with XLinkResource {

  def underlyingResource: NonStandardResource
}
