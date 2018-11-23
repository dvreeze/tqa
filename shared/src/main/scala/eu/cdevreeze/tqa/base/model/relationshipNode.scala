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
 * Any relationship node that can be a relationship source or target in the model.
 *
 * @author Chris de Vreeze
 */
sealed trait RelationshipNode

object RelationshipNode {

  sealed trait GlobalElementDeclaration extends RelationshipNode {

    def targetEName: EName
  }

  final case class Concept(targetEName: EName) extends GlobalElementDeclaration

  final case class RoleType(roleUri: String) extends RelationshipNode

  final case class ArcroleType(roleUri: String) extends RelationshipNode

  // TODO Complete label and reference resources, including reference content (which normally is XML)

  sealed trait Resource extends RelationshipNode {

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

  sealed trait GenericResource extends Resource

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

  final case class ElementLabelResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends GenericResource

  final case class ElementReferenceResource(
    docUri: URI,
    elr: String,
    roleOption: Option[String],
    nonXLinkAttributes: Map[EName, String],
    text: String) extends GenericResource

  // TODO XPointer

  final case class OtherElement(docUri: URI, xpointer: String) extends RelationshipNode
}
