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
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.Relationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy parser and analyser, showing some statistics about the taxonomy.
 *
 * @author Chris de Vreeze
 */
object AnalyseTaxonomy {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: AnalyseTaxonomy <taxo root dir or ZIP file> <entry point URI 1> ...")
    val rootDirOrZipFile = new File(args(0))

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val basicTaxo = buildTaxonomy(rootDirOrZipFile, entryPointUris, useSaxon)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")

    val relationshipGroups: Map[String, immutable.IndexedSeq[Relationship]] =
      basicTaxo.relationships.groupBy(_.getClass.getSimpleName)

    // scalastyle:off magic.number
    logger.info(
      s"Relationship group sizes (topmost 15): ${relationshipGroups.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(15).mkString(", ")}")

    val sortedRelationshipGroups = relationshipGroups.toIndexedSeq.sortBy(_._2.size).reverse

    sortedRelationshipGroups foreach {
      case (relationshipName, relationships) =>
        val relationshipsByUri: Map[URI, immutable.IndexedSeq[Relationship]] = relationships.groupBy(_.docUri)

        val uris = relationshipsByUri.keySet.toSeq.sortBy(_.toString)

        uris foreach { uri =>
          val currentRelationships = relationshipsByUri.getOrElse(uri, Vector())
          val elrs = currentRelationships.map(_.elr).distinct.sorted
          val arcroles = currentRelationships.map(_.arcrole).distinct.sorted

          logger.info(
            s"Found ${currentRelationships.size} ${relationshipName}s in doc '${uri}'. ELRs: ${elrs.mkString(", ")}. Arcroles: ${arcroles.mkString(", ")}.")
        }
    }
  }

  private def buildTaxonomy(rootDirOrZipFile: File, entryPointUris: Set[URI], useSaxon: Boolean): BasicTaxonomy = {
    val documentBuilder = getDocumentBuilder(useSaxon, rootDirOrZipFile)
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
    basicTaxo
  }

  private def getDocumentBuilder(useSaxon: Boolean, rootDirOrZipFile: File): DocumentBuilder = {
    val uriResolver =
      if (rootDirOrZipFile.isDirectory) {
        UriResolvers.fromLocalMirrorRootDirectory(rootDirOrZipFile)
      } else {
        UriResolvers.forZipFileContainingLocalMirror(rootDirOrZipFile)
      }

    if (useSaxon) {
      val processor = new Processor(false)

      SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
    } else {
      IndexedDocumentBuilder(DocumentParserUsingStax.newInstance(), uriResolver)
    }
  }
}
