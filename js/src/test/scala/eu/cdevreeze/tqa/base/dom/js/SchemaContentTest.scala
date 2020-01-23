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

package eu.cdevreeze.tqa.base.dom.js

import scala.reflect.classTag

import org.scalatest.funsuite.AnyFunSuite

import org.scalajs.dom.{ raw => sjsdom }

import eu.cdevreeze.tqa.ENames.TypeEName
import eu.cdevreeze.tqa.Namespaces.XsNamespace
import eu.cdevreeze.tqa.base.dom._
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.jsdom.JsDomDocument
import eu.cdevreeze.yaidom.jsdom.JsDomElem
import org.scalajs.dom.experimental.domparser.DOMParser
import org.scalajs.dom.experimental.domparser.SupportedType

/**
 * Schema content test case.
 *
 * @author Chris de Vreeze
 */
class SchemaContentTest extends AnyFunSuite {

  test("testSchemaContent") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(shiporderXsdString, SupportedType.`text/xml`))

    val rootElem: JsDomElem = domDoc.documentElement

    val xsdSchema = XsdSchema.build(rootElem)

    val schemas = xsdSchema.findAllElemsOrSelfOfType(classTag[XsdSchema])

    assertResult(Set(xsdSchema)) {
      schemas.toSet
    }

    val globalElemDecls = xsdSchema.findAllGlobalElementDeclarations

    assertResult(1) {
      globalElemDecls.size
    }

    assertResult(Set(EName("shiporder"))) {
      xsdSchema.filterGlobalElementDeclarations(_.targetEName.namespaceUriOption.isEmpty).map(_.targetEName).toSet
    }
    assertResult(globalElemDecls) {
      xsdSchema.filterGlobalElementDeclarations(_.targetEName.namespaceUriOption.isEmpty)
    }

    val globalElemDecl = globalElemDecls.head

    assertResult(List(EName("orderid"))) {
      globalElemDecl.filterElemsOfType(classTag[LocalAttributeDeclaration])(_.nameAttributeValue == "orderid").map(decl => EName(decl.nameAttributeValue))
    }

    val attrDecl = xsdSchema.findElemOfType(classTag[AttributeDeclaration])(_.nameAttributeValue == "orderid").head

    assertResult(Some(EName(XsNamespace, "string"))) {
      attrDecl.attributeAsResolvedQNameOption(TypeEName)
    }

    val topmostComplexTypeDefs = xsdSchema.findTopmostElemsOfType(classTag[AnonymousComplexTypeDefinition])(_ => true)

    assertResult(1) {
      topmostComplexTypeDefs.size
    }

    val topmostComplexTypeDef = topmostComplexTypeDefs.head

    assertResult(Some(underlyingElem(globalElemDecl))) {
      topmostComplexTypeDef.backingElem.parentOption.map(e => underlyingElem(TaxonomyElem.build(e)))
    }

    val sequenceModelGroupOption = xsdSchema.findElemOfType(classTag[SequenceModelGroup])(_ => true)

    assertResult(true) {
      sequenceModelGroupOption.isDefined
    }

    assertResult(Some(underlyingElem(topmostComplexTypeDef))) {
      sequenceModelGroupOption.get.backingElem.parentOption.map(e => underlyingElem(TaxonomyElem.build(e)))
    }
  }

  // This function is only needed as long as the JS DOM wrapper has no good equality!
  private def underlyingElem(taxoElem: TaxonomyElem): sjsdom.Element = {
    taxoElem.backingElem.asInstanceOf[JsDomElem].wrappedNode
  }

  private val shiporderXsdString =
    """<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xs:element name="shiporder">
  <xs:complexType>
    <xs:sequence>
      <xs:element name="orderperson" type="xs:string"/>
      <xs:element name="shipto">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="name" type="xs:string"/>
            <xs:element name="address" type="xs:string"/>
            <xs:element name="city" type="xs:string"/>
            <xs:element name="country" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
      <xs:element name="item" maxOccurs="unbounded">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="title" type="xs:string"/>
            <xs:element name="note" type="xs:string" minOccurs="0"/>
            <xs:element name="quantity" type="xs:positiveInteger"/>
            <xs:element name="price" type="xs:decimal"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:sequence>
    <xs:attribute name="orderid" type="xs:string" use="required"/>
  </xs:complexType>
</xs:element>

</xs:schema>"""
}
