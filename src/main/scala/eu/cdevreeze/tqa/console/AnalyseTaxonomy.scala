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

import eu.cdevreeze.tqa.backingelem.BackingElemBuilder
import eu.cdevreeze.tqa.backingelem.indexed.IndexedBackingElemBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonBackingElemBuilder
import eu.cdevreeze.tqa.dom.TaxonomyRootElem
import eu.cdevreeze.tqa.factory.TaxonomyFactory
import eu.cdevreeze.tqa.factory.TaxonomyRootElemCollector
import eu.cdevreeze.tqa.relationship.DefaultRelationshipsFactory
import eu.cdevreeze.tqa.relationship.Relationship
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
    require(args.size == 1, s"Usage: AnalyseTaxonomy <taxo root dir>")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val backingElemBuilder = getBackingElemBuilder(useSaxon, rootDir)
    val rootElemCollector = getRootElemCollector(rootDir)

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipsFactory =
      if (lenient) DefaultRelationshipsFactory.LenientInstance else DefaultRelationshipsFactory.StrictInstance

    val taxoBuilder =
      TaxonomyFactory.
        withBackingElemBuilder(backingElemBuilder).
        withRootElemCollector(rootElemCollector).
        withRelationshipsFactory(relationshipsFactory)

    val basicTaxo = taxoBuilder.build()

    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")

    val relationshipGroups: Map[String, immutable.IndexedSeq[Relationship]] =
      basicTaxo.relationships.groupBy(_.getClass.getSimpleName)

    logger.info(s"Relationship group sizes (topmost 15): ${relationshipGroups.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(15).mkString(", ")}")

    val sortedRelationshipGroups = relationshipGroups.toIndexedSeq.sortBy(_._2.size).reverse

    sortedRelationshipGroups foreach {
      case (relationshipName, relationships) =>
        val relationshipsByUri: Map[URI, immutable.IndexedSeq[Relationship]] = relationships.groupBy(_.docUri)

        val uris = relationshipsByUri.keySet.toSeq.sortBy(_.toString)

        uris foreach { uri =>
          val currentRelationships = relationshipsByUri.getOrElse(uri, Vector())
          val elrs = currentRelationships.map(_.elr).distinct.sorted
          val arcroles = currentRelationships.map(_.arcrole).distinct.sorted

          logger.info(s"Found ${currentRelationships.size} ${relationshipName}s in doc '${uri}'. ELRs: ${elrs.mkString(", ")}. Arcroles: ${arcroles.mkString(", ")}.")
        }
    }
  }

  private def findNormalFiles(rootDir: File, p: File => Boolean): immutable.IndexedSeq[File] = {
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    rootDir.listFiles.toIndexedSeq flatMap {
      case f: File if f.isFile =>
        immutable.IndexedSeq(f).filter(p)
      case d: File if d.isDirectory =>
        // Recursive call
        findNormalFiles(d, p)
      case f: File =>
        immutable.IndexedSeq()
    }
  }

  private def isTaxoFile(f: File): Boolean = {
    f.isFile && (f.getName.endsWith(".xml") || f.getName.endsWith(".xsd"))
  }

  private def fileToUri(f: File, rootDir: File): URI = {
    // Not robust
    val idx = f.getPath.indexOf(rootDir.getName)
    require(idx >= 0)
    val relevantPath = f.getPath.substring(idx + rootDir.getName.size).dropWhile(_ == '/')
    URI.create(s"http://${relevantPath}")
  }

  private def uriToLocalUri(uri: URI, rootDir: File): URI = {
    // Not robust
    val relativePath = uri.getScheme match {
      case "http"  => uri.toString.drop("http://".size)
      case "https" => uri.toString.drop("https://".size)
      case _       => sys.error(s"Unexpected URI $uri")
    }

    val f = new File(rootDir, relativePath)
    f.toURI
  }

  private def getRootElemCollector(rootDir: File): TaxonomyRootElemCollector = {
    new TaxonomyRootElemCollector {

      def collectRootElems(backingElemBuilder: BackingElemBuilder): immutable.IndexedSeq[TaxonomyRootElem] = {
        val taxoFiles = findNormalFiles(rootDir, isTaxoFile)

        logger.info(s"Found ${taxoFiles.size} taxonomy files")

        val taxoUris = taxoFiles.map(f => fileToUri(f, rootDir))

        val backingElems = taxoUris.map(uri => backingElemBuilder.build(uri))

        logger.info(s"Found ${backingElems.size} taxonomy backing root elements")

        val taxoRootElems = backingElems.flatMap(e => TaxonomyRootElem.buildOptionally(e))

        logger.info(s"Found ${taxoRootElems.size} taxonomy root elements")
        taxoRootElems
      }
    }
  }

  private def getBackingElemBuilder(useSaxon: Boolean, rootDir: File): BackingElemBuilder = {
    if (useSaxon) {
      val processor = new Processor(false)

      new SaxonBackingElemBuilder(processor.newDocumentBuilder(), uriToLocalUri(_, rootDir))
    } else {
      new IndexedBackingElemBuilder(DocumentParserUsingStax.newInstance(), uriToLocalUri(_, rootDir))
    }
  }
}
