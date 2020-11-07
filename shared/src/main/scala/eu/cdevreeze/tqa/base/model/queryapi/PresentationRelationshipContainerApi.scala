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

import eu.cdevreeze.tqa.base.model.ParentChildRelationship
import eu.cdevreeze.tqa.base.model.ParentChildRelationshipPath
import eu.cdevreeze.tqa.base.model.PresentationRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag

/**
 * Purely abstract trait offering a presentation relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait PresentationRelationshipContainerApi {

  // Finding and filtering relationships without looking at source or target concept

  def findAllPresentationRelationshipsOfType[A <: PresentationRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterPresentationRelationshipsOfType[A <: PresentationRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A]

  def findAllParentChildRelationships: immutable.IndexedSeq[ParentChildRelationship]

  def filterParentChildRelationships(
      p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all parent-child relationships that are outgoing from the given concept.
   */
  def findAllOutgoingParentChildRelationships(sourceConcept: EName): immutable.IndexedSeq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are outgoing from the given concept.
   */
  def filterOutgoingParentChildRelationships(sourceConcept: EName)(
      p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingParentChildRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[ParentChildRelationship]

  /**
   * Finds all "following" ("consecutive") parent-child relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingParentChildRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveParentChildRelationships(
      relationship: ParentChildRelationship): immutable.IndexedSeq[ParentChildRelationship]

  // Finding and filtering incoming relationships

  /**
   * Finds all parent-child relationships that are incoming to the given concept.
   */
  def findAllIncomingParentChildRelationships(targetConcept: EName): immutable.IndexedSeq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are incoming to the given concept.
   */
  def filterIncomingParentChildRelationships(targetConcept: EName)(
      p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship]

  // Filtering outgoing and incoming relationship paths

  /**
   * Returns `filterOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveParentChildRelationshipPaths(
      sourceConcept: EName): immutable.IndexedSeq[ParentChildRelationshipPath]

  /**
   * Filters the consecutive (!) parent-child relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept: EName)(
      p: ParentChildRelationshipPath => Boolean): immutable.IndexedSeq[ParentChildRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveParentChildRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveParentChildRelationshipPaths(
      targetConcept: EName): immutable.IndexedSeq[ParentChildRelationshipPath]

  /**
   * Filters the consecutive (!) parent-child relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterIncomingConsecutiveParentChildRelationshipPaths(targetConcept: EName)(
      p: ParentChildRelationshipPath => Boolean): immutable.IndexedSeq[ParentChildRelationshipPath]
}
