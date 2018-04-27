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

package eu.cdevreeze.tqa.extension.table.layoutmodel.common

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope

/**
 * Utility holding a type for aspects in a layout model.
 *
 * @author Chris de Vreeze
 */
object LayoutModelAspects {

  /**
   * Aspect in a layout model.
   */
  sealed trait Aspect {

    import Aspect._

    final override def toString: String = this match {
      case Concept => "concept"
      case EntityIdentifier => "entity-identifier"
      case Period => "period"
      case UnitAspect => "unit"
      case Segment => "segment"
      case Scenario => "scenario"
      case Dimension(dim) => dim.toString
    }

    final def toDisplayString(scope: Scope): String = this match {
      case Concept => "concept"
      case EntityIdentifier => "entity-identifier"
      case Period => "period"
      case UnitAspect => "unit"
      case Segment => "segment"
      case Scenario => "scenario"
      case Dimension(EName(None, nm)) => nm
      case Dimension(dim @ EName(Some(ns), nm)) =>
        val prefix = scope.prefixesForNamespace(ns).ensuring(_.nonEmpty).head
        val prefixOption = if (prefix.isEmpty) None else Some(prefix)
        dim.toQName(prefixOption).toString
    }
  }

  object Aspect {

    case object Concept extends Aspect
    case object EntityIdentifier extends Aspect
    case object Period extends Aspect
    case object UnitAspect extends Aspect

    /**
     * Non-XDT or complete segment depending on the aspect model
     */
    case object Segment extends Aspect

    /**
     * Non-XDT or complete scenario depending on the aspect model
     */
    case object Scenario extends Aspect

    final case class Dimension(dimension: EName) extends Aspect

    def fromString(s: String): Aspect = s match {
      case "concept" => Concept
      case "entity-identifier" => EntityIdentifier
      case "period" => Period
      case "unit" => UnitAspect
      case "segment" => Segment
      case "scenario" => Scenario
      case s => Dimension(EName.parse(s))
    }

    def fromDisplayString(s: String, scope: Scope): Aspect = s match {
      case "concept" => Concept
      case "entity-identifier" => EntityIdentifier
      case "period" => Period
      case "unit" => UnitAspect
      case "segment" => Segment
      case "scenario" => Scenario
      case s => Dimension(scope.resolveQName(QName.parse(s)))
    }
  }
}
