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

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.Aspect
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.backingelem.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.AspectSpec
import eu.cdevreeze.tqa.extension.table.dom.ConceptAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.EntityIdentifierAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.PeriodAspectSpec
import eu.cdevreeze.tqa.extension.table.dom.RuleNode
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.dom.UnitAspectSpec
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
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

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowAspectsInTables <taxo root dir> <entrypoint URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointUris = args.drop(1).map(u => URI.create(u)).toSet

    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val documentBuilder = getDocumentBuilder(useSaxon, rootDir)
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

    val aspectsInTaxonomy = findAllAspects(tableTaxo.underlyingTaxonomy)

    aspectsInTaxonomy.toSeq.sortBy(_.toString) foreach { aspect =>
      logger.info(s"Aspect in taxonomy: $aspect")
    }

    tables foreach { table =>
      val tableId = table.underlyingResource.attributeOption(ENames.IdEName).getOrElse("<no ID>")
      val elr = table.elr
      val aspects = findAllAspects(table, tableTaxo)

      logger.info(s"Table with ID $tableId in ELR $elr (${table.underlyingResource.docUri}).\n\tAspects: ${aspects.toSeq.sortBy(_.toString).mkString(", ")}")
    }
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

  private def getDocumentBuilder(useSaxon: Boolean, rootDir: File): DocumentBuilder = {
    if (useSaxon) {
      val processor = new Processor(false)

      new SaxonDocumentBuilder(processor.newDocumentBuilder(), uriToLocalUri(_, rootDir))
    } else {
      new IndexedDocumentBuilder(DocumentParserUsingStax.newInstance(), uriToLocalUri(_, rootDir))
    }
  }

  // TODO Location? Language?
}
