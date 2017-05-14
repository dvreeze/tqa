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
import eu.cdevreeze.tqa.relationship.InterConceptRelationshipPath
import eu.cdevreeze.tqa.relationship.NotAllRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `DimensionalRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipContainerLike extends DimensionalRelationshipContainerApi { self: InterConceptRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source or target concept

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

  final def filterOutgoingHasHypercubeRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[HasHypercubeRelationship] = {

    filterOutgoingHasHypercubeRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])
  }

  final def filterOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName)(p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  final def filterOutgoingHypercubeDimensionRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    filterOutgoingHypercubeDimensionRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllOutgoingDimensionDomainRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDomainRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])
  }

  final def filterOutgoingDimensionDomainRelationships(
    sourceConcept: EName)(p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])(p)
  }

  final def filterOutgoingDimensionDomainRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[DimensionDomainRelationship] = {

    filterOutgoingDimensionDomainRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllOutgoingDomainMemberRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])
  }

  final def filterOutgoingDomainMemberRelationships(
    sourceConcept: EName)(p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  final def filterOutgoingDomainMemberRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[DomainMemberRelationship] = {

    filterOutgoingDomainMemberRelationships(sourceConcept)(_.elr == elr)
  }

  final def findAllOutgoingDimensionDefaultRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])
  }

  final def filterOutgoingDimensionDefaultRelationships(
    sourceConcept: EName)(p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])(p)
  }

  final def filterOutgoingDimensionDefaultRelationshipsOnElr(
    sourceConcept: EName, elr: String): immutable.IndexedSeq[DimensionDefaultRelationship] = {

    filterOutgoingDimensionDefaultRelationships(sourceConcept)(_.elr == elr)
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

  final def findAllIncomingDomainAwareRelationships(
    targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationship] = {

    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])
  }

  final def filterIncomingDomainAwareRelationships(
    targetConcept: EName)(p: DomainAwareRelationship => Boolean): immutable.IndexedSeq[DomainAwareRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  final def findAllLongestOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)
  }

  final def filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterLongestOutgoingInterConceptRelationshipPaths(sourceConcept, classTag[DomainAwareRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }

  final def findAllLongestOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)
  }

  final def filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterLongestOutgoingInterConceptRelationshipPaths(sourceConcept, classTag[DomainMemberRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }

  final def findAllLongestIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterLongestIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept)(_ => true)
  }

  final def filterLongestIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName)(p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath] = {

    filterLongestIncomingInterConceptRelationshipPaths(targetConcept, classTag[DomainAwareRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }

  final def findAllLongestIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterLongestIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)
  }

  final def filterLongestIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName)(p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    filterLongestIncomingInterConceptRelationshipPaths(targetConcept, classTag[DomainMemberRelationship]) { path =>
      path.isElrValid && p(path)
    }
  }

  // Other query methods

  final def findAllOwnOrInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths =
      findAllLongestIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs =
      domainMemberRelationships.map(rel => (rel.elr -> rel.sourceConceptEName))

    val ownElrSourceConceptPairs =
      findAllOutgoingHasHypercubeRelationships(concept).map(rel => (rel.elr -> rel.sourceConceptEName))

    val elrSourceConceptPairs = (inheritedElrSourceConceptPairs ++ ownElrSourceConceptPairs).distinct

    val hasHypercubes =
      elrSourceConceptPairs flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  final def findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllOwnOrInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).mapValues(_.map(_.sourceConceptEName).toSet)
  }

  final def findAllInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths =
      findAllLongestIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs =
      domainMemberRelationships.map(rel => (rel.elr -> rel.sourceConceptEName)).distinct

    val hasHypercubes =
      inheritedElrSourceConceptPairs flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  final def findAllInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).mapValues(_.map(_.sourceConceptEName).toSet)
  }

  final def computeHasHypercubeInheritanceOrSelf: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    val hasHypercubes = findAllHasHypercubeRelationships

    val conceptHasHypercubes: immutable.IndexedSeq[(EName, HasHypercubeRelationship)] =
      hasHypercubes flatMap { hasHypercube =>
        val domainMemberPaths =
          filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(_.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConceptEName).distinct
        val ownOrInheritingConcepts = hasHypercube.primary +: inheritingConcepts

        ownOrInheritingConcepts.map(concept => (concept -> hasHypercube))
      }

    conceptHasHypercubes.groupBy(_._1).mapValues(_.map(_._2).distinct)
  }

  final def computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritanceOrSelf mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).mapValues(_.map(_.sourceConceptEName).toSet)
    }
  }

  final def computeHasHypercubeInheritance: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] = {
    val hasHypercubes = findAllHasHypercubeRelationships

    val conceptHasHypercubes: immutable.IndexedSeq[(EName, HasHypercubeRelationship)] =
      hasHypercubes flatMap { hasHypercube =>
        val domainMemberPaths =
          filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(_.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConceptEName).distinct

        inheritingConcepts.map(concept => (concept -> hasHypercube))
      }

    conceptHasHypercubes.groupBy(_._1).mapValues(_.map(_._2).distinct)
  }

  final def computeHasHypercubeInheritanceReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritance mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).mapValues(_.map(_.sourceConceptEName).toSet)
    }
  }

  // TODO In the dimension member query methods below, mind default members!

  final def findAllMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
        path.firstRelationship.isInstanceOf[DimensionDomainRelationship] &&
          path.firstRelationship.targetConceptEName == domain &&
          path.firstRelationship.elr == dimensionDomainElr
      }

    val result = dimensionDomainPaths.flatMap(_.relationships).map(_.targetConceptEName).toSet
    result
  }

  final def findAllUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
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
    findAllMembers(dimension, domain, dimensionDomainElr).diff(findAllUsableMembers(dimension, domain, dimensionDomainElr))
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
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship) map {
      case (dim, domainElrPairs) =>
        (dim -> findAllMembers(dim, domainElrPairs))
    }
  }

  final def findAllUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship) map {
      case (dim, domainElrPairs) =>
        (dim -> findAllUsableMembers(dim, domainElrPairs))
    }
  }

  final def findAllNonUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship) map {
      case (dim, domainElrPairs) =>
        (dim -> findAllNonUsableMembers(dim, domainElrPairs))
    }
  }

  private def findAllDomainElrPairsPerDimension(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[(EName, String)]] = {
    val hypercubeDimensionRelationships =
      filterOutgoingHypercubeDimensionRelationshipsOnElr(hasHypercubeRelationship.hypercube, hasHypercubeRelationship.effectiveTargetRole)

    val dimensionDomainRelationships =
      hypercubeDimensionRelationships.flatMap(hd => filterOutgoingDimensionDomainRelationshipsOnElr(hd.dimension, hd.effectiveTargetRole))

    val dimensionDomainRelationshipsByDimension = dimensionDomainRelationships.groupBy(_.dimension)

    dimensionDomainRelationshipsByDimension.mapValues(_.map(rel => (rel.domain -> rel.elr)).toSet)
  }
}
