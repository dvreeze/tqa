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

import scala.collection.immutable

import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder

/**
 * Taxonomy parser and analyser, showing the URIs of the loaded documents in the taxonomy. This program
 * can be used to see what is in a DTS, and what is not, which in turn can be used to create "minimal"
 * taxonomy packages.
 *
 * @author Chris de Vreeze
 */
object ShowLoadedDocUris {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowLoadedDocUris <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipFile = new ZipFile(new File(args(0)).ensuring(_.isFile))

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val taxoBuilder: TaxonomyBuilder = ConsoleUtil.createTaxonomyBuilder(zipFile, useSaxon, lenient)
    val basicTaxo = taxoBuilder.build(entryPointUris)

    val loadedDocUris: Seq[URI] = basicTaxo.taxonomyDocs.map(_.uriOption.get).sortBy(_.toString)

    logger.info(s"Found ${loadedDocUris.size} documents in the DTS")
    loadedDocUris.foreach(u => logger.info(s"Document: $u"))

    zipFile.close() // Not robust
  }
}
