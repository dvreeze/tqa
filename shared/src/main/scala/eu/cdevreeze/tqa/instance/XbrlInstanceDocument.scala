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

package eu.cdevreeze.tqa.instance

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.XmlDeclaration
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.queryapi.DocumentApi
import eu.cdevreeze.yaidom.queryapi.Nodes

/**
 * "XBRL instance DOM document".
 *
 * @author Chris de Vreeze
 */
// scalastyle:off null
final class XbrlInstanceDocument(
  val xmlDeclarationOption: Option[XmlDeclaration],
  val children:             immutable.IndexedSeq[CanBeXbrliDocumentChild]) extends DocumentApi {

  require(xmlDeclarationOption ne null)
  require(children ne null)
  require(documentElement ne null)

  type ThisDoc = XbrlInstanceDocument

  type DocElemType = XbrlInstance

  def uriOption: Option[URI] = documentElement.backingElem.docUriOption

  def uri: URI = uriOption.getOrElse(XbrlInstanceDocument.EmptyUri)

  def documentElement: XbrlInstance = {
    (children collectFirst { case e: XbrlInstance => e }).getOrElse(sys.error(s"Missing document element"))
  }

  def processingInstructions: immutable.IndexedSeq[XbrliProcessingInstructionNode] = {
    children.collect({ case pi: XbrliProcessingInstructionNode => pi })
  }

  def comments: immutable.IndexedSeq[XbrliCommentNode] = {
    children.collect({ case c: XbrliCommentNode => c })
  }

  def withXmlDeclarationOption(newXmlDeclarationOption: Option[XmlDeclaration]): XbrlInstanceDocument = {
    new XbrlInstanceDocument(newXmlDeclarationOption, children)
  }
}

object XbrlInstanceDocument {

  private val EmptyUri = URI.create("")

  def apply(xmlDeclarationOption: Option[XmlDeclaration], children: immutable.IndexedSeq[CanBeXbrliDocumentChild]): XbrlInstanceDocument = {
    new XbrlInstanceDocument(xmlDeclarationOption, children)
  }

  def apply(xmlDeclarationOption: Option[XmlDeclaration], documentElement: XbrlInstance): XbrlInstanceDocument = {
    new XbrlInstanceDocument(xmlDeclarationOption, Vector(documentElement))
  }

  /**
   * Builds a `XbrlInstanceDocument` from a `BackingDocumentApi`.
   */
  def build(backingDoc: BackingDocumentApi): XbrlInstanceDocument = {
    val rootElem = XbrlInstance.build(backingDoc.documentElement)

    val xmlDeclarationOption = backingDoc.xmlDeclarationOption

    val children: immutable.IndexedSeq[CanBeXbrliDocumentChild] = backingDoc.children map {
      case c: Nodes.Comment                => XbrliCommentNode(c.text)
      case pi: Nodes.ProcessingInstruction => XbrliProcessingInstructionNode(pi.target, pi.data)
      case _: Nodes.Elem                   => rootElem
    }

    XbrlInstanceDocument(xmlDeclarationOption, children)
  }
}
