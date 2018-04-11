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

package eu.cdevreeze.tqa.base.relationship

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName

/**
 * Inter-concept relationship path that can be empty. It is useful in the context of table concept relationship
 * node resolution.
 *
 * @author Chris de Vreeze
 */
sealed trait EmptiableInterConceptRelationshipPath {

  type RelationshipType <: InterConceptRelationship

  /**
   * Returns the relationships of the emptiable path, which may be an empty collection
   */
  def relationships: immutable.IndexedSeq[RelationshipType]

  /**
   * Returns the source concept of the emptiable path
   */
  def sourceConcept: EName

  /**
   * Returns the target concept of the emptiable path, which is the same as the source concept for empty paths
   */
  def targetConcept: EName

  /**
   * Returns the relationship target concepts, or a singleton collection holding the concept for empty paths
   */
  def relationshipTargetConcepts: immutable.IndexedSeq[EName]
}

final case class EmptyInterConceptRelationshipPath(concept: EName) extends EmptiableInterConceptRelationshipPath {

  type RelationshipType = InterConceptRelationship

  def relationships: immutable.IndexedSeq[RelationshipType] = immutable.IndexedSeq()

  def sourceConcept: EName = concept

  def targetConcept: EName = concept

  def relationshipTargetConcepts: immutable.IndexedSeq[EName] = immutable.IndexedSeq(concept)
}

final case class NonEmptyInterConceptRelationshipPath[A <: InterConceptRelationship](
  interConceptRelationshipPath: InterConceptRelationshipPath[A]) extends EmptiableInterConceptRelationshipPath {

  type RelationshipType = A

  def relationships: immutable.IndexedSeq[RelationshipType] = {
    interConceptRelationshipPath.relationships
  }

  def sourceConcept: EName = interConceptRelationshipPath.sourceConcept

  def targetConcept: EName = interConceptRelationshipPath.targetConcept

  def relationshipTargetConcepts: immutable.IndexedSeq[EName] = {
    interConceptRelationshipPath.relationshipTargetConcepts
  }
}
