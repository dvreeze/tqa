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

import eu.cdevreeze.tqa.base.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.base.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.base.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa.base.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.reflect.ClassTag

/**
 * Purely abstract trait offering a dimensional relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipContainerApi {

  // Finding and filtering relationships without looking at source or target concept

  def findAllDimensionalRelationships: immutable.IndexedSeq[DimensionalRelationship]

  def filterDimensionalRelationships(
      p: DimensionalRelationship => Boolean): immutable.IndexedSeq[DimensionalRelationship]

  def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](relationshipType: ClassTag[A])(
      p: A => Boolean): immutable.IndexedSeq[A]

  def findAllHasHypercubeRelationships: immutable.IndexedSeq[HasHypercubeRelationship]

  def filterHasHypercubeRelationships(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship]

  def findAllHypercubeDimensionRelationships: immutable.IndexedSeq[HypercubeDimensionRelationship]

  def filterHypercubeDimensionRelationships(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship]

  def findAllDimensionDomainRelationships: immutable.IndexedSeq[DimensionDomainRelationship]

  def filterDimensionDomainRelationships(
      p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship]

  def findAllDomainMemberRelationships: immutable.IndexedSeq[DomainMemberRelationship]

  def filterDomainMemberRelationships(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship]

  def findAllDimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship]

  def filterDimensionDefaultRelationships(
      p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all has-hypercube relationships that are outgoing from the given concept.
   */
  def findAllOutgoingHasHypercubeRelationships(sourceConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are outgoing from the given concept.
   */
  def filterOutgoingHasHypercubeRelationships(sourceConcept: EName)(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingHasHypercubeRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Finds all hypercube-dimension relationships that are outgoing from the given concept.
   */
  def findAllOutgoingHypercubeDimensionRelationships(
      sourceConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are outgoing from the given concept.
   */
  def filterOutgoingHypercubeDimensionRelationships(sourceConcept: EName)(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingHypercubeDimensionRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Finds all consecutive hypercube-dimension relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingHypercubeDimensionRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveHypercubeDimensionRelationships(
      relationship: HasHypercubeRelationship): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Finds all dimension-domain relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDomainRelationships(
      sourceConcept: EName): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Filters dimension-domain relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDomainRelationships(sourceConcept: EName)(
      p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Filters dimension-domain relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDimensionDomainRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Finds all consecutive dimension-domain relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingDimensionDomainRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveDimensionDomainRelationships(
      relationship: HypercubeDimensionRelationship): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Finds all domain-member relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDomainMemberRelationships(sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are outgoing from the given concept.
   */
  def filterOutgoingDomainMemberRelationships(sourceConcept: EName)(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDomainMemberRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Finds all consecutive domain-member relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingDomainMemberRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveDomainMemberRelationships(
      relationship: DomainAwareRelationship): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Finds all dimension-default relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDefaultRelationships(
      sourceConcept: EName): immutable.IndexedSeq[DimensionDefaultRelationship]

  /**
   * Filters dimension-default relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDefaultRelationships(sourceConcept: EName)(
      p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship]

  /**
   * Filters dimension-default relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDimensionDefaultRelationshipsOnElr(
      sourceConcept: EName,
      elr: String): immutable.IndexedSeq[DimensionDefaultRelationship]

  // Finding and filtering incoming relationships

  /**
   * Finds all domain-member relationships that are incoming to the given concept.
   */
  def findAllIncomingDomainMemberRelationships(targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are incoming to the given concept.
   */
  def filterIncomingDomainMemberRelationships(targetConcept: EName)(
      p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Finds all "domain-aware" relationships that are incoming to the given concept.
   */
  def findAllIncomingDomainAwareRelationships(targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationship]

  /**
   * Filters "domain-aware" relationships that are incoming to the given concept.
   */
  def filterIncomingDomainAwareRelationships(targetConcept: EName)(
      p: DomainAwareRelationship => Boolean): immutable.IndexedSeq[DomainAwareRelationship]

  /**
   * Finds all hypercube-dimension relationships that are incoming to the given (dimension) concept.
   */
  def findAllIncomingHypercubeDimensionRelationships(
      targetConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are incoming to the given (dimension) concept.
   */
  def filterIncomingHypercubeDimensionRelationships(targetConcept: EName)(
      p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Finds all has-hypercube relationships that are incoming to the given (hypercube) concept.
   */
  def findAllIncomingHasHypercubeRelationships(targetConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are incoming to the given (hypercube) concept.
   */
  def filterIncomingHasHypercubeRelationships(targetConcept: EName)(
      p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship]

  // Filtering outgoing and incoming relationship paths

  /**
   * Returns `filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveDomainAwareRelationshipPaths(
      sourceConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Filters the consecutive (!) dimension-domain-or-domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Returns `filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveDomainMemberRelationshipPaths(
      sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Filters the consecutive (!) domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveDomainAwareRelationshipPaths(
      targetConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Filters the consecutive (!) dimension-domain-or-domain-member relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveDomainMemberRelationshipPaths(
      targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Filters the consecutive (!) domain-member relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on encountering a cycle in a path it stops growing beyond a certain path length.
   */
  def filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath]

  // Other query methods

  /**
   * Finds all own or inherited has-hypercubes. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllOwnOrInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Finds all own or inherited has-hypercubes as a Map from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]]

  /**
   * Finds all inherited has-hypercubes. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubes(concept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Finds all inherited has-hypercubes as a Map from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]]

  /**
   * Finds all own or inherited has-hypercubes per concept that pass the predicate.
   * See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is potentially an expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and
   * should typically be called as few times as possible.
   */
  def computeFilteredHasHypercubeInheritanceOrSelf(
      p: HasHypercubeRelationship => Boolean): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept that pass the predicate.
   * See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is potentially an expensive bulk version of method findAllInheritedHasHypercubes, and
   * should typically be called as few times as possible.
   */
  def computeFilteredHasHypercubeInheritance(
      p: HasHypercubeRelationship => Boolean): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritanceOrSelf(_ => true)
   * }}}
   */
  def computeHasHypercubeInheritanceOrSelf: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept returning Maps from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]]

  /**
   * Finds all inherited has-hypercubes per concept. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritance(_ => true)
   * }}}
   */
  def computeHasHypercubeInheritance: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept returning Maps from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]]

  /**
   * Finds all own or inherited has-hypercubes per concept for the given ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritanceOrSelf(_.elr == elr)
   * }}}
   */
  def computeHasHypercubeInheritanceOrSelfForElr(
      elr: String): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept, for the given ELR, returning Sets of all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(elr: String): Map[EName, Set[EName]]

  /**
   * Finds all inherited has-hypercubes per concept for the given ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritance(_.elr == elr)
   * }}}
   */
  def computeHasHypercubeInheritanceForElr(elr: String): Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept, for the given ELR, returning Sets of all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceForElrReturningPrimaries(elr: String): Map[EName, Set[EName]]

  /**
   * Finds all members in the given domain, for the given ELR. This method is more general than the corresponding findAllMembers
   * method that takes a dimension as first parameter. This method is also applicable to Extensible Enumerations 2.0, to
   * find a domain of (allowed or disallowed) enumeration values.
   */
  def findAllMembers(domain: EName, elr: String): Set[EName]

  /**
   * Finds all usable members in the given domain, for the given ELR. This method is more general than the corresponding
   * findAllUsableMembers method that takes a dimension as first parameter. This method is also applicable to Extensible Enumerations 2.0,
   * to find a domain of allowed enumeration values.
   */
  def findAllUsableMembers(domain: EName, elr: String, headUsable: Boolean): Set[EName]

  /**
   * Finds all non-usable members in the given domain, for the given ELR. This method is more general than the corresponding
   * findAllNonUsableMembers method that takes a dimension as first parameter. This method is also applicable to Extensible
   * Enumerations 2.0, to find a domain of disallowed enumeration values.
   */
  def findAllNonUsableMembers(domain: EName, elr: String, headUsable: Boolean): Set[EName]

  /**
   * Finds all members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   *
   * This method is equivalent to but more user-friendly than:
   * {{{
   * findAllMembers(domain, targetElr)
   * }}}
   * where targetElr is the target ELR of the dimension-domain relationship.
   */
  def findAllMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all usable members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   *
   * This method is equivalent to but more user-friendly than:
   * {{{
   * findAllUsableMembers(domain, targetElr, headUsable)
   * }}}
   * where headUsable is taken from the xbrldt:usable attribute on the dimension-domain relationship,
   * and targetElr is the target ELR of the dimension-domain relationship.
   */
  def findAllUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all non-usable members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   *
   * This method is equivalent to but more user-friendly than:
   * {{{
   * findAllNonUsableMembers(domain, targetElr, headUsable)
   * }}}
   * where headUsable is taken from the xbrldt:usable attribute on the dimension-domain relationship,
   * and targetElr is the target ELR of the dimension-domain relationship.
   */
  def findAllNonUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all members in the given effective domain of the given dimension.
   */
  def findAllMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all usable members in the given effective domain of the given dimension. If a member is
   * usable in one dimension-domain but not usable in another one, it is considered not usable.
   */
  def findAllUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all non-usable members in the given effective domain of the given dimension. If a member is
   * usable in one dimension-domain but not usable in another one, it is considered not usable.
   */
  def findAllNonUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]

  /**
   * Finds all usable (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]

  /**
   * Finds all non-usable (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllNonUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]
}
