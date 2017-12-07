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
import java.net.URI

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
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

  import DimensionalContext.contextToDimensionalContext

  // 202-DefaultValueUsedInInstanceError

  test("testDefaultValueInInstanceOK") {
    val instance = makeTestInstance("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstanceOK.xbrl")
    val validator = makeValidator(instance)

    val tns = "http://xbrl.org/dims/conformance"
    val productTns = "http://www.xbrl.org/dim/conf/product"

    assertResult(true) {
      validator.dimensionDefaults.get(EName(tns, "ProductDim")).contains(EName(productTns, "AllProducts"))
    }

    assertResult(Set(EName(productTns, "Cars"), EName(productTns, "Wine"))) {
      instance.allContexts.flatMap(_.explicitDimensionMembers.get(EName(tns, "ProductDim"))).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(3) {
      dimContexts.size
    }
    assertResult(3) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testDefaultValueInInstance") {
    val instance = makeTestInstance("200-xbrldie/202-DefaultValueUsedInInstanceError/defaultValueInInstance.xbrl")
    val validator = makeValidator(instance)

    val tns = "http://xbrl.org/dims/conformance"
    val productTns = "http://www.xbrl.org/dim/conf/product"

    assertResult(true) {
      validator.dimensionDefaults.get(EName(tns, "ProductDim")).contains(EName(productTns, "AllProducts"))
    }

    assertResult(Set(EName(productTns, "AllProducts"), EName(productTns, "Cars"), EName(productTns, "Wine"))) {
      instance.allContexts.flatMap(_.explicitDimensionMembers.get(EName(tns, "ProductDim"))).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(3) {
      dimContexts.size
    }
    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isFailure)
    }
    intercept[DefaultValueUsedInInstanceError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  // 204-RepeatedDimensionInInstanceError

  test("testContextContainsTypedDimensionValid") {
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsTypedDimensionValid.xbrl")
    val validator = makeValidator(instance)

    assertResult(List(ContextElement.Segment)) {
      validator.taxonomy.findAllHasHypercubeRelationships.map(_.contextElement)
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
    assertResult(List(false)) {
      dimContexts.map(_.hasRepeatedDimensions)
    }

    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testContextContainsRepeatedDimension") {
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/contextContainsRepeatedDimension.xbrl")
    val validator = makeValidator(instance)

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
    assertResult(List(true)) {
      dimContexts.map(_.hasRepeatedDimensions)
    }

    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isFailure)
    }
    intercept[RepeatedDimensionInInstanceError.type] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testBiLocatableExplicitDimInSeg") {
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/bi-locational-seg-explicit-instance.xml")
    val validator = makeValidator(instance)

    assertResult(Set(ContextElement.Segment, ContextElement.Scenario)) {
      validator.taxonomy.findAllHasHypercubeRelationships.map(_.contextElement).toSet
    }

    assertResult(Set(ContextElement.Segment, ContextElement.Scenario)) {
      instance.allTopLevelItems.flatMap(fact => validator.taxonomy.findAllOwnOrInheritedHasHypercubes(fact.resolvedName)).
        map(_.contextElement).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(1) {
      instance.allContexts.flatMap(_.entity.segmentOption).flatMap(_.explicitMembers).size
    }
    assertResult(List(false)) {
      dimContexts.map(_.hasRepeatedDimensions)
    }

    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testBiLocatableExplicitDimInSegAndScen") {
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/bi-locational-dual-explicit-instance.xml")
    val validator = makeValidator(instance)

    assertResult(Set(ContextElement.Segment, ContextElement.Scenario)) {
      validator.taxonomy.findAllHasHypercubeRelationships.map(_.contextElement).toSet
    }

    assertResult(Set(ContextElement.Segment, ContextElement.Scenario)) {
      instance.allTopLevelItems.flatMap(fact => validator.taxonomy.findAllOwnOrInheritedHasHypercubes(fact.resolvedName)).
        map(_.contextElement).toSet
    }

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(List(true)) {
      dimContexts.map(_.hasRepeatedDimensions)
    }

    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isFailure)
    }
    intercept[RepeatedDimensionInInstanceError.type] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testRepeatedDimensionInScenario") {
    val instance = makeTestInstance("200-xbrldie/204-RepeatedDimensionInInstanceError/repeatedDimensionInScenario.xbrl")

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(List(true)) {
      dimContexts.map(_.hasRepeatedDimensions)
    }
    assertResult(List(true)) {
      dimContexts.map(_.dimensionalScenario).map(_.hasRepeatedDimensions)
    }
    assertResult(List(false)) {
      dimContexts.map(_.dimensionalSegment).map(_.hasRepeatedDimensions)
    }
  }

  // 205-TypedMemberNotTypedDimensionError

  test("testTypedMemberIsExplicitInvalid") {
    val instance = makeTestInstance("200-xbrldie/205-TypedMemberNotTypedDimensionError/typedMemberIsExplicitInvalid.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[TypedMemberNotTypedDimensionError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  // 206-ExplicitMemberNotExplicitDimensionError

  test("testContextMemberNotExplicitDimension") {
    val instance = makeTestInstance("200-xbrldie/206-ExplicitMemberNotExplicitDimensionError/contextMemberNotExplicitDimension.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[ExplicitMemberNotExplicitDimensionError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  // 207-ExplicitMemberUndefinedQNameError

  test("testContextExplicitDimDomainMemberNotFound") {
    val instance = makeTestInstance("200-xbrldie/207-ExplicitMemberUndefinedQNameError/contextExplicitDimDomainMemberNotFound.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[ExplicitMemberUndefinedQNameError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testEmptyDimensionAndNotAllHypercube-1") {
    val instance = makeTestInstance("200-xbrldie/207-ExplicitMemberUndefinedQNameError/contextExplicitDimDomainMemberNotFound.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[ExplicitMemberUndefinedQNameError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testEmptyDimensionAndNotAllHypercube-4") {
    val instance = makeTestInstance("200-xbrldie/207-ExplicitMemberUndefinedQNameError/EmptyDimensionAndNotAllHypercube-4.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[ExplicitMemberUndefinedQNameError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testEmptyDimensionAndNotAllHypercube-5") {
    val instance = makeTestInstance("200-xbrldie/207-ExplicitMemberUndefinedQNameError/EmptyDimensionAndNotAllHypercube-5.xbrl")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  // 208-IllegalTypedDimensionContentError

  test("testTypedDimSegIsValid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimSegValid-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testTypedDimScenIsValid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimScenValid-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    assertResult(1) {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).count(_.isSuccess)
    }
  }

  test("testTypedDimSegIsInvalid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimSegInvalid-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[IllegalTypedDimensionContentError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testTypedDimScenIsInvalid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimScenInvalid-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[IllegalTypedDimensionContentError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testUnusedTypedDimSegIsInvalid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimSegUnused-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[IllegalTypedDimensionContentError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  test("testUnusedTypedDimScenIsInvalid") {
    val instance = makeTestInstance("200-xbrldie/208-IllegalTypedDimensionContentError/typedDimScenUnused-instance.xml")
    val validator = makeValidator(instance)

    val dimContexts = instance.allContexts.map(contextToDimensionalContext)

    assertResult(1) {
      dimContexts.size
    }
    intercept[IllegalTypedDimensionContentError] {
      dimContexts.map(ctx => validator.validateDimensionalContext(ctx)).find(_.isFailure).get.get
    }
  }

  // Helper methods

  private def makeValidator(xbrlInstance: XbrlInstance, doResolveProhibitionAndOverriding: Boolean = false): DimensionalValidator = {
    val entrypointHrefs =
      xbrlInstance.findAllSchemaRefs.map(_.resolvedHref) ++ xbrlInstance.findAllLinkbaseRefs.map(_.resolvedHref)

    doMakeValidator(
      entrypointHrefs.toSet.filterNot(Set(URI.create("http://www.xbrl.org/2006/xbrldi-2006.xsd"))),
      doResolveProhibitionAndOverriding)
  }

  private def makeTestInstance(relativeDocPath: String): XbrlInstance = {
    val rootDir = new File(classOf[DimensionalInstanceValidationTest].getResource("/conf-suite-dim").toURI)
    val docFile = new File(rootDir, relativeDocPath)

    XbrlInstance(docBuilder.build(docFile.toURI))
  }

  private def doMakeValidator(entrypointUris: Set[URI], doResolveProhibitionAndOverriding: Boolean): DimensionalValidator = {
    val documentCollector = DefaultDtsCollector(entrypointUris)

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    val basicTaxo = taxoBuilder.build()
    val effectiveTaxo =
      if (doResolveProhibitionAndOverriding) {
        basicTaxo.resolveProhibitionAndOverriding(relationshipFactory)
      } else {
        basicTaxo
      }

    DimensionalValidator.build(effectiveTaxo)
  }

  private val processor = new Processor(false)

  private val docBuilder = {
    val otherRootDir = new File(classOf[DimensionalInstanceValidationTest].getResource("/xbrl-and-w3").toURI)

    SaxonDocumentBuilder.usingUriConverter(processor.newDocumentBuilder(), { uri =>
      if (uri.getScheme == "http" || uri.getScheme == "https") {
        UriConverters.uriToLocalUri(uri, otherRootDir)
      } else {
        uri
      }
    })
  }
}
