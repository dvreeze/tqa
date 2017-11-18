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

import eu.cdevreeze.yaidom.core.EName

/**
 * Aspect (of a fact in an XBRL instance).
 *
 * See http://www.xbrl.org/specification/variables/REC-2009-06-22/variables-REC-2009-06-22.html#sec-aspects.
 *
 * @author Chris de Vreeze
 */
sealed trait Aspect

object Aspect {

  // First the required aspects, for both aspect models (dimensional and non-dimensional)

  case object LocationAspect extends Aspect {

    override def toString: String = "location"
  }

  case object ConceptAspect extends Aspect {

    override def toString: String = "concept"
  }

  case object EntityIdentifierAspect extends Aspect {

    override def toString: String = "entityIdentifier"
  }

  case object PeriodAspect extends Aspect {

    override def toString: String = "period"
  }

  case object UnitAspect extends Aspect {

    override def toString: String = "unit"
  }

  // Next the aspects that belong to one of the aspect models

  /**
   * Open context component aspect, so a segment or a scenario.
   */
  sealed trait OccAspect extends Aspect

  /**
   * NonXDTSegmentAspect, in the dimensional aspect model
   */
  case object NonXDTSegmentAspect extends OccAspect {

    override def toString: String = "nonXDTSegment"
  }

  /**
   * CompleteSegmentAspect, in the non-dimensional aspect model
   */
  case object CompleteSegmentAspect extends OccAspect {

    override def toString: String = "completeSegment"
  }

  /**
   * NonXDTScenarioAspect, in the dimensional aspect model
   */
  case object NonXDTScenarioAspect extends OccAspect {

    override def toString: String = "nonXDTScenario"
  }

  /**
   * CompleteScenarioAspect, in the non-dimensional aspect model
   */
  case object CompleteScenarioAspect extends OccAspect {

    override def toString: String = "completeScenario"
  }

  /**
   * A dimensional aspect (for either an explicit or typed dimension), in the dimensional aspect model
   */
  final case class DimensionAspect(dimension: EName) extends Aspect {

    override def toString: String = dimension.toString
  }
}
