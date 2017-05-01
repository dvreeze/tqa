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

import scala.reflect.ClassTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.dom.AnyTaxonomyElem
import eu.cdevreeze.tqa.xlink.LabeledXLink
import eu.cdevreeze.tqa.xlink.XLinkLocator
import eu.cdevreeze.tqa.xlink.XLinkResource

/**
 * Resolved locator or resource. This can act as the resolved "from" or "to" side of a relationship.
 * It holds both the XLink locator or XLink resource and the resolved element. Sub-type `ResolvedLocator`
 * is used for resolved XLink locators, and sub-type `ResolvedResource` is used for XLink resources, where
 * the XLink resource and its "resolution" are one and the same element.
 *
 * A "remote resource" (XLink locator to an XLink resource) is a specific `ResolvedLocator` where the
 * resolved element type is an XLink resource type.
 *
 * These objects must be very efficient to create, in order to make relationship creation fast.
 *
 * @author Chris de Vreeze
 */
sealed trait ResolvedLocatorOrResource[E <: AnyTaxonomyElem] {

  def xlinkLocatorOrResource: LabeledXLink

  def resolvedElem: E

  final def xmlFragmentKey: XmlFragmentKey = {
    resolvedElem.key
  }

  final def elr: String = xlinkLocatorOrResource.elr

  /**
   * Creates an equivalent ResolvedLocatorOrResource, but transforming the resolved element.
   * An example transformation lifts a low level taxonomy element to a higher level of abstraction (such as the table DOM level).
   */
  def transform[A <: AnyTaxonomyElem](f: E => A): ResolvedLocatorOrResource[A]

  /**
   * Creates an equivalent ResolvedLocatorOrResource, but casting the resolved element.
   * If we know that the resolved element is of type `A`, then the cast will succeed.
   */
  final def cast[A <: AnyTaxonomyElem](clsTag: ClassTag[A]): ResolvedLocatorOrResource[A] = transform(_.asInstanceOf[A])
}

/**
 * An XLink locator with the taxonomy element referred to by that locator. The taxonomy element referred to may occur
 * in another document than the locator, which is typically the case.
 */
final class ResolvedLocator[E <: AnyTaxonomyElem](val locator: XLinkLocator, val resolvedElem: E) extends ResolvedLocatorOrResource[E] {

  def xlinkLocatorOrResource: XLinkLocator = locator

  def transform[A <: AnyTaxonomyElem](f: E => A): ResolvedLocator[A] = new ResolvedLocator(locator, f(resolvedElem))
}

/**
 * An XLink resource wrapped as `ResolvedLocatorOrResource`.
 */
final class ResolvedResource[E <: AnyTaxonomyElem](val resource: E) extends ResolvedLocatorOrResource[E] {
  require(resource.isInstanceOf[XLinkResource], s"Expected an XLink resource")

  def xlinkLocatorOrResource: XLinkResource = resource.asInstanceOf[XLinkResource]

  def resolvedElem: E = resource

  def transform[A <: AnyTaxonomyElem](f: E => A): ResolvedResource[A] = new ResolvedResource(f(resolvedElem))
}
