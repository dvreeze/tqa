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

import scala.collection.immutable

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriConverters
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
    val taxonomyBuilder: TaxonomyBuilder = getTaxonomyBuilder(resultUriResolver)

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

  /**
   * Helper class implementing custom URI resolution, where URIs can be resolved from one of several directories where the same
   * document may be found, or from some ZIP file, or "from the internet" (the latter replaced by another local directory in this test).
   *
   * For each URI, if needed it will be tried to resolve it from one of the several local directories, or otherwise from the ZIP file,
   * or otherwise "from the internet" (see above).
   *
   * The tric is to do this URI resolution in such a way that the URI resolution workflow in practice hardly leans on exception
   * handling. It is also desirable to do URI resolution in such a way that we do not need to build (in-memory) XML catalogs.
   * This is indeed achieved in this CustomUriResolution class.
   */
  final class CustomUriResolution(val localDirCatalogs: immutable.IndexedSeq[SimpleCatalog]) {

    def getResultUriResolver: UriResolver = {
      UriResolvers.fromPartialUriResolversWithoutFallback(Vector(getFirstPartialUriResolver, getSecondPartialUriResolver))
    }

    private def getFirstPartialUriResolver: PartialUriResolver = {
      // Resolution workflow starting with the local root directories

      val uriResolver1: UriResolver = UriResolvers.fromPartialUriResolverWithoutFallback(partialUriResolverForLocalDirs)

      // Next attempt in the resolution workflow

      val uriResolver2: UriResolver = uriResolverForZipFile

      // Falling back "to the internet"

      val uriResolver3: UriResolver = fakeUriResolverForInternet

      // Wiring this resolution workflow together

      val workflow: UriResolver = uriResolver1.withResolutionFallback(uriResolver2).withResolutionFallback(uriResolver3)

      // Make this URI resolver partial, in order to only handle URIs that may be resolved from the local root directories

      PartialUriResolvers.fromUriResolver(workflow, acceptUriInFirstPartialUriResolver)
    }

    private def getSecondPartialUriResolver: PartialUriResolver = {
      // In this second resolution workflow, no URIs will be passed that could potentially have been resolved from the local
      // root directories, so forget about these local root directories here.

      // First attempt in the resolution workflow

      val uriResolver1: UriResolver = uriResolverForZipFile

      // Falling back "to the internet"

      val uriResolver2: UriResolver = fakeUriResolverForInternet

      // Wiring this resolution workflow together

      val workflow: UriResolver = uriResolver1.withResolutionFallback(uriResolver2)

      // Make this URI resolver "partial", but here there is no need to filter on any URIs. After all, each URI is either
      // handled by the first partial URI resolver, or not (in which case this fallback partial URI resolver is used).

      // Note the 2 different "fallback concepts" in URI resolution terminology: "URI fallback" versus "resolution fallback"
      // (for the same URIs).

      PartialUriResolvers.fromUriResolver(workflow)
    }

    private def acceptUriInFirstPartialUriResolver(uri: URI): Boolean = {
      partialUriResolverForLocalDirs(uri).nonEmpty // Nice, no need to create a new in-memory XML catalog
    }

    private val partialUriResolverForLocalDirs: PartialUriResolver = {
      val partialUriConverter: URI => Option[URI] =
        PartialUriConverters.fromCatalogs(localDirCatalogs, PartialUriConverters.acceptOnlyExistingFile)

      PartialUriResolvers.fromPartialUriConverter(partialUriConverter)
    }

    private val uriResolverForZipFile: UriResolver = {
      val zipFile: File = new File(zipFileUri)

      // TODO Pass ZipFile from the outside, in order to be able to close it
      UriResolvers.forZipFileContainingLocalMirrorWithoutScheme(new ZipFile(zipFile), Some(URI.create("taxonomie/")))
    }

    private val fakeUriResolverForInternet: UriResolver = {
      val taxoFolder: File = new File(scatteredTaxoFilesDir, "remaining-files/taxonomie").ensuring(_.isDirectory)

      UriResolvers.fromLocalMirrorRootDirectoryWithoutScheme(taxoFolder)
    }
  }

  private val resultUriResolver: UriResolver = {
    val folder1: File = new File(scatteredTaxoFilesDir, "folder1").ensuring(_.isDirectory)

    val catalog1: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> s"${folder1.toURI}taxonomie/www.nltaxonomie.nl/nt12/ez/",
    ))

    val folder2: File = new File(scatteredTaxoFilesDir, "folder2").ensuring(_.isDirectory)

    val catalog2: SimpleCatalog = SimpleCatalog.from(Map(
      "http://www.nltaxonomie.nl/nt12/ez/" -> s"${folder2.toURI}taxonomie/www.nltaxonomie.nl/nt12/ez/",
      "http://www.nltaxonomie.nl/nt12/sbr/" -> s"${folder2.toURI}taxonomie/www.nltaxonomie.nl/nt12/sbr/",
    ))

    val customUriResolution = new CustomUriResolution(Vector(catalog1, catalog2))

    customUriResolution.getResultUriResolver
  }

  private def getDocBuilder(uriResolver: UriResolver): SaxonDocumentBuilder = {
    SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
  }

  private def getTaxonomyBuilder(uriResolver: UriResolver): TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val docBuilder = getDocBuilder(uriResolver)

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
