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

import eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Program that shows concept labels in a given taxonomy. One use case is finding translations for certain
 * terms in the given taxonomy, provided there are labels in these desired languages.
 *
 * @author Chris de Vreeze
 */
object ShowLabels {

  private val logger = Logger.getGlobal

  /**
   * The optional parent path as relative URI, if the taxonomy is in a ZIP file but not at the root.
   */
  private val parentPathOption: Option[URI] =
    Option(System.getProperty("parentPath")).map(p => URI.create(p).ensuring(!_.isAbsolute))

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowLabels <taxo root dir or ZIP file> <entry point URI 1> ...")
    val rootDirOrZipFile = new File(args(0))

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val basicTaxo = buildTaxonomy(rootDirOrZipFile, parentPathOption, entryPointUris, useSaxon)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    val conceptLabelRelationships = basicTaxo.findAllConceptLabelRelationships

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")
    logger.info(s"The taxonomy has ${conceptLabelRelationships.size} concept-label relationships")

    val conceptLabelsByConcept: Map[EName, immutable.IndexedSeq[ConceptLabelRelationship]] =
      conceptLabelRelationships.groupBy(_.sourceConceptEName)

    val concepts = conceptLabelsByConcept.keySet.toIndexedSeq.sortBy(_.toString)

    concepts foreach { concept =>
      val conceptLabels = conceptLabelsByConcept.getOrElse(concept, Vector())

      println(s"Concept: $concept")

      conceptLabels foreach { rel =>
        println(s"\tLabel (role: ${rel.resourceRole}, language: ${rel.language}): ${rel.resource.text}")
      }
    }

    logger.info("Ready")
  }

  private def buildTaxonomy(rootDirOrZipFile: File, parentPathOption: Option[URI], entryPointUris: Set[URI], useSaxon: Boolean): BasicTaxonomy = {
    val documentBuilder = getDocumentBuilder(useSaxon, rootDirOrZipFile, parentPathOption)
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

  private def getDocumentBuilder(useSaxon: Boolean, rootDirOrZipFile: File, parentPathOption: Option[URI]): DocumentBuilder = {
    val uriResolver =
      if (rootDirOrZipFile.isDirectory) {
        UriResolvers.fromLocalMirrorRootDirectory(rootDirOrZipFile)
      } else {
        UriResolvers.forZipFileContainingLocalMirror(new ZipFile(rootDirOrZipFile), parentPathOption)
      }

    if (useSaxon) {
      val processor = new Processor(false)

      SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
    } else {
      IndexedDocumentBuilder(DocumentParserUsingStax.newInstance(), uriResolver)
    }
  }
}
