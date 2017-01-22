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

import eu.cdevreeze.tqa.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a dimensional relationship query API.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipContainerApi {

  type DomainMemberRelationshipPath = InterConceptRelationshipPath[DomainMemberRelationship]

  type DomainAwareRelationshipPath = InterConceptRelationshipPath[DomainAwareRelationship]

  // Finding and filtering relationships without looking at source or target concept

  def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

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
  def findAllOutgoingHasHypercubeRelationships(
    sourceConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are outgoing from the given concept.
   */
  def filterOutgoingHasHypercubeRelationships(
    sourceConcept: EName)(p: HasHypercubeRelationship => Boolean): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Finds all hypercube-dimension relationships that are outgoing from the given concept.
   */
  def findAllOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are outgoing from the given concept.
   */
  def filterOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName)(p: HypercubeDimensionRelationship => Boolean): immutable.IndexedSeq[HypercubeDimensionRelationship]

  /**
   * Finds all dimension-domain relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDomainRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Filters dimension-domain relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDomainRelationships(
    sourceConcept: EName)(p: DimensionDomainRelationship => Boolean): immutable.IndexedSeq[DimensionDomainRelationship]

  /**
   * Finds all domain-member relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDomainMemberRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are outgoing from the given concept.
   */
  def filterOutgoingDomainMemberRelationships(
    sourceConcept: EName)(p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Finds all dimension-default relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDefaultRelationships(
    sourceConcept: EName): immutable.IndexedSeq[DimensionDefaultRelationship]

  /**
   * Filters dimension-default relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDefaultRelationships(
    sourceConcept: EName)(p: DimensionDefaultRelationship => Boolean): immutable.IndexedSeq[DimensionDefaultRelationship]

  // Finding and filtering incoming relationships

  /**
   * Finds all domain-member relationships that are incoming to the given concept.
   */
  def findAllIncomingDomainMemberRelationships(
    targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are incoming to the given concept.
   */
  def filterIncomingDomainMemberRelationships(
    targetConcept: EName)(p: DomainMemberRelationship => Boolean): immutable.IndexedSeq[DomainMemberRelationship]

  // Filtering outgoing and incoming relationship paths

  /**
   * Returns `filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllLongestOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Filters the longest consecutive (!) dimension-domain-or-domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   *
   * It is a dangerous method in that termination is not guaranteed, but may depend on the passed
   * relationship path predicate. For safety, make sure that the predicate detects cycles and returns
   * false on detecting them.
   */
  def filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName)(
      p: DomainAwareRelationshipPath => Boolean): immutable.IndexedSeq[DomainAwareRelationshipPath]

  /**
   * Returns `filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllLongestOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Filters the longest consecutive (!) domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   *
   * It is a dangerous method in that termination is not guaranteed, but may depend on the passed
   * relationship path predicate. For safety, make sure that the predicate detects cycles and returns
   * false on detecting them.
   */
  def filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName)(
      p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Returns `filterLongestIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllLongestIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName): immutable.IndexedSeq[DomainMemberRelationshipPath]

  /**
   * Filters the longest consecutive (!) domain-member relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   *
   * It is a dangerous method in that termination is not guaranteed, but may depend on the passed
   * relationship path predicate. For safety, make sure that the predicate detects cycles and returns
   * false on detecting them.
   */
  def filterLongestIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName)(p: DomainMemberRelationshipPath => Boolean): immutable.IndexedSeq[DomainMemberRelationshipPath]

  // Other query methods

  /**
   * Finds all inherited has-hypercubes. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubes(targetConcept: EName): immutable.IndexedSeq[HasHypercubeRelationship]

  /**
   * Finds all inherited has-hypercubes as a Map from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubesAsElrToPrimariesMap(targetConcept: EName): Map[String, Set[EName]]
}
