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

import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder

import scala.collection.immutable

/**
 * Taxonomy parser and analyser, showing counts of used elements in the taxonomy.
 *
 * @author Chris de Vreeze
 */
object ShowUsedElements {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowUsedElements <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipFile = new ZipFile(new File(args(0)).ensuring(_.isFile))

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet

    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val taxoBuilder: TaxonomyBuilder = ConsoleUtil.createTaxonomyBuilder(zipFile, useSaxon)
    val basicTaxo = taxoBuilder.build(entryPointUris)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    val allElems = rootElems.flatMap(_.findAllElemsOrSelf)

    logger.info(s"The taxonomy has ${allElems.size} taxonomy elements")

    val allElemsGroupedByClass: Map[String, immutable.IndexedSeq[TaxonomyElem]] =
      allElems.groupBy(_.getClass.getSimpleName)

    for {
      (className, elemGroup) <- allElemsGroupedByClass.toSeq.sortBy(_._2.size).reverse
    } {
      val sortedENames = elemGroup.map(_.resolvedName).distinct.sortBy(_.toString)

      logger.info(s"Element $className. Count: ${elemGroup.size}. Element names: ${sortedENames.mkString(", ")}")
    }

    zipFile.close() // Not robust
  }
}
