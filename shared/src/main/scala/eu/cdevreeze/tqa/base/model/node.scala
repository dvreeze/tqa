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

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.yaidom.core.EName

/**
 * Any node that can be a relationship source or target in the model. There are 2 immediate
 * sub-types: LocatorNode and ResourceNode (abstractions of XLink locators and XLink resources, respectively).
 *
 * A LocatorNode is a unique key to a taxonomy element, and not the taxonomy element itself. This decoupling
 * respects the fact that it is not known beforehand how relationships in taxonomies are traversed.
 * ResourceNodes, on the other hand, are the taxonomy elements themselves.
 *
 * Note that Nodes are easy to create on the fly, which is by design.
 *
 * @author Chris de Vreeze
 */
sealed trait Node

/**
 * Locator node. It holds a unique key to a taxonomy element. It is an abstraction of what in the XML
 * representation is an XLink locator, but then resolved as a key to a taxonomy element.
 */
sealed trait LocatorNode extends Node

/**
 * Resource node. It is an abstraction of what in the XML representation is a taxonomy element that is
 * an XLink resource. Note that each resource node that locators can point to is expected to have an ID.
 */
sealed trait ResourceNode extends Node {

  def idOption: Option[String]

  def elr: String

  def roleOption: Option[String]

  def nonXLinkAttributes: Map[EName, String]

  final def elementKeyOption: Option[ResourceKey] = {
    idOption.map { id =>
      ResourceKey(elr, roleOption, id)
    }
  }
}

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

  /**
   * Any other LocatorNode. It contains an ElementKey, and therefore carries no semantics in isolation.
   * It is assumed that the element key is unique in the entire document collection.
   *
   * An example is a locator to an enumeration value, assuming the enumeration value has an ID and the element
   * key is unique across the taxonomy document collection.
   *
   * Another example is a locator to a ResourceNode, using a ResourceKey as element key.
   */
  final case class OtherLocatorNode(elementKey: ElementKey) extends LocatorNode

  /**
   * A (standard or non-standard) label or reference resource
   */
  sealed trait DocumentationResource extends ResourceNode {

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
    idOption: Option[String],
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends StandardDocumentationResource with LabelResource

  final case class ConceptReferenceResource(
    idOption: Option[String],
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    parts: Map[EName, String]) extends StandardDocumentationResource with ReferenceResource

  /**
   * An element label, named label:label (and not any resource in that substitution group).
   */
  final case class ElementLabelResource(
    idOption: Option[String],
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends NonStandardDocumentationResource with LabelResource

  /**
   * An element reference, named reference:reference (and not any resource in that substitution group).
   */
  final case class ElementReferenceResource(
    idOption: Option[String],
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    parts: Map[EName, String]) extends NonStandardDocumentationResource with ReferenceResource

  // TODO OtherNonStandardDocumentationResource

  /**
   * Any other ResourceNode. For example, table content like table, ruleNode, or formula content like
   * formula:valueAssertion, or a custom resource node, such as sbr:linkroleOrder in Dutch taxonomies.
   *
   * It contains an optional element key, and therefore carries no semantics in isolation.
   * It is assumed that the element key, if present, is unique in the entire document collection.
   *
   * TODO Model the content of an OtherResourceNode.
   */
  final case class OtherResourceNode(
    idOption: Option[String],
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String]) extends ResourceNode
}
