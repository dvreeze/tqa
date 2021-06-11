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

import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.extension.formula.relationship.FormulaRelationship
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy

/**
 * Formula-aware taxonomy parser and analyser, showing some statistics about the formula-aware taxonomy.
 *
 * @author Chris de Vreeze
 */
object AnalyseFormulaTaxonomy {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: AnalyseFormulaTaxonomy <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipInputFile: File = new File(args(0)).ensuring(_.isFile)

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "true").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean
    val useZipStreams = System.getProperty("useZipStreams", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val basicTaxo: BasicTaxonomy = ConsoleUtil
      .createTaxonomy(entryPointUris, zipInputFile, useZipStreams, useSaxon, lenient)

    logger.info(s"Starting building the formula-aware taxonomy with entry point(s) ${entryPointUris.mkString(", ")}")

    val formulaTaxo = BasicFormulaTaxonomy.build(basicTaxo)

    val formulaRelationships = formulaTaxo.formulaRelationships

    logger.info(s"The taxonomy has ${formulaRelationships.size} formula relationships")

    val formulaRelationshipGroups: Map[String, IndexedSeq[FormulaRelationship]] =
      formulaRelationships.groupBy(_.getClass.getSimpleName)

    // scalastyle:off magic.number
    logger.info(
      s"Formula relationship group sizes (topmost 15): ${formulaRelationshipGroups.view.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(15).mkString(", ")}")

    val sortedFormulaRelationshipGroups = formulaRelationshipGroups.toIndexedSeq.sortBy(_._2.size).reverse

    sortedFormulaRelationshipGroups.foreach {
      case (relationshipName, relationships) =>
        val relationshipsByUri: Map[URI, IndexedSeq[FormulaRelationship]] = relationships.groupBy(_.docUri)

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
