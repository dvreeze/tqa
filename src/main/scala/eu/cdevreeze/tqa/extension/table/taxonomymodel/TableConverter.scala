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

package eu.cdevreeze.tqa.extension.table.taxonomymodel

import scala.collection.immutable
import scala.util.Try

import eu.cdevreeze.tqa.extension.formula
import eu.cdevreeze.tqa.extension.table.dom
import eu.cdevreeze.tqa.extension.table.model
import eu.cdevreeze.tqa.extension.table.relationship.BreakdownTreeRelationship
import eu.cdevreeze.tqa.extension.table.relationship.DefinitionNodeSubtreeRelationship
import eu.cdevreeze.tqa.extension.table.relationship.TableBreakdownRelationship
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.extension.formula.taxonomymodel.AspectRuleConverter

/**
 * Converter from table DOM tables to tables in the model layer.
 *
 * @author Chris de Vreeze
 */
final class TableConverter(val tableTaxonomy: BasicTableTaxonomy) {

  def tryToConvertTable(domTable: dom.Table): Try[model.Table] = {
    Try {
      val tableBreakdownRelationships: immutable.IndexedSeq[TableBreakdownRelationship] =
        tableTaxonomy.findAllOutgoingTableBreakdownRelationships(domTable)

      val tableBreakdowns =
        tableBreakdownRelationships.map(rel => convertTableBreakdownRelationship(rel))

      model.Table(domTable.parentChildOrder, tableBreakdowns)
    }
  }

  private def convertTableBreakdownRelationship(
    tableBreakdownRelationship: TableBreakdownRelationship): model.TableBreakdown = {

    val breakdown = convertBreakdown(tableBreakdownRelationship.breakdown)

    model.TableBreakdown(
      tableBreakdownRelationship.elr,
      breakdown,
      tableBreakdownRelationship.axis,
      tableBreakdownRelationship.order,
      tableBreakdownRelationship.priority,
      tableBreakdownRelationship.use)
  }

  private def convertBreakdown(domBreakdown: dom.TableBreakdown): model.Breakdown = {
    val breakdownTreeRelationships: immutable.IndexedSeq[BreakdownTreeRelationship] =
      tableTaxonomy.findAllOutgoingBreakdownTreeRelationships(domBreakdown)

    val breakdownTrees: immutable.IndexedSeq[model.BreakdownTree] =
      breakdownTreeRelationships.map(rel => convertBreakdownTreeRelationship(rel))

    model.Breakdown(domBreakdown.parentChildOrderOption, breakdownTrees)
  }

  private def convertBreakdownTreeRelationship(
    breakdownTreeRelationship: BreakdownTreeRelationship): model.BreakdownTree = {

    val subtree = convertDefinitionNode(breakdownTreeRelationship.definitionNode)

    model.BreakdownTree(
      breakdownTreeRelationship.elr,
      subtree,
      breakdownTreeRelationship.order,
      breakdownTreeRelationship.priority,
      breakdownTreeRelationship.use)
  }

  private def convertDefinitionNode(domNode: dom.DefinitionNode): model.DefinitionNode = domNode match {
    case n: dom.RuleNode                  => convertRuleNode(n)
    case n: dom.ConceptRelationshipNode   => convertConceptRelationshipNode(n)
    case n: dom.DimensionRelationshipNode => convertDimensionRelationshipNode(n)
    case n: dom.AspectNode                => convertAspectNode(n)
  }

  private def convertDefinitionNodeSubtreeRelationship(
    definitionNodeSubtreeRelationship: DefinitionNodeSubtreeRelationship): model.DefinitionNodeSubtree = {

    val subtree = convertDefinitionNode(definitionNodeSubtreeRelationship.toNode)

    model.DefinitionNodeSubtree(
      definitionNodeSubtreeRelationship.elr,
      subtree,
      definitionNodeSubtreeRelationship.order,
      definitionNodeSubtreeRelationship.priority,
      definitionNodeSubtreeRelationship.use)
  }

  private def convertRuleNode(domNode: dom.RuleNode): model.RuleNode = {
    val definitionNodeSubtreeRelationships: immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] =
      tableTaxonomy.findAllOutgoingDefinitionNodeSubtreeRelationships(domNode)

    val definitionNodeSubtrees =
      definitionNodeSubtreeRelationships.map(rel => convertDefinitionNodeSubtreeRelationship(rel))

    val untaggedAspects: immutable.IndexedSeq[formula.model.AspectRule] =
      domNode.untaggedAspects.map(aspect => AspectRuleConverter.convertAspectRule(aspect))

    val ruleSets = domNode.ruleSets.map(ruleSet => convertRuleSet(ruleSet))

    model.RuleNode(
      domNode.parentChildOrderOption,
      domNode.tagSelectorOption,
      untaggedAspects,
      ruleSets,
      domNode.isAbstract,
      domNode.isMerged,
      definitionNodeSubtrees)
  }

  private def convertConceptRelationshipNode(domNode: dom.ConceptRelationshipNode): model.ConceptRelationshipNode = {
    val definitionNodeSubtreeRelationships: immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] =
      tableTaxonomy.findAllOutgoingDefinitionNodeSubtreeRelationships(domNode)

    val definitionNodeSubtrees =
      definitionNodeSubtreeRelationships.map(rel => convertDefinitionNodeSubtreeRelationship(rel))

    model.ConceptRelationshipNode(
      domNode.parentChildOrderOption,
      domNode.tagSelectorOption,
      domNode.sourceValuesOrExpressions,
      domNode.linkroleValueOrExprOption,
      domNode.linknameValueOrExprOption,
      domNode.arcroleValueOrExpr,
      domNode.arcnameValueOrExprOption,
      domNode.formulaAxisValueOrExprOption,
      domNode.generationsValueOrExprOption,
      definitionNodeSubtrees)
  }

  private def convertDimensionRelationshipNode(domNode: dom.DimensionRelationshipNode): model.DimensionRelationshipNode = {
    val definitionNodeSubtreeRelationships: immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] =
      tableTaxonomy.findAllOutgoingDefinitionNodeSubtreeRelationships(domNode)

    val definitionNodeSubtrees =
      definitionNodeSubtreeRelationships.map(rel => convertDefinitionNodeSubtreeRelationship(rel))

    model.DimensionRelationshipNode(
      domNode.parentChildOrderOption,
      domNode.tagSelectorOption,
      domNode.dimensionName,
      domNode.sourceValuesOrExpressions,
      domNode.linkroleValueOrExprOption,
      domNode.formulaAxisValueOrExprOption,
      domNode.generationsValueOrExprOption,
      definitionNodeSubtrees)
  }

  private def convertAspectNode(domNode: dom.AspectNode): model.AspectNode = {
    val definitionNodeSubtreeRelationships: immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] =
      tableTaxonomy.findAllOutgoingDefinitionNodeSubtreeRelationships(domNode)

    val definitionNodeSubtrees =
      definitionNodeSubtreeRelationships.map(rel => convertDefinitionNodeSubtreeRelationship(rel))

    val aspectSpec: model.AspectSpec = convertAspectSpec(domNode.aspectSpec)

    model.AspectNode(
      aspectSpec,
      domNode.tagSelectorOption,
      definitionNodeSubtrees)
  }

  private def convertRuleSet(domRuleSet: dom.RuleSet): model.RuleSet = {
    model.RuleSet(
      domRuleSet.aspects.map(aspect => AspectRuleConverter.convertAspectRule(aspect)),
      domRuleSet.tag)
  }

  private def convertAspectSpec(domAspectSpec: dom.AspectSpec): model.AspectSpec = domAspectSpec match {
    case e: dom.ConceptAspectSpec          => model.ConceptAspectSpec
    case e: dom.UnitAspectSpec             => model.UnitAspectSpec
    case e: dom.EntityIdentifierAspectSpec => model.EntityIdentifierAspectSpec
    case e: dom.PeriodAspectSpec           => model.PeriodAspectSpec
    case e: dom.DimensionAspectSpec        => model.DimensionAspectSpec(e.dimension, e.isIncludeUnreportedValue)
  }
}
