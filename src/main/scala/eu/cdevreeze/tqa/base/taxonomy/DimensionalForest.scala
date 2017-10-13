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

package eu.cdevreeze.tqa.base.taxonomy

import scala.collection.immutable

import eu.cdevreeze.tqa.base.dom.DimensionDeclaration
import eu.cdevreeze.tqa.base.dom.HypercubeDeclaration
import eu.cdevreeze.tqa.base.relationship.AllRelationship
import eu.cdevreeze.tqa.base.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.relationship.NotAllRelationship
import eu.cdevreeze.tqa.base.queryapi.TaxonomyApi
import eu.cdevreeze.yaidom.core.EName

/**
 * Collection of dimensional trees. Only the dimensional trees themselves are represented, not inheritance
 * of dimensional trees.
 *
 * Instances of this class are expensive to create, and should be created only once per DTS and then
 * retained in memory. This class is designed for quick searches through dimensional trees. It is ideal
 * for dimensional XBRL instance validation.
 *
 * This class is most useful if the taxonomy from which it is instantiated is XBRL Core and Dimensions
 * valid.
 *
 * @author Chris de Vreeze
 */
final class DimensionalForest private (
    val taxonomy: TaxonomyApi,
    val hasHypercubesByElrAndPrimary: Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]],
    val hypercubeDimensionsByElrAndHypercube: Map[String, Map[EName, immutable.IndexedSeq[HypercubeDimensionRelationship]]],
    val dimensionDomainsByElrAndDimension: Map[String, Map[EName, immutable.IndexedSeq[DimensionalForest.DimensionDomain]]],
    val dimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship],
    val hypercubeDeclarationsByEName: Map[EName, HypercubeDeclaration],
    val dimensionDeclarationsByEName: Map[EName, DimensionDeclaration]) {

  def filterHasHypercubesOnElrAndPrimaries(
    elr: String,
    primaries: Set[EName]): immutable.IndexedSeq[HasHypercubeRelationship] = {

    hasHypercubesByElrAndPrimary.getOrElse(elr, Map()).filterKeys(primaries).values.flatten.toIndexedSeq
  }

  def filterHasHypercubesOnElrAndPrimary(
    elr: String,
    primary: EName): immutable.IndexedSeq[HasHypercubeRelationship] = {

    hasHypercubesByElrAndPrimary.getOrElse(elr, Map()).getOrElse(primary, immutable.IndexedSeq())
  }

  def filterHypercubeDimensionsOnHasHypercube(
    hasHypercube: HasHypercubeRelationship): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    hypercubeDimensionsByElrAndHypercube.getOrElse(hasHypercube.effectiveTargetRole, Map()).
      getOrElse(hasHypercube.hypercube, immutable.IndexedSeq())
  }

  def filterDimensionDomainsOnHypercubeDimension(
    hypercubeDimension: HypercubeDimensionRelationship): immutable.IndexedSeq[DimensionalForest.DimensionDomain] = {

    dimensionDomainsByElrAndDimension.getOrElse(hypercubeDimension.effectiveTargetRole, Map()).
      getOrElse(hypercubeDimension.dimension, immutable.IndexedSeq())
  }

  def findAllDefaultDimensionMembers: Map[EName, EName] = {
    // Assuming no more than 1 dimension-default per dimension
    dimensionDefaultRelationships.map(rel => (rel.dimension, rel.defaultOfDimension)).toMap
  }

  // Instance dimensional context validation support

  def isDimensionallyValid(
    dimensionalContext: DimensionalForest.DimensionalContext,
    ownOrInheritedElrToPrimaryMaps: Map[String, Set[EName]]): Boolean = {

    val ownOrInheritedHasHypercubesByElrAndPrimary =
      filterHasHypercubes(ownOrInheritedElrToPrimaryMaps)

    ownOrInheritedHasHypercubesByElrAndPrimary.isEmpty || {
      ownOrInheritedHasHypercubesByElrAndPrimary exists {
        case (elr, hasHypercubesByPrimary) =>
          val primaries = hasHypercubesByPrimary.keySet

          isDimensionallyValidForElr(dimensionalContext, elr, primaries)
      }
    }
  }

  def isDimensionallyValidForElr(
    dimensionalContext: DimensionalForest.DimensionalContext,
    hasHypercubeElr: String,
    ownOrInheritedPrimaries: Set[EName]): Boolean = {

    val hasHypercubes =
      hasHypercubesByElrAndPrimary.getOrElse(hasHypercubeElr, Map()).filterKeys(ownOrInheritedPrimaries).
        values.flatten.toIndexedSeq

    hasHypercubes.nonEmpty &&
      hasHypercubes.forall(hh => isDimensionallyValidForHasHypercube(dimensionalContext, hh))
  }

  def isDimensionallyValidForHasHypercube(
    dimensionalContext: DimensionalForest.DimensionalContext,
    hasHypercube: HasHypercubeRelationship): Boolean = {

    hasHypercube match {
      case hh: AllRelationship =>
        isDimensionallyValidForAllRelationship(dimensionalContext, hh)
      case hh: NotAllRelationship =>
        isDimensionallyInvalidForNotAllRelationship(dimensionalContext, hh)
    }
  }

  def isDimensionallyValidForAllRelationship(
    dimensionalContext: DimensionalForest.DimensionalContext,
    allRelationship: AllRelationship): Boolean = {

    // TODO Implement
    ???
  }

  def isDimensionallyInvalidForNotAllRelationship(
    dimensionalContext: DimensionalForest.DimensionalContext,
    notAllRelationship: NotAllRelationship): Boolean = {

    // TODO Implement
    ???
  }

  def explicitDimensionMemberIsValid(
    member: EName,
    hypercubeDimension: HypercubeDimensionRelationship): Boolean = {

    // TODO Implement
    ???
  }

  private def filterHasHypercubes(
    ownOrInheritedElrToPrimaryMaps: Map[String, Set[EName]]): Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]] = {

    val ownOrInheritedHasHypercubesByElrAndPrimary =
      (hasHypercubesByElrAndPrimary.filterKeys(ownOrInheritedElrToPrimaryMaps.keySet).toIndexedSeq map {
        case (elr, primaryToHasHypercubeMap) =>
          val primaries = ownOrInheritedElrToPrimaryMaps.getOrElse(elr, Set())

          val filteredPrimaryToHasHypercubeMap =
            primaryToHasHypercubeMap.filterKeys(primaries).filter(_._2.nonEmpty)

          elr -> filteredPrimaryToHasHypercubeMap
      } filter {
        case (elr, primaryToHasHypercubeMap) => primaryToHasHypercubeMap.nonEmpty
      }).toMap

    ownOrInheritedHasHypercubesByElrAndPrimary.ensuring(_.values.forall(_.nonEmpty))
  }
}

object DimensionalForest {

  def build(taxonomy: TaxonomyApi): DimensionalForest = {
    val hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship] = taxonomy.findAllHasHypercubeRelationships
    val hypercubeDimensions: immutable.IndexedSeq[HypercubeDimensionRelationship] = taxonomy.findAllHypercubeDimensionRelationships

    // TODO Implement
    val dimensionDomains: immutable.IndexedSeq[DimensionalForest.DimensionDomain] = ???

    val dimensionDefaults: immutable.IndexedSeq[DimensionDefaultRelationship] = taxonomy.findAllDimensionDefaultRelationships

    val hypercubeDeclarationsByEName: Map[EName, HypercubeDeclaration] =
      taxonomy.findAllHypercubeDeclarations.groupBy(_.targetEName).mapValues(_.head)

    val dimensionDeclarationsByEName: Map[EName, DimensionDeclaration] =
      taxonomy.findAllDimensionDeclarations.groupBy(_.targetEName).mapValues(_.head)

    new DimensionalForest(
      taxonomy,
      hasHypercubes.groupBy(_.elr).mapValues(_.groupBy(_.primary)),
      hypercubeDimensions.groupBy(_.elr).mapValues(_.groupBy(_.hypercube)),
      dimensionDomains.groupBy(_.elr).mapValues(_.groupBy(_.dimension)),
      dimensionDefaults,
      hypercubeDeclarationsByEName,
      dimensionDeclarationsByEName)
  }

  final class DimensionDomain(
      val dimension: EName,
      val elr: String,
      val domain: DimensionalForest.Member,
      val domainMembers: Map[EName, DimensionalForest.Member]) {

    require(domainMembers.forall(kv => kv._2.ename == kv._1), s"Corrupt dimension domain")
  }

  final case class Member(ename: EName, usable: Boolean)

  // Dimensional instance data

  sealed abstract class DimensionalContextElement(
    val explicitDimensionMembers: Map[EName, EName],
    val typedDimensions: Set[EName])

  final case class DimensionalSegment(
    override val explicitDimensionMembers: Map[EName, EName],
    override val typedDimensions: Set[EName]) extends DimensionalContextElement(explicitDimensionMembers, typedDimensions)

  final case class DimensionalScenario(
    override val explicitDimensionMembers: Map[EName, EName],
    override val typedDimensions: Set[EName]) extends DimensionalContextElement(explicitDimensionMembers, typedDimensions)

  final case class DimensionalContext(
      dimensionalSegment: DimensionalSegment,
      dimensionalScenario: DimensionalScenario) {

    def explicitDimensionMembers: Map[EName, EName] = {
      dimensionalSegment.explicitDimensionMembers ++ dimensionalScenario.explicitDimensionMembers
    }

    def typedDimensions: Set[EName] = {
      dimensionalSegment.typedDimensions.union(dimensionalScenario.typedDimensions)
    }
  }
}
