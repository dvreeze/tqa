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

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.XPointer
import eu.cdevreeze.yaidom.core.EName

/**
 * Any node that can be a relationship source or target in the model. There are 2 immediate
 * sub-types: LocatorNode and ResourceNode (abstractions of XLink locators and XLink resources, respectively).
 *
 * A LocatorNode is a unique key to a taxonomy element, and not the taxonomy element itself. This decoupling
 * respects the fact that it is not known beforehand how relationships in taxonomies are traversed.
 * ResourceNodes, on the other hand, are the taxonomy elements themselves.
 *
 * @author Chris de Vreeze
 */
sealed trait Node

/**
 * Locator node. It holds a unique key to a taxonomy element. It is an abstraction of what in the XML
 * representation is an XLink locator, but then resolved as a key to a taxonomy element.
 *
 * TODO Do we need locators to resources?
 */
sealed trait LocatorNode extends Node

/**
 * Resource node. It is an abstraction of what in the XML representation is a taxonomy element that is
 * an XLink resource.
 */
sealed trait ResourceNode extends Node

object Node {

  /**
   * Key referring to a global element declaration. Typically the global element declaration is for a concept
   * (that is, item or tuple), but this is only known if we have enough context to determine the substitution
   * group inheritance hierarchy. It could also be a global element declaration for a typed dimension member.
   */
  final case class GlobalElementDecl(targetEName: EName) extends LocatorNode

  /**
   * Key referring to a role type definition
   */
  final case class RoleType(roleUri: String) extends LocatorNode

  /**
   * Key referring to an arcrole type definition
   */
  final case class ArcroleType(arcroleUri: String) extends LocatorNode

  /**
   * Key referring to a named type definition.
   */
  final case class NamedTypeDef(targetEName: EName) extends LocatorNode

  // TODO Key for enumeration value (XPointer-based?)

  /**
   * A (standard or non-standard) label or reference resource
   */
  sealed trait DocumentationResource extends ResourceNode {

    def docUri: URI

    def elr: String

    def roleOption: Option[String]

    def nonXLinkAttributes: Map[EName, String]

    final def langOption: Option[String] = {
      nonXLinkAttributes.get(ENames.XmlLangEName)
    }
  }

  sealed trait LabelResource extends DocumentationResource {

    def text: String
  }

  sealed trait ReferenceResource extends DocumentationResource {

    def parts: Map[EName, String]
  }

  sealed trait StandardDocumentationResource extends DocumentationResource

  sealed trait NonStandardDocumentationResource extends DocumentationResource

  final case class ConceptLabelResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends StandardDocumentationResource with LabelResource

  final case class ConceptReferenceResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    parts: Map[EName, String]) extends StandardDocumentationResource with ReferenceResource

  /**
   * An element label, named label:label (and not any resource in that substitution group).
   */
  final case class ElementLabelResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends NonStandardDocumentationResource with LabelResource

  /**
   * An element reference, named reference:reference (and not any resource in that substitution group).
   */
  final case class ElementReferenceResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    parts: Map[EName, String]) extends NonStandardDocumentationResource with ReferenceResource

  // TODO OtherNonStandardDocumentationResource

  /**
   * Any other LocatorNode. It contains an XPointer, and therefore carries no semantics in isolation.
   *
   * Note that only XPointers containing just an ID are stable in that they do not depend on the order of
   * elements in the document pointed to.
   */
  final case class OtherLocatorNode(docUri: URI, xpointer: XPointer) extends LocatorNode

  /**
   * Any other ResourceNode. For example, table content like table, ruleNode, or formula content like
   * formula:valueAssertion, or a custom resource node, such as sbr:linkroleOrder in Dutch taxonomies.
   *
   * It contains an XPointer, and therefore carries no semantics in isolation.
   *
   * Note that only XPointers containing just an ID are stable in that they do not depend on the order of
   * elements in the document pointed to.
   */
  final case class OtherResourceNode(docUri: URI, xpointer: XPointer) extends ResourceNode
}
