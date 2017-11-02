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

import scala.collection.immutable

import eu.cdevreeze.tqa.base.dom.TaxonomyRootElem
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder

/**
 * Strategy for collecting taxonomy document root elements. Typically implemented as DTS discovery, although
 * file search under a given root directory is also possible.
 *
 * @author Chris de Vreeze
 */
trait DocumentCollector {

  def collectTaxonomyRootElems(documentBuilder: DocumentBuilder): immutable.IndexedSeq[TaxonomyRootElem]
}
