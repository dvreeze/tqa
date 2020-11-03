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

import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.tqa.base.relationship.InterElementDeclarationRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag

/**
 * Purely abstract trait offering a standard/generic inter-concept relationship query API.
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

  def findAllInterConceptRelationships: immutable.IndexedSeq[InterElementDeclarationRelationship]

  def filterInterConceptRelationships(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship]

  def findAllInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all standard/generic inter-concept relationships that are outgoing from the given concept.
   */
  def findAllOutgoingInterConceptRelationships(
      sourceConcept: EName): immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Filters standard/generic inter-concept relationships that are outgoing from the given concept.
   */
  def filterOutgoingInterConceptRelationships(sourceConcept: EName)(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Finds all standard/generic inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def findAllOutgoingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters standard/generic inter-concept relationships of the given type that are outgoing from the given concept.
   */
  def filterOutgoingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all "consecutive" standard/generic inter-concept relationships.
   *
   * Two relationships "follow" each other if method `InterElementDeclarationRelationship.isFollowedBy` says so.
   *
   * Note that for non-dimensional relationships this implies that the parameter and result relationship
   * types must be the same, or else no relationships are returned.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingInterConceptRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveInterConceptRelationships(
      relationship: InterElementDeclarationRelationship): immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Finds all "following" ("consecutive") standard/generic inter-concept relationships of the given result type.
   *
   * Two relationships "follow" each other if method `InterElementDeclarationRelationship.isFollowedBy` says so.
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
  def findAllConsecutiveInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      relationship: InterElementDeclarationRelationship,
      resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Finds all standard/generic inter-concept relationships that are incoming to the given concept.
   */
  def findAllIncomingInterConceptRelationships(
      targetConcept: EName): immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Filters standard/generic inter-concept relationships that are incoming to the given concept.
   */
  def filterIncomingInterConceptRelationships(targetConcept: EName)(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Finds all standard/generic inter-concept relationships of the given type that are incoming to the given concept.
   */
  def findAllIncomingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters standard/generic inter-concept relationships of the given type that are incoming to the given concept.
   */
  def filterIncomingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Calls method `filterOutgoingUnrestrictedInterConceptRelationshipPaths`, adding sub-predicate
   * `isConsecutiveRelationshipPath` to the relationship path predicate.
   *
   * Typically this method should be preferred over method `filterOutgoingUnrestrictedInterConceptRelationshipPaths`.
   */
  def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Calls method `filterIncomingUnrestrictedInterConceptRelationshipPaths`, adding sub-predicate
   * `isConsecutiveRelationshipPath` to the relationship path predicate.
   *
   * Typically this method should be preferred over method `filterIncomingUnrestrictedInterConceptRelationshipPaths`.
   */
  def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Filters the standard/generic inter-concept relationship paths that are outgoing from the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   *
   * This method can be useful for finding relationship paths that are not consecutive and therefore
   * not allowed, when we do not yet know that the taxonomy is XBRL-valid.
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits. Typically prefer method `filterOutgoingConsecutiveInterConceptRelationshipPaths` instead.
   */
  def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  /**
   * Filters the standard/generic inter-concept relationship paths that are incoming to the given concept and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   *
   * This method can be useful for finding relationship paths that are not consecutive and therefore
   * not allowed, when we do not yet know that the taxonomy is XBRL-valid.
   *
   * This is a very general method that is used to implement specific methods in more specific
   * relationship query API traits. Typically prefer method `filterIncomingConsecutiveInterConceptRelationshipPaths` instead.
   */
  def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]]

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
