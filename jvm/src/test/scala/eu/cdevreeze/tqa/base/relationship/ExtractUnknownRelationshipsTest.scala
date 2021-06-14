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

package eu.cdevreeze.tqa.base.relationship

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.ENames.NameEName
import eu.cdevreeze.tqa.ENames.RefEName
import eu.cdevreeze.tqa.ENames.XsElementEName
import eu.cdevreeze.tqa.base.dom.ConceptLabelResource
import eu.cdevreeze.tqa.base.dom.OtherElemInXsdNamespace
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.simple

/**
 * Unknown relationship extraction test case. It uses test data from the XBRL Core Conformance Suite, but adapted
 * in order to invalidate the taxonomy and trigger extraction of unknown relationships.
 *
 * The point made by this test case is that taxonomy data may be invalid (or unknown) without breaking.
 *
 * @author Chris de Vreeze
 */
class ExtractUnknownRelationshipsTest extends AnyFunSuite {

  test("testExtractUnknownRelationships") {
    // Using a simple linkbase and schema from the XBRL Core Conformance Suite, but adapted.

    val docBuilder = getDocumentBuilder()

    val xsdDocUri = URI.create("file:///conf-suite/Common/200-linkbase/202-01-HrefResolution.xsd")
    val linkbaseDocUri = URI.create("file:///conf-suite/Common/200-linkbase/202-01-HrefResolution-label.xml")

    val parsedSchemaDocElem = docBuilder.build(xsdDocUri).documentElement

    val editedSchemaDocSimpleElem =
      parsedSchemaDocElem.underlyingElem transformElemsOrSelf {
        case e if e.attributeOption(NameEName).contains("changeInRetainedEarnings") =>
          e.plusAttribute(QName("ref"), "tns:changeInRetainedEarnings")
        case e =>
          e
      }
    val xsdBackingDoc =
      indexed.Document.from(simple.Document(Some(parsedSchemaDocElem.docUri), editedSchemaDocSimpleElem))

    val linkbaseBackingDoc = docBuilder.build(linkbaseDocUri)

    val xsdSchemaDoc = TaxonomyDocument.build(xsdBackingDoc)
    val linkbaseDoc = TaxonomyDocument.build(linkbaseBackingDoc)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchemaDoc, linkbaseDoc))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val conceptLabelRelationships = relationships.collect { case rel: ConceptLabelRelationship => rel }
    val unknownRelationships = relationships.collect { case rel: UnknownRelationship => rel }

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
      unknownRelationships.head.sourceElem.isInstanceOf[OtherElemInXsdNamespace]
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

  private val zipFile: ZipFile = {
    val uri = classOf[ExtractUnknownRelationshipsTest].getResource("/XBRL-CONF-2014-12-10.zip").toURI
    new ZipFile(new File(uri))
  }

  private def getDocumentBuilder(): IndexedDocumentBuilder = {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalog: SimpleCatalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite("file:///conf-suite/", "XBRL-CONF-2014-12-10/")))

    val uriResolver = UriResolvers.forZipFileUsingCatalogWithFallback(zipFile, catalog)

    IndexedDocumentBuilder(docParser, uriResolver)
  }
}
