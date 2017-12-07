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
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Dimensional instance validation test case 271 (271-PrimaryItemDimensionallyInvalidError), from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class PrimaryItemDimensionalValidation271Test extends FunSuite {

  private val processor = new Processor(false)

  private val docBuilder = {
    val otherRootDir = new File(classOf[PrimaryItemDimensionalValidation271Test].getResource("/xbrl-and-w3").toURI)

    SaxonDocumentBuilder.usingUriConverter(processor.newDocumentBuilder(), { uri =>
      if (uri.getScheme == "http" || uri.getScheme == "https") {
        UriConverters.uriToLocalUri(uri, otherRootDir)
      } else {
        uri
      }
    })
  }

  private val testCaseFile =
    new File(classOf[PrimaryItemDimensionalValidation271Test].getResource(
      "/conf-suite-dim/200-xbrldie/271-PrimaryItemDimensionallyInvalidError/271-Testcase-PrimaryItemDimensionallyInvalidError.xml").toURI).
      ensuring(_.isFile)

  private val xbrldieNs = "http://xbrl.org/2005/xbrldi/errors"
  private val confNs = "http://xbrl.org/2005/conformance"

  private val ValidationErrorEName = EName(xbrldieNs, "PrimaryItemDimensionallyInvalidError")

  private val testCaseDocElem =
    docBuilder.build(testCaseFile.toURI).ensuring(_.resolvedName == EName(confNs, "testcase")).
      ensuring(_.filterElems(_.resolvedName == EName(confNs, "error")).forall(_.textAsResolvedQName == ValidationErrorEName))

  private def skipTestVariation(id: String): Boolean = {
    Set().contains(id)
  }

  private val variationElems =
    testCaseDocElem.filterChildElems(_.resolvedName == EName(confNs, "variation")) filterNot { e =>
      skipTestVariation(e.attribute(EName("id")))
    }

  variationElems foreach { variationElem =>
    val id = variationElem.attribute(EName("id"))
    val name = variationElem.attribute(EName("name"))

    val instanceElem =
      variationElem.findElem(_.resolvedName == EName(confNs, "instance")).getOrElse(sys.error(s"No instance mentioned in test variation $id"))
    require(instanceElem.parent.resolvedName == EName(confNs, "data"))

    val instanceFileName = instanceElem.text

    val resultElem = variationElem.getChildElem(_.resolvedName == EName(confNs, "result"))

    val expectedValidationErrorCount =
      resultElem.filterChildElems(_.resolvedName == EName(confNs, "error")).size

    val expectedSuccessCount: Int = {
      val fileElems = resultElem.filterChildElems(_.resolvedName == EName(confNs, "file"))

      val fileNames = fileElems.map(_.text)

      val outputFileElems = fileNames map { fileName =>
        val outputFile = new File(testCaseFile.getParentFile, s"out/$fileName")
        val outputFileElem = docBuilder.build(outputFile.toURI)

        outputFileElem.ensuring(_.resolvedName == EName("facts"))
      }

      val successCountInOutputFiles = outputFileElems.map(_.filterChildElems(_.resolvedName == EName("item")).size).sum
      successCountInOutputFiles
    }

    require(expectedSuccessCount > 0 || expectedValidationErrorCount > 0, s"Expected at least one validation result for variation $id")

    // Invoke the ScalaTest test corresponding to the test variation
    test(s"${id}_$name") {
      performTestVariation(id, name, instanceFileName, expectedSuccessCount, expectedValidationErrorCount)
    }
  }

  final def performTestVariation(
    id: String,
    name: String,
    instanceFileName: String,
    expectedSuccessCount: Int,
    expectedValidationErrorCount: Int): Unit = {

    val instance = makeTestInstance(
      "200-xbrldie/271-PrimaryItemDimensionallyInvalidError/" + instanceFileName)

    val validator = makeValidator(instance)

    val itemFacts = instance.findAllItems

    val itemValidationResults = itemFacts.map(fact => validator.validateDimensionally(fact, instance))

    assertResult(expectedValidationErrorCount) {
      itemValidationResults.filter(_.isSuccess).count(!_.get)
    }

    if (expectedValidationErrorCount == 0) {
      assertResult(expectedSuccessCount) {
        itemValidationResults.filter(_.isSuccess).count(_.get)
      }
    }

    // For this test, the only errors are xbrldie:PrimaryItemDimensionallyInvalidError ones
    assertResult(0) {
      itemValidationResults.count(_.isFailure)
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
    val rootDir = new File(classOf[PrimaryItemDimensionalValidation271Test].getResource("/conf-suite-dim").toURI)
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
}
