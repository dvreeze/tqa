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
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

import scala.collection.immutable.ArraySeq
import scala.collection.immutable.ListMap
import scala.io.Codec
import scala.util.Using
import scala.util.chaining._

import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm.TaxonomyFactoryFromRemoteZip
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.base.taxonomybuilder.jvm.TaxonomyBuilderSupport
import eu.cdevreeze.tqa.common.schema.SubstitutionGroupMap
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import eu.cdevreeze.yaidom.simple
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy bootstrapping utility for the console programs.
 *
 * @author Chris de Vreeze
 */
private[console] object ConsoleUtil {

  private val logger = Logger.getGlobal

  def createTaxonomyBuilder(taxonomyPackage: ZipFile, useSaxon: Boolean, lenient: Boolean): TaxonomyBuilder = {
    // Exploiting parallelism, in DTS collection and relationship creation.

    val processor = new Processor(false)

    val rawTaxonomyBuilder: TaxonomyBuilder =
      if (useSaxon) {
        TaxonomyBuilderSupport.forTaxonomyPackage(taxonomyPackage, processor)
      } else {
        TaxonomyBuilderSupport.forTaxonomyPackageUsingIndexedDocuments(taxonomyPackage, processor)
      }

    if (lenient) {
      rawTaxonomyBuilder.withRelationshipFactory(DefaultParallelRelationshipFactory.LenientInstance)
    } else {
      rawTaxonomyBuilder
    }
  }

  def createTaxonomyFromZipFile(
      entryPointUris: Set[URI],
      taxonomyPackage: ZipFile,
      useSaxon: Boolean,
      lenient: Boolean): BasicTaxonomy = {
    val taxonomyBuilder: TaxonomyBuilder = createTaxonomyBuilder(taxonomyPackage, useSaxon, lenient)
    taxonomyBuilder.build(entryPointUris)
  }

  def createTaxonomyFromZipStreams(
      entryPointUris: Set[URI],
      getTaxonomyPackageStream: () => ZipInputStream,
      useSaxon: Boolean,
      lenient: Boolean): BasicTaxonomy = {
    val relationshipFactory: RelationshipFactory =
      if (lenient) DefaultParallelRelationshipFactory.LenientInstance
      else DefaultParallelRelationshipFactory.StrictInstance

    def transformDocument(doc: SaxonDocument): BackingDocumentApi = {
      if (useSaxon) doc
      else indexed.Document.from(simple.Document(doc.uriOption, simple.Elem.from(doc.documentElement)))
    }

    val taxoFactory: TaxonomyFactoryFromRemoteZip =
      TaxonomyFactoryFromRemoteZip(getTaxonomyPackageStream)
        .withTransformDocument(transformDocument)
        .withRelationshipFactory(relationshipFactory)

    val xmlByteArrays: ListMap[String, ArraySeq[Byte]] = taxoFactory.readAllXmlDocuments()

    logger.info(s"Number of (not yet parsed) documents in the ZIP (including those in META-INF): ${xmlByteArrays.size}")

    taxoFactory.build(entryPointUris, xmlByteArrays)
  }

  def createTaxonomy(
      entryPointUris: Set[URI],
      taxonomyPackageFile: File,
      useZipStreams: Boolean,
      useSaxon: Boolean,
      lenient: Boolean): BasicTaxonomy = {

    if (useZipStreams) {
      // Assuming UTF-8 for ZIP entry names
      def createStream(): ZipInputStream =
        new ZipInputStream(new FileInputStream(taxonomyPackageFile), Codec.UTF8.charSet)

      createTaxonomyFromZipStreams(entryPointUris, createStream, useSaxon, lenient)
    } else {
      // Assuming UTF-8 for ZIP entry names
      Using.resource(new ZipFile(taxonomyPackageFile)) { zipFile =>
        createTaxonomyFromZipFile(entryPointUris, zipFile, useSaxon, lenient)
      }
    }
  }
}
