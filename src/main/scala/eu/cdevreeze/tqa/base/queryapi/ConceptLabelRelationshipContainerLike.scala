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
import scala.reflect.classTag

import eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `ConceptLabelRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait ConceptLabelRelationshipContainerLike extends ConceptLabelRelationshipContainerApi { self: StandardRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source concept

  final def findAllConceptLabelRelationships: immutable.IndexedSeq[ConceptLabelRelationship] = {
    findAllStandardRelationshipsOfType(classTag[ConceptLabelRelationship])
  }

  final def filterConceptLabelRelationships(
    p: ConceptLabelRelationship => Boolean): immutable.IndexedSeq[ConceptLabelRelationship] = {

    filterStandardRelationshipsOfType(classTag[ConceptLabelRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingConceptLabelRelationships(
    sourceConcept: EName): immutable.IndexedSeq[ConceptLabelRelationship] = {

    findAllOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptLabelRelationship])
  }

  final def filterOutgoingConceptLabelRelationships(
    sourceConcept: EName)(p: ConceptLabelRelationship => Boolean): immutable.IndexedSeq[ConceptLabelRelationship] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptLabelRelationship])(p)
  }
}
