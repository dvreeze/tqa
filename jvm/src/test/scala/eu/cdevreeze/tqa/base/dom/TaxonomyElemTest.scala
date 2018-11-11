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

package eu.cdevreeze.tqa.base.dom

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.Namespaces.XbrliNamespace
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.simple

/**
 * Taxonomy test case. It uses test data from the XBRL Core Conformance Suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyElemTest extends FunSuite {

  test("testParseSchemaWithEmbeddedLinkbase") {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val docBuilder = getDocumentBuilder()

    val docUri = URI.create("file:///conf-suite/Common/200-linkbase/292-00-Embeddedlinkbaseinthexsd.xsd")

    val rootElem = docBuilder.build(docUri).documentElement

    val xsdSchema = XsdSchema.build(rootElem)

    doTestParseSchemaWithEmbeddedLinkbase(xsdSchema)
  }

  test("testParseNonTaxonomyContent") {
    // Using a test overview file, which is not taxonomy content.

    val docBuilder = getDocumentBuilder()

    val docUri = URI.create("file:///conf-suite/xbrl.xml")

    val rootElem = docBuilder.build(docUri).documentElement

    val taxoElem = TaxonomyElem.build(rootElem)

    assertResult(Set(EName("testcases"), EName("testcase"))) {
      val otherElems = taxoElem.findAllElemsOrSelfOfType(classTag[OtherNonXLinkElem])

      otherElems.map(_.resolvedName).toSet
    }
  }

  test("testParseWrappedSchemaWithEmbeddedLinkbase") {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val docBuilder = getDocumentBuilder()

    val docUri = URI.create("file:///conf-suite/Common/200-linkbase/292-00-Embeddedlinkbaseinthexsd.xsd")

    val rootElem = docBuilder.build(docUri).documentElement

    val unwrappedElem = TaxonomyElem.build(rootElem)

    val taxoElem: TaxonomyElem = addWrapperRootElem(unwrappedElem, emptyWrapperElem)
    val xsdSchema: XsdSchema = taxoElem.findChildElemOfType(classTag[XsdSchema])(_ => true).get

    doTestParseSchemaWithEmbeddedLinkbase(xsdSchema)
  }

  private def doTestParseSchemaWithEmbeddedLinkbase(xsdSchema: XsdSchema): Unit = {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val embeddedLinks = xsdSchema.findAllElemsOfType(classTag[StandardExtendedLink])

    assertResult(3) {
      embeddedLinks.size
    }
    assertResult(true) {
      embeddedLinks(0).isInstanceOf[LabelLink]
    }
    assertResult(true) {
      embeddedLinks(1).isInstanceOf[PresentationLink]
    }
    assertResult(true) {
      embeddedLinks(2).isInstanceOf[CalculationLink]
    }

    assertResult(List("http://www.xbrl.org/2003/role/link")) {
      embeddedLinks.map(_.role).distinct
    }

    assertResult(List("http://www.xbrl.org/2003/role/link")) {
      embeddedLinks.flatMap(_.xlinkChildren.map(_.elr)).distinct
    }

    assertResult(1) {
      xsdSchema.findAllImports.size
    }

    val elemDecls = xsdSchema.findAllGlobalElementDeclarations

    assertResult(7) {
      elemDecls.size
    }

    val expectedTns = "http://www.UBmatrix.com/Patterns/BasicCalculation"

    assertResult(
      Set(
        EName(expectedTns, "Building"),
        EName(expectedTns, "ComputerEquipment"),
        EName(expectedTns, "FurnitureFixtures"),
        EName(expectedTns, "Land"),
        EName(expectedTns, "Other"),
        EName(expectedTns, "PropertyPlantEquipment"),
        EName(expectedTns, "TotalPropertyPlantEquipment"))) {

        elemDecls.map(_.targetEName).toSet
      }

    assertResult(Set(XbrliItemEName)) {
      elemDecls.flatMap(_.substitutionGroupOption).toSet
    }

    assertResult(Set(EName(XbrliNamespace, "stringItemType"), EName(XbrliNamespace, "monetaryItemType"))) {
      elemDecls.flatMap(_.typeOption).toSet
    }

    val conceptDeclBuilder = new ConceptDeclaration.Builder(SubstitutionGroupMap.Empty)
    val conceptDecls = elemDecls.flatMap(e => conceptDeclBuilder.optConceptDeclaration(e))

    assertResult(elemDecls) {
      conceptDecls.map(_.globalElementDeclaration)
    }

    assertResult(conceptDecls) {
      conceptDecls collect { case primaryItemDecl: PrimaryItemDeclaration => primaryItemDecl }
    }
  }

  private def addWrapperRootElem(rootElem: TaxonomyElem, emptyWrapperElem: simple.Elem): TaxonomyElem = {
    val simpleRootElem = simple.Elem.from(rootElem.backingElem)

    val wrapperElem = emptyWrapperElem.plusChild(simpleRootElem).notUndeclaringPrefixes(rootElem.scope)

    TaxonomyElem.build(indexed.Elem(wrapperElem))
  }

  private val emptyWrapperElem: simple.Elem = {
    simple.Node.emptyElem(QName("wrapperElem"), Scope.Empty)
  }

  private val zipFile: ZipFile = {
    val uri = classOf[TaxonomyElemTest].getResource("/XBRL-CONF-2014-12-10.zip").toURI
    new ZipFile(new File(uri))
  }

  private def getDocumentBuilder(): IndexedDocumentBuilder = {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalog: SimpleCatalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(None, "file:///conf-suite/", "XBRL-CONF-2014-12-10/")))

    val uriResolver = UriResolvers.forZipFileUsingCatalogWithFallback(zipFile, catalog)

    IndexedDocumentBuilder(docParser, uriResolver)
  }
}
