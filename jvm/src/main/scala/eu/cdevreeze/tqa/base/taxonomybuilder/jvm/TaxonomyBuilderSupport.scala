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

package eu.cdevreeze.tqa.base.taxonomybuilder.jvm

import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedThreadSafeWrapperDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.CachingThreadSafeDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.CachingThreadSafeDocumentBuilder.LoadingCacheWrapper
import eu.cdevreeze.tqa.docbuilder.jvm.TaxonomyPackageUriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.ThreadSafeSaxonDocumentBuilder
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor

import scala.collection.immutable

/**
 * Easy to use TaxonomyBuilder creation support that should be applicable in many cases.
 *
 * @author Chris de Vreeze
 */
object TaxonomyBuilderSupport {

  /**
   * Calls method `forTaxonomyPackages` for one taxonomy package ZIP file.
   */
  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile, processor: Processor): TaxonomyBuilder = {
    forTaxonomyPackages(immutable.IndexedSeq(taxonomyPackageZipFile), processor)
  }

  /**
   * Creates a TaxonomyBuilder, exploiting parallelism during parsing and relationship creation.
   * Documents are parsed from taxonomy package ZIP files (each having at least a catalog.xml), into SaxonDocument instances.
   * The used (parallel) relationship factory is a strict one.
   */
  def forTaxonomyPackages(
      taxonomyPackageZipFiles: immutable.IndexedSeq[ZipFile],
      processor: Processor): TaxonomyBuilder = {
    val uriResolver = TaxonomyPackageUriResolvers.forTaxonomyPackages(taxonomyPackageZipFiles)

    TaxonomyBuilder
      .withDocumentBuilder(ThreadSafeSaxonDocumentBuilder(processor, uriResolver))
      .withDocumentCollector(DefaultParallelDtsCollector())
      .withRelationshipFactory(DefaultParallelRelationshipFactory.StrictInstance)
  }

  /**
   * Calls method `forTaxonomyPackagesUsingIndexedDocuments` for one taxonomy package ZIP file.
   */
  def forTaxonomyPackageUsingIndexedDocuments(
      taxonomyPackageZipFile: ZipFile,
      processor: Processor): TaxonomyBuilder = {
    forTaxonomyPackagesUsingIndexedDocuments(immutable.IndexedSeq(taxonomyPackageZipFile), processor)
  }

  /**
   * Creates a TaxonomyBuilder, exploiting parallelism during parsing and relationship creation.
   * Documents are parsed from taxonomy package ZIP files (each having at least a catalog.xml), into indexed.Document instances.
   * The used (parallel) relationship factory is a strict one.
   */
  def forTaxonomyPackagesUsingIndexedDocuments(
      taxonomyPackageZipFiles: immutable.IndexedSeq[ZipFile],
      processor: Processor): TaxonomyBuilder = {
    val uriResolver = TaxonomyPackageUriResolvers.forTaxonomyPackages(taxonomyPackageZipFiles)

    TaxonomyBuilder
      .withDocumentBuilder(
        IndexedThreadSafeWrapperDocumentBuilder(ThreadSafeSaxonDocumentBuilder(processor, uriResolver)))
      .withDocumentCollector(DefaultParallelDtsCollector())
      .withRelationshipFactory(DefaultParallelRelationshipFactory.StrictInstance)
  }

  /**
   * Creates a TaxonomyBuilder from a LoadingCacheWrapper, which is typically created by method `createDocumentCache`.
   * The DTS collector is a parallel one, and so is the strict relationship factory.
   */
  def usingDocumentCache[D <: BackingDocumentApi](docCacheWrapper: LoadingCacheWrapper[D]): TaxonomyBuilder = {
    val docBuilder = new CachingThreadSafeDocumentBuilder(docCacheWrapper)

    TaxonomyBuilder
      .withDocumentBuilder(docBuilder)
      .withDocumentCollector(DefaultParallelDtsCollector())
      .withRelationshipFactory(DefaultParallelRelationshipFactory.StrictInstance)
  }

  /**
   * Factory method to create a Caffeine SaxonDocument cache, returned as LoadingCacheWrapper. Note that the LoadingCacheWrapper
   * (in particular its cache) is an expensive object that should typically have a long lifetime, and should typically be reused.
   */
  def createDocumentCache(
      taxonomyPackageZipFile: ZipFile,
      processor: Processor,
      documentCacheSize: Int): LoadingCacheWrapper[SaxonDocument] = {
    createDocumentCache(immutable.IndexedSeq(taxonomyPackageZipFile), processor, documentCacheSize)
  }

  /**
   * Factory method to create a Caffeine SaxonDocument cache, returned as LoadingCacheWrapper. Note that the LoadingCacheWrapper
   * (in particular its cache) is an expensive object that should typically have a long lifetime, and should typically be reused.
   */
  def createDocumentCache(
      taxonomyPackageZipFiles: immutable.IndexedSeq[ZipFile],
      processor: Processor,
      documentCacheSize: Int): LoadingCacheWrapper[SaxonDocument] = {
    val uriResolver = TaxonomyPackageUriResolvers.forTaxonomyPackages(taxonomyPackageZipFiles)

    CachingThreadSafeDocumentBuilder.createCache(
      ThreadSafeSaxonDocumentBuilder(processor, uriResolver),
      documentCacheSize)
  }

  /**
   * Factory method to create a Caffeine indexed.Document cache, returned as LoadingCacheWrapper. Note that the LoadingCacheWrapper
   * (in particular its cache) is an expensive object that should typically have a long lifetime, and should typically be reused.
   */
  def createIndexedDocumentCache(
      taxonomyPackageZipFile: ZipFile,
      processor: Processor,
      documentCacheSize: Int): LoadingCacheWrapper[indexed.Document] = {
    createIndexedDocumentCache(immutable.IndexedSeq(taxonomyPackageZipFile), processor, documentCacheSize)
  }

  /**
   * Factory method to create a Caffeine indexed.Document cache, returned as LoadingCacheWrapper. Note that the LoadingCacheWrapper
   * (in particular its cache) is an expensive object that should typically have a long lifetime, and should typically be reused.
   */
  def createIndexedDocumentCache(
      taxonomyPackageZipFiles: immutable.IndexedSeq[ZipFile],
      processor: Processor,
      documentCacheSize: Int): LoadingCacheWrapper[indexed.Document] = {
    val uriResolver = TaxonomyPackageUriResolvers.forTaxonomyPackages(taxonomyPackageZipFiles)

    CachingThreadSafeDocumentBuilder.createCache(
      IndexedThreadSafeWrapperDocumentBuilder(ThreadSafeSaxonDocumentBuilder(processor, uriResolver)),
      documentCacheSize)
  }
}
