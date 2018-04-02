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
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.DocumentApi

/**
 * "XBRL instance DOM document".
 *
 * @author Chris de Vreeze
 */
// scalastyle:off null
final class XbrlInstanceDocument private (
  val backingDocument: BackingDocumentApi,
  val documentElement: XbrlInstance) extends DocumentApi {

  // Note that the constructor arguments are overlapping. This is safe because the constructor is
  // private, and the public factory method does not have this overlap. The data redundancy in the
  // private constructor prevents expensive construction of the XbrlInstance document element, while
  // retaining all of the backing document (including its non-element children), which may not
  // be recoverable from the document children.

  require(backingDocument ne null)
  require(documentElement ne null)
  assert(backingDocument.documentElement.resolvedName == documentElement.resolvedName)

  type ThisDoc = XbrlInstanceDocument

  type DocElemType = XbrlInstance

  def xmlDeclarationOption: Option[XmlDeclaration] = backingDocument.xmlDeclarationOption

  def children: immutable.IndexedSeq[CanBeXbrliDocumentChild] = {
    backingDocument.children map {
      case c: BackingNodes.Comment                => XbrliCommentNode(c.text)
      case pi: BackingNodes.ProcessingInstruction => XbrliProcessingInstructionNode(pi.target, pi.data)
      case _: BackingNodes.Elem                   => documentElement
    }
  }

  def uriOption: Option[URI] = backingDocument.uriOption

  def uri: URI = uriOption.getOrElse(XbrlInstanceDocument.EmptyUri)

  def processingInstructions: immutable.IndexedSeq[XbrliProcessingInstructionNode] = {
    children.collect { case pi: XbrliProcessingInstructionNode => pi }
  }

  def comments: immutable.IndexedSeq[XbrliCommentNode] = {
    children.collect { case c: XbrliCommentNode => c }
  }
}

object XbrlInstanceDocument {

  private val EmptyUri = URI.create("")

  /**
   * Builds an `XbrlInstanceDocument` from a `BackingDocumentApi`.
   */
  def build(backingDoc: BackingDocumentApi): XbrlInstanceDocument = {
    val rootElem = XbrlInstance.build(backingDoc.documentElement)

    new XbrlInstanceDocument(backingDoc, rootElem)
  }
}
