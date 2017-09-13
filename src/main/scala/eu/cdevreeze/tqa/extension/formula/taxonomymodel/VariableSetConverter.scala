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

import org.scalactic.Bad
import org.scalactic.Good
import org.scalactic.One
import org.scalactic.Or

import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetFilterRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetPreconditionRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetRelationship
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy

/**
 * Converter from formula taxonomy filters to filters in the model layer.
 *
 * @author Chris de Vreeze
 */
final class VariableSetConverter(val formulaTaxonomy: BasicFormulaTaxonomy) {

  private val filterConverter = new FilterConverter(formulaTaxonomy)

  def convertVariableSet(domVariableSet: dom.VariableSet): model.VariableSet Or One[ConversionError] = domVariableSet match {
    case f: dom.ValueAssertion     => convertValueAssertion(f)
    case f: dom.ExistenceAssertion => convertExistenceAssertion(f)
    case f: dom.Formula            => convertFormula(f)
  }

  def convertValueAssertion(domVariableSet: dom.ValueAssertion): model.ValueAssertion Or One[ConversionError] = {
    try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      Good(model.ValueAssertion(
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.testExpr,
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters))
    } catch {
      case exc: Exception => Bad(One(VariableSetConversionError(s"Could not convert value assertion ${domVariableSet.key}", exc)))
    }
  }

  def convertExistenceAssertion(domVariableSet: dom.ExistenceAssertion): model.ExistenceAssertion Or One[ConversionError] = {
    try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      Good(model.ExistenceAssertion(
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.testExprOption,
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters))
    } catch {
      case exc: Exception => Bad(One(VariableSetConversionError(s"Could not convert existence assertion ${domVariableSet.key}", exc)))
    }
  }

  def convertFormula(domVariableSet: dom.Formula): model.Formula Or One[ConversionError] = {
    try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      val aspectRuleGroups: immutable.IndexedSeq[model.AspectRuleGroup] =
        extractAspectRuleGroups(domVariableSet)

      Good(model.Formula(
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.sourceOption,
        domVariableSet.valueExpr,
        aspectRuleGroups,
        domVariableSet.precisionElemOption.map(_.expr),
        domVariableSet.decimalsElemOption.map(_.expr),
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters))
    } catch {
      case exc: Exception => Bad(One(VariableSetConversionError(s"Could not convert formula ${domVariableSet.key}", exc)))
    }
  }

  private def extractVariableSetFilters(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetFilter] = {
    val varSetFilterRelationships: immutable.IndexedSeq[VariableSetFilterRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetFilterRelationships(domVariableSet)

    val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
      varSetFilterRelationships flatMap { rel =>
        val filterOption = filterConverter.convertFilter(rel.filter).toOption

        filterOption.map(filter => model.VariableSetFilter(rel.elr, filter, rel.complement, rel.order, rel.priority, rel.use))
      }

    variableSetFilters
  }

  private def extractVariableSetPreconditions(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetPrecondition] = {
    val varSetPreconditionRelationships: immutable.IndexedSeq[VariableSetPreconditionRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetPreconditionRelationships(domVariableSet)

    val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
      varSetPreconditionRelationships map { rel =>
        val precondition = model.Precondition(rel.precondition.testExpr)

        model.VariableSetPrecondition(rel.elr, precondition, rel.order, rel.priority, rel.use)
      }

    variableSetPreconditions
  }

  private def extractVariableSetVariablesOrParameters(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetVariableOrParameter] = {
    val varSetRelationships: immutable.IndexedSeq[VariableSetRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetRelationships(domVariableSet)

    val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
      varSetRelationships map { rel =>
        val domVarOrPar = rel.variableOrParameter

        val varOrPar: model.VariableOrParameter = domVarOrPar match {
          case par: dom.Parameter =>
            model.Parameter(par.name, par.selectExprOption, par.requiredOption, par.asOption)
          case genVar: dom.GeneralVariable =>
            model.GeneralVariable(genVar.bindAsSequence, genVar.selectExpr)
          case factVar: dom.FactVariable =>
            val varFilterRelationships = formulaTaxonomy.findAllOutgoingVariableFilterRelationships(factVar)

            val variableFilters: immutable.IndexedSeq[model.VariableFilter] = varFilterRelationships flatMap { rel =>
              val filterOption = filterConverter.convertFilter(rel.filter).toOption

              filterOption map { filter =>
                model.VariableFilter(rel.elr, filter, rel.complement, rel.cover, rel.order, rel.priority, rel.use)
              }
            }

            model.FactVariable(factVar.bindAsSequence, factVar.fallbackValueExprOption, factVar.matchesOption, factVar.nilsOption, variableFilters)
        }

        model.VariableSetVariableOrParameter(rel.elr, varOrPar, rel.name, rel.order, rel.priority, rel.use)
      }

    variableSetVariablesOrParameters
  }

  private def extractAspectRuleGroups(domFormula: dom.Formula): immutable.IndexedSeq[model.AspectRuleGroup] = {
    domFormula.formulaAspectsElems map { aspectsElem =>
      model.AspectRuleGroup(aspectsElem.sourceOption, aspectsElem.formulaAspects.map(asp => convertAspectRule(asp)))
    }
  }

  private def convertAspectRule(domAspectRule: dom.FormulaAspect): model.AspectRule = domAspectRule match {
    case fa: dom.ConceptAspect =>
      model.ConceptAspectRule(fa.sourceOption, fa.qnameValueOrExprOption)
    case fa: dom.EntityIdentifierAspect =>
      model.EntityIdentifierAspectRule(fa.sourceOption, fa.schemeExprOption, fa.valueExprOption)
    case fa: dom.PeriodAspect =>
      val periods: immutable.IndexedSeq[model.PeriodAspectRule.Period] = fa.periodElems map {
        case pe: dom.ForeverElem  => model.PeriodAspectRule.ForeverPeriod
        case pe: dom.InstantElem  => model.PeriodAspectRule.InstantPeriod(pe.valueExprOption)
        case pe: dom.DurationElem => model.PeriodAspectRule.DurationPeriod(pe.startExprOption, pe.endExprOption)
      }

      model.PeriodAspectRule(fa.sourceOption, periods)
    case fa: dom.UnitAspect =>
      model.UnitAspectRule(
        fa.sourceOption,
        fa.multiplyByElems.map(e => model.MultiplyBy(e.sourceOption, e.measureExprOption)),
        fa.divideByElems.map(e => model.DivideBy(e.sourceOption, e.measureExprOption)),
        fa.augmentOption)
    case fa: dom.OccEmptyAspect =>
      model.OccEmptyAspectRule(fa.sourceOption, fa.occ)
    case fa: dom.OccFragmentsAspect =>
      model.OccFragmentsAspectRule(fa.sourceOption, fa.occ)
    case fa: dom.OccXpathAspect =>
      model.OccXPathAspectRule(fa.sourceOption, fa.occ, fa.selectExprOption)
    case fa: dom.ExplicitDimensionAspect =>
      model.ExplicitDimensionAspectRule(fa.sourceOption, fa.dimension, fa.memberElemOption.map(_.qnameValueOrExpr))
    case fa: dom.TypedDimensionAspect =>
      // TODO Typed dimension member!

      model.TypedDimensionAspectRule(fa.sourceOption, fa.dimension, None)
  }
}
