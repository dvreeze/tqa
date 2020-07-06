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
import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable
import scala.collection.compat._

/**
 * Program that shows concept labels in a given taxonomy. One use case is finding translations for certain
 * terms in the given taxonomy, provided there are labels in these desired languages.
 *
 * @author Chris de Vreeze
 */
object ShowLabels {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowLabels <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipFile = new ZipFile(new File(args(0)).ensuring(_.isFile))

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val taxoBuilder: TaxonomyBuilder = ConsoleUtil.createTaxonomyBuilder(zipFile, useSaxon, lenient)
    val basicTaxo = taxoBuilder.build(entryPointUris)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    val conceptLabelRelationships = basicTaxo.findAllConceptLabelRelationships

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")
    logger.info(s"The taxonomy has ${conceptLabelRelationships.size} concept-label relationships")

    val conceptLabelsByConcept: Map[EName, immutable.IndexedSeq[ConceptLabelRelationship]] =
      conceptLabelRelationships.groupBy(_.sourceConceptEName)

    val concepts = conceptLabelsByConcept.keySet.toIndexedSeq.sortBy(_.toString)

    concepts.foreach { concept =>
      val conceptLabels = conceptLabelsByConcept.getOrElse(concept, Vector())

      println(s"Concept: $concept")

      conceptLabels.foreach { rel =>
        println(s"\tLabel (role: ${rel.resourceRole}, language: ${rel.language}): ${rel.resource.text}")
      }
    }

    logger.info("Ready")

    zipFile.close() // Not robust
  }
}
