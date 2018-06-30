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

import eu.cdevreeze.tqa.XmlFragmentKey

/**
 * Non-standard relationship path. Subsequent relationships in the path must match in target and
 * source XML element (key), respectively. It is not required that the arc role remains the same, or that
 * ELRs remain the same, although in practice the latter will be the case. A relationship path
 * must have at least one relationship.
 *
 * In practice, non-standard relationship paths are "ELR-valid", that is, their relationships are in the same ELR.
 *
 * @author Chris de Vreeze
 */
final case class NonStandardRelationshipPath[A <: NonStandardRelationship] private (val relationships: immutable.IndexedSeq[A]) {
  require(relationships.size >= 1, s"A relationship path must have at least one relationship")

  def sourceKey: XmlFragmentKey = firstRelationship.sourceElem.key

  def targetKey: XmlFragmentKey = lastRelationship.targetElem.key

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def elementKeys: immutable.IndexedSeq[XmlFragmentKey] = {
    relationships.map(_.sourceElem.key) :+ relationships.last.targetElem.key
  }

  def relationshipTargetElementKeys: immutable.IndexedSeq[XmlFragmentKey] = {
    relationships.map(_.targetElem.key)
  }

  def hasCycle: Boolean = {
    val keys = relationships.map(_.sourceElem.key) :+ relationships.last.targetElem.key
    keys.distinct.size < elementKeys.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.map(p => !p.hasCycle).getOrElse(true)
  }

  def append(relationship: A): NonStandardRelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new NonStandardRelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): NonStandardRelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new NonStandardRelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    this.targetKey == relationship.sourceElem.key
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceKey == relationship.targetElem.key
  }

  def inits: immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new NonStandardRelationshipPath[A](rels))
  }

  def tails: immutable.IndexedSeq[NonStandardRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new NonStandardRelationshipPath[A](rels))
  }

  def initOption: Option[NonStandardRelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new NonStandardRelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path are in the same ELR.
   */
  def isSingleElrRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).elr == pair(1).elr)
  }
}

object NonStandardRelationshipPath {

  def apply[A <: NonStandardRelationship](relationship: A): NonStandardRelationshipPath[A] = {
    new NonStandardRelationshipPath(Vector(relationship))
  }

  def from[A <: NonStandardRelationship](relationships: immutable.IndexedSeq[A]): NonStandardRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingElementKeys(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source elements")

    new NonStandardRelationshipPath(relationships.toVector)
  }

  private def haveMatchingElementKeys[A <: NonStandardRelationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.targetElem.key == relationship2.sourceElem.key
  }
}
