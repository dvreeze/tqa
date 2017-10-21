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

import eu.cdevreeze.yaidom.core.EName

/**
 * Dimensional context. This type is optimized for dimensional instance validation.
 *
 * @author Chris de Vreeze
 */
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
}
