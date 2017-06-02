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
import eu.cdevreeze.tqa.backingelem.CachingDocumentBuilder
import eu.cdevreeze.tqa.backingelem.UriConverters
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.dom.NonStandardResource
import eu.cdevreeze.tqa.dom.PeriodType
import eu.cdevreeze.tqa.dom.XLinkArc
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
import eu.cdevreeze.tqa.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.relationship.RelationshipFactory
import eu.cdevreeze.tqa.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon
import eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon
import eu.cdevreeze.tqa.xpath.jaxp.saxon.SimpleUriResolver
import eu.cdevreeze.tqa.xpathaware.extension.table.ConceptAspectData
import eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData
import eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData
import eu.cdevreeze.tqa.xpathaware.extension.table.ExplicitDimensionAspectData
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import javax.xml.xpath.XPathVariableResolver
import net.sf.saxon.s9api.Processor

/**
 * Table-aware taxonomy parser and analyser, showing modeled aspects in the tables of the table-aware taxonomy.
 * Potential problems with the tables are logged as warnings. The following potential table problems are found:
 * <ul>
 * <li>Missing (period) aspect in the rule nodes of a table</li>
 * <li>Given the concepts "implied" by the table (in relationship nodes and rule nodes) and the dimensional trees
 * of those concepts, the dimension members occurring in the table do not occur in the DTS.</li>
 * <li>Given the concepts "implied" by the table (in relationship nodes and rule nodes) and their period types,
 * the period type mentioned in the table does not occur as period type of one of the "implied" concepts.</li>
 * </ul>
 *
 * TODO Mind table filters.
 *
 * @author Chris de Vreeze
 */
object ShowAspectsInTables {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowAspectsInTables <taxo root dir> <entrypoint URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointUris = args.drop(1).map(u => URI.create(u)).toSet

    val cacheSize = System.getProperty("cacheSize", "5000").toInt

    val processor = new Processor(false)

    val documentBuilder = getDocumentBuilder(rootDir, processor)
    val cachingDocumentBuilder =
      new CachingDocumentBuilder(CachingDocumentBuilder.createCache(documentBuilder, cacheSize))

    showAspectsInTables(entrypointUris, rootDir, cachingDocumentBuilder, processor)
  }

  def showAspectsInTables(
    entrypointUrisOfDts: Set[URI],
    rootDir: File,
    documentBuilder: CachingDocumentBuilder[_],
    processor: Processor): Unit = {

    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val documentCollector = DefaultDtsCollector(entrypointUrisOfDts)

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    def filterArc(arc: XLinkArc): Boolean = {
      if (lenient) RelationshipFactory.AnyArcHavingArcrole(arc) else RelationshipFactory.AnyArc(arc)
    }

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(documentBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory).
        withArcFilter(filterArc _)

    logger.info(s"Starting building the DTS with entrypoint(s) ${entrypointUrisOfDts.mkString(", ")}")

    val basicTaxo = taxoBuilder.build()

    logger.info(s"Found ${basicTaxo.relationships.size} relationships in the DTS")
    logger.info(s"Starting building the table-aware taxonomy with entrypoint(s) ${entrypointUrisOfDts.mkString(", ")}")

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    logger.info(s"Found ${tableTaxo.tableRelationships.size} 'table relationships' in the DTS")

    val tables = tableTaxo.tableResources collect { case table: Table => table }

    logger.info(s"The taxonomy has ${tables.size} tables")

    val xpathEvaluatorFactory =
      JaxpXPathEvaluatorFactoryUsingSaxon.newInstance(processor.getUnderlyingConfiguration)

    val parameterElems: immutable.IndexedSeq[NonStandardResource] =
      tableTaxo.underlyingTaxonomy.rootElems.flatMap(_.filterElemsOfType(classTag[NonStandardResource])(_.resolvedName == ENames.VariableParameterEName))

    // Disregarding arcs to parameters
    // Assuming all parameters to occur in one and the same document
    val scope = parameterElems.headOption.map(_.scope).getOrElse(Scope.Empty) ++ JaxpXPathEvaluatorUsingSaxon.MinimalScope
    logger.info(s"Creating XPath evaluator with scope $scope")

    val xpathEvaluator =
      JaxpXPathEvaluatorUsingSaxon.newInstance(
        xpathEvaluatorFactory,
        parameterElems.headOption.map(_.docUri).getOrElse(URI.create("")),
        scope,
        new SimpleUriResolver(u => UriConverters.uriToLocalUri(u, rootDir)))

    val paramValues: Map[EName, AnyRef] =
      (parameterElems map { e =>
        logger.info(s"Compiling and evaluating XPath expression in document ${e.docUri}. XPath expression:\n\t${e.attribute(ENames.SelectEName)}")

        val expr = xpathEvaluator.toXPathExpression(e.attribute(ENames.SelectEName))
        val v = xpathEvaluator.evaluateAsString(expr, None)
        (e.attributeAsResolvedQName(ENames.NameEName) -> v)
      }).toMap

    xpathEvaluatorFactory.underlyingEvaluatorFactory.setXPathVariableResolver(new XPathVariableResolver {

      def resolveVariable(variableName: javax.xml.namespace.QName): AnyRef = {
        val variableEName = EName.fromJavaQName(variableName)

        logger.fine(s"Resolving variable $variableEName")
        paramValues.getOrElse(variableEName, sys.error(s"Missing variable $variableEName"))
      }
    })

    logger.info("Computing concept has-hypercube inheritance map ...")

    val conceptHasHypercubeMap: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] =
      tableTaxo.underlyingTaxonomy.computeHasHypercubeInheritanceOrSelf

    tables foreach { table =>
      val xpathEvaluator = makeXPathEvaluator(xpathEvaluatorFactory, table, scope, rootDir)

      logger.info(s"Created XPathEvaluator. Entrypoint(s): ${entrypointUrisOfDts.mkString(", ")}")

      showTableAspectInfo(table, tableTaxo, conceptHasHypercubeMap, xpathEvaluator)
    }

    logger.info("Ready")
  }

  def showTableAspectInfo(
    table: Table,
    tableTaxo: BasicTableTaxonomy,
    conceptHasHypercubeMap: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]],
    xpathEvaluator: XPathEvaluator): Unit = {

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

    val itemDecls = concepts.toIndexedSeq.flatMap(c => tableTaxo.underlyingTaxonomy.findItemDeclaration(c))
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

    showDimensionalTableAspectInfo(table, concepts, tableTaxo, conceptHasHypercubeMap, xpathEvaluator)
  }

  def showDimensionalTableAspectInfo(
    table: Table,
    concepts: Set[EName],
    tableTaxo: BasicTableTaxonomy,
    conceptHasHypercubeMap: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]],
    xpathEvaluator: XPathEvaluator): Unit = {

    val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")

    val dimensionMembersInTable: Map[EName, Set[EName]] =
      findAllExplicitDimensionMembersInTable(table, tableTaxo)(xpathEvaluator)

    val nonExplicitDimensionENames: Set[EName] =
      dimensionMembersInTable.keySet.filter(en => tableTaxo.underlyingTaxonomy.findExplicitDimensionDeclaration(en).isEmpty)

    for (ename <- nonExplicitDimensionENames.toSeq.sortBy(_.toString)) {
      logger.warning(s"Table $tableId erroneously claims the following to be an explicit dimension: $ename")
    }

    val hasHypercubes = conceptHasHypercubeMap.filterKeys(concepts).values.flatten.toIndexedSeq

    // Usable and non-usable members in the taxonomy
    val allDimMemPairs: immutable.IndexedSeq[(EName, EName)] =
      hasHypercubes flatMap { hh =>
        tableTaxo.underlyingTaxonomy.findAllDimensionMembers(hh).toSeq.flatMap(dimMems => dimMems._2.map(mem => (dimMems._1 -> mem)))
      }

    val allDimensionMembers: Map[EName, Set[EName]] = allDimMemPairs.groupBy(_._1).mapValues(_.map(_._2).toSet)

    val unexpectedDimensionMembersInTable =
      dimensionMembersInTable map {
        case (dim, members) =>
          val expectedMembers = allDimensionMembers.getOrElse(dim, Set())
          val unexpectedMembers = members.filterNot(expectedMembers)
          (dim -> unexpectedMembers)
      } filter (_._2.nonEmpty)

    if (unexpectedDimensionMembersInTable.nonEmpty) {
      unexpectedDimensionMembersInTable foreach {
        case (dim, members) =>
          logger.warning(s"In table $tableId, some unexpected members for dimension $dim are: ${members.toSeq.sortBy(_.toString).take(15).mkString(", ")}")
      }
    } else {
      logger.info(s"In table $tableId, all dimension members implied by the table are potentially indeed members for concepts implied by the table")
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
          ConceptRelationshipNodeData.findAllConceptsInConceptRelationshipNode(node, taxo)(xpathEvaluator)
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

  /**
   * Returns all dimension members "touched" by the table. These are the dimension members in expanded dimension relationship nodes,
   * and the dimension members in rule nodes. Not only usable members are returned per specified dimension, but all the ones found.
   */
  def findAllExplicitDimensionMembersInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Map[EName, Set[EName]] = {
    val nodes = findAllNodes(table, taxo)

    val dimMemPairs: Seq[(EName, EName)] =
      nodes flatMap {
        case node: DimensionRelationshipNode =>
          DimensionRelationshipNodeData.findAllMembersInDimensionRelationshipNode(node, taxo)(xpathEvaluator).toSeq.
            map(mem => (node.dimensionName -> mem))
        case node: RuleNode =>
          findAllExplicitDimensionMembersInRuleNode(node, taxo)(xpathEvaluator).toSeq
        case node =>
          Seq.empty
      }

    dimMemPairs.groupBy(_._1).mapValues(_.map(_._2).toSet)
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

    val explicitDimensionAspectDataObjects =
      explicitDimensionAspectElems.map(e => new ExplicitDimensionAspectData(e))

    val dimensionMembers =
      explicitDimensionAspectDataObjects.flatMap(da => da.memberOption(xpathEvaluator).map(m => da.dimensionName -> m))
    dimensionMembers.toMap
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

  private def makeXPathEvaluator(factory: JaxpXPathEvaluatorFactoryUsingSaxon, table: Table, startScope: Scope, localRootDir: File): XPathEvaluator = {
    JaxpXPathEvaluatorUsingSaxon.newInstance(
      factory,
      table.underlyingResource.docUri,
      startScope ++ table.underlyingResource.scope ++ JaxpXPathEvaluatorUsingSaxon.MinimalScope,
      new SimpleUriResolver(u => UriConverters.uriToLocalUri(u, localRootDir)))
  }

  private def getDocumentBuilder(rootDir: File, processor: Processor): SaxonDocumentBuilder = {
    new SaxonDocumentBuilder(processor.newDocumentBuilder(), UriConverters.uriToLocalUri(_, rootDir))
  }
}
