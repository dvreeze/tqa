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
   * Finds all non-standard relationships that are outgoing from the given XML element.
   */
  def findAllOutgoingNonStandardRelationships(
    sourceKey: XmlFragmentKey): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationships(
    sourceKey: XmlFragmentKey)(p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: XmlFragmentKey,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: XmlFragmentKey,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all non-standard relationships that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationships(
    targetKey: XmlFragmentKey): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationships(
    targetKey: XmlFragmentKey)(p: NonStandardRelationship => Boolean): immutable.IndexedSeq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: XmlFragmentKey,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters non-standard relationships of the given type that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: XmlFragmentKey,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]
}
