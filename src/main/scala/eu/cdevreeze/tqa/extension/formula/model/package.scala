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

package eu.cdevreeze.tqa.extension.formula

/**
 * Model of formula linkbase content, as free from XML as possible. The model is not XPath-aware,
 * other than keeping XPath expressions as strings. It is not XLink-aware, but replaces relationships
 * by a case class hierarchy.
 *
 * The model may contain less information than the XML-backed DOM and relationship classes for formulas.
 *
 * This model is not dependent on the (formula) DOM, relationship and query API packages. The reverse
 * is also true. In other words, this model package can be used in isolation.
 *
 * @author Chris de Vreeze
 */
package object model
