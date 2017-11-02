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

package eu.cdevreeze.tqa.extension.formula.model

import scala.collection.immutable

import eu.cdevreeze.tqa.ENameValueOrExpr
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.aspect.Aspect
import eu.cdevreeze.tqa.aspect.AspectModel
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.extension.formula.common.Occ
import eu.cdevreeze.yaidom.core.EName

/**
 * Formula aspect rule.
 *
 * @author Chris de Vreeze
 */
sealed trait AspectRule {

  def sourceOption: Option[EName]

  def aspect(aspectModel: AspectModel): Aspect
}

final case class ConceptAspectRule(
    sourceOption: Option[EName],
    conceptNameOrExprOption: Option[ENameValueOrExpr]) extends AspectRule {

  def aspect(aspectModel: AspectModel): Aspect = Aspect.ConceptAspect
}

final case class EntityIdentifierAspectRule(
    sourceOption: Option[EName],
    schemeExprOption: Option[ScopedXPathString],
    identifierValueExprOption: Option[ScopedXPathString]) extends AspectRule {

  def aspect(aspectModel: AspectModel): Aspect = Aspect.EntityIdentifierAspect
}

final case class PeriodAspectRule(
    sourceOption: Option[EName],
    periods: immutable.IndexedSeq[PeriodAspectRule.Period]) extends AspectRule {

  def aspect(aspectModel: AspectModel): Aspect = Aspect.PeriodAspect
}

final case class UnitAspectRule(
    sourceOption: Option[EName],
    multiplyBy: immutable.IndexedSeq[MultiplyBy],
    divideBy: immutable.IndexedSeq[DivideBy],
    augmentOption: Option[Boolean]) extends AspectRule {

  def aspect(aspectModel: AspectModel): Aspect = Aspect.UnitAspect
}

sealed trait OccAspectRule extends AspectRule {

  def occ: Occ

  final def aspect(aspectModel: AspectModel): Aspect.OccAspect = (occ, aspectModel) match {
    case (Occ.Segment, AspectModel.DimensionalAspectModel)     => Aspect.NonXDTSegmentAspect
    case (Occ.Segment, AspectModel.NonDimensionalAspectModel)  => Aspect.CompleteSegmentAspect
    case (Occ.Scenario, AspectModel.DimensionalAspectModel)    => Aspect.NonXDTScenarioAspect
    case (Occ.Scenario, AspectModel.NonDimensionalAspectModel) => Aspect.CompleteScenarioAspect
  }
}

final case class OccEmptyAspectRule(
  sourceOption: Option[EName],
  occ: Occ) extends OccAspectRule

// TODO Fragments
final case class OccFragmentsAspectRule(
  sourceOption: Option[EName],
  occ: Occ) extends OccAspectRule

final case class OccXPathAspectRule(
  sourceOption: Option[EName],
  occ: Occ,
  selectExprOption: Option[ScopedXPathString]) extends OccAspectRule

sealed abstract class DimensionAspectRule(
    val sourceOption: Option[EName],
    val dimension: EName) extends AspectRule {

  final def aspect(aspectModel: AspectModel): Aspect = Aspect.DimensionAspect(dimension)
}

// TODO Optional omit "element"
final case class ExplicitDimensionAspectRule(
  override val sourceOption: Option[EName],
  override val dimension: EName,
  memberNameOrExprOption: Option[ENameValueOrExpr]) extends DimensionAspectRule(sourceOption, dimension)

// TODO Optional omit and value "elements"
final case class TypedDimensionAspectRule(
  override val sourceOption: Option[EName],
  override val dimension: EName,
  memberExprOption: Option[ScopedXPathString]) extends DimensionAspectRule(sourceOption, dimension)

object PeriodAspectRule {

  sealed trait Period {

    def periodType: PeriodType
  }

  case object ForeverPeriod extends Period {

    def periodType: PeriodType = PeriodType.Duration
  }

  final case class InstantPeriod(
      instantExprOption: Option[ScopedXPathString]) extends Period {

    def periodType: PeriodType = PeriodType.Instant
  }

  final case class DurationPeriod(
      startExprOption: Option[ScopedXPathString],
      endExprOption: Option[ScopedXPathString]) extends Period {

    def periodType: PeriodType = PeriodType.Duration
  }
}
