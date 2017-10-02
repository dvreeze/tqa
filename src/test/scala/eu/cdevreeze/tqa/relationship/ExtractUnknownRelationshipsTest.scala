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

import eu.cdevreeze.tqa.ENames.NameEName
import eu.cdevreeze.tqa.ENames.RefEName
import eu.cdevreeze.tqa.ENames.XsElementEName
import eu.cdevreeze.tqa.dom.ConceptLabelResource
import eu.cdevreeze.tqa.dom.Linkbase
import eu.cdevreeze.tqa.dom.OtherXsdElem
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Unknown relationship extraction test case. It uses test data from the XBRL Core Conformance Suite, but adapted
 * in order to invalidate the taxonomy and trigger extraction of unknown relationships.
 *
 * The point made by this test case is that taxonomy data may be invalid (or unknown) without breaking.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class ExtractUnknownRelationshipsTest extends FunSuite {

  test("testExtractUnknownRelationships") {
    // Using a simple linkbase and schema from the XBRL Core Conformance Suite, but adapted.

    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[ExtractUnknownRelationshipsTest].getResource("/conf-suite/Common/200-linkbase/202-01-HrefResolution.xsd").toURI
    val linkbaseDocUri = classOf[ExtractUnknownRelationshipsTest].getResource("/conf-suite/Common/200-linkbase/202-01-HrefResolution-label.xml").toURI

    val parsedSchemaDoc = docParser.parse(xsdDocUri)

    val editedSchemaDoc =
      parsedSchemaDoc transformElemsOrSelf {
        case e if e.attributeOption(NameEName).contains("changeInRetainedEarnings") =>
          e.plusAttribute(QName("ref"), "tns:changeInRetainedEarnings")
        case e =>
          e
      }

    val xsdDoc = indexed.Document(editedSchemaDoc.withUriOption(Some(xsdDocUri)))
    val linkbaseDoc = indexed.Document(docParser.parse(linkbaseDocUri).withUriOption(Some(linkbaseDocUri)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase = Linkbase.build(linkbaseDoc.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val conceptLabelRelationships = relationships collect { case rel: ConceptLabelRelationship => rel }
    val unknownRelationships = relationships collect { case rel: UnknownRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(1) {
      conceptLabelRelationships.size
    }
    assertResult(1) {
      unknownRelationships.size
    }

    assertResult(Set((EName(tns, "fixedAssets"), "Fixed Assets"))) {
      conceptLabelRelationships.map(rel => (rel.sourceConceptEName, rel.labelText)).toSet
    }

    assertResult(true) {
      unknownRelationships.head.sourceElem.isInstanceOf[OtherXsdElem]
    }
    assertResult(XsElementEName) {
      unknownRelationships.head.sourceElem.resolvedName
    }
    assertResult(true) {
      unknownRelationships.head.sourceElem.attributeOption(NameEName).isDefined &&
        unknownRelationships.head.sourceElem.attributeOption(RefEName).isDefined
    }

    assertResult(true) {
      unknownRelationships.head.targetElem.isInstanceOf[ConceptLabelResource]
    }
  }
}
