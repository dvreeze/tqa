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
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa.base.model.InterConceptRelationship
import eu.cdevreeze.tqa.base.model.ConsecutiveRelationshipPath
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `InterConceptRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait InterConceptRelationshipContainerLike extends InterConceptRelationshipContainerApi {

  // Abstract methods

  /**
   * Returns a map from source concepts to inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]]

  /**
   * Returns a map from target concepts to inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]]

  def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def findAllInterConceptRelationships: immutable.IndexedSeq[InterConceptRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[InterConceptRelationship])
  }

  final def filterInterConceptRelationships(
    p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[InterConceptRelationship])(p)
  }

  final def filterInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllInterConceptRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingInterConceptRelationships(
    sourceConcept: EName): immutable.IndexedSeq[InterConceptRelationship] = {

    filterOutgoingInterConceptRelationships(sourceConcept)(_ => true)
  }

  final def filterOutgoingInterConceptRelationships(
    sourceConcept: EName)(p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

    interConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).filter(p)
  }

  final def findAllOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(_ => true)
  }

  final def filterOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    interConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()) collect { case relationship: A if p(relationship) => relationship }
  }

  final def findAllConsecutiveInterConceptRelationships(relationship: InterConceptRelationship): immutable.IndexedSeq[InterConceptRelationship] = {
    filterOutgoingInterConceptRelationships(relationship.targetConceptEName) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllConsecutiveInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationship: InterConceptRelationship,
    resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(relationship.targetConceptEName, resultRelationshipType) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllIncomingInterConceptRelationships(
    targetConcept: EName): immutable.IndexedSeq[InterConceptRelationship] = {

    filterIncomingInterConceptRelationships(targetConcept)(_ => true)
  }

  final def filterIncomingInterConceptRelationships(
    targetConcept: EName)(p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).filter(p)
  }

  final def findAllIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(_ => true)
  }

  final def filterIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()) collect { case relationship: A if p(relationship) => relationship }
  }

  final def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel => p(ConsecutiveRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel => filterOutgoingConsecutiveInterConceptRelationshipPaths(ConsecutiveRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships = filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(rel => p(ConsecutiveRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel => filterIncomingConsecutiveInterConceptRelationshipPaths(ConsecutiveRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
    path: ConsecutiveRelationshipPath[A],
    relationshipType: ClassTag[A])(p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingInterConceptRelationshipsOfType(
        path.targetConcept, relationshipType)(relationship => !path.hasCycle && path.canAppend(relationship) && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths flatMap { nextPath =>
        // Recursive calls
        filterOutgoingConsecutiveInterConceptRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
    path: ConsecutiveRelationshipPath[A],
    relationshipType: ClassTag[A])(p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingInterConceptRelationshipsOfType(
        path.sourceConcept, relationshipType)(relationship => !path.hasCycle && path.canPrepend(relationship) && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths flatMap { prevPath =>
        // Recursive calls
        filterIncomingConsecutiveInterConceptRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
