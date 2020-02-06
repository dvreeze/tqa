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
 * Partial implementation of `InterConceptRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait InterConceptRelationshipContainerLike extends InterConceptRelationshipContainerApi {

  // Abstract methods

  def findAllInterConceptRelationships: immutable.IndexedSeq[InterConceptRelationship]

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

  final def filterInterConceptRelationships(
      p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

    findAllInterConceptRelationships.filter(p)
  }

  final def filterInterConceptRelationshipsOfType[A <: InterConceptRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllInterConceptRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingInterConceptRelationships(
      sourceConcept: EName): immutable.IndexedSeq[InterConceptRelationship] = {

    filterOutgoingInterConceptRelationships(sourceConcept)(_ => true)
  }

  final def filterOutgoingInterConceptRelationships(sourceConcept: EName)(
      p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

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
    interConceptRelationshipsBySource.getOrElse(sourceConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
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

  final def filterIncomingInterConceptRelationships(targetConcept: EName)(
      p: InterConceptRelationship => Boolean): immutable.IndexedSeq[InterConceptRelationship] = {

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
    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterOutgoingUnrestrictedInterConceptRelationshipPaths(sourceConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  final def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterIncomingUnrestrictedInterConceptRelationshipPaths(targetConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  final def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(
      p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel =>
      p(InterConceptRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel =>
      filterOutgoingUnrestrictedInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
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

  private def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
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

  private def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
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
