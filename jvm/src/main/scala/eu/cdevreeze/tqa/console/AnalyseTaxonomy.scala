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

import eu.cdevreeze.tqa.base.relationship.Relationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy

/**
 * Taxonomy parser and analyser, showing some statistics about the taxonomy.
 *
 * @author Chris de Vreeze
 */
object AnalyseTaxonomy {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: AnalyseTaxonomy <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipInputFile: File = new File(args(0)).ensuring(_.isFile)

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "true").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean
    val useZipStreams = System.getProperty("useZipStreams", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val basicTaxo: BasicTaxonomy = ConsoleUtil
      .createTaxonomy(entryPointUris, zipInputFile, useZipStreams, useSaxon, lenient)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")

    val relationshipGroups: Map[String, IndexedSeq[Relationship]] =
      basicTaxo.relationships.groupBy(_.getClass.getSimpleName)

    // scalastyle:off magic.number
    logger.info(
      s"Relationship group sizes (topmost 15): ${relationshipGroups.view.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(15).mkString(", ")}")

    val sortedRelationshipGroups = relationshipGroups.toIndexedSeq.sortBy(_._2.size).reverse

    sortedRelationshipGroups.foreach {
      case (relationshipName, relationships) =>
        val relationshipsByUri: Map[URI, IndexedSeq[Relationship]] = relationships.groupBy(_.docUri)

        val uris = relationshipsByUri.keySet.toSeq.sortBy(_.toString)

        uris.foreach { uri =>
          val currentRelationships = relationshipsByUri.getOrElse(uri, Vector())
          val elrs = currentRelationships.map(_.elr).distinct.sorted
          val arcroles = currentRelationships.map(_.arcrole).distinct.sorted

          logger.info(s"Found ${currentRelationships.size} ${relationshipName}s in doc '${uri}'. ELRs: ${elrs.mkString(
            ", ")}. Arcroles: ${arcroles.mkString(", ")}.")
        }
    }
  }
}
