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
import java.io.FileInputStream
import java.net.URI
import java.util.logging.Logger
import java.util.regex.Pattern
import java.util.zip.ZipInputStream

import scala.collection.immutable.ArraySeq
import scala.collection.immutable.ListMap
import scala.io.Codec
import scala.util.chaining._

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyBaseFactoryFromRemoteZip
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyFactoryFromRemoteZip
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import eu.cdevreeze.yaidom.simple

/**
 * Given a taxonomy ZIP file location and one or more entry point URI regular expressions, finds the entry point
 * URIs in the ZIP file that conform to one of the regular expressions.
 *
 * @author Chris de Vreeze
 */
object CollectEntryPointUris {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: CollectEntryPointUris <taxonomy package ZIP file> <entry point URI regex 1> ...")
    val zipInputFile: File = new File(args(0)).ensuring(_.isFile)

    val entryPointUriRegexes = args.drop(1).map(u => Pattern.compile(u)).toSeq
    val useSaxon = System.getProperty("useSaxon", "true").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) matching one of the regexes")

    val taxoFactory: TaxonomyFactoryFromRemoteZip = getTaxonomyFactory(zipInputFile, useSaxon, lenient)
    val basicTaxo: BasicTaxonomy = createTaxonomy(entryPointUriRegexes, taxoFactory)

    val loadedDocUris: Seq[URI] = basicTaxo.taxonomyDocs.map(_.uriOption.get).sortBy(_.toString)

    logger.info(s"Found ${loadedDocUris.size} documents in the combined DTS")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")

    // Finding entry point URIs again
    val entryPointDocUris: Seq[URI] =
      basicTaxo.taxonomyDocs
        .map(_.uriOption.get)
        .filter(u => entryPointUriRegexes.exists(p => p.matcher(u.toString).matches))
        .distinct
        .sortBy(_.toString)

    logger.info(s"Found ${entryPointDocUris.size} entry point documents in the combined DTS")
    entryPointDocUris.foreach(u => logger.info(s"Entry point document: $u"))

    logger.info(s"All entry points: ${entryPointDocUris.mkString(" ")}")
  }

  def createTaxonomy(entryPointUriRegexes: Seq[Pattern], taxoFactory: TaxonomyFactoryFromRemoteZip): BasicTaxonomy = {
    val taxoBaseFactory: TaxonomyBaseFactoryFromRemoteZip = taxoFactory.taxonomyBaseFactory

    val xmlByteArrays: ListMap[String, ArraySeq[Byte]] = taxoBaseFactory.readAllXmlDocuments()

    logger.info(s"Number of (not yet parsed) documents in the ZIP (including those in META-INF): ${xmlByteArrays.size}")

    val catalog: SimpleCatalog = taxoBaseFactory.locateAndParseCatalog(xmlByteArrays)

    val docs: IndexedSeq[SaxonDocument] =
      taxoBaseFactory.parseAllTaxonomyDocuments(xmlByteArrays, catalog).ensuring(_.forall(_.uriOption.nonEmpty))

    val entryPointUris: Set[URI] =
      docs.map(_.uriOption.get).filter(u => entryPointUriRegexes.exists(p => p.matcher(u.toString).matches)).toSet

    // If the catalog is not invertible, it is likely that DTS discovery will fail!
    val dtsUris: Set[URI] = taxoBaseFactory.findDtsUris(entryPointUris, docs)

    val docsInDts: IndexedSeq[BackingDocumentApi] =
      docs.filter(d => dtsUris.contains(d.uriOption.get)).map(taxoBaseFactory.transformDocument)

    val taxonomyBase: TaxonomyBase = docsInDts.map(TaxonomyDocument.build).pipe(TaxonomyBase.build)

    BasicTaxonomy.build(taxonomyBase, SubstitutionGroupMap.Empty, taxoFactory.relationshipFactory, _ => true)
  }

  def getTaxonomyFactory(
      taxonomyPackageFile: File,
      useSaxon: Boolean,
      lenient: Boolean): TaxonomyFactoryFromRemoteZip = {

    // Assuming UTF-8 for ZIP entry names
    def createStream(): ZipInputStream =
      new ZipInputStream(new FileInputStream(taxonomyPackageFile), Codec.UTF8.charSet)

    val relationshipFactory: RelationshipFactory =
      if (lenient) DefaultParallelRelationshipFactory.LenientInstance
      else DefaultParallelRelationshipFactory.StrictInstance

    def transformDocument(doc: SaxonDocument): BackingDocumentApi = {
      if (useSaxon) doc
      else indexed.Document.from(simple.Document(doc.uriOption, simple.Elem.from(doc.documentElement)))
    }

    val taxoFactory: TaxonomyFactoryFromRemoteZip =
      TaxonomyFactoryFromRemoteZip(createStream)
        .withTransformDocument(transformDocument)
        .withRelationshipFactory(relationshipFactory)

    taxoFactory
  }
}
