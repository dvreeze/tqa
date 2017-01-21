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

package eu.cdevreeze.tqa.queryapi

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.relationship.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a standard relationship query API.
 *
 * Implementations should make sure that looking up relationships by source EName is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * For some of the graph theory terms used, see http://artint.info/html/ArtInt_50.html.
 *
 * @author Chris de Vreeze
 */
trait StandardRelationshipContainerApi {

  def findAllStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all standard relationships of the given type that are outgoing from the given concept.
   */
  def findAllOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters standard relationships of the given type that are outgoing from the given concept.
   */
  def filterOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
