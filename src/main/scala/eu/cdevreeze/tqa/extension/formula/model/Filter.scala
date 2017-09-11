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
import eu.cdevreeze.tqa.StringValueOrExpr
import eu.cdevreeze.tqa.extension.formula.common.AspectCoverFilters
import eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters
import eu.cdevreeze.yaidom.core.EName

/**
 * Filter. The filter does not know its ELR.
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
  val subFilters: immutable.IndexedSeq[SubFilter]) extends Filter

final case class AndFilter(
  override val subFilters: immutable.IndexedSeq[SubFilter]) extends BooleanFilter(subFilters)

final case class OrFilter(
  override val subFilters: immutable.IndexedSeq[SubFilter]) extends BooleanFilter(subFilters)

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
  aspects: Set[AspectCoverFilters.Aspect],
  dimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr],
  excludeDimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr]) extends Filter

final case class ConceptRelationFilter(
  sourceNameOrExpr: ENameValueOrExpr,
  linkroleOrExpr: StringValueOrExpr,
  linknameOrExprOption: Option[ENameValueOrExpr],
  arcroleOrExpr: StringValueOrExpr,
  arcnameOrExprOption: Option[ENameValueOrExpr],
  axis: ConceptRelationFilters.Axis,
  generationsOption: Option[Int],
  exprOption: Option[ScopedXPathString]) extends Filter
