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
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterOutgoingUnrestrictedInterConceptRelationshipPaths(sourceConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  final def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterIncomingUnrestrictedInterConceptRelationshipPaths(targetConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  final def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel =>
      p(InterConceptRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel =>
      filterOutgoingUnrestrictedInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val prevRelationships = filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(rel =>
      p(InterConceptRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel =>
      filterIncomingUnrestrictedInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      path: InterConceptRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingInterConceptRelationshipsOfType(path.targetConcept, relationshipType)(relationship =>
        !path.hasCycle && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingUnrestrictedInterConceptRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterElementDeclarationRelationship](
      path: InterConceptRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingInterConceptRelationshipsOfType(path.sourceConcept, relationshipType)(relationship =>
        !path.hasCycle && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingUnrestrictedInterConceptRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
