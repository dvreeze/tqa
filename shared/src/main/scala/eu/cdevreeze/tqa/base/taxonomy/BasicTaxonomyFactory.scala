/*
 * Copyright 2011-2021 Chris de Vreeze
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

package eu.cdevreeze.tqa.base.taxonomy

import java.net.URI

/**
 * Abstract API for factories of BasicTaxonomy instances, given an entry points (as non-empty set of
 * document URIs).
 *
 * Typically the returned BasicTaxonomy results from DTS discovery, although this is not required. The "document
 * collection" algorithm, whether DTS discovery or not, is not part of this API.
 *
 * @author Chris de Vreeze
 */
trait BasicTaxonomyFactory {

  /**
   * Builds a `BasicTaxonomy` from the given entry point URIs.
   */
  def build(entryPointUris: Set[URI]): BasicTaxonomy
}
