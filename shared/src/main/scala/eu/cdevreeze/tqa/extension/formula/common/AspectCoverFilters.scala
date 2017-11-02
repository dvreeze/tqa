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

package eu.cdevreeze.tqa.extension.formula.common

/**
 * Utility holding a type for aspects in an aspect cover filter, which allows values like "all" and "dimensions".
 *
 * @author Chris de Vreeze
 */
object AspectCoverFilters {

  /**
   * Aspect in an aspect cover filter, which allows values like "all" and "dimensions".
   */
  sealed trait Aspect {

    import Aspect._

    final override def toString: String = this match {
      case All              => "all"
      case Concept          => "concept"
      case EntityIdentifier => "entity-identifier"
      case Location         => "location"
      case Period           => "period"
      case UnitAspect       => "unit"
      case CompleteSegment  => "complete-segment"
      case CompleteScenario => "complete-scenario"
      case NonXDTSegment    => "non-XDT-segment"
      case NonXDTScenario   => "non-XDT-scenario"
      case Dimensions       => "dimensions"
    }
  }

  object Aspect {

    case object All extends Aspect
    case object Concept extends Aspect
    case object EntityIdentifier extends Aspect
    case object Location extends Aspect
    case object Period extends Aspect
    case object UnitAspect extends Aspect
    case object CompleteSegment extends Aspect
    case object CompleteScenario extends Aspect
    case object NonXDTSegment extends Aspect
    case object NonXDTScenario extends Aspect
    case object Dimensions extends Aspect

    def fromString(s: String): Aspect = s match {
      case "all"               => All
      case "concept"           => Concept
      case "entity-identifier" => EntityIdentifier
      case "location"          => Location
      case "period"            => Period
      case "unit"              => UnitAspect
      case "complete-segment"  => CompleteSegment
      case "complete-scenario" => CompleteScenario
      case "non-XDT-segment"   => NonXDTSegment
      case "non-XDT-scenario"  => NonXDTScenario
      case "dimensions"        => Dimensions
      case _                   => sys.error(s"Not a valid aspect (in an aspect filter): $s")
    }
  }
}
