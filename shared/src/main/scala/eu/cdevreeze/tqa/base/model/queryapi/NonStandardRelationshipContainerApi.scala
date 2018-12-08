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

import eu.cdevreeze.tqa.base.model.Node
import eu.cdevreeze.tqa.base.model.NonStandardRelationship
import eu.cdevreeze.tqa.base.model.RelationshipPath

/**
 * Purely abstract trait offering a non-standard relationship query API.
 *
 * Implementations should make sure that looking up relationships by source XML fragment key is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * For some of the graph theory terms used, see http://artint.info/html/ArtInt_50.html.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipContainerApi {

  def findAllNonStandardRelationships: immutable.IndexedSeq[NonStandardRelationship]

  def filterNonStandardRelationships(
    p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship]

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all non-standard relationships that are outgoing from the given node.
   */
  def findAllOutgoingNonStandardRelationships(
    source: Node): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are outgoing from the given node.
   */
  def filterOutgoingNonStandardRelationships(
    source: Node)(p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are outgoing from the given node.
   */
  def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    source: Node,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters non-standard relationships of the given type that are outgoing from the given node.
   */
  def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    source: Node,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all non-standard relationships that are incoming to the given node.
   */
  def findAllIncomingNonStandardRelationships(
    target: Node): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are incoming to the given node.
   */
  def filterIncomingNonStandardRelationships(
    target: Node)(p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are incoming to the given node.
   */
  def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    target: Node,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters non-standard relationships of the given type that are incoming to the given node.
   */
  def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    target: Node,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Filters the non-standard relationship paths that are outgoing from the given node and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   */
  def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    source: Node,
    relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]]

  /**
   * Filters the non-standard relationship paths that are incoming to the given node and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   */
  def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    target: Node,
    relationshipType: ClassTag[A])(p: RelationshipPath[A] => Boolean): immutable.IndexedSeq[RelationshipPath[A]]
}
