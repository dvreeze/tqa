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

package eu.cdevreeze.tqa.console

import java.io.File
import java.net.URI
import java.util.logging.Logger

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.Aspect
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.RuleNode
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.richtaxonomy.ConceptRelationshipNodeData
import eu.cdevreeze.tqa.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon
import eu.cdevreeze.tqa.xpath.jaxp.saxon.SimpleUriResolver
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import net.sf.saxon.s9api.Processor

/**
 * Table-aware taxonomy parser and analyser, showing modeled aspects in the tables of the table-aware taxonomy.
 *
 * TODO Mind filters, ELRs, tags (?), and evaluate XPath where needed.
 * TODO Mind aspects that can have only one value and that can therefore be ignored.
 * TODO Are merging and abstract relevant?
 *
 * @author Chris de Vreeze
 */
object ShowAspectsInTables {

  private val logger = Logger.getGlobal

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowAspectsInTables <taxo root dir> <entrypoint URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointUris = args.drop(1).map(u => URI.create(u)).toSet

    val documentBuilder = getDocumentBuilder(rootDir)
    val documentCollector = DefaultDtsCollector(entrypointUris)

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(documentBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    logger.info(s"Starting building the DTS with entrypoint(s) ${entrypointUris.mkString(", ")}")

    val basicTaxo = taxoBuilder.build()

    logger.info(s"Starting building the table-aware taxonomy with entrypoint(s) ${entrypointUris.mkString(", ")}")

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val tables = tableTaxo.tableResources collect { case table: Table => table }

    logger.info(s"The taxonomy has ${tables.size} tables")

    logger.info(s"Created XPathEvaluator")

    tables foreach { table =>
      val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")
      val elr = table.elr

      val xpathEvaluator =
        makeXPathEvaluator(
          table.underlyingResource.docUri,
          table.underlyingResource.scope ++ JaxpXPathEvaluatorUsingSaxon.MinimalScope,
          rootDir)

      val concepts = findAllConceptsInTable(table, tableTaxo)(xpathEvaluator)
      val sortedConcepts = concepts.toIndexedSeq.sortBy(_.toString)

      logger.info(s"Analysing table with ID $tableId")

      sortedConcepts foreach { concept =>
        logger.info(s"Table with ID $tableId in ELR $elr (${table.underlyingResource.docUri}).\n\tConcept in table: $concept}")
      }
    }
  }

  def makeXPathEvaluator(docUri: URI, scope: Scope, localRootDir: File): XPathEvaluator = {
    JaxpXPathEvaluatorUsingSaxon.createXPathEvaluator(
      processor.getUnderlyingConfiguration,
      docUri,
      scope,
      new SimpleUriResolver(u => uriToLocalUri(u, localRootDir)))
  }

  /**
   * Returns all concepts "touched" by the table. These are the concepts in expanded concept relationship nodes,
   * and the concepts in rule nodes.
   */
  def findAllConceptsInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {
    val nodes = findAllNodes(table, taxo)

    // TODO Rule nodes etc.

    val concepts: Seq[EName] =
      nodes flatMap {
        case node: ConceptRelationshipNode =>
          findAllConceptsInConceptRelationshipNode(node, taxo)(xpathEvaluator)
        case node =>
          Seq.empty
      }

    concepts.toSet
  }

  def findAllConceptsInConceptRelationshipNode(
    conceptRelationshipNode: ConceptRelationshipNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val conceptRelationNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)
    val axis = conceptRelationNodeData.formulaAxis(xpathEvaluator)

    val rawRelationshipSources: immutable.IndexedSeq[EName] =
      conceptRelationNodeData.relationshipSources(xpathEvaluator)

    // Resolving xfi:root.
    // TODO
    val effectiveRelationshipSources: immutable.IndexedSeq[EName] = rawRelationshipSources

    val linkroleOption: Option[String] = conceptRelationNodeData.linkroleOption(xpathEvaluator)

    val arcroleOption: Option[String] = conceptRelationNodeData.arcroleOption(xpathEvaluator)

    val linknameOption: Option[String] = conceptRelationNodeData.linknameOption(xpathEvaluator)

    val arcnameOption: Option[String] = conceptRelationNodeData.arcnameOption(xpathEvaluator)

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
        new ConceptTreeWalkSpec(startConcept, includeSelf, effectiveGenerationsOption, linkroleOption, arcroleOption, linknameOption, arcnameOption)
      }

    // Find the descendant-or-self or descendant concepts for the given number of generations, if applicable.
    val conceptsExcludingSiblings: Set[EName] =
      if (axis.includesDescendantsOrChildren) {
        (conceptTreeWalkSpecs.map(spec => filterDescendantOrSelfConcepts(spec, taxo)(xpathEvaluator))).flatten.toSet
      } else {
        if (axis.includesSelf) effectiveRelationshipSources.toSet else Set.empty
      }

    // Include siblings if needed
    // TODO
    val includedSiblings: Set[EName] = Set()

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
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val relationshipPaths =
      taxo.underlyingTaxonomy.filterLongestOutgoingNonCyclicInterConceptRelationshipPaths(
        treeWalkSpec.startConcept,
        classTag[InterConceptRelationship]) { path =>
          path.isElrValid &&
            treeWalkSpec.generationsOption.forall(gen => path.relationships.size <= gen) &&
            treeWalkSpec.linkroleOption.forall(lr => path.relationships.head.elr == lr) &&
            treeWalkSpec.arcroleOption.forall(ar => path.relationships.head.arcrole == ar) &&
            treeWalkSpec.linknameOption.forall(ln => path.relationships.map(_.baseSetKey.extLinkEName).forall(_ == ln)) &&
            treeWalkSpec.arcnameOption.forall(an => path.relationships.map(_.baseSetKey.arcEName).forall(_ == an))
        }

    val resultIncludingStartConcept = relationshipPaths.flatMap(_.concepts).toSet
    if (treeWalkSpec.includeSelf) resultIncludingStartConcept else resultIncludingStartConcept.diff(Set(treeWalkSpec.startConcept))
  }

  def findAllAspects(table: Table, taxo: BasicTableTaxonomy): Set[Aspect] = {
    val nodes = findAllNodes(table, taxo)
    nodes.flatMap(node => findAllAspects(node)).toSet
  }

  def findAllNodes(table: Table, taxo: BasicTableTaxonomy): immutable.IndexedSeq[DefinitionNode] = {
    // Naive implementation

    val breakdownTreeRels =
      for {
        tableBreakdownRel <- taxo.findAllOutgoingTableBreakdownRelationships(table)
        breakdownTreeRel <- taxo.findAllOutgoingBreakdownTreeRelationships(tableBreakdownRel.breakdown)
      } yield {
        breakdownTreeRel
      }

    val nodeSubtreeRels =
      for {
        breakdownTreeRel <- breakdownTreeRels
        nodeSubtreeRel <- taxo.findAllOutgoingDefinitionNodeSubtreeRelationships(breakdownTreeRel.definitionNode)
      } yield {
        nodeSubtreeRel
      }

    val nodes = (breakdownTreeRels.map(_.definitionNode) ++ nodeSubtreeRels.map(_.toNode)).distinct
    nodes
  }

  def findAllAspects(node: DefinitionNode): Set[Aspect] = {
    node match {
      case node: RuleNode =>
        findAllAspectsInRuleNode(node)
      case node: ConceptRelationshipNode =>
        Set(Aspect.ConceptAspect)
      case node: DimensionRelationshipNode =>
        Set(Aspect.DimensionAspect(node.dimensionName))
      case node: AspectNode =>
        Set(node.aspectSpec.aspect)
    }
  }

  def findAllAspects(taxo: TaxonomyApi): Set[Aspect] = {
    val dimensionAspects: Set[Aspect] = findAllDimensionAspects(taxo) collect { case aspect: Aspect => aspect }

    Aspect.WellKnownAspects.union(dimensionAspects)
  }

  def findAllDimensionAspects(taxo: TaxonomyApi): Set[Aspect.DimensionAspect] = {
    // Are relationships and ELRs important here?
    taxo.findAllDimensionDeclarations.map(dimDecl => Aspect.DimensionAspect(dimDecl.dimensionEName)).toSet
  }

  private def findAllAspectsInRuleNode(node: RuleNode): Set[Aspect] = {
    // TODO Tagged aspects. How to deal with tagged aspects?
    node.untaggedAspects.flatMap(e => toAspectOption(e)).toSet
  }

  private def toAspectOption(formulaAspect: tqa.dom.OtherElem): Option[Aspect] = formulaAspect.resolvedName match {
    case ENames.FormulaConceptEName          => Some(Aspect.ConceptAspect)
    case ENames.FormulaEntityIdentifierEName => Some(Aspect.EntityIdentifierAspect)
    case ENames.FormulaPeriodEName           => Some(Aspect.PeriodAspect)
    case ENames.FormulaUnitEName             => Some(Aspect.UnitAspect)
    case ENames.FormulaExplicitDimensionEName =>
      Some(Aspect.DimensionAspect(formulaAspect.attributeAsResolvedQName(ENames.DimensionEName)))
    case ENames.FormulaTypedDimensionEName =>
      Some(Aspect.DimensionAspect(formulaAspect.attributeAsResolvedQName(ENames.DimensionEName)))
    case _ => None
  }

  private def uriToLocalUri(uri: URI, rootDir: File): URI = {
    // Not robust
    val relativePath = uri.getScheme match {
      case "http"  => uri.toString.drop("http://".size)
      case "https" => uri.toString.drop("https://".size)
      case _       => sys.error(s"Unexpected URI $uri")
    }

    val f = new File(rootDir, relativePath.dropWhile(_ == '/'))
    f.toURI
  }

  private def getDocumentBuilder(rootDir: File): DocumentBuilder = {
    new SaxonDocumentBuilder(processor.newDocumentBuilder(), uriToLocalUri(_, rootDir))
  }

  // TODO Location? Language?

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
    val arcroleOption: Option[String],
    val linknameOption: Option[String],
    val arcnameOption: Option[String])
}
