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

package eu.cdevreeze.tqa.docbuilder.indexed

import java.net.URI

import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.yaidom.indexed.Document
import eu.cdevreeze.yaidom.simple

import scala.collection.immutable

/**
 * Indexed document builder using an underlying ThreadSafeDocumentBuilder, whose resulting documents are converted to
 * indexed Documents.
 *
 * The URI resolver is used for parsing the documents themselves (unlike SAX EntityResolver).
 * Typically the URI resolver takes HTTP(S) URIs and resolves them to resources in a local mirror.
 *
 * @author Chris de Vreeze
 */
final class IndexedThreadSafeWrapperDocumentBuilder(
    val underlyingDocumentBuilder: DocumentBuilder.ThreadSafeDocumentBuilder)
    extends DocumentBuilder.ThreadSafeDocumentBuilder {

  type BackingDoc = Document

  def build(uri: URI): Document = {
    val underlyingDoc = underlyingDocumentBuilder.build(uri)

    val simpleDocChildren: immutable.IndexedSeq[simple.CanBeDocumentChild] = underlyingDoc.children
      .map(ch => simple.Node.from(ch))
      .collect { case ch: simple.CanBeDocumentChild => ch }

    val simpleDoc: simple.Document = simple.Document.document(Some(uri.toString), simpleDocChildren)
    Document(simpleDoc)
  }
}

object IndexedThreadSafeWrapperDocumentBuilder {

  /**
   * Creates an IndexedThreadSafeWrapperDocumentBuilder from an underlying ThreadSafeDocumentBuilder.
   * The URI resolver is typically obtained through the UriResolvers singleton object.
   */
  def apply(
      underlyingDocumentBuilder: DocumentBuilder.ThreadSafeDocumentBuilder): IndexedThreadSafeWrapperDocumentBuilder = {
    new IndexedThreadSafeWrapperDocumentBuilder(underlyingDocumentBuilder)
  }
}
