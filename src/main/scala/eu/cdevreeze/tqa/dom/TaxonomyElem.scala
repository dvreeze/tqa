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

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.collection.immutable

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
sealed trait TaxonomyXLinkElem extends TaxonomyElem

sealed trait TaxonomyExtendedLinkElem extends TaxonomyXLinkElem

sealed trait TaxonomyXLinkArcElem extends TaxonomyXLinkElem

sealed trait TaxonomyXLinkResourceElem extends TaxonomyXLinkElem

sealed trait TaxonomyXLinkLocatorElem extends TaxonomyXLinkElem

sealed trait TaxonomySimpleLinkElem extends TaxonomyXLinkElem

// Schema content or linkbase content.

/**
 * Element in the xs namespace.
 */
sealed trait XsdElem extends TaxonomyElem

/**
 * Element in the link namespace.
 */
sealed trait LinkElem extends TaxonomyElem

// The class inheritance hierarchy, under TaxonomyElem. First the root elements.

/**
 * The xs:schema root element of a taxonomy schema.
 */
final class XsdRootElem private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with XsdElem with TaxonomyRootElem

/**
 * The link:linkbase root element of a taxonomy schema. It may be embedded within a taxonomy schema document.
 */
final class LinkbaseRootElem private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with LinkElem with TaxonomyRootElem

// The remaining classes. First for schema content, then for linkbase content.

sealed trait Particle extends XsdElem

// Element declarations or references.

sealed trait ElementDeclarationOrReference extends XsdElem

sealed trait ElementDeclaration extends ElementDeclarationOrReference

final class GlobalElementDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration

final class LocalElementDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclaration with Particle

final class ElementReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ElementDeclarationOrReference

// Attribute declarations or references.

sealed trait AttributeDeclarationOrReference extends XsdElem

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference

final class GlobalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class LocalAttributeDeclaration private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclaration

final class AttributeReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeDeclarationOrReference

// Type definitions.

sealed trait TypeDefinition extends XsdElem

sealed trait NamedTypeDefinition extends TypeDefinition

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

// Attribute groups.

sealed trait AttributeGroupDefinitionOrReference extends XsdElem

final class AttributeGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference

final class AttributeGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with AttributeGroupDefinitionOrReference

// Model groups.

sealed trait ModelGroupDefinitionOrReference extends XsdElem

final class ModelGroupDefinition private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

final class ModelGroupReference private[dom] (
  backingElem: BackingElemApi,
  childElems: immutable.IndexedSeq[TaxonomyElem]) extends TaxonomyElem(backingElem, childElems) with ModelGroupDefinitionOrReference

// Ignoring identity constraints, notations, model groups, wildcards, complex/simple content.

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

// TODO Linkbase content.

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

object XsdElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): XsdElem = {
    require(backingElem.resolvedName.namespaceUriOption == Some(XsNamespace))

    backingElem.resolvedName match {
      case XsElementEName     => ElementDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsAttributeEName   => AttributeDeclarationOrReference.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsSimpleTypeEName  => SimpleTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsComplexTypeEName => ComplexTypeDefinition.opt(backingElem, childElems).getOrElse(new OtherXsdElem(backingElem, childElems))
      case XsSchemaEName      => new XsdRootElem(backingElem, childElems)
      case XsAnnotationEName  => new Annotation(backingElem, childElems)
      case XsAppinfoEName     => new Appinfo(backingElem, childElems)
      case XsImportEName      => new Import(backingElem, childElems)
      case XsIncludeEName     => new Include(backingElem, childElems)
      case _                  => new OtherXsdElem(backingElem, childElems)
      // TODO Model groups and attribute groups
    }
  }
}

object LinkElem {

  private[dom] def apply(backingElem: BackingElemApi, childElems: immutable.IndexedSeq[TaxonomyElem]): LinkElem = {
    require(backingElem.resolvedName.namespaceUriOption == Some(LinkNamespace))

    backingElem.resolvedName match {
      case LinkLinkbaseEName => new LinkbaseRootElem(backingElem, childElems)
      case _                 => new OtherLinkbaseElem(backingElem, childElems)
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
