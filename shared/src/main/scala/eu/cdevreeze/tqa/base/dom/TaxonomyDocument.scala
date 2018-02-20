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
final class TaxonomyDocument(
  val xmlDeclarationOption: Option[XmlDeclaration],
  val children:             immutable.IndexedSeq[CanBeTaxonomyDocumentChild]) extends DocumentApi {

  require(xmlDeclarationOption ne null)
  require(children ne null)
  require(documentElement ne null)

  type ThisDoc = TaxonomyDocument

  type DocElemType = TaxonomyElem

  def uriOption: Option[URI] = documentElement.backingElem.docUriOption

  def uri: URI = uriOption.getOrElse(TaxonomyDocument.EmptyUri)

  def documentElement: TaxonomyElem = {
    (children collectFirst { case e: TaxonomyElem => e }).getOrElse(sys.error(s"Missing document element"))
  }

  def processingInstructions: immutable.IndexedSeq[TaxonomyProcessingInstructionNode] = {
    children.collect({ case pi: TaxonomyProcessingInstructionNode => pi })
  }

  def comments: immutable.IndexedSeq[TaxonomyCommentNode] = {
    children.collect({ case c: TaxonomyCommentNode => c })
  }

  def withXmlDeclarationOption(newXmlDeclarationOption: Option[XmlDeclaration]): TaxonomyDocument = {
    new TaxonomyDocument(newXmlDeclarationOption, children)
  }
}

object TaxonomyDocument {

  private val EmptyUri = URI.create("")

  def apply(xmlDeclarationOption: Option[XmlDeclaration], children: immutable.IndexedSeq[CanBeTaxonomyDocumentChild]): TaxonomyDocument = {
    new TaxonomyDocument(xmlDeclarationOption, children)
  }

  def apply(xmlDeclarationOption: Option[XmlDeclaration], documentElement: TaxonomyElem): TaxonomyDocument = {
    new TaxonomyDocument(xmlDeclarationOption, Vector(documentElement))
  }

  /**
   * Builds a `TaxonomyDocument` from a `BackingDocumentApi`.
   */
  def build(backingDoc: BackingDocumentApi): TaxonomyDocument = {
    val taxoRootElem = TaxonomyElem.build(backingDoc.documentElement)

    val xmlDeclarationOption = backingDoc.xmlDeclarationOption

    val children: immutable.IndexedSeq[CanBeTaxonomyDocumentChild] = backingDoc.children map {
      case c: Nodes.Comment                => TaxonomyCommentNode(c.text)
      case pi: Nodes.ProcessingInstruction => TaxonomyProcessingInstructionNode(pi.target, pi.data)
      case _: Nodes.Elem                   => taxoRootElem
    }

    TaxonomyDocument(xmlDeclarationOption, children)
  }
}
