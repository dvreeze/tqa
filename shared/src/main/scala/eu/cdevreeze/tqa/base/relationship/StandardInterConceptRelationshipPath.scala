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
 * Standard inter-concept relationship path. Subsequent relationships in the path must match in target and
 * source concept, respectively. It is not required that the arc role remains the same, or that
 * targetRole attributes are followed, although in practice this will be the case. A relationship path
 * must have at least one relationship.
 *
 * In practice, standard inter-concept relationship paths are "ELR-valid", that is, their relationships are in the same ELR,
 * or, in the case of dimensional relationship paths, their relationships are consecutive relationships.
 *
 * @author Chris de Vreeze
 */
final case class StandardInterConceptRelationshipPath[A <: StandardInterConceptRelationship] private(val relationships: immutable.IndexedSeq[A]) {
  require(relationships.size >= 1, s"A relationship path must have at least one relationship")

  def sourceConcept: EName = firstRelationship.sourceConceptEName

  def targetConcept: EName = lastRelationship.targetConceptEName

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def concepts: immutable.IndexedSeq[EName] = {
    relationships.map(_.sourceConceptEName) :+ relationships.last.targetConceptEName
  }

  def relationshipTargetConcepts: immutable.IndexedSeq[EName] = {
    relationships.map(_.targetConceptEName)
  }

  def hasCycle: Boolean = {
    val concepts = relationships.map(_.sourceConceptEName) :+ relationships.last.targetConceptEName
    concepts.distinct.size < concepts.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.map(p => !p.hasCycle).getOrElse(true)
  }

  def append(relationship: A): StandardInterConceptRelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new StandardInterConceptRelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): StandardInterConceptRelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new StandardInterConceptRelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    this.targetConcept == relationship.sourceConceptEName
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceConcept == relationship.targetConceptEName
  }

  def inits: immutable.IndexedSeq[StandardInterConceptRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new StandardInterConceptRelationshipPath[A](rels))
  }

  def tails: immutable.IndexedSeq[StandardInterConceptRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new StandardInterConceptRelationshipPath[A](rels))
  }

  def initOption: Option[StandardInterConceptRelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new StandardInterConceptRelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path "follow each other".
   * For dimensional relationships this means that they are consecutive relationships.
   */
  def isConsecutiveRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).isFollowedBy(pair(1)))
  }
}

object StandardInterConceptRelationshipPath {

  def apply[A <: StandardInterConceptRelationship](relationship: A): StandardInterConceptRelationshipPath[A] = {
    new StandardInterConceptRelationshipPath(Vector(relationship))
  }

  def from[A <: StandardInterConceptRelationship](relationships: immutable.IndexedSeq[A]): StandardInterConceptRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingConcepts(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source concepts")

    new StandardInterConceptRelationshipPath(relationships.toVector)
  }

  private def haveMatchingConcepts[A <: StandardInterConceptRelationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.targetConceptEName == relationship2.sourceConceptEName
  }
}
