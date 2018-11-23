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

package eu.cdevreeze.tqa.base

/**
 * Model of (mainly standard) linkbase and schema content, as free from XML as possible. The model is not XPath-aware,
 * other than keeping XPath expressions as strings. It is not XLink-aware, but replaces relationships
 * by a case class hierarchy.
 *
 * The model is meant to be used for (somewhat high-level) querying, as an attractive alternative to the XML
 * representation. It is not meant to be used for taxonomy editing and creation, because the model elements
 * may represent context that in the XML representation could even consist to multiple documents. Think for
 * example knowledge about substitution groups and their ancestry. As a less extreme example, think the ELR
 * of the parent extended link of a relationship.
 *
 * The model is not hierarchical.
 *
 * Unlike the XML representation, model creation may fail for non-XBRL-valid taxonomies.
 *
 * The model may contain slightly less information than the XML-backed DOM and relationship classes, although it
 * should be complete enough to generate the XML representation again (but mind the remark made earlier about
 * the purpose of the model, which is not taxonomy editing). It is also complete enough for grouping
 * relationships into base sets.
 *
 * This model is not dependent on the DOM, relationship and query API packages. The reverse
 * is also true. In other words, this model package can be used in isolation.
 *
 * @author Chris de Vreeze
 */
package object model
