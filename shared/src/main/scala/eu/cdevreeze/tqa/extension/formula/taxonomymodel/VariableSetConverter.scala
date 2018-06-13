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
import scala.util.Try

import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetFilterRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetPreconditionRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetRelationship
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy

/**
 * Converter from formula DOM variable sets to variable sets in the model layer.
 *
 * @author Chris de Vreeze
 */
final class VariableSetConverter(val formulaTaxonomy: BasicFormulaTaxonomy) {

  private val filterConverter = new FilterConverter(formulaTaxonomy)

  def tryToConvertVariableSet(domVariableSet: dom.VariableSet): Try[model.VariableSet] = domVariableSet match {
    case f: dom.ValueAssertion => tryToConvertValueAssertion(f)
    case f: dom.ExistenceAssertion => tryToConvertExistenceAssertion(f)
    case f: dom.Formula => tryToConvertFormula(f)
  }

  def tryToConvertVariableSetAssertion(domVariableSetAssertion: dom.VariableSetAssertion): Try[model.VariableSetAssertion] = {
    domVariableSetAssertion match {
      case f: dom.ValueAssertion => tryToConvertValueAssertion(f)
      case f: dom.ExistenceAssertion => tryToConvertExistenceAssertion(f)
    }
  }

  def tryToConvertValueAssertion(domVariableSet: dom.ValueAssertion): Try[model.ValueAssertion] = {
    Try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      model.ValueAssertion(
        domVariableSet.underlyingResource.idOption,
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.testExpr,
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters)
    }
  }

  def tryToConvertExistenceAssertion(domVariableSet: dom.ExistenceAssertion): Try[model.ExistenceAssertion] = {
    Try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      model.ExistenceAssertion(
        domVariableSet.underlyingResource.idOption,
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.testExprOption,
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters)
    }
  }

  def tryToConvertFormula(domVariableSet: dom.Formula): Try[model.Formula] = {
    Try {
      val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
        extractVariableSetFilters(domVariableSet)

      val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
        extractVariableSetPreconditions(domVariableSet)

      val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
        extractVariableSetVariablesOrParameters(domVariableSet)

      val aspectRuleGroups: immutable.IndexedSeq[model.AspectRuleGroup] =
        extractAspectRuleGroups(domVariableSet)

      model.Formula(
        domVariableSet.underlyingResource.idOption,
        domVariableSet.implicitFiltering,
        domVariableSet.aspectModel,
        domVariableSet.sourceOption,
        domVariableSet.valueExpr,
        aspectRuleGroups,
        domVariableSet.precisionElemOption.map(_.expr),
        domVariableSet.decimalsElemOption.map(_.expr),
        variableSetFilters,
        variableSetPreconditions,
        variableSetVariablesOrParameters)
    }
  }

  private def extractVariableSetFilters(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetFilter] = {
    val varSetFilterRelationships: immutable.IndexedSeq[VariableSetFilterRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetFilterRelationships(domVariableSet).sortBy(_.order)

    val variableSetFilters: immutable.IndexedSeq[model.VariableSetFilter] =
      varSetFilterRelationships map { rel =>
        // Throwing an exception if not successful, and that is ok here.
        val filter = filterConverter.tryToConvertFilter(rel.filter).get

        model.VariableSetFilter(
          model.CommonRelationshipAttributes(
            rel.elr,
            rel.order,
            rel.priority,
            rel.use),
          rel.complement,
          filter)
      }

    variableSetFilters
  }

  private def extractVariableSetPreconditions(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetPrecondition] = {
    val varSetPreconditionRelationships: immutable.IndexedSeq[VariableSetPreconditionRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetPreconditionRelationships(domVariableSet).sortBy(_.order)

    val variableSetPreconditions: immutable.IndexedSeq[model.VariableSetPrecondition] =
      varSetPreconditionRelationships map { rel =>
        val precondition = model.Precondition(rel.precondition.underlyingResource.idOption, rel.precondition.testExpr)

        model.VariableSetPrecondition(
          model.CommonRelationshipAttributes(
            rel.elr,
            rel.order,
            rel.priority,
            rel.use),
          precondition)
      }

    variableSetPreconditions
  }

  private def extractVariableSetVariablesOrParameters(domVariableSet: dom.VariableSet): immutable.IndexedSeq[model.VariableSetVariableOrParameter] = {
    val varSetRelationships: immutable.IndexedSeq[VariableSetRelationship] =
      formulaTaxonomy.findAllOutgoingVariableSetRelationships(domVariableSet).sortBy(_.order)

    val variableSetVariablesOrParameters: immutable.IndexedSeq[model.VariableSetVariableOrParameter] =
      varSetRelationships map { rel =>
        val domVarOrPar = rel.variableOrParameter

        val varOrPar: model.VariableOrParameter = domVarOrPar match {
          case par: dom.Parameter =>
            model.Parameter(par.underlyingResource.idOption, par.name, par.selectExprOption, par.requiredOption, par.asOption)
          case genVar: dom.GeneralVariable =>
            model.GeneralVariable(genVar.underlyingResource.idOption, genVar.bindAsSequence, genVar.selectExpr)
          case factVar: dom.FactVariable =>
            val varFilterRelationships =
              formulaTaxonomy.findAllOutgoingVariableFilterRelationships(factVar).sortBy(_.order)

            val variableFilters: immutable.IndexedSeq[model.VariableFilter] = varFilterRelationships map { rel =>
              // Throwing an exception if not successful, and that is ok here.
              val filter = filterConverter.tryToConvertFilter(rel.filter).get

              model.VariableFilter(
                model.CommonRelationshipAttributes(
                  rel.elr,
                  rel.order,
                  rel.priority,
                  rel.use),
                rel.complement,
                rel.cover,
                filter)
            }

            model.FactVariable(
              factVar.underlyingResource.idOption,
              factVar.bindAsSequence,
              factVar.fallbackValueExprOption,
              factVar.matchesOption,
              factVar.nilsOption,
              variableFilters)
        }

        model.VariableSetVariableOrParameter(
          model.CommonRelationshipAttributes(
            rel.elr,
            rel.order,
            rel.priority,
            rel.use),
          rel.name,
          varOrPar)
      }

    variableSetVariablesOrParameters
  }

  private def extractAspectRuleGroups(domFormula: dom.Formula): immutable.IndexedSeq[model.AspectRuleGroup] = {
    domFormula.formulaAspectsElems map { aspectsElem =>
      model.AspectRuleGroup(
        aspectsElem.sourceOption,
        aspectsElem.formulaAspects.map(asp => AspectRuleConverter.convertAspectRule(asp)))
    }
  }
}
