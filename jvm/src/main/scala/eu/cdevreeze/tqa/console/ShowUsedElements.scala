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

import eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy parser and analyser, showing counts of used elements in the taxonomy.
 *
 * @author Chris de Vreeze
 */
object ShowUsedElements {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowUsedElements <taxo root dir> <entry point URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet

    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val documentBuilder = getDocumentBuilder(useSaxon, rootDir)
    val documentCollector = DefaultDtsCollector()

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(documentBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

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
  }

  private def getDocumentBuilder(useSaxon: Boolean, rootDir: File): DocumentBuilder = {
    if (useSaxon) {
      val processor = new Processor(false)

      SaxonDocumentBuilder.usingUriConverter(processor.newDocumentBuilder(), UriConverters.uriToLocalUri(_, rootDir))
    } else {
      IndexedDocumentBuilder.usingUriConverter(DocumentParserUsingStax.newInstance(), UriConverters.uriToLocalUri(_, rootDir))
    }
  }
}
