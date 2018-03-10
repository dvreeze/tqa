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

package eu.cdevreeze.tqa.base.dom

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces.LinkNamespace
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.tqa.Namespaces.XsNamespace
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.CyclesAllowed
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.base.common.StandardLabelRoles
import eu.cdevreeze.tqa.base.common.StandardReferenceRoles
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.base.common.Variety
import eu.cdevreeze.tqa.xlink
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem
import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * "Taxonomy DOM node".
 *
 * @author Chris de Vreeze
 */
sealed abstract class TaxonomyNode extends Nodes.Node

sealed abstract class CanBeTaxonomyDocumentChild extends TaxonomyNode with Nodes.CanBeDocumentChild

final case class TaxonomyTextNode(text: String) extends TaxonomyNode with Nodes.Text

final case class TaxonomyProcessingInstructionNode(target: String, data: String) extends CanBeTaxonomyDocumentChild
  with Nodes.ProcessingInstruction

final case class TaxonomyCommentNode(text: String) extends CanBeTaxonomyDocumentChild with Nodes.Comment

/**
 * Any element in a taxonomy schema or linkbase document. The classes in this class hierarchy offer the yaidom query API,
 * in particular the `ScopedElemApi` and `SubtypeAwareElemApi` query API.
 *
 * ==Usage==
 *
 * Suppose we have an [[eu.cdevreeze.tqa.base.dom.XsdSchema]] called `schema`. Then we can find all global element declarations in
 * this schema as follows:
 *
 * {{{
 * import scala.reflect.classTag
 * import eu.cdevreeze.tqa.ENames
 * import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
 *
 * // Low level yaidom query, returning the result XML elements as TaxonomyElem elements
 * val globalElemDecls1 = schema.filterChildElems(_.resolvedName == ENames.XsElementEName)
 *
 * // Higher level yaidom query, querying for the type GlobalElementDeclaration
 * // Prefer this to the lower level yaidom query above
 * val globalElemDecls2 = schema.findAllChildElemsOfType(classTag[GlobalElementDeclaration])
 *
 * // The following query would have given the same result, because all global element declarations
 * // are child elements of the schema root. Instead of child elements, we now query for all
 * // descendant-or-self elements that are global element declarations
 * val globalElemDecls3 = schema.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration])
 *
 * // We can query the schema for global element declarations directly, so let's do that
 * val globalElemDecls4 = schema.findAllGlobalElementDeclarations
 * }}}
 *
 * ==Leniency==
 *
 * The classes in this type hierarchy have been designed to be very '''lenient when instantiating''' them, even for schema-invalid content.
 * The few builder methods that may throw exceptions have been clearly documented to potentially do so. For schema-invalid taxonomy
 * content, the resulting object may be something like `OtherXsdElem`, `OtherLinkElem` or `OtherNonXLinkElem`. For example,
 * an element named xs:element with both a name and ref attribute cannot be both an element declaration and element
 * reference, and will be instantiated as an `OtherXsdElem`. A non-standard XLink arc, whether a known generic arc or some
 * unknown and potentially erroneous arc, becomes a `NonStandardArc`, etc.
 *
 * Some '''instance methods''' may fail, however, if taxonomy content is invalid, and if it is '''schema-invalid''' in particular.
 * All instance methods must not fail on schema-valid content, unless mentioned otherwise.
 *
 * Typical instance methods that may fail on schema-invalid content are:
 * <ul>
 * <li>methods that query for one mandatory attribute (as far as the schema is concerned)</li>
 * <li>methods that query for one mandatory child element (as far as the schema is concerned)</li>
 * <li>methods that query for values of specific types (enumerations, integers etc.)</li>
 * </ul>
 * It is important to keep this in mind. Schema-invalid taxonomies will be instantiated successfully, but after instantiation the API user
 * should fall back to (defensive) yaidom level query methods when needed. This is indeed the responsibility of the API user.
 *
 * ==Other remarks==
 *
 * The type hierarchy for taxonomy elements is not a strict hierarchy. There are mixin traits for XLink content, "root elements",
 * elements in the xs and link namespaces, etc. Some element types mix in more than one of these traits.
 *
 * See http://www.datypic.com/sc/xsd/s-xmlschema.xsd.html for schema content in general (as opposed to taxonomy
 * schema content in particular).
 *
 * It is perfectly fine to embed linkbase content in schema content, and such an element tree will be instantiated correctly.
 *
 * The underlying backing elements can be any backing element implementation, including `BackingElemApi`
 * wrappers around Saxon tiny trees! Hence, this taxonomy DOM API is flexible in that it is not bound to one specific
 * backing element implementation.
 *
 * @author Chris de Vreeze
 */
sealed abstract class TaxonomyElem private[dom] (
  val backingElem: BackingElemApi,
  val childElems:  immutable.IndexedSeq[TaxonomyElem]) extends CanBeTaxonomyDocumentChild
  with AnyTaxonomyElem with Nodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  type ThisElem = TaxonomyElem

  // TODO Restore old equality on the backing elements themselves (after JS DOM wrappers have appropriate equality)
  assert(
    childElems.map(_.backingElem).map(_.resolvedName) == backingElem.findAllChildElems.map(_.resolvedName),
    msg("Corrupt element!"))

  // Implementations of abstract query API methods, and overridden equals and hashCode methods

  final def thisElem: ThisElem = this

  /**
   * Returns all child elements, and returns them extremely fast. This is important for fast querying, at the
   * expense of more expensive recursive creation.
   */
  final def findAllChildElems: immutable.IndexedSeq[TaxonomyElem] = childElems

  final def resolvedName: EName = backingElem.resolvedName

  final def resolvedAttributes: immutable.IndexedSeq[(EName, String)] = backingElem.resolvedAttributes.toIndexedSeq

  final def text: String = backingElem.text

  final def qname: QName = backingElem.qname

  final def attributes: immutable.IndexedSeq[(QName, String)] = backingElem.attributes.toIndexedSeq

  final def scope: Scope = backingElem.scope

  final override def equals(obj: Any): Boolean = obj match {
    case other: TaxonomyElem =>
      (other.backingElem == this.backingElem)
    case _ => false
  }

  final override def hashCode: Int = backingElem.hashCode

  // Other public methods

  final def key: XmlFragmentKey = backingElem.key

  final def docUri: URI = backingElem.docUri

  final def baseUriOption: Option[URI] = backingElem.baseUriOption

  final def baseUri: URI = backingElem.baseUri

  final def idOption: Option[String] = attributeOption(ENames.IdEName)

  // Internal functions

  protected final def msg(s: String): String = s"${s} (${key})"
}

// Root elements, like linkbase or schema root elements.

/**
 * Taxonomy root element, like an xs:schema element or a link:linkbase element.
 */
sealed trait TaxonomyRootElem extends TaxonomyElem

// XLink elements in taxonomies.

/**
 * An XLink element in a taxonomy, obeying the constraints on XLink imposed by XBRL. For example, an XLink arc or extended link.
 *
 * XLink (see https://www.w3.org/TR/xlink11/) is a somewhat low level standard on top of XML, but it is
 * very important in an XBRL context. Many taxonomy elements are also XLink elements, especially inside linkbases.
 */
sealed trait XLinkElem extends TaxonomyElem with xlink.XLinkElem {

  def xlinkType: String

  final def xlinkAttributes: Map[EName, String] = {
    resolvedAttributes.toMap.filterKeys(_.namespaceUriOption.contains(XLinkNamespace))
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
   * This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption.flatMap(_.attributeOption(ENames.XLinkRoleEName))
   * }}}
   */
  final def elr: String = {
    underlyingParentElem.attribute(ENames.XLinkRoleEName)
  }

  /**
   * Returns the underlying parent element.
   * This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * backingElem.parentOption
   * }}}
   */
  final def underlyingParentElem: BackingElemApi = {
    backingElem.parent
  }
}

/**
 * XLink locator or resource.
 */
sealed trait LabeledXLink extends ChildXLink with xlink.LabeledXLink {

  /**
   * Returns the XLink label. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
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
sealed trait ExtendedLink extends XLinkLink with xlink.ExtendedLink {

  final def xlinkType: String = {
    "extended"
  }

  /**
   * Returns the extended link role. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
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
sealed trait XLinkArc extends ChildXLink with xlink.XLinkArc {

  final def xlinkType: String = {
    "arc"
  }

  /**
   * Returns the arcrole. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkArcroleEName)
   * }}}
   */
  final def arcrole: String = {
    attribute(ENames.XLinkArcroleEName)
  }

  /**
   * Returns the XLink "from". This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkFromEName)
   * }}}
   */
  final def from: String = {
    attribute(ENames.XLinkFromEName)
  }

  /**
   * Returns the XLink "to". This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, use the following code instead:
   * {{{
   * attributeOption(ENames.XLinkToEName)
   * }}}
   */
  final def to: String = {
    attribute(ENames.XLinkToEName)
  }

  /**
   * Returns the Base Set key. This may fail with an exception if the taxonomy is not schema-valid.
   *
   * If the taxonomy is not known to be schema-valid, it may be impossible to create a Base Set key.
   */
  final def baseSetKey: BaseSetKey = {
    val underlyingParent = underlyingParentElem
    BaseSetKey(resolvedName, arcrole, underlyingParent.resolvedName, underlyingParent.attribute(ENames.XLinkRoleEName))
  }

  /**
   * Returns the "use" attribute (defaulting to "optional"). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def use: Use = {
    Use.fromString(attributeOption(ENames.UseEName).getOrElse("optional"))
  }

  /**
   * Returns the "priority" integer attribute (defaulting to 0). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def priority: Int = {
    attributeOption(ENames.PriorityEName).getOrElse("0").toInt
  }

  /**
   * Returns the "order" decimal attribute (defaulting to 1). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def order: BigDecimal = {
    BigDecimal(attributeOption(ENames.OrderEName).getOrElse("1"))
  }
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
sealed trait XLinkResource extends LabeledXLink with xlink.XLinkResource {

  final def xlinkType: String = {
    "resource"
  }
}

/**
 * XLink locator. For example:
 *
 * {{{
 * <link:loc xlink:type="locator"
 *   xlink:label="entityAxis"
 *   xlink:href="Axes.xsd#entityAxis" />
 * }}}
 */
sealed trait XLinkLocator extends LabeledXLink with xlink.XLinkLocator {

  final def xlinkType: String = {
    "locator"
  }

  /**
   * Returns the XLink href as URI. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def rawHref: URI = {
    URI.create(attribute(ENames.XLinkHrefEName))
  }

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the taxonomy
   * is not schema-valid.
   */
  final def resolvedHref: URI = {
    baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
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
   * Returns the XLink href as URI. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def rawHref: URI = {
    URI.create(attribute(ENames.XLinkHrefEName))
  }

  /**
   * Returns the XLink href as URI, resolved using XML Base. This may fail with an exception if the taxonomy
   * is not schema-valid.
   */
  final def resolvedHref: URI = {
    baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
}

// Schema content or linkbase content.

/**
 * Element in the XML Schema namespace ("http://www.w3.org/2001/XMLSchema").
 */
sealed trait XsdElem extends TaxonomyElem {

  /**
   * Returns the optional target namespace of the surrounding schema root element (or self), ignoring the possibility that
   * this is an included chameleon schema.
   */
  final def schemaTargetNamespaceOption: Option[String] = {
    backingElem.findAncestorOrSelf(_.resolvedName == ENames.XsSchemaEName).flatMap(_.attributeOption(ENames.TargetNamespaceEName))
  }
}

/**
 * Element in the link namespace ("http://www.xbrl.org/2003/linkbase").
 */
sealed trait LinkElem extends TaxonomyElem

// The "capabilities" of schema content.

/**
 * Super-type of schema components that can be abstract.
 */
sealed trait CanBeAbstract extends XsdElem {

  /**
   * Returns the boolean "abstract" attribute (defaulting to false). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def isAbstract: Boolean = {
    attributeOption(ENames.AbstractEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(false)
  }

  final def isConcrete: Boolean = {
    !isAbstract
  }
}

/**
 * Super-type of schema components that have a name attribute.
 */
sealed trait NamedDeclOrDef extends XsdElem {

  /**
   * Returns the "name" attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def nameAttributeValue: String = {
    attribute(ENames.NameEName)
  }
}

/**
 * Super-type of schema components that are references.
 */
sealed trait Reference extends XsdElem {

  /**
   * Returns the "ref" attribute as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def ref: EName = {
    attributeAsResolvedQName(ENames.RefEName)
  }
}

// The class inheritance hierarchy, under TaxonomyElem. First the root elements.

/**
 * The xs:schema root element of a taxonomy schema.
 *
 * ==Usage==
 *
 * Content inside a schema (root element) can be queried using the yaidom query API, of course, but this class also offers
 * some own query methods (that are themselves implemented as yaidom queries). For example:
 *
 * {{{
 * val globalElemDecls = schema.findAllGlobalElementDeclarations
 *
 * val globalElemDeclTypeENames =
 *   globalElemDecls.flatMap(_.typeOption).toSet
 * }}}
 */
final class XsdSchema private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem with TaxonomyRootElem {

  /**
   * Returns the optional target namespace of this schema root element itself, ignoring the possibility that this is an included chameleon schema.
   */
  def targetNamespaceOption: Option[String] = {
    attributeOption(ENames.TargetNamespaceEName)
  }

  def findAllImports: immutable.IndexedSeq[Import] = {
    findAllChildElemsOfType(classTag[Import])
  }

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): immutable.IndexedSeq[GlobalElementDeclaration] = {
    filterChildElemsOfType(classTag[GlobalElementDeclaration])(p)
  }

  def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(_ => true)
  }

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): immutable.IndexedSeq[GlobalAttributeDeclaration] = {
    filterChildElemsOfType(classTag[GlobalAttributeDeclaration])(p)
  }

  def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration] = {
    filterGlobalAttributeDeclarations(_ => true)
  }

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): immutable.IndexedSeq[NamedTypeDefinition] = {
    filterChildElemsOfType(classTag[NamedTypeDefinition])(p)
  }

  def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition] = {
    filterNamedTypeDefinitions(_ => true)
  }

  def filterComplexTypeDefinitions(p: ComplexTypeDefinition => Boolean): immutable.IndexedSeq[ComplexTypeDefinition] = {
    filterChildElemsOfType(classTag[ComplexTypeDefinition])(p)
  }

  def findAllComplexTypeDefinitions: immutable.IndexedSeq[ComplexTypeDefinition] = {
    filterComplexTypeDefinitions(_ => true)
  }

  def filterSimpleTypeDefinitions(p: SimpleTypeDefinition => Boolean): immutable.IndexedSeq[SimpleTypeDefinition] = {
    filterChildElemsOfType(classTag[SimpleTypeDefinition])(p)
  }

  def findAllSimpleTypeDefinitions: immutable.IndexedSeq[SimpleTypeDefinition] = {
    filterSimpleTypeDefinitions(_ => true)
  }

  def filterModelGroupDefinitionOrReferences(p: ModelGroupDefinitionOrReference => Boolean): immutable.IndexedSeq[ModelGroupDefinitionOrReference] = {
    filterChildElemsOfType(classTag[ModelGroupDefinitionOrReference])(p)
  }

  def filterAttributeGroupDefinitionOrReferences(p: AttributeGroupDefinitionOrReference => Boolean): immutable.IndexedSeq[AttributeGroupDefinitionOrReference] = {
    filterChildElemsOfType(classTag[AttributeGroupDefinitionOrReference])(p)
  }
}

/**
 * The link:linkbase root element of a linkbase. It may be embedded within a taxonomy schema document.
 */
final class Linkbase private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with TaxonomyRootElem {

  /**
   * Returns all extended link child elements. Only "taxonomy DOM" extended links are returned.
   */
  def findAllExtendedLinks: immutable.IndexedSeq[ExtendedLink] = {
    findAllChildElemsOfType(classTag[ExtendedLink])
  }

  def findAllRoleRefs: immutable.IndexedSeq[RoleRef] = {
    findAllChildElemsOfType(classTag[RoleRef])
  }

  def findAllArcroleRefs: immutable.IndexedSeq[ArcroleRef] = {
    findAllChildElemsOfType(classTag[ArcroleRef])
  }
}

// The remaining classes. First for schema content, then for linkbase content.

/**
 * Particle (in the context of XML Schema), having optional minOccurs and maxOccurs attributes.
 */
sealed trait Particle extends XsdElem {

  /**
   * The minOccurs attribute as integer, defaulting to 1.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def minOccurs: Int = {
    attributeOption(ENames.MinOccursEName).getOrElse("1").toInt
  }

  /**
   * The maxOccurs attribute as optional integer, defaulting to 1, but returning
   * None if unbounded. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def maxOccursOption: Option[Int] = {
    attributeOption(ENames.MaxOccursEName) match {
      case Some("unbounded") => None
      case Some(i)           => Some(i.toInt)
      case None              => Some(1)
    }
  }
}

// Element declarations or references.

/**
 * Either an element declaration or an element reference.
 */
sealed trait ElementDeclarationOrReference extends XsdElem

/**
 * Either a global element declaration or a local element declaration.
 */
sealed trait ElementDeclaration extends ElementDeclarationOrReference with NamedDeclOrDef {

  /**
   * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  def typeOption: Option[EName] = {
    attributeAsResolvedQNameOption(ENames.TypeEName)
  }
}

/**
 * Global element declaration. This element in isolation does not know if the element declaration is a concept declaration,
 * because it does not know from which substitution groups its own substitution group, if any, derives.
 *
 * Example, assuming an xs:schema parent (and document root) element:
 * {{{
 * <xs:element
 *   id="businessSegments"
 *   name="BusinessSegments"
 *   type="xbrli:monetaryItemType"
 *   substitutionGroup="xbrli:item"
 *   xbrli:periodType="duration" />
 * }}}
 *
 * In this case, we see immediately that the global element declaration is an item concept declaration, but as said above,
 * in general we cannot determine this without looking at the context of all other taxonomy documents in the same "taxonomy".
 *
 * Once we have a `SubstitutionGroupMap` as context, we can turn the global element declaration into a `ConceptDeclaration`,
 * if the global element declaration is indeed an item or tuple declaration according to the `SubstitutionGroupMap`.
 */
final class GlobalElementDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with CanBeAbstract {

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }

  /**
   * Returns the optional substitution group (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  def substitutionGroupOption: Option[EName] = {
    attributeAsResolvedQNameOption(ENames.SubstitutionGroupEName)
  }

  /**
   * Returns true if this global element declaration has the given substitution group, either
   * directly or indirectly. The given mappings are used as the necessary context, but are not needed if the element
   * declaration directly has the substitution group itself.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def hasSubstitutionGroup(substGroup: EName, substitutionGroupMap: SubstitutionGroupMap): Boolean = {
    (substitutionGroupOption.contains(substGroup)) || {
      val derivedSubstGroups = substitutionGroupMap.substitutionGroupDerivations.getOrElse(substGroup, Set.empty)

      // Recursive calls

      derivedSubstGroups.exists(substGrp => hasSubstitutionGroup(substGrp, substitutionGroupMap))
    }
  }

  /**
   * Returns the optional xbrli:periodType attribute, as `PeriodType`.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def periodTypeOption: Option[PeriodType] = {
    attributeOption(ENames.XbrliPeriodTypeEName).map(v => PeriodType.fromString(v))
  }
}

/**
 * Local element declaration. Like a global element declaration, it is an xs:element XML element with a name attribute.
 * Unlike a global element declaration, it is not a child element of the xs:schema root element, but it is nested inside
 * a type definition, for example. Unlike a global element declaration, it cannot have any substitution group, and therefore
 * cannot be a concept declaration.
 *
 * In an XBRL taxonomy, local element declarations are rare, if they occur at all. After all, most element declarations
 * are global element declarations declaring item or tuple concepts, and tuple concept content models refer to other
 * (item or tuple) concept declarations.
 */
final class LocalElementDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with Particle

/**
 * Element reference, referring to a global element declaration. Like local element declarations it is not a child element of
 * the xs:schema root element, but unlike global and local element declarations it has a ref attribute instead of a name attribute.
 */
final class ElementReference private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclarationOrReference with Reference

// Attribute declarations or references.

/**
 * Either an attribute declaration or an attribute reference.
 */
sealed trait AttributeDeclarationOrReference extends XsdElem

/**
 * Either a global attribute declaration or a local attribute declaration.
 */
sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with NamedDeclOrDef {

  /**
   * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  def typeOption: Option[EName] = {
    attributeAsResolvedQNameOption(ENames.TypeEName)
  }
}

/**
 * Global attribute declaration. It is an xs:attribute element, and a child element of the xs:schema root element.
 */
final class GlobalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration {

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }
}

/**
 * Local attribute declaration. It is an xs:attribute element, but not a direct child element of the xs:schema root element.
 */
final class LocalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

/**
 * Attribute reference. It is an xs:attribute element referring to a global attribute declaration. It is not a direct child element of
 * the xs:schema root element.
 */
final class AttributeReference private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclarationOrReference with Reference

// Type definitions.

/**
 * Type definition. It is either a complex or simple type definition, and it is also either a named or anonymous type definition.
 */
sealed trait TypeDefinition extends XsdElem {

  /**
   * Returns the base type of this type, as EName, if any, wrapped in an Option.
   * If defined, this type is then a restriction or extension of that base type.
   *
   * For type xs:anyType, None is returned. For union and list types, None is returned as well.
   *
   * For simple types, derivation (from the base type) is always by restriction.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def baseTypeOption: Option[EName]
}

/**
 * Named type definition, so either a named complex type definition or a named simple type definition.
 */
sealed trait NamedTypeDefinition extends TypeDefinition with NamedDeclOrDef {

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }
}

/**
 * Anonymous type definition, so either an anonymous complex type definition or an anonymous simple type definition.
 */
sealed trait AnonymousTypeDefinition extends TypeDefinition

/**
 * Simple type definition, so either a named simple type definition or an anonymous simple type definition.
 */
sealed trait SimpleTypeDefinition extends TypeDefinition {

  /**
   * Returns the variety. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def variety: Variety = {
    if (findChildElem(_.resolvedName == ENames.XsListEName).isDefined) {
      Variety.List
    } else if (findChildElem(_.resolvedName == ENames.XsUnionEName).isDefined) {
      Variety.Union
    } else if (findChildElem(_.resolvedName == ENames.XsRestrictionEName).isDefined) {
      Variety.Atomic
    } else {
      sys.error(msg(s"Could not determine variety"))
    }
  }

  /**
   * Returns the optional base type. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def baseTypeOption: Option[EName] = variety match {
    case Variety.Atomic =>
      findChildElemOfType(classTag[Restriction])(anyElem).headOption.flatMap(_.baseTypeOption)
    case _ => None
  }
}

/**
 * Complex type definition, so either a named complex type definition or an anonymous complex type definition.
 */
sealed trait ComplexTypeDefinition extends TypeDefinition {

  final def contentElemOption: Option[Content] = {
    val complexContentOption = findChildElemOfType(classTag[ComplexContent])(anyElem)
    val simpleContentOption = findChildElemOfType(classTag[SimpleContent])(anyElem)

    complexContentOption.orElse(simpleContentOption)
  }

  /**
   * Returns the optional base type. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def baseTypeOption: Option[EName] = {
    contentElemOption.flatMap(_.baseTypeOption).orElse(Some(ENames.XsAnyTypeEName))
  }
}

/**
 * Named simple type definition. It is a top-level xs:simpleType element with a name attribute.
 */
final class NamedSimpleTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with SimpleTypeDefinition

/**
 * Anonymous simple type definition. It is a non-top-level xs:simpleType element without any name attribute.
 */
final class AnonymousSimpleTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with SimpleTypeDefinition

/**
 * Named complex type definition. It is a top-level xs:complexType element with a name attribute.
 */
final class NamedComplexTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with ComplexTypeDefinition

/**
 * Anonymous complex type definition. It is a non-top-level xs:complexType element without any name attribute.
 */
final class AnonymousComplexTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with ComplexTypeDefinition

// Attribute group definitions and references.

/**
 * Attribute group definition or attribute group reference.
 */
sealed trait AttributeGroupDefinitionOrReference extends XsdElem

/**
 * Attribute group definition, so a top-level xs:attributeGroup element with a name attribute.
 */
final class AttributeGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference with NamedDeclOrDef

/**
 * Attribute group reference, so a non-top-level xs:attributeGroup element with a ref attribute, referring to an attribute group definition.
 */
final class AttributeGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference with Reference

// Model group definitions and references.

/**
 * Model group definition or model group reference.
 */
sealed trait ModelGroupDefinitionOrReference extends XsdElem

/**
 * Model group definition, so a top-level xs:group element with a name attribute.
 */
final class ModelGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

/**
 * Model group reference, so a non-top-level xs:group element with a ref attribute, referring to a model group definition.
 */
final class ModelGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference with Reference

// Ignoring identity constraints, notations, wildcards.

/**
 * Model group, so either a sequence, choice or all model group.
 */
sealed trait ModelGroup extends XsdElem

/**
 * Sequence model group, so an xs:sequence element.
 */
final class SequenceModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

/**
 * Choice model group, so an xs:choice element.
 */
final class ChoiceModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

/**
 * All model group, so an xs:all element.
 */
final class AllModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

/**
 * Either a restriction or an extension.
 */
sealed trait RestrictionOrExtension extends XsdElem {

  /**
   * Returns the optional base type. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def baseTypeOption: Option[EName] = {
    attributeAsResolvedQNameOption(ENames.BaseEName)
  }
}

/**
 * An xs:restriction element.
 */
final class Restriction private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with RestrictionOrExtension

/**
 * An xs:extension element.
 */
final class Extension private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with RestrictionOrExtension

/**
 * Either simple content or complex content.
 */
sealed trait Content extends XsdElem {

  /**
   * Returns the derivation. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def derivation: RestrictionOrExtension = {
    findChildElemOfType(classTag[Restriction])(anyElem).
      orElse(findChildElemOfType(classTag[Extension])(anyElem)).
      getOrElse(sys.error(msg(s"Expected xs:restriction or xs:extension child element")))
  }

  /**
   * Convenience method to get the base type of the child restriction or extension element.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def baseTypeOption: Option[EName] = derivation.baseTypeOption
}

/**
 * An xs:simpleContent element.
 */
final class SimpleContent private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with Content

/**
 * An xs:complexContent element.
 */
final class ComplexContent private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with Content

/**
 * An xs:annotation element.
 */
final class Annotation private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

/**
 * An xs:appinfo element.
 */
final class Appinfo private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

/**
 * An xs:import element.
 */
final class Import private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

/**
 * An xs:include element.
 */
final class Include private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// No redefines.

/**
 * Any `XsdElem` not recognized as an instance of one of the other concrete `XsdElem` sub-types. This means that either
 * this is valid schema content not modeled in the `XsdElem` sub-type hierarchy, or it is syntactically incorrect.
 * As an example of the latter, an xs:element XML element with both a name and a ref attribute is clearly invalid.
 */
final class OtherXsdElem private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// Linkbase content.

/**
 * An XBRL standard extended link. This is an XLink extended link, and it is either a definition link, presentation link,
 * calculation link, label link or reference link.
 */
sealed abstract class StandardExtendedLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with ExtendedLink

/**
 * An XBRL definition link. It is a link:definitionLink element.
 */
final class DefinitionLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardExtendedLink(backingElem, childElems)

/**
 * An XBRL presentation link. It is a link:presentationLink element.
 */
final class PresentationLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardExtendedLink(backingElem, childElems)

/**
 * An XBRL calculation link. It is a link:calculationLink element.
 */
final class CalculationLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardExtendedLink(backingElem, childElems)

/**
 * An XBRL label link. It is a link:labelLink element.
 */
final class LabelLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardExtendedLink(backingElem, childElems)

/**
 * An XBRL reference link. It is a link:referenceLink element.
 */
final class ReferenceLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardExtendedLink(backingElem, childElems)

/**
 * An XBRL standard arc. This is an XLink arc, and it is either a definition arc, presentation arc,
 * calculation arc, label arc or reference arc.
 */
sealed abstract class StandardArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkArc

/**
 * An XBRL definition arc. It is a link:definitionArc element.
 */
final class DefinitionArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

/**
 * An XBRL presentation arc. It is a link:presentationArc element.
 */
final class PresentationArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

/**
 * An XBRL calculation arc. It is a link:calculationArc element.
 */
final class CalculationArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

/**
 * An XBRL label arc. It is a link:labelArc element.
 */
final class LabelArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

/**
 * An XBRL reference arc. It is a link:referenceArc element.
 */
final class ReferenceArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

/**
 * An XBRL standard locator. This is an XLink locator, and it is a link:loc element.
 */
final class StandardLoc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkLocator

/**
 * Either a concept-label resource or a concept-reference resource.
 */
sealed abstract class StandardResource private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkResource

/**
 * Concept-label resource. It is a link:label element.
 */
final class ConceptLabelResource private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardResource(backingElem, childElems) {

  def isStandardLabel: Boolean = {
    roleOption.forall(_ == StandardLabelRoles.StandardLabel)
  }
}

/**
 * Concept-reference resource. It is a link:reference element.
 */
final class ConceptReferenceResource private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends StandardResource(backingElem, childElems) {

  def isStandardReference: Boolean = {
    roleOption.forall(_ == StandardReferenceRoles.StandardReference)
  }
}

// Generic linkbase content.

/**
 * Non-standard extended link, so an XLink extended link that is not a standard link. Typically it is a generic link.
 *
 * Note that in general it is very hard to determine if a non-standard link is a generic link by looking
 * at the link element itself, because we need substitution group (inheritance) context.
 */
final class NonStandardExtendedLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ExtendedLink

/**
 * Non-standard simple link, so an XLink simple link that is not a standard simple link. Rarely, if ever, encountered in practice.
 */
final class NonStandardSimpleLink private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with SimpleLink

/**
 * Non-standard arc, so an XLink arc that is not a standard arc. Typically it is a generic arc.
 * Some well-known formula/table-related arcs also fall into this category. Finally, unknown (and
 * possibly incorrect) arcs also fall into this category.
 */
final class NonStandardArc private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XLinkArc

/**
 * Non-standard resource, so an XLink resource that is not a standard resource. Typically it is a generic label or generic reference.
 * Formula/table-related XLink resources also fall into this category.
 */
final class NonStandardResource private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XLinkResource

/**
 * Non-standard locator, so an XLink locator that is not a standard locator. Rarely, if ever, encountered in practice.
 */
final class NonStandardLocator private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XLinkLocator

// Known simple links etc.

/**
 * A link:linkbaseRef element.
 */
final class LinkbaseRef private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

/**
 * A link:schemaRef element.
 */
final class SchemaRef private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

/**
 * A link:roleRef element.
 */
final class RoleRef private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink {

  def roleUri: String = {
    attribute(ENames.RoleURIEName)
  }
}

/**
 * A link:arcroleRef element.
 */
final class ArcroleRef private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink {

  def arcroleUri: String = {
    attribute(ENames.ArcroleURIEName)
  }
}

// Role types, arcrole types etc.

/**
 * A link:roleType element.
 */
final class RoleType private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem {

  /**
   * Returns the roleURI attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def roleUri: String = {
    attribute(ENames.RoleURIEName)
  }

  def definitionOption: Option[Definition] = {
    findChildElemOfType(classTag[Definition])(anyElem)
  }

  def usedOn: immutable.IndexedSeq[UsedOn] = {
    findAllChildElemsOfType(classTag[UsedOn])
  }
}

/**
 * A link:arcroleType element.
 */
final class ArcroleType private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem {

  /**
   * Returns the arcroleURI attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def arcroleUri: String = {
    attribute(ENames.ArcroleURIEName)
  }

  /**
   * Returns the cyclesAllowed attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def cyclesAllowed: CyclesAllowed = {
    CyclesAllowed.fromString(attribute(ENames.CyclesAllowedEName))
  }

  def definitionOption: Option[Definition] = {
    findChildElemOfType(classTag[Definition])(anyElem)
  }

  def usedOn: immutable.IndexedSeq[UsedOn] = {
    findAllChildElemsOfType(classTag[UsedOn])
  }
}

/**
 * A link:definition element.
 */
final class Definition private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem

/**
 * A link:usedOn element.
 */
final class UsedOn private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem {

  /**
   * Returns the usedOn value as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def usedOnValue: EName = {
    textAsResolvedQName
  }
}

// Remaining elements.

/**
 * Any `LinkElem` not recognized as an instance of one of the other concrete `LinkElem` sub-types. This means that either
 * this is valid linkbase content not modeled in the `LinkElem` sub-type hierarchy, or it is syntactically incorrect.
 */
final class OtherLinkElem private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem

/**
 * Any element that is neither an `XsdElem` nor a `LinkElem`, and that is also not recognized as a non-standard link,
 * non-standard arc or non-standard resource. It may still be valid taxonomy content, but even in taxonomies with XBRL formulas
 * or XBRL tables most non-standard linkbase content is still XLink arc or resource content, and therefore does not fall in
 * this `OtherNonXLinkElem` category.
 *
 * The elements that do fall into this `OtherNonXLinkElem` category are typically either reference parts (used in reference linkbases)
 * or formula/table-related non-XLink content.
 */
final class OtherNonXLinkElem private[dom] (
  backingElem: BackingElemApi,
  childElems:  immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems)

// Companion objects.

object TaxonomyElem {

  /**
   * Recursively builds a `TaxonomyElem` from the passed backing element. This is a relatively expensive method,
   * but the resulting `TaxonomyElem` supports fast querying.
   *
   * This method is designed to successfully return a `TaxonomyElem` even if the content is invalid. For example,
   * an xs:element XML element with both a name and a ref attribute is invalid, and is neither an element declaration
   * nor an element reference, but it will be returned as an `OtherXsdElem` element.
   *
   * This property makes the TQA DOM type hierarchy useful in situations where TQA is used for validating (possibly
   * invalid) taxonomies, but it does imply that `OtherXsdElem`, `OtherLinkElem` and `OtherNonXLinkElem` elements must be
   * recognized and possibly rejected during validation. The same may be true for some `NonStandardArc` objects, etc.
   */
  def build(backingElem: BackingElemApi): TaxonomyElem = {
    // Recursive calls
    val childElems = backingElem.findAllChildElems.map(che => build(che))
    apply(backingElem, childElems)
  }

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): TaxonomyElem = {
    backingElem.resolvedName.namespaceUriOption match {
      case Some(XsNamespace)   => XsdElem(backingElem, childElems)
      case Some(LinkNamespace) => LinkElem(backingElem, childElems)
      case _ =>
        // Elements not in the "xs" and "link" namespaces, but that may still be XLink elements.
        // Note that a NonStandardArc may be a known generic arc, or an unknown or even incorrect arc.

        // Also note that if by the element name we know that the element must be an arc, but if the
        // XLink type attribute is (erroneously) absent, the element will not be recognized as a NonStandardArc.
        // Analogous remarks apply to extended links and XLink resources.

        backingElem.attributeOption(ENames.XLinkTypeEName) match {
          case Some("extended") => new NonStandardExtendedLink(backingElem, childElems)
          case Some("simple")   => new NonStandardSimpleLink(backingElem, childElems)
          case Some("arc")      => new NonStandardArc(backingElem, childElems)
          case Some("resource") => new NonStandardResource(backingElem, childElems)
          case Some("locator")  => new NonStandardLocator(backingElem, childElems)
          case _                => new OtherNonXLinkElem(backingElem, childElems)
        }
    }
  }
}

object TaxonomyRootElem {

  /**
   * Returns `TaxonomyElem.build(backingElem)` cast to an optional `TaxonomyRootElem`, but returning None
   * if the cast is unsuccessful.
   */
  def buildOptionally(backingElem: BackingElemApi): Option[TaxonomyRootElem] = {
    Some(TaxonomyElem.build(backingElem)) collect { case taxoRoot: TaxonomyRootElem => taxoRoot }
  }

  /**
   * Returns `TaxonomyElem.build(backingElem)` cast to a `TaxonomyRootElem`, and throws an exception
   * if the cast is unsuccessful.
   */
  def build(backingElem: BackingElemApi): TaxonomyRootElem = {
    TaxonomyElem.build(backingElem).asInstanceOf[TaxonomyRootElem]
  }
}

object XsdSchema {

  /**
   * Returns `TaxonomyElem.build(backingElem)` cast to an `XsdSchema`, and throws an exception
   * if the cast is unsuccessful.
   */
  def build(backingElem: BackingElemApi): XsdSchema = {
    TaxonomyElem.build(backingElem).asInstanceOf[XsdSchema]
  }
}

object Linkbase {

  /**
   * Returns `TaxonomyElem.build(backingElem)` cast to a `Linkbase`, and throws an exception
   * if the cast is unsuccessful.
   */
  def build(backingElem: BackingElemApi): Linkbase = {
    TaxonomyElem.build(backingElem).asInstanceOf[Linkbase]
  }
}

object XsdElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): XsdElem = {
    require(backingElem.resolvedName.namespaceUriOption.contains(XsNamespace))

    backingElem.resolvedName match {
      case ENames.XsSchemaEName         => new XsdSchema(backingElem, childElems)
      case ENames.XsElementEName        => ElementDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsAttributeEName      => AttributeDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsSimpleTypeEName     => SimpleTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsComplexTypeEName    => ComplexTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsGroupEName          => ModelGroupDefinitionOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsAttributeGroupEName => AttributeGroupDefinitionOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case ENames.XsSequenceEName       => new SequenceModelGroup(backingElem, childElems)
      case ENames.XsChoiceEName         => new ChoiceModelGroup(backingElem, childElems)
      case ENames.XsAllEName            => new AllModelGroup(backingElem, childElems)
      case ENames.XsRestrictionEName    => new Restriction(backingElem, childElems)
      case ENames.XsExtensionEName      => new Extension(backingElem, childElems)
      case ENames.XsSimpleContentEName  => new SimpleContent(backingElem, childElems)
      case ENames.XsComplexContentEName => new ComplexContent(backingElem, childElems)
      case ENames.XsAnnotationEName     => new Annotation(backingElem, childElems)
      case ENames.XsAppinfoEName        => new Appinfo(backingElem, childElems)
      case ENames.XsImportEName         => new Import(backingElem, childElems)
      case ENames.XsIncludeEName        => new Include(backingElem, childElems)
      case _                            => new OtherXsdElem(backingElem, childElems)
    }
  }
}

object LinkElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): LinkElem = {
    require(backingElem.resolvedName.namespaceUriOption.contains(LinkNamespace))

    backingElem.resolvedName match {
      case ENames.LinkLinkbaseEName         => new Linkbase(backingElem, childElems)
      case ENames.LinkLocEName              => new StandardLoc(backingElem, childElems)
      case ENames.LinkLabelEName            => new ConceptLabelResource(backingElem, childElems)
      case ENames.LinkReferenceEName        => new ConceptReferenceResource(backingElem, childElems)
      case ENames.LinkDefinitionLinkEName   => new DefinitionLink(backingElem, childElems)
      case ENames.LinkPresentationLinkEName => new PresentationLink(backingElem, childElems)
      case ENames.LinkCalculationLinkEName  => new CalculationLink(backingElem, childElems)
      case ENames.LinkLabelLinkEName        => new LabelLink(backingElem, childElems)
      case ENames.LinkReferenceLinkEName    => new ReferenceLink(backingElem, childElems)
      case ENames.LinkDefinitionArcEName    => new DefinitionArc(backingElem, childElems)
      case ENames.LinkPresentationArcEName  => new PresentationArc(backingElem, childElems)
      case ENames.LinkCalculationArcEName   => new CalculationArc(backingElem, childElems)
      case ENames.LinkLabelArcEName         => new LabelArc(backingElem, childElems)
      case ENames.LinkReferenceArcEName     => new ReferenceArc(backingElem, childElems)
      case ENames.LinkLinkbaseRefEName      => new LinkbaseRef(backingElem, childElems)
      case ENames.LinkSchemaRefEName        => new SchemaRef(backingElem, childElems)
      case ENames.LinkRoleRefEName          => new RoleRef(backingElem, childElems)
      case ENames.LinkArcroleRefEName       => new ArcroleRef(backingElem, childElems)
      case ENames.LinkRoleTypeEName         => new RoleType(backingElem, childElems)
      case ENames.LinkArcroleTypeEName      => new ArcroleType(backingElem, childElems)
      case ENames.LinkDefinitionEName       => new Definition(backingElem, childElems)
      case ENames.LinkUsedOnEName           => new UsedOn(backingElem, childElems)
      case _                                => new OtherLinkElem(backingElem, childElems)
    }
  }
}

object ElementDeclarationOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[ElementDeclarationOrReference] = {
    require(backingElem.resolvedName == ENames.XsElementEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined
    val hasRef = backingElem.attributeOption(ENames.RefEName).isDefined

    if (parentIsSchema && hasName && !hasRef) {
      Some(new GlobalElementDeclaration(backingElem, childElems))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(new LocalElementDeclaration(backingElem, childElems))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new ElementReference(backingElem, childElems))
    } else {
      None
    }
  }
}

object AttributeDeclarationOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[AttributeDeclarationOrReference] = {
    require(backingElem.resolvedName == ENames.XsAttributeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined
    val hasRef = backingElem.attributeOption(ENames.RefEName).isDefined

    if (parentIsSchema && hasName && !hasRef) {
      Some(new GlobalAttributeDeclaration(backingElem, childElems))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(new LocalAttributeDeclaration(backingElem, childElems))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new AttributeReference(backingElem, childElems))
    } else {
      None
    }
  }
}

object SimpleTypeDefinition {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[SimpleTypeDefinition] = {
    require(backingElem.resolvedName == ENames.XsSimpleTypeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined

    if (parentIsSchema && hasName) {
      Some(new NamedSimpleTypeDefinition(backingElem, childElems))
    } else if (!parentIsSchema && !hasName) {
      Some(new AnonymousSimpleTypeDefinition(backingElem, childElems))
    } else {
      None
    }
  }
}

object ComplexTypeDefinition {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[ComplexTypeDefinition] = {
    require(backingElem.resolvedName == ENames.XsComplexTypeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined

    if (parentIsSchema && hasName) {
      Some(new NamedComplexTypeDefinition(backingElem, childElems))
    } else if (!parentIsSchema && !hasName) {
      Some(new AnonymousComplexTypeDefinition(backingElem, childElems))
    } else {
      None
    }
  }
}

object ModelGroupDefinitionOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[ModelGroupDefinitionOrReference] = {
    require(backingElem.resolvedName == ENames.XsGroupEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined
    val hasRef = backingElem.attributeOption(ENames.RefEName).isDefined

    if (parentIsSchema && hasName && !hasRef) {
      Some(new ModelGroupDefinition(backingElem, childElems))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new ModelGroupReference(backingElem, childElems))
    } else {
      None
    }
  }
}

object AttributeGroupDefinitionOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[AttributeGroupDefinitionOrReference] = {
    require(backingElem.resolvedName == ENames.XsAttributeGroupEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.contains(ENames.XsSchemaEName)
    val hasName = backingElem.attributeOption(ENames.NameEName).isDefined
    val hasRef = backingElem.attributeOption(ENames.RefEName).isDefined

    if (parentIsSchema && hasName && !hasRef) {
      Some(new AttributeGroupDefinition(backingElem, childElems))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new AttributeGroupReference(backingElem, childElems))
    } else {
      None
    }
  }
}
