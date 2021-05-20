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
import java.time.ZonedDateTime
import java.time.temporal.Temporal

import scala.collection.immutable
import scala.reflect.classTag
import scala.util.Try

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
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ClarkElemApi.withEName
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * "XBRL instance DOM node".
 *
 * @author Chris de Vreeze
 */
sealed abstract class XbrliNode extends ScopedNodes.Node

sealed abstract class CanBeXbrliDocumentChild extends XbrliNode with ScopedNodes.CanBeDocumentChild

final case class XbrliTextNode(text: String) extends XbrliNode with ScopedNodes.Text

final case class XbrliProcessingInstructionNode(target: String, data: String) extends CanBeXbrliDocumentChild
  with ScopedNodes.ProcessingInstruction

final case class XbrliCommentNode(text: String) extends CanBeXbrliDocumentChild with ScopedNodes.Comment

/**
 * XML element inside XBRL instance (or the entire XBRL instance itself). This API is immutable, provided
 * the backing element is immutable.
 *
 * The yaidom `SubtypeAwareElemApi` and `ScopedElemApi` query API is offered.
 *
 * Also note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying,
 * at the expense of (expensive recursive element) creation.
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
 * Note that the backing element implementation can be any implementation of yaidom query API trait `BackingNodes.Elem`.
 *
 * This class hierarchy depends on Java 8 or later, due to the use of Java 8 time API.
 *
 * Creation of `XbrliElem` objects is designed not to fail, even if the XML element is not an XBRL instance or part thereof.
 * Of course, after creation many query methods may fail in such cases. It is also possible to use these data classes for
 * XBRL instances embedded in other XML elements, or only for parts of XBRL instances. As an example of the latter, table layout
 * models may contain pieces of XBRL context data, such as periods. Their parent elements can be parsed into an `XbrliElem`,
 * thus offering the XBRL instance query API on such XBRL context data.
 */
sealed abstract class XbrliElem private[instance] (
  val backingElem: BackingNodes.Elem,
  val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends CanBeXbrliDocumentChild
  with ScopedNodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  // TODO Restore old equality on the backing elements themselves (after JS DOM wrappers have appropriate equality)
  assert(
    childElems.map(_.backingElem).map(_.resolvedName) == backingElem.findAllChildElems.map(_.resolvedName),
    msg("Corrupt element!"))

  type ThisElem = XbrliElem

  type ThisNode = XbrliNode

  final def thisElem: ThisElem = this

  final def children: immutable.IndexedSeq[XbrliNode] = {
    var childElemIdx = 0

    backingElem.children.flatMap {
      case che: BackingNodes.Elem =>
        val e = childElems(childElemIdx)
        childElemIdx += 1
        Some(e)
      case ch: BackingNodes.Text =>
        Some(XbrliTextNode(ch.text))
      case ch: BackingNodes.Comment =>
        Some(XbrliCommentNode(ch.text))
      case ch: BackingNodes.ProcessingInstruction =>
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
    case _ => false
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem],
  allContextsById: Map[String, XbrliContext],
  allUnitsById: Map[String, XbrliUnit],
  allFactsByEName: Map[EName, immutable.IndexedSeq[Fact]]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliXbrlEName, s"Expected EName $XbrliXbrlEName but found $resolvedName")

  // Find/get methods using some key

  def findContextById(id: String): Option[XbrliContext] = {
    allContextsById.get(id)
  }

  def getContextById(id: String): XbrliContext = {
    allContextsById.getOrElse(id, sys.error(s"Missing context with ID $id"))
  }

  def findUnitById(id: String): Option[XbrliUnit] = {
    allUnitsById.get(id)
  }

  def getUnitById(id: String): XbrliUnit = {
    allUnitsById.getOrElse(id, sys.error(s"Missing unit with ID $id"))
  }

  def filterFactsByEName(factName: EName): immutable.IndexedSeq[Fact] = {
    allFactsByEName.getOrElse(factName, immutable.IndexedSeq())
  }

  def filterItemsByEName(factName: EName): immutable.IndexedSeq[ItemFact] = {
    filterFactsByEName(factName).collect { case f: ItemFact => f }
  }

  def filterTuplesByEName(factName: EName): immutable.IndexedSeq[TupleFact] = {
    filterFactsByEName(factName).collect { case f: TupleFact => f }
  }

  // FindAll/filter methods

  def findAllContexts: immutable.IndexedSeq[XbrliContext] = {
    findAllChildElemsOfType(classTag[XbrliContext])
  }

  def findAllUnits: immutable.IndexedSeq[XbrliUnit] = {
    findAllChildElemsOfType(classTag[XbrliUnit])
  }

  def filterContexts(p: XbrliContext => Boolean): immutable.IndexedSeq[XbrliContext] = {
    filterChildElemsOfType(classTag[XbrliContext])(p)
  }

  def filterUnits(p: XbrliUnit => Boolean): immutable.IndexedSeq[XbrliUnit] = {
    filterChildElemsOfType(classTag[XbrliUnit])(p)
  }

  def findAllTopLevelFacts: immutable.IndexedSeq[Fact] = {
    findAllChildElemsOfType(classTag[Fact])
  }

  def findAllTopLevelItems: immutable.IndexedSeq[ItemFact] = {
    findAllChildElemsOfType(classTag[ItemFact])
  }

  def findAllTopLevelTuples: immutable.IndexedSeq[TupleFact] = {
    findAllChildElemsOfType(classTag[TupleFact])
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
    resolvedAttributes.toMap.filter(_._1.namespaceUriOption.contains(Namespaces.XLinkNamespace))
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
  final def underlyingParentElem: BackingNodes.Elem = {
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with SimpleLink {

  require(resolvedName == LinkSchemaRefEName, s"Expected EName $LinkSchemaRefEName but found $resolvedName")
}

/**
 * LinkbaseRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class LinkbaseRef private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with SimpleLink {

  require(resolvedName == LinkLinkbaseRefEName, s"Expected EName $LinkLinkbaseRefEName but found $resolvedName")
}

/**
 * RoleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class RoleRef private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with SimpleLink {

  require(resolvedName == LinkRoleRefEName, s"Expected EName $LinkRoleRefEName but found $resolvedName")
}

/**
 * ArcroleRef in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class ArcroleRef private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with SimpleLink {

  require(resolvedName == LinkArcroleRefEName, s"Expected EName $LinkArcroleRefEName but found $resolvedName")
}

/**
 * Context in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class XbrliContext private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliContextEName, s"Expected EName $XbrliContextEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def entity: Entity = {
    getChildElemOfType(classTag[Entity])(_ => true)
  }

  def period: Period = {
    getChildElemOfType(classTag[Period])(_ => true)
  }

  def scenarioOption: Option[Scenario] = {
    findChildElemOfType(classTag[Scenario])(_ => true)
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliUnitEName, s"Expected EName $XbrliUnitEName but found $resolvedName")

  def id: String = attribute(IdEName)

  def measures: immutable.IndexedSeq[EName] = {
    filterChildElems(XbrliMeasureEName).map (e => e.textAsResolvedQName)
  }

  def findDivide: Option[Divide] = {
    findChildElemOfType(classTag[Divide])(_ => true)
  }

  def divide: Divide = {
    getChildElemOfType(classTag[Divide])(_ => true)
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  final def isTopLevel: Boolean = {
    backingElem.parentOption.exists(_.resolvedName == XbrliXbrlEName)
  }

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(backingElem, ancestorOrSelfENames, childElems) {

  require(attributeOption(UnitRefEName).isEmpty, s"Expected no attribute $UnitRefEName")

  def unitRefOption: Option[String] = None
}

/**
 * Numeric item fact in an XBRL instance, either top-level or nested (and either non-nil or nil)
 *
 * @author Chris de Vreeze
 */
abstract class NumericItemFact private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends ItemFact(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, ancestorOrSelfENames, childElems) {

  require(isNil, s"Expected nil numeric item fact")
}

/**
 * Non-nil non-fraction numeric item fact in an XBRL instance, either top-level or nested
 *
 * @author Chris de Vreeze
 */
final class NonNilNonFractionNumericItemFact private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends NumericItemFact(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends Fact(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with ExtendedLink {

  require(resolvedName == LinkFootnoteLinkEName, s"Expected EName $LinkFootnoteLinkEName but found $resolvedName")
}

/**
 * FootnoteArc in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class FootnoteArc private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with XLinkArc {

  require(resolvedName == LinkFootnoteArcEName, s"Expected EName $LinkFootnoteArcEName but found $resolvedName")
}

/**
 * Footnote in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class Footnote private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with XLinkResource {

  require(resolvedName == LinkFootnoteEName, s"Expected EName $LinkFootnoteEName but found $resolvedName")
}

/**
 * Standard locator in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class StandardLoc private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with XLinkLocator {

  require(resolvedName == LinkLocEName, s"Expected EName $LinkLocEName but found $resolvedName")
}

/**
 * Entity in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class Entity private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliEntityEName, s"Expected EName $XbrliEntityEName but found $resolvedName")

  def identifier: Identifier = {
    getChildElemOfType(classTag[Identifier])(_ => true)
  }

  def segmentOption: Option[Segment] = {
    findChildElemOfType(classTag[Segment])(_ => true)
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliPeriodEName, s"Expected EName $XbrliPeriodEName but found $resolvedName")

  def isInstantPeriod: Boolean = {
    findChildElemOfType(classTag[Instant])(_ => true).isDefined
  }

  def isStartEndDatePeriod: Boolean = {
    findChildElemOfType(classTag[StartDate])(_ => true).isDefined
  }

  def isForeverPeriod: Boolean = {
    findChildElemOfType(classTag[Forever])(_ => true).isDefined
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, ancestorOrSelfENames, childElems) {

  require(isInstantPeriod)

  def instant: Instant = getChildElemOfType(classTag[Instant])(_ => true)

  def instantDateTime: Temporal = instant.dateTime
}

/**
 * Start-end-date period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class StartEndDatePeriod private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, ancestorOrSelfENames, childElems) {

  require(isStartEndDatePeriod)

  def startDate: StartDate = getChildElemOfType(classTag[StartDate])(_ => true)

  def endDate: EndDate = getChildElemOfType(classTag[EndDate])(_ => true)

  def startDateTime: Temporal = startDate.dateTime

  def endDateTime: Temporal = endDate.dateTime
}

/**
 * Forever period in an XBRL instance context
 *
 * @author Chris de Vreeze
 */
final class ForeverPeriod private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends Period(backingElem, ancestorOrSelfENames, childElems) {

  require(isForeverPeriod)
}

/**
 * Instant in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class Instant private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliInstantEName, s"Expected EName $XbrliInstantEName but found $resolvedName")

  def dateTime: Temporal = {
    Period.parseInstantOrEndDate(text)
  }
}

/**
 * Start date in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class StartDate private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliStartDateEName, s"Expected EName $XbrliStartDateEName but found $resolvedName")

  def dateTime: Temporal = {
    Period.parseStartDate(text)
  }
}

/**
 * End date in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class EndDate private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliEndDateEName, s"Expected EName $XbrliEndDateEName but found $resolvedName")

  def dateTime: Temporal = {
    Period.parseInstantOrEndDate(text)
  }
}

/**
 * Forver in an XBRL instance context's period
 *
 * @author Chris de Vreeze
 */
final class Forever private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrliForeverEName, s"Expected EName $XbrliForeverEName but found $resolvedName")
}

sealed trait MayContainDimensions extends XbrliElem {

  final def explicitMembers: immutable.IndexedSeq[ExplicitMember] = {
    findAllChildElemsOfType(classTag[ExplicitMember])
  }

  final def explicitDimensionMembers: Map[EName, EName] = {
    (explicitMembers.map { e =>
      val dim = e.attributeAsResolvedQName(DimensionEName)
      val mem = e.textAsResolvedQName

      (dim -> mem)
    }).toMap
  }

  final def typedMembers: immutable.IndexedSeq[TypedMember] = {
    findAllChildElemsOfType(classTag[TypedMember])
  }

  final def typedDimensionMembers: Map[EName, XbrliElem] = {
    (typedMembers.flatMap { e =>
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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with MayContainDimensions {

  require(resolvedName == XbrliScenarioEName, s"Expected EName $XbrliScenarioEName but found $resolvedName")
}

/**
 * Segment in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Segment private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) with MayContainDimensions {

  require(resolvedName == XbrliSegmentEName, s"Expected EName $XbrliSegmentEName but found $resolvedName")
}

/**
 * Identifier in an XBRL instance context entity
 *
 * @author Chris de Vreeze
 */
final class Identifier private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

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
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrldiExplicitMemberEName, s"Expected EName $XbrldiExplicitMemberEName but found $resolvedName")

  def dimension: EName = {
    attributeAsResolvedQName(DimensionEName)
  }

  def member: EName = {
    textAsResolvedQName
  }
}

final class TypedMember private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems) {

  require(resolvedName == XbrldiTypedMemberEName, s"Expected EName $XbrldiTypedMemberEName but found $resolvedName")

  def dimension: EName = {
    attributeAsResolvedQName(DimensionEName)
  }

  def member: XbrliElem = {
    findAllChildElems.headOption.getOrElse(
      sys.error(s"Missing typed dimension member in element ${backingElem.key}"))
  }
}

/**
 * Other element in an XBRL instance
 *
 * @author Chris de Vreeze
 */
final class OtherXbrliElem private[instance] (
  override val backingElem: BackingNodes.Elem,
  override val ancestorOrSelfENames: List[EName],
  childElems: immutable.IndexedSeq[XbrliElem]) extends XbrliElem(backingElem, ancestorOrSelfENames, childElems)

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
  def build(elem: BackingNodes.Elem): XbrliElem = {
    val ancestorOrSelfENames: List[EName] = elem.ancestorsOrSelf.map(_.resolvedName).toList

    build(elem, ancestorOrSelfENames)
  }

  /**
   * Expensive recursive method to create an XbrliElem tree
   */
  private[instance] def build(elem: BackingNodes.Elem, ancestorOrSelfENames: List[EName]): XbrliElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => build(e, e.resolvedName :: ancestorOrSelfENames))

    apply(elem, ancestorOrSelfENames, childElems)
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {

    elem.resolvedName.namespaceUriOption match {
      case Some(XbrliNs) => applyForXbrliNamespace(elem, ancestorOrSelfENames, childElems)
      case Some(LinkNs) => applyForLinkNamespace(elem, ancestorOrSelfENames, childElems)
      case Some(XbrldiNs) => applyForXbrldiNamespace(elem, ancestorOrSelfENames, childElems)
      case _ => applyForOtherNamespace(elem, ancestorOrSelfENames, childElems)
    }
  }

  private[instance] def applyForXbrliNamespace(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {

    elem.resolvedName match {
      case XbrliXbrlEName =>
        XbrlInstance(elem, ancestorOrSelfENames, childElems)
      case XbrliContextEName => new XbrliContext(elem, ancestorOrSelfENames, childElems)
      case XbrliUnitEName => new XbrliUnit(elem, ancestorOrSelfENames, childElems)
      case XbrliEntityEName => new Entity(elem, ancestorOrSelfENames, childElems)
      case XbrliPeriodEName if Period.accepts(elem) =>
        Period(elem, ancestorOrSelfENames, childElems)
      case XbrliScenarioEName => new Scenario(elem, ancestorOrSelfENames, childElems)
      case XbrliSegmentEName => new Segment(elem, ancestorOrSelfENames, childElems)
      case XbrliIdentifierEName => new Identifier(elem, ancestorOrSelfENames, childElems)
      case XbrliDivideEName => new Divide(elem, ancestorOrSelfENames, childElems)
      case XbrliInstantEName => new Instant(elem, ancestorOrSelfENames, childElems)
      case XbrliStartDateEName => new StartDate(elem, ancestorOrSelfENames, childElems)
      case XbrliEndDateEName => new EndDate(elem, ancestorOrSelfENames, childElems)
      case XbrliForeverEName => new Forever(elem, ancestorOrSelfENames, childElems)
      case _ => new OtherXbrliElem(elem, ancestorOrSelfENames, childElems)
    }
  }

  private[instance] def applyForLinkNamespace(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {

    elem.resolvedName match {
      case LinkSchemaRefEName => new SchemaRef(elem, ancestorOrSelfENames, childElems)
      case LinkLinkbaseRefEName => new LinkbaseRef(elem, ancestorOrSelfENames, childElems)
      case LinkRoleRefEName => new RoleRef(elem, ancestorOrSelfENames, childElems)
      case LinkArcroleRefEName => new ArcroleRef(elem, ancestorOrSelfENames, childElems)
      case LinkFootnoteLinkEName => new FootnoteLink(elem, ancestorOrSelfENames, childElems)
      case LinkFootnoteArcEName => new FootnoteArc(elem, ancestorOrSelfENames, childElems)
      case LinkFootnoteEName => new Footnote(elem, ancestorOrSelfENames, childElems)
      case LinkLocEName => new StandardLoc(elem, ancestorOrSelfENames, childElems)
      case _ => new OtherXbrliElem(elem, ancestorOrSelfENames, childElems)
    }
  }

  private[instance] def applyForXbrldiNamespace(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {

    elem.resolvedName match {
      case XbrldiExplicitMemberEName => new ExplicitMember(elem, ancestorOrSelfENames, childElems)
      case XbrldiTypedMemberEName => new TypedMember(elem, ancestorOrSelfENames, childElems)
      case _ => new OtherXbrliElem(elem, ancestorOrSelfENames, childElems)
    }
  }

  private[instance] def applyForOtherNamespace(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrliElem = {

    elem.resolvedName match {
      case _ if Fact.accepts(elem, ancestorOrSelfENames) =>
        Fact(elem, ancestorOrSelfENames, childElems)
      case _ =>
        new OtherXbrliElem(elem, ancestorOrSelfENames, childElems)
    }
  }
}

object XbrlInstance {

  def build(elem: BackingNodes.Elem): XbrlInstance = {
    require(elem.resolvedName == XbrliXbrlEName, s"Expected $XbrliXbrlEName but found ${elem.resolvedName}")
    XbrliElem.build(elem).asInstanceOf[XbrlInstance]
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): XbrlInstance = {

    val (nonFacts, facts) = childElems.partition { e =>
      e.resolvedName.namespaceUriOption.contains(XbrliNs) ||
        e.resolvedName.namespaceUriOption.contains(LinkNs)
    }

    val allContextsById: Map[String, XbrliContext] =
      nonFacts.collect { case e: XbrliContext => e }.groupBy(_.id).view.mapValues(_.head).toMap

    val allUnitsById: Map[String, XbrliUnit] =
      nonFacts.collect { case e: XbrliUnit => e }.groupBy(_.id).view.mapValues(_.head).toMap

    val allFactsByEName: Map[EName, immutable.IndexedSeq[Fact]] =
      facts.flatMap(_.findAllElemsOrSelfOfType(classTag[Fact])).groupBy(_.resolvedName)

    new XbrlInstance(elem, ancestorOrSelfENames, childElems, allContextsById, allUnitsById, allFactsByEName)
  }
}

object Period {

  def accepts(elem: BackingNodes.Elem): Boolean = {
    elem.resolvedName == XbrliPeriodEName && (isInstant(elem) || isFiniteDuration(elem) || isForever(elem))
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): Period = {

    if (isInstant(elem)) new InstantPeriod(elem, ancestorOrSelfENames, childElems)
    else if (isFiniteDuration(elem)) new StartEndDatePeriod(elem, ancestorOrSelfENames, childElems)
    else new ForeverPeriod(elem, ancestorOrSelfENames, childElems)
  }

  private def isInstant(elem: BackingNodes.Elem): Boolean = {
    elem.findChildElem(XbrliInstantEName).isDefined
  }

  private def isFiniteDuration(elem: BackingNodes.Elem): Boolean = {
    elem.findChildElem(XbrliStartDateEName).isDefined
  }

  private def isForever(elem: BackingNodes.Elem): Boolean = {
    elem.findChildElem(XbrliForeverEName).isDefined
  }

  private[instance] def parseStartDate(s: String): Temporal = {
    if (s.contains('T')) {
      Try(LocalDateTime.parse(s))
        .getOrElse(ZonedDateTime.parse(s))
    } else {
      LocalDate.parse(s).atStartOfDay
    }
  }

  private[instance] def parseInstantOrEndDate(s: String): Temporal = {
    if (s.contains('T')) {
      Try(LocalDateTime.parse(s))
        .getOrElse(ZonedDateTime.parse(s))
    } else {
      LocalDate.parse(s).plusDays(1).atStartOfDay
    }
  }
}

object Fact {

  def accepts(elem: BackingNodes.Elem): Boolean = {
    val ancestorOrSelfENames: List[EName] = elem.ancestorsOrSelf.map(_.resolvedName).toList

    accepts(elem, ancestorOrSelfENames)
  }

  private[instance] def accepts(elem: BackingNodes.Elem, ancestorOrSelfENames: List[EName]): Boolean = {
    require(ancestorOrSelfENames.nonEmpty)

    val xbrliOrLinkAncestorENameOption: Option[EName] =
      ancestorOrSelfENames.tail.find(en => en.namespaceUriOption.contains(XbrliNs) || en.namespaceUriOption.contains(LinkNs))

    xbrliOrLinkAncestorENameOption.contains(XbrliXbrlEName)
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): Fact = {

    if (elem.attributeOption(ContextRefEName).isDefined) {
      ItemFact(elem, ancestorOrSelfENames, childElems)
    } else {
      TupleFact(elem, ancestorOrSelfENames, childElems)
    }
  }
}

object ItemFact {

  def accepts(elem: BackingNodes.Elem): Boolean = {
    val ancestorOrSelfENames: List[EName] = elem.ancestorsOrSelf.map(_.resolvedName).toList

    accepts(elem, ancestorOrSelfENames)
  }

  private[instance] def accepts(elem: BackingNodes.Elem, ancestorOrSelfENames: List[EName]): Boolean = {
    Fact.accepts(elem, ancestorOrSelfENames) && elem.attributeOption(ContextRefEName).isDefined
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): ItemFact = {

    val unitRefOption = elem.attributeOption(UnitRefEName)

    if (unitRefOption.isEmpty) new NonNumericItemFact(elem, ancestorOrSelfENames, childElems)
    else {
      if (elem.attributeOption(XsiNilEName).contains("true"))
        new NilNumericItemFact(elem, ancestorOrSelfENames, childElems)
      else if (elem.findChildElem(withEName(XbrliNumeratorEName)).isDefined)
        new NonNilFractionItemFact(elem, ancestorOrSelfENames, childElems)
      else
        new NonNilNonFractionNumericItemFact(elem, ancestorOrSelfENames, childElems)
    }
  }
}

object TupleFact {

  def accepts(elem: BackingNodes.Elem): Boolean = {
    val ancestorOrSelfENames: List[EName] = elem.ancestorsOrSelf.map(_.resolvedName).toList

    accepts(elem, ancestorOrSelfENames)
  }

  private[instance] def accepts(elem: BackingNodes.Elem, ancestorOrSelfENames: List[EName]): Boolean = {
    Fact.accepts(elem, ancestorOrSelfENames) && elem.attributeOption(ContextRefEName).isEmpty
  }

  private[instance] def apply(
    elem: BackingNodes.Elem,
    ancestorOrSelfENames: List[EName],
    childElems: immutable.IndexedSeq[XbrliElem]): TupleFact = {

    new TupleFact(elem, ancestorOrSelfENames, childElems)
  }
}
