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

import eu.cdevreeze.yaidom.core.EName

/**
 * Dimensional context element, so either a dimensional segment or dimensional scenario. This type and
 * its sub-types are optimized for dimensional instance validation.
 *
 * @author Chris de Vreeze
 */
sealed abstract class DimensionalContextElement(
    val explicitDimensionMemberSeq: immutable.IndexedSeq[(EName, EName)],
    val typedDimensionMemberSeq: immutable.IndexedSeq[(EName, TypedDimensionMember)]) {

  final def explicitDimensionMembers: Map[EName, EName] = {
    explicitDimensionMemberSeq.toMap
  }

  final def typedDimensionMembers: Map[EName, TypedDimensionMember] = {
    typedDimensionMemberSeq.toMap
  }

  final def typedDimensions: Set[EName] = {
    typedDimensionMembers.keySet
  }

  def filterDimensions(dimensions: Set[EName]): DimensionalContextElement

  final def dimensions: Set[EName] = {
    explicitDimensionMembers.keySet.union(typedDimensions)
  }

  final def hasRepeatedDimensions: Boolean = {
    val dimensionSeq: immutable.IndexedSeq[EName] =
      explicitDimensionMemberSeq.map(_._1) ++ typedDimensionMemberSeq.map(_._1)

    dimensionSeq.distinct.size < dimensionSeq.size
  }
}

final case class DimensionalSegment(
    override val explicitDimensionMemberSeq: immutable.IndexedSeq[(EName, EName)],
    override val typedDimensionMemberSeq: immutable.IndexedSeq[(EName, TypedDimensionMember)]) extends DimensionalContextElement(explicitDimensionMemberSeq, typedDimensionMemberSeq) {

  final def filterDimensions(dimensions: Set[EName]): DimensionalSegment = {
    DimensionalSegment(
      explicitDimensionMemberSeq.filter(dimMem => dimensions.contains(dimMem._1)),
      typedDimensionMemberSeq.filter(dimMem => dimensions.contains(dimMem._1)))
  }
}

final case class DimensionalScenario(
    override val explicitDimensionMemberSeq: immutable.IndexedSeq[(EName, EName)],
    override val typedDimensionMemberSeq: immutable.IndexedSeq[(EName, TypedDimensionMember)]) extends DimensionalContextElement(explicitDimensionMemberSeq, typedDimensionMemberSeq) {

  final def filterDimensions(dimensions: Set[EName]): DimensionalScenario = {
    DimensionalScenario(
      explicitDimensionMemberSeq.filter(dimMem => dimensions.contains(dimMem._1)),
      typedDimensionMemberSeq.filter(dimMem => dimensions.contains(dimMem._1)))
  }
}
