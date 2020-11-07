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

import eu.cdevreeze.tqa.base.model.Node
import eu.cdevreeze.tqa.base.model.NonStandardRelationship
import eu.cdevreeze.tqa.base.model.RelationshipPath

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Partial implementation of `NonStandardRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipContainerLike extends NonStandardRelationshipContainerApi with PathLengthRestrictionApi {

  // Abstract methods

  /**
   * Returns a map from source nodes to non-standard relationships. Must be fast in order for this trait to be fast.
   */
  def nonStandardRelationshipsBySource: Map[Node, immutable.IndexedSeq[NonStandardRelationship]]

  /**
   * Returns a map from target nodes to non-standard relationships. Must be fast in order for this trait to be fast.
   */
  def nonStandardRelationshipsByTarget: Map[Node, immutable.IndexedSeq[NonStandardRelationship]]

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

  final def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllNonStandardRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingNonStandardRelationships(source: Node): immutable.IndexedSeq[NonStandardRelationship] = {

    filterOutgoingNonStandardRelationships(source)(_ => true)
  }

  final def filterOutgoingNonStandardRelationships(source: Node)(
      p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    nonStandardRelationshipsBySource.getOrElse(source, Vector()).filter(p)
  }

  final def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      source: Node,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingNonStandardRelationshipsOfType(source, relationshipType)(_ => true)
  }

  final def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      source: Node,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType
    nonStandardRelationshipsBySource.getOrElse(source, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def findAllIncomingNonStandardRelationships(target: Node): immutable.IndexedSeq[NonStandardRelationship] = {

    filterIncomingNonStandardRelationships(target)(_ => true)
  }

  final def filterIncomingNonStandardRelationships(target: Node)(
      p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship] = {

    nonStandardRelationshipsByTarget.getOrElse(target, Vector()).filter(p)
  }

  final def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      target: Node,
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingNonStandardRelationshipsOfType(target, relationshipType)(_ => true)
  }

  final def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
      target: Node,
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag = relationshipType

    nonStandardRelationshipsByTarget.getOrElse(target, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }

  final def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      source: Node,
      relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingNonStandardRelationshipsOfType(source, relationshipType)(rel => p(RelationshipPath(rel)))

    val paths = nextRelationships.flatMap(rel =>
      filterOutgoingUnrestrictedNonStandardRelationshipPaths(RelationshipPath(rel), relationshipType)(p))
    paths
  }

  final def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      target: Node,
      relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingNonStandardRelationshipsOfType(target, relationshipType)(rel => p(RelationshipPath(rel)))

    val paths = prevRelationships.flatMap(rel =>
      filterIncomingUnrestrictedNonStandardRelationshipPaths(RelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
      path: RelationshipPath[A],
      relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingNonStandardRelationshipsOfType(path.target, relationshipType)(relationship =>
        !path.dropRight(maxPathLengthBeyondCycle).hasCycle && p(path.append(relationship)))

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
      path: RelationshipPath[A],
      relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingNonStandardRelationshipsOfType(path.source, relationshipType)(relationship =>
        !path.drop(maxPathLengthBeyondCycle).hasCycle && p(path.prepend(relationship)))

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
