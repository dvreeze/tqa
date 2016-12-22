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
import javax.xml.bind.DatatypeConverter

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import eu.cdevreeze.tqa.Namespaces._
import eu.cdevreeze.tqa.ENames._

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

  // Other public methods

  final def key: XmlFragmentKey = backingElem.key

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
 * An XLink element in a taxonomy. For example, an XLink arc or extended link.
 */
sealed trait XLinkElem extends TaxonomyElem {

  def xlinkType: String

  final def xlinkAttributes: Map[EName, String] = {
    resolvedAttributes.toMap.filterKeys(_.namespaceUriOption == Some(XLinkNamespace))
  }
}

sealed trait XLinkLinkElem extends XLinkElem

sealed trait XLinkInExtendedLink extends XLinkElem {

  final def elr: String = {
    backingElem.parent.attribute(XLinkRoleEName)
  }
}

sealed trait LabeledXLinkElem extends XLinkInExtendedLink {

  final def xlinkLabel: String = {
    attribute(XLinkLabelEName)
  }
}

sealed trait ExtendedLink extends XLinkLinkElem {

  final def xlinkType: String = {
    "extended"
  }

  final def roleOption: Option[String] = {
    attributeOption(XLinkRoleEName)
  }

  final def xlinkChildren: immutable.IndexedSeq[XLinkInExtendedLink] = {
    findAllChildElemsOfType(classTag[XLinkInExtendedLink])
  }
}

sealed trait XLinkArc extends XLinkInExtendedLink {

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
}

sealed trait XLinkResource extends LabeledXLinkElem {

  final def xlinkType: String = {
    "resource"
  }
}

sealed trait XLinkLocator extends LabeledXLinkElem {

  final def xlinkType: String = {
    "locator"
  }

  final def rawHref: URI = {
    URI.create(attribute(XLinkHrefEName))
  }
}

sealed trait SimpleLink extends XLinkLinkElem {

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
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class LocalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class AttributeReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclarationOrReference with Reference

// Type definitions.

sealed trait TypeDefinition extends XsdElem

sealed trait NamedTypeDefinition extends TypeDefinition with NamedDeclOrDef

sealed trait AnonymousTypeDefinition extends TypeDefinition

sealed trait SimpleTypeDefinition extends TypeDefinition

sealed trait ComplexTypeDefinition extends TypeDefinition

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

final class Restriction private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class Extension private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class SimpleContent private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

final class ComplexContent private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem

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

// TODO Known simple links etc. Generic links and arcs.

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
      case _                   => new OtherElem(backingElem, childElems)
    }
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

    if (parentIsSchema) {
      if (hasName && !hasRef) {
        Some(new GlobalElementDeclaration(backingElem, childElems))
      } else if (hasRef && !hasName) {
        Some(new ElementReference(backingElem, childElems))
      } else {
        None
      }
    } else {
      if (hasName && !hasRef) {
        Some(new LocalElementDeclaration(backingElem, childElems))
      } else {
        None
      }
    }
  }
}

object AttributeDeclarationOrReference {

  private[dom] def opt(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): Option[AttributeDeclarationOrReference] = {
    require(backingElem.resolvedName == XsAttributeEName)

    val parentIsSchema = backingElem.reverseAncestryENames.lastOption.exists(_ == XsSchemaEName)
    val hasName = backingElem.attributeOption(NameEName).isDefined
    val hasRef = backingElem.attributeOption(RefEName).isDefined

    if (parentIsSchema) {
      if (hasName && !hasRef) {
        Some(new GlobalAttributeDeclaration(backingElem, childElems))
      } else if (hasRef && !hasName) {
        Some(new AttributeReference(backingElem, childElems))
      } else {
        None
      }
    } else {
      if (hasName && !hasRef) {
        Some(new LocalAttributeDeclaration(backingElem, childElems))
      } else {
        None
      }
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

    if (parentIsSchema) {
      if (hasName && !hasRef) {
        Some(new ModelGroupDefinition(backingElem, childElems))
      } else if (hasRef && !hasName) {
        Some(new ModelGroupReference(backingElem, childElems))
      } else {
        None
      }
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

    if (parentIsSchema) {
      if (hasName && !hasRef) {
        Some(new AttributeGroupDefinition(backingElem, childElems))
      } else if (hasRef && !hasName) {
        Some(new AttributeGroupReference(backingElem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}
