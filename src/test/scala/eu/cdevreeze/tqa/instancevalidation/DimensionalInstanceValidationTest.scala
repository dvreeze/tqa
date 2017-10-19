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

package eu.cdevreeze.tqa.instancevalidation

import java.io.File

import scala.collection.immutable

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.backingelem.UriConverters
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy.DefaultValueUsedInInstanceError
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy.DimensionalContext
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy.DimensionalScenario
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy.DimensionalSegment
import eu.cdevreeze.tqa.base.taxonomy.DimensionalTaxonomy.RepeatedDimensionInInstanceError
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.instance.XbrliContext
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Dimensional instance validation test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class DimensionalInstanceValidationTest extends FunSuite {

  test("testDefaultValueInInstanceOK") {
    val taxo = makeTestDts(Vector("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstance.xsd"))
    val instance = makeTestInstance("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstanceOK.xbrl")

    val tns = "http://xbrl.org/dims/conformance"
    val productTns = "http://www.xbrl.org/dim/conf/product"

    assertResult(true) {
      taxo.dimensionDefaults.get(EName(tns, "ProductDim")).contains(EName(productTns, "AllProducts"))
    }

    assertResult(Set(EName(productTns, "Cars"), EName(productTns, "Wine"))) {
      instance.allContexts.flatMap(_.explicitDimensionMembers.get(EName(tns, "ProductDim"))).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(3) {
      dimContexts.size
    }
    assertResult(3) {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testDefaultValueInInstance") {
    val taxo = makeTestDts(Vector("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstance.xsd"))
    val instance = makeTestInstance("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstance.xbrl")

    val tns = "http://xbrl.org/dims/conformance"
    val productTns = "http://www.xbrl.org/dim/conf/product"

    assertResult(true) {
      taxo.dimensionDefaults.get(EName(tns, "ProductDim")).contains(EName(productTns, "AllProducts"))
    }

    assertResult(Set(EName(productTns, "AllProducts"), EName(productTns, "Cars"), EName(productTns, "Wine"))) {
      instance.allContexts.flatMap(_.explicitDimensionMembers.get(EName(tns, "ProductDim"))).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(3) {
      dimContexts.size
    }
    assertResult(1) {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).count(_.isFailure)
    }
    intercept[DefaultValueUsedInInstanceError] {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testContextContainsTypedDimensionValid") {
    val taxo = makeTestDts(Vector("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsRepeatedDimension.xsd"))
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsTypedDimensionValid.xbrl")

    assertResult(List(ContextElement.Segment)) {
      taxo.taxonomy.findAllHasHypercubeRelationships.map(_.contextElement)
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(1) {
      instance.allContexts.flatMap(_.entity.segmentOption).flatMap(_.typedMembers).size
    }
    assertResult(1) {
      instance.allContexts.flatMap(_.entity.segmentOption).flatMap(_.typedMembers).map(_.dimension).distinct.size
    }

    assertResult(1) {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testContextContainsRepeatedDimension") {
    val taxo = makeTestDts(Vector("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsRepeatedDimension.xsd"))
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsRepeatedDimension.xbrl")

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(2) {
      instance.allContexts.flatMap(_.entity.segmentOption).flatMap(_.typedMembers).size
    }
    assertResult(1) {
      instance.allContexts.flatMap(_.entity.segmentOption).flatMap(_.typedMembers).map(_.dimension).distinct.size
    }

    assertResult(1) {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).count(_.isFailure)
    }
    intercept[RepeatedDimensionInInstanceError.type] {
      dimContexts.map(ctx => taxo.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  // Helper methods

  private def makeTestDts(relativeDocPaths: immutable.IndexedSeq[String]): DimensionalTaxonomy = {
    val rootDir = new File(classOf[DimensionalInstanceValidationTest].getResource("/conf-suite-dim").toURI)
    val docFiles = relativeDocPaths.map(relativePath => new File(rootDir, relativePath))

    val entrypointUris = docFiles.map(_.toURI).toSet

    val documentCollector = DefaultDtsCollector(entrypointUris)

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    val basicTaxo = taxoBuilder.build()
    DimensionalTaxonomy.build(basicTaxo)
  }

  private def makeTestInstance(relativeDocPath: String): XbrlInstance = {
    val rootDir = new File(classOf[DimensionalInstanceValidationTest].getResource("/conf-suite-dim").toURI)
    val docFile = new File(rootDir, relativeDocPath)

    XbrlInstance(docBuilder.build(docFile.toURI))
  }

  private def contextToDimensionalContext(ctx: XbrliContext): DimensionalContext = {
    DimensionalContext(
      DimensionalSegment(
        ctx.entity.segmentOption.toIndexedSeq.flatMap(_.explicitMembers).map(e => (e.dimension -> e.member)),
        ctx.entity.segmentOption.toIndexedSeq.flatMap(_.typedMembers).map(_.dimension)),
      DimensionalScenario(
        ctx.scenarioOption.toIndexedSeq.flatMap(_.explicitMembers).map(e => (e.dimension -> e.member)),
        ctx.scenarioOption.toIndexedSeq.flatMap(_.typedMembers).map(_.dimension)))
  }

  private val processor = new Processor(false)

  private val docBuilder = {
    val otherRootDir = new File(classOf[DimensionalInstanceValidationTest].getResource("/xbrl-and-w3").toURI)

    new SaxonDocumentBuilder(processor.newDocumentBuilder(), { uri =>
      if (uri.getScheme == "http" || uri.getScheme == "https") {
        UriConverters.uriToLocalUri(uri, otherRootDir)
      } else {
        uri
      }
    })
  }
}
