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

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ClarkElemApi
import eu.cdevreeze.yaidom.queryapi.ClarkElemLike

// TODO Add node super-type and text node type as well, and have the element type offer the BackingNodes.Elem API
// TODO Then there is a children field for all text and element nodes that are child nodes
// TODO Use minimalScope instead of attribute and element text transformations
// TODO Must SchemaContentElement store the SchemaContentElement children?
// TODO Analogous to SchemaContentBackingElem, create ResourceContentBackingElem and the corresponding node and text types
// TODO Analogous to SchemaContentElement, create ResourceContentElement (backed by ResourceContentBackingElem) for resource descendants
// TODO Can we then generalize over SchemaContentBackingElem and ResourceContentBackingElem into, say, MiniDialectElem?
// TODO ResourceContentElement keeps as "ancestry state" the ELR (?) and link element (?) (so anything outside a resource is not modeled)
// TODO Then ResourceNode contains ResourceContentElement child elements; other than that, relationships and nodes are independent of the rest
// TODO Alternatively, no XML for resource content, but make (extensible) model for that (?)
// TODO Should ElementKey become just one case class containing an optional (!) document URI plus required ID?
// TODO Or should SchemaContentElementKey contain optional TNS plus required ID, and LinkContentKey contain ELR, link name and ID?
// TODO What does this mean in terms of (uniqueness) assumptions about the taxonomy?
// TODO Can we define a friendly "case class-like" ("low ancestry context") XML format that ties all this together?

/**
 * XML element in a schema, aware of the target namespace, if any. It is a custom yaidom element
 * implementation for schema content offering the ClarkElemApi query API. It is not a "yaidom dialect", but
 * it is a backing element for such a dialect, namely SchemaContentElement.
 *
 * Note that these elements are easy to create on the fly, which is by design. The downside is that they
 * do not know much of their ancestry, not even whether they are top-level root children or not.
 *
 * The attributes that in the original XML are QName-valued are here resolved ones and therefore EName-valued.
 * Make sure that this is indeed the case, because these elements contain no in-scope namespaces!
 * The same is true for element text!
 *
 * Mixed element content is not supported. Either the element contains text content, or it contains child
 * elements, but not both.
 *
 * @author Chris de Vreeze
 */
final case class SchemaContentBackingElem(
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

  // TODO We should also transform typedDomainRefs to ENames, but cannot do that without any context!

  def fromSchemaRootElem(elem: BackingNodes.Elem): SchemaContentBackingElem = {
    require(elem.resolvedName == ENames.XsSchemaEName, s"Expected ${ENames.XsSchemaEName} but got ${elem.resolvedName}")

    val tnsOption: Option[String] = elem.attributeOption(ENames.TargetNamespaceEName)

    from(tnsOption, elem)
  }

  private def from(
    tnsOption: Option[String],
    elem: BackingNodes.Elem): SchemaContentBackingElem = {

    // Recursive calls

    val childElems = elem.findAllChildElems.map(e => from(tnsOption, e))

    SchemaContentBackingElem(
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
