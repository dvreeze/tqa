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

import scala.collection.compat._

import org.scalatest.FunSuite

import eu.cdevreeze.tqa.base.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy equivalence test case. It uses test data from the NL taxonomy (EZ).
 *
 * @author Chris de Vreeze
 */
class TaxonomyEquivalenceTest extends FunSuite {

  test("testTaxonomyWithoutLabels") {
    val zipFileUri =
      classOf[TaxonomyEquivalenceTest].getResource("/taxonomy-zip-files/taxonomie-ez-no-labels.zip").toURI

    val dts = createDts(entryPointUri, zipFileUri)

    doTestDtsEquivalence(dts)
  }

  test("testTaxonomyWithoutLabelsCombiningXsds") {
    val zipFileUri =
      classOf[TaxonomyEquivalenceTest].getResource("/taxonomy-zip-files/taxonomie-ez-no-labels-combined-xsds.zip").toURI

    val dts = createDts(entryPointUri, zipFileUri)

    doTestDtsEquivalence(dts)
  }

  private def doTestDtsEquivalence(dts: TaxonomyApi): Unit = {
    assertResult(true) {
      dts.findAllConceptDeclarations.nonEmpty
    }

    assertResult(referenceTaxonomy.findAllConceptDeclarations.map(_.targetEName).toSet) {
      dts.findAllConceptDeclarations.map(_.targetEName).toSet
    }

    assertResult(true) {
      dts.findAllHypercubeDeclarations.nonEmpty
    }

    assertResult(referenceTaxonomy.findAllHypercubeDeclarations.map(_.targetEName).toSet) {
      dts.findAllHypercubeDeclarations.map(_.targetEName).toSet
    }

    assertResult(true) {
      dts.computeHasHypercubeInheritanceOrSelf.nonEmpty
    }

    assertResult(referenceTaxonomy.computeHasHypercubeInheritanceOrSelf.view.mapValues(_.map(_.elr).toSet).toMap) {
      dts.computeHasHypercubeInheritanceOrSelf.view.mapValues(_.map(_.elr).toSet).toMap
    }

    assertResult(true) {
      dts.findAllParentChildRelationships.nonEmpty
    }

    assertResult(referenceTaxonomy.findAllParentChildRelationships.map(_.elr).toSet) {
      dts.findAllParentChildRelationships.map(_.elr).toSet
    }

    assertResult(referenceTaxonomy.findAllParentChildRelationships.map(_.targetConceptEName).toSet) {
      dts.findAllParentChildRelationships.map(_.targetConceptEName).toSet
    }
  }

  // Bootstrapping

  private val processor = new Processor(false)

  private def createDts(entryPointUri: URI, zipFileUri: URI): BasicTaxonomy = {
    val taxoBuilder = taxonomyBuilder(zipFileUri)

    taxoBuilder.build(Set(entryPointUri))
  }

  private def docBuilder(zipFileUri: URI): SaxonDocumentBuilder = {
    val zipFile = new File(zipFileUri)

    val catalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(None, "http://www.nltaxonomie.nl/", "taxonomie/www.nltaxonomie.nl/"),
          SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", "taxonomie/www.xbrl.org/"),
          SimpleCatalog.UriRewrite(None, "http://www.w3.org/", "taxonomie/www.w3.org/")))

    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      UriResolvers.forZipFileUsingCatalogWithFallback(new ZipFile(zipFile), catalog))
  }

  private def taxonomyBuilder(zipFileUri: URI): TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder(zipFileUri)).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    taxoBuilder
  }

  // "Reference taxonomy"

  private val entryPointUri =
    URI.create("http://www.nltaxonomie.nl/nt12/ez/20170714.a/entrypoints/ez-rpt-ncgc-nederlandse-corporate-governance-code.xsd")

  private val referenceTaxonomy: BasicTaxonomy = {
    val zipFileUri = classOf[TaxonomyEquivalenceTest].getResource("/taxonomy-zip-files/taxonomie-ez.zip").toURI

    val dts = createDts(entryPointUri, zipFileUri)

    dts.ensuring(_.relationships.size == 675, s"Expected 675 but found ${dts.relationships.size} relationships")
  }
}
