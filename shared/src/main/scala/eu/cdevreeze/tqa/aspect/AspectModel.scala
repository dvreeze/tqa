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

package eu.cdevreeze.tqa.aspect

/**
 * Aspect model.
 *
 * @author Chris de Vreeze
 */
sealed trait AspectModel {

  /**
   * Returns all known aspects. That is, returns all aspects, except for dimension aspects (in the
   * dimensional aspect model), because they are unknown upfront.
   */
  def wellKnownAspects: Set[Aspect]

  /**
   * Returns all known item aspects shared by numeric and non-numeric items.
   */
  def requiredItemAspects: Set[Aspect]

  /**
   * Returns all known aspects of numeric items.
   */
  def requiredNumericItemAspects: Set[Aspect]
}

object AspectModel {

  import Aspect._

  case object NonDimensionalAspectModel extends AspectModel {

    val wellKnownAspects: Set[Aspect] =
      Set(LocationAspect, ConceptAspect, PeriodAspect, EntityIdentifierAspect, UnitAspect, CompleteSegmentAspect, CompleteScenarioAspect)

    val requiredItemAspects: Set[Aspect] =
      Set(LocationAspect, ConceptAspect, PeriodAspect, EntityIdentifierAspect)

    val requiredNumericItemAspects: Set[Aspect] =
      requiredItemAspects.union(Set(UnitAspect))
  }

  case object DimensionalAspectModel extends AspectModel {

    val wellKnownAspects: Set[Aspect] =
      Set(LocationAspect, ConceptAspect, PeriodAspect, EntityIdentifierAspect, UnitAspect, NonXDTSegmentAspect, NonXDTScenarioAspect)

    val requiredItemAspects: Set[Aspect] =
      Set(LocationAspect, ConceptAspect, PeriodAspect, EntityIdentifierAspect)

    val requiredNumericItemAspects: Set[Aspect] =
      requiredItemAspects.union(Set(UnitAspect))
  }
}
