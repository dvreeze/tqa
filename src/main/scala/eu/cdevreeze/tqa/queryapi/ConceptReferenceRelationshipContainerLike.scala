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
import scala.reflect.classTag

import eu.cdevreeze.tqa.relationship.ConceptReferenceRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `ConceptReferenceRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait ConceptReferenceRelationshipContainerLike extends ConceptReferenceRelationshipContainerApi { self: StandardRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source concept

  final def findAllConceptReferenceRelationships: immutable.IndexedSeq[ConceptReferenceRelationship] = {
    findAllStandardRelationshipsOfType(classTag[ConceptReferenceRelationship])
  }

  final def filterConceptReferenceRelationships(
    p: ConceptReferenceRelationship => Boolean): immutable.IndexedSeq[ConceptReferenceRelationship] = {

    filterStandardRelationshipsOfType(classTag[ConceptReferenceRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingConceptReferenceRelationships(
    sourceConcept: EName): immutable.IndexedSeq[ConceptReferenceRelationship] = {

    findAllOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptReferenceRelationship])
  }

  final def filterOutgoingConceptReferenceRelationships(
    sourceConcept: EName)(p: ConceptReferenceRelationship => Boolean): immutable.IndexedSeq[ConceptReferenceRelationship] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptReferenceRelationship])(p)
  }
}
