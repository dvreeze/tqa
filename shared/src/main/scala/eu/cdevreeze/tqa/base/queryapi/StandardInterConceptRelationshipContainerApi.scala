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

import eu.cdevreeze.tqa.base.relationship.StandardInterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a standard inter-concept relationship query API.
 *
 * Implementations should make sure that looking up relationships by source (or target) EName is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * For some of the graph theory terms used, see http://artint.info/html/ArtInt_50.html.
 *
 * @author Chris de Vreeze
 */
trait StandardInterConceptRelationshipContainerApi {

  def findAllStandardInterConceptRelationships: immutable.IndexedSeq[StandardInterConceptRelationship]

  def filterStandardInterConceptRelationships(
    p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship]

  def findAllStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all standard inter-concept relationships that are outgoing from the given concept.
   */
  def findAllOutgoingStandardInterConceptRelationships(
    sourceConcept: EName): immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Filters standard inter-concept relationships that are outgoing from the given concept.
   */
  def filterOutgoingStandardInterConceptRelationships(
    sourceConcept: EName)(p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Finds all standard inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def findAllOutgoingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters standard inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def filterOutgoingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all "following" ("consecutive") standard inter-concept relationships of the given result type.
   *
   * Two relationships "follow" each other if method `StandardInterConceptRelationship.isFollowedBy` says so.
   *
   * Note that for non-dimensional relationships this implies that the parameter and result relationship
   * types must be the same, or else no relationships are returned.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingStandardInterConceptRelationshipsOfType(relationship.targetConceptEName, resultRelationshipType) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    relationship:           StandardInterConceptRelationship,
    resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Finds all standard inter-concept relationships that are incoming to the given concept.
   */
  def findAllIncomingStandardInterConceptRelationships(
    targetConcept: EName): immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Filters standard inter-concept relationships that are incoming to the given concept.
   */
  def filterIncomingStandardInterConceptRelationships(
    targetConcept: EName)(p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Finds all standard inter-concept relationships of the given type that are incoming to the given concept.
   */
  def findAllIncomingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    targetConcept:    EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters standard inter-concept relationships of the given type that are incoming to the given concept.
   */
  def filterIncomingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
    targetConcept:    EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Calls method `filterOutgoingUnrestrictedStandardInterConceptRelationshipPaths`, adding sub-predicate
   * `isConsecutiveRelationshipPath` to the relationship path predicate.
   *
   * Typically this method should be preferred over method `filterOutgoingUnrestrictedStandardInterConceptRelationshipPaths`.
   */
  def filterOutgoingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Calls method `filterIncomingUnrestrictedStandardInterConceptRelationshipPaths`, adding sub-predicate
   * `isConsecutiveRelationshipPath` to the relationship path predicate.
   *
   * Typically this method should be preferred over method `filterIncomingUnrestrictedStandardInterConceptRelationshipPaths`.
   */
  def filterIncomingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
    targetConcept:    EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Filters the standard inter-concept relationship paths that are outgoing from the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   *
   * This method can be useful for finding relationship paths that are not consecutive and therefore
   * not allowed, when we do not yet know that the taxonomy is XBRL-valid.
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits. Typically prefer method `filterOutgoingConsecutiveStandardInterConceptRelationshipPaths` instead.
   */
  def filterOutgoingUnrestrictedStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Filters the standard inter-concept relationship paths that are incoming to the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   *
   * This method can be useful for finding relationship paths that are not consecutive and therefore
   * not allowed, when we do not yet know that the taxonomy is XBRL-valid.
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits. Typically prefer method `filterIncomingConsecutiveStandardInterConceptRelationshipPaths` instead.
   */
  def filterIncomingUnrestrictedStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
    targetConcept:    EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
