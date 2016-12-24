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

package eu.cdevreeze.tqa.dom

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames.IdEName

/**
 * Very light-weight notion of a taxonomy, as a collection of taxonomy root elements. It contains a map from URIs
 * (with fragments) to taxonomy elements, for quick element lookups based on URIs with fragments.
 *
 * Taxonomy creation should never fail, if correct URIs are passed. Even the instance methods are very lenient and
 * should never fail. Typically, a taxonomy instantiated as an object of this class has not yet been validated.
 * This taxonomy class has one main purpose: support for locator resolution when building relationships.
 *
 * @author Chris de Vreeze
 */
final class UriAwareTaxonomy private (val rootElems: immutable.IndexedSeq[TaxonomyElem]) {
  require(
    rootElems.forall(e => e.backingElem.docUri.getFragment == null),
    s"Expected document URIs but got at least one URI with fragment")

  /**
   * Somewhat expensive map from URIs without fragments to corresponding root elements. If there are duplicate IDs, data is lost.
   */
  val rootElemUriMap: Map[URI, TaxonomyElem] = {
    rootElems.groupBy(e => e.backingElem.docUri).mapValues(_.head)
  }

  /**
   * Expensive map from URIs with ID fragments to corresponding elements. If there are duplicate IDs, data is lost.
   */
  val elemUriMap: Map[URI, TaxonomyElem] = {
    rootElems.flatMap(e => getElemUriMap(e)).toMap
  }

  /**
   * Finds the (first) optional element with the given URI. The fragment, if any, must be an ID. If there is no fragment,
   * the first root element with the given document URI is searched for. This is a quick operation.
   *
   * The schema type of the ID attributes is not taken into account, although strictly speaking that is incorrect.
   *
   * TODO Element scheme XPointer
   */
  def findElemByUri(elemUri: URI): Option[TaxonomyElem] = {
    require(elemUri.isAbsolute, s"URI '${elemUri}' is not absolute")

    if (elemUri.getFragment == null) {
      rootElemUriMap.get(elemUri)
    } else {
      elemUriMap.get(elemUri)
    }
  }

  /**
   * Returns true if the DOM tree with the given root element has any duplicate ID attributes.
   * If so, the taxonomy is incorrect, and the map from URIs to elements loses data.
   *
   * The type of the ID attributes is not taken into account, although strictly speaking that is incorrect.
   */
  def hasDuplicateIds(rootElem: TaxonomyElem): Boolean = {
    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)
    val ids = elemsWithId.map(_.attribute(IdEName))

    ids.distinct.size < ids.size
  }

  private def getElemUriMap(rootElem: TaxonomyElem): Map[URI, TaxonomyElem] = {
    val docUri = rootElem.backingElem.docUri
    assert(docUri.isAbsolute)

    // The schema type of the ID attributes is not checked! That would be very expensive without any real advantage.

    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)
    elemsWithId.map(e => (makeUriWithIdFragment(e.backingElem.baseUri, e.attribute(IdEName)) -> e)).toMap
  }

  private def makeUriWithIdFragment(baseUri: URI, idFragment: String): URI = {
    require(baseUri.isAbsolute, s"Expected absolute base URI but got '${baseUri}'")
    new URI(baseUri.getScheme, baseUri.getSchemeSpecificPart, idFragment)
  }
}

object UriAwareTaxonomy {

  def build(rootElems: immutable.IndexedSeq[TaxonomyElem]): UriAwareTaxonomy = {
    new UriAwareTaxonomy(rootElems)
  }
}
