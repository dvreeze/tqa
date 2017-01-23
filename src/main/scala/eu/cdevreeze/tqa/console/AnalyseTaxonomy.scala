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

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.nodeinfo.DomNode
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.relationship.DefaultRelationshipsFactory
import eu.cdevreeze.tqa.relationship.Relationship
import eu.cdevreeze.tqa.relationship.RelationshipsFactory
import eu.cdevreeze.tqa.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import net.sf.saxon.s9api.Processor
import eu.cdevreeze.yaidom.java8.domelem.DomDocument

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

    val taxoFiles = findNormalFiles(rootDir, isTaxoFile)

    logger.info(s"Found ${taxoFiles.size} taxonomy files")

    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val backingRootElems = toBackingElems(taxoFiles, rootDir, useSaxon)

    logger.info(s"Found ${backingRootElems.size} taxonomy backing root elements")

    val taxoRootElems = backingRootElems.map(e => TaxonomyElem.build(e))

    logger.info(s"Found ${taxoRootElems.size} taxonomy root elements")

    val underlyingTaxo = Taxonomy.build(taxoRootElems)

    logger.info(s"Built taxonomy with ${underlyingTaxo.rootElems.size} taxonomy root elements")

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipsFactory =
      if (lenient) DefaultRelationshipsFactory.LenientInstance else DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(underlyingTaxo, RelationshipsFactory.AnyArc)

    val basicTaxo = new BasicTaxonomy(underlyingTaxo, SubstitutionGroupMap.Empty, relationships)

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

  private def toBackingElems(files: immutable.IndexedSeq[File], rootDir: File, useSaxon: Boolean): immutable.IndexedSeq[BackingElemApi] = {
    if (useSaxon) {
      toBackingElemsUsingSaxon(files, rootDir)
    } else {
      toBackingElemsUsingNativeYaidom(files, rootDir)
    }
  }

  private def toBackingElemsUsingSaxon(files: immutable.IndexedSeq[File], rootDir: File): immutable.IndexedSeq[BackingElemApi] = {
    val processor = new Processor(false)

    val docBuilder = processor.newDocumentBuilder()

    files map { f =>
      val node = docBuilder.build(f).getUnderlyingNode
      node.setSystemId(fileToUri(f, rootDir).toString)
      DomNode.wrapDocument(node.getTreeInfo).documentElement
    }
  }

  private def toBackingElemsUsingNativeYaidom(files: immutable.IndexedSeq[File], rootDir: File): immutable.IndexedSeq[BackingElemApi] = {
    val docParser = DocumentParserUsingStax.newInstance()

    val taxoDocs = files.map(f => docParser.parse(f).withUriOption(Some(fileToUri(f, rootDir))))
    taxoDocs.map(d => indexed.Document(d).documentElement)
  }
}
