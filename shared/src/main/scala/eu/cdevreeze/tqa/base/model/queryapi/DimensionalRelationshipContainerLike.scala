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

package eu.cdevreeze.tqa.base.model.queryapi

import eu.cdevreeze.tqa.base.model.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.model.DimensionDomainRelationship
import eu.cdevreeze.tqa.base.model.DimensionalRelationship
import eu.cdevreeze.tqa.base.model.DomainAwareRelationship
import eu.cdevreeze.tqa.base.model.DomainAwareRelationshipPath
import eu.cdevreeze.tqa.base.model.DomainMemberRelationship
import eu.cdevreeze.tqa.base.model.DomainMemberRelationshipPath
import eu.cdevreeze.tqa.base.model.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.model.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Partial implementation of `DimensionalRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipContainerLike extends DimensionalRelationshipContainerApi {
  self: StandardInterConceptRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source or target concept

  final def findAllDimensionalRelationships: immutable.IndexedSeq[DimensionalRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[DimensionalRelationship])
  }

  final def filterDimensionalRelationships(
      p: DimensionalRelationship => Boolean): immutable.IndexedSeq[DimensionalRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[DimensionalRelationship])(p)
  }

  final def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    findAllStandardInterConceptRelationshipsOfType(relationshipType)
  }

  final def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A] = {

    filterStandardInterConceptRelationshipsOfType(relationshipType)(p)
  }

  final def findAllHasHypercubeRelationships: immutable.IndexedSeq[HasHypercubeRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[HasHypercubeRelationship])
  }

  final def filterHasHypercubeRelationships(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[HasHypercubeRelationship])(p)
  }

  final def findAllHypercubeDimensionRelationships: immutable.IndexedSeq[HypercubeDimensionRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship])
  }

  final def filterHypercubeDimensionRelationships(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship])(p)
  }

  final def findAllDimensionDomainRelationships: immutable.IndexedSeq[DimensionDomainRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[DimensionDomainRelationship])
  }

  final def filterDimensionDomainRelationships(
      p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[DimensionDomainRelationship])(p)
  }

  final def findAllDomainMemberRelationships: immutable.IndexedSeq[DomainMemberRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[DomainMemberRelationship])
  }

  final def filterDomainMemberRelationships(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[DomainMemberRelationship])(p)
  }

  final def findAllDimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship] = {
    findAllStandardInterConceptRelationshipsOfType(classTag[DimensionDefaultRelationship])
  }

  final def filterDimensionDefaultRelationships(
      p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterStandardInterConceptRelationshipsOfType(classTag[DimensionDefaultRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingHasHypercubeRelationships(
      sourceConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {

    findAllOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])
  }

  final def filterOutgoingHasHypercubeRelationships(sourceConcept: EName)(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])(p)
  }

  final def filterOutgoingHasHypercubeRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterOutgoingHasHypercubeRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllOutgoingHypercubeDimensionRelationships(
      sourceConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    findAllOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])
  }

  final def filterOutgoingHypercubeDimensionRelationships(sourceConcept: EName)(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  final def filterOutgoingHypercubeDimensionRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingHypercubeDimensionRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllConsecutiveHypercubeDimensionRelationships(
      relationship: HasHypercubeRelationship): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingHypercubeDimensionRelationships(relationship.hypercube) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllOutgoingDimensionDomainRelationships(
      sourceConcept: EName): immutable.IndexedSeq[DimensionDomainRelationship] = {

    findAllOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])
  }

  final def filterOutgoingDimensionDomainRelationships(sourceConcept: EName)(
      p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])(p)
  }

  final def filterOutgoingDimensionDomainRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingDimensionDomainRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllConsecutiveDimensionDomainRelationships(
      relationship: HypercubeDimensionRelationship): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingDimensionDomainRelationships(relationship.dimension) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllOutgoingDomainMemberRelationships(
      sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationship] = {

    findAllOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])
  }

  final def filterOutgoingDomainMemberRelationships(sourceConcept: EName)(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  final def filterOutgoingDomainMemberRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingDomainMemberRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllConsecutiveDomainMemberRelationships(
      relationship: DomainAwareRelationship): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingDomainMemberRelationships(relationship.targetConceptEName) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  final def findAllOutgoingDimensionDefaultRelationships(
      sourceConcept: EName): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    findAllOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])
  }

  final def filterOutgoingDimensionDefaultRelationships(sourceConcept: EName)(
      p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterOutgoingStandardInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])(p)
  }

  final def filterOutgoingDimensionDefaultRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterOutgoingDimensionDefaultRelationships(sourceConcept)(_.elr == elr)
  }

  // Finding and filtering incoming relationships

  final def findAllIncomingDomainMemberRelationships(
      targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationship] = {

    findAllIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])
  }

  final def filterIncomingDomainMemberRelationships(targetConcept: EName)(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])(p)
  }

  final def findAllIncomingDomainAwareRelationships(
      targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationship] = {

    findAllIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])
  }

  final def filterIncomingDomainAwareRelationships(targetConcept: EName)(
      p: DomainAwareRelationship => Boolean): immutable.IndexedSeq[DomainAwareRelationship] = {

    filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])(p)
  }

  final def findAllIncomingHypercubeDimensionRelationships(
      targetConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    findAllIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[HypercubeDimensionRelationship])
  }

  final def filterIncomingHypercubeDimensionRelationships(targetConcept: EName)(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  final def findAllIncomingHasHypercubeRelationships(
      targetConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {

    findAllIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[HasHypercubeRelationship])
  }

  final def filterIncomingHasHypercubeRelationships(targetConcept: EName)(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterIncomingStandardInterConceptRelationshipsOfType(targetConcept, classTag[HasHypercubeRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  final def findAllOutgoingConsecutiveDomainAwareRelationshipPaths(
      sourceConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)
  }

  final def filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterOutgoingConsecutiveStandardInterConceptRelationshipPaths(sourceConcept, classTag[DomainAwareRelationship])(p)
  }

  final def findAllOutgoingConsecutiveDomainMemberRelationshipPaths(
      sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)
  }

  final def filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterOutgoingConsecutiveStandardInterConceptRelationshipPaths(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  final def findAllIncomingConsecutiveDomainAwareRelationshipPaths(
      targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept)(_ => true)
  }

  final def filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterIncomingConsecutiveStandardInterConceptRelationshipPaths(targetConcept, classTag[DomainAwareRelationship])(p)
  }

  final def findAllIncomingConsecutiveDomainMemberRelationshipPaths(
      targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)
  }

  final def filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterIncomingConsecutiveStandardInterConceptRelationshipPaths(targetConcept, classTag[DomainMemberRelationship])(p)
  }

  // Other query methods

  final def findAllOwnOrInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths =
      findAllIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs =
      domainMemberRelationships.map(rel => rel.elr -> rel.sourceConceptEName)

    val ownElrSourceConceptPairs =
      findAllOutgoingHasHypercubeRelationships(concept).map(rel => rel.elr -> rel.sourceConceptEName)

    val elrSourceConceptPairs = (inheritedElrSourceConceptPairs ++ ownElrSourceConceptPairs).distinct

    val hasHypercubes =
      elrSourceConceptPairs.flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  final def findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllOwnOrInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConceptEName).toSet).toMap
  }

  final def findAllInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths =
      findAllIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs =
      domainMemberRelationships.map(rel => rel.elr -> rel.sourceConceptEName).distinct

    val hasHypercubes =
      inheritedElrSourceConceptPairs.flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  final def findAllInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConceptEName).toSet).toMap
  }

  final def computeFilteredHasHypercubeInheritanceOrSelf(
      p: HasHypercubeRelationship => Boolean): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {

    val hasHypercubes = filterHasHypercubeRelationships(p)

    val conceptHasHypercubes: immutable.IndexedSeq[(EName, HasHypercubeRelationship)] =
      hasHypercubes.flatMap { hasHypercube =>
        val domainMemberPaths =
          filterOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(
            _.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConceptEName).distinct
        val ownOrInheritingConcepts = hasHypercube.primary +: inheritingConcepts

        ownOrInheritingConcepts.map(concept => concept -> hasHypercube)
      }

    conceptHasHypercubes.groupBy(_._1).view.mapValues(_.map(_._2).distinct).toMap
  }

  final def computeFilteredHasHypercubeInheritance(
      p: HasHypercubeRelationship => Boolean): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {

    val hasHypercubes = filterHasHypercubeRelationships(p)

    val conceptHasHypercubes: immutable.IndexedSeq[(EName, HasHypercubeRelationship)] =
      hasHypercubes.flatMap { hasHypercube =>
        val domainMemberPaths =
          filterOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(
            _.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConceptEName).distinct

        inheritingConcepts.map(concept => concept -> hasHypercube)
      }

    conceptHasHypercubes.groupBy(_._1).view.mapValues(_.map(_._2).distinct).toMap
  }

  final def computeHasHypercubeInheritanceOrSelf: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritanceOrSelf(_ => true)
  }

  final def computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritanceOrSelf.view.mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConceptEName).toSet).toMap
    }.toMap
  }

  final def computeHasHypercubeInheritance: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritance(_ => true)
  }

  final def computeHasHypercubeInheritanceReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritance.view.mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConceptEName).toSet).toMap
    }.toMap
  }

  final def computeHasHypercubeInheritanceOrSelfForElr(
      elr: String): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritanceOrSelf(_.elr == elr)
  }

  final def computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(elr: String): Map[EName, Set[EName]] = {
    computeHasHypercubeInheritanceOrSelfForElr(elr).view.mapValues { hasHypercubes =>
      hasHypercubes.map(_.sourceConceptEName).toSet
    }.toMap
  }

  final def computeHasHypercubeInheritanceForElr(
      elr: String): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritance(_.elr == elr)
  }

  final def computeHasHypercubeInheritanceForElrReturningPrimaries(elr: String): Map[EName, Set[EName]] = {
    computeHasHypercubeInheritanceForElr(elr).view.mapValues { hasHypercubes =>
      hasHypercubes.map(_.sourceConceptEName).toSet
    }.toMap
  }

  final def findAllMembers(domain: EName, elr: String): Set[EName] = {
    val domainMemberPaths =
      filterOutgoingConsecutiveDomainMemberRelationshipPaths(domain) { path =>
        path.firstRelationship.elr == elr
      }

    val resultWithoutHead = domainMemberPaths.flatMap(_.relationships).map(_.targetConceptEName).toSet
    resultWithoutHead + domain
  }

  final def findAllUsableMembers(domain: EName, elr: String, headUsable: Boolean): Set[EName] = {
    val domainMemberPaths =
      filterOutgoingConsecutiveDomainMemberRelationshipPaths(domain) { path =>
        path.firstRelationship.elr == elr
      }

    val potentiallyUsableNonHeadMembers =
      domainMemberPaths.flatMap(_.relationships).filter(_.usable).map(_.targetConceptEName).toSet

    val potentiallyNonUsableNonHeadMembers =
      domainMemberPaths.flatMap(_.relationships).filterNot(_.usable).map(_.targetConceptEName).toSet

    val resultWithoutHead = potentiallyUsableNonHeadMembers.diff(potentiallyNonUsableNonHeadMembers)

    if (headUsable) {
      resultWithoutHead + domain
    } else {
      resultWithoutHead
    }
  }

  final def findAllNonUsableMembers(domain: EName, elr: String, headUsable: Boolean): Set[EName] = {
    findAllMembers(domain, elr).diff(findAllUsableMembers(domain, elr, headUsable))
  }

  final def findAllMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
        path.firstRelationship.isInstanceOf[DimensionDomainRelationship] &&
        path.firstRelationship.targetConceptEName == domain &&
        path.firstRelationship.elr == dimensionDomainElr
      }

    val result = dimensionDomainPaths.flatMap(_.relationships).map(_.targetConceptEName).toSet
    result
  }

  final def findAllUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
        path.firstRelationship.isInstanceOf[DimensionDomainRelationship] &&
        path.firstRelationship.targetConceptEName == domain &&
        path.firstRelationship.elr == dimensionDomainElr
      }

    val potentiallyUsableMembers =
      dimensionDomainPaths.flatMap(_.relationships).filter(_.usable).map(_.targetConceptEName).toSet

    val potentiallyNonUsableMembers =
      dimensionDomainPaths.flatMap(_.relationships).filterNot(_.usable).map(_.targetConceptEName).toSet

    potentiallyUsableMembers.diff(potentiallyNonUsableMembers)
  }

  final def findAllNonUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    findAllMembers(dimension, domain, dimensionDomainElr).diff(
      findAllUsableMembers(dimension, domain, dimensionDomainElr))
  }

  final def findAllMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllMembers(dimension, domain, elr) }).toSet
  }

  final def findAllUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    val potentiallyUsableMembers =
      domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllUsableMembers(dimension, domain, elr) }).toSet

    val potentiallyNonUsableMembers =
      domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllNonUsableMembers(dimension, domain, elr) }).toSet

    potentiallyUsableMembers.diff(potentiallyNonUsableMembers)
  }

  final def findAllNonUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    findAllMembers(dimension, domainElrPairs).diff(findAllUsableMembers(dimension, domainElrPairs))
  }

  final def findAllDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllMembers(dim, domainElrPairs)
    }
  }

  final def findAllUsableDimensionMembers(
      hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllUsableMembers(dim, domainElrPairs)
    }
  }

  final def findAllNonUsableDimensionMembers(
      hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllNonUsableMembers(dim, domainElrPairs)
    }
  }

  // Private methods

  private def findAllDomainElrPairsPerDimension(
      hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[(EName, String)]] = {
    val hypercubeDimensionRelationships = findAllConsecutiveHypercubeDimensionRelationships(hasHypercubeRelationship)

    val dimensionDomainRelationships =
      hypercubeDimensionRelationships.flatMap(hd => findAllConsecutiveDimensionDomainRelationships(hd))

    val dimensionDomainRelationshipsByDimension = dimensionDomainRelationships.groupBy(_.dimension)

    dimensionDomainRelationshipsByDimension.view.mapValues(_.map(rel => rel.domain -> rel.elr).toSet).toMap
  }
}
