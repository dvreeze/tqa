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

package eu.cdevreeze.tqa.base.dom

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.XmlDeclaration
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.queryapi.DocumentApi
import eu.cdevreeze.yaidom.queryapi.Nodes

/**
 * "Taxonomy DOM document".
 *
 * @author Chris de Vreeze
 */
// scalastyle:off null
final class TaxonomyDocument private (
  val backingDocument: BackingDocumentApi,
  val documentElement: TaxonomyElem) extends DocumentApi {

  // Note that the constructor arguments are overlapping. This is safe because the constructor is
  // private, and the public factory method does not have this overlap. The data redundancy in the
  // private constructor prevents expensive construction of the TaxonomyElem document element, while
  // retaining all of the backing document (including its non-element children), which may not
  // be recoverable from the document children.

  require(backingDocument ne null)
  require(documentElement ne null)

  type ThisDoc = TaxonomyDocument

  type DocElemType = TaxonomyElem

  def xmlDeclarationOption: Option[XmlDeclaration] = backingDocument.xmlDeclarationOption

  def children: immutable.IndexedSeq[CanBeTaxonomyDocumentChild] = {
    backingDocument.children map {
      case c: Nodes.Comment                => TaxonomyCommentNode(c.text)
      case pi: Nodes.ProcessingInstruction => TaxonomyProcessingInstructionNode(pi.target, pi.data)
      case _: Nodes.Elem                   => documentElement
    }
  }

  def uriOption: Option[URI] = backingDocument.uriOption

  def uri: URI = uriOption.getOrElse(TaxonomyDocument.EmptyUri)

  def processingInstructions: immutable.IndexedSeq[TaxonomyProcessingInstructionNode] = {
    children.collect({ case pi: TaxonomyProcessingInstructionNode => pi })
  }

  def comments: immutable.IndexedSeq[TaxonomyCommentNode] = {
    children.collect({ case c: TaxonomyCommentNode => c })
  }
}

object TaxonomyDocument {

  private val EmptyUri = URI.create("")

  /**
   * Builds a `TaxonomyDocument` from a `BackingDocumentApi`.
   */
  def build(backingDoc: BackingDocumentApi): TaxonomyDocument = {
    val taxoRootElem = TaxonomyElem.build(backingDoc.documentElement)

    new TaxonomyDocument(backingDoc, taxoRootElem)
  }
}
