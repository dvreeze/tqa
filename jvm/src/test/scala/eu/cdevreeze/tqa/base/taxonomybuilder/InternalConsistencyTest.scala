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

import scala.reflect.classTag

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.StandardInterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.NonStandardRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import net.sf.saxon.s9api.Processor

/**
 * Relationship query API internal consistency test. It uses test data from the NL taxonomy (EZ).
 *
 * @author Chris de Vreeze
 */
class InternalConsistencyTest extends AnyFunSuite {

  test("testQueryingForStandardRelationships") {
    val standardRelationships = dts.findAllStandardRelationships.ensuring(_.nonEmpty)

    val sourceConcepts = standardRelationships.map(_.sourceConceptEName).toSet
    val targetKeys = standardRelationships.map(_.targetElem.key).toSet

    assertResult(targetKeys) {
      sourceConcepts.toSeq.flatMap(c => dts.findAllOutgoingStandardRelationships(c))
        .map(_.targetElem.key).toSet
    }
  }

  test("testQueryingForStandardInterConceptRelationships") {
    val interConceptRelationships = dts.findAllStandardInterConceptRelationships.ensuring(_.nonEmpty)

    val sourceConcepts = interConceptRelationships.map(_.sourceConceptEName).toSet
    val targetConcepts = interConceptRelationships.map(_.targetConceptEName).toSet

    assertResult(targetConcepts) {
      sourceConcepts.toSeq.flatMap(c => dts.findAllOutgoingStandardInterConceptRelationships(c))
        .map(_.targetConceptEName).toSet
    }

    assertResult(sourceConcepts) {
      targetConcepts.toSeq.flatMap(c => dts.findAllIncomingStandardInterConceptRelationships(c))
        .map(_.sourceConceptEName).toSet
    }
  }

  test("testQueryingForNonStandardRelationships") {
    val nonStandardRelationships = dts.findAllNonStandardRelationships.ensuring(_.nonEmpty)

    val sourceKeys = nonStandardRelationships.map(_.sourceElem.key).toSet
    val targetKeys = nonStandardRelationships.map(_.targetElem.key).toSet

    assertResult(targetKeys) {
      sourceKeys.toSeq.flatMap(k => dts.findAllOutgoingNonStandardRelationships(k))
        .map(_.targetElem.key).toSet
    }

    assertResult(sourceKeys) {
      targetKeys.toSeq.flatMap(k => dts.findAllIncomingNonStandardRelationships(k))
        .map(_.sourceElem.key).toSet
    }
  }

  test("testQueryingForStandardInterConceptRelationshipPaths") {
    val interConceptRelationships = dts.findAllStandardInterConceptRelationships.ensuring(_.nonEmpty)

    val sourceConcepts = interConceptRelationships.map(_.sourceConceptEName).toSet
    val targetConcepts = interConceptRelationships.map(_.targetConceptEName).toSet

    val rootConcepts = sourceConcepts.diff(targetConcepts)
    val leafConcepts = targetConcepts.diff(sourceConcepts)

    assertResult(leafConcepts) {
      rootConcepts.toSeq
        .flatMap(c => dts.filterOutgoingConsecutiveStandardInterConceptRelationshipPaths(c, classTag[StandardInterConceptRelationship])(_ => true))
        .map(_.targetConcept).toSet
    }

    assertResult(rootConcepts) {
      leafConcepts.toSeq
        .flatMap(c => dts.filterIncomingConsecutiveStandardInterConceptRelationshipPaths(c, classTag[StandardInterConceptRelationship])(_ => true))
        .map(_.sourceConcept).toSet
    }
  }

  test("testQueryingForNonStandardRelationshipPaths") {
    val nonStandardRelationships = dts.findAllNonStandardRelationships.ensuring(_.nonEmpty)

    val sourceKeys = nonStandardRelationships.map(_.sourceElem.key).toSet
    val targetKeys = nonStandardRelationships.map(_.targetElem.key).toSet

    val rootKeys = sourceKeys.diff(targetKeys)
    val leafKeys = targetKeys.diff(sourceKeys)

    assertResult(leafKeys) {
      rootKeys.toSeq
        .flatMap { c =>
          dts.filterOutgoingUnrestrictedNonStandardRelationshipPaths(c, classTag[NonStandardRelationship])(_.isSingleElrRelationshipPath)
        }
        .map(_.targetKey).toSet
    }

    assertResult(rootKeys) {
      leafKeys.toSeq
        .flatMap { c =>
          dts.filterIncomingUnrestrictedNonStandardRelationshipPaths(c, classTag[NonStandardRelationship])(_.isSingleElrRelationshipPath)
        }
        .map(_.sourceKey).toSet
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
          SimpleCatalog.UriRewrite("http://www.nltaxonomie.nl/", "taxonomie/www.nltaxonomie.nl/"),
          SimpleCatalog.UriRewrite("http://www.xbrl.org/", "taxonomie/www.xbrl.org/"),
          SimpleCatalog.UriRewrite("http://www.w3.org/", "taxonomie/www.w3.org/")))

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

  private val entryPointUri =
    URI.create("http://www.nltaxonomie.nl/nt12/ez/20170714.a/entrypoints/ez-rpt-ncgc-nederlandse-corporate-governance-code.xsd")

  private val dts: BasicTaxonomy = {
    val zipFileUri = classOf[InternalConsistencyTest].getResource("/taxonomy-zip-files/taxonomie-ez.zip").toURI

    val dts = createDts(entryPointUri, zipFileUri)

    dts.ensuring(_.relationships.size == 675, s"Expected 675 but found ${dts.relationships.size} relationships")
  }
}
