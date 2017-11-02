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

import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.TypeEName
import eu.cdevreeze.tqa.Namespaces.XsNamespace
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Schema content test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class SchemaContentTest extends FunSuite {

  test("testSchemaContent") {

    val docParser = DocumentParserUsingStax.newInstance()

    val docUri = classOf[SchemaContentTest].getResource("shiporder.xsd").toURI

    val doc = indexed.Document(docParser.parse(docUri).withUriOption(Some(docUri)))

    val xsdSchema = XsdSchema.build(doc.documentElement)

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

    assertResult(Some(globalElemDecl)) {
      topmostComplexTypeDef.backingElem.parentOption.map(e => TaxonomyElem.build(e))
    }

    val sequenceModelGroupOption = xsdSchema.findElemOfType(classTag[SequenceModelGroup])(_ => true)

    assertResult(true) {
      sequenceModelGroupOption.isDefined
    }

    assertResult(Some(topmostComplexTypeDef)) {
      sequenceModelGroupOption.get.backingElem.parentOption.map(e => TaxonomyElem.build(e))
    }
  }
}
