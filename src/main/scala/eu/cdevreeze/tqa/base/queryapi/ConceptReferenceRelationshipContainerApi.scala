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

import eu.cdevreeze.tqa.base.relationship.ConceptReferenceRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a concept-reference relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait ConceptReferenceRelationshipContainerApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptReferenceRelationships: immutable.IndexedSeq[ConceptReferenceRelationship]

  def filterConceptReferenceRelationships(
    p: ConceptReferenceRelationship => Boolean): immutable.IndexedSeq[ConceptReferenceRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-reference relationships that are outgoing from the given concept.
   */
  def findAllOutgoingConceptReferenceRelationships(
    sourceConcept: EName): immutable.IndexedSeq[ConceptReferenceRelationship]

  /**
   * Filters concept-reference relationships that are outgoing from the given concept.
   */
  def filterOutgoingConceptReferenceRelationships(
    sourceConcept: EName)(p: ConceptReferenceRelationship => Boolean): immutable.IndexedSeq[ConceptReferenceRelationship]
}
