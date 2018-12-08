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

package eu.cdevreeze.tqa.base.model.queryapi

import scala.collection.immutable

import eu.cdevreeze.tqa.base.model.ElementReferenceRelationship
import eu.cdevreeze.tqa.base.model.Node

/**
 * Purely abstract trait offering an element-reference relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait ElementReferenceRelationshipContainerApi {

  // Finding and filtering relationships without looking at source element

  def findAllElementReferenceRelationships: immutable.IndexedSeq[ElementReferenceRelationship]

  def filterElementReferenceRelationships(
    p: ElementReferenceRelationship => Boolean): immutable.IndexedSeq[ElementReferenceRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all element-reference relationships that are outgoing from the given node.
   */
  def findAllOutgoingElementReferenceRelationships(
    source: Node): immutable.IndexedSeq[ElementReferenceRelationship]

  /**
   * Filters element-reference relationships that are outgoing from the given node.
   */
  def filterOutgoingElementReferenceRelationships(
    source: Node)(p: ElementReferenceRelationship => Boolean): immutable.IndexedSeq[ElementReferenceRelationship]
}
