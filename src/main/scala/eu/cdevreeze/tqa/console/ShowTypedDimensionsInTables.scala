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
import eu.cdevreeze.tqa.extension.formula.dom.ConceptAspect
import eu.cdevreeze.tqa.extension.formula.dom.ExplicitDimensionAspect
import eu.cdevreeze.tqa.extension.formula.dom.TypedDimensionAspect
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
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
 * Table-aware taxonomy parser and analyser, showing typed dimensions and their usage in the tables of the table-aware taxonomy.
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

    logger.info("Computing mappings from has-hypercubes to typed dimensions and to explicit dimension members ...")

    val hasHypercubes = tableTaxo.underlyingTaxonomy.findAllHasHypercubeRelationships

    val hasHypercubeTypedDimMap: Map[HasHypercubeKey, Set[EName]] =
      computeMappingFromHasHypercubeToTypedDimensions(hasHypercubes, tableTaxo.underlyingTaxonomy)

    val hasHypercubeExplicitDimMemberMap: Map[HasHypercubeKey, Map[EName, Set[EName]]] =
      computeMappingFromHasHypercubeToExplicitDimensionMembers(hasHypercubes, tableTaxo.underlyingTaxonomy)

    logger.info("Analysing the tables ...")

    val typedDimensionUsagesInTables: immutable.IndexedSeq[TypedDimensionUsageInTable] = {
      (tables flatMap { table =>
        val xpathEvaluator = makeXPathEvaluator(xpathEvaluatorFactory, table, scope, rootDir)

        val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")
        logger.info(s"Created XPathEvaluator for table $tableId. Entrypoint(s): ${entrypointUrisOfDts.mkString(", ")}")

        findAllTypedDimensionUsagesInTable(
          table,
          tableTaxo,
          conceptHasHypercubeMap,
          hasHypercubeTypedDimMap,
          hasHypercubeExplicitDimMemberMap,
          xpathEvaluator).toIndexedSeq.
          map(typedDimUsage => TypedDimensionUsageInTable(table.key, typedDimUsage))
      }).distinct
    }

    logger.info(s"Ready analysing tables. Now looking for duplicate typed dimension usage across tables. " +
      s"Entrypoint(s): ${entrypointUrisOfDts.mkString(", ")}")

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
    hasHypercubeTypedDimMap: Map[HasHypercubeKey, Set[EName]],
    hasHypercubeExplicitDimMemberMap: Map[HasHypercubeKey, Map[EName, Set[EName]]],
    xpathEvaluator: XPathEvaluator): Set[TypedDimensionUsage] = {

    val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")

    logger.info(s"Collecting typed dimension usage for table with ID $tableId (${table.underlyingResource.docUri})")

    val conceptsInTable: Set[EName] = findAllConceptsInTable(table, tableTaxo)(xpathEvaluator)

    // First filter the mapping from concepts to has-hypercubes on those concepts that are used in the table

    val hasHypercubeMapForTable: Map[EName, immutable.IndexedSeq[HasHypercubeRelationship]] =
      conceptHasHypercubeMap.filterKeys(conceptsInTable)

    // Dimensions (and members) found in the tables, not limited to certain concepts

    val allFoundTypedDimensionsInTable: Set[EName] = findAllTypedDimensionsInTable(table, tableTaxo)

    val allFoundExplicitDimensionMembersInTable: Map[EName, Set[EName]] =
      findAllExplicitDimensionMembersInTable(table, tableTaxo)(xpathEvaluator)

    val typedDimensionUsages: immutable.IndexedSeq[TypedDimensionUsage] =
      hasHypercubeMapForTable.toIndexedSeq flatMap {
        case (concept, hasHypercubes) =>
          val hasHypercubeKeys = hasHypercubes.map(hh => getKey(hh)).toSet

          val typedDimsForConceptInTaxo: Set[EName] =
            hasHypercubeTypedDimMap.filterKeys(hasHypercubeKeys).values.flatten.toSet

          val explicitDimMembersForConceptInTaxo: Map[EName, Set[EName]] =
            combineExplicitDimensionMembers(
              hasHypercubeExplicitDimMemberMap.filterKeys(hasHypercubeKeys).values.toIndexedSeq)

          val typedDimsForConceptInTable: Set[EName] =
            allFoundTypedDimensionsInTable.filter(typedDimsForConceptInTaxo)

          // TODO Warning if allFoundTypedDimensionsInTable.diff(typedDimsForConceptInTaxo).nonEmpty

          val explicitDimMembersForConceptInTable: Map[EName, Set[EName]] =
            (allFoundExplicitDimensionMembersInTable.toSeq map {
              case (dim, members) =>
                val filteredMembers = members.filter(explicitDimMembersForConceptInTaxo.getOrElse(dim, Set()))
                (dim -> filteredMembers)
            }).toMap filter (_._2.nonEmpty)

          val explicitDimsForConceptInTable: immutable.IndexedSeq[Map[EName, EName]] =
            toDimensionMemberMaps(explicitDimMembersForConceptInTable).distinct

          for {
            typedDim <- typedDimsForConceptInTable.toIndexedSeq
            explicitDims <- explicitDimsForConceptInTable
          } yield {
            TypedDimensionUsage(typedDim, concept, explicitDims)
          }
      }

    typedDimensionUsages.toSet
  }

  private def toDimensionMemberMaps(dimMembers: Map[EName, Set[EName]]): immutable.IndexedSeq[Map[EName, EName]] = {
    val dims = dimMembers.keySet

    if (dimMembers.isEmpty) {
      immutable.IndexedSeq()
    } else {
      val dim = dims.head

      if (dims.size == 1) {
        assert(dimMembers.size == 1)
        dimMembers(dim).toIndexedSeq.map(m => Map(dim -> m))
      } else {
        val remainingDims = dims - dim

        // Recursive call
        val remainingDimsResult = toDimensionMemberMaps(dimMembers.filterKeys(remainingDims))

        for {
          remainingDimsRecord <- remainingDimsResult
          mem <- dimMembers(dim)
        } yield {
          remainingDimsRecord + (dim -> mem)
        }
      }
    }
  }

  private def computeMappingFromHasHypercubeToTypedDimensions(
    hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship],
    taxo: TaxonomyApi): Map[HasHypercubeKey, Set[EName]] = {

    require(hasHypercubes.size == hasHypercubes.distinct.size)
    require(hasHypercubes.size == hasHypercubes.map(hh => getKey(hh)).distinct.size)

    (hasHypercubes map { hh =>
      val hypercubeDimensions = taxo.findAllConsecutiveHypercubeDimensionRelationships(hh)
      val dimensions = hypercubeDimensions.map(_.dimension)
      val typedDimensions = dimensions.filter(dim => taxo.findTypedDimensionDeclaration(dim).nonEmpty)
      getKey(hh) -> typedDimensions.toSet
    }).toMap
  }

  private def computeMappingFromHasHypercubeToExplicitDimensionMembers(
    hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship],
    taxo: TaxonomyApi): Map[HasHypercubeKey, Map[EName, Set[EName]]] = {

    require(hasHypercubes.size == hasHypercubes.distinct.size)
    require(hasHypercubes.size == hasHypercubes.map(hh => getKey(hh)).distinct.size)

    (hasHypercubes map { hh =>
      // Both usable and non-usable members!
      getKey(hh) -> taxo.findAllDimensionMembers(hh)
    }).toMap
  }

  private def combineExplicitDimensionMembers(dimensionMemberGroups: immutable.IndexedSeq[Map[EName, Set[EName]]]): Map[EName, Set[EName]] = {
    val dimMems: immutable.IndexedSeq[(EName, EName)] =
      dimensionMemberGroups.toIndexedSeq.flatMap(dimMemMap => dimMemMap.toSeq.flatMap({ case (dim, mems) => mems.map(m => (dim -> m)) }))

    dimMems.groupBy({ case (dim, mem) => dim }).mapValues(grp => grp.map(_._2).toSet)
  }

  /**
   * Returns all typed dimensions "touched" by the table. These are the typed dimensions in rule nodes and aspect nodes.
   */
  private def findAllTypedDimensionsInTable(table: Table, taxo: BasicTableTaxonomy): Set[EName] = {
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

  private def findAllTypedDimensionsInRuleNode(
    ruleNode: RuleNode,
    taxo: BasicTableTaxonomy): Set[EName] = {

    val typedDimensionAspectElems =
      ruleNode.allAspects collect { case dimensionAspect: TypedDimensionAspect => dimensionAspect }

    typedDimensionAspectElems.map(_.dimension).toSet
  }

  private def findAllTypedDimensionsInAspectNode(
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
  private def findAllConceptsInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Set[EName] = {
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

  private def findAllConceptsInRuleNode(
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
  private def findAllExplicitDimensionMembersInTable(table: Table, taxo: BasicTableTaxonomy)(implicit xpathEvaluator: XPathEvaluator): Map[EName, Set[EName]] = {
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

  private def findAllExplicitDimensionMembersInRuleNode(
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

  private def findAllNodes(table: Table, taxo: BasicTableTaxonomy): immutable.IndexedSeq[DefinitionNode] = {
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

  private def findAllDefinitionNodeSubtreeRelationships(
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
    HasHypercubeKey(hh.arc.key, hh.primary, hh.hypercube)
  }

  private final case class HasHypercubeKey(arcKey: XmlFragmentKey, primary: EName, hypercube: EName)

  final case class TypedDimensionUsage(typedDim: EName, concept: EName, explicitDimMembers: Map[EName, EName])

  final case class TypedDimensionUsageInTable(tableKey: XmlFragmentKey, typedDimensionUsage: TypedDimensionUsage)
}
