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

package eu.cdevreeze.tqa.extension.formula.taxonomymodel

import scala.collection.immutable
import scala.reflect.classTag

import org.scalactic.Bad
import org.scalactic.Good
import org.scalactic.One
import org.scalactic.Or

import eu.cdevreeze.tqa.ENameExpr
import eu.cdevreeze.tqa.ENameValue
import eu.cdevreeze.tqa.StringExpr
import eu.cdevreeze.tqa.StringValue
import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.relationship.BooleanFilterRelationship
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy
import eu.cdevreeze.tqa.extension.formula.common.AspectCoverFilters

/**
 * Converter from formula taxonomy filters to filters in the model layer.
 *
 * @author Chris de Vreeze
 */
final class FilterConverter(val formulaTaxonomy: BasicFormulaTaxonomy) {

  def convertFilter(domFilter: dom.Filter): model.Filter Or One[ConversionError] = domFilter match {
    case f: dom.ConceptFilter         => convertConceptFilter(f)
    case f: dom.BooleanFilter         => convertBooleanFilter(f)
    case f: dom.DimensionFilter       => convertDimensionFilter(f)
    case f: dom.EntityFilter          => convertEntityFilter(f)
    case f: dom.GeneralFilter         => convertGeneralFilter(f)
    case f: dom.MatchFilter           => convertMatchFilter(f)
    case f: dom.PeriodAspectFilter    => convertPeriodAspectFilter(f)
    case f: dom.RelativeFilter        => convertRelativeFilter(f)
    case f: dom.SegmentScenarioFilter => convertSegmentScenarioFilter(f)
    case f: dom.TupleFilter           => convertTupleFilter(f)
    case f: dom.UnitFilter            => convertUnitFilter(f)
    case f: dom.ValueFilter           => convertValueFilter(f)
    case f: dom.AspectCoverFilter     => convertAspectCoverFilter(f)
    case f: dom.ConceptRelationFilter => convertConceptRelationFilter(f)
  }

  def convertConceptFilter(domFilter: dom.ConceptFilter): model.ConceptFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.ConceptNameFilter =>
          val conceptNamesOrExprs = f.concepts flatMap { cfConcept =>
            cfConcept.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              cfConcept.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v)))
          }

          Good(model.ConceptNameFilter(conceptNamesOrExprs))
        case f: dom.ConceptPeriodTypeFilter =>
          Good(model.ConceptPeriodTypeFilter(f.periodType))
        case f: dom.ConceptBalanceFilter =>
          Good(model.ConceptBalanceFilter(f.balance))
        case f: dom.ConceptCustomAttributeFilter =>
          Good(model.ConceptCustomAttributeFilter(
            f.customAttribute.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.customAttribute.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
            f.valueExprOption))
        case f: dom.ConceptDataTypeFilter =>
          Good(model.ConceptDataTypeFilter(
            f.conceptDataType.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.conceptDataType.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
            f.strict))
        case f: dom.ConceptSubstitutionGroupFilter =>
          Good(model.ConceptSubstitutionGroupFilter(
            f.conceptSubstitutionGroup.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.conceptSubstitutionGroup.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
            f.strict))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert concept filter ${domFilter.key}")))
    }
  }

  def convertBooleanFilter(domFilter: dom.BooleanFilter): model.BooleanFilter Or One[ConversionError] = {
    try {
      val booleanFilterRelationships =
        formulaTaxonomy.findAllOutgoingFormulaRelationshipsOfType(domFilter, classTag[BooleanFilterRelationship]).sortBy(_.order)

      // Recursive calls to convertFilter
      val subFilters =
        booleanFilterRelationships flatMap { rel =>
          val subFilterOption = convertFilter(rel.subFilter).toOption

          subFilterOption.map(subFilter => model.SubFilter(rel.complement, rel.cover, subFilter))
        }

      domFilter match {
        case f: dom.AndFilter =>
          Good(model.AndFilter(subFilters))
        case f: dom.OrFilter =>
          Good(model.OrFilter(subFilters))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert boolean filter ${domFilter.key}")))
    }
  }

  def convertDimensionFilter(domFilter: dom.DimensionFilter): model.DimensionFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.ExplicitDimensionFilter =>
          val dimMembers: immutable.IndexedSeq[model.DimensionFilterMember] = f.members map { mem =>
            model.DimensionFilterMember(
              mem.variableElemOption.map(_.name).map(v => ENameValue(v)).orElse(
                mem.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v))).orElse(
                  mem.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
              mem.linkroleElemOption.map(_.underlyingElem.text),
              mem.arcroleElemOption.map(_.underlyingElem.text),
              mem.axisElemOption.map(_.underlyingElem.text))
          }

          Good(model.ExplicitDimensionFilter(
            f.dimension.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.dimension.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
            dimMembers))
        case f: dom.TypedDimensionFilter =>
          Good(model.TypedDimensionFilter(
            f.dimension.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.dimension.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get,
            f.testExprOption))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert dimension filter ${domFilter.key}")))
    }
  }

  def convertEntityFilter(domFilter: dom.EntityFilter): model.EntityFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.IdentifierFilter =>
          Good(model.IdentifierFilter(f.testExpr))
        case f: dom.SpecificSchemeFilter =>
          Good(model.SpecificSchemeFilter(f.schemeExpr))
        case f: dom.RegexpSchemeFilter =>
          Good(model.RegexpSchemeFilter(f.pattern))
        case f: dom.SpecificIdentifierFilter =>
          Good(model.SpecificIdentifierFilter(
            f.schemeExpr,
            f.valueExpr))
        case f: dom.RegexpIdentifierFilter =>
          Good(model.RegexpIdentifierFilter(f.pattern))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert entity filter ${domFilter.key}")))
    }
  }

  def convertGeneralFilter(domFilter: dom.GeneralFilter): model.GeneralFilter Or One[ConversionError] = {
    try {
      Good(model.GeneralFilter(domFilter.testExprOption))
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert general filter ${domFilter.key}")))
    }
  }

  def convertMatchFilter(domFilter: dom.MatchFilter): model.MatchFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.MatchConceptFilter =>
          Good(model.MatchConceptFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchLocationFilter =>
          Good(model.MatchLocationFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchUnitFilter =>
          Good(model.MatchUnitFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchEntityIdentifierFilter =>
          Good(model.MatchEntityIdentifierFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchPeriodFilter =>
          Good(model.MatchPeriodFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchSegmentFilter =>
          Good(model.MatchSegmentFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchScenarioFilter =>
          Good(model.MatchScenarioFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchNonXDTSegmentFilter =>
          Good(model.MatchNonXDTSegmentFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchNonXDTScenarioFilter =>
          Good(model.MatchNonXDTScenarioFilter(
            f.variable,
            f.matchAny))
        case f: dom.MatchDimensionFilter =>
          Good(model.MatchDimensionFilter(
            f.dimension,
            f.variable,
            f.matchAny))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert match filter ${domFilter.key}")))
    }
  }

  def convertPeriodAspectFilter(domFilter: dom.PeriodAspectFilter): model.PeriodAspectFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.PeriodFilter =>
          Good(model.PeriodFilter(f.testExpr))
        case f: dom.PeriodStartFilter =>
          Good(model.PeriodStartFilter(
            f.dateExpr,
            f.timeExprOption))
        case f: dom.PeriodEndFilter =>
          Good(model.PeriodEndFilter(
            f.dateExpr,
            f.timeExprOption))
        case f: dom.PeriodInstantFilter =>
          Good(model.PeriodInstantFilter(
            f.dateExpr,
            f.timeExprOption))
        case f: dom.ForeverFilter =>
          Good(model.ForeverFilter)
        case f: dom.InstantDurationFilter =>
          Good(model.InstantDurationFilter(
            f.variable,
            f.boundary))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert period aspect filter ${domFilter.key}")))
    }
  }

  def convertRelativeFilter(domFilter: dom.RelativeFilter): model.RelativeFilter Or One[ConversionError] = {
    try {
      Good(model.RelativeFilter(domFilter.variable))
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert relative filter ${domFilter.key}")))
    }
  }

  def convertSegmentScenarioFilter(domFilter: dom.SegmentScenarioFilter): model.SegmentScenarioFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.SegmentFilter =>
          Good(model.SegmentFilter(f.testExprOption))
        case f: dom.ScenarioFilter =>
          Good(model.ScenarioFilter(f.testExprOption))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert segment-scenario filter ${domFilter.key}")))
    }
  }

  def convertTupleFilter(domFilter: dom.TupleFilter): model.TupleFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.ParentFilter =>
          Good(model.ParentFilter(
            f.parent.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.parent.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get))
        case f: dom.AncestorFilter =>
          Good(model.AncestorFilter(
            f.ancestor.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.ancestor.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get))
        case f: dom.SiblingFilter =>
          Good(model.SiblingFilter(f.variable))
        case f: dom.LocationFilter =>
          Good(model.LocationFilter(
            f.variable,
            f.locationExpr))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert tuple filter ${domFilter.key}")))
    }
  }

  def convertUnitFilter(domFilter: dom.UnitFilter): model.UnitFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.SingleMeasureFilter =>
          Good(model.SingleMeasureFilter(
            f.measure.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
              f.measure.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get))
        case f: dom.GeneralMeasuresFilter =>
          Good(model.GeneralMeasuresFilter(f.testExpr))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert unit filter ${domFilter.key}")))
    }
  }

  def convertValueFilter(domFilter: dom.ValueFilter): model.ValueFilter Or One[ConversionError] = {
    try {
      domFilter match {
        case f: dom.NilFilter =>
          Good(model.NilFilter)
        case f: dom.PrecisionFilter =>
          Good(model.PrecisionFilter(f.minimumExpr))
      }
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert value filter ${domFilter.key}")))
    }
  }

  def convertAspectCoverFilter(domFilter: dom.AspectCoverFilter): model.AspectCoverFilter Or One[ConversionError] = {
    try {
      val aspects: Set[AspectCoverFilters.Aspect] = domFilter.aspects.map(_.aspectValue).toSet

      val dimensions = domFilter.dimensions flatMap { dim =>
        dim.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
          dim.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v)))
      }

      val excludeDimensions = domFilter.excludeDimensions flatMap { dim =>
        dim.qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
          dim.qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v)))
      }

      Good(model.AspectCoverFilter(
        aspects,
        dimensions,
        excludeDimensions))
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert aspect cover filter ${domFilter.key}")))
    }
  }

  def convertConceptRelationFilter(domFilter: dom.ConceptRelationFilter): model.ConceptRelationFilter Or One[ConversionError] = {
    try {
      val sourceNameOrExpr =
        domFilter.variableOption.map(_.name).map(v => ENameValue(v)).orElse(
          domFilter.qnameOption.map(_.qnameValue).map(v => ENameValue(v))).orElse(
            domFilter.qnameExpressionOption.map(_.expr).map(v => ENameExpr(v))).get

      val linkroleOrExpr =
        domFilter.linkroleOption.map(_.linkrole).map(v => StringValue(v)).orElse(
          domFilter.linkroleExpressionOption.map(_.expr).map(v => StringExpr(v))).get

      val linknameExpr =
        domFilter.linknameOption.map(_.linknameValue).map(v => ENameValue(v)).orElse(
          domFilter.linknameExpressionOption.map(_.expr).map(v => ENameExpr(v))).get

      val arcroleExpr =
        domFilter.arcroleOption.map(_.arcrole).map(v => StringValue(v)).orElse(
          domFilter.arcroleExpressionOption.map(_.expr).map(v => StringExpr(v))).get

      val arcnameExpr =
        domFilter.arcnameOption.map(_.arcnameValue).map(v => ENameValue(v)).orElse(
          domFilter.arcnameExpressionOption.map(_.expr).map(v => ENameExpr(v))).get

      Good(model.ConceptRelationFilter(
        sourceNameOrExpr,
        linkroleOrExpr,
        linknameExpr,
        arcroleExpr,
        arcnameExpr,
        domFilter.axis.axisValue,
        domFilter.generationsOption.map(_.underlyingElem.text.toInt),
        domFilter.testExprOption))
    } catch {
      case exc: Exception => Bad(One(FilterConversionError(s"Could not convert concept relation filter ${domFilter.key}")))
    }
  }
}
