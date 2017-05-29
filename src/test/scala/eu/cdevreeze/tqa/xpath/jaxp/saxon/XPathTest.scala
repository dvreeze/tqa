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

package eu.cdevreeze.tqa.xpath.jaxp.saxon

import java.io.File
import java.net.URI

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonNode
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import javax.xml.xpath.XPathFunction
import javax.xml.xpath.XPathFunctionResolver
import javax.xml.xpath.XPathVariableResolver
import net.sf.saxon.om.NodeInfo
import net.sf.saxon.s9api.Processor

/**
 * XPath test case using JAXP backed by Saxon.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class XPathTest extends FunSuite {

  private val processor = new Processor(false)

  private val MyFuncNamespace = "http://example.com/xbrl-xpath-functions"
  private val MyVarNamespace = "http://example.com/xbrl-xpath-variables"

  private val rootDir = new File(classOf[XPathTest].getResource("/taxonomies").toURI)

  private val instanceDir = new File(classOf[XPathTest].getResource("/sample-instances").toURI)
  private val docUri = (new File(instanceDir, "sample-xbrl-instance.xml")).toURI

  private val docBuilder = getDocumentBuilder(rootDir)

  private val rootElem: SaxonElem = docBuilder.build(docUri)

  private val xpathEvaluatorFactory =
    JaxpXPathEvaluatorFactoryUsingSaxon.newInstance(processor.getUnderlyingConfiguration)

  xpathEvaluatorFactory.underlyingEvaluatorFactory.setXPathFunctionResolver(new XPathFunctionResolver {

    def resolveFunction(functionName: javax.xml.namespace.QName, arity: Int): XPathFunction = {
      if (arity == 1 && (functionName == EName(MyFuncNamespace, "contexts").toJavaQName(None))) {
        new FindAllXbrliContexts
      } else {
        sys.error(s"Unknown function with name $functionName and arity $arity")
      }
    }
  })

  xpathEvaluatorFactory.underlyingEvaluatorFactory.setXPathVariableResolver(new XPathVariableResolver {

    def resolveVariable(variableName: javax.xml.namespace.QName): AnyRef = {
      if (variableName == EName("contextPosition").toJavaQName(None)) {
        java.lang.Integer.valueOf(4)
      } else if (variableName == EName(MyVarNamespace, "contextPosition").toJavaQName(None)) {
        java.lang.Integer.valueOf(4)
      } else {
        sys.error(s"Unknown variable with name $variableName")
      }
    }
  })

  private val xpathEvaluator =
    JaxpXPathEvaluatorUsingSaxon.newInstance(
      xpathEvaluatorFactory,
      rootElem.docUri,
      rootElem.scope ++ JaxpXPathEvaluatorUsingSaxon.MinimalScope ++
        Scope.from("myfun" -> MyFuncNamespace, "myvar" -> MyVarNamespace),
      new SimpleUriResolver(u => uriToLocalUri(u, rootDir)))

  test("testSimpleStringXPathWithoutContextItem") {
    val exprString = "string(count((1, 2, 3, 4, 5)))"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsString(expr, None)

    assertResult("5") {
      result
    }
  }

  test("testSimpleNumberXPathWithoutContextItem") {
    val exprString = "count((1, 2, 3, 4, 5))"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBigDecimal(expr, None)

    assertResult(5) {
      result.toInt
    }
  }

  test("testSimpleBooleanXPathWithoutContextItem") {
    val exprString = "empty((1, 2, 3, 4, 5))"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBoolean(expr, None)

    assertResult(false) {
      result
    }
  }

  test("testSimpleENameXPathWithoutContextItem") {
    val exprString = "xs:QName('xbrli:item')"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsEName(expr, None)

    assertResult(ENames.XbrliItemEName) {
      result
    }
  }

  test("testLoopingXPathWithoutContextItem") {
    val exprString = "max(for $i in (1 to 5) return $i * 2)"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBigDecimal(expr, None)

    assertResult(BigDecimal("10")) {
      result
    }
  }

  test("testSimpleNodeXPath") {
    val exprString = "//xbrli:context[1]/xbrli:entity/xbrli:segment/xbrldi:explicitMember[1]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNode(expr, Some(rootElem.wrappedNode))

    assertResult("gaap:ABCCompanyDomain") {
      SaxonNode.wrapElement(result.asInstanceOf[NodeInfo]).text.trim
    }
  }

  test("testSimpleNodeSeqXPath") {
    val exprString = "//xbrli:context/xbrli:entity/xbrli:segment/xbrldi:explicitMember"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNodeSeq(expr, Some(rootElem.wrappedNode))

    assertResult(true) {
      result.size > 100
    }
  }

  test("testYaidomQueryOnXPathNodeResults") {
    val exprString = "//xbrli:context/xbrli:entity/xbrli:segment/xbrldi:explicitMember"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNodeSeq(expr, Some(rootElem.wrappedNode))

    // Use yaidom query API on results

    val resultElems = result.map(e => SaxonNode.wrapElement(e))

    assertResult(true) {
      val someDimQNames =
        Set(QName("gaap:EntityAxis"), QName("gaap:VerificationAxis"), QName("gaap:PremiseAxis"), QName("gaap:ShareOwnershipPlanIdentifierAxis"))

      val someDimENames = someDimQNames.map(qn => rootElem.scope.resolveQNameOption(qn).get)

      val foundDimensions =
        resultElems.flatMap(_.attributeAsResolvedQNameOption(ENames.DimensionEName)).toSet

      someDimENames.subsetOf(foundDimensions)
    }

    // The Paths are not lost!

    val resultElemPaths = resultElems.map(_.path)

    assertResult(Set(List("context", "entity", "segment", "explicitMember"))) {
      resultElemPaths.map(_.entries.map(_.elementName.localPart)).toSet
    }
    assertResult(Set(EName(Namespaces.XbrliNamespace, "xbrl"))) {
      resultElems.map(_.rootElem.resolvedName).toSet
    }

    assertResult(resultElems) {
      resultElems.map(e => e.rootElem.getElemOrSelfByPath(e.path))
    }
  }

  test("testSimpleBackingElemXPath") {
    val exprString = "//xbrli:context[1]/xbrli:entity/xbrli:segment/xbrldi:explicitMember[1]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val resultElem = xpathEvaluator.evaluateAsBackingElem(expr, Some(rootElem.wrappedNode))

    assertResult("gaap:ABCCompanyDomain") {
      resultElem.text.trim
    }
  }

  test("testSimpleBackingElemSeqXPath") {
    val exprString = "//xbrli:context/xbrli:entity/xbrli:segment/xbrldi:explicitMember"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val resultElems = xpathEvaluator.evaluateAsBackingElemSeq(expr, Some(rootElem.wrappedNode))

    assertResult(true) {
      resultElems.size > 100
    }
  }

  test("testYaidomQueryOnXPathBackingElemResults") {
    val exprString = "//xbrli:context/xbrli:entity/xbrli:segment/xbrldi:explicitMember"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val resultElems = xpathEvaluator.evaluateAsBackingElemSeq(expr, Some(rootElem.wrappedNode))

    // Use yaidom query API on results

    assertResult(true) {
      val someDimQNames =
        Set(QName("gaap:EntityAxis"), QName("gaap:VerificationAxis"), QName("gaap:PremiseAxis"), QName("gaap:ShareOwnershipPlanIdentifierAxis"))

      val someDimENames = someDimQNames.map(qn => rootElem.scope.resolveQNameOption(qn).get)

      val foundDimensions =
        resultElems.flatMap(_.attributeAsResolvedQNameOption(ENames.DimensionEName)).toSet

      someDimENames.subsetOf(foundDimensions)
    }

    // The Paths are not lost!

    val resultElemPaths = resultElems.map(_.path)

    assertResult(Set(List("context", "entity", "segment", "explicitMember"))) {
      resultElemPaths.map(_.entries.map(_.elementName.localPart)).toSet
    }
    assertResult(Set(EName(Namespaces.XbrliNamespace, "xbrl"))) {
      resultElems.map(_.rootElem.resolvedName).toSet
    }

    assertResult(resultElems) {
      resultElems.map(e => e.rootElem.getElemOrSelfByPath(e.path))
    }
  }

  test("testBaseUri") {
    val exprString = "base-uri(/xbrli:xbrl)"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val resultAsString = xpathEvaluator.evaluateAsString(expr, Some(rootElem.wrappedNode))
    val result = URI.create(resultAsString)

    assertResult(true) {
      resultAsString.contains("sample-instances") && resultAsString.contains("sample-xbrl-instance.xml")
    }
    assertResult(result) {
      rootElem.baseUri
    }
  }

  test("testDocFunction") {
    val exprString =
      "doc('http://www.nltaxonomie.nl/nt11/kvk/20170419/presentation/kvk-balance-sheet-education-pre.xml')//link:presentationLink[1]/link:loc[10]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNode(expr, Some(rootElem.wrappedNode))

    val resultElem = SaxonNode.wrapElement(result.asInstanceOf[NodeInfo])

    assertResult(Some("urn:kvk:linkrole:balance-sheet-education")) {
      // Getting parent element, to make the example more exciting
      resultElem.parent.attributeOption(ENames.XLinkRoleEName)
    }
  }

  test("testCustomFunction") {
    val exprString = "myfun:contexts(.)[4]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNode(expr, Some(rootElem.wrappedNode))

    val resultElem = SaxonNode.wrapElement(result.asInstanceOf[NodeInfo])

    assertResult(EName(Namespaces.XbrliNamespace, "context")) {
      resultElem.resolvedName
    }
    assertResult(Some("I-2005")) {
      resultElem.attributeOption(ENames.IdEName)
    }
  }

  test("testCustomFunctionAndVariable") {
    val exprString = "myfun:contexts(.)[$contextPosition]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNode(expr, Some(rootElem.wrappedNode))

    val resultElem = SaxonNode.wrapElement(result.asInstanceOf[NodeInfo])

    assertResult(EName(Namespaces.XbrliNamespace, "context")) {
      resultElem.resolvedName
    }
    assertResult(Some("I-2005")) {
      resultElem.attributeOption(ENames.IdEName)
    }
  }

  test("testCustomFunctionAndPrefixedVariable") {
    val exprString = "myfun:contexts(.)[$myvar:contextPosition]"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsNode(expr, Some(rootElem.wrappedNode))

    val resultElem = SaxonNode.wrapElement(result.asInstanceOf[NodeInfo])

    assertResult(EName(Namespaces.XbrliNamespace, "context")) {
      resultElem.resolvedName
    }
    assertResult(Some("I-2005")) {
      resultElem.attributeOption(ENames.IdEName)
    }
  }

  test("testInstanceOfElement") {
    val exprString = ". instance of element()"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBoolean(expr, Some(rootElem.wrappedNode))

    assertResult(true) {
      result
    }
  }

  test("testNotInstanceOfElement") {
    val exprString = "3 instance of element()"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBoolean(expr, None)

    assertResult(false) {
      result
    }
  }

  test("testInstanceOfElementSeq") {
    val exprString = "myfun:contexts(.) instance of element()+"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBoolean(expr, Some(rootElem.wrappedNode))

    assertResult(true) {
      result
    }
  }

  test("testSumOfEmptySeq") {
    val exprString = "sum(())"

    val expr = xpathEvaluator.toXPathExpression(exprString)
    val result = xpathEvaluator.evaluateAsBigDecimal(expr, None)

    assertResult(0) {
      result.toInt
    }
  }

  private def uriToLocalUri(uri: URI, rootDir: File): URI = {
    // Not robust
    val relativePath = uri.getScheme match {
      case "http"  => uri.toString.drop("http://".size)
      case "https" => uri.toString.drop("https://".size)
      case _       => sys.error(s"Unexpected URI $uri")
    }

    val f = new File(rootDir, relativePath.dropWhile(_ == '/'))
    f.toURI
  }

  private def getDocumentBuilder(rootDir: File): SaxonDocumentBuilder = {
    new SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      { u => if (u.getScheme == "file") u else uriToLocalUri(u, rootDir) })
  }
}
