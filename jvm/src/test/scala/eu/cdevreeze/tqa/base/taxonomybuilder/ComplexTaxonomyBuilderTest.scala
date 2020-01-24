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

package eu.cdevreeze.tqa.base.taxonomybuilder

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers.PartialUriResolver
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers._
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite

/**
 * Complex taxonomy bootstrapping test, where taxonomy files are scattered across directory trees and ZIP files. It uses test data from the NL taxonomy (EZ).
 *
 * @author Chris de Vreeze
 */
class ComplexTaxonomyBuilderTest extends AnyFunSuite {

  test("testComplexTaxonomyBootstrapping") {
    val dts: BasicTaxonomy = taxonomyBuilder.build(Set(entryPointUri))

    assertResult(true) {
      dts.taxonomyBase.taxonomyDocUriMap.contains(entryPointUri)
    }

    assertResult(675) {
      dts.relationships.size
    }
  }

  // Bootstrapping

  private val processor = new Processor(false)

  private val zipFileUri = {
    classOf[ComplexTaxonomyBuilderTest].getResource("/scattered-taxonomy-files/core-files-without-xbrl-instance-2003-12-31.zip").toURI
  }

  private val scatteredTaxoFilesDir: File = new File(zipFileUri).getParentFile.ensuring(_.isDirectory)

  private val ntPartialUriResolver: PartialUriResolver = {
    val folder1: File = new File(scatteredTaxoFilesDir, "folder1").ensuring(_.isDirectory)

    val catalog1: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> s"${folder1.toURI}taxonomie/www.nltaxonomie.nl/nt12/ez/",
    ))

    val folder1Resolver: UriResolver = UriResolvers.fromCatalogWithoutFallback(catalog1)

    val folder2: File = new File(scatteredTaxoFilesDir, "folder2").ensuring(_.isDirectory)

    val catalog2: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> s"${folder2.toURI}taxonomie/www.nltaxonomie.nl/nt12/ez/",
      "http://www.nltaxonomie.nl/nt12/sbr/" -> s"${folder2.toURI}taxonomie/www.nltaxonomie.nl/nt12/sbr/",
    ))

    val folder2Resolver: UriResolver = UriResolvers.fromCatalogWithoutFallback(catalog2)

    val zipFile: File = new File(zipFileUri)

    val catalogForZipFile: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> "taxonomie/www.nltaxonomie.nl/nt12/ez/",
      "http://www.nltaxonomie.nl/nt12/sbr/" -> "taxonomie/www.nltaxonomie.nl/nt12/sbr/",
    ))

    // TODO Pass ZipFile from the outside, in order to be able to close it
    val zipResolver: UriResolver = UriResolvers.forZipFileUsingCatalogWithoutFallback(new ZipFile(zipFile), catalogForZipFile)

    val remainingFilesFolder: File = new File(scatteredTaxoFilesDir, "remaining-files").ensuring(_.isDirectory)

    val fallbackCatalog: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> s"${remainingFilesFolder.toURI}taxonomie/www.nltaxonomie.nl/nt12/ez/",
      "http://www.nltaxonomie.nl/nt12/sbr/" -> s"${remainingFilesFolder.toURI}taxonomie/www.nltaxonomie.nl/nt12/sbr/",
    ))

    val remainingFilesResolver: UriResolver = UriResolvers.fromCatalogWithoutFallback(fallbackCatalog)

    val resolver: UriResolver = folder1Resolver
      .withResolutionFallback(folder2Resolver)
      .withResolutionFallback(zipResolver)
      .withResolutionFallback(remainingFilesResolver)

    PartialUriResolvers.fromUriResolver(resolver, _.toString.startsWith("http://www.nltaxonomie.nl/nt12/"))
  }

  private val coreFilePartialUriResolver: PartialUriResolver = {
    val zipFile: File = new File(zipFileUri)

    val catalogForZipFile: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/" -> "taxonomie/www.nltaxonomie.nl/",
      "http://www.xbrl.org/" -> "taxonomie/www.xbrl.org/",
      "http://www.w3.org/" -> "taxonomie/www.w3.org/",
    ))

    // TODO Pass ZipFile from the outside, in order to be able to close it
    val zipResolver: UriResolver = UriResolvers.forZipFileUsingCatalogWithoutFallback(new ZipFile(zipFile), catalogForZipFile)

    val remainingFilesFolder: File = new File(scatteredTaxoFilesDir, "remaining-files").ensuring(_.isDirectory)

    val fallbackCatalog: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/" -> s"${remainingFilesFolder.toURI}taxonomie/www.nltaxonomie.nl/",
      "http://www.xbrl.org/" -> s"${remainingFilesFolder.toURI}taxonomie/www.xbrl.org/",
      "http://www.w3.org/" -> s"${remainingFilesFolder.toURI}taxonomie/www.w3.org/",
    ))

    val remainingFilesResolver: UriResolver = UriResolvers.fromCatalogWithoutFallback(fallbackCatalog)

    val resolver: UriResolver = zipResolver.withResolutionFallback(remainingFilesResolver)

    PartialUriResolvers.fromUriResolver(resolver)
  }

  private val uriResolver: UriResolver = {
    UriResolvers.fromPartialUriResolversWithoutFallback(Vector(ntPartialUriResolver, coreFilePartialUriResolver))
  }

  private val docBuilder: SaxonDocumentBuilder = {
    SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
  }

  private val taxonomyBuilder: TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder
        .withDocumentBuilder(docBuilder)
        .withDocumentCollector(documentCollector)
        .withRelationshipFactory(relationshipFactory)

    taxoBuilder
  }

  private val entryPointUri =
    URI.create(
      "http://www.nltaxonomie.nl/nt12/ez/20170714.a/entrypoints/ez-rpt-ncgc-nederlandse-corporate-governance-code.xsd")
}
