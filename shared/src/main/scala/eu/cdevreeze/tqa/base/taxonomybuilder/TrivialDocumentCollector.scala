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

package eu.cdevreeze.tqa.base.taxonomybuilder

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder

/**
 * Trivial document collector, performing no DTS discovery, but expecting all document URIs to be
 * explicitly given as "entry points" instead.
 *
 * @author Chris de Vreeze
 */
object TrivialDocumentCollector extends DocumentCollector {

  final def collectTaxonomyDocuments(
    entryPointUris:  Set[URI],
    documentBuilder: DocumentBuilder): immutable.IndexedSeq[TaxonomyDocument] = {

    entryPointUris.toIndexedSeq.sortBy(_.toString).map(uri => buildTaxonomyDoc(uri, documentBuilder))
  }

  private def buildTaxonomyDoc(uri: URI, documentBuilder: DocumentBuilder): TaxonomyDocument = {
    val taxoDocOption = TaxonomyDocument.buildOptionally(documentBuilder.build(uri))

    taxoDocOption.getOrElse(sys.error(s"Could not find taxonomy document for $uri"))
  }
}
