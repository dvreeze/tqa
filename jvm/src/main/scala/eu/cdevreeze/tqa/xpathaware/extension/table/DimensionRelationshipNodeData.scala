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

import eu.cdevreeze.tqa.base.dom.BaseSetKey
import eu.cdevreeze.tqa.base.queryapi.DomainAwareRelationshipPath
import eu.cdevreeze.tqa.base.queryapi.DomainMemberRelationshipPath
import eu.cdevreeze.tqa.base.relationship.DomainAwareRelationship
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
   * Finds all "result paths" according to the given dimension relationship node in the given taxonomy.
   * All concepts in the result paths (including the roots) belong to the resolution of the dimension relationship node.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def findAllResultPaths(
    dimensionRelationshipNode: DimensionRelationshipNode,
    taxo:                      BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator, scope: Scope): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    val relationshipsToSources: immutable.IndexedSeq[DomainAwareRelationship] =
      findAllDomainAwareRelationshipsToRelationshipSources(dimensionRelationshipNode, taxo)(xpathEvaluator, scope)

    val resultPaths: immutable.IndexedSeq[DomainMemberRelationshipPath] =
      findAllResultPaths(relationshipsToSources, dimensionRelationshipNode, taxo)

    resultPaths
  }

  /**
   * Finds all relationships to relationship sources according to the given dimension relationship node in the given taxonomy.
   *
   * TODO Come up with invented domain-aware relationships where needed, or, alternatively, do not return relationships but pairs of ELRs and domain-members
   * (like "keys" of domain-aware relationship targets), including invented ones. Using these pairs, we can compute consecutive relationship paths, without
   * having to invent relationships.
   */
  def findAllDomainAwareRelationshipsToRelationshipSources(
    dimensionRelationshipNode: DimensionRelationshipNode,
    taxo:                      BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator, scope: Scope): immutable.IndexedSeq[DomainAwareRelationship] = {

    // Start with reading the needed information in the dimension relationship node.

    val dimension: EName = dimensionRelationshipNode.dimensionName

    val dimensionRelationNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    val rawRelationshipSources: immutable.IndexedSeq[EName] =
      dimensionRelationNodeData.relationshipSources(xpathEvaluator, scope)

    val linkroleOption: Option[String] = dimensionRelationNodeData.linkroleOption(xpathEvaluator, scope)

    val effectiveLinkrole: String = linkroleOption.getOrElse(BaseSetKey.StandardElr)

    // First find all has-hypercubes matching the given linkrole (default: standard linkrole), and their consecutive hypercube-dimensions.
    // Note that we interpret the linkrole as the ELR of the has-hypercube relationship(s).

    // Starting with incoming hypercube-dimension relationships for performance.

    val potentialHypercubeDimensions =
      taxo.underlyingTaxonomy.findAllIncomingHypercubeDimensionRelationships(dimension)

    val potentialHasHypercubes = potentialHypercubeDimensions
      .flatMap(hd => taxo.underlyingTaxonomy.filterIncomingHasHypercubeRelationships(hd.hypercube)(_.isFollowedBy(hd)))
      .distinct

    val hasHypercubes = potentialHasHypercubes.filter(hh => hh.elr == effectiveLinkrole)

    val hypercubeDimensions = hasHypercubes
      .flatMap(hh => taxo.underlyingTaxonomy.findAllConsecutiveHypercubeDimensionRelationships(hh))
      .filter(_.dimension == dimension)
      .distinct

    // Next find all domain-aware relationship paths ending in the relationship sources.

    val pathsToRelationshipSources: immutable.IndexedSeq[DomainAwareRelationshipPath] =
      if (rawRelationshipSources.isEmpty) {
        hypercubeDimensions
          .flatMap { hd =>
            taxo.underlyingTaxonomy.filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { p =>
              hd.isFollowedBy(p.firstRelationship) && p.relationships.size <= 1
            }
          }
          .filter(_.relationships.size == 1)
          .distinct
      } else {
        val sources: Set[EName] = rawRelationshipSources.toSet

        hypercubeDimensions
          .flatMap { hd =>
            taxo.underlyingTaxonomy.filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { p =>
              hd.isFollowedBy(p.firstRelationship) && p.initOption.forall(_.concepts.toSet.intersect(sources).isEmpty)
            }
          }
          .filter(path => sources.contains(path.targetConcept))
          .distinct
      }

    pathsToRelationshipSources.map(_.lastRelationship).distinct
  }

  /**
   * Finds all "result paths" starting with the given relationships to sources according to the given dimension relationship node in the given taxonomy.
   * All concepts in the result paths (including the roots) belong to the resolution of the dimension relationship node.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def findAllResultPaths(
    relationshipsToSources:    immutable.IndexedSeq[DomainAwareRelationship],
    dimensionRelationshipNode: DimensionRelationshipNode,
    taxo:                      BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator, scope: Scope): immutable.IndexedSeq[DomainMemberRelationshipPath] = {

    // Start with reading the needed information in the dimension relationship node.

    val dimensionRelationNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)
    val axis = dimensionRelationNodeData.formulaAxis(xpathEvaluator, scope)

    val includeSelf: Boolean = axis.includesSelf

    // Number of generations (optional), from the perspective of finding the descendant-or-self
    // (or only descendant) concepts. So 1 for the child axis, for example. 0 becomes None.
    val effectiveGenerationsOption: Option[Int] = {
      val rawValue = dimensionRelationNodeData.generations(xpathEvaluator, scope)
      val optionalRawResult = if (rawValue == 0) None else Some(rawValue)
      val resultOption = if (axis.includesChildrenButNotDeeperDescendants) Some(1) else optionalRawResult
      resultOption
    }

    // Next resolve the dimension relationship node

    val parentRelationships: immutable.IndexedSeq[DomainAwareRelationship] =
      if (includeSelf) {
        relationshipsToSources
      } else {
        relationshipsToSources.flatMap(rel => taxo.underlyingTaxonomy.findAllConsecutiveDomainMemberRelationships(rel)).distinct
      }

    val resultPaths: immutable.IndexedSeq[DomainMemberRelationshipPath] =
      parentRelationships
        .flatMap { rel =>
          taxo.underlyingTaxonomy.filterOutgoingConsecutiveDomainMemberRelationshipPaths(rel.targetConceptEName) { path =>
            rel.isFollowedBy(path.firstRelationship) && effectiveGenerationsOption.forall(gen => path.relationships.size <= gen)
          }
        }
        .filter { path =>
          effectiveGenerationsOption.forall(gen => path.relationships.size == gen)
        }

    // TODO Ignore unusable members without any usable descendants

    resultPaths
  }
}
