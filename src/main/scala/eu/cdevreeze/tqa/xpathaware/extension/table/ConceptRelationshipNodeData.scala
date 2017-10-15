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
import scala.reflect.classTag

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.extension.table.common.ConceptRelationshipNodes
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator
import eu.cdevreeze.yaidom.core.EName

/**
 * Wrapper around a ConceptRelationshipNode, which can extract the relevant data by evaluating XPath where needed.
 *
 * @author Chris de Vreeze
 */
final class ConceptRelationshipNodeData(val conceptRelationshipNode: ConceptRelationshipNode) {

  // Below, make sure that the passed XPathEvaluator knows about the needed namespace bindings in the XPath expressions.

  def relationshipSources(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[EName] = {
    conceptRelationshipNode.sourceValuesOrExpressions.
      map(valueOrExpr => ENameValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))
  }

  def linkroleOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    conceptRelationshipNode.linkroleValueOrExprOption.
      map(valueOrExpr => StringValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))
  }

  def arcrole(implicit xpathEvaluator: XPathEvaluator): String = {
    StringValueOrExprEvaluator.evaluate(conceptRelationshipNode.arcroleValueOrExpr)(xpathEvaluator)
  }

  def linknameOption(implicit xpathEvaluator: XPathEvaluator): Option[EName] = {
    conceptRelationshipNode.linknameValueOrExprOption.
      map(valueOrExpr => ENameValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))
  }

  def arcnameOption(implicit xpathEvaluator: XPathEvaluator): Option[EName] = {
    conceptRelationshipNode.arcnameValueOrExprOption.
      map(valueOrExpr => ENameValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))
  }

  def formulaAxis(implicit xpathEvaluator: XPathEvaluator): ConceptRelationshipNodes.FormulaAxis = {
    val stringResultOption =
      conceptRelationshipNode.formulaAxisValueOrExprOption.
        map(valueOrExpr => StringValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))

    stringResultOption.map(v => ConceptRelationshipNodes.FormulaAxis.fromString(v)).
      getOrElse(ConceptRelationshipNodes.FormulaAxis.DescendantOrSelfAxis)
  }

  def generations(implicit xpathEvaluator: XPathEvaluator): Int = {
    val resultAsBigDecimalOption =
      conceptRelationshipNode.generationsValueOrExprOption.
        map(valueOrExpr => BigDecimalValueOrExprEvaluator.evaluate(valueOrExpr)(xpathEvaluator))

    resultAsBigDecimalOption.map(_.toInt).getOrElse(0)
  }
}

object ConceptRelationshipNodeData {

  /**
   * Finds all concepts referred to by the given concept relationship node in the given taxonomy.
   */
  def findAllConceptsInConceptRelationshipNode(
    conceptRelationshipNode: ConceptRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val conceptRelationNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)
    val axis = conceptRelationNodeData.formulaAxis(xpathEvaluator)

    val rawRelationshipSources: immutable.IndexedSeq[EName] =
      conceptRelationNodeData.relationshipSources(xpathEvaluator)

    val linkroleOption: Option[String] = conceptRelationNodeData.linkroleOption(xpathEvaluator)

    val arcrole: String = conceptRelationNodeData.arcrole(xpathEvaluator)

    val linknameOption: Option[EName] = conceptRelationNodeData.linknameOption(xpathEvaluator)

    val arcnameOption: Option[EName] = conceptRelationNodeData.arcnameOption(xpathEvaluator)

    val doResolveXfiRoot = rawRelationshipSources.isEmpty || rawRelationshipSources.contains(ENames.XfiRootEName)

    val effectiveRelationshipSources: immutable.IndexedSeq[EName] = {
      if (doResolveXfiRoot) {
        val resolvedXfiRoot = resolveXfiRoot(linkroleOption, arcrole, linknameOption, arcnameOption, taxo).toIndexedSeq

        (resolvedXfiRoot ++ rawRelationshipSources.filterNot(Set(ENames.XfiRootEName))).sortBy(_.toString)
      } else {
        rawRelationshipSources.toIndexedSeq.sortBy(_.toString)
      }
    }

    val includeSelf: Boolean = axis.includesSelf

    // Number of generations (optional), from the perspective of finding the descendant-or-self
    // (or only descendant) concepts. So 1 for the child axis, for example. 0 becomes None.
    val effectiveGenerationsOption: Option[Int] = {
      val rawValue = conceptRelationNodeData.generations(xpathEvaluator)
      val optionalRawResult = if (rawValue == 0) None else Some(rawValue)
      val resultOption = if (axis.includesChildrenButNotDeeperDescendants) Some(1) else optionalRawResult
      resultOption
    }

    val conceptTreeWalkSpecs: immutable.IndexedSeq[ConceptTreeWalkSpec] =
      effectiveRelationshipSources map { startConcept =>
        new ConceptTreeWalkSpec(startConcept, includeSelf, effectiveGenerationsOption, linkroleOption, arcrole, linknameOption, arcnameOption)
      }

    // Find the descendant-or-self or descendant concepts for the given number of generations, if applicable.
    val conceptsExcludingSiblings: Set[EName] =
      if (axis.includesDescendantsOrChildren) {
        (conceptTreeWalkSpecs.map(spec => filterDescendantOrSelfConcepts(spec, taxo))).flatten.toSet
      } else {
        if (axis.includesSelf) effectiveRelationshipSources.toSet else Set.empty
      }

    val includedSiblings: Set[EName] = {
      if (axis.includesSiblings) {
        rawRelationshipSources.filterNot(Set(ENames.XfiRootEName)).
          flatMap(c => findAllSiblings(c, linkroleOption, arcrole, linknameOption, arcnameOption, taxo)).toSet
      } else {
        Set()
      }
    }

    conceptsExcludingSiblings.union(includedSiblings)
  }

  /**
   * Returns the descendant-or-self concepts in a concept tree walk according to the parameter specification of the walk.
   * If the start concept must not be included, the tree walk finds descendant concepts instead of descendant-or-self concepts.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def filterDescendantOrSelfConcepts(
    treeWalkSpec: ConceptTreeWalkSpec,
    taxo: BasicTableTaxonomy): Set[EName] = {

    val relationshipPaths =
      taxo.underlyingTaxonomy.filterLongestOutgoingNonCyclicInterConceptRelationshipPaths(
        treeWalkSpec.startConcept,
        classTag[InterConceptRelationship]) { path =>
          path.isConsecutiveRelationshipPath &&
            treeWalkSpec.generationsOption.forall(gen => path.relationships.size <= gen) &&
            treeWalkSpec.linkroleOption.forall(lr => path.relationships.head.elr == lr) &&
            (path.firstRelationship.arcrole == treeWalkSpec.arcrole) &&
            treeWalkSpec.linknameOption.forall(ln => path.relationships.map(_.baseSetKey.extLinkEName).forall(_ == ln)) &&
            treeWalkSpec.arcnameOption.forall(an => path.relationships.map(_.baseSetKey.arcEName).forall(_ == an))
        }

    val resultIncludingStartConcept = relationshipPaths.flatMap(_.concepts).toSet
    if (treeWalkSpec.includeSelf) resultIncludingStartConcept else resultIncludingStartConcept.diff(Set(treeWalkSpec.startConcept))
  }

  private def resolveXfiRoot(
    linkroleOption: Option[String],
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): Set[EName] = {

    val relationships =
      taxo.underlyingTaxonomy filterInterConceptRelationships { rel =>
        relationshipMatchesCriteria(rel, linkroleOption, arcrole, linknameOption, arcnameOption)
      }

    val sources = relationships.map(_.sourceConceptEName).toSet
    val targets = relationships.map(_.targetConceptEName).toSet
    sources.diff(targets)
  }

  private def findAllSiblings(
    concept: EName,
    linkroleOption: Option[String],
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): Set[EName] = {

    val incomingRelationships =
      taxo.underlyingTaxonomy.filterIncomingInterConceptRelationshipsOfType(concept, classTag[InterConceptRelationship]) { rel =>
        relationshipMatchesCriteria(rel, linkroleOption, arcrole, linknameOption, arcnameOption)
      }

    if (incomingRelationships.nonEmpty) {
      (incomingRelationships flatMap { rel =>
        taxo.underlyingTaxonomy.filterOutgoingInterConceptRelationshipsOfType(rel.sourceConceptEName, classTag[InterConceptRelationship]) { r =>
          relationshipMatchesCriteria(r, linkroleOption, arcrole, linknameOption, arcnameOption)
        }
      }).map(_.targetConceptEName).toSet.diff(Set(concept))
    } else {
      // Find roots
      resolveXfiRoot(linkroleOption, arcrole, linknameOption, arcnameOption, taxo).diff(Set(concept))
    }
  }

  private def relationshipMatchesCriteria(
    relationship: InterConceptRelationship,
    linkroleOption: Option[String],
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName]): Boolean = {

    linkroleOption.forall(lr => relationship.elr == lr) &&
      (relationship.arcrole == arcrole) &&
      linknameOption.forall(ln => relationship.baseSetKey.extLinkEName == ln) &&
      arcnameOption.forall(an => relationship.baseSetKey.arcEName == an)
  }

  /**
   * Specification of a concept tree walk starting with one concept (which must not be xfi:root but a real concept).
   * The tree walk finds descendant-or-self concepts in the network, but if the start concept must
   * be excluded the tree walk only finds descendant concepts.
   *
   * The optional generations cannot contain 0. None means unbounded.
   */
  final case class ConceptTreeWalkSpec(
    val startConcept: EName,
    val includeSelf: Boolean,
    val generationsOption: Option[Int],
    val linkroleOption: Option[String],
    val arcrole: String,
    val linknameOption: Option[EName],
    val arcnameOption: Option[EName])
}
