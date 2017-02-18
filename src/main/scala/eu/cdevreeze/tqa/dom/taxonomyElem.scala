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
import scala.reflect.classTag

import eu.cdevreeze.tqa.ENames.AbstractEName
import eu.cdevreeze.tqa.ENames.BaseEName
import eu.cdevreeze.tqa.ENames.LinkArcroleRefEName
import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.LinkCalculationLinkEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionArcEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionLinkEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelEName
import eu.cdevreeze.tqa.ENames.LinkLabelLinkEName
import eu.cdevreeze.tqa.ENames.LinkLinkbaseEName
import eu.cdevreeze.tqa.ENames.LinkLinkbaseRefEName
import eu.cdevreeze.tqa.ENames.LinkLocEName
import eu.cdevreeze.tqa.ENames.LinkPresentationArcEName
import eu.cdevreeze.tqa.ENames.LinkPresentationLinkEName
import eu.cdevreeze.tqa.ENames.LinkReferenceArcEName
import eu.cdevreeze.tqa.ENames.LinkReferenceEName
import eu.cdevreeze.tqa.ENames.LinkReferenceLinkEName
import eu.cdevreeze.tqa.ENames.LinkRoleRefEName
import eu.cdevreeze.tqa.ENames.LinkSchemaRefEName
import eu.cdevreeze.tqa.ENames.NameEName
import eu.cdevreeze.tqa.ENames.PriorityEName
import eu.cdevreeze.tqa.ENames.RefEName
import eu.cdevreeze.tqa.ENames.SubstitutionGroupEName
import eu.cdevreeze.tqa.ENames.TargetNamespaceEName
import eu.cdevreeze.tqa.ENames.TypeEName
import eu.cdevreeze.tqa.ENames.UseEName
import eu.cdevreeze.tqa.ENames.XLinkArcroleEName
import eu.cdevreeze.tqa.ENames.XLinkFromEName
import eu.cdevreeze.tqa.ENames.XLinkHrefEName
import eu.cdevreeze.tqa.ENames.XLinkLabelEName
import eu.cdevreeze.tqa.ENames.XLinkRoleEName
import eu.cdevreeze.tqa.ENames.XLinkToEName
import eu.cdevreeze.tqa.ENames.XLinkTypeEName
import eu.cdevreeze.tqa.ENames.XsAllEName
import eu.cdevreeze.tqa.ENames.XsAnnotationEName
import eu.cdevreeze.tqa.ENames.XsAnyTypeEName
import eu.cdevreeze.tqa.ENames.XsAppinfoEName
import eu.cdevreeze.tqa.ENames.XsAttributeEName
import eu.cdevreeze.tqa.ENames.XsAttributeGroupEName
import eu.cdevreeze.tqa.ENames.XsChoiceEName
import eu.cdevreeze.tqa.ENames.XsComplexContentEName
import eu.cdevreeze.tqa.ENames.XsComplexTypeEName
import eu.cdevreeze.tqa.ENames.XsElementEName
import eu.cdevreeze.tqa.ENames.XsExtensionEName
import eu.cdevreeze.tqa.ENames.XsGroupEName
import eu.cdevreeze.tqa.ENames.XsImportEName
import eu.cdevreeze.tqa.ENames.XsIncludeEName
import eu.cdevreeze.tqa.ENames.XsListEName
import eu.cdevreeze.tqa.ENames.XsRestrictionEName
import eu.cdevreeze.tqa.ENames.XsSchemaEName
import eu.cdevreeze.tqa.ENames.XsSequenceEName
import eu.cdevreeze.tqa.ENames.XsSimpleContentEName
import eu.cdevreeze.tqa.ENames.XsSimpleTypeEName
import eu.cdevreeze.tqa.ENames.XsUnionEName
import eu.cdevreeze.tqa.Namespaces.LinkNamespace
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.tqa.Namespaces.XsNamespace
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem
import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import javax.xml.bind.DatatypeConverter

/**
 * Any taxonomy XML element. These classes are reasonably lenient when instantiating them (although schema validity helps), but query
 * methods may fail if the taxonomy XML is not schema-valid (against the schemas for this higher level taxonomy model).
 * Instantiation is designed to never fail, but the result may be something like an `OtherElem` instance. For example,
 * an element named xs:element with both a name and ref attribute cannot be both an element declaration and element
 * reference (it is not even allowed), and will be instantiated as an `OtherXsdElem`.
 *
 * The instance methods may fail, however, if taxonomy content is invalid, and if it is schema-invalid in particular.
 *
 * The type hierarchy for taxonomy elements is not a strict hierarchy. There are mixin traits for XLink content, "root elements",
 * elements in the xs and link namespaces, etc. Some element types mix in more than one of these traits.
 *
 * See http://www.datypic.com/sc/xsd/s-xmlschema.xsd.html for schema content in general (as opposed to taxonomy
 * schema content in particular).
 *
 * It is perfectly fine to embed linkbase content in schema content, and such an element tree will be instantiated correctly.
 *
 * The underlying backing elements can be any backing element implementation, including BackingElemApi
 * wrappers around Saxon tiny trees!
 *
 * @author Chris de Vreeze
 */
sealed abstract class TaxonomyElem private[dom] (
    val backingElem: BackingElemApi,
    val childElems: immutable.IndexedSeq[TaxonomyElem]) extends AnyTaxonomyElem with Nodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  type ThisElem = TaxonomyElem

  assert(childElems.map(_.backingElem) == backingElem.findAllChildElems, msg("Corrupt element!"))

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

  final override def key: XmlFragmentKey = backingElem.key

  // Other public methods

  final def docUri: URI = backingElem.docUri

  final def baseUriOption: Option[URI] = backingElem.baseUriOption

  final def baseUri: URI = backingElem.baseUri

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
 */
sealed trait XLinkElem extends TaxonomyElem {

  def xlinkType: String

  final def xlinkAttributes: Map[EName, String] = {
    resolvedAttributes.toMap.filterKeys(_.namespaceUriOption == Some(XLinkNamespace))
  }
}

sealed trait XLinkLink extends XLinkElem

sealed trait ChildXLink extends XLinkElem {

  final def elr: String = {
    underlyingParentElem.attribute(XLinkRoleEName)
  }

  final def underlyingParentElem: BackingElemApi = {
    backingElem.parent
  }
}

sealed trait LabeledXLink extends ChildXLink {

  final def xlinkLabel: String = {
    attribute(XLinkLabelEName)
  }
}

sealed trait ExtendedLink extends XLinkLink {

  final def xlinkType: String = {
    "extended"
  }

  final def roleOption: Option[String] = {
    attributeOption(XLinkRoleEName)
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

  final def labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]] = {
    labeledXlinkChildren.groupBy(_.xlinkLabel)
  }
}

sealed trait XLinkArc extends ChildXLink {

  final def xlinkType: String = {
    "arc"
  }

  final def arcrole: String = {
    attribute(XLinkArcroleEName)
  }

  final def from: String = {
    attribute(XLinkFromEName)
  }

  final def to: String = {
    attribute(XLinkToEName)
  }

  final def baseSetKey: BaseSetKey = {
    val underlyingParent = underlyingParentElem
    BaseSetKey(resolvedName, arcrole, underlyingParent.resolvedName, underlyingParent.attribute(XLinkRoleEName))
  }

  final def use: Use = {
    Use.fromString(backingElem.attributeOption(UseEName).getOrElse("optional"))
  }

  final def priority: Int = {
    backingElem.attributeOption(PriorityEName).getOrElse("0").toInt
  }
}

sealed trait XLinkResource extends LabeledXLink {

  final def xlinkType: String = {
    "resource"
  }

  final def roleOption: Option[String] = {
    attributeOption(XLinkRoleEName)
  }
}

sealed trait XLinkLocator extends LabeledXLink {

  final def xlinkType: String = {
    "locator"
  }

  final def rawHref: URI = {
    URI.create(attribute(XLinkHrefEName))
  }
}

sealed trait SimpleLink extends XLinkLink {

  final def xlinkType: String = {
    "simple"
  }

  final def rawHref: URI = {
    URI.create(attribute(XLinkHrefEName))
  }
}

// Schema content or linkbase content.

/**
 * Element in the xs namespace.
 */
sealed trait XsdElem extends TaxonomyElem {

  /**
   * Returns the optional target namespace of the surrounding schema root element (or self), ignoring the possibility that this is an included chameleon schema.
   */
  final def schemaTargetNamespaceOption: Option[String] = {
    backingElem.findAncestorOrSelf(_.resolvedName == XsSchemaEName).flatMap(_.attributeOption(TargetNamespaceEName))
  }
}

/**
 * Element in the link namespace.
 */
sealed trait LinkElem extends TaxonomyElem

// The "capabilities" of schema content.

sealed trait CanBeAbstract extends XsdElem {

  final def isAbstract: Boolean = {
    attributeOption(AbstractEName).map(v => DatatypeConverter.parseBoolean(v)).getOrElse(false)
  }

  final def isConcrete: Boolean = {
    !isAbstract
  }
}

sealed trait NamedDeclOrDef extends XsdElem {

  final def nameAttributeValue: String = {
    attribute(NameEName)
  }
}

sealed trait Reference extends XsdElem {

  final def ref: EName = {
    attributeAsResolvedQName(RefEName)
  }
}

// The class inheritance hierarchy, under TaxonomyElem. First the root elements.

/**
 * The xs:schema root element of a taxonomy schema.
 */
final class XsdSchema private[dom] (
    backingElem: BackingElemApi,
    childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem with TaxonomyRootElem {

  /**
   * Returns the optional target namespace of this schema root element itself, ignoring the possibility that this is an included chameleon schema.
   */
  def targetNamespaceOption: Option[String] = {
    attributeOption(TargetNamespaceEName)
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

  def filterModelGroupDefinitionOrReferences(p: ModelGroupDefinitionOrReference => Boolean): immutable.IndexedSeq[ModelGroupDefinitionOrReference] = {
    filterChildElemsOfType(classTag[ModelGroupDefinitionOrReference])(p)
  }

  def filterAttributeGroupDefinitionOrReferences(p: AttributeGroupDefinitionOrReference => Boolean): immutable.IndexedSeq[AttributeGroupDefinitionOrReference] = {
    filterChildElemsOfType(classTag[AttributeGroupDefinitionOrReference])(p)
  }
}

/**
 * The link:linkbase root element of a taxonomy schema. It may be embedded within a taxonomy schema document.
 */
final class Linkbase private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with TaxonomyRootElem

// The remaining classes. First for schema content, then for linkbase content.

sealed trait Particle extends XsdElem

// Element declarations or references.

sealed trait ElementDeclarationOrReference extends XsdElem

sealed trait ElementDeclaration extends ElementDeclarationOrReference with NamedDeclOrDef

/**
 * Global element declaration. This element in isolation does not know if the element declaration is a concept declaration.
 */
final class GlobalElementDeclaration private[dom] (
    backingElem: BackingElemApi,
    childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with CanBeAbstract {

  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }

  def substitutionGroupOption: Option[EName] = {
    attributeAsResolvedQNameOption(SubstitutionGroupEName)
  }

  def typeOption: Option[EName] = {
    attributeAsResolvedQNameOption(TypeEName)
  }

  /**
   * Returns true if this global element declaration has the given substitution group, either
   * directly or indirectly. The given mappings are used as the necessary context, but are not needed if the element
   * declaration directly has the substitution group itself.
   */
  def hasSubstitutionGroup(substGroup: EName, substitutionGroupMap: SubstitutionGroupMap): Boolean = {
    (substitutionGroupOption == Some(substGroup)) || {
      val derivedSubstGroups = substitutionGroupMap.substitutionGroupDerivations.getOrElse(substGroup, Set.empty)

      // Recursive calls

      derivedSubstGroups.exists(substGrp => hasSubstitutionGroup(substGrp, substitutionGroupMap))
    }
  }
}

final class LocalElementDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with Particle

final class ElementReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclarationOrReference with Reference

// Attribute declarations or references.

sealed trait AttributeDeclarationOrReference extends XsdElem

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with NamedDeclOrDef

final class GlobalAttributeDeclaration private[dom] (
    backingElem: BackingElemApi,
    childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration {

  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }
}

final class LocalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class AttributeReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclarationOrReference with Reference

// Type definitions.

sealed trait TypeDefinition extends XsdElem {

  /**
   * Returns the base type of this type, as EName, if any, wrapped in an Option.
   * If defined, this type is then a restriction or extension of that base type.
   *
   * For type xs:anyType, None is returned. For union and list types, None is returned as well.
   *
   * For simple types, derivation (from the base type) is always by restriction.
   */
  def baseTypeOption: Option[EName]
}

sealed trait NamedTypeDefinition extends TypeDefinition with NamedDeclOrDef {

  def targetEName: EName = {
    val tnsOption = schemaTargetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }
}

sealed trait AnonymousTypeDefinition extends TypeDefinition

sealed trait SimpleTypeDefinition extends TypeDefinition {

  final def variety: Variety = {
    if (findChildElem(_.resolvedName == XsListEName).isDefined) {
      Variety.List
    } else if (findChildElem(_.resolvedName == XsUnionEName).isDefined) {
      Variety.Union
    } else if (findChildElem(_.resolvedName == XsRestrictionEName).isDefined) {
      Variety.Atomic
    } else {
      sys.error(msg(s"Could not determine variety"))
    }
  }

  final def baseTypeOption: Option[EName] = variety match {
    case Variety.Atomic =>
      findChildElemOfType(classTag[Restriction])(anyElem).headOption.flatMap(_.baseTypeOption)
    case _ => None
  }
}

sealed trait ComplexTypeDefinition extends TypeDefinition {

  final def contentElemOption: Option[Content] = {
    val complexContentOption = findChildElemOfType(classTag[ComplexContent])(anyElem)
    val simpleContentOption = findChildElemOfType(classTag[SimpleContent])(anyElem)

    complexContentOption.orElse(simpleContentOption)
  }

  final def baseTypeOption: Option[EName] = {
    contentElemOption.flatMap(_.baseTypeOption).orElse(Some(XsAnyTypeEName))
  }
}

final class NamedSimpleTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with SimpleTypeDefinition

final class AnonymousSimpleTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with SimpleTypeDefinition

final class NamedComplexTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with NamedTypeDefinition with ComplexTypeDefinition

final class AnonymousComplexTypeDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AnonymousTypeDefinition with ComplexTypeDefinition

// Attribute group definitions and references.

sealed trait AttributeGroupDefinitionOrReference extends XsdElem

final class AttributeGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference with NamedDeclOrDef

final class AttributeGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference with Reference

// Model group definitions and references.

sealed trait ModelGroupDefinitionOrReference extends XsdElem

final class ModelGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

final class ModelGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference with Reference

// Ignoring identity constraints, notations, wildcards.

sealed trait ModelGroup extends XsdElem

final class SequenceModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

final class ChoiceModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

final class AllModelGroup private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroup

sealed trait RestrictionOrExtension extends XsdElem {

  def baseTypeOption: Option[EName] = {
    attributeAsResolvedQNameOption(BaseEName)
  }
}

final class Restriction private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with RestrictionOrExtension

final class Extension private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with RestrictionOrExtension

sealed trait Content extends XsdElem {

  final def derivation: RestrictionOrExtension = {
    findChildElemOfType(classTag[Restriction])(anyElem).
      orElse(findChildElemOfType(classTag[Extension])(anyElem)).
      getOrElse(sys.error(msg(s"Expected xs:restriction or xs:extension child element")))
  }

  /**
   * Convenience method to get the base type of the child restriction or extension element.
   */
  final def baseTypeOption: Option[EName] = derivation.baseTypeOption
}

final class SimpleContent private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with Content

final class ComplexContent private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with Content

final class Annotation private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Appinfo private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Import private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Include private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// No redefines.

final class OtherXsdElem private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

// Linkbase content.

sealed abstract class StandardLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with ExtendedLink

final class DefinitionLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardLink(backingElem, childElems)

final class PresentationLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardLink(backingElem, childElems)

final class CalculationLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardLink(backingElem, childElems)

final class LabelLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardLink(backingElem, childElems)

final class ReferenceLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardLink(backingElem, childElems)

sealed abstract class StandardArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkArc

final class DefinitionArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

final class PresentationArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

final class CalculationArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

final class LabelArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

final class ReferenceArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardArc(backingElem, childElems)

final class StandardLoc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkLocator

sealed abstract class StandardResource private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with XLinkResource

final class ConceptLabelResource private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardResource(backingElem, childElems)

final class ConceptReferenceResource private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends StandardResource(backingElem, childElems)

// Generic linkbase content.

final class NonStandardLink private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ExtendedLink

final class NonStandardArc private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XLinkArc

final class NonStandardResource private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XLinkResource

// Known simple links etc.

final class LinkbaseRef private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

final class SchemaRef private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

final class RoleRef private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

final class ArcroleRef private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with SimpleLink

// Remaining elements.

final class OtherLinkbaseElem private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem

final class OtherElem private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems)

object TaxonomyElem {

  /**
   * Recursively builds a TaxonomyElem from the passed backing element.
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
        backingElem.attributeOption(XLinkTypeEName) match {
          case Some("extended") => new NonStandardLink(backingElem, childElems)
          case Some("arc")      => new NonStandardArc(backingElem, childElems)
          case Some("resource") => new NonStandardResource(backingElem, childElems)
          case _                => new OtherElem(backingElem, childElems)
        }
    }
  }
}

object TaxonomyRootElem {

  def buildOptionally(backingElem: BackingElemApi): Option[TaxonomyRootElem] = {
    Some(TaxonomyElem.build(backingElem)) collect { case taxoRoot: TaxonomyRootElem => taxoRoot }
  }

  def build(backingElem: BackingElemApi): TaxonomyRootElem = {
    TaxonomyElem.build(backingElem).asInstanceOf[TaxonomyRootElem]
  }
}

object XsdSchema {

  def build(backingElem: BackingElemApi): XsdSchema = {
    TaxonomyElem.build(backingElem).asInstanceOf[XsdSchema]
  }
}

object Linkbase {

  def build(backingElem: BackingElemApi): Linkbase = {
    TaxonomyElem.build(backingElem).asInstanceOf[Linkbase]
  }
}

object XsdElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): XsdElem = {
    require(backingElem.resolvedName.namespaceUriOption == Some(XsNamespace))

    backingElem.resolvedName match {
      case XsSchemaEName         => new XsdSchema(backingElem, childElems)
      case XsElementEName        => ElementDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsAttributeEName      => AttributeDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsSimpleTypeEName     => SimpleTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsComplexTypeEName    => ComplexTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsGroupEName          => ModelGroupDefinitionOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsAttributeGroupEName => AttributeGroupDefinitionOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsSequenceEName       => new SequenceModelGroup(backingElem, childElems)
      case XsChoiceEName         => new ChoiceModelGroup(backingElem, childElems)
      case XsAllEName            => new AllModelGroup(backingElem, childElems)
      case XsRestrictionEName    => new Restriction(backingElem, childElems)
      case XsExtensionEName      => new Extension(backingElem, childElems)
      case XsSimpleContentEName  => new SimpleContent(backingElem, childElems)
      case XsComplexContentEName => new ComplexContent(backingElem, childElems)
      case XsAnnotationEName     => new Annotation(backingElem, childElems)
      case XsAppinfoEName        => new Appinfo(backingElem, childElems)
      case XsImportEName         => new Import(backingElem, childElems)
      case XsIncludeEName        => new Include(backingElem, childElems)
      case _                     => new OtherXsdElem(backingElem, childElems)
    }
  }
}

object LinkElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): LinkElem = {
    require(backingElem.resolvedName.namespaceUriOption == Some(LinkNamespace))

    backingElem.resolvedName match {
      case LinkLinkbaseEName         => new Linkbase(backingElem, childElems)
      case LinkLocEName              => new StandardLoc(backingElem, childElems)
      case LinkLabelEName            => new ConceptLabelResource(backingElem, childElems)
      case LinkReferenceEName        => new ConceptReferenceResource(backingElem, childElems)
      case LinkDefinitionLinkEName   => new DefinitionLink(backingElem, childElems)
      case LinkPresentationLinkEName => new PresentationLink(backingElem, childElems)
      case LinkCalculationLinkEName  => new CalculationLink(backingElem, childElems)
      case LinkLabelLinkEName        => new LabelLink(backingElem, childElems)
      case LinkReferenceLinkEName    => new ReferenceLink(backingElem, childElems)
      case LinkDefinitionArcEName    => new DefinitionArc(backingElem, childElems)
      case LinkPresentationArcEName  => new PresentationArc(backingElem, childElems)
      case LinkCalculationArcEName   => new CalculationArc(backingElem, childElems)
      case LinkLabelArcEName         => new LabelArc(backingElem, childElems)
      case LinkReferenceArcEName     => new ReferenceArc(backingElem, childElems)
      case LinkLinkbaseRefEName      => new LinkbaseRef(backingElem, childElems)
      case LinkSchemaRefEName        => new SchemaRef(backingElem, childElems)
      case LinkRoleRefEName          => new RoleRef(backingElem, childElems)
      case LinkArcroleRefEName       => new ArcroleRef(backingElem, childElems)
      case _                         => new OtherLinkbaseElem(backingElem, childElems)
    }
  }
}

object ElementDeclarationOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[ElementDeclarationOrReference] = {
    require(backingElem.resolvedName == XsElementEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined
    val hasRef = backingElem.attributeOption(RefEName).isDefined

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
    require(backingElem.resolvedName == XsAttributeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined
    val hasRef = backingElem.attributeOption(RefEName).isDefined

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
    require(backingElem.resolvedName == XsSimpleTypeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined

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
    require(backingElem.resolvedName == XsComplexTypeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined

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
    require(backingElem.resolvedName == XsGroupEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined
    val hasRef = backingElem.attributeOption(RefEName).isDefined

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
    require(backingElem.resolvedName == XsAttributeGroupEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined
    val hasRef = backingElem.attributeOption(RefEName).isDefined

    if (parentIsSchema && hasName && !hasRef) {
      Some(new AttributeGroupDefinition(backingElem, childElems))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new AttributeGroupReference(backingElem, childElems))
    } else {
      None
    }
  }
}
