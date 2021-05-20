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

package eu.cdevreeze.tqa.extension.table.model

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.BigDecimalValueOrExpr
import eu.cdevreeze.tqa.ENameValueOrExpr
import eu.cdevreeze.tqa.StringValueOrExpr
import eu.cdevreeze.tqa.extension.formula.model.AspectRule
import eu.cdevreeze.tqa.extension.table.common.ParentChildOrder
import eu.cdevreeze.yaidom.core.EName

/**
 * Definition node.
 *
 * @author Chris de Vreeze
 */
sealed trait DefinitionNode extends Resource {

  def tagSelectorOption: Option[String]

  def definitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree]

  final def findAllOwnOrDescendantDefinitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree] = {
    definitionNodeSubtrees.flatMap { defNodeSubtree =>
      // Recursive call
      defNodeSubtree +: (defNodeSubtree.target.findAllOwnOrDescendantDefinitionNodeSubtrees)
    }
  }

  final def children: immutable.IndexedSeq[DefinitionNode] = {
    definitionNodeSubtrees.map(_.target)
  }

  final def descendantsOrSelf: immutable.IndexedSeq[DefinitionNode] = {
    // Recursive calls
    this +: (children.flatMap(_.descendantsOrSelf))
  }

  final def descendants: immutable.IndexedSeq[DefinitionNode] = {
    children.flatMap(_.descendantsOrSelf)
  }
}

sealed trait ClosedDefinitionNode extends DefinitionNode {

  def parentChildOrderOption: Option[ParentChildOrder]
}

sealed trait OpenDefinitionNode extends DefinitionNode

sealed trait RelationshipNode extends ClosedDefinitionNode

final case class ConceptRelationshipNode(
  idOption: Option[String],
  parentChildOrderOption: Option[ParentChildOrder],
  tagSelectorOption: Option[String],
  relationshipSourceNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr],
  linkroleOrExprOption: Option[StringValueOrExpr],
  linknameOrExprOption: Option[ENameValueOrExpr],
  arcroleOrExpr: StringValueOrExpr,
  arcnameOrExprOption: Option[ENameValueOrExpr],
  formulaAxisOrExprOption: Option[StringValueOrExpr], // Parsable as ConceptRelationshipNodes.FormulaAxis
  generationsOrExprOption: Option[BigDecimalValueOrExpr],
  definitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree]) extends RelationshipNode

final case class DimensionRelationshipNode(
  idOption: Option[String],
  parentChildOrderOption: Option[ParentChildOrder],
  tagSelectorOption: Option[String],
  dimension: EName,
  relationshipSourceNamesOrExprs: immutable.IndexedSeq[ENameValueOrExpr],
  linkroleOrExprOption: Option[StringValueOrExpr],
  formulaAxisOrExprOption: Option[StringValueOrExpr], // Parsable as DimensionRelationshipNodes.FormulaAxis
  generationsOrExprOption: Option[BigDecimalValueOrExpr],
  definitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree]) extends RelationshipNode

final case class RuleNode(
  idOption: Option[String],
  parentChildOrderOption: Option[ParentChildOrder],
  tagSelectorOption: Option[String],
  untaggedAspects: immutable.IndexedSeq[AspectRule],
  ruleSets: immutable.IndexedSeq[RuleSet],
  isAbstract: Boolean,
  isMerged: Boolean,
  definitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree]) extends ClosedDefinitionNode {

  def allAspectRules: immutable.IndexedSeq[AspectRule] = {
    untaggedAspects ++ (ruleSets.flatMap(_.aspects))
  }

  def findAllUntaggedAspectsOfType[A <: AspectRule](cls: ClassTag[A]): immutable.IndexedSeq[A] = {
    implicit val clsTag = cls
    untaggedAspects.collect { case asp: A => asp }
  }

  def allAspectsByTagOption: Map[Option[String], immutable.IndexedSeq[AspectRule]] = {
    val taggedAspects: immutable.IndexedSeq[(AspectRule, Option[String])] =
      untaggedAspects.map(aspect => (aspect, None)) ++
        ruleSets.flatMap(ruleSet => ruleSet.aspects.map(aspect => (aspect, Some(ruleSet.tag))))

    taggedAspects.groupBy({ case (aspect, tagOption) => tagOption }).view.mapValues(taggedAspects => taggedAspects.map(_._1)).toMap
  }
}

final case class AspectNode(
  idOption: Option[String],
  aspectSpec: AspectSpec,
  tagSelectorOption: Option[String],
  definitionNodeSubtrees: immutable.IndexedSeq[DefinitionNodeSubtree]) extends OpenDefinitionNode
