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

package eu.cdevreeze.tqa.base.relationship

import scala.reflect.ClassTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.base.dom.AnyTaxonomyElem
import eu.cdevreeze.tqa.xlink.LabeledXLink
import eu.cdevreeze.tqa.xlink.XLinkLocator
import eu.cdevreeze.tqa.xlink.XLinkResource

/**
 * Resolved locator or resource. This can act as the resolved "from" or "to" side of a relationship.
 * It holds both the XLink locator or XLink resource and the resolved element. Sub-type `ResolvedLocatorOrResource.Locator`
 * is used for resolved XLink locators, and sub-type `ResolvedLocatorOrResource.Resource` is used for XLink resources, where
 * the XLink resource and its "resolution" are one and the same element.
 *
 * A "remote resource" (XLink locator to an XLink resource) is a specific `Locator` where the
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
}

object ResolvedLocatorOrResource {

  type AnyResource = AnyTaxonomyElem with XLinkResource

  /**
   * An XLink locator with the taxonomy element referred to by that locator. The taxonomy element referred to may occur
   * in another document than the locator, which is typically the case.
   */
  final class Locator[E <: AnyTaxonomyElem](val locator: XLinkLocator, val resolvedElem: E) extends ResolvedLocatorOrResource[E] {

    def xlinkLocatorOrResource: XLinkLocator = locator

    /**
     * Creates an equivalent Locator, but transforming the resolved element.
     * An example transformation lifts a low level taxonomy element to a higher level of abstraction (such as the table DOM level).
     */
    def transform[A <: AnyTaxonomyElem](f: E => A): Locator[A] = {
      new Locator(locator, f(resolvedElem))
    }

    /**
     * Creates an equivalent Locator, but casting the resolved element.
     * If we know that the resolved element is of type `A`, then the cast will succeed.
     */
    def cast[A <: AnyTaxonomyElem](clsTag: ClassTag[A]): Locator[A] = transform(_.asInstanceOf[A])
  }

  /**
   * An XLink resource wrapped as `ResolvedLocatorOrResource`.
   */
  final class Resource[E <: AnyTaxonomyElem with XLinkResource](val resource: E) extends ResolvedLocatorOrResource[E] {

    def xlinkLocatorOrResource: E = resource

    def resolvedElem: E = resource
  }

  /**
   * Creates an equivalent ResolvedLocatorOrResource, but transforming the resolved XLink resource element.
   * An example transformation lifts a low level taxonomy element to a higher level of abstraction (such as the table DOM level).
   */
  def transformResource[E1 <: AnyResource, E2 <: AnyResource](resolvedLocOrRes: ResolvedLocatorOrResource[E1], f: E1 => E2): ResolvedLocatorOrResource[E2] = {
    resolvedLocOrRes match {
      case loc: Locator[E1] =>
        new Locator(loc.locator, f(loc.resolvedElem))
      case res: Resource[E1] =>
        new Resource(f(res.resolvedElem))
    }
  }

  /**
   * Creates an equivalent ResolvedLocatorOrResource, but casting the resolved XLink resource element.
   * If we know that the resolved element is of type `E2`, then the cast will succeed.
   */
  def castResource[E1 <: AnyResource, E2 <: AnyResource](resolvedLocOrRes: ResolvedLocatorOrResource[E1], clsTag: ClassTag[E2]): ResolvedLocatorOrResource[E2] = {
    transformResource(resolvedLocOrRes, { (e: E1) => e.asInstanceOf[E2] })
  }

  // TODO Make the following methods obsolete

  /**
   * Like `transformResource`, but assuming and not checking that the input is resolved as a `AnyResource`.
   */
  def unsafeTransformResource[A <: AnyResource](resolvedLocOrRes: ResolvedLocatorOrResource[_], f: AnyTaxonomyElem => A): ResolvedLocatorOrResource[A] = {
    transformResource(resolvedLocOrRes.asInstanceOf[ResolvedLocatorOrResource[AnyResource]], { (e: AnyResource) => f(e) })
  }

  /**
   * Like `castResource`, but assuming and not checking that the input is resolved as a `AnyResource`.
   */
  def unsafeCastResource[A <: AnyResource](resolvedLocOrRes: ResolvedLocatorOrResource[_], clsTag: ClassTag[A]): ResolvedLocatorOrResource[A] = {
    unsafeTransformResource(resolvedLocOrRes, { (e: AnyTaxonomyElem) => e.asInstanceOf[A] })
  }
}
