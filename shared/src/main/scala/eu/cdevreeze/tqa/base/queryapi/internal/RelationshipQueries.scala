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

import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.Relationship
import eu.cdevreeze.tqa.base.relationship.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Generic implementations of relationship query functions.
 *
 * @author Chris de Vreeze
 */
object RelationshipQueries {

  // Finding and filtering relationships without looking at source or target

  def simpleRelationshipQueryApi[R <: Relationship](relationships: immutable.IndexedSeq[R]): SimpleRelationshipQueryApi[R] = {
    new SimpleRelationshipQueryApi[R](relationships)
  }

  // Finding and filtering outgoing standard relationships, with good performance

  def outgoingStandardRelationshipQueryApi[R <: StandardRelationship](
    standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[R]]): OutgoingStandardRelationshipQueryApi[R] = {

    new OutgoingStandardRelationshipQueryApi[R](standardRelationshipsBySource)
  }

  // Finding and filtering incoming standard inter-concept relationships, with good performance

  def incomingInterConceptRelationshipQueryApi[R <: InterConceptRelationship](
    interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[R]]): IncomingInterConceptRelationshipQueryApi[R] = {

    new IncomingInterConceptRelationshipQueryApi[R](interConceptRelationshipsByTarget)
  }
}
