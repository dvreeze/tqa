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

import eu.cdevreeze.tqa.base.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Collection of dimensional trees. Only the dimensional trees themselves are represented, not inheritance
 * of dimensional trees.
 *
 * Instances of this class are expensive to create, and should be created only once per DTS and then
 * retained in memory. This class is designed for quick searches through dimensional trees. It is ideal
 * for dimensional XBRL instance validation.
 *
 * The taxonomy is still needed as additional context, for its schema content, especially for
 * typed dimensions.
 *
 * This class is most useful if the taxonomy from which it is instantiated is XBRL Core and Dimensions
 * valid.
 *
 * @author Chris de Vreeze
 */
final class DimensionalForest private (
    val hasHypercubesByElrAndPrimary: Map[String, Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]]],
    val hypercubeDimensionsByElrAndHypercube: Map[String, Map[EName, immutable.IndexedSeq[HypercubeDimensionRelationship]]],
    val dimensionDomainsByElrAndDimension: Map[String, Map[EName, immutable.IndexedSeq[DimensionalForest.DimensionDomain]]],
    val dimensionDefaultRelationships: immutable.IndexedSeq[DimensionDefaultRelationship]) {

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
}

object DimensionalForest {

  def apply(
    hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship],
    hypercubeDimensions: immutable.IndexedSeq[HypercubeDimensionRelationship],
    dimensionDomains: immutable.IndexedSeq[DimensionalForest.DimensionDomain],
    dimensionDefaults: immutable.IndexedSeq[DimensionDefaultRelationship]): DimensionalForest = {

    new DimensionalForest(
      hasHypercubes.groupBy(_.elr).mapValues(_.groupBy(_.primary)),
      hypercubeDimensions.groupBy(_.elr).mapValues(_.groupBy(_.hypercube)),
      dimensionDomains.groupBy(_.elr).mapValues(_.groupBy(_.dimension)),
      dimensionDefaults)
  }

  final class DimensionDomain(
      val dimension: EName,
      val elr: String,
      val domain: DimensionalForest.Member,
      val domainMembers: Map[EName, DimensionalForest.Member]) {

    require(domainMembers.forall(kv => kv._2.ename == kv._1), s"Corrupt dimension domain")
  }

  final case class Member(ename: EName, usable: Boolean)
}
