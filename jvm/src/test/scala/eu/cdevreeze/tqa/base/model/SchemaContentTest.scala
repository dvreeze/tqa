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

package eu.cdevreeze.tqa.base.model

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

    val backingElem = SchemaContentBackingElem.fromSchemaRootElem(doc.documentElement)
    val schemaContentElems = SchemaContentElement.collectSchemaContent(backingElem)

    val globalElemDecls = schemaContentElems.collect { case e: GlobalElementDeclaration => e }

    assertResult(1) {
      globalElemDecls.size
    }

    assertResult(Set(EName("shiporder"))) {
      globalElemDecls.filter(_.targetEName.namespaceUriOption.isEmpty).map(_.targetEName).toSet
    }
    assertResult(globalElemDecls) {
      globalElemDecls.filter(_.targetEName.namespaceUriOption.isEmpty)
    }

    val globalElemDecl = globalElemDecls.head

    assertResult(List(EName("orderid"))) {
      globalElemDecl.filterElemsOfType(classTag[LocalAttributeDeclaration])(_.nameAttributeValue == "orderid")
        .map(decl => EName(decl.nameAttributeValue))
    }

    val attrDecl =
      schemaContentElems.flatMap(_.findElemOfType(classTag[AttributeDeclaration])(_.nameAttributeValue == "orderid")).head

    assertResult(Some(EName(XsNamespace, "string"))) {
      attrDecl.attributes.get(TypeEName).map(v => EName(v))
    }

    val topmostComplexTypeDefs =
      schemaContentElems.flatMap(_.findTopmostElemsOrSelfOfType(classTag[AnonymousComplexTypeDefinition])(_ => true))

    assertResult(1) {
      topmostComplexTypeDefs.size
    }

    val topmostComplexTypeDef = topmostComplexTypeDefs.head

    val sequenceModelGroupOption =
      schemaContentElems.flatMap(_.findElemOfType(classTag[SequenceModelGroup])(_ => true)).headOption

    assertResult(true) {
      sequenceModelGroupOption.isDefined
    }

    assertResult(sequenceModelGroupOption) {
      topmostComplexTypeDef.findAllChildElemsOfType(classTag[SequenceModelGroup]).headOption
    }
  }
}
