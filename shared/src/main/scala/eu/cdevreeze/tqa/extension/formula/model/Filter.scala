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
sealed trait Filter extends Resource

sealed trait ConceptFilter extends Filter

final case class ConceptNameFilter(
  idOption: Option[String],
  conceptNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr]) extends ConceptFilter

final case class ConceptPeriodTypeFilter(idOption: Option[String], periodType: String) extends ConceptFilter

final case class ConceptBalanceFilter(idOption: Option[String], balance: String) extends ConceptFilter

final case class ConceptCustomAttributeFilter(
  idOption: Option[String],
  attrNameOrExpr: ENameValueOrExpr,
  attrValueExprOption: Option[ScopedXPathString]) extends ConceptFilter

final case class ConceptDataTypeFilter(
  idOption: Option[String],
  typeNameOrExpr: ENameValueOrExpr,
  strict: Boolean) extends ConceptFilter

final case class ConceptSubstitutionGroupFilter(
  idOption: Option[String],
  substitutionGroupNameOrExpr: ENameValueOrExpr,
  strict: Boolean) extends ConceptFilter

sealed abstract class BooleanFilter(
  val subFilters: immutable.IndexedSeq[BooleanFilterSubFilter]) extends Filter

final case class AndFilter(
  idOption: Option[String],
  override val subFilters: immutable.IndexedSeq[BooleanFilterSubFilter]) extends BooleanFilter(subFilters)

final case class OrFilter(
  idOption: Option[String],
  override val subFilters: immutable.IndexedSeq[BooleanFilterSubFilter]) extends BooleanFilter(subFilters)

sealed abstract class DimensionFilter(val dimensionNameOrExpr: ENameValueOrExpr) extends Filter

final case class ExplicitDimensionFilter(
  idOption: Option[String],
  override val dimensionNameOrExpr: ENameValueOrExpr,
  members: immutable.IndexedSeq[DimensionFilterMember]) extends DimensionFilter(dimensionNameOrExpr)

final case class TypedDimensionFilter(
  idOption: Option[String],
  override val dimensionNameOrExpr: ENameValueOrExpr,
  memberExprOption: Option[ScopedXPathString]) extends DimensionFilter(dimensionNameOrExpr)

sealed trait EntityFilter extends Filter

final case class IdentifierFilter(idOption: Option[String], identifierValueExpr: ScopedXPathString) extends EntityFilter

final case class SpecificSchemeFilter(idOption: Option[String], schemeExpr: ScopedXPathString) extends EntityFilter

final case class RegexpSchemeFilter(idOption: Option[String], schemePattern: String) extends EntityFilter

final case class SpecificIdentifierFilter(
  idOption: Option[String],
  schemeExpr: ScopedXPathString,
  identifierValueExpr: ScopedXPathString) extends EntityFilter

final case class RegexpIdentifierFilter(idOption: Option[String], identifierValuePattern: String) extends EntityFilter

final case class GeneralFilter(idOption: Option[String], exprOption: Option[ScopedXPathString]) extends Filter

sealed abstract class MatchFilter(val variable: EName, val matchAny: Boolean) extends Filter

final case class MatchConceptFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchLocationFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchUnitFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchEntityIdentifierFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchPeriodFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchSegmentFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchScenarioFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchNonXDTSegmentFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchNonXDTScenarioFilter(
  idOption: Option[String],
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

final case class MatchDimensionFilter(
  idOption: Option[String],
  dimension: EName,
  override val variable: EName,
  override val matchAny: Boolean) extends MatchFilter(variable, matchAny)

sealed trait PeriodAspectFilter extends Filter

final case class PeriodFilter(idOption: Option[String], expr: ScopedXPathString) extends PeriodAspectFilter

final case class PeriodStartFilter(
  idOption: Option[String],
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case class PeriodEndFilter(
  idOption: Option[String],
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case class PeriodInstantFilter(
  idOption: Option[String],
  dateExpr: ScopedXPathString,
  timeExprOption: Option[ScopedXPathString]) extends PeriodAspectFilter

final case class ForeverFilter(idOption: Option[String]) extends PeriodAspectFilter

final case class InstantDurationFilter(
  idOption: Option[String],
  variable: EName,
  boundary: String) extends PeriodAspectFilter

final case class RelativeFilter(idOption: Option[String], variable: EName) extends Filter

sealed abstract class SegmentScenarioFilter(val exprOption: Option[ScopedXPathString]) extends Filter

final case class SegmentFilter(
  idOption: Option[String],
  override val exprOption: Option[ScopedXPathString]) extends SegmentScenarioFilter(exprOption)

final case class ScenarioFilter(
  idOption: Option[String],
  override val exprOption: Option[ScopedXPathString]) extends SegmentScenarioFilter(exprOption)

sealed trait TupleFilter extends Filter

final case class ParentFilter(idOption: Option[String], parentNameOrExpr: ENameValueOrExpr) extends TupleFilter

final case class AncestorFilter(idOption: Option[String], ancestorNameOrExpr: ENameValueOrExpr) extends TupleFilter

final case class SiblingFilter(idOption: Option[String], variable: EName) extends TupleFilter

final case class LocationFilter(
  idOption: Option[String],
  variable: EName,
  locationExpr: ScopedXPathString) extends TupleFilter

sealed trait UnitFilter extends Filter

final case class SingleMeasureFilter(idOption: Option[String], measureNameOrExpr: ENameValueOrExpr) extends UnitFilter

final case class GeneralMeasuresFilter(idOption: Option[String], expr: ScopedXPathString) extends UnitFilter

sealed trait ValueFilter extends Filter

final case class NilFilter(idOption: Option[String]) extends ValueFilter

final case class PrecisionFilter(idOption: Option[String], minimumExpr: ScopedXPathString) extends ValueFilter

final case class AspectCoverFilter(
  idOption: Option[String],
  aspects: Set[AspectCoverFilters.Aspect],
  dimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr],
  excludeDimensionNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr]) extends Filter

final case class ConceptRelationFilter(
  idOption: Option[String],
  sourceNameOrExpr: ENameValueOrExpr,
  linkroleOrExpr: StringValueOrExpr,
  linknameOrExprOption: Option[ENameValueOrExpr],
  arcroleOrExpr: StringValueOrExpr,
  arcnameOrExprOption: Option[ENameValueOrExpr],
  axis: ConceptRelationFilters.Axis,
  generationsOption: Option[Int],
  exprOption: Option[ScopedXPathString]) extends Filter
