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
 * Strategy for collecting taxonomy document root elements. Typically implemented as DTS discovery, although
 * it is also possible that all document URIs are explicitly mentioned.
 *
 * @author Chris de Vreeze
 */
trait DocumentCollector {

  /**
   * Collects taxonomy root elements for the given entry points, using the given document builder.
   *
   * If this document collector performs DTS discovery, the entry point URIs are the entry points for
   * DTS discovery. If this document collector does not perform any DTS discovery, and expects all
   * document URIs to be explicitly passed, then those document URIs are considered the "entry points"
   * as far as this method is concerned.
   *
   * The entry point URIs should normally be the canonical, published document locations.
   */
  def collectTaxonomyRootElems(
    entryPointUris: Set[URI],
    documentBuilder: DocumentBuilder): immutable.IndexedSeq[TaxonomyRootElem]
}
