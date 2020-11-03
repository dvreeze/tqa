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
import eu.cdevreeze.tqa.base.model.InterElementDeclarationRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag

/**
 * Partial implementation of `InterConceptRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait InterConceptRelationshipContainerLike extends InterConceptRelationshipContainerApi {

  // Abstract methods

  def interConceptRelationships: immutable.IndexedSeq[InterElementDeclarationRelationship]

  /**
   * Returns a map from source concepts to standard/generic inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterElementDeclarationRelationship]]

  /**
   * Returns a map from target concepts to standard/generic inter-concept relationships. Must be fast in order for this trait to be fast.
   */
  def interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterElementDeclarationRelationship]]

  def findAllInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def findAllInterConceptRelationships: immutable.IndexedSeq[InterElementDeclarationRelationship] = {
    interConceptRelationships
  }

  final def filterInterConceptRelationships(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    findAllInterConceptRelationships.filter(p)
  }

  final def filterInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllInterConceptRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingInterConceptRelationships(
      sourceConcept: EName): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    filterOutgoingInterConceptRelationships(sourceConcept)(_ => true)
  }

  final def filterOutgoingInterConceptRelationships(sourceConcept: EName)(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    interConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).filter(p)
  }

  final def findAllOutgoingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(_ => true)
  }

  final def filterOutgoingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    interConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def findAllConsecutiveInterConceptRelationships(
      relationship: InterElementDeclarationRelationship): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    filterOutgoingInterConceptRelationships(relationship.targetElementTargetEName) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllConsecutiveInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      relationship: InterElementDeclarationRelationship,
      resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(relationship.targetElementTargetEName, resultRelationshipType) {
      rel =>
        relationship.isFollowedBy(rel)
    }
  }

  final def findAllIncomingInterConceptRelationships(
      targetConcept: EName): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    filterIncomingInterConceptRelationships(targetConcept)(_ => true)
  }

  final def filterIncomingInterConceptRelationships(targetConcept: EName)(
      p: InterElementDeclarationRelationship => Boolean): immutable.IndexedSeq[InterElementDeclarationRelationship] = {

    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).filter(p)
  }

  final def findAllIncomingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(_ => true)
  }

  final def filterIncomingInterConceptRelationshipsOfType[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel =>
      p(ConsecutiveRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel =>
      filterOutgoingConsecutiveInterConceptRelationshipPaths(ConsecutiveRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships = filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(rel =>
      p(ConsecutiveRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel =>
      filterIncomingConsecutiveInterConceptRelationshipPaths(ConsecutiveRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      path: ConsecutiveRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingInterConceptRelationshipsOfType(path.targetConcept, relationshipType)(relationship =>
        !path.hasCycle && path.canAppend(relationship) && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingConsecutiveInterConceptRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      path: ConsecutiveRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: ConsecutiveRelationshipPath[A] => Boolean): immutable.IndexedSeq[ConsecutiveRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingInterConceptRelationshipsOfType(path.sourceConcept, relationshipType)(relationship =>
        !path.hasCycle && path.canPrepend(relationship) && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingConsecutiveInterConceptRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
