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

import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Generic incoming standard inter-concept relationship query functions. The performance of the query methods is good, due to the
 * parameter "indexed" relationships.
 *
 * @author Chris de Vreeze
 */
final class IncomingInterConceptRelationshipQueryApi[R <: InterConceptRelationship](
  val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[R]])
  extends AnyVal {

  /**
   * Finds all relationships of type R that are incoming to the given concept.
   */
  def findAllIncomingInterConceptRelationships(targetConcept: EName): immutable.IndexedSeq[R] = {
    filterIncomingInterConceptRelationships(targetConcept)(_ => true)
  }

  /**
   * Filters relationships of type R that are incoming to the given concept.
   */
  def filterIncomingInterConceptRelationships(targetConcept: EName)(p: R => Boolean): immutable.IndexedSeq[R] = {
    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).filter(p)
  }

  /**
   * Finds all relationships of the given type that are incoming to the given concept.
   */
  def findAllIncomingInterConceptRelationshipsOfType[A <: R](
    targetConcept: EName,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(_ => true)
  }

  /**
   * Filters relationships of the given type that are incoming to the given concept.
   */
  def filterIncomingInterConceptRelationshipsOfType[A <: R](targetConcept: EName, relationshipType: ClassTag[A])(
    p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val relationshipClassTag: ClassTag[A] = relationshipType

    interConceptRelationshipsByTarget.getOrElse(targetConcept, Vector()).collect {
      case relationship: A if p(relationship) => relationship
    }
  }
}
