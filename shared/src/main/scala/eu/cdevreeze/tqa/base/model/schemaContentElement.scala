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

package eu.cdevreeze.tqa.base.model

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.base.common.Variety
import eu.cdevreeze.yaidom.core.EName

/**
 * Any '''taxonomy schema content element''' in the model. It is either standard schema content, which is a representation
 * of an element in the XML Schema namespace, or it is appinfo content. The schema root element, and imports and
 * linkbaseRefs are not represented in this model. Neither are the annotation and appinfo elements.
 *
 * Due to the fact that this schema model hardly contains any URI references, it plays no role in DTS discovery.
 * Also note that this schema model has no knowledge about XLink.
 *
 * Schema content in this model needs as little context as possible, although substitution group "knowledge" must
 * be provided in order to determine in general whether a global element declaration is a concept (item or tuple)
 * declaration.
 *
 * Several kinds of schema content elements in this model are easy to refer to from relationships, as source or
 * target. Note that these "links" are semantically meaningful, and need no URIs to point to them.
 *
 * Schema content elements are easy to create, and could be part of the basis for easy programmatic taxonomy
 * creation. Note, however, that the taxonomy creation algorithm must at some point worry about closure with
 * respect to DTS discovery, and about containing the needed substitution group ancestry.
 *
 * @author Chris de Vreeze
 */
sealed trait SchemaContentElement {

  def docUri: URI

  /**
   * The attributes in the corresponding XML element, but with QName values replaced by ENames in James Clark notation.
   */
  def attributes: Map[EName, String]

  /**
   * The (immediate) child elements of this schema content element.
   */
  def children: immutable.IndexedSeq[SchemaContentElement]

  /**
   * The resolved name of the corresponding XML element.
   */
  def resolvedName: EName

  // Querying for children, descendants, etc.

  final def findChildElem(p: SchemaContentElement => Boolean): Option[SchemaContentElement] = {
    children.find(p)
  }

  final def findChildElemOfType[A <: SchemaContentElement](
    classTag: ClassTag[A])(
    p: SchemaContentElement => Boolean): Option[A] = {

    implicit val clsTag = classTag

    children.collectFirst { case ch: A if p(ch) => ch }
  }
}

/**
 * Representation of an element in the XML Schema namespace, but excluding the schema root element.
 */
sealed trait StandardSchemaContentElement extends SchemaContentElement {

  def targetNamespaceOption: Option[String]
}

sealed trait AppinfoContentElement extends SchemaContentElement

// The "capabilities" of schema content elements.

/**
 * Super-type of schema components that can be abstract.
 */
sealed trait CanBeAbstract extends StandardSchemaContentElement {

  /**
   * Returns the boolean "abstract" attribute (defaulting to false). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def isAbstract: Boolean = {
    attributes.get(ENames.AbstractEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(false)
  }

  final def isConcrete: Boolean = {
    !isAbstract
  }
}

/**
 * Super-type of schema components that have a name attribute.
 */
sealed trait NamedDeclOrDef extends StandardSchemaContentElement {

  /**
   * Returns the "name" attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def nameAttributeValue: String = {
    attributes(ENames.NameEName)
  }
}

/**
 * Super-type of schema components that are references.
 */
sealed trait Reference extends StandardSchemaContentElement {

  /**
   * Returns the "ref" attribute as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def ref: EName = {
    EName.parse(attributes(ENames.RefEName))
  }
}

// The inheritance hierarchy

/**
 * Particle (in the context of XML Schema), having optional minOccurs and maxOccurs attributes.
 */
sealed trait Particle extends StandardSchemaContentElement {

  /**
   * The minOccurs attribute as integer, defaulting to 1.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def minOccurs: Int = {
    attributes.get(ENames.MinOccursEName).getOrElse("1").toInt
  }

  /**
   * The maxOccurs attribute as optional integer, defaulting to 1, but returning
   * None if unbounded. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def maxOccursOption: Option[Int] = {
    attributes.get(ENames.MaxOccursEName) match {
      case Some("unbounded") => None
      case Some(i) => Some(i.toInt)
      case None => Some(1)
    }
  }
}

// Element declarations or references.

/**
 * Either an element declaration or an element reference.
 */
sealed trait ElementDeclarationOrReference extends StandardSchemaContentElement {

  final def resolvedName: EName = ENames.XsElementEName
}

/**
 * Either a global element declaration or a local element declaration.
 */
sealed trait ElementDeclaration extends ElementDeclarationOrReference with NamedDeclOrDef {

  /**
   * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def typeOption: Option[EName] = {
    attributes.get(ENames.TypeEName).map(EName.parse)
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
final case class GlobalElementDeclaration(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclaration with CanBeAbstract {

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = targetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }

  /**
   * Returns the optional substitution group (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  def substitutionGroupOption: Option[EName] = {
    attributes.get(ENames.SubstitutionGroupEName).map(EName.parse)
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
   * Returns all own or transitively inherited substitution groups. The given mappings are used as the necessary context.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def findAllOwnOrTransitivelyInheritedSubstitutionGroups(substitutionGroupMap: SubstitutionGroupMap): Set[EName] = {
    substitutionGroupOption.toSeq.flatMap { sg =>
      substitutionGroupMap.transitivelyInheritedSubstitutionGroups(sg) + sg
    }.toSet
  }

  /**
   * Returns the optional xbrli:periodType attribute, as `PeriodType`.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def periodTypeOption: Option[PeriodType] = {
    attributes.get(ENames.XbrliPeriodTypeEName).map(PeriodType.fromString)
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
final case class LocalElementDeclaration(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclaration with Particle

/**
 * Element reference, referring to a global element declaration. Like local element declarations it is not a child element of
 * the xs:schema root element, but unlike global and local element declarations it has a ref attribute instead of a name attribute.
 */
final case class ElementReference(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclarationOrReference with Reference

// Attribute declarations or references.

/**
 * Either an attribute declaration or an attribute reference.
 */
sealed trait AttributeDeclarationOrReference extends StandardSchemaContentElement {

  final def resolvedName: EName = ENames.XsAttributeEName
}

/**
 * Either a global attribute declaration or a local attribute declaration.
 */
sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with NamedDeclOrDef {

  /**
   * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  def typeOption: Option[EName] = {
    attributes.get(ENames.TypeEName).map(EName.parse)
  }
}

/**
 * Global attribute declaration. It is an xs:attribute element, and a child element of the xs:schema root element.
 */
final case class GlobalAttributeDeclaration(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclaration {

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = targetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }
}

/**
 * Local attribute declaration. It is an xs:attribute element, but not a direct child element of the xs:schema root element.
 */
final case class LocalAttributeDeclaration(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclaration

/**
 * Attribute reference. It is an xs:attribute element referring to a global attribute declaration. It is not a direct child element of
 * the xs:schema root element.
 */
final case class AttributeReference(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclarationOrReference with Reference

// Type definitions.

/**
 * Type definition. It is either a complex or simple type definition, and it is also either a named or anonymous type definition.
 */
sealed trait TypeDefinition extends StandardSchemaContentElement {

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
  final def targetEName: EName = {
    val tnsOption = targetNamespaceOption
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

  final def resolvedName: EName = ENames.XsSimpleTypeEName

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
      // TODO Better error message!
      sys.error(s"Could not determine variety")
    }
  }

  /**
   * Returns the optional base type. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def baseTypeOption: Option[EName] = variety match {
    case Variety.Atomic =>
      findChildElemOfType(classTag[Restriction])(_ => true).headOption.flatMap(_.baseTypeOption)
    case _ => None
  }
}

/**
 * Complex type definition, so either a named complex type definition or an anonymous complex type definition.
 */
sealed trait ComplexTypeDefinition extends TypeDefinition {

  final def resolvedName: EName = ENames.XsComplexTypeEName

  final def contentElemOption: Option[Content] = {
    val complexContentOption = findChildElemOfType(classTag[ComplexContent])(_ => true)
    val simpleContentOption = findChildElemOfType(classTag[SimpleContent])(_ => true)

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
final case class NamedSimpleTypeDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends NamedTypeDefinition with SimpleTypeDefinition

/**
 * Anonymous simple type definition. It is a non-top-level xs:simpleType element without any name attribute.
 */
final case class AnonymousSimpleTypeDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AnonymousTypeDefinition with SimpleTypeDefinition

/**
 * Named complex type definition. It is a top-level xs:complexType element with a name attribute.
 */
final case class NamedComplexTypeDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends NamedTypeDefinition with ComplexTypeDefinition

/**
 * Anonymous complex type definition. It is a non-top-level xs:complexType element without any name attribute.
 */
final case class AnonymousComplexTypeDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AnonymousTypeDefinition with ComplexTypeDefinition

// Attribute group definitions and references.

/**
 * Attribute group definition or attribute group reference.
 */
sealed trait AttributeGroupDefinitionOrReference extends StandardSchemaContentElement {

  final def resolvedName: EName = ENames.XsAttributeGroupEName
}

/**
 * Attribute group definition, so a top-level xs:attributeGroup element with a name attribute.
 */
final case class AttributeGroupDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AttributeGroupDefinitionOrReference with NamedDeclOrDef

/**
 * Attribute group reference, so a non-top-level xs:attributeGroup element with a ref attribute, referring to an attribute group definition.
 */
final case class AttributeGroupReference(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends AttributeGroupDefinitionOrReference with Reference

// Model group definitions and references.

/**
 * Model group definition or model group reference.
 */
sealed trait ModelGroupDefinitionOrReference extends StandardSchemaContentElement {

  final def resolvedName: EName = ENames.XsGroupEName
}

/**
 * Model group definition, so a top-level xs:group element with a name attribute.
 */
final case class ModelGroupDefinition(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroupDefinitionOrReference

/**
 * Model group reference, so a non-top-level xs:group element with a ref attribute, referring to a model group definition.
 */
final case class ModelGroupReference(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroupDefinitionOrReference with Reference

// Ignoring identity constraints, notations, wildcards.

/**
 * Model group, so either a sequence, choice or all model group.
 */
sealed trait ModelGroup extends StandardSchemaContentElement

/**
 * Sequence model group, so an xs:sequence element.
 */
final case class SequenceModelGroup(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup {

  def resolvedName: EName = ENames.XsSequenceEName
}

/**
 * Choice model group, so an xs:choice element.
 */
final case class ChoiceModelGroup(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup {

  def resolvedName: EName = ENames.XsChoiceEName
}

/**
 * All model group, so an xs:all element.
 */
final case class AllModelGroup(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup {

  def resolvedName: EName = ENames.XsAllEName
}

/**
 * Either a restriction or an extension.
 */
sealed trait RestrictionOrExtension extends StandardSchemaContentElement {

  /**
   * Returns the optional base type. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def baseTypeOption: Option[EName] = {
    attributes.get(ENames.BaseEName).map(EName.parse)
  }
}

/**
 * An xs:restriction element.
 */
final case class Restriction(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends RestrictionOrExtension {

  def resolvedName: EName = ENames.XsRestrictionEName
}

/**
 * An xs:extension element.
 */
final case class Extension(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends RestrictionOrExtension {

  def resolvedName: EName = ENames.XsExtensionEName
}

/**
 * Either simple content or complex content.
 */
sealed trait Content extends StandardSchemaContentElement {

  /**
   * Returns the derivation. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def derivation: RestrictionOrExtension = {
    // TODO Better error message!

    findChildElemOfType(classTag[Restriction])(_ => true)
      .orElse(findChildElemOfType(classTag[Extension])(_ => true))
      .getOrElse(sys.error(s"Expected xs:restriction or xs:extension child element"))
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
final case class SimpleContent(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends Content {

  def resolvedName: EName = ENames.XsSimpleContentEName
}

/**
 * An xs:complexContent element.
 */
final case class ComplexContent(
  docUri: URI,
  targetNamespaceOption: Option[String],
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends Content {

  def resolvedName: EName = ENames.XsComplexContentEName
}

// TODO Sub-types of AppinfoContentElement, like role types and arcrole types.

/**
 * Any `SchemaContentElement` not recognized as an instance of one of the other concrete `SchemaContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `SchemaContentElement` sub-type hierarchy, or it is
 * syntactically incorrect. As an example of the latter, an xs:element XML element with both a name and a ref attribute is clearly invalid.
 */
final case class OtherSchemaContentElement(
  docUri: URI,
  targetNamespaceOption: Option[String],
  resolvedName: EName,
  attributes: Map[EName, String],
  children: immutable.IndexedSeq[SchemaContentElement]) extends SchemaContentElement
