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

package eu.cdevreeze.tqa.dom

import scala.reflect.classTag
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.tqa.Namespaces._
import eu.cdevreeze.tqa.ENames._

/**
 * Taxonomy test case. It uses test data from the XBRL Core Conformance Suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyElemTest extends FunSuite {

  test("testParseSchemaWithEmbeddedLinkbase") {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val docParser = DocumentParserUsingStax.newInstance()

    val docUri = classOf[TaxonomyElemTest].getResource("292-00-Embeddedlinkbaseinthexsd.xsd").toURI

    val doc = indexed.Document(docParser.parse(docUri).withUriOption(Some(docUri)))

    val xsdSchema = XsdSchema.build(doc.documentElement)

    val embeddedLinks = xsdSchema.findAllElemsOfType(classTag[StandardLink])

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
      embeddedLinks.flatMap(_.roleOption).distinct
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
}
