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

import eu.cdevreeze.tqa.base.queryapi.internal.RelationshipQueries
import eu.cdevreeze.tqa.base.relationship.Relationship

/**
 * Partial implementation of `RelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait RelationshipContainerLike extends RelationshipContainerApi {

  // Abstract methods

  def relationships: immutable.IndexedSeq[Relationship]

  def findAllRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  // Concrete methods

  final def filterRelationships(p: Relationship => Boolean): immutable.IndexedSeq[Relationship] = {
    RelationshipQueries.simpleRelationshipQueryApi(relationships).filterRelationships(p)
  }

  final def filterRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    RelationshipQueries.simpleRelationshipQueryApi(relationships).filterRelationshipsOfType(relationshipType)(p)
  }
}
