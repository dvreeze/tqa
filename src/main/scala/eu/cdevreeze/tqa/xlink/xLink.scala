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

package eu.cdevreeze.tqa.xlink

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import eu.cdevreeze.yaidom.queryapi.ScopedElemApi

/**
 * An XLink element in a taxonomy, obeying the constraints on XLink imposed by XBRL. For example, an XLink arc or extended link.
 *
 * The XLink elements offer (at least) the yaidom `ScopedElemApi` query API.
 *
 * The XLink elements are backed by a yaidom `BackingElemApi`. As a consequence, XLink child elements of an extended link know their
 * parent element (as BackingElemApi), and therefore know their ELR (extended link role).
 *
 * XLink (see https://www.w3.org/TR/xlink11/) is a somewhat low level standard on top of XML, but it is
 * very important in an XBRL context. Many taxonomy elements are also XLink elements, especially inside linkbases.
 *
 * @author Chris de Vreeze
 */
trait XLinkElem extends ScopedElemApi {

  def backingElem: BackingElemApi

  def xlinkType: String

  def xlinkAttributes: Map[EName, String]
}

// TODO XLink title and documentation (abstract) elements have not been modeled (yet).

/**
 * Simple or extended XLink link.
 */
trait XLinkLink extends XLinkElem

/**
 * XLink child element of an extended link, so an XLink arc, locator or resource.
 */
trait ChildXLink extends XLinkElem {

  /**
   * Returns the extended link role of the surrounding extended link element.
   * This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption.flatMap(_.attributeOption(ENames.XLinkRoleEName))
   * }}}
   */
  def elr: String

  /**
   * Returns the underlying parent element.
   * This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption
   * }}}
   */
  def underlyingParentElem: BackingElemApi
}

/**
 * XLink locator or resource.
 */
trait LabeledXLink extends ChildXLink {

  /**
   * Returns the XLink label. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkLabelEName)
   * }}}
   */
  def xlinkLabel: String

  def roleOption: Option[String]
}

/**
 * XLink extended link. For example (child elements have been left out):
 *
 * {{{
 * <link:presentationLink
 *   xlink:type="extended" xlink:role="http://mycompany.com/myPresentationElr">
 *
 * </link:presentationLink>
 * }}}
 *
 * Or, for example (again leaving out child elements):
 *
 * {{{
 * <link:labelLink
 *   xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
 *
 * </link:labelLink>
 * }}}
 */
trait ExtendedLink extends XLinkLink {

  /**
   * Returns the extended link role. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkRoleEName)
   * }}}
   */
  def role: String

  def xlinkChildren: immutable.IndexedSeq[ChildXLink]

  def labeledXlinkChildren: immutable.IndexedSeq[LabeledXLink]

  def arcs: immutable.IndexedSeq[XLinkArc]

  /**
   * Returns the XLink locators and resources grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  def labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]]
}

/**
 * XLink arc. For example, showing an XLink arc in a presentation link:
 *
 * {{{
 * <link:presentationArc xlink:type="arc"
 *   xlink:arcrole="http://www.xbrl.org/2003/arcrole/parent-child"
 *   xlink:from="parentConcept" xlink:to="childConcept" />
 * }}}
 *
 * The xlink:from and xlink:to attributes point to XLink locators or resources
 * in the same extended link with the corresponding xlink:label attributes.
 */
trait XLinkArc extends ChildXLink {

  /**
   * Returns the arcrole. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkArcroleEName)
   * }}}
   */
  def arcrole: String

  /**
   * Returns the XLink "from". This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkFromEName)
   * }}}
   */
  def from: String

  /**
   * Returns the XLink "to". This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkToEName)
   * }}}
   */
  def to: String
}

/**
 * XLink resource. For example, showing an XLink resource in a label link:
 *
 * {{{
 * <link:label xlink:type="resource"
 *   xlink:label="regionAxis_lbl" xml:lang="en"
 *   xlink:role="http://www.xbrl.org/2003/role/label">Region [Axis]</link:label>
 * }}}
 */
trait XLinkResource extends LabeledXLink

/**
 * XLink locator. For example:
 *
 * {{{
 * <link:loc xlink:type="locator"
 *   xlink:label="entityAxis"
 *   xlink:href="Axes.xsd#entityAxis" />
 * }}}
 */
trait XLinkLocator extends LabeledXLink {

  /**
   * Returns the XLink href as URI. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def rawHref: URI

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the taxonomy
   * is not schema-valid.
   */
  def resolvedHref: URI
}

/**
 * XLink simple link. For example, showing a roleRef:
 *
 * {{{
 * <link:roleRef xlink:type="simple"
 *   xlink:href="Concepts.xsd#SalesAnalysis"
 *   roleURI="http://mycompany.com/2017/SalesAnalysis" />
 * }}}
 */
trait SimpleLink extends XLinkLink {

  def arcroleOption: Option[String]

  def roleOption: Option[String]

  /**
   * Returns the XLink href as URI. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def rawHref: URI

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the taxonomy
   * is not schema-valid.
   */
  def resolvedHref: URI
}
