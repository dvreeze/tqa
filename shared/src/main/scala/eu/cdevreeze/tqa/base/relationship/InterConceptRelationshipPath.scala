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

import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable

/**
 * Standard or generic inter-concept relationship path. Subsequent relationships in the path must match in target and
 * source concept, respectively. It is not required that the arc role remains the same, or that
 * targetRole attributes are followed, although in practice this will be the case. A relationship path
 * must have at least one relationship.
 *
 * In practice, inter-concept relationship paths are "ELR-valid", that is, their relationships are in the same ELR,
 * or, in the case of dimensional relationship paths, their relationships are consecutive relationships.
 *
 * The relationships in the relationship path are inter-element-declaration relationships, which allows for non-concept
 * element declarations as relationship sources or targets. It makes no sense to use this class for such inter-element-declaration
 * relationships, but this class does not protect against such use.
 *
 * @author Chris de Vreeze
 */
final case class InterConceptRelationshipPath[A <: InterElementDeclarationRelationship] private (
    relationships: immutable.IndexedSeq[A]) {
  require(relationships.nonEmpty, s"A relationship path must have at least one relationship")

  def sourceConcept: EName = firstRelationship.sourceElementTargetEName

  def targetConcept: EName = lastRelationship.targetElementTargetEName

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def concepts: immutable.IndexedSeq[EName] = {
    relationships.map(_.sourceElementTargetEName) :+ relationships.last.targetElementTargetEName
  }

  def relationshipTargetConcepts: immutable.IndexedSeq[EName] = {
    relationships.map(_.targetElementTargetEName)
  }

  def hasCycle: Boolean = {
    val concepts = relationships.map(_.sourceElementTargetEName) :+ relationships.last.targetElementTargetEName
    concepts.distinct.size < concepts.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.forall(p => !p.hasCycle)
  }

  def append(relationship: A): InterConceptRelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new InterConceptRelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): InterConceptRelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new InterConceptRelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    this.targetConcept == relationship.sourceElementTargetEName
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceConcept == relationship.targetElementTargetEName
  }

  def inits: immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }

  def tails: immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }

  def initOption: Option[InterConceptRelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new InterConceptRelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path "follow each other".
   * For dimensional relationships this means that they are consecutive relationships.
   */
  def isConsecutiveRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).isFollowedBy(pair(1)))
  }

  def drop(n: Int): InterConceptRelationshipPath[A] = {
    InterConceptRelationshipPath(relationships.drop(n.min(relationships.size - 1)))
  }

  def dropRight(n: Int): InterConceptRelationshipPath[A] = {
    InterConceptRelationshipPath(relationships.dropRight(n.min(relationships.size - 1)))
  }
}

object InterConceptRelationshipPath {

  def apply[A <: InterElementDeclarationRelationship](relationship: A): InterConceptRelationshipPath[A] = {
    new InterConceptRelationshipPath(Vector(relationship))
  }

  def from[A <: InterElementDeclarationRelationship](
      relationships: immutable.IndexedSeq[A]): InterConceptRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingConcepts(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source concepts"
    )

    new InterConceptRelationshipPath(relationships.toVector)
  }

  private def haveMatchingConcepts[A <: InterElementDeclarationRelationship](
      relationship1: A,
      relationship2: A): Boolean = {
    relationship1.targetElementTargetEName == relationship2.sourceElementTargetEName
  }
}
