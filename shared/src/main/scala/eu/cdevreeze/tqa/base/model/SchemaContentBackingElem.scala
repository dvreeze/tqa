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

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ClarkElemApi
import eu.cdevreeze.yaidom.queryapi.ClarkElemLike

/**
 * XML element in a schema, aware of the target namespace, if any, and of the document URI.
 *
 * The attributes that in the original XML are QName-valued are here resolved ones and therefore EName-valued.
 * Make sure that this is indeed the case, because these elements contain no in-scope namespaces!
 * The same is true for element text!
 *
 * @author Chris de Vreeze
 */
final case class SchemaContentBackingElem(
  docUri: URI,
  targetNamespaceOption: Option[String],
  resolvedName: EName,
  attributes: Map[EName, String],
  text: String,
  childElems: immutable.IndexedSeq[SchemaContentBackingElem]) extends ClarkElemApi with ClarkElemLike {

  type ThisElem = SchemaContentBackingElem

  // Methods needed for completing the ClarkElemApi API

  def thisElem: SchemaContentBackingElem = this

  def findAllChildElems: immutable.IndexedSeq[SchemaContentBackingElem] = childElems

  def resolvedAttributes: Map[EName, String] = attributes
}

object SchemaContentBackingElem {

  def fromSchemaRootElem(elem: BackingNodes.Elem): SchemaContentBackingElem = {
    require(elem.resolvedName == ENames.XsSchemaEName, s"Expected ${ENames.XsSchemaEName} but got ${elem.resolvedName}")

    val tnsOption: Option[String] = elem.attributeOption(ENames.TargetNamespaceEName)

    from(elem.docUri, tnsOption, elem)
  }

  private def from(docUri: URI, tnsOption: Option[String], elem: BackingNodes.Elem): SchemaContentBackingElem = {
    // Recursive calls

    val childElems = elem.findAllChildElems.map(e => from(docUri, tnsOption, e))

    SchemaContentBackingElem(
      docUri,
      tnsOption,
      elem.resolvedName,
      transformAttributes(elem.resolvedName, elem.resolvedAttributes.toMap, elem.scope),
      transformText(elem.resolvedName, elem.text, elem.scope),
      childElems)
  }

  private def transformAttributes(elemName: EName, attrs: Map[EName, String], scope: Scope): Map[EName, String] = {
    val editedAttributes = attrs.filterKeys(qnameValuedAttributes.getOrElse(elemName, Set.empty))
      .mapValues(v => scope.resolveQName(QName(v)).toString)

    attrs ++ editedAttributes
  }

  private def transformText(elemName: EName, txt: String, scope: Scope): String = {
    if (qnameValuedElems.contains(elemName)) {
      scope.resolveQName(QName(txt)).toString
    } else {
      txt
    }
  }

  private val qnameValuedAttributes: Map[EName, Set[EName]] = {
    import ENames._

    Map(
      XsElementEName -> Set(RefEName, SubstitutionGroupEName, TypeEName),
      XsAttributeEName -> Set(RefEName, TypeEName),
      XsGroupEName -> Set(RefEName),
      XsAttributeGroupEName -> Set(RefEName),
      XsRestrictionEName -> Set(BaseEName),
      XsExtensionEName -> Set(BaseEName),
      XsKeyrefEName -> Set(ReferEName))
  }

  private val qnameValuedElems: Set[EName] = {
    import ENames._

    Set(LinkUsedOnEName)
  }
}
