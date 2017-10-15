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

import eu.cdevreeze.tqa.base.relationship.ParentChildRelationship
import eu.cdevreeze.tqa.base.relationship.PresentationRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `PresentationRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait PresentationRelationshipContainerLike extends PresentationRelationshipContainerApi { self: InterConceptRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source or target concept

  final def findAllPresentationRelationshipsOfType[A <: PresentationRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    findAllInterConceptRelationshipsOfType(relationshipType)
  }

  final def filterPresentationRelationshipsOfType[A <: PresentationRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    filterInterConceptRelationshipsOfType(relationshipType)(p)
  }

  final def findAllParentChildRelationships: immutable.IndexedSeq[ParentChildRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[ParentChildRelationship])
  }

  final def filterParentChildRelationships(
    p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[ParentChildRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingParentChildRelationships(
    sourceConcept: EName): immutable.IndexedSeq[ParentChildRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[ParentChildRelationship])
  }

  final def filterOutgoingParentChildRelationships(
    sourceConcept: EName)(p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[ParentChildRelationship])(p)
  }

  final def filterOutgoingParentChildRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[ParentChildRelationship] = {

    filterOutgoingParentChildRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllConsecutiveParentChildRelationships(
    relationship: ParentChildRelationship): immutable.IndexedSeq[ParentChildRelationship] = {

    filterOutgoingParentChildRelationships(relationship.targetConceptEName) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  // Finding and filtering incoming relationships

  final def findAllIncomingParentChildRelationships(
    targetConcept: EName): immutable.IndexedSeq[ParentChildRelationship] = {

    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[ParentChildRelationship])
  }

  final def filterIncomingParentChildRelationships(
    targetConcept: EName)(p: ParentChildRelationship => Boolean): immutable.IndexedSeq[ParentChildRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[ParentChildRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  final def findAllLongestOutgoingConsecutiveParentChildRelationshipPaths(
    sourceConcept: EName): immutable.IndexedSeq[ParentChildRelationshipPath] = {

    filterLongestOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept)(_ => true)
  }

  final def filterLongestOutgoingConsecutiveParentChildRelationshipPaths(
    sourceConcept: EName)(
      p: ParentChildRelationshipPath => Boolean): immutable.IndexedSeq[ParentChildRelationshipPath] = {

    filterLongestOutgoingInterConceptRelationshipPaths(sourceConcept, classTag[ParentChildRelationship]) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  final def findAllLongestIncomingConsecutiveParentChildRelationshipPaths(
    targetConcept: EName): immutable.IndexedSeq[ParentChildRelationshipPath] = {

    filterLongestIncomingConsecutiveParentChildRelationshipPaths(targetConcept)(_ => true)
  }

  final def filterLongestIncomingConsecutiveParentChildRelationshipPaths(
    targetConcept: EName)(p: ParentChildRelationshipPath => Boolean): immutable.IndexedSeq[ParentChildRelationshipPath] = {

    filterLongestIncomingInterConceptRelationshipPaths(targetConcept, classTag[ParentChildRelationship]) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }
}
