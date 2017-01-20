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
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa.relationship.AllRelationship
import eu.cdevreeze.tqa.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.relationship.NotAllRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of DimensionalRelationshipContainerApi.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipContainerLike extends DimensionalRelationshipContainerApi with InterConceptRelationshipContainerLike {

  // Finding and filtering relationships without looking at source of target concept

  final def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    findAllInterConceptRelationshipsOfType(relationshipType)
  }

  final def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    filterInterConceptRelationshipsOfType(relationshipType)(p)
  }

  final def findAllHasHypercubeRelationships: immutable.IndexedSeq[HasHypercubeRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[HasHypercubeRelationship])
  }

  final def filterHasHypercubeRelationships(
    p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[HasHypercubeRelationship])(p)
  }

  final def findAllHypercubeDimensionRelationships: immutable.IndexedSeq[HypercubeDimensionRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship])
  }

  final def filterHypercubeDimensionRelationships(
    p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship])(p)
  }

  final def findAllDimensionDomainRelationships: immutable.IndexedSeq[DimensionDomainRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[DimensionDomainRelationship])
  }

  final def filterDimensionDomainRelationships(
    p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[DimensionDomainRelationship])(p)
  }

  final def findAllDomainMemberRelationships: immutable.IndexedSeq[DomainMemberRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[DomainMemberRelationship])
  }

  final def filterDomainMemberRelationships(
    p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[DomainMemberRelationship])(p)
  }

  final def findAllDimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[DimensionDefaultRelationship])
  }

  final def filterDimensionDefaultRelationships(
    p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterInterConceptRelationshipsOfType(classTag[DimensionDefaultRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingHasHypercubeRelationships(
    sourceConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])
  }

  final def filterOutgoingHasHypercubeRelationships(
    sourceConcept: EName)(p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])(p)
  }

  final def findAllOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])
  }

  final def filterOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName)(p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  final def findAllOutgoingDimensionDomainRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDomainRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])
  }

  final def filterOutgoingDimensionDomainRelationships(
    sourceConcept: EName)(p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])(p)
  }

  final def findAllOutgoingDomainMemberRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])
  }

  final def filterOutgoingDomainMemberRelationships(
    sourceConcept: EName)(p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  final def findAllOutgoingDimensionDefaultRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])
  }

  final def filterOutgoingDimensionDefaultRelationships(
    sourceConcept: EName)(p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])(p)
  }

  // Finding and filtering incoming relationships

  final def findAllIncomingDomainMemberRelationships(
    targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationship] = {

    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])
  }

  final def filterIncomingDomainMemberRelationships(
    targetConcept: EName)(p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  final def filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterLongestOutgoingInterConceptRelationshipPaths(sourceConcept, classTag[DomainAwareRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }

  final def filterLongestIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName)(p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterLongestIncomingInterConceptRelationshipPaths(targetConcept, classTag[DomainMemberRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }
}
