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

import eu.cdevreeze.tqa.base.model.ConsecutiveRelationshipPath
import eu.cdevreeze.tqa.base.model.StandardInterConceptRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag

/**
 * Partial implementation of `StandardInterConceptRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait StandardInterConceptRelationshipContainerLike extends StandardInterConceptRelationshipContainerApi {

  // Abstract methods

  def standardInterConceptRelationships: immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Returns a map from source concepts to standard inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def standardInterConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardInterConceptRelationship]]

  /**
   * Returns a map from target concepts to standard inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def standardInterConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[StandardInterConceptRelationship]]

  def findAllStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def findAllStandardInterConceptRelationships: immutable.IndexedSeq[StandardInterConceptRelationship] = {
    standardInterConceptRelationships
  }

  final def filterStandardInterConceptRelationships(
      p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    findAllStandardInterConceptRelationships.filter(p)
  }

  final def filterStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllStandardInterConceptRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingStandardInterConceptRelationships(
      sourceConcept: EName): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    filterOutgoingStandardInterConceptRelationships(sourceConcept)(_ => true)
  }

  final def filterOutgoingStandardInterConceptRelationships(sourceConcept: EName)(
      p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    standardInterConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).filter(p)
  }

  final def findAllOutgoingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, relationshipType)(_ => true)
  }

  final def filterOutgoingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    standardInterConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def findAllConsecutiveStandardInterConceptRelationships(
      relationship: StandardInterConceptRelationship): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    filterOutgoingStandardInterConceptRelationships(relationship.targetConceptEName) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllConsecutiveStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      relationship: StandardInterConceptRelationship,
      resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(relationship.targetConceptEName, resultRelationshipType) {
      rel =>
        relationship.isFollowedBy(rel)
    }
  }

  final def findAllIncomingStandardInterConceptRelationships(
      targetConcept: EName): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    filterIncomingStandardInterConceptRelationships(targetConcept)(_ => true)
  }

  final def filterIncomingStandardInterConceptRelationships(targetConcept: EName)(
      p: StandardInterConceptRelationship => Boolean): immutable.IndexedSeq[StandardInterConceptRelationship] = {

    standardInterConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).filter(p)
  }

  final def findAllIncomingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, relationshipType)(_ => true)
  }

  final def filterIncomingStandardInterConceptRelationshipsOfType[A <: StandardInterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    standardInterConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def filterOutgoingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, relationshipType)(
      rel => p(ConsecutiveRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(
      rel =>
        filterOutgoingConsecutiveStandardInterConceptRelationshipPaths(
          ConsecutiveRelationshipPath(rel),
          relationshipType)(p))
    paths
  }

  final def filterIncomingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships = filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, relationshipType)(
      rel => p(ConsecutiveRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(
      rel =>
        filterIncomingConsecutiveStandardInterConceptRelationshipPaths(
          ConsecutiveRelationshipPath(rel),
          relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
      path: ConsecutiveRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingStandardInterConceptRelationshipsOfType(path.targetConcept, relationshipType)(relationship =>
        !path.hasCycle && path.canAppend(relationship) && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingConsecutiveStandardInterConceptRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingConsecutiveStandardInterConceptRelationshipPaths[A <: StandardInterConceptRelationship](
      path: ConsecutiveRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingStandardInterConceptRelationshipsOfType(path.sourceConcept, relationshipType)(relationship =>
        !path.hasCycle && path.canPrepend(relationship) && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingConsecutiveStandardInterConceptRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
