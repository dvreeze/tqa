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

import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.backingelem.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.extension.table.relationship.TableRelationship
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Table-aware taxonomy parser and analyser, showing some statistics about the table-aware taxonomy.
 *
 * @author Chris de Vreeze
 */
object AnalyseTableTaxonomy {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: AnalyseTableTaxonomy <taxo root dir> <entrypoint URI 1> ...")
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

    val tableRelationships = tableTaxo.tableRelationships

    logger.info(s"The taxonomy has ${tableRelationships.size} table relationships")

    val tableRelationshipGroups: Map[String, immutable.IndexedSeq[TableRelationship]] =
      tableRelationships.groupBy(_.getClass.getSimpleName)

    logger.info(s"Table relationship group sizes (topmost 15): ${tableRelationshipGroups.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(15).mkString(", ")}")

    val sortedTableRelationshipGroups = tableRelationshipGroups.toIndexedSeq.sortBy(_._2.size).reverse

    sortedTableRelationshipGroups foreach {
      case (relationshipName, relationships) =>
        val tableRelationshipsByUri: Map[URI, immutable.IndexedSeq[TableRelationship]] = tableRelationships.groupBy(_.docUri)

        val uris = tableRelationshipsByUri.keySet.toSeq.sortBy(_.toString)

        uris foreach { uri =>
          val currentRelationships = tableRelationshipsByUri.getOrElse(uri, Vector())
          val elrs = currentRelationships.map(_.elr).distinct.sorted
          val arcroles = currentRelationships.map(_.arcrole).distinct.sorted

          logger.info(s"Found ${currentRelationships.size} ${relationshipName}s in doc '${uri}'. ELRs: ${elrs.mkString(", ")}. Arcroles: ${arcroles.mkString(", ")}.")
        }
    }
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
}
