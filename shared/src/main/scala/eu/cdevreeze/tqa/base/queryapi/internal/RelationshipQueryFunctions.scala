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

package eu.cdevreeze.tqa.base.queryapi.internal

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.base.relationship.Relationship

/**
 * Generic implementations of relationship query functions.
 *
 * @author Chris de Vreeze
 */
object RelationshipQueryFunctions {

  // Finding and filtering relationships without looking at source or target

  def simpleQueryApi[R <: Relationship](relationships: immutable.IndexedSeq[R]): SimpleQueryApi[R] = {
    new SimpleQueryApi[R](relationships)
  }

  final class SimpleQueryApi[R <: Relationship](val relationships: immutable.IndexedSeq[R]) extends AnyVal {

    def filterRelationships(p: R => Boolean): immutable.IndexedSeq[R] = {
      relationships.filter(p)
    }

    def findAllRelationshipsOfType[A <: R](relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {
      filterRelationshipsOfType(relationshipType)(_ => true)
    }

    def filterRelationshipsOfType[A <: R](relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {
      implicit val clsTag: ClassTag[A] = relationshipType
      relationships.collect { case rel: A if p(rel) => rel }
    }
  }
}
