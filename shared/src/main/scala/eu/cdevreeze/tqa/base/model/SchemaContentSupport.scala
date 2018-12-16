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
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ClarkElemLike
import eu.cdevreeze.yaidom.queryapi.ClarkNodes

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
 * TODO REMOVE!
 *
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
object SchemaContentSupport {

  sealed trait Node extends ClarkNodes.Node

  final case class Elem(
    targetNamespaceOption: Option[String],
    minimalScope: Scope,
    resolvedName: EName,
    attributes: Map[EName, String],
    children: immutable.IndexedSeq[Node]) extends Node with ClarkNodes.Elem with ClarkElemLike {

    type ThisNode = Node

    type ThisElem = Elem

    // Methods needed for completing the ClarkNodes.Elem API

    def thisElem: Elem = this

    def findAllChildElems: immutable.IndexedSeq[Elem] = {
      children.collect { case e: Elem => e }
    }

    def resolvedAttributes: Map[EName, String] = attributes

    /**
     * Returns the concatenation of the texts of text children, including whitespace. Non-text children are ignored.
     * If there are no text children, the empty string is returned.
     */
    def text: String = {
      val textStrings = children.collect { case t: Text => t }.map(_.text)
      textStrings.mkString
    }
  }

  final case class Text(text: String) extends Node with ClarkNodes.Text {

    /** Returns `text.trim`. */
    final def trimmedText: String = text.trim

    /** Returns `XmlStringUtils.normalizeString(text)` .*/
    final def normalizedText: String = normalizeString(text)
  }

  // TODO We should also transform typedDomainRefs to ENames, but cannot do that without any context!

  def fromSchemaRootElem(elem: BackingNodes.Elem): Elem = {
    require(elem.resolvedName == ENames.XsSchemaEName, s"Expected ${ENames.XsSchemaEName} but got ${elem.resolvedName}")

    val tnsOption: Option[String] = elem.attributeOption(ENames.TargetNamespaceEName)

    from(tnsOption, elem)
  }

  private def from(
    tnsOption: Option[String],
    elem: BackingNodes.Elem): Elem = {

    // Recursive calls

    val children: immutable.IndexedSeq[Node] = elem.children.flatMap {
      case e: BackingNodes.Elem => Some(from(tnsOption, e))
      case t: BackingNodes.Text => Some(Text(t.text))
      case _ => None
    }

    val minimalScope: Scope = ???

    Elem(
      tnsOption,
      minimalScope,
      elem.resolvedName,
      elem.resolvedAttributes.toMap,
      children)
  }

  /**
   * Normalizes the string, removing surrounding whitespace and normalizing internal whitespace to a single space.
   * Whitespace includes #x20 (space), #x9 (tab), #xD (carriage return), #xA (line feed). If there is only whitespace,
   * the empty string is returned. Inspired by the JDOM library.
   */
  private def normalizeString(s: String): String = {
    require(s ne null) // scalastyle:off null

    val separators = Array(' ', '\t', '\r', '\n')
    val words: Seq[String] = s.split(separators).toSeq.filterNot(_.isEmpty)

    words.mkString(" ") // Returns empty string if words.isEmpty
  }
}
