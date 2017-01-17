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

package eu.cdevreeze.tqa.queryapi

import scala.collection.immutable

import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Inter-concept relationship path. Subsequent relationships in the path must match in target and
 * source concept, respectively. It is not required that the arc role remains the same, or that
 * targetRole attributes are followed, although in practice this will be the case. A relationship path
 * must have at least one relationship.
 *
 * @author Chris de Vreeze
 */
final case class InterConceptRelationshipPath[A <: InterConceptRelationship] private (val relationships: immutable.IndexedSeq[A]) {
  require(relationships.size >= 1, s"A relationship chain must have at least one relationship")

  def sourceConcept: EName = firstRelationship.sourceConceptEName

  def targetConcept: EName = lastRelationship.targetConceptEName

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def concepts: immutable.IndexedSeq[EName] = {
    relationships.map(_.sourceConceptEName) :+ relationships.last.targetConceptEName
  }

  def hasCycle: Boolean = {
    val concepts = relationships.map(_.sourceConceptEName) :+ relationships.last.targetConceptEName
    concepts.distinct.size < concepts.size
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
    this.targetConcept == relationship.sourceConceptEName
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceConcept == relationship.targetConceptEName
  }

  def inits: immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }

  def tails: immutable.IndexedSeq[InterConceptRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }
}

object InterConceptRelationshipPath {

  def apply[A <: InterConceptRelationship](relationship: A): InterConceptRelationshipPath[A] = {
    new InterConceptRelationshipPath(Vector(relationship))
  }

  def from[A <: InterConceptRelationship](relationships: immutable.IndexedSeq[A]): InterConceptRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingConcepts(pair(0), pair(1))),
      s"All subsequent relationships in a chain must have matching target/source concepts")

    new InterConceptRelationshipPath(relationships.toVector)
  }

  private def haveMatchingConcepts[A <: InterConceptRelationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.targetConceptEName == relationship2.sourceConceptEName
  }
}
