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
import scala.util.Success
import scala.util.Try

import eu.cdevreeze.tqa.base.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.base.relationship.AllRelationship
import eu.cdevreeze.tqa.base.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.relationship.NotAllRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Dimensional taxonomy, geared towards fast dimensional instance validation queries. It wraps a normal
 * taxonomy as `TaxonomyApi`.
 *
 * Instances of this class are expensive to create, and should be created only once per DTS and then
 * retained in memory.
 *
 * This class is most useful if the taxonomy from which it is instantiated is known to be XBRL Core and Dimensions
 * valid.
 *
 * This class does not offer any schema validation queries, so typed dimension validation queries miss
 * the schema validation part for typed dimension members.
 *
 * @author Chris de Vreeze
 */
final class DimensionalTaxonomy private (
    val taxonomy: TaxonomyApi,
    val hasHypercubesByElrAndPrimary: Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]],
    val hypercubeDimensionsByElrAndHypercube: Map[String, Map[EName, immutable.IndexedSeq[HypercubeDimensionRelationship]]],
    val dimensionDomainsByElrAndDimension: Map[String, Map[EName, immutable.IndexedSeq[DimensionalTaxonomy.DimensionDomain]]],
    val dimensionDefaults: Map[EName, EName],
    val hasHypercubeInheritanceOrSelf: Map[EName, Map[String, Set[EName]]]) {

  import DimensionalTaxonomy._

  def dimensionsHavingDefault: Set[EName] = dimensionDefaults.keySet

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
    hypercubeDimension: HypercubeDimensionRelationship): immutable.IndexedSeq[DimensionDomain] = {

    dimensionDomainsByElrAndDimension.getOrElse(hypercubeDimension.effectiveTargetRole, Map()).
      getOrElse(hypercubeDimension.dimension, immutable.IndexedSeq())
  }

  // Instance dimensional validation support

  /**
   * Validates the given concept and dimensional context dimensionally. Validation does not include
   * schema validation for typed dimension members.
   *
   * This method does not validate the dimensional context itself, isolated from the concept.
   * That is, it does not call method `validateDimensionalContext`.
   */
  def validateDimensionally(
    concept: EName,
    dimensionalContext: DimensionalContext): Try[Boolean] = {

    val ownOrInheritedElrToPrimaryMaps: Map[String, Set[EName]] =
      hasHypercubeInheritanceOrSelf.getOrElse(concept, Map())

    val ownOrInheritedHasHypercubesByElrAndPrimary =
      filterHasHypercubes(ownOrInheritedElrToPrimaryMaps)

    if (ownOrInheritedHasHypercubesByElrAndPrimary.isEmpty) {
      // No has-hypercubes
      Success(true)
    } else {
      val elrValidationResults =
        ownOrInheritedHasHypercubesByElrAndPrimary.toSeq map {
          case (elr, hasHypercubesByPrimary) =>
            val primaries = hasHypercubesByPrimary.keySet

            validateDimensionallyForElr(dimensionalContext, elr, primaries)
        }

      // Valid if valid for at least one hypercube

      elrValidationResults.find(_.isFailure) getOrElse {
        elrValidationResults.find(_.toOption.contains(true)).getOrElse(Success(false))
      }
    }
  }

  /**
   * Calls `dimensionalContext.validate(this)`.
   */
  def validateDimensionalContext(
    dimensionalContext: DimensionalContext): Try[Unit] = {

    dimensionalContext.validate(this)
  }

  def validateDimensionallyForElr(
    dimensionalContext: DimensionalContext,
    hasHypercubeElr: String,
    ownOrInheritedPrimaries: Set[EName]): Try[Boolean] = {

    val hasHypercubes =
      hasHypercubesByElrAndPrimary.getOrElse(hasHypercubeElr, Map()).filterKeys(ownOrInheritedPrimaries).
        values.flatten.toIndexedSeq

    if (hasHypercubes.isEmpty) {
      Success(false)
    } else {
      val validationResults =
        hasHypercubes.map(hh => validateDimensionallyForHasHypercube(dimensionalContext, hh))

      // Valid if valid for all hypercubes in this base set (but minding notAll relationships as well)

      validationResults.find(_.isFailure) getOrElse {
        validationResults.find(_.toOption.contains(false)).getOrElse(Success(true))
      }
    }
  }

  def validateDimensionallyForHasHypercube(
    dimensionalContext: DimensionalContext,
    hasHypercube: HasHypercubeRelationship): Try[Boolean] = {

    hasHypercube match {
      case hh: AllRelationship =>
        validateDimensionallyAsIfForAllRelationship(dimensionalContext, hh)
      case hh: NotAllRelationship =>
        validateDimensionallyAsIfForAllRelationship(dimensionalContext, hh).map(res => !res) // TODO Correct?
    }
  }

  def validateDimensionallyAsIfForAllRelationship(
    dimensionalContext: DimensionalContext,
    hasHypercube: HasHypercubeRelationship): Try[Boolean] = {

    val hypercubeDimensions = filterHypercubeDimensionsOnHasHypercube(hasHypercube)

    // TODO Is this correct?

    val dimensionsToValidate: Set[EName] = hypercubeDimensions.map(_.dimension).toSet

    val dimensionalContextToValidate =
      if (hasHypercube.closed) {
        dimensionalContext
      } else {
        dimensionalContext.filterDimensions(dimensionsToValidate)
      }

    // TODO Type for ContextElement "enumeration"

    val dimensionalContextElementToValidate =
      if (hasHypercube.contextElement == "segment") {
        dimensionalContextToValidate.dimensionalSegment
      } else {
        dimensionalContextToValidate.dimensionalScenario
      }

    if (dimensionalContextElementToValidate.dimensions.union(dimensionsHavingDefault) != dimensionsToValidate) {
      Success(false)
    } else {
      val dimensionDomains: immutable.IndexedSeq[DimensionDomain] =
        hypercubeDimensions.flatMap(hd => filterDimensionDomainsOnHypercubeDimension(hd))

      // Valid if valid for all dimensions

      val explicitDimValidationResult =
        dimensionalContextElementToValidate.explicitDimensionMembers.keySet forall {
          case dim =>
            validateExplicitDimensionValue(dim, dimensionalContextElementToValidate, dimensionDomains)
        }

      Success(explicitDimValidationResult)
    }
  }

  /**
   * Validate a dimension value (possibly default) against the effective domain of the dimension
   */
  def validateExplicitDimensionValue(
    dimension: EName,
    dimensionalContextElement: DimensionalContextElement,
    dimensionDomains: immutable.IndexedSeq[DimensionDomain]): Boolean = {

    require(
      dimensionDomains.map(_.dimension).toSet == Set(dimension),
      s"Expected non-empty dimension-domain collection, all for dimension $dimension")

    val dimensionValueOption: Option[EName] =
      dimensionalContextElement.explicitDimensionMembers.get(dimension).orElse(dimensionDefaults.get(dimension))

    if (dimensionValueOption.isEmpty) {
      false
    } else {
      val dimensionValue = dimensionValueOption.get

      val foundMembers: immutable.IndexedSeq[Member] =
        dimensionDomains.flatMap(_.domainMembers.get(dimensionValue)).ensuring(_.forall(_.ename == dimensionValue))

      foundMembers.nonEmpty && foundMembers.forall(_.usable)
    }
  }

  private def filterHasHypercubes(
    elrToPrimaryMaps: Map[String, Set[EName]]): Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]] = {

    val ownOrInheritedHasHypercubesByElrAndPrimary =
      (hasHypercubesByElrAndPrimary.filterKeys(elrToPrimaryMaps.keySet).toIndexedSeq map {
        case (elr, primaryToHasHypercubeMap) =>
          val primaries = elrToPrimaryMaps.getOrElse(elr, Set())

          val filteredPrimaryToHasHypercubeMap =
            primaryToHasHypercubeMap.filterKeys(primaries).filter(_._2.nonEmpty)

          elr -> filteredPrimaryToHasHypercubeMap
      } filter {
        case (elr, primaryToHasHypercubeMap) =>
          primaryToHasHypercubeMap.nonEmpty
      }).toMap

    ownOrInheritedHasHypercubesByElrAndPrimary.
      ensuring(_.values.forall(primaryHasHypercubeMap => primaryHasHypercubeMap.nonEmpty && primaryHasHypercubeMap.values.forall(_.nonEmpty)))
  }
}

object DimensionalTaxonomy {

  def build(taxonomy: TaxonomyApi): DimensionalTaxonomy = {
    val hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship] = taxonomy.findAllHasHypercubeRelationships
    val hypercubeDimensions: immutable.IndexedSeq[HypercubeDimensionRelationship] = taxonomy.findAllHypercubeDimensionRelationships

    val dimensions = hypercubeDimensions.map(_.dimension).toSet
    val dimensionDomains: immutable.IndexedSeq[DimensionDomain] = extractDimensionDomains(taxonomy, dimensions)

    val dimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship] = taxonomy.findAllDimensionDefaultRelationships
    // Assuming no more than 1 dimension-default per dimension
    val dimensionDefaults = dimensionDefaultRelationships.map(rel => (rel.dimension, rel.defaultOfDimension)).toMap

    val hasHypercubeInheritanceOrSelf: Map[EName, Map[String, Set[EName]]] =
      taxonomy.computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps

    new DimensionalTaxonomy(
      taxonomy,
      hasHypercubes.groupBy(_.elr).mapValues(_.groupBy(_.primary)),
      hypercubeDimensions.groupBy(_.elr).mapValues(_.groupBy(_.hypercube)),
      dimensionDomains.groupBy(_.dimensionDomainElr).mapValues(_.groupBy(_.dimension)),
      dimensionDefaults,
      hasHypercubeInheritanceOrSelf)
  }

  private def extractDimensionDomains(taxonomy: TaxonomyApi, dimensions: Set[EName]): immutable.IndexedSeq[DimensionDomain] = {
    val domainAwarePaths =
      dimensions.toIndexedSeq.flatMap(dim => taxonomy.filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(dim)(path => !path.hasCycle))

    val domainAwarePathsByDimensionDomain: Map[(EName, String), immutable.IndexedSeq[taxonomy.DomainAwareRelationshipPath]] =
      domainAwarePaths.groupBy(path => (path.firstRelationship.sourceConceptEName -> path.firstRelationship.elr))

    val dimensionDomains: immutable.IndexedSeq[DimensionDomain] = domainAwarePathsByDimensionDomain.toIndexedSeq map {
      case ((dimension, dimensionDomainElr), paths) =>
        val domainMembers: Map[EName, Member] =
          paths.flatMap(_.relationships).map(rel => Member(rel.targetConceptEName, rel.usable)).groupBy(_.ename).
            mapValues(mems => mems.find(m => !m.usable).getOrElse(mems.head))

        new DimensionDomain(dimension, dimensionDomainElr, domainMembers)
    }

    dimensionDomains
  }

  final class DimensionDomain(
      val dimension: EName,
      val dimensionDomainElr: String,
      val domainMembers: Map[EName, Member]) {

    require(domainMembers.forall(kv => kv._2.ename == kv._1), s"Corrupt dimension domain")
  }

  final case class Member(ename: EName, usable: Boolean)

  // Dimensional instance data

  sealed abstract class DimensionalContextElement(
      val explicitDimensionMembers: Map[EName, EName],
      val typedDimensions: Set[EName]) {

    def filterDimensions(dimensions: Set[EName]): DimensionalContextElement

    def dimensions: Set[EName] = {
      explicitDimensionMembers.keySet.union(typedDimensions)
    }

    final def hasRepeatedDimensions: Boolean = explicitDimensionMembers.keySet.intersect(typedDimensions).nonEmpty
  }

  final case class DimensionalSegment(
      override val explicitDimensionMembers: Map[EName, EName],
      override val typedDimensions: Set[EName]) extends DimensionalContextElement(explicitDimensionMembers, typedDimensions) {

    final def filterDimensions(dimensions: Set[EName]): DimensionalSegment = {
      DimensionalSegment(
        explicitDimensionMembers.filterKeys(dimensions),
        typedDimensions.intersect(dimensions))
    }
  }

  final case class DimensionalScenario(
      override val explicitDimensionMembers: Map[EName, EName],
      override val typedDimensions: Set[EName]) extends DimensionalContextElement(explicitDimensionMembers, typedDimensions) {

    final def filterDimensions(dimensions: Set[EName]): DimensionalScenario = {
      DimensionalScenario(
        explicitDimensionMembers.filterKeys(dimensions),
        typedDimensions.intersect(dimensions))
    }
  }

  final case class DimensionalContext(
      dimensionalSegment: DimensionalSegment,
      dimensionalScenario: DimensionalScenario) {

    def explicitDimensionMembers: Map[EName, EName] = {
      dimensionalSegment.explicitDimensionMembers ++ dimensionalScenario.explicitDimensionMembers
    }

    def typedDimensions: Set[EName] = {
      dimensionalSegment.typedDimensions.union(dimensionalScenario.typedDimensions)
    }

    def dimensions: Set[EName] = {
      explicitDimensionMembers.keySet.union(typedDimensions)
    }

    def filterDimensions(dimensions: Set[EName]): DimensionalContext = {
      DimensionalContext(
        dimensionalSegment.filterDimensions(dimensions),
        dimensionalScenario.filterDimensions(dimensions))
    }

    def hasRepeatedDimensions: Boolean = {
      dimensionalSegment.hasRepeatedDimensions || dimensionalScenario.hasRepeatedDimensions ||
        dimensionalSegment.dimensions.intersect(dimensionalScenario.dimensions).nonEmpty
    }

    def validate(taxonomy: DimensionalTaxonomy): Try[Unit] = {
      Try {
        if (hasRepeatedDimensions) throw RepeatedDimensionInInstanceError

        explicitDimensionMembers.keySet.toSeq.foreach(dim => validateExplicitDimension(dim, taxonomy))

        typedDimensions.toSeq.foreach(dim => validateTypedDimension(dim, taxonomy))

        explicitDimensionMembers.toSeq.foreach(dimMem => validateExplicitDimensionMember(dimMem._2, taxonomy))

        val usedDefaultDimensionMembers =
          taxonomy.dimensionDefaults.toSet.intersect(explicitDimensionMembers.toSet)

        if (usedDefaultDimensionMembers.nonEmpty) {
          throw DefaultValueUsedInInstanceError(usedDefaultDimensionMembers.head._1)
        }
      }
    }

    private def validateExplicitDimension(ename: EName, taxonomy: DimensionalTaxonomy): Unit = {
      val isExplicitDimension = taxonomy.taxonomy.findExplicitDimensionDeclaration(ename).nonEmpty

      if (!isExplicitDimension) throw ExplicitMemberNotExplicitDimensionError(ename)
    }

    private def validateTypedDimension(ename: EName, taxonomy: DimensionalTaxonomy): Unit = {
      val isTypedDimension = taxonomy.taxonomy.findTypedDimensionDeclaration(ename).nonEmpty

      if (!isTypedDimension) throw TypedMemberNotTypedDimensionError(ename)
    }

    private def validateExplicitDimensionMember(ename: EName, taxonomy: DimensionalTaxonomy): Unit = {
      val isMember = taxonomy.taxonomy.findGlobalElementDeclaration(ename).nonEmpty

      if (!isMember) throw ExplicitMemberUndefinedQNameError(ename)
    }
  }

  // Exceptions encountered during validation. Error xbrldie:PrimaryItemDimensionallyInvalidError does not count
  // as an exception here, but is treated as a normal Boolean validation result. Error xbrldie:IllegalTypedDimensionContentError
  // is absent due to the lack of schema validation (for typed dimension members) in this context.

  sealed trait ValidationException extends RuntimeException

  final case class DefaultValueUsedInInstanceError(dimension: EName) extends ValidationException

  case object RepeatedDimensionInInstanceError extends ValidationException

  final case class ExplicitMemberNotExplicitDimensionError(ename: EName) extends ValidationException

  final case class TypedMemberNotTypedDimensionError(ename: EName) extends ValidationException

  final case class ExplicitMemberUndefinedQNameError(ename: EName) extends ValidationException
}
