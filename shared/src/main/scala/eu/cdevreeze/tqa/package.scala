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

package eu.cdevreeze

/**
 * Root package of the '''Taxonomy Query API'''. This package itself contains commonly used data classes and many constants
 * for namespaces and expanded names.
 *
 * There are 3 layers in TQA. The lowest layer is the '''type-safe taxonomy DOM model'''. It uses '''yaidom'''
 * ([[https://github.com/dvreeze/yaidom]]) for its "XML dialect support", where the XML dialect is XBRL taxonomy data. It
 * knows only about individual DOM trees.
 *
 * On top of the type-safe DOM layer is the '''relationship layer'''. It resolves the arcs of the DOM layer as relationships.
 * Of course, to resolve arcs we need other documents as context.
 *
 * On top of the relationship layer is the '''taxonomy query API layer'''. It uses the underlying layers to offer a query
 * API in which taxonomy elements (such as concept declarations) and relationships can easily be queried.
 *
 * It is important to note that higher layers do not abstract away lower layers. Think of the layering more in
 * terms of dependencies. The "taxonomy DOM" layer depends only on yaidom, the relationship layer only depends on the
 * taxonomy DOM layer, and the query API layer depends on both the taxonomy DOM and relationship layers. On the other
 * hand, relationships are indeed abstractions of the underlying XLink arcs and locators/resources.
 *
 * These 3 layers are as follows in terms of packages, from low to high:
 *
 * <ul>
 * <li>Package [[eu.cdevreeze.tqa.base.dom]]</li>
 * <li>Package [[eu.cdevreeze.tqa.base.relationship]]</li>
 * <li>Package [[eu.cdevreeze.tqa.base.queryapi]]</li>
 * </ul>
 *
 * ==Usage==
 *
 * How do we query XBRL taxonomies with TQA? For some examples, see package [[eu.cdevreeze.tqa.base.queryapi]].
 *
 * It should be noted that typical queries involve all 3 packages [[eu.cdevreeze.tqa.base.queryapi]], [[eu.cdevreeze.tqa.base.relationship]]
 * and [[eu.cdevreeze.tqa.base.dom]].
 *
 * It should also be noted that TQA is deeply rooted in the '''yaidom''' XML query API ([[https://github.com/dvreeze/yaidom]]).
 * This is true internally and externally. Internally TQA has been built in a bottom-up manner on top of yaidom, and this
 * is in particular visible in the internals of the TQA DOM package. Externally many TQA query methods remind of yaidom
 * query methods, but taking and returning type-safe TQA DOM taxonomy content instead of arbitrary XML content.
 * In other words, '''knowing yaidom helps in getting to know TQA'''. Of course, knowing the basics of XBRL taxonomies
 * and XBRL dimensions also helps in getting to know TQA.
 *
 * For all TQA queries, it is possible to write an equivalent lower level query, possibly even a low level yaidom query.
 * Doing so may be time-consuming, but it can also be a good learning experience if one intends to use TQA extensively.
 *
 * TQA tries to follow the XBRL Core and Dimensions specifications. For example, TQA knows about networks of relationships,
 * XPointer in an XBRL context, embedded linkbases etc.
 *
 * TQA feels like an API designed in a bottom-up manner on top of yaidom, and that is indeed what it is. So, although
 * TQA can help in learning more about XBRL taxonomies, it is not an API for users who have no knowledge about XBRL.
 * From the lower level DOM-like operations to the higher level dimensional bulk query methods, TQA is useful only
 * for users who know what data they are looking for in a taxonomy.
 *
 * See package [[eu.cdevreeze.tqa.base.queryapi]] for some examples, and navigate to the [[eu.cdevreeze.tqa.base.relationship]] and
 * [[eu.cdevreeze.tqa.base.dom]] packages for specific relationships and type-safe taxonomy DOM content, respectively.
 *
 * ==Backing XML DOM implementations==
 *
 * TQA is flexible in the DOM implementation used. It is pluggable, as long as it offers a yaidom `BackingNodes.Elem`
 * facade and we have a [[eu.cdevreeze.tqa.docbuilder.DocumentBuilder]] for it.
 *
 * Out of the box, 2 backing DOM implementations are available:
 * <ul>
 * <li>A yaidom `BackingNodes.Elem` implementation wrapping Saxon `NodeInfo` objects, and typically implemented using Saxon tiny trees</li>
 * <li>A yaidom `BackingNodes.Elem` native implementation using yaidom "indexed" elements</li>
 * </ul>
 *
 * The Saxon (9.7) wrapper implementation, especially when tiny trees are used under the hood, is efficient in space
 * and time, so it should be preferred for production code. The native yaidom implementation is less efficient, in
 * particular in memory footprint, but it is easy to debug and therefore a nice choice in test code. Whichever the
 * choice of underlying DOM implementation, most code using TQA is completely unaffected by it.
 *
 * @author Chris de Vreeze
 */
package object tqa {

  // Names

  val Namespaces = common.names.Namespaces

  val ENames = common.names.ENames

  // Schema types

  val XsdBooleans = common.schematypes.XsdBooleans

  val XsdFloats = common.schematypes.XsdFloats

  val XsdDoubles = common.schematypes.XsdDoubles

  val XsdDecimals = common.schematypes.XsdDecimals

  // XPath

  type ScopedXPathString = common.xpath.ScopedXPathString

  val ScopedXPathString = common.xpath.ScopedXPathString

  type ValueOrExpr[A] = common.xpath.ValueOrExpr[A]

  type Value[A] = common.xpath.Value[A]

  type Expr[A] = common.xpath.Expr[A]

  type StringValueOrExpr = common.xpath.StringValueOrExpr

  type StringValue = common.xpath.StringValue

  val StringValue = common.xpath.StringValue

  type StringExpr = common.xpath.StringExpr

  val StringExpr = common.xpath.StringExpr

  type BigDecimalValueOrExpr = common.xpath.BigDecimalValueOrExpr

  type BigDecimalValue = common.xpath.BigDecimalValue

  val BigDecimalValue = common.xpath.BigDecimalValue

  type BigDecimalExpr = common.xpath.BigDecimalExpr

  val BigDecimalExpr = common.xpath.BigDecimalExpr

  type ENameValueOrExpr = common.xpath.ENameValueOrExpr

  type ENameValue = common.xpath.ENameValue

  val ENameValue = common.xpath.ENameValue

  type ENameExpr = common.xpath.ENameExpr

  val ENameExpr = common.xpath.ENameExpr

  // XPointer

  type XPointer = common.xpointer.XPointer

  val XPointer = common.xpointer.XPointer

  type XPointerContainingId = common.xpointer.XPointerContainingId

  type ShorthandPointer = common.xpointer.ShorthandPointer

  val ShorthandPointer = common.xpointer.ShorthandPointer

  type ElementSchemePointer = common.xpointer.ElementSchemePointer

  type IdPointer = common.xpointer.IdPointer

  val IdPointer = common.xpointer.IdPointer

  type IdChildSequencePointer = common.xpointer.IdChildSequencePointer

  val IdChildSequencePointer = common.xpointer.IdChildSequencePointer

  type ChildSequencePointer = common.xpointer.ChildSequencePointer

  val ChildSequencePointer = common.xpointer.ChildSequencePointer

  type ElemPointer = common.xpointer.ElemPointer

  val ElemPointer = common.xpointer.ElemPointer

  // Schema

  type SubstitutionGroupMap = common.schema.SubstitutionGroupMap

  val SubstitutionGroupMap = common.schema.SubstitutionGroupMap

  // Miscellaneous

  type XmlFragmentKey = common.XmlFragmentKey

  val XmlFragmentKey = common.XmlFragmentKey

  type XsiSchemaLocation = common.XsiSchemaLocation

  val XsiSchemaLocation = common.XsiSchemaLocation

  val AttributeSupport = common.AttributeSupport
}
