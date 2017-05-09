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

import eu.cdevreeze.tqa.Aspect
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.dom.PeriodType
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.ConceptAspect
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.ExplicitDimensionAspect
import eu.cdevreeze.tqa.extension.table.dom.PeriodAspect
import eu.cdevreeze.tqa.extension.table.dom.RuleNode
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.relationship.DefinitionNodeSubtreeRelationship
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.queryapi.InterConceptRelationshipPath
import eu.cdevreeze.tqa.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.richtaxonomy.ConceptAspectData
import eu.cdevreeze.tqa.richtaxonomy.ConceptRelationshipNodeData
import eu.cdevreeze.tqa.richtaxonomy.DimensionRelationshipNodeData
import eu.cdevreeze.tqa.richtaxonomy.ExplicitDimensionAspectData
import eu.cdevreeze.tqa.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon
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
      val xpathEvaluator = makeXPathEvaluator(table, rootDir)

      showTableAspectInfo(table, tableTaxo, xpathEvaluator)
    }
  }

  def showTableAspectInfo(table: Table, tableTaxo: BasicTableTaxonomy, xpathEvaluator: XPathEvaluator): Unit = {
    val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")
    val elr = table.elr

    logger.info(s"Analysing table with ID $tableId (${table.underlyingResource.docUri})")

    val aspectsInTable = findAllTouchedAspects(table, tableTaxo)

    logger.info(s"Aspects in table with ID $tableId\n\t ${aspectsInTable.toSeq.sortBy(_.toString).mkString(", ")}")

    Aspect.RequiredItemAspects.diff(Set(Aspect.EntityIdentifierAspect)).diff(aspectsInTable) foreach { aspect =>
      logger.warning(s"Table with ID $tableId misses required item aspect (other than EntityIdentifierAspect): $aspect")
    }

    val concepts = findAllConceptsInTable(table, tableTaxo)(xpathEvaluator)
    val sortedConcepts = concepts.toIndexedSeq.sortBy(_.toString)

    val itemDecls = tableTaxo.underlyingTaxonomy.filterItemDeclarations(decl => concepts.contains(decl.targetEName))
    val expectedPeriodTypes: Set[PeriodType] = itemDecls.map(_.periodType).toSet

    val periodTypes: Set[PeriodType] = findAllPeriodTypesInTable(table, tableTaxo)(xpathEvaluator)

    logger.info(s"Found period type(s) for the concepts 'touched' by the table with ID $tableId: ${periodTypes.mkString(", ")}")
    logger.info(s"Expected period type(s) for the concepts 'touched' by the table with ID $tableId: ${expectedPeriodTypes.mkString(", ")}")

    if (periodTypes.diff(expectedPeriodTypes).nonEmpty) {
      logger.warning(s"Table with ID $tableId has rule nodes for period type(s) ${periodTypes.mkString(", ")}, but expected only ${expectedPeriodTypes.mkString(", ")}")
    }

    if (expectedPeriodTypes.diff(periodTypes).nonEmpty) {
      logger.warning(s"Table with ID $tableId misses rule nodes for period type(s) ${expectedPeriodTypes.diff(periodTypes).mkString(", ")}")
    }

    sortedConcepts foreach { concept =>
      val conceptDecl = tableTaxo.underlyingTaxonomy.getConceptDeclaration(concept)
      val periodTypeOption = conceptDecl.globalElementDeclaration.periodTypeOption

      val rawMsg = s"Table with ID $tableId in ELR $elr (${table.underlyingResource.docUri}).\n\tConcept in table: $concept"
      val msg = if (periodTypeOption.isEmpty) rawMsg else rawMsg + s" (periodType: ${periodTypeOption.get})"
      logger.info(msg)
    }
  }

  /**
   * Returns all concepts "touched" by the table. These are the concepts in expanded concept relationship nodes,
   * and the concepts in rule nodes.
   */
  def findAllConceptsInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {
    val nodes = findAllNodes(table, taxo)

    val concepts: Seq[EName] =
      nodes flatMap {
        case node: ConceptRelationshipNode =>
          findAllConceptsInConceptRelationshipNode(node, taxo)(xpathEvaluator)
        case node: RuleNode =>
          findAllConceptsInRuleNode(node, taxo)(xpathEvaluator)
        case node =>
          Seq.empty
      }

    concepts.toSet
  }

  def findAllConceptsInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val conceptAspectElems =
      ruleNode.allAspects collect { case conceptAspect: ConceptAspect => conceptAspect }

    val conceptAspectDataObjects = conceptAspectElems.map(e => new ConceptAspectData(e))

    val concepts = conceptAspectDataObjects.flatMap(ca => ca.qnameValueOption)
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

  def findAllExplicitDimensionsInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val explicitDimensionAspectElems =
      ruleNode.allAspects collect { case dimensionAspect: ExplicitDimensionAspect => dimensionAspect }

    explicitDimensionAspectElems.map(_.dimension).toSet
  }

  def findAllExplicitDimensionMembersInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Map[EName, EName] = {

    val explicitDimensionAspectElems =
      ruleNode.allAspects collect { case dimensionAspect: ExplicitDimensionAspect => dimensionAspect }

    val explicitDimensionAspectDataObjects = explicitDimensionAspectElems.map(e => new ExplicitDimensionAspectData(e))

    val dimensionMembers =
      explicitDimensionAspectDataObjects.flatMap(da => da.memberOption(xpathEvaluator).map(m => da.dimensionName -> m))
    dimensionMembers.toMap
  }

  /**
   * Returns the descendant-or-self members in a dimension-member tree walk according to the parameter specification of the walk.
   * If the start member must not be included, the tree walk finds descendant members instead of descendant-or-self members.
   *
   * It is assumed yet not checked that the tree walk corresponds to the given dimension.
   *
   * TODO Mind networks of relationships (that is, after resolution of prohibition/overriding).
   */
  def filterDescendantOrSelfMembers(
    treeWalkSpec: DimensionMemberTreeWalkSpec,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val relationshipPaths =
      taxo.underlyingTaxonomy.filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(
        treeWalkSpec.startMember) { path =>
          path.isElrValid &&
            treeWalkSpec.generationsOption.forall(gen => path.relationships.size <= gen) &&
            treeWalkSpec.linkroleOption.forall(lr => treeWalkSpec.elrToCheck(path) == lr)
        }

    val resultIncludingStartMember = relationshipPaths.flatMap(_.concepts).toSet
    if (treeWalkSpec.includeSelf) resultIncludingStartMember else resultIncludingStartMember.diff(Set(treeWalkSpec.startMember))
  }

  /**
   * Returns all period types in rule nodes in the table.
   */
  def findAllPeriodTypesInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[PeriodType] = {
    val nodes = findAllNodes(table, taxo)

    val periodTypes: Seq[PeriodType] =
      nodes flatMap {
        case node: RuleNode =>
          val periodAspectElems = node.allAspects collect { case e: PeriodAspect => e }
          periodAspectElems.flatMap(_.periodElems).map(_.periodType)
        case node =>
          Seq.empty
      }

    periodTypes.toSet
  }

  def findAllTouchedAspects(table: Table, taxo: BasicTableTaxonomy): Set[Aspect] = {
    val nodes = findAllNodes(table, taxo)
    nodes.flatMap(node => findAllAspects(node)).toSet
  }

  def findAllNodes(table: Table, taxo: BasicTableTaxonomy): immutable.IndexedSeq[DefinitionNode] = {
    // Naive implementation

    val elr = table.elr

    val breakdownTreeRels =
      for {
        tableBreakdownRel <- taxo.filterOutgoingTableBreakdownRelationships(table)(_.elr == elr)
        breakdownTreeRel <- taxo.filterOutgoingBreakdownTreeRelationships(tableBreakdownRel.breakdown)(_.elr == elr)
      } yield {
        breakdownTreeRel
      }

    val nodeSubtreeRels =
      for {
        breakdownTreeRel <- breakdownTreeRels
        directNodeSubtreeRel <- taxo.filterOutgoingDefinitionNodeSubtreeRelationships(breakdownTreeRel.definitionNode)(_.elr == elr)
        nodeSubtreeRel <- findAllDefinitionNodeSubtreeRelationships(directNodeSubtreeRel, taxo)
      } yield {
        nodeSubtreeRel
      }

    val nodes = (breakdownTreeRels.map(_.definitionNode) ++ nodeSubtreeRels.map(_.toNode)).distinct
    nodes
  }

  def findAllDefinitionNodeSubtreeRelationships(
    relationship: DefinitionNodeSubtreeRelationship,
    taxo: BasicTableTaxonomy): immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] = {

    val nextRelationships =
      taxo.filterOutgoingDefinitionNodeSubtreeRelationships(relationship.toNode)(_.elr == relationship.elr)

    // Recursive calls
    relationship +: (nextRelationships.flatMap(rel => findAllDefinitionNodeSubtreeRelationships(rel, taxo)))
  }

  def findAllAspects(node: DefinitionNode): Set[Aspect] = {
    node match {
      case node: RuleNode =>
        node.allAspects.map(_.aspect).toSet
      case node: ConceptRelationshipNode =>
        Set(Aspect.ConceptAspect)
      case node: DimensionRelationshipNode =>
        Set(Aspect.DimensionAspect(node.dimensionName))
      case node: AspectNode =>
        Set(node.aspectSpec.aspect)
    }
  }

  def findAllDimensionAspects(taxo: TaxonomyApi): Set[Aspect.DimensionAspect] = {
    // Are relationships and ELRs important here?
    taxo.findAllDimensionDeclarations.map(dimDecl => Aspect.DimensionAspect(dimDecl.dimensionEName)).toSet
  }

  private def makeXPathEvaluator(table: Table, localRootDir: File): XPathEvaluator = {
    makeXPathEvaluator(
      table.underlyingResource.docUri,
      table.underlyingResource.scope ++ JaxpXPathEvaluatorUsingSaxon.MinimalScope,
      localRootDir)
  }

  private def makeXPathEvaluator(docUri: URI, scope: Scope, localRootDir: File): XPathEvaluator = {
    val factory =
      JaxpXPathEvaluatorFactoryUsingSaxon.newInstance(processor.getUnderlyingConfiguration)

    // TODO Register variables and functions

    JaxpXPathEvaluatorUsingSaxon.newInstance(
      factory,
      docUri,
      scope,
      new SimpleUriResolver(u => uriToLocalUri(u, localRootDir)))
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

  /**
   * Specification of a dimension member tree walk for some explicit dimension, starting with one member.
   * The tree walk finds descendant-or-self members in the network, but if the start member must
   * be excluded the tree walk only finds descendant members.
   *
   * The optional generations cannot contain 0. None means unbounded.
   *
   * The start relationship must be either a dimension-domain relationship for the given explicit dimension,
   * or a domain-member relationship in the DRS of such a dimension-domain relationship. Otherwise this
   * data is corrupt.
   */
  final case class DimensionMemberTreeWalkSpec(
      val explicitDimension: EName,
      val startRelationship: DomainAwareRelationship,
      val includeSelf: Boolean,
      val generationsOption: Option[Int],
      val linkroleOption: Option[String]) {

    def startMember: EName = startRelationship.targetConceptEName

    def elrToCheck(path: InterConceptRelationshipPath[DomainMemberRelationship]): String = {
      startRelationship match {
        case rel: DimensionDomainRelationship => rel.elr
        case rel                              => path.relationships.head.elr
      }
    }
  }
}
