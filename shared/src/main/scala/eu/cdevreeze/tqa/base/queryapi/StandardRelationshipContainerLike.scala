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

import eu.cdevreeze.tqa.base.relationship.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `StandardRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait StandardRelationshipContainerLike extends StandardRelationshipContainerApi {

  // Abstract methods

  def findAllStandardRelationships: immutable.IndexedSeq[StandardRelationship]

  /**
   * Returns a map from source concepts to standard relationships. Must be fast in order for this trait to be fast.
   */
  def standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]]

  def findAllStandardRelationshipsOfType[A <: StandardRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def filterStandardRelationships(
      p: StandardRelationship => Boolean): immutable.IndexedSeq[StandardRelationship] = {

    findAllStandardRelationships.filter(p)
  }

  final def filterStandardRelationshipsOfType[A <: StandardRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllStandardRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingStandardRelationships(sourceConcept: EName): immutable.IndexedSeq[StandardRelationship] = {
    filterOutgoingStandardRelationships(sourceConcept)(_ => true)
  }

  final def filterOutgoingStandardRelationships(sourceConcept: EName)(
      p: StandardRelationship => Boolean): immutable.IndexedSeq[StandardRelationship] = {

    standardRelationshipsBySource.getOrElse(sourceConcept, Vector()).filter(p)
  }

  final def findAllOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, relationshipType)(_ => true)
  }

  final def filterOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    standardRelationshipsBySource.getOrElse(sourceConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }
}
