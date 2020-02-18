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

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.base.relationship.NonStandardRelationship
import eu.cdevreeze.tqa.base.relationship.NonStandardRelationshipPath

/**
 * Partial implementation of `NonStandardRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipContainerLike extends NonStandardRelationshipContainerApi {

  // Abstract methods

  def nonStandardRelationships: immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Returns a map from source XML fragment keys to non-standard relationships. Must be fast in order for this trait to be fast.
   */
  def nonStandardRelationshipsBySource: Map[XmlFragmentKey, immutable.IndexedSeq[NonStandardRelationship]]

  /**
   * Returns a map from target XML fragment keys to non-standard relationships. Must be fast in order for this trait to be fast.
   */
  def nonStandardRelationshipsByTarget: Map[XmlFragmentKey, immutable.IndexedSeq[NonStandardRelationship]]

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def findAllNonStandardRelationships: immutable.IndexedSeq[NonStandardRelationship] = {
    nonStandardRelationships
  }

  final def filterNonStandardRelationships(
      p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    findAllNonStandardRelationships.filter(p)
  }

  final def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllNonStandardRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingNonStandardRelationships(
      sourceKey: XmlFragmentKey): immutable.IndexedSeq[NonStandardRelationship] = {

    filterOutgoingNonStandardRelationships(sourceKey)(_ => true)
  }

  final def filterOutgoingNonStandardRelationships(sourceKey: XmlFragmentKey)(
      p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    nonStandardRelationshipsBySource.getOrElse(sourceKey, Vector()).filter(p)
  }

  final def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      sourceKey: XmlFragmentKey,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(_ => true)
  }

  final def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      sourceKey: XmlFragmentKey,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    nonStandardRelationshipsBySource.getOrElse(sourceKey, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def findAllIncomingNonStandardRelationships(
      targetKey: XmlFragmentKey): immutable.IndexedSeq[NonStandardRelationship] = {

    filterIncomingNonStandardRelationships(targetKey)(_ => true)
  }

  final def filterIncomingNonStandardRelationships(targetKey: XmlFragmentKey)(
      p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    nonStandardRelationshipsByTarget.getOrElse(targetKey, Vector()).filter(p)
  }

  final def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      targetKey: XmlFragmentKey,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)(_ => true)
  }

  final def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      targetKey: XmlFragmentKey,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    nonStandardRelationshipsByTarget.getOrElse(targetKey, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      sourceKey: XmlFragmentKey,
      relationshipType: ClassTag[A])(
      p: NonStandardRelationshipPath[A] => Boolean): immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(rel =>
      p(NonStandardRelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel =>
      filterOutgoingUnrestrictedNonStandardRelationshipPaths(NonStandardRelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      targetKey: XmlFragmentKey,
      relationshipType: ClassTag[A])(
      p: NonStandardRelationshipPath[A] => Boolean): immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {

    val prevRelationships = filterIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)(rel =>
      p(NonStandardRelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel =>
      filterIncomingUnrestrictedNonStandardRelationshipPaths(NonStandardRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      path: NonStandardRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: NonStandardRelationshipPath[A] => Boolean): immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingNonStandardRelationshipsOfType(path.targetKey, relationshipType)(relationship =>
        !path.hasCycle && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingUnrestrictedNonStandardRelationshipPaths(nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      path: NonStandardRelationshipPath[A],
      relationshipType: ClassTag[A])(
      p: NonStandardRelationshipPath[A] => Boolean): immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingNonStandardRelationshipsOfType(path.sourceKey, relationshipType)(relationship =>
        !path.hasCycle && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      immutable.IndexedSeq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingUnrestrictedNonStandardRelationshipPaths(prevPath, relationshipType)(p)
      }
    }
  }
}
