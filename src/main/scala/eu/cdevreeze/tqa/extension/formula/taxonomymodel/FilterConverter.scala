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
import scala.util.Try

import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.relationship.BooleanFilterRelationship
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy

/**
 * Converter from formula DOM filters to filters in the model layer.
 *
 * @author Chris de Vreeze
 */
final class FilterConverter(val formulaTaxonomy: BasicFormulaTaxonomy) {

  def tryToConvertFilter(domFilter: dom.Filter): Try[model.Filter] = domFilter match {
    case f: dom.ConceptFilter         => tryToConvertConceptFilter(f)
    case f: dom.BooleanFilter         => tryToConvertBooleanFilter(f)
    case f: dom.DimensionFilter       => tryToConvertDimensionFilter(f)
    case f: dom.EntityFilter          => tryToConvertEntityFilter(f)
    case f: dom.GeneralFilter         => tryToConvertGeneralFilter(f)
    case f: dom.MatchFilter           => tryToConvertMatchFilter(f)
    case f: dom.PeriodAspectFilter    => tryToConvertPeriodAspectFilter(f)
    case f: dom.RelativeFilter        => tryToConvertRelativeFilter(f)
    case f: dom.SegmentScenarioFilter => tryToConvertSegmentScenarioFilter(f)
    case f: dom.TupleFilter           => tryToConvertTupleFilter(f)
    case f: dom.UnitFilter            => tryToConvertUnitFilter(f)
    case f: dom.ValueFilter           => tryToConvertValueFilter(f)
    case f: dom.AspectCoverFilter     => tryToConvertAspectCoverFilter(f)
    case f: dom.ConceptRelationFilter => tryToConvertConceptRelationFilter(f)
  }

  def tryToConvertConceptFilter(domFilter: dom.ConceptFilter): Try[model.ConceptFilter] = {
    Try {
      domFilter match {
        case f: dom.ConceptNameFilter =>
          model.ConceptNameFilter(f.concepts.map(_.qnameValueOrExpr))
        case f: dom.ConceptPeriodTypeFilter =>
          model.ConceptPeriodTypeFilter(f.periodType)
        case f: dom.ConceptBalanceFilter =>
          model.ConceptBalanceFilter(f.balance)
        case f: dom.ConceptCustomAttributeFilter =>
          model.ConceptCustomAttributeFilter(f.customAttribute.qnameValueOrExpr, f.valueExprOption)
        case f: dom.ConceptDataTypeFilter =>
          model.ConceptDataTypeFilter(f.conceptDataType.qnameValueOrExpr, f.strict)
        case f: dom.ConceptSubstitutionGroupFilter =>
          model.ConceptSubstitutionGroupFilter(f.conceptSubstitutionGroup.qnameValueOrExpr, f.strict)
      }
    }
  }

  def tryToConvertBooleanFilter(domFilter: dom.BooleanFilter): Try[model.BooleanFilter] = {
    Try {
      val booleanFilterRelationships =
        formulaTaxonomy.findAllOutgoingFormulaRelationshipsOfType(domFilter, classTag[BooleanFilterRelationship]).sortBy(_.order)

      // Recursive calls to convertFilter
      val subFilters =
        booleanFilterRelationships map { rel =>
          // Throwing an exception if not successful, and that is ok here.
          val subFilter = tryToConvertFilter(rel.subFilter).get

          model.BooleanFilterSubFilter(
            model.CommonRelationshipAttributes(
              rel.elr,
              rel.order,
              rel.priority,
              rel.use),
            rel.complement,
            rel.cover,
            subFilter)
        }

      domFilter match {
        case f: dom.AndFilter =>
          model.AndFilter(subFilters)
        case f: dom.OrFilter =>
          model.OrFilter(subFilters)
      }
    }
  }

  def tryToConvertDimensionFilter(domFilter: dom.DimensionFilter): Try[model.DimensionFilter] = {
    Try {
      domFilter match {
        case f: dom.ExplicitDimensionFilter =>
          val dimMembers: immutable.IndexedSeq[model.DimensionFilterMember] = f.members map { mem =>
            model.DimensionFilterMember(
              mem.qnameValueOrExpr,
              mem.linkroleElemOption.map(_.linkrole),
              mem.arcroleElemOption.map(_.arcrole),
              mem.axisElemOption.map(_.axis))
          }

          model.ExplicitDimensionFilter(f.dimension.qnameValueOrExpr, dimMembers)
        case f: dom.TypedDimensionFilter =>
          model.TypedDimensionFilter(f.dimension.qnameValueOrExpr, f.testExprOption)
      }
    }
  }

  def tryToConvertEntityFilter(domFilter: dom.EntityFilter): Try[model.EntityFilter] = {
    Try {
      domFilter match {
        case f: dom.IdentifierFilter =>
          model.IdentifierFilter(f.testExpr)
        case f: dom.SpecificSchemeFilter =>
          model.SpecificSchemeFilter(f.schemeExpr)
        case f: dom.RegexpSchemeFilter =>
          model.RegexpSchemeFilter(f.pattern)
        case f: dom.SpecificIdentifierFilter =>
          model.SpecificIdentifierFilter(f.schemeExpr, f.valueExpr)
        case f: dom.RegexpIdentifierFilter =>
          model.RegexpIdentifierFilter(f.pattern)
      }
    }
  }

  def tryToConvertGeneralFilter(domFilter: dom.GeneralFilter): Try[model.GeneralFilter] = {
    Try {
      model.GeneralFilter(domFilter.testExprOption)
    }
  }

  def tryToConvertMatchFilter(domFilter: dom.MatchFilter): Try[model.MatchFilter] = {
    Try {
      domFilter match {
        case f: dom.MatchConceptFilter =>
          model.MatchConceptFilter(f.variable, f.matchAny)
        case f: dom.MatchLocationFilter =>
          model.MatchLocationFilter(f.variable, f.matchAny)
        case f: dom.MatchUnitFilter =>
          model.MatchUnitFilter(f.variable, f.matchAny)
        case f: dom.MatchEntityIdentifierFilter =>
          model.MatchEntityIdentifierFilter(f.variable, f.matchAny)
        case f: dom.MatchPeriodFilter =>
          model.MatchPeriodFilter(f.variable, f.matchAny)
        case f: dom.MatchSegmentFilter =>
          model.MatchSegmentFilter(f.variable, f.matchAny)
        case f: dom.MatchScenarioFilter =>
          model.MatchScenarioFilter(f.variable, f.matchAny)
        case f: dom.MatchNonXDTSegmentFilter =>
          model.MatchNonXDTSegmentFilter(f.variable, f.matchAny)
        case f: dom.MatchNonXDTScenarioFilter =>
          model.MatchNonXDTScenarioFilter(f.variable, f.matchAny)
        case f: dom.MatchDimensionFilter =>
          model.MatchDimensionFilter(f.dimension, f.variable, f.matchAny)
      }
    }
  }

  def tryToConvertPeriodAspectFilter(domFilter: dom.PeriodAspectFilter): Try[model.PeriodAspectFilter] = {
    Try {
      domFilter match {
        case f: dom.PeriodFilter =>
          model.PeriodFilter(f.testExpr)
        case f: dom.PeriodStartFilter =>
          model.PeriodStartFilter(f.dateExpr, f.timeExprOption)
        case f: dom.PeriodEndFilter =>
          model.PeriodEndFilter(f.dateExpr, f.timeExprOption)
        case f: dom.PeriodInstantFilter =>
          model.PeriodInstantFilter(f.dateExpr, f.timeExprOption)
        case f: dom.ForeverFilter =>
          model.ForeverFilter
        case f: dom.InstantDurationFilter =>
          model.InstantDurationFilter(f.variable, f.boundary)
      }
    }
  }

  def tryToConvertRelativeFilter(domFilter: dom.RelativeFilter): Try[model.RelativeFilter] = {
    Try {
      model.RelativeFilter(domFilter.variable)
    }
  }

  def tryToConvertSegmentScenarioFilter(domFilter: dom.SegmentScenarioFilter): Try[model.SegmentScenarioFilter] = {
    Try {
      domFilter match {
        case f: dom.SegmentFilter =>
          model.SegmentFilter(f.testExprOption)
        case f: dom.ScenarioFilter =>
          model.ScenarioFilter(f.testExprOption)
      }
    }
  }

  def tryToConvertTupleFilter(domFilter: dom.TupleFilter): Try[model.TupleFilter] = {
    Try {
      domFilter match {
        case f: dom.ParentFilter =>
          model.ParentFilter(f.parent.qnameValueOrExpr)
        case f: dom.AncestorFilter =>
          model.AncestorFilter(f.ancestor.qnameValueOrExpr)
        case f: dom.SiblingFilter =>
          model.SiblingFilter(f.variable)
        case f: dom.LocationFilter =>
          model.LocationFilter(f.variable, f.locationExpr)
      }
    }
  }

  def tryToConvertUnitFilter(domFilter: dom.UnitFilter): Try[model.UnitFilter] = {
    Try {
      domFilter match {
        case f: dom.SingleMeasureFilter =>
          model.SingleMeasureFilter(f.measure.qnameValueOrExpr)
        case f: dom.GeneralMeasuresFilter =>
          model.GeneralMeasuresFilter(f.testExpr)
      }
    }
  }

  def tryToConvertValueFilter(domFilter: dom.ValueFilter): Try[model.ValueFilter] = {
    Try {
      domFilter match {
        case f: dom.NilFilter =>
          model.NilFilter
        case f: dom.PrecisionFilter =>
          model.PrecisionFilter(f.minimumExpr)
      }
    }
  }

  def tryToConvertAspectCoverFilter(domFilter: dom.AspectCoverFilter): Try[model.AspectCoverFilter] = {
    Try {
      model.AspectCoverFilter(
        domFilter.aspects.map(_.aspectValue).toSet,
        domFilter.dimensions.map(_.qnameValueOrExpr),
        domFilter.excludeDimensions.map(_.qnameValueOrExpr))
    }
  }

  def tryToConvertConceptRelationFilter(domFilter: dom.ConceptRelationFilter): Try[model.ConceptRelationFilter] = {
    Try {
      model.ConceptRelationFilter(
        domFilter.sourceValueOrExpr,
        domFilter.linkroleValueOrExpr,
        domFilter.linknameValueOrExprOption,
        domFilter.arcroleValueOrExpr,
        domFilter.arcnameValueOrExprOption,
        domFilter.axis.axisValue,
        domFilter.generationsOption.map(_.intValue),
        domFilter.testExprOption)
    }
  }
}
