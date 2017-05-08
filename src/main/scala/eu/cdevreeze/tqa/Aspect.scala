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

package eu.cdevreeze.tqa

import eu.cdevreeze.yaidom.core.EName

/**
 * Aspect (of a fact in an XBRL instance).
 *
 * @author Chris de Vreeze
 */
sealed trait Aspect

object Aspect {

  case object ConceptAspect extends Aspect

  case object PeriodAspect extends Aspect

  case object EntityIdentifierAspect extends Aspect

  case object UnitAspect extends Aspect

  sealed trait OccAspect extends Aspect

  case object SegmentOccAspect extends OccAspect

  case object ScenarioOccAspect extends OccAspect

  final case class DimensionAspect(dimension: EName) extends Aspect

  val WellKnownAspects: Set[Aspect] =
    Set(ConceptAspect, PeriodAspect, EntityIdentifierAspect, UnitAspect, SegmentOccAspect, ScenarioOccAspect)
}
