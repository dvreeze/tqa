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

import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.relationship.DefaultRelationshipsFactory
import eu.cdevreeze.tqa.relationship.Relationship
import eu.cdevreeze.tqa.relationship.RelationshipsFactory
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

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

    val docParser = DocumentParserUsingStax.newInstance()

    val taxoDocs = taxoFiles.map(f => docParser.parse(f).withUriOption(Some(fileToUri(f, rootDir))))

    logger.info(s"Found ${taxoDocs.size} taxonomy documents")

    val taxoRootElems = taxoDocs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    logger.info(s"Found ${taxoRootElems.size} taxonomy root elements")

    val taxo = Taxonomy.build(taxoRootElems)

    logger.info(s"Built taxonomy with ${taxo.rootElems.size} taxonomy root elements")

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    logger.info(s"The taxonomy has ${relationships.size} relationships")

    val relationshipGroups: Map[String, immutable.IndexedSeq[Relationship]] =
      relationships.groupBy(_.getClass.getSimpleName)

    logger.info(s"Relationship group sizes (topmost 10): ${relationshipGroups.mapValues(_.size).toSeq.sortBy(_._2).reverse.take(10).mkString(", ")}")
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
}
