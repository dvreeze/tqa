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

import eu.cdevreeze.tqa.relationship.ConceptLabelRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a concept-label relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait ConceptLabelRelationshipContainerApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptLabelRelationships: immutable.IndexedSeq[ConceptLabelRelationship]

  def filterConceptLabelRelationships(
    p: ConceptLabelRelationship => Boolean): immutable.IndexedSeq[ConceptLabelRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-label relationships that are outgoing from the given concept.
   */
  def findAllOutgoingConceptLabelRelationships(
    sourceConcept: EName): immutable.IndexedSeq[ConceptLabelRelationship]

  /**
   * Filters concept-label relationships that are outgoing from the given concept.
   */
  def filterOutgoingConceptLabelRelationships(
    sourceConcept: EName)(p: ConceptLabelRelationship => Boolean): immutable.IndexedSeq[ConceptLabelRelationship]
}
