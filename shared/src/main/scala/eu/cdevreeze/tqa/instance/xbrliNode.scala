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

package eu.cdevreeze.tqa.instance

import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

import scala.collection.immutable
import scala.reflect.classTag

import XbrliElem._
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.tqa.xlink
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemNodeApi
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem
import eu.cdevreeze.yaidom.queryapi.HasENameApi.withEName
import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemNodeApi
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import eu.cdevreeze.yaidom.resolved.ResolvedNodes

/**
 * "XBRL instance DOM node".
 *
 * @author Chris de Vreeze
 */
sealed abstract class XbrliNode extends ResolvedNodes.Node

sealed abstract class CanBeXbrliDocumentChild extends XbrliNode with Nodes.CanBeDocumentChild

final case class XbrliTextNode(text: String) extends XbrliNode with ResolvedNodes.Text

final case class XbrliProcessingInstructionNode(target: String, data: String) extends CanBeXbrliDocumentChild
  with Nodes.ProcessingInstruction

final case class XbrliCommentNode(text: String) extends CanBeXbrliDocumentChild with Nodes.Comment

/**
 * XML element inside XBRL instance (or the entire XBRL instance itself). This API is immutable, provided
 * the backing element is immutable.
 *
 * The yaidom `SubtypeAwareElemApi` and `ScopedElemApi` query API is offered.
 *
 * Also note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying.
 *
 * These XBRL instance elements are just an XBRL instance view on the underlying "backing element" tree, and
 * therefore do not know about the taxonomy describing the XBRL instance (other than the href to the DTS entry point).
 * It is not even required that the XBRL instance is schema-valid. Construction of an instance is indeed quite lenient.
 *
 * As a consequence, this model must recognize facts by only looking at the elements and their ancestry, without knowing
 * anything about the substitution groups of the corresponding concept declarations. Fortunately, the XBRL instance
 * schema (xbrl-instance-2003-12-31.xsd) and the specification of allowed XBRL tuple content are (almost) restrictive enough
 * in order to recognize facts.
 *
 * It is even possible to easily distinguish between item facts and tuple facts, based on the presence or absence of the
 * contextRef attribute. There is one complication, though, and that is nil item and tuple facts. Unfortunately, concept
 * declarations in taxonomy schemas may have the nillable attribute set to true. This led to some clutter in the
 * inheritance hierarchy for numeric item facts.
 *
 * Hence, regarding nil facts, the user of the API is responsible for keeping in mind that facts can indeed be nil facts
 * (which facts are easy to filter away).
 *
 * Another limitation is that without the taxonomy, default dimensions are unknown. Finally, the lack of typing information
 * is a limitation.
 *
 * Note that the backing element implementation can be any implementation of yaidom query API trait `BackingElemNodeApi`.
 *
 * This class hierarchy depends on Java 8 or later, due to the use of Java 8 time API.
 *
 * @author Chris de Vreeze
 */
sealed class XbrliElem private[instance] (
  val backingElem: BackingElemNodeApi,
  childElems:      immutable.IndexedSeq[XbrliElem]) extends CanBeXbrliDocumentChild
  with ResolvedNodes.Elem with ScopedElemNodeApi with ScopedElemLike with SubtypeAwareElemLike {

  // TODO Restore old equality on the backing elements themselves (after JS DOM wrappers have appropriate equality)
  assert(
    childElems.map(_.backingElem).map(_.resolvedName) == backingElem.findAllChildElems.map(_.resolvedName),
    msg("Corrupt element!"))

  type ThisElem = XbrliElem

  type ThisNode = XbrliNode

  final def thisElem: ThisElem = this

  final def children: immutable.IndexedSeq[XbrliNode] = {
    var childElemIdx = 0

    backingElem.children flatMap {
      case che: Nodes.Elem =>
        val e = childElems(childElemIdx)
        childElemIdx += 1
        Some(e)
      case ch: Nodes.Text =>
        Some(XbrliTextNode(ch.text))
      case ch: Nodes.Comment =>
        Some(XbrliCommentNode(ch.text))
      case ch: Nodes.ProcessingInstruction =>
        Some(XbrliProcessingInstructionNode(ch.target, ch.data))
      case ch =>
        None
    } ensuring (childElemIdx == childElems.size)
  }

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[XbrliElem] = childElems

  final def resolvedName: EName = backingElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = backingElem.resolvedAttributes

  final def qname: QName = backingElem.qname

  final def attributes: immutable.Iterable[(QName, String)] = backingElem.attributes

  final def scope: Scope = backingElem.scope

  final def text: String = backingElem.text

  final override def equals(other: Any): Boolean = other match {
    case e: XbrliElem => backingElem == e.backingElem
    case _            => false
  }

  final override def hashCode: Int = backingElem.hashCode

  private def msg(s: String): String = s"${s} (${backingElem.key})"
}

/**
 * XBRL instance.
 *
 * It does not check validity of the XBRL instance. Neither does it know about the DTS describing the XBRL instance.
 * It does, however, contain the entry point URI(s) to the DTS.
 *
 * Without any knowledge about the DTS, this class only recognizes (item and tuple) facts by looking at the
 * structure of the element and its ancestry. Attribute @contextRef is only allowed for item facts, and tuple facts can be
 * recognized by looking at the "path" of the element.
 *
 * @author Chris de Vreeze
 */
final class XbrlInstance private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliXbrlEName, s"Expected EName $XbrliXbrlEName but found $resolvedName")
  require(backingElem.path.isEmpty, s"The XbrlInstance must be the root element")

  val allContexts: immutable.IndexedSeq[XbrliContext] =
    findAllChildElemsOfType(classTag[XbrliContext])

  val allContextsById: Map[String, XbrliContext] =
    allContexts.groupBy(_.id) mapValues (_.head)

  val allUnits: immutable.IndexedSeq[XbrliUnit] =
    findAllChildElemsOfType(classTag[XbrliUnit])

  val allUnitsById: Map[String, XbrliUnit] =
    allUnits.groupBy(_.id) mapValues (_.head)

  val allTopLevelFacts: immutable.IndexedSeq[Fact] =
    findAllChildElemsOfType(classTag[Fact])

  val allTopLevelItems: immutable.IndexedSeq[ItemFact] =
    findAllChildElemsOfType(classTag[ItemFact])

  val allTopLevelTuples: immutable.IndexedSeq[TupleFact] =
    findAllChildElemsOfType(classTag[TupleFact])

  val allTopLevelFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] =
    allTopLevelFacts groupBy (_.resolvedName)

  val allTopLevelItemsByEName: Map[EName, immutable.IndexedSeq[ItemFact]] =
    allTopLevelItems groupBy (_.resolvedName)

  val allTopLevelTuplesByEName: Map[EName, immutable.IndexedSeq[TupleFact]] =
    allTopLevelTuples groupBy (_.resolvedName)

  def filterContexts(p: XbrliContext => Boolean): immutable.IndexedSeq[XbrliContext] = {
    filterChildElemsOfType(classTag[XbrliContext])(p)
  }

  def filterUnits(p: XbrliUnit => Boolean): immutable.IndexedSeq[XbrliUnit] = {
    filterChildElemsOfType(classTag[XbrliUnit])(p)
  }

  def filterTopLevelFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterChildElemsOfType(classTag[Fact])(p)
  }

  def filterTopLevelItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    filterChildElemsOfType(classTag[ItemFact])(p)
  }

  def filterTopLevelTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    filterChildElemsOfType(classTag[TupleFact])(p)
  }

  def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllElemsOfType(classTag[Fact])
  }

  def findAllItems: immutable.IndexedSeq[ItemFact] = {
    findAllElemsOfType(classTag[ItemFact])
  }

  def findAllTuples: immutable.IndexedSeq[TupleFact] = {
    findAllElemsOfType(classTag[TupleFact])
  }

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterElemsOfType(classTag[Fact])(p)
  }

  def filterItems(p: ItemFact => Boolean): immutable.IndexedSeq[ItemFact] = {
    filterElemsOfType(classTag[ItemFact])(p)
  }

  def filterTuples(p: TupleFact => Boolean): immutable.IndexedSeq[TupleFact] = {
    filterElemsOfType(classTag[TupleFact])(p)
  }

  def findAllSchemaRefs: immutable.IndexedSeq[SchemaRef] = {
    findAllChildElemsOfType(classTag[SchemaRef])
  }

  def findAllLinkbaseRefs: immutable.IndexedSeq[LinkbaseRef] = {
    findAllChildElemsOfType(classTag[LinkbaseRef])
  }

  def findAllRoleRefs: immutable.IndexedSeq[RoleRef] = {
    findAllChildElemsOfType(classTag[RoleRef])
  }

  def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef] = {
    findAllChildElemsOfType(classTag[ArcroleRef])
  }

  def findAllFootnoteLinks: immutable.IndexedSeq[FootnoteLink] = {
    findAllChildElemsOfType(classTag[FootnoteLink])
  }
}

// XLink elements in XBRL instances.

/**
 * An XLink element in an XBRL instances, obeying the constraints on XLink imposed by XBRL. For example, an XLink arc or extended link.
 *
 * XLink (see https://www.w3.org/TR/xlink11/) is a somewhat low level standard on top of XML, but it is
 * very important in an XBRL context (but more so in taxonomies than in instances). Some instance elements are also XLink elements.
 */
sealed trait XLinkElem extends XbrliElem with xlink.XLinkElem {

  def xlinkType: String

  final def key: XmlFragmentKey = backingElem.key

  final def xlinkAttributes: Map[EName, String] = {
    resolvedAttributes.toMap.filterKeys(_.namespaceUriOption.contains(Namespaces.XLinkNamespace))
  }
}

// TODO XLink title and documentation (abstract) elements have not been modeled (yet).

/**
 * Simple or extended XLink link.
 */
sealed trait XLinkLink extends XLinkElem with xlink.XLinkLink

/**
 * XLink child element of an extended link, so an XLink arc, locator or resource.
 */
sealed trait ChildXLink extends XLinkElem with xlink.ChildXLink {

  /**
   * Returns the extended link role of the surrounding extended link element.
   * This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption.flatMap(_.attributeOption(ENames.XLinkRoleEName))
   * }}}
   */
  final def elr: String = {
    underlyingParentElem.attribute(ENames.XLinkRoleEName)
  }

  /**
   * Returns the underlying parent element.
   * This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption
   * }}}
   */
  final def underlyingParentElem: BackingElemNodeApi = {
    backingElem.parent
  }
}

/**
 * XLink locator or resource.
 */
sealed trait LabeledXLink extends ChildXLink with xlink.LabeledXLink {

  /**
   * Returns the XLink label. This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkLabelEName)
   * }}}
   */
  final def xlinkLabel: String = {
    attribute(ENames.XLinkLabelEName)
  }

  final def roleOption: Option[String] = {
    attributeOption(ENames.XLinkRoleEName)
  }
}

/**
 * XLink extended link. For example, a footnote link.
 */
sealed trait ExtendedLink extends XLinkLink with xlink.ExtendedLink {

  final def xlinkType: String = {
    "extended"
  }

  /**
   * Returns the extended link role. This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkRoleEName)
   * }}}
   */
  final def role: String = {
    attribute(ENames.XLinkRoleEName)
  }

  final def xlinkChildren: immutable.IndexedSeq[ChildXLink] = {
    findAllChildElemsOfType(classTag[ChildXLink])
  }

  final def labeledXlinkChildren: immutable.IndexedSeq[LabeledXLink] = {
    findAllChildElemsOfType(classTag[LabeledXLink])
  }

  final def arcs: immutable.IndexedSeq[XLinkArc] = {
    findAllChildElemsOfType(classTag[XLinkArc])
  }

  /**
   * Returns the XLink locators and resources grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  final def labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]] = {
    labeledXlinkChildren.groupBy(_.xlinkLabel)
  }
}

/**
 * XLink arc.
 *
 * The xlink:from and xlink:to attributes point to XLink locators or resources
 * in the same extended link with the corresponding xlink:label attributes.
 */
sealed trait XLinkArc extends ChildXLink with xlink.XLinkArc {

  final def xlinkType: String = {
    "arc"
  }

  /**
   * Returns the arcrole. This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkArcroleEName)
   * }}}
   */
  final def arcrole: String = {
    attribute(ENames.XLinkArcroleEName)
  }

  /**
   * Returns the XLink "from". This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkFromEName)
   * }}}
   */
  final def from: String = {
    attribute(ENames.XLinkFromEName)
  }

  /**
   * Returns the XLink "to". This may fail with an exception if the instance is not schema-valid.
   *
   * If the instance is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkToEName)
   * }}}
   */
  final def to: String = {
    attribute(ENames.XLinkToEName)
  }
}

/**
 * XLink resource.
 */
sealed trait XLinkResource extends LabeledXLink with xlink.XLinkResource {

  final def xlinkType: String = {
    "resource"
  }
}

/**
 * XLink locator.
 */
sealed trait XLinkLocator extends LabeledXLink with xlink.XLinkLocator {

  final def xlinkType: String = {
    "locator"
  }

  /**
   * Returns the XLink href as URI. This may fail with an exception if the instance is not schema-valid.
   */
  final def rawHref: URI = {
    URI.create(attribute(ENames.XLinkHrefEName))
  }

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the instance
   * is not schema-valid.
   */
  final def resolvedHref: URI = {
    backingElem.baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
}

/**
 * XLink simple link.
 */
sealed trait SimpleLink extends XLinkLink with xlink.SimpleLink {

  final def xlinkType: String = {
    "simple"
  }

  final def arcroleOption: Option[String] = {
    attributeOption(ENames.XLinkArcroleEName)
  }

  final def roleOption: Option[String] = {
    attributeOption(ENames.XLinkRoleEName)
  }

  /**
   * Returns the XLink href as URI. This may fail with an exception if the instance is not schema-valid.
   */
  final def rawHref: URI = {
    URI.create(attribute(ENames.XLinkHrefEName))
  }

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the instance
   * is not schema-valid.
   */
  final def resolvedHref: URI = {
    backingElem.baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
}

/**
 * SchemaRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class SchemaRef private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with SimpleLink {

  require(resolvedName == LinkSchemaRefEName, s"Expected EName $LinkSchemaRefEName but found $resolvedName")
}

/**
 * LinkbaseRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class LinkbaseRef private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with SimpleLink {

  require(resolvedName == LinkLinkbaseRefEName, s"Expected EName $LinkLinkbaseRefEName but found $resolvedName")
}

/**
 * RoleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class RoleRef private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with SimpleLink {

  require(resolvedName == LinkRoleRefEName, s"Expected EName $LinkRoleRefEName but found $resolvedName")
}

/**
 * ArcroleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class ArcroleRef private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with SimpleLink {

  require(resolvedName == LinkArcroleRefEName, s"Expected EName $LinkArcroleRefEName but found $resolvedName")
}

/**
 * Context in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliContext private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliContextEName, s"Expected EName $XbrliContextEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def entity: Entity = {
    getChildElemOfType(classTag[Entity])(anyElem)
  }

  def period: Period = {
    getChildElemOfType(classTag[Period])(anyElem)
  }

  def scenarioOption: Option[Scenario] = {
    findChildElemOfType(classTag[Scenario])(anyElem)
  }

  def identifierScheme: String = {
    entity.identifierScheme
  }

  def identifierValue: String = {
    entity.identifierValue
  }

  def explicitDimensionMembers: Map[EName, EName] = {
    entity.segmentOption.map(_.explicitDimensionMembers).getOrElse(Map.empty) ++
      scenarioOption.map(_.explicitDimensionMembers).getOrElse(Map.empty)
  }

  def typedDimensionMembers: Map[EName, XbrliElem] = {
    entity.segmentOption.map(_.typedDimensionMembers).getOrElse(Map.empty) ++
      scenarioOption.map(_.typedDimensionMembers).getOrElse(Map.empty)
  }

  def typedDimensions: Set[EName] = {
    entity.segmentOption.map(_.typedDimensions).getOrElse(Set.empty) union
      scenarioOption.map(_.typedDimensions).getOrElse(Set.empty)
  }
}

/**
 * Unit in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliUnit private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliUnitEName, s"Expected EName $XbrliUnitEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def measures: immutable.IndexedSeq[EName] = {
    filterChildElems(XbrliMeasureEName) map (e => e.textAsResolvedQName)
  }

  def findDivide: Option[Divide] = {
    findChildElemOfType(classTag[Divide])(anyElem)
  }

  def divide: Divide = {
    getChildElemOfType(classTag[Divide])(anyElem)
  }

  def numeratorMeasures: immutable.IndexedSeq[EName] = {
    findDivide.map(_.numerator).getOrElse(measures)
  }

  def denominatorMeasures: immutable.IndexedSeq[EName] = {
    findDivide.map(_.denominator).getOrElse(immutable.IndexedSeq())
  }
}

/**
 * Item or tuple fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
abstract class Fact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  final def isTopLevel: Boolean = path.entries.size == 1

  final def isNil: Boolean = attributeOption(XsiNilEName).contains("true")

  /**
   * In the aspect model for instances, the concept core aspect.
   */
  final def conceptEName: EName = resolvedName

  final def path: Path = backingElem.path
}

/**
 * Item fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
abstract class ItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends Fact(backingElem, childElems) {

  require(attributeOption(ContextRefEName).isDefined, s"Expected attribute $ContextRefEName")

  final def contextRef: String = attribute(ContextRefEName)

  def unitRefOption: Option[String]
}

/**
 * Non-numeric item fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
final class NonNumericItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends ItemFact(backingElem, childElems) {

  require(attributeOption(UnitRefEName).isEmpty, s"Expected no attribute $UnitRefEName")

  def unitRefOption: Option[String] = None
}

/**
 * Numeric item fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
abstract class NumericItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends ItemFact(backingElem, childElems) {

  require(attributeOption(UnitRefEName).isDefined, s"Expected attribute $UnitRefEName")

  final def unitRef: String = attribute(UnitRefEName)

  final def unitRefOption: Option[String] = Some(unitRef)
}

/**
 * Nil numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NilNumericItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, childElems) {

  require(isNil, s"Expected nil numeric item fact")
}

/**
 * Non-nil non-fraction numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NonNilNonFractionNumericItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, childElems) {

  require(!isNil, s"Expected non-nil numeric item fact")

  def precisionOption: Option[String] = attributeOption(PrecisionEName)

  def decimalsOption: Option[String] = attributeOption(DecimalsEName)
}

/**
 * Non-nil fraction item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NonNilFractionItemFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, childElems) {

  require(!isNil, s"Expected non-nil numeric item fact")

  require(findAllChildElems.map(_.resolvedName).toSet == Set(XbrliNumeratorEName, XbrliDenominatorEName))

  def numerator: BigDecimal = {
    val s = getChildElem(XbrliNumeratorEName).text
    BigDecimal(s)
  }

  def denominator: BigDecimal = {
    val s = getChildElem(XbrliDenominatorEName).text
    BigDecimal(s)
  }
}

/**
 * Tuple fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
final class TupleFact private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends Fact(backingElem, childElems) {

  def findAllChildFacts: immutable.IndexedSeq[Fact] = {
    findAllChildElemsOfType(classTag[Fact])
  }

  def findAllFacts: immutable.IndexedSeq[Fact] = {
    findAllElemsOfType(classTag[Fact])
  }

  def filterChildFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterChildElemsOfType(classTag[Fact])(p)
  }

  def filterFacts(p: Fact => Boolean): immutable.IndexedSeq[Fact] = {
    filterElemsOfType(classTag[Fact])(p)
  }
}

/**
 * FootnoteLink in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class FootnoteLink private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with ExtendedLink {

  require(resolvedName == LinkFootnoteLinkEName, s"Expected EName $LinkFootnoteLinkEName but found $resolvedName")
}

/**
 * FootnoteArc in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class FootnoteArc private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with XLinkArc {

  require(resolvedName == LinkFootnoteArcEName, s"Expected EName $LinkFootnoteArcEName but found $resolvedName")
}

/**
 * Footnote in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class Footnote private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with XLinkResource {

  require(resolvedName == LinkFootnoteEName, s"Expected EName $LinkFootnoteEName but found $resolvedName")
}

/**
 * Standard locator in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class StandardLoc private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with XLinkLocator {

  require(resolvedName == LinkLocEName, s"Expected EName $LinkLocEName but found $resolvedName")
}

/**
 * Entity in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class Entity private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliEntityEName, s"Expected EName $XbrliEntityEName but found $resolvedName")

  def identifier: Identifier = {
    getChildElemOfType(classTag[Identifier])(anyElem)
  }

  def segmentOption: Option[Segment] = {
    findChildElemOfType(classTag[Segment])(anyElem)
  }

  def identifierScheme: String = {
    identifier.identifierScheme
  }

  def identifierValue: String = {
    identifier.identifierValue
  }
}

/**
 * Period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
abstract class Period private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliPeriodEName, s"Expected EName $XbrliPeriodEName but found $resolvedName")

  def isInstantPeriod: Boolean = {
    findChildElemOfType(classTag[Instant])(anyElem).isDefined
  }

  def isStartEndDatePeriod: Boolean = {
    findChildElemOfType(classTag[StartDate])(anyElem).isDefined
  }

  def isForeverPeriod: Boolean = {
    findChildElemOfType(classTag[Forever])(anyElem).isDefined
  }

  def asInstantPeriod: InstantPeriod = {
    require(isInstantPeriod, s"Not an instant period: $backingElem")
    this.asInstanceOf[InstantPeriod]
  }

  def asStartEndDatePeriod: StartEndDatePeriod = {
    require(isStartEndDatePeriod, s"Not a finite duration period: $backingElem")
    this.asInstanceOf[StartEndDatePeriod]
  }

  def asForeverPeriod: ForeverPeriod = {
    require(isForeverPeriod, s"Not a forever period: $backingElem")
    this.asInstanceOf[ForeverPeriod]
  }
}

/**
 * Instant period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class InstantPeriod private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, childElems) {

  require(isInstantPeriod)

  // TODO How about time zones?

  def instant: Instant = getChildElemOfType(classTag[Instant])(anyElem)

  def instantDateTime: LocalDateTime = instant.dateTime
}

/**
 * Start-end-date period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class StartEndDatePeriod private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, childElems) {

  require(isStartEndDatePeriod)

  // TODO How about time zones?

  def startDate: StartDate = getChildElemOfType(classTag[StartDate])(anyElem)

  def endDate: EndDate = getChildElemOfType(classTag[EndDate])(anyElem)

  def startDateTime: LocalDateTime = startDate.dateTime

  def endDateTime: LocalDateTime = endDate.dateTime
}

/**
 * Forever period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class ForeverPeriod private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, childElems) {

  require(isForeverPeriod)
}

/**
 * Instant in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class Instant private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliInstantEName, s"Expected EName $XbrliInstantEName but found $resolvedName")

  def dateTime: LocalDateTime = {
    Period.parseInstantOrEndDate(text)
  }
}

/**
 * Start date in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class StartDate private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliStartDateEName, s"Expected EName $XbrliStartDateEName but found $resolvedName")

  def dateTime: LocalDateTime = {
    Period.parseStartDate(text)
  }
}

/**
 * End date in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class EndDate private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliEndDateEName, s"Expected EName $XbrliEndDateEName but found $resolvedName")

  def dateTime: LocalDateTime = {
    Period.parseInstantOrEndDate(text)
  }
}

/**
 * Forver in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class Forever private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliForeverEName, s"Expected EName $XbrliForeverEName but found $resolvedName")
}

sealed trait MayContainDimensions extends XbrliElem {

  final def explicitMembers: immutable.IndexedSeq[ExplicitMember] = {
    findAllChildElemsOfType(classTag[ExplicitMember])
  }

  final def explicitDimensionMembers: Map[EName, EName] = {
    (explicitMembers map { e =>
      val dim = e.attributeAsResolvedQName(DimensionEName)
      val mem = e.textAsResolvedQName

      (dim -> mem)
    }).toMap
  }

  final def typedMembers: immutable.IndexedSeq[TypedMember] = {
    findAllChildElemsOfType(classTag[TypedMember])
  }

  final def typedDimensionMembers: Map[EName, XbrliElem] = {
    (typedMembers flatMap { e =>
      val dim = e.attributeAsResolvedQName(DimensionEName)
      val memOption = e.findAllChildElems.headOption

      memOption.map(mem => (dim -> mem))
    }).toMap
  }

  final def typedDimensions: Set[EName] = {
    typedMembers.map(_.dimension).toSet
  }
}

/**
 * Scenario in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class Scenario private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with MayContainDimensions {

  require(resolvedName == XbrliScenarioEName, s"Expected EName $XbrliScenarioEName but found $resolvedName")
}

/**
 * Segment in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Segment private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) with MayContainDimensions {

  require(resolvedName == XbrliSegmentEName, s"Expected EName $XbrliSegmentEName but found $resolvedName")
}

/**
 * Identifier in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Identifier private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliIdentifierEName, s"Expected EName $XbrliIdentifierEName but found $resolvedName")

  def identifierScheme: String = attribute(SchemeEName)

  def identifierValue: String = text
}

/**
 * Divide in an XBRL instance unit
 *
 * @author Chris de Vreeze
 */
final class Divide private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrliDivideEName, s"Expected EName $XbrliDivideEName but found $resolvedName")

  def numerator: immutable.IndexedSeq[EName] = {
    val unitNumerator = getChildElem(XbrliUnitNumeratorEName)
    val result = unitNumerator.filterChildElems(XbrliMeasureEName).map(e => e.textAsResolvedQName)
    result
  }

  def denominator: immutable.IndexedSeq[EName] = {
    val unitDenominator = getChildElem(XbrliUnitDenominatorEName)
    val result = unitDenominator.filterChildElems(XbrliMeasureEName).map(e => e.textAsResolvedQName)
    result
  }
}

final class ExplicitMember private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrldiExplicitMemberEName, s"Expected EName $XbrldiExplicitMemberEName but found $resolvedName")

  def dimension: EName = {
    attributeAsResolvedQName(DimensionEName)
  }

  def member: EName = {
    textAsResolvedQName
  }
}

final class TypedMember private[instance] (
  override val backingElem: BackingElemNodeApi,
  childElems:               immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, childElems) {

  require(resolvedName == XbrldiTypedMemberEName, s"Expected EName $XbrldiTypedMemberEName but found $resolvedName")

  def dimension: EName = {
    attributeAsResolvedQName(DimensionEName)
  }

  def member: XbrliElem = {
    findAllChildElems.headOption.getOrElse(
      sys.error(s"Missing typed dimension member in element ${backingElem.key}"))
  }
}

// Companion objects

object XbrliElem {

  val XbrliNs = "http://www.xbrl.org/2003/instance"
  val LinkNs = "http://www.xbrl.org/2003/linkbase"
  val XmlNs = "http://www.w3.org/XML/1998/namespace"
  val XsiNs = "http://www.w3.org/2001/XMLSchema-instance"
  val XbrldiNs = "http://xbrl.org/2006/xbrldi"

  val XbrliXbrlEName = EName(XbrliNs, "xbrl")
  val XbrliContextEName = EName(XbrliNs, "context")
  val XbrliUnitEName = EName(XbrliNs, "unit")
  val XbrliEntityEName = EName(XbrliNs, "entity")
  val XbrliPeriodEName = EName(XbrliNs, "period")
  val XbrliScenarioEName = EName(XbrliNs, "scenario")
  val XbrliIdentifierEName = EName(XbrliNs, "identifier")
  val XbrliSegmentEName = EName(XbrliNs, "segment")
  val XbrliInstantEName = EName(XbrliNs, "instant")
  val XbrliStartDateEName = EName(XbrliNs, "startDate")
  val XbrliEndDateEName = EName(XbrliNs, "endDate")
  val XbrliForeverEName = EName(XbrliNs, "forever")
  val XbrliMeasureEName = EName(XbrliNs, "measure")
  val XbrliDivideEName = EName(XbrliNs, "divide")
  val XbrliNumeratorEName = EName(XbrliNs, "numerator")
  val XbrliDenominatorEName = EName(XbrliNs, "denominator")
  val XbrliUnitNumeratorEName = EName(XbrliNs, "unitNumerator")
  val XbrliUnitDenominatorEName = EName(XbrliNs, "unitDenominator")

  val XbrldiExplicitMemberEName = EName(XbrldiNs, "explicitMember")
  val XbrldiTypedMemberEName = EName(XbrldiNs, "typedMember")

  val LinkSchemaRefEName = EName(LinkNs, "schemaRef")
  val LinkLinkbaseRefEName = EName(LinkNs, "linkbaseRef")
  val LinkRoleRefEName = EName(LinkNs, "roleRef")
  val LinkArcroleRefEName = EName(LinkNs, "arcroleRef")
  val LinkFootnoteLinkEName = EName(LinkNs, "footnoteLink")
  val LinkFootnoteArcEName = EName(LinkNs, "footnoteArc")
  val LinkFootnoteEName = EName(LinkNs, "footnote")
  val LinkLocEName = EName(LinkNs, "loc")

  val XmlLangEName = EName(XmlNs, "lang")

  val XsiNilEName = EName(XsiNs, "nil")

  val IdEName = EName("id")
  val ContextRefEName = EName("contextRef")
  val UnitRefEName = EName("unitRef")
  val PrecisionEName = EName("precision")
  val DecimalsEName = EName("decimals")
  val SchemeEName = EName("scheme")
  val DimensionEName = EName("dimension")

  /**
   * Expensive method to create an XbrliElem tree
   */
  def build(elem: BackingElemNodeApi): XbrliElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => build(e))
    apply(elem, childElems)
  }

  private[instance] def apply(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {
    elem.resolvedName.namespaceUriOption match {
      case Some(XbrliNs)  => applyForXbrliNamespace(elem, childElems)
      case Some(LinkNs)   => applyForLinkNamespace(elem, childElems)
      case Some(XbrldiNs) => applyForXbrldiNamespace(elem, childElems)
      case _              => applyForOtherNamespace(elem, childElems)
    }
  }

  private[instance] def applyForXbrliNamespace(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {
    elem.resolvedName match {
      case XbrliXbrlEName                           => new XbrlInstance(elem, childElems)
      case XbrliContextEName                        => new XbrliContext(elem, childElems)
      case XbrliUnitEName                           => new XbrliUnit(elem, childElems)
      case XbrliEntityEName                         => new Entity(elem, childElems)
      case XbrliPeriodEName if Period.accepts(elem) => Period(elem, childElems)
      case XbrliScenarioEName                       => new Scenario(elem, childElems)
      case XbrliSegmentEName                        => new Segment(elem, childElems)
      case XbrliIdentifierEName                     => new Identifier(elem, childElems)
      case XbrliDivideEName                         => new Divide(elem, childElems)
      case XbrliInstantEName                        => new Instant(elem, childElems)
      case XbrliStartDateEName                      => new StartDate(elem, childElems)
      case XbrliEndDateEName                        => new EndDate(elem, childElems)
      case XbrliForeverEName                        => new Forever(elem, childElems)
      case _                                        => new XbrliElem(elem, childElems)
    }
  }

  private[instance] def applyForLinkNamespace(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {
    elem.resolvedName match {
      case LinkSchemaRefEName    => new SchemaRef(elem, childElems)
      case LinkLinkbaseRefEName  => new LinkbaseRef(elem, childElems)
      case LinkRoleRefEName      => new RoleRef(elem, childElems)
      case LinkArcroleRefEName   => new ArcroleRef(elem, childElems)
      case LinkFootnoteLinkEName => new FootnoteLink(elem, childElems)
      case LinkFootnoteArcEName  => new FootnoteArc(elem, childElems)
      case LinkFootnoteEName     => new Footnote(elem, childElems)
      case LinkLocEName          => new StandardLoc(elem, childElems)
      case _                     => new XbrliElem(elem, childElems)
    }
  }

  private[instance] def applyForXbrldiNamespace(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {
    elem.resolvedName match {
      case XbrldiExplicitMemberEName => new ExplicitMember(elem, childElems)
      case XbrldiTypedMemberEName    => new TypedMember(elem, childElems)
      case _                         => new XbrliElem(elem, childElems)
    }
  }

  private[instance] def applyForOtherNamespace(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {
    elem.resolvedName match {
      case _ if Fact.accepts(elem) => Fact(elem, childElems)
      case _                       => new XbrliElem(elem, childElems)
    }
  }
}

object XbrlInstance {

  def build(elem: BackingElemNodeApi): XbrlInstance = {
    require(elem.resolvedName == XbrliXbrlEName)
    XbrliElem.build(elem).asInstanceOf[XbrlInstance]
  }
}

object Period {

  def accepts(elem: BackingElemNodeApi): Boolean = {
    elem.resolvedName == XbrliPeriodEName && (isInstant(elem) || isFiniteDuration(elem) || isForever(elem))
  }

  private[instance] def apply(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): Period = {
    if (isInstant(elem)) new InstantPeriod(elem, childElems)
    else if (isFiniteDuration(elem)) new StartEndDatePeriod(elem, childElems)
    else new ForeverPeriod(elem, childElems)
  }

  private def isInstant(elem: BackingElemNodeApi): Boolean = {
    elem.findChildElem(XbrliInstantEName).isDefined
  }

  private def isFiniteDuration(elem: BackingElemNodeApi): Boolean = {
    elem.findChildElem(XbrliStartDateEName).isDefined
  }

  private def isForever(elem: BackingElemNodeApi): Boolean = {
    elem.findChildElem(XbrliForeverEName).isDefined
  }

  private[instance] def parseStartDate(s: String): LocalDateTime = {
    if (s.contains('T')) {
      LocalDateTime.parse(s)
    } else {
      LocalDate.parse(s).atStartOfDay
    }
  }

  private[instance] def parseInstantOrEndDate(s: String): LocalDateTime = {
    if (s.contains('T')) {
      LocalDateTime.parse(s)
    } else {
      LocalDate.parse(s).plusDays(1).atStartOfDay
    }
  }
}

object Fact {

  def accepts(elem: BackingElemNodeApi): Boolean = ItemFact.accepts(elem) || TupleFact.accepts(elem)

  private[instance] def apply(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): Fact =
    if (ItemFact.accepts(elem)) ItemFact(elem, childElems) else TupleFact(elem, childElems)

  def isFactPath(path: Path): Boolean = {
    !path.isEmpty &&
      !Set(Option(LinkNs), Option(XbrliNs)).contains(path.firstEntry.elementName.namespaceUriOption)
  }
}

object ItemFact {

  def accepts(elem: BackingElemNodeApi): Boolean = {
    Fact.isFactPath(elem.path) &&
      elem.attributeOption(ContextRefEName).isDefined
  }

  private[instance] def apply(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): ItemFact = {
    require(Fact.isFactPath(elem.path))
    require(elem.attributeOption(ContextRefEName).isDefined)

    val unitRefOption = elem.attributeOption(UnitRefEName)

    if (unitRefOption.isEmpty) new NonNumericItemFact(elem, childElems)
    else {
      if (elem.attributeOption(XsiNilEName).contains("true"))
        new NilNumericItemFact(elem, childElems)
      else if (elem.findChildElem(withEName(XbrliNumeratorEName)).isDefined)
        new NonNilFractionItemFact(elem, childElems)
      else
        new NonNilNonFractionNumericItemFact(elem, childElems)
    }
  }
}

object TupleFact {

  def accepts(elem: BackingElemNodeApi): Boolean = {
    Fact.isFactPath(elem.path) &&
      elem.attributeOption(ContextRefEName).isEmpty
  }

  private[instance] def apply(elem: BackingElemNodeApi, childElems: immutable.IndexedSeq[XbrliElem]): TupleFact = {
    require(Fact.isFactPath(elem.path))
    require(elem.attributeOption(ContextRefEName).isEmpty)

    new TupleFact(elem, childElems)
  }
}
