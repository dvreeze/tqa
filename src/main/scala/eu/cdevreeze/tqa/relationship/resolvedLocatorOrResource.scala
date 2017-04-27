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

package eu.cdevreeze.tqa.relationship

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.LabeledXLink
import eu.cdevreeze.tqa.dom.XLinkLocator
import eu.cdevreeze.tqa.dom.XLinkResource

/**
 * Resolved locator or resource. This can act as the resolved "from" or "to" side of a relationship.
 *
 * These objects must be very efficient to create.
 *
 * @author Chris de Vreeze
 */
sealed trait ResolvedLocatorOrResource[E <: TaxonomyElem] {

  def xlinkLocatorOrResource: LabeledXLink

  def resolvedElem: E

  final def xmlFragmentKey: XmlFragmentKey = {
    resolvedElem.key
  }

  final def elr: String = xlinkLocatorOrResource.elr
}

/**
 * An XLink locator with the taxonomy element referred to by that locator. The taxonomy element referred to may occur
 * in another document than the locator, which is typically the case.
 */
final class ResolvedLocator[E <: TaxonomyElem](val locator: XLinkLocator, val resolvedElem: E) extends ResolvedLocatorOrResource[E] {

  def xlinkLocatorOrResource: XLinkLocator = locator
}

/**
 * An XLink resource wrapped as `ResolvedLocatorOrResource`.
 */
final class ResolvedResource(val resource: XLinkResource) extends ResolvedLocatorOrResource[XLinkResource] {

  def xlinkLocatorOrResource: XLinkResource = resource

  def resolvedElem: XLinkResource = resource
}
