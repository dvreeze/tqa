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

package eu.cdevreeze.tqa.base.model

import scala.collection.immutable

/**
 * Relationship path. Subsequent relationships in the path must match in target/source node. Typically this is
 * not restrictive enough, because normally the ELR must remain the same among the relationships (except for
 * dimensional relationships).
 *
 * A relationship path must have at least one relationship.
 *
 * @author Chris de Vreeze
 */
final case class RelationshipPath[A <: Relationship] private (relationships: immutable.IndexedSeq[A]) {
  require(relationships.nonEmpty, s"A relationship path must have at least one relationship")

  def source: Node = firstRelationship.source

  def target: Node = lastRelationship.target

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def nodes: immutable.IndexedSeq[Node] = {
    relationships.map(_.source) :+ relationships.last.target
  }

  def relationshipTargets: immutable.IndexedSeq[Node] = {
    relationships.map(_.target)
  }

  def hasCycle: Boolean = {
    val nodes = relationships.map(_.source) :+ relationships.last.target
    nodes.distinct.size < nodes.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.forall(p => !p.hasCycle)
  }

  def append(relationship: A): RelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new RelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): RelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new RelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    RelationshipPath.haveMatchingNodes(lastRelationship, relationship)
  }

  def canPrepend(relationship: A): Boolean = {
    RelationshipPath.haveMatchingNodes(relationship, firstRelationship)
  }

  def inits: immutable.IndexedSeq[RelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toIndexedSeq.map(rels => new RelationshipPath[A](rels))
  }

  def tails: immutable.IndexedSeq[RelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toIndexedSeq.map(rels => new RelationshipPath[A](rels))
  }

  def initOption: Option[RelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new RelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path are in the same ELR.
   */
  def isSingleElrRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).elr == pair(1).elr)
  }

  def drop(n: Int): RelationshipPath[A] = {
    RelationshipPath(relationships.drop(n.min(relationships.size - 1)))
  }

  def dropRight(n: Int): RelationshipPath[A] = {
    RelationshipPath(relationships.dropRight(n.min(relationships.size - 1)))
  }
}

object RelationshipPath {

  def apply[A <: Relationship](relationship: A): RelationshipPath[A] = {
    new RelationshipPath(immutable.IndexedSeq(relationship))
  }

  def from[A <: Relationship](relationships: immutable.IndexedSeq[A]): RelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingNodes(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source nodes"
    )

    new RelationshipPath(relationships)
  }

  private def haveMatchingNodes[A <: Relationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.target == relationship2.source
  }
}
