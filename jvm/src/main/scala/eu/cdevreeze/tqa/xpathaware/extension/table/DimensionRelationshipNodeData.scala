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

package eu.cdevreeze.tqa.xpathaware.extension.table

import scala.collection.immutable

import eu.cdevreeze.tqa.base.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.base.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa.base.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.tqa.extension.table.common.DimensionRelationshipNodes
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.xpath.XPathEvaluator

/**
 * Wrapper around a DimensionRelationshipNode, which can extract the relevant data by evaluating XPath where needed.
 *
 * @author Chris de Vreeze
 */
final class DimensionRelationshipNodeData(val dimensionRelationshipNode: DimensionRelationshipNode) {

  // Below, make sure that the passed XPathEvaluator knows about the needed namespace bindings in the XPath expressions.

  /**
   * Returns the dimension as EName, by calling `dimensionRelationshipNode.dimensionName`.
   */
  def dimensionName: EName = dimensionRelationshipNode.dimensionName

  def relationshipSources(implicit xpathEvaluator: XPathEvaluator, scope: Scope): immutable.IndexedSeq[EName] = {
    dimensionRelationshipNode.sourceValuesOrExpressions.
      map(valueOrExpr => ENameValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator, scope))
  }

  def linkroleOption(implicit xpathEvaluator: XPathEvaluator, scope: Scope): Option[String] = {
    dimensionRelationshipNode.linkroleValueOrExprOption.
      map(valueOrExpr => StringValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator, scope))
  }

  def formulaAxis(implicit xpathEvaluator: XPathEvaluator, scope: Scope): DimensionRelationshipNodes.FormulaAxis = {
    val stringResultOption =
      dimensionRelationshipNode.formulaAxisValueOrExprOption.
        map(valueOrExpr => StringValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator, scope))

    stringResultOption.map(v => DimensionRelationshipNodes.FormulaAxis.fromString(v)).
      getOrElse(DimensionRelationshipNodes.FormulaAxis.DescendantOrSelfAxis)
  }

  def generations(implicit xpathEvaluator: XPathEvaluator, scope: Scope): Int = {
    val resultAsBigDecimalOption =
      dimensionRelationshipNode.generationsValueOrExprOption.
        map(valueOrExpr => BigDecimalValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator, scope))

    resultAsBigDecimalOption.map(_.toInt).getOrElse(0)
  }
}

object DimensionRelationshipNodeData {

  /**
   * Finds all dimension members referred to by the given dimension relationship node in the given taxonomy.
   */
  def findAllMembersInDimensionRelationshipNode(
    dimensionRelationshipNode: DimensionRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator, scope: Scope): Set[EName] = {

    val dimension: EName = dimensionRelationshipNode.dimensionName

    val dimensionRelationNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)
    val axis = dimensionRelationNodeData.formulaAxis(xpathEvaluator, scope)

    val rawRelationshipSources: immutable.IndexedSeq[EName] =
      dimensionRelationNodeData.relationshipSources(xpathEvaluator, scope)

    val linkroleOption: Option[String] = dimensionRelationNodeData.linkroleOption(xpathEvaluator, scope)

    val startMembers: immutable.IndexedSeq[DimensionMemberTreeWalkSpec.StartMember] = {
      if (rawRelationshipSources.isEmpty) {
        val dimDomRelationships =
          taxo.underlyingTaxonomy.filterOutgoingDimensionDomainRelationships(dimension) { rel =>
            linkroleOption.forall(_ == rel.elr)
          }

        dimDomRelationships.map(rel => new DimensionMemberTreeWalkSpec.DimensionDomainSource(rel))
      } else {
        rawRelationshipSources.map(member => new DimensionMemberTreeWalkSpec.MemberSource(member))
      }
    }

    val includeSelf: Boolean = axis.includesSelf

    // Number of generations (optional), from the perspective of finding the descendant-or-self
    // (or only descendant) concepts. So 1 for the child axis, for example. 0 becomes None.
    val effectiveGenerationsOption: Option[Int] = {
      val rawValue = dimensionRelationNodeData.generations(xpathEvaluator, scope)
      val optionalRawResult = if (rawValue == 0) None else Some(rawValue)
      val resultOption = if (axis.includesChildrenButNotDeeperDescendants) Some(1) else optionalRawResult
      resultOption
    }

    val dimensionMemberTreeWalkSpecs: immutable.IndexedSeq[DimensionMemberTreeWalkSpec] =
      startMembers map { startMember =>
        new DimensionMemberTreeWalkSpec(dimension, startMember, includeSelf, effectiveGenerationsOption, linkroleOption)
      }

    // Find the descendant-or-self or descendant members for the given number of generations, if applicable.
    val conceptsExcludingSiblings: Set[EName] =
      (dimensionMemberTreeWalkSpecs.map(spec => filterDescendantOrSelfMembers(spec, taxo))).flatten.toSet

    conceptsExcludingSiblings
  }

  /**
   * Returns the descendant-or-self members in a dimension-member tree walk according to the parameter specification of the walk.
   * If the start member must not be included, the tree walk finds descendant members instead of descendant-or-self members.
   *
   * It is assumed yet not checked that the tree walk can be made for the given dimension (so the dimension is indeed an ancestor
   * in a sequence of consecutive "domain-aware" relationships).
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def filterDescendantOrSelfMembers(
    treeWalkSpec: DimensionMemberTreeWalkSpec,
    taxo: BasicTableTaxonomy): Set[EName] = {

    // Ignoring unusable members without any usable descendants

    val relationshipPaths =
      taxo.underlyingTaxonomy.filterOutgoingConsecutiveDomainMemberRelationshipPaths(
        treeWalkSpec.startMemberName) { path =>
          treeWalkSpec.startMember.incomingRelationshipOption.forall(_.isFollowedBy(path.firstRelationship)) &&
            path.isConsecutiveRelationshipPath &&
            treeWalkSpec.generationsOption.forall(gen => path.relationships.size <= gen) &&
            treeWalkSpec.linkroleOption.forall(lr => treeWalkSpec.elrToCheck(path) == lr)
        }

    val resultIncludingStartMember = relationshipPaths.flatMap(_.concepts).toSet
    if (treeWalkSpec.includeSelf) resultIncludingStartMember else resultIncludingStartMember.diff(Set(treeWalkSpec.startMemberName))
  }

  /**
   * Specification of a dimension member tree walk for some explicit dimension, starting with one member.
   * The tree walk finds descendant-or-self members in the network, but if the start member must
   * be excluded the tree walk only finds descendant members.
   *
   * The optional generations cannot contain 0. None means unbounded.
   */
  final case class DimensionMemberTreeWalkSpec(
      val explicitDimension: EName,
      val startMember: DimensionMemberTreeWalkSpec.StartMember,
      val includeSelf: Boolean,
      val generationsOption: Option[Int],
      val linkroleOption: Option[String]) {

    def startMemberName: EName = startMember.startMemberName

    def elrToCheck(path: InterConceptRelationshipPath[DomainMemberRelationship]): String = {
      startMember match {
        case startMember: DimensionMemberTreeWalkSpec.DimensionDomainSource =>
          startMember.dimensionDomainRelationship.elr
        case startMember: DimensionMemberTreeWalkSpec.MemberSource =>
          path.relationships.head.elr
      }
    }
  }

  object DimensionMemberTreeWalkSpec {

    sealed trait StartMember {
      def startMemberName: EName
      def incomingRelationshipOption: Option[DomainAwareRelationship]
    }

    final class DimensionDomainSource(val dimensionDomainRelationship: DimensionDomainRelationship) extends StartMember {
      def startMemberName: EName = dimensionDomainRelationship.targetConceptEName
      def incomingRelationshipOption: Option[DomainAwareRelationship] = Some(dimensionDomainRelationship)
    }

    final class MemberSource(val startMemberName: EName) extends StartMember {
      def incomingRelationshipOption: Option[DomainAwareRelationship] = None
    }
  }
}
