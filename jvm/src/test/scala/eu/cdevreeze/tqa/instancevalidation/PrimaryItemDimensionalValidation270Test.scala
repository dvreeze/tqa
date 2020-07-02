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
import java.util.zip.ZipFile

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import net.sf.saxon.s9api.Processor

/**
 * Dimensional instance validation test case 270 (270-DefaultValueExamples), from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
class PrimaryItemDimensionalValidation270Test extends AnyFunSuite {

  private val processor = new Processor(false)

  private val dummyUriPrefix: URI = URI.create("http://www.example.com/")

  private val docBuilder: SaxonDocumentBuilder = {
    val otherRootDir = new File(classOf[PrimaryItemDimensionalValidation270Test].getResource("/xbrl-and-w3").toURI)
    val zipFile = new File(classOf[PrimaryItemDimensionalValidation270Test].getResource("/xdt-conf-cr4-2009-10-06.zip").toURI)

    val xbrlCatalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", otherRootDir.toURI.toString.stripSuffix("/") + "/www.xbrl.org/"),
          SimpleCatalog.UriRewrite(None, "http://www.w3.org/", otherRootDir.toURI.toString.stripSuffix("/") + "/www.w3.org/")))

    val xbrlAndW3UriPartialResolver = PartialUriResolvers.fromCatalog(xbrlCatalog)

    val catalog =
      SimpleCatalog(
        None,
        Vector(SimpleCatalog.UriRewrite(None, dummyUriPrefix.toString, "")))

    val zipFilePartialResolver = PartialUriResolvers.forZipFileUsingCatalog(new ZipFile(zipFile), catalog)

    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      UriResolvers.fromPartialUriResolversWithFallback(
        Vector(zipFilePartialResolver, xbrlAndW3UriPartialResolver)))
  }

  private val testCaseUri: URI = {
    val relativeUri = URI.create("200-xbrldie/270-DefaultValueExamples/270-DefaultValueExamples.xml")
    dummyUriPrefix.resolve(relativeUri)
  }

  private val xbrldieNs = "http://xbrl.org/2005/xbrldi/errors"
  private val confNs = "http://xbrl.org/2005/conformance"

  private val ValidationErrorEName = EName(xbrldieNs, "PrimaryItemDimensionallyInvalidError")

  private val testCaseDocElem: BackingNodes.Elem = {
    docBuilder.build(testCaseUri).documentElement.ensuring(_.resolvedName == EName(confNs, "testcase")).
      ensuring(_.filterElems(_.resolvedName == EName(confNs, "error")).forall(_.textAsResolvedQName == ValidationErrorEName))
  }

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

      val outputFileElems = fileNames.map { fileName =>
        val outputFileUri: URI = testCaseUri.resolve(s"out/$fileName")
        val outputFileElem = docBuilder.build(outputFileUri).documentElement

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
    id:                           String,
    name:                         String,
    instanceFileName:             String,
    expectedSuccessCount:         Int,
    expectedValidationErrorCount: Int): Unit = {

    val instance = makeTestInstance(
      "200-xbrldie/270-DefaultValueExamples/" + instanceFileName)

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
    val entryPointHrefs =
      xbrlInstance.findAllSchemaRefs.map(_.resolvedHref) ++ xbrlInstance.findAllLinkbaseRefs.map(_.resolvedHref)

    doMakeValidator(
      entryPointHrefs.toSet.filterNot(Set(URI.create("http://www.xbrl.org/2006/xbrldi-2006.xsd"))),
      doResolveProhibitionAndOverriding)
  }

  private def makeTestInstance(relativeDocPath: String): XbrlInstance = {
    // We expect no spaces in the path, so we can create a relative URI from it.

    val uri = dummyUriPrefix.resolve(relativeDocPath)

    XbrlInstance.build(docBuilder.build(uri).documentElement)
  }

  private def doMakeValidator(entryPointUris: Set[URI], doResolveProhibitionAndOverriding: Boolean): DimensionalValidator = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    val basicTaxo = taxoBuilder.build(entryPointUris)
    val effectiveTaxo =
      if (doResolveProhibitionAndOverriding) {
        basicTaxo.resolveProhibitionAndOverriding(relationshipFactory)
      } else {
        basicTaxo
      }

    DimensionalValidator.build(effectiveTaxo)
  }
}
