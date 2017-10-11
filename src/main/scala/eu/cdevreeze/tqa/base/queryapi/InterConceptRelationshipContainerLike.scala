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
import scala.reflect.classTag

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

  final def findAllConsecutiveInterConceptRelationships[A <: InterConceptRelationship](
    relationship: InterConceptRelationship,
    resultRelationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(relationship.targetConceptEName, resultRelationshipType) { rel =>
      relationship.isFollowedBy(rel)
    }
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

  final def filterLongestOutgoingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel => p(InterConceptRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel => filterLongestOutgoingInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterLongestIncomingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val prevRelationships = filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(rel => p(InterConceptRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel => filterLongestIncomingInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterLongestOutgoingNonCyclicInterConceptRelationshipPaths[A <: InterConceptRelationship](
    sourceConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterLongestOutgoingInterConceptRelationshipPaths(sourceConcept, relationshipType)(path => !path.hasCycle && p(path))
  }

  final def filterLongestIncomingNonCyclicInterConceptRelationshipPaths[A <: InterConceptRelationship](
    targetConcept: EName,
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    filterLongestIncomingInterConceptRelationshipPaths(targetConcept, relationshipType)(path => !path.hasCycle && p(path))
  }

  // Private methods

  private def filterLongestOutgoingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    path: InterConceptRelationshipPath[A],
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingInterConceptRelationshipsOfType(path.targetConcept, relationshipType)(relationship => p(path.append(relationship)))
    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths flatMap { nextPath =>
        // Recursive calls
        filterLongestOutgoingInterConceptRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterLongestIncomingInterConceptRelationshipPaths[A <: InterConceptRelationship](
    path: InterConceptRelationshipPath[A],
    relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {

    val prevRelationships = filterIncomingInterConceptRelationshipsOfType(path.sourceConcept, relationshipType)(relationship => p(path.prepend(relationship)))
    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths flatMap { prevPath =>
        // Recursive calls
        filterLongestIncomingInterConceptRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
