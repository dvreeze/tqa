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

package eu.cdevreeze.tqa.relationship

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.dom.UriAwareTaxonomy
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Relationship extraction test case. It uses test data from the XBRL Core Conformance Suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class ExtractRelationshipsTest extends FunSuite {

  test("testExtractRelationshipsInEmbeddedLinkbase") {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val docParser = DocumentParserUsingStax.newInstance()

    val docUri = classOf[ExtractRelationshipsTest].getResource("292-00-Embeddedlinkbaseinthexsd.xsd").toURI

    val doc = indexed.Document(docParser.parse(docUri).withUriOption(Some(docUri)))

    val xsdSchema = XsdSchema.build(doc.documentElement)

    val tns = "http://www.UBmatrix.com/Patterns/BasicCalculation"

    val taxo = UriAwareTaxonomy.build(Vector(xsdSchema))

    val relationshipsFactory = DefaultRelationshipsFactory.LenientInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val parentChildRelationshipParents: Set[EName] =
      (relationships collect { case rel: ParentChildRelationship => rel.sourceConceptEName }).toSet

    assertResult(Set(EName(tns, "PropertyPlantEquipment"))) {
      parentChildRelationshipParents
    }

    val parentChildRelationshipChildren: Set[EName] =
      (relationships collect { case rel: ParentChildRelationship => rel.targetConceptEName }).toSet

    assertResult(
      Set(
        EName(tns, "Building"),
        EName(tns, "ComputerEquipment"),
        EName(tns, "FurnitureFixtures"),
        EName(tns, "Land"),
        EName(tns, "Other"),
        EName(tns, "TotalPropertyPlantEquipment"))) {

        parentChildRelationshipChildren
      }

    assertResult(parentChildRelationshipChildren) {
      (relationships collect { case rel: ParentChildRelationship => rel.targetGlobalElementDeclaration.targetEName }).toSet
    }
  }
}
