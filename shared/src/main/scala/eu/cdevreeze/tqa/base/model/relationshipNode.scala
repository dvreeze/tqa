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
import eu.cdevreeze.yaidom.core.EName

/**
 * Any relationship node that can be a relationship source or target in the model. There are 2 immediate
 * sub-types: LocatorNode and ResourceNode (abstractions of XLink locators and XLink resources, respectively).
 *
 * A LocatorNode is a unique key to a taxonomy element, and not the taxonomy element itself. This decoupling
 * respects the fact that it is not known beforehand how relationships in taxonomies are traversed.
 * ResourceNodes, on the other hand, are the taxonomy elements themselves.
 *
 * Be careful not to confuse this notion of RelationshipNode with relationship nodes in XBRL Tables!
 *
 * @author Chris de Vreeze
 */
sealed trait RelationshipNode

/**
 * Locator node. It holds a unique key to a taxonomy element. It is an abstraction of what in the XML
 * representation is an XLink locator, but then resolved as a key to a taxonomy element.
 *
 * TODO Do we need locators to resources?
 */
sealed trait LocatorNode extends RelationshipNode

/**
 * Resource node. It is an abstraction of what in the XML representation is a taxonomy element that is
 * an XLink resource.
 */
sealed trait ResourceNode extends RelationshipNode

object RelationshipNode {

  /**
   * Key referring to a global element declaration
   */
  sealed trait Element extends LocatorNode {

    def targetEName: EName
  }

  /**
   * Key referring to a global element declaration for a concept (that is, item or tuple)
   */
  final case class Concept(targetEName: EName) extends Element

  /**
   * Key referring to a global element declaration for something that is not a concept. For example,
   * this key could refer to a typed dimension member declaration.
   */
  final case class OtherElement(targetEName: EName) extends Element

  /**
   * Key referring to a role type definition
   */
  final case class RoleType(roleUri: String) extends LocatorNode

  /**
   * Key referring to an arcrole type definition
   */
  final case class ArcroleType(roleUri: String) extends LocatorNode

  /**
   * Key referring to a named type definition.
   */
  final case class SchemaType(targetEName: EName) extends LocatorNode

  // TODO Key for enumeration value (XPointer-based?)

  // TODO Complete label and reference resources, including reference content (which normally is XML)

  /**
   * A (standard or non-standard) label or reference resource
   */
  sealed trait Resource extends ResourceNode {

    def docUri: URI

    def elr: String

    def roleOption: Option[String]

    def nonXLinkAttributes: Map[EName, String]

    def text: String

    final def langOption: Option[String] = {
      nonXLinkAttributes.get(ENames.XmlLangEName)
    }
  }

  sealed trait StandardResource extends Resource

  sealed trait NonStandardResource extends Resource

  final case class ConceptLabelResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends StandardResource

  final case class ConceptReferenceResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends StandardResource

  /**
   * An element label, named label:label (and not any resource in that substitution group).
   */
  final case class ElementLabelResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends NonStandardResource

  /**
   * An element reference, named reference:reference (and not any resource in that substitution group).
   */
  final case class ElementReferenceResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends NonStandardResource

  // TODO OtherNonStandardResource

  // TODO XPointer?

  final case class OtherLocatorNode(docUri: URI, xpointer: String) extends LocatorNode

  /**
   * Any other ResourceNode. For example, table content like table: ruleNode, or formula content like
   * formula:valueAssertion, or a custom resource node, such as sbr:linkroleOrder in Dutch taxonomies.
   */
  final case class OtherResourceNode(docUri: URI, xpointer: String) extends ResourceNode
}
