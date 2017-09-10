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

import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.yaidom.core.EName

/**
 * Filter.
 *
 * @author Chris de Vreeze
 */
sealed trait Filter

sealed trait ConceptFilter extends Filter

final case class ConceptNameFilter(
  conceptNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr]) extends ConceptFilter

final case class ConceptPeriodTypeFilter(periodType: String) extends ConceptFilter

final case class ConceptBalanceFilter(balance: String) extends ConceptFilter

final case class ConceptCustomAttributeFilter(
  attrNameOrExpr: ENameValueOrExpr,
  attrValueExprOption: Option[ScopedXPathString]) extends ConceptFilter

final case class ConceptDataTypeFilter(
  typeNameOrExpr: ENameValueOrExpr,
  strict: Boolean) extends ConceptFilter

final case class ConceptSubstitutionGroupFilter(
  substitutionGroupNameOrExpr: ENameValueOrExpr,
  strict: Boolean) extends ConceptFilter

sealed abstract class BooleanFilter(
  val complement: Boolean,
  val cover: Boolean,
  val subFilters: immutable.IndexedSeq[Filter]) extends Filter

final case class AndFilter(
  override val complement: Boolean,
  override val cover: Boolean,
  override val subFilters: immutable.IndexedSeq[Filter]) extends BooleanFilter(complement, cover, subFilters)

final case class OrFilter(
  override val complement: Boolean,
  override val cover: Boolean,
  override val subFilters: immutable.IndexedSeq[Filter]) extends BooleanFilter(complement, cover, subFilters)

sealed abstract class DimensionFilter(val dimensionNameOrExpr: ENameValueOrExpr) extends Filter

final case class ExplicitDimensionFilter(
  override val dimensionNameOrExpr: ENameValueOrExpr,
  members: immutable.IndexedSeq[DimensionFilterMember]) extends DimensionFilter(dimensionNameOrExpr)

final case class TypedDimensionFilter(
  override val dimensionNameOrExpr: ENameValueOrExpr,
  memberExprOption: Option[ScopedXPathString]) extends DimensionFilter(dimensionNameOrExpr)

sealed trait EntityFilter extends Filter

final case class IdentifierFilter(identifierValueExpr: ScopedXPathString) extends EntityFilter

final case class SpecificSchemeFilter(schemeExpr: ScopedXPathString) extends EntityFilter

final case class RegexpSchemeFilter(schemePattern: String) extends EntityFilter

final case class SpecificIdentifierFilter(
  schemeExpr: ScopedXPathString,
  identifierValueExpr: ScopedXPathString) extends EntityFilter

final case class RegexpIdentifierFilter(identifierValuePattern: String) extends EntityFilter

final case class GeneralFilter(exprOption: Option[ScopedXPathString]) extends Filter

sealed abstract class MatchFilter(val variable: EName, val matchAny: Boolean) extends Filter

final case class MatchConceptFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchLocationFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchUnitFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchEntityIdentifierFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchPeriodFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchSegmentFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchScenarioFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchNonXDTSegmentFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchNonXDTScenarioFilter(
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchDimensionFilter(
  dimension: EName,
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

sealed trait PeriodAspectFilter extends Filter

final case class PeriodFilter(expr: ScopedXPathString) extends PeriodAspectFilter

final case class PeriodStartFilter(
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case class PeriodEndFilter(
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case class PeriodInstantFilter(
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case object ForeverFilter extends PeriodAspectFilter

final case class InstantDurationFilter(
  variable: EName,
  boundary: String) extends PeriodAspectFilter

final case class RelativeFilter(variable: EName) extends Filter

sealed abstract class SegmentScenarioFilter(val exprOption: Option[ScopedXPathString]) extends Filter

final case class SegmentFilter(
  override val exprOption: Option[ScopedXPathString]) extends SegmentScenarioFilter(exprOption)

final case class ScenarioFilter(
  override val exprOption: Option[ScopedXPathString]) extends SegmentScenarioFilter(exprOption)

sealed trait TupleFilter extends Filter

final case class ParentFilter(parentNameOrExpr: ENameValueOrExpr) extends TupleFilter

final case class AncestorFilter(ancestorNameOrExpr: ENameValueOrExpr) extends TupleFilter

final case class SiblingFilter(variable: EName) extends TupleFilter

final case class LocationFilter(
  variable: EName,
  locationExpr: ScopedXPathString) extends TupleFilter

sealed trait UnitFilter extends Filter

final case class SingleMeasureFilter(measureNameOrExpr: ENameValueOrExpr) extends UnitFilter

final case class GeneralMeasuresFilter(expr: ScopedXPathString) extends UnitFilter

sealed trait ValueFilter extends Filter

final case object NilFilter extends ValueFilter

final case class PrecisionFilter(minimumExpr: ScopedXPathString) extends ValueFilter

final case class AspectCoverFilter(
  aspects: Set[AspectCoverFilter.Aspect],
  dimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr],
  excludeDimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr]) extends Filter

final case class ConceptRelationFilter(
  sourceNameOrExpr: ENameValueOrExpr,
  linkroleOrExpr: StringValueOrExpr,
  linknameOrExpr: StringValueOrExpr,
  arcroleOrExpr: StringValueOrExpr,
  arcnameOrExpr: StringValueOrExpr,
  axis: ConceptRelationFilter.Axis,
  generationsOption: Option[Int],
  exprOption: Option[ScopedXPathString]) extends Filter

// Companion objects

object AspectCoverFilter {

  sealed trait Aspect {

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

  object Aspect {

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
      case _                   => sys.error(s"Not a valid aspect: $s")
    }
  }
}

object ConceptRelationFilter {

  sealed trait Axis {

    final override def toString: String = this match {
      case ChildAxis               => "child"
      case ChildOrSelfAxis         => "child-or-self"
      case DescendantAxis          => "descendant"
      case DescendantOrSelfAxis    => "descendant-or-self"
      case ParentAxis              => "parent"
      case ParentOrSelfAxis        => "parent-or-self"
      case AncestorAxis            => "ancestor"
      case AncestorOrSelfAxis      => "ancestor-or-self"
      case SiblingAxis             => "sibling"
      case SiblingOrSelfAxis       => "sibling-or-self"
      case SiblingOrDescendantAxis => "sibling-or-descendant"
    }
  }

  case object ChildAxis extends Axis
  case object ChildOrSelfAxis extends Axis
  case object DescendantAxis extends Axis
  case object DescendantOrSelfAxis extends Axis
  case object ParentAxis extends Axis
  case object ParentOrSelfAxis extends Axis
  case object AncestorAxis extends Axis
  case object AncestorOrSelfAxis extends Axis
  case object SiblingAxis extends Axis
  case object SiblingOrSelfAxis extends Axis
  case object SiblingOrDescendantAxis extends Axis

  object Axis {

    def fromString(s: String): Axis = s match {
      case "child"                 => ChildAxis
      case "child-or-self"         => ChildOrSelfAxis
      case "descendant"            => DescendantAxis
      case "descendant-or-self"    => DescendantOrSelfAxis
      case "parent"                => ParentAxis
      case "parent-or-self"        => ParentOrSelfAxis
      case "ancestor"              => AncestorAxis
      case "ancestor-or-self"      => AncestorOrSelfAxis
      case "sibling"               => SiblingAxis
      case "sibling-or-self"       => SiblingOrSelfAxis
      case "sibling-or-descendant" => SiblingOrDescendantAxis
      case _                       => sys.error(s"Not a valid axis: $s")
    }
  }
}
