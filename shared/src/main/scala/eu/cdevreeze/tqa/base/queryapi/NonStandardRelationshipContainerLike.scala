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

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.base.relationship.NonStandardRelationship

/**
 * Partial implementation of `NonStandardRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipContainerLike extends NonStandardRelationshipContainerApi {

  // Abstract methods

  /**
   * Returns a map from source XML fragment keys to non-standard relationships. Must be fast in order for this trait to be fast.
   */
  def nonStandardRelationshipsBySource: Map[XmlFragmentKey, immutable.IndexedSeq[NonStandardRelationship]]

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def findAllNonStandardRelationships: immutable.IndexedSeq[NonStandardRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[NonStandardRelationship])
  }

  final def filterNonStandardRelationships(
    p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    filterNonStandardRelationshipsOfType(classTag[NonStandardRelationship])(p)
  }

  final def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllNonStandardRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingNonStandardRelationships(
    sourceKey: XmlFragmentKey): immutable.IndexedSeq[NonStandardRelationship] = {

    filterOutgoingNonStandardRelationships(sourceKey)(_ => true)
  }

  final def filterOutgoingNonStandardRelationships(
    sourceKey: XmlFragmentKey)(p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    nonStandardRelationshipsBySource.getOrElse(sourceKey, Vector()).filter(p)
  }

  final def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey:        XmlFragmentKey,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(_ => true)
  }

  final def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey:        XmlFragmentKey,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    nonStandardRelationshipsBySource.getOrElse(sourceKey, Vector()) collect { case relationship: A if p(relationship) => relationship }
  }
}
