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
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.relationship.StandardInterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.tqa.extension.table.common.ConceptRelationshipNodes
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator
import eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.xpath.XPathEvaluator

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

  import ConceptRelationshipNodePath._

  /**
   * Finds all "result paths" according to the given concept relationship node in the given taxonomy.
   * All `relationshipTargetConcepts` in the result paths belong to the resolution of the concept relationship node.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def findAllResultPaths(
    conceptRelationshipNode: ConceptRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val relationshipSources: immutable.IndexedSeq[EName] =
      findAllRelationshipSources(conceptRelationshipNode, taxo)(xpathEvaluator)

    val resultPaths: immutable.IndexedSeq[ConceptRelationshipNodePath] =
      findAllResultPaths(relationshipSources, conceptRelationshipNode, taxo)

    resultPaths
  }

  /**
   * Finds all relationship sources according to the given concept relationship node in the given taxonomy.
   *
   * The relationship sources are returned in the order in which they occur in the relationship node, replacing xfi:root concepts
   * by root concepts of the specified network. The latter root concepts are sorted by expanded name (first on namespace,
   * then on local name). Note that it is possible that the relationship sources contain duplicate concept names.
   */
  def findAllRelationshipSources(
    conceptRelationshipNode: ConceptRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[EName] = {

    // Start with reading the needed information in the concept relationship node.

    val conceptRelationNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    // Get the raw relationship sources from the relationship node, in the order of occurrence.

    val rawRelationshipSources: immutable.IndexedSeq[EName] =
      conceptRelationNodeData.relationshipSources(xpathEvaluator).distinct

    val linkrole: String =
      conceptRelationNodeData.linkroleOption(xpathEvaluator)
        .getOrElse(BaseSetKey.StandardElr)

    val arcrole: String = conceptRelationNodeData.arcrole(xpathEvaluator)

    val linknameOption: Option[EName] = conceptRelationNodeData.linknameOption(xpathEvaluator)

    val arcnameOption: Option[EName] = conceptRelationNodeData.arcnameOption(xpathEvaluator)

    // Next find all "real" relationship sources.

    if (rawRelationshipSources.isEmpty) {
      resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)
    } else {
      rawRelationshipSources
        .flatMap { sourceConcept =>
          if (sourceConcept == ENames.XfiRootEName) {
            resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)
          } else {
            immutable.IndexedSeq(sourceConcept)
          }
        }
    }
  }

  /**
   * Finds all "result paths" according to the given concept relationship node in the given taxonomy.
   * All `relationshipTargetConcepts` in the result paths belong to the resolution of the concept relationship node.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def findAllResultPaths(
    relationshipSources: immutable.IndexedSeq[EName],
    conceptRelationshipNode: ConceptRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    // Start with reading the needed information in the concept relationship node.

    val conceptRelationNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    val linkrole: String =
      conceptRelationNodeData.linkroleOption(xpathEvaluator)
        .getOrElse(BaseSetKey.StandardElr)

    val arcrole: String = conceptRelationNodeData.arcrole(xpathEvaluator)

    val linknameOption: Option[EName] = conceptRelationNodeData.linknameOption(xpathEvaluator)

    val arcnameOption: Option[EName] = conceptRelationNodeData.arcnameOption(xpathEvaluator)

    val axis = conceptRelationNodeData.formulaAxis(xpathEvaluator)

    // Number of generations (optional), from the perspective of finding the descendant-or-self
    // (or only descendant) concepts. So 1 for the child axis, for example. 0 becomes None.

    val effectiveGenerationsOption: Option[Int] = {
      val rawValue = conceptRelationNodeData.generations(xpathEvaluator)
      val optionalRawResult = if (rawValue == 0) None else Some(rawValue)
      val resultOption = if (axis.includesChildrenButNotDeeperDescendants) Some(1) else optionalRawResult
      resultOption
    }

    // Next resolve the concept relationship node

    relationshipSources
      .flatMap { sourceConcept =>
        axis match {
          case ConceptRelationshipNodes.FormulaAxis.DescendantAxis =>
            findAllDescendants(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
          case ConceptRelationshipNodes.FormulaAxis.DescendantOrSelfAxis =>
            findAllDescendantsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
          case ConceptRelationshipNodes.FormulaAxis.ChildAxis =>
            findAllDescendants(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, Some(1), taxo)
          case ConceptRelationshipNodes.FormulaAxis.ChildOrSelfAxis =>
            findAllDescendantsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, Some(1), taxo)
          case ConceptRelationshipNodes.FormulaAxis.SiblingAxis =>
            findAllSiblings(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
          case ConceptRelationshipNodes.FormulaAxis.SiblingOrSelfAxis =>
            findAllSiblingsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
          case ConceptRelationshipNodes.FormulaAxis.SiblingOrDescendantAxis =>
            findAllSiblingsOrDescendants(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
          case ConceptRelationshipNodes.FormulaAxis.SiblingOrDescendantOrSelfAxis =>
            findAllSiblingsOrDescendantsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
        }
      }
  }

  private def findAllDescendants(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    effectiveGenerationsOption: Option[Int],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val paths = taxo.underlyingTaxonomy
      .filterOutgoingConsecutiveInterConceptRelationshipPaths(sourceConcept, classTag[StandardInterConceptRelationship]) { path =>
        relationshipMatchesCriteria(path.firstRelationship, linkrole, arcrole, linknameOption, arcnameOption) &&
          effectiveGenerationsOption.forall(gen => path.relationships.size <= gen) &&
          hasMoreMatchingDescendantsIfEndingInAbstract(path, effectiveGenerationsOption, taxo)
      }

    paths.map(p => DescendantPath(p))
  }

  private def findAllDescendantsOrSelf(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    effectiveGenerationsOption: Option[Int],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val paths = taxo.underlyingTaxonomy
      .filterOutgoingConsecutiveInterConceptRelationshipPaths(sourceConcept, classTag[StandardInterConceptRelationship]) { path =>
        relationshipMatchesCriteria(path.firstRelationship, linkrole, arcrole, linknameOption, arcnameOption) &&
          effectiveGenerationsOption.forall(gen => path.relationships.size <= gen) &&
          hasMoreMatchingDescendantsIfEndingInAbstract(path, effectiveGenerationsOption, taxo)
      }

    if (paths.isEmpty) {
      immutable.IndexedSeq(SingleConceptPath(sourceConcept))
    } else {
      paths.map(p => DescendantOrSelfPath(p))
    }
  }

  private def findAllSiblings(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val roots = resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)

    val concepts =
      if (roots.contains(sourceConcept)) {
        roots.filterNot(Set(sourceConcept))
      } else {
        findAllNonRootSiblings(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
      }

    concepts.map(concept => SingleConceptPath(concept))
  }

  private def findAllSiblingsOrSelf(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val roots = resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)

    val concepts =
      if (roots.contains(sourceConcept)) {
        roots
      } else {
        findAllNonRootSiblingsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
      }

    concepts.map(concept => SingleConceptPath(concept))
  }

  private def findAllSiblingsOrDescendants(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    effectiveGenerationsOption: Option[Int],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val roots = resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)

    val siblingsOrSelf =
      if (roots.contains(sourceConcept)) {
        roots
      } else {
        findAllNonRootSiblingsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
      }

    siblingsOrSelf.flatMap { concept =>
      if (concept == sourceConcept) {
        findAllDescendants(concept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
      } else {
        immutable.IndexedSeq(SingleConceptPath(concept))
      }
    }
  }

  private def findAllSiblingsOrDescendantsOrSelf(
    sourceConcept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    effectiveGenerationsOption: Option[Int],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[ConceptRelationshipNodePath] = {

    val roots = resolveXfiRoot(linkrole, arcrole, linknameOption, arcnameOption, taxo)

    val siblingsOrSelf =
      if (roots.contains(sourceConcept)) {
        roots
      } else {
        findAllNonRootSiblingsOrSelf(sourceConcept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
      }

    siblingsOrSelf.flatMap { concept =>
      if (concept == sourceConcept) {
        findAllDescendantsOrSelf(concept, linkrole, arcrole, linknameOption, arcnameOption, effectiveGenerationsOption, taxo)
      } else {
        immutable.IndexedSeq(SingleConceptPath(concept))
      }
    }
  }

  private def resolveXfiRoot(
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[EName] = {

    val relationships =
      taxo.underlyingTaxonomy
        .filterInterConceptRelationships { rel =>
          relationshipMatchesCriteria(rel, linkrole, arcrole, linknameOption, arcnameOption)
        }

    val sources = relationships.map(_.sourceConceptEName).toSet
    val targets = relationships.map(_.targetConceptEName).toSet
    val rootConcepts = sources.diff(targets)

    sortConcepts(rootConcepts)
  }

  private def relationshipMatchesCriteria(
    relationship: StandardInterConceptRelationship,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName]): Boolean = {

    (relationship.elr == linkrole) &&
      (relationship.arcrole == arcrole) &&
      linknameOption.forall(ln => relationship.baseSetKey.extLinkEName == ln) &&
      arcnameOption.forall(an => relationship.baseSetKey.arcEName == an)
  }

  private def findAllNonRootSiblingsOrSelf(
    concept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[EName] = {

    val incomingRelationships =
      taxo.underlyingTaxonomy.filterIncomingInterConceptRelationships(concept) { rel =>
        relationshipMatchesCriteria(rel, linkrole, arcrole, linknameOption, arcnameOption)
      }

    val targetConcepts: immutable.IndexedSeq[EName] =
      incomingRelationships
        .flatMap { rel =>
          taxo.underlyingTaxonomy.filterOutgoingInterConceptRelationships(rel.sourceConceptEName) { r =>
            relationshipMatchesCriteria(r, linkrole, arcrole, linknameOption, arcnameOption)
          }
        }
        .sortBy(_.order)
        .map(_.targetConceptEName)

    targetConcepts
  }

  private def findAllNonRootSiblings(
    concept: EName,
    linkrole: String,
    arcrole: String,
    linknameOption: Option[EName],
    arcnameOption: Option[EName],
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[EName] = {

    findAllNonRootSiblingsOrSelf(concept, linkrole, arcrole, linknameOption, arcnameOption, taxo)
      .filterNot(Set(concept))
  }

  private def sortConcepts(concepts: Set[EName]): immutable.IndexedSeq[EName] = {
    concepts.toIndexedSeq
      .sortBy(concept => (concept.namespaceUriOption.getOrElse(""), concept.localPart))
  }

  private def hasMoreMatchingDescendantsIfEndingInAbstract(
    path: InterConceptRelationshipPath[StandardInterConceptRelationship],
    effectiveGenerationsOption: Option[Int],
    taxo: BasicTableTaxonomy): Boolean = {

    val endsInAbstract = taxo.underlyingTaxonomy.findConceptDeclaration(path.targetConcept).filter(_.isAbstract).nonEmpty

    if (endsInAbstract) {
      val followingPathsWithConcreteConcepts =
        taxo.underlyingTaxonomy.filterOutgoingConsecutiveInterConceptRelationshipPaths(path.targetConcept, classTag[StandardInterConceptRelationship]) { p =>
          path.lastRelationship.isFollowedBy(p.firstRelationship) &&
            effectiveGenerationsOption.forall(gen => path.relationships.size + p.relationships.size <= gen) &&
            p.relationships.map(_.targetConceptEName).exists(c => taxo.underlyingTaxonomy.findConceptDeclaration(c).map(_.isConcrete).getOrElse(false))
        }

      followingPathsWithConcreteConcepts.nonEmpty
    } else {
      true
    }
  }
}
