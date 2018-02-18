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

import eu.cdevreeze.tqa.base.dom.TaxonomyRootElem
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder

/**
 * Trivial document collector, performing no DTS discovery, but expecting all document URIs to be
 * explicitly given as "entry points" instead.
 *
 * @author Chris de Vreeze
 */
object TrivialDocumentCollector extends DocumentCollector {

  final def collectTaxonomyRootElems(
    entryPointUris:  Set[URI],
    documentBuilder: DocumentBuilder): immutable.IndexedSeq[TaxonomyRootElem] = {

    entryPointUris.toIndexedSeq.sortBy(_.toString).map(uri => buildRootElem(uri, documentBuilder))
  }

  private def buildRootElem(uri: URI, documentBuilder: DocumentBuilder): TaxonomyRootElem = {
    val taxoRootElemOption = TaxonomyRootElem.buildOptionally(documentBuilder.build(uri).documentElement)

    taxoRootElemOption.getOrElse(sys.error(s"Could not find taxonomy root element for $uri"))
  }
}
