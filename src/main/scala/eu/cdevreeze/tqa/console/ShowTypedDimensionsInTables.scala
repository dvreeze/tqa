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

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.backingelem.CachingDocumentBuilder
import eu.cdevreeze.tqa.backingelem.UriConverters
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.dom.NonStandardResource
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.ConceptAspect
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.ExplicitDimensionAspect
import eu.cdevreeze.tqa.extension.table.dom.RuleNode
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.dom.TypedDimensionAspect
import eu.cdevreeze.tqa.extension.table.relationship.DefinitionNodeSubtreeRelationship
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
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
 * Table-aware taxonomy parser and analyser, showing typed dimensions in the tables of the table-aware taxonomy.
 * Overlapping combinations of typed dimension and (concept and explicit dimension) aspect values across tables are logged as warnings.
 *
 * TODO Mind table filters.
 *
 * @author Chris de Vreeze
 */
object ShowTypedDimensionsInTables {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowTypedDimensionsInTables <taxo root dir> <entrypoint URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointUris = args.drop(1).map(u => URI.create(u)).toSet

    val cacheSize = System.getProperty("cacheSize", "5000").toInt

    val processor = new Processor(false)

    val documentBuilder = getDocumentBuilder(rootDir, processor)
    val cachingDocumentBuilder =
      new CachingDocumentBuilder(CachingDocumentBuilder.createCache(documentBuilder, cacheSize))

    showTypedDimensionUsageInTables(entrypointUris, rootDir, cachingDocumentBuilder, processor)
  }

  def showTypedDimensionUsageInTables(
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

    val typedDimensionUsagesInTables: immutable.IndexedSeq[TypedDimensionUsageInTable] = {
      tables flatMap { table =>
        val xpathEvaluator = makeXPathEvaluator(xpathEvaluatorFactory, table, scope, rootDir)

        val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")
        logger.info(s"Created XPathEvaluator for table $tableId. Entrypoint(s): ${entrypointUrisOfDts.mkString(", ")}")

        findAllTypedDimensionUsagesInTable(table, tableTaxo, conceptHasHypercubeMap, xpathEvaluator).toIndexedSeq.
          map(typedDimUsage => TypedDimensionUsageInTable(table.key, typedDimUsage))
      }
    }

    val typedDimensionUsageToTableKeyMap: Map[TypedDimensionUsage, Set[XmlFragmentKey]] =
      typedDimensionUsagesInTables.groupBy(_.typedDimensionUsage).mapValues(grp => grp.map(_.tableKey).toSet)

    typedDimensionUsageToTableKeyMap.filter(_._2.size >= 2) foreach {
      case (TypedDimensionUsage(typedDim, concept, explicitDimMembers), tableKeys) =>
        logger.warning(
          s"Typed dimension $typedDim used with concept $concept and " +
            s"explicit dimension members ${explicitDimMembers.mkString("[", ", ", "]")} occurs in more than 1 table. " +
            s"Table linkbases:\n\t${tableKeys.map(_.docUri).toSeq.sortBy(_.toString).mkString("\n\t")}")
    }

    logger.info("Ready")
  }

  def findAllTypedDimensionUsagesInTable(
    table: Table,
    tableTaxo: BasicTableTaxonomy,
    conceptHasHypercubeMap: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]],
    xpathEvaluator: XPathEvaluator): Set[TypedDimensionUsage] = {

    val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")

    logger.info(s"Collecting typed dimension usage for table with ID $tableId (${table.underlyingResource.docUri})")

    val conceptsInTable: Set[EName] = findAllConceptsInTable(table, tableTaxo)(xpathEvaluator)

    // First filter on used concepts

    val hasHypercubeMapForTable: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] =
      conceptHasHypercubeMap.filterKeys(conceptsInTable)

    val explicitDimensionMembersInTable = findAllExplicitDimensionMembersInTable(table, tableTaxo)(xpathEvaluator)
    val explicitDimensionsInTable = explicitDimensionMembersInTable.keySet

    val typedDimensionsInTable = findAllTypedDimensionsInTable(table, tableTaxo)

    val typedDimensionUsages: immutable.IndexedSeq[TypedDimensionUsage] =
      hasHypercubeMapForTable.toIndexedSeq flatMap {
        case (concept, hasHypercubes) =>
          val dimensionsOfConcept: Set[EName] =
            hasHypercubes.flatMap(hh => tableTaxo.underlyingTaxonomy.findAllConsecutiveHypercubeDimensionRelationships(hh)).map(_.dimension).toSet

          val typedDimensionsOfConceptInTable: Set[EName] = typedDimensionsInTable.filter(dimensionsOfConcept)

          val explicitDimensionMembersOfConcept: Set[(EName, EName)] =
            hasHypercubes.flatMap(hh => tableTaxo.underlyingTaxonomy.findAllDimensionMembers(hh).toSeq.flatMap(kv => kv._2.map(m => (kv._1 -> m)))).toSet

          val explicitDimensionMembersOfConceptInTable: Map[EName, Set[EName]] =
            explicitDimensionMembersOfConcept filter {
              case (dim, mem) =>
                explicitDimensionsInTable.contains(dim) &&
                  explicitDimensionMembersInTable.getOrElse(dim, Set()).contains(mem)
            } groupBy (_._1) mapValues (grp => grp.map(_._2))

          typedDimensionsOfConceptInTable.toSeq flatMap { typedDim =>
            val dims = explicitDimensionMembersOfConceptInTable.keySet

            val dimMemCombis: immutable.IndexedSeq[Map[EName, EName]] = ???

            dimMemCombis.map(dimMemCombi => TypedDimensionUsage(typedDim, concept, dimMemCombi))
          }
      }

    typedDimensionUsages.toSet
  }

  /**
   * Returns all typed dimensions "touched" by the table. These are the typed dimensions in rule nodes and aspect nodes.
   */
  def findAllTypedDimensionsInTable(table: Table, taxo: BasicTableTaxonomy): Set[EName] = {
    val nodes = findAllNodes(table, taxo)

    val dims: Seq[EName] =
      nodes flatMap {
        case node: RuleNode =>
          findAllTypedDimensionsInRuleNode(node, taxo).toSeq
        case node: AspectNode =>
          findAllTypedDimensionsInAspectNode(node, taxo).toSeq
        case node =>
          Seq.empty
      }

    dims.toSet
  }

  def findAllTypedDimensionsInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy): Set[EName] = {

    val typedDimensionAspectElems =
      ruleNode.allAspects collect { case dimensionAspect: TypedDimensionAspect => dimensionAspect }

    typedDimensionAspectElems.map(_.dimension).toSet
  }

  def findAllTypedDimensionsInAspectNode(
    aspectNode: AspectNode,
    taxo: BasicTableTaxonomy): Set[EName] = {

    val typedDimensions =
      aspectNode.aspectSpec match {
        case das: DimensionAspectSpec =>
          Set(das.dimension).filter(dim => taxo.underlyingTaxonomy.findTypedDimensionDeclaration(dim).nonEmpty)
        case as =>
          Set()
      }

    typedDimensions.toSet
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
    taxo: BasicTableTaxonomy): Set[EName] = {

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

  def findAllConceptsInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {

    val conceptAspectElems =
      ruleNode.allAspects collect { case conceptAspect: ConceptAspect => conceptAspect }

    val conceptAspectDataObjects = conceptAspectElems.map(e => new ConceptAspectData(e))

    val concepts = conceptAspectDataObjects.flatMap(ca => ca.qnameValueOption)
    concepts.toSet
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

  private def getKey(hh: HasHypercubeRelationship): HasHypercubeKey = {
    HasHypercubeKey(hh.primary, hh.hypercube, hh.elr, hh.effectiveTargetRole)
  }

  private final case class HasHypercubeKey(primary: EName, hypercube: EName, elr: String, targetElr: String)

  final case class TypedDimensionUsage(typedDim: EName, concept: EName, explicitDimMembers: Map[EName, EName])

  final case class TypedDimensionUsageInTable(tableKey: XmlFragmentKey, typedDimensionUsage: TypedDimensionUsage)
}
