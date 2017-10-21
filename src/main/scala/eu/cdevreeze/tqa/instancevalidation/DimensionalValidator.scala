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

package eu.cdevreeze.tqa.instancevalidation

import scala.collection.immutable
import scala.util.Success
import scala.util.Try

import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.queryapi.DomainAwareRelationshipPath
import eu.cdevreeze.tqa.base.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.base.relationship.AllRelationship
import eu.cdevreeze.tqa.base.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.relationship.NotAllRelationship
import eu.cdevreeze.tqa.instance.ItemFact
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName

/**
 * Dimensional instance validator. It wraps a taxonomy as `TaxonomyApi` instance.
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
final class DimensionalValidator private (
    val taxonomy: TaxonomyApi,
    val hasHypercubesByElrAndPrimary: Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]],
    val hypercubeDimensionsByElrAndHypercube: Map[String, Map[EName, immutable.IndexedSeq[HypercubeDimensionRelationship]]],
    val dimensionDomainsByElrAndDimension: Map[String, Map[EName, immutable.IndexedSeq[DimensionDomain]]],
    val dimensionDefaults: Map[EName, EName],
    val hasHypercubeInheritanceOrSelf: Map[EName, Map[String, Set[EName]]]) {

  /**
   * Validates the given item fact in the given XBRL instance dimensionally. It invokes the
   * equally named validation method taking the fact's concept as first argument and its XBRL context
   * converted to a DimensionalContext as second argument.
   */
  def validateDimensionally(
    itemFact: ItemFact,
    instance: XbrlInstance): Try[Boolean] = {

    val ctxRef = itemFact.contextRef
    val context = instance.allContextsById(ctxRef)
    val dimensionalContext = DimensionalContext.contextToDimensionalContext(context)

    validateDimensionally(itemFact.resolvedName, dimensionalContext)
  }

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
   * Validates the dimensional context in isolation.
   */
  def validateDimensionalContext(
    dimensionalContext: DimensionalContext): Try[Unit] = {

    Try {
      if (dimensionalContext.hasRepeatedDimensions) throw RepeatedDimensionInInstanceError

      dimensionalContext.explicitDimensionMembers.keySet.toSeq.foreach(dim => validateExplicitDimension(dim))

      dimensionalContext.typedDimensions.toSeq.foreach(dim => validateTypedDimension(dim))

      dimensionalContext.explicitDimensionMembers.toSeq.foreach(dimMem => validateExplicitDimensionMember(dimMem._2))

      val usedDefaultDimensionMembers =
        dimensionDefaults.toSet.intersect(dimensionalContext.explicitDimensionMembers.toSet)

      if (usedDefaultDimensionMembers.nonEmpty) {
        throw DefaultValueUsedInInstanceError(usedDefaultDimensionMembers.head._1)
      }
    }
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
        validateDimensionallyAsIfForAllRelationship(dimensionalContext, hh).map(res => !res)
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

    val dimensionalContextElementToValidate =
      if (hasHypercube.contextElement == ContextElement.Segment) {
        dimensionalContextToValidate.dimensionalSegment
      } else {
        dimensionalContextToValidate.dimensionalScenario
      }

    val dimensionsInContext = dimensionalContextElementToValidate.dimensions union {
      dimensionDefaults.keySet.filter(dimensionsToValidate)
    }

    if (dimensionsInContext != dimensionsToValidate) {
      Success(false)
    } else {
      val dimensionDomains: immutable.IndexedSeq[DimensionDomain] =
        hypercubeDimensions.flatMap(hd => filterDimensionDomainsOnHypercubeDimension(hd))

      // Valid if valid for all dimensions

      val explicitDimValidationResult =
        dimensionalContextElementToValidate.explicitDimensionMembers.keySet forall {
          case dim =>
            validateExplicitDimensionValue(
              dim,
              dimensionalContextElementToValidate,
              dimensionDomains.filter(_.dimension == dim))
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
      dimensionDomains.forall(_.dimension == dimension),
      s"Dimension domains not all for dimension $dimension")

    val dimensionValueOption: Option[EName] =
      dimensionalContextElement.explicitDimensionMembers.get(dimension) orElse {
        dimensionDefaults.get(dimension)
      }

    if (dimensionValueOption.isEmpty) {
      false
    } else {
      val dimensionValue = dimensionValueOption.get

      val foundMembers: immutable.IndexedSeq[DimensionDomain.Member] =
        dimensionDomains.flatMap(_.members.get(dimensionValue)).ensuring(_.forall(_.ename == dimensionValue))

      foundMembers.nonEmpty && foundMembers.forall(_.usable)
    }
  }

  private def filterHypercubeDimensionsOnHasHypercube(
    hasHypercube: HasHypercubeRelationship): immutable.IndexedSeq[HypercubeDimensionRelationship] = {

    hypercubeDimensionsByElrAndHypercube.getOrElse(hasHypercube.effectiveTargetRole, Map()).
      getOrElse(hasHypercube.hypercube, immutable.IndexedSeq())
  }

  private def filterDimensionDomainsOnHypercubeDimension(
    hypercubeDimension: HypercubeDimensionRelationship): immutable.IndexedSeq[DimensionDomain] = {

    dimensionDomainsByElrAndDimension.getOrElse(hypercubeDimension.effectiveTargetRole, Map()).
      getOrElse(hypercubeDimension.dimension, immutable.IndexedSeq())
  }

  private def validateExplicitDimension(ename: EName): Unit = {
    val isExplicitDimension = taxonomy.findExplicitDimensionDeclaration(ename).nonEmpty

    if (!isExplicitDimension) throw ExplicitMemberNotExplicitDimensionError(ename)
  }

  private def validateTypedDimension(ename: EName): Unit = {
    val isTypedDimension = taxonomy.findTypedDimensionDeclaration(ename).nonEmpty

    if (!isTypedDimension) throw TypedMemberNotTypedDimensionError(ename)
  }

  private def validateExplicitDimensionMember(ename: EName): Unit = {
    val isMember = taxonomy.findGlobalElementDeclaration(ename).nonEmpty

    if (!isMember) throw ExplicitMemberUndefinedQNameError(ename)
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

object DimensionalValidator {

  def build(taxonomy: TaxonomyApi): DimensionalValidator = {
    val hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship] = taxonomy.findAllHasHypercubeRelationships
    val hypercubeDimensions: immutable.IndexedSeq[HypercubeDimensionRelationship] = taxonomy.findAllHypercubeDimensionRelationships

    val dimensions = hypercubeDimensions.map(_.dimension).toSet
    val dimensionDomains: immutable.IndexedSeq[DimensionDomain] = extractDimensionDomains(taxonomy, dimensions)

    val dimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship] = taxonomy.findAllDimensionDefaultRelationships
    // Assuming no more than 1 dimension-default per dimension
    val dimensionDefaults = dimensionDefaultRelationships.map(rel => (rel.dimension, rel.defaultOfDimension)).toMap

    val hasHypercubeInheritanceOrSelf: Map[EName, Map[String, Set[EName]]] =
      taxonomy.computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps

    new DimensionalValidator(
      taxonomy,
      hasHypercubes.groupBy(_.elr).mapValues(_.groupBy(_.primary)),
      hypercubeDimensions.groupBy(_.elr).mapValues(_.groupBy(_.hypercube)),
      dimensionDomains.groupBy(_.dimensionDomainElr).mapValues(_.groupBy(_.dimension)),
      dimensionDefaults,
      hasHypercubeInheritanceOrSelf)
  }

  private def extractDimensionDomains(taxonomy: TaxonomyApi, dimensions: Set[EName]): immutable.IndexedSeq[DimensionDomain] = {
    val domainAwarePaths =
      dimensions.toIndexedSeq.flatMap(dim => taxonomy.filterOutgoingConsecutiveDomainAwareRelationshipPaths(dim)(path => !path.hasCycle))

    val domainAwarePathsByDimensionDomain: Map[(EName, String), immutable.IndexedSeq[DomainAwareRelationshipPath]] =
      domainAwarePaths.groupBy(path => (path.firstRelationship.sourceConceptEName -> path.firstRelationship.elr))

    val dimensionDomains: immutable.IndexedSeq[DimensionDomain] = domainAwarePathsByDimensionDomain.toIndexedSeq map {
      case ((dimension, dimensionDomainElr), paths) =>
        val domainMembers: Map[EName, DimensionDomain.Member] =
          paths.flatMap(_.relationships).map(rel => DimensionDomain.Member(rel.targetConceptEName, rel.usable)).groupBy(_.ename).
            mapValues(mems => mems.find(m => !m.usable).getOrElse(mems.head))

        new DimensionDomain(dimension, dimensionDomainElr, domainMembers)
    }

    dimensionDomains
  }
}
