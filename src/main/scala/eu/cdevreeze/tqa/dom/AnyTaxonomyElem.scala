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

package eu.cdevreeze.tqa.dom

import eu.cdevreeze.tqa.XmlFragmentKey

/**
 * Any element in a taxonomy schema or linkbase document, at any abstraction level. For example, the
 * type-safe DOM element types have this common super-type, but so does type `ConceptDeclaration` and
 * so do the type-safe DOM extensions for XBRL formulas and tables. The purpose of this type is to have a
 * common taxonomy DOM element abstraction that can be used in relationships.
 *
 * @author Chris de Vreeze
 */
trait AnyTaxonomyElem {

  def key: XmlFragmentKey
}
