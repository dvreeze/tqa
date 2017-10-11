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

package eu.cdevreeze.tqa.base.queryapi

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering an inter-concept relationship query API.
 *
 * Implementations should make sure that looking up relationships by source (or target) EName is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * For some of the graph theory terms used, see http://artint.info/html/ArtInt_50.html.
 *
 * @author Chris de Vreeze
 */
trait InterConceptRelationshipContainerApi {

  def findAllInterConceptRelationships: immutable.IndexedSeq[InterConceptRelationship]

  def filterInterConceptRelationships(
    p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship]

  def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def findAllOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def filterOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all "following" ("consecutive") inter-concept relationships of the given result type.
   *
   * Two relationships "follow" each other if method `InterConceptRelationship.isFollowedBy` says so.
   *
   * Note that for non-dimensional relationships this implies that the parameter and result relationship
   * types must be the same, or else no relationships are returned.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingInterConceptRelationshipsOfType(relationship.targetConceptEName, resultRelationshipType) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveInterConceptRelationships[A <: InterConceptRelationship](
    relationship: InterConceptRelationship,
    resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Finds all inter-concept relationships of the given type that are incoming to the given concept.
   */
  def findAllIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters inter-concept relationships of the given type that are incoming to the given concept.
   */
  def filterIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Filters the longest inter-concept relationship paths that are outgoing from the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter!
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits.
   *
   * It is also a dangerous method in that termination is not guaranteed, but may depend on the passed
   * relationship path predicate. For safety, make sure that the predicate detects cycles and returns
   * false on detecting them.
   */
  def filterLongestOutgoingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Filters the longest inter-concept relationship paths that are incoming to the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter!
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits.
   *
   * It is also a dangerous method in that termination is not guaranteed, but may depend on the passed
   * relationship path predicate. For safety, make sure that the predicate detects cycles and returns
   * false on detecting them.
   */
  def filterLongestIncomingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Calls filterLongestOutgoingInterConceptRelationshipPaths, but adds cycle detection to the predicate.
   */
  def filterLongestOutgoingNonCyclicInterConceptRelationshipPaths[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Calls filterLongestIncomingInterConceptRelationshipPaths, but adds cycle detection to the predicate.
   */
  def filterLongestIncomingNonCyclicInterConceptRelationshipPaths[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
