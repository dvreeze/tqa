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
 * Model of (mainly standard) linkbase and schema content, as free from (nested) XML as possible. The model is not XPath-aware,
 * other than keeping XPath expressions as strings. It is not XLink-aware, but replaces XML-backed relationships
 * by a case class hierarchy for relationships. It is also not aware of XML Base, nor does it have to be.
 * It does not even know about documents and document URIs! (Hence the model supports no "DTS discovery", but instead
 * DTSes are explicitly "collected" from outside the model.)
 *
 * THIS PACKAGE AND ITS SUB-PACKAGES ARE EXPERIMENTAL!
 *
 * The model consists of schema content elements, and "linkbase content". The latter consists of nodes (which are
 * abstractions of XLink locators and resource) and of relationships (which are abstractions of XLink arcs).
 *
 * The model is meant to be used for (somewhat high-level) querying, as an attractive alternative to the XML
 * representation. It can also be used for taxonomy editing and creation, but keep in mind that the model elements
 * may need context that in the XML representation could even consist of multiple documents. Think for
 * example of knowledge about substitution groups and their ancestry.
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
 * This model is not dependent on the DOM, relationship and query API packages directly under the base namespace (or in this
 * namespace). The reverse is also true for those packages directly under the base namespace. In other words, this
 * model package can be used in isolation.
 *
 * @author Chris de Vreeze
 */
package object model {

  type ParentChildRelationshipPath = ConsecutiveRelationshipPath[ParentChildRelationship]

  type DomainMemberRelationshipPath = ConsecutiveRelationshipPath[DomainMemberRelationship]

  type DomainAwareRelationshipPath = ConsecutiveRelationshipPath[DomainAwareRelationship]

  type DimensionalRelationshipPath = ConsecutiveRelationshipPath[DimensionalRelationship]
}
