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

import scala.collection.immutable
import scala.reflect.classTag
import scala.util.Try

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.base.common.Variety
import eu.cdevreeze.tqa.common.schematypes.AttributeSupport
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemApi
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem

/**
 * Any '''taxonomy schema content element''' in the model. It is either standard schema content, which is a representation
 * of an element in the XML Schema namespace, or it is appinfo content. The schema root element, and imports and
 * linkbaseRefs are not represented in this model. Neither are the topmost annotation and their child appinfo or documentation elements.
 *
 * This schema content model offers the yaidom SubtypeAwareElemApi API. Note, however, that most schema content
 * elements have no child elements, but type definitions typically do have descendant elements.
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
 * Schema content elements are relatively easy to create on the fly, and could be part of the basis for easy programmatic
 * taxonomy creation. Note, however, that the taxonomy creation algorithm must at some point worry about closure with
 * respect to DTS discovery, and about containing the needed substitution group ancestry.
 *
 * When creating schema content elements on the fly, keep in mind that the element and its descendants should agree on
 * the optional target namespace. This is not checked!
 *
 * See http://www.datypic.com/sc/xsd for the XML Schema model (unaware of XBRL).
 *
 * TODO Remove linkbaseRefs from model.
 *
 * @author Chris de Vreeze
 */
sealed trait SchemaContentElement extends SubtypeAwareElemApi with SubtypeAwareElemLike {

  type ThisElem = SchemaContentElement

  /**
   * The optional target namespace that "is in scope".
   */
  def targetNamespaceOption: Option[String]

  /**
   * The resolved name of the corresponding XML element.
   */
  def resolvedName: EName

  /**
   * The (partially type-safe) attributes of the corresponding XML element.
   */
  def attributes: SchemaContentElement.Attributes

  /**
   * The (immediate) child elements of this schema content element.
   */
  def childElems: immutable.IndexedSeq[SchemaContentElement]

  // Methods needed for completing the SubtypeAwareElemApi API

  final def thisElem: SchemaContentElement = this

  final def findAllChildElems: immutable.IndexedSeq[SchemaContentElement] = {
    childElems
  }
}

/**
 * Representation of an element in the XML Schema namespace, but excluding the schema root element.
 * More precisely, any child element of the schema root element that is not an xs:annotation and that is
 * in the XML Schema namespace, or any of its desdendants in the XML Schema namespace (even xs:annotation).
 */
sealed trait StandardSchemaContentElement extends SchemaContentElement

/**
 * Representation of an element in an xs:appinfo element (as descendant). The ancestor xs:appinfo element
 * may or may not be a child of a top-level xs:annotation element.
 */
sealed trait AppinfoContentElement extends SchemaContentElement

/**
 * Representation of an element in an xs:documentation element (as descendant). The ancestor xs:documentation element
 * may or may not be a child of a top-level xs:annotation element.
 */
sealed trait DocumentationContentElement extends SchemaContentElement

// The "capabilities" of schema content elements.

/**
 * Super-type of schema components that can be abstract.
 */
sealed trait CanBeAbstract extends StandardSchemaContentElement {

  def attributes: CanBeAbstract.Attributes

  final def isAbstract: Boolean = attributes.isAbstract

  final def isConcrete: Boolean = !isAbstract
}

/**
 * Super-type of schema components that have a name attribute.
 */
sealed trait NamedDeclOrDef extends StandardSchemaContentElement {

  def attributes: NamedDeclOrDef.Attributes

  final def nameAttributeValue: String = attributes.name
}

/**
 * Super-type of schema components that are references.
 */
sealed trait Reference extends StandardSchemaContentElement {

  def attributes: Reference.Attributes

  final def ref: EName = attributes.ref
}

// The inheritance hierarchy

/**
 * Particle (in the context of XML Schema), having optional minOccurs and maxOccurs attributes.
 */
sealed trait Particle extends StandardSchemaContentElement {

  def attributes: Particle.Attributes

  /**
   * The minOccurs attribute as integer, defaulting to 1.
   */
  final def minOccurs: Int = attributes.minOccurs

  /**
   * The maxOccurs attribute as optional integer, defaulting to 1, but returning
   * None if unbounded.
   */
  final def maxOccursOption: Option[Int] = attributes.maxOccursOption
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

  def attributes: ElementDeclaration.Attributes

  final def typeOption: Option[EName] = attributes.typeOption
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
 *
 * The "other child elements" passed below, if any, are xs:unique, xs:key and xs:keyref elements.
 */
final case class GlobalElementDeclaration(
    targetNamespaceOption: Option[String],
    attributes: GlobalElementDeclaration.Attributes,
    annotationOption: Option[Annotation],
    anonymousTypeDefinitionOption: Option[AnonymousTypeDefinition],
    otherChildElems: immutable.IndexedSeq[SchemaContentElement])
    extends ElementDeclaration
    with CanBeAbstract {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ anonymousTypeDefinitionOption.toIndexedSeq ++ otherChildElems
  }

  /**
   * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
   * name attribute as local part. This may fail with an exception if the taxonomy is not schema-valid, although such a failure
   * is very unlikely.
   */
  def targetEName: EName = {
    val tnsOption = targetNamespaceOption
    EName(tnsOption, nameAttributeValue)
  }

  def substitutionGroupOption: Option[EName] = attributes.substitutionGroupOption

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
      substitutionGroupMap.transitivelyInheritedSubstitutionGroupsIncludingSelf(sg)
    }.toSet
  }

  def periodTypeOption: Option[PeriodType] = {
    attributes.otherAttributes.get(ENames.XbrliPeriodTypeEName).map(PeriodType.fromString)
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
    targetNamespaceOption: Option[String],
    attributes: LocalElementDeclaration.Attributes,
    annotationOption: Option[Annotation],
    anonymousTypeDefinitionOption: Option[AnonymousTypeDefinition],
    otherChildElems: immutable.IndexedSeq[SchemaContentElement])
    extends ElementDeclaration
    with Particle {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ anonymousTypeDefinitionOption.toIndexedSeq ++ otherChildElems
  }
}

/**
 * Element reference, referring to a global element declaration. Like local element declarations it is not a child element of
 * the xs:schema root element, but unlike global and local element declarations it has a ref attribute instead of a name attribute.
 */
final case class ElementReference(
    targetNamespaceOption: Option[String],
    attributes: ElementReference.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ElementDeclarationOrReference
    with Reference

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

  def attributes: AttributeDeclaration.Attributes

  /**
   * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def typeOption: Option[EName] = attributes.typeOption
}

/**
 * Global attribute declaration. It is an xs:attribute element, and a child element of the xs:schema root element.
 */
final case class GlobalAttributeDeclaration(
    targetNamespaceOption: Option[String],
    attributes: GlobalAttributeDeclaration.Attributes,
    annotationOption: Option[Annotation],
    anonymousSimpleTypeDefinitionOption: Option[AnonymousSimpleTypeDefinition])
    extends AttributeDeclaration {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ anonymousSimpleTypeDefinitionOption.toIndexedSeq
  }

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
    targetNamespaceOption: Option[String],
    attributes: LocalAttributeDeclaration.Attributes,
    annotationOption: Option[Annotation],
    anonymousSimpleTypeDefinitionOption: Option[AnonymousSimpleTypeDefinition])
    extends AttributeDeclaration {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ anonymousSimpleTypeDefinitionOption.toIndexedSeq
  }
}

/**
 * Attribute reference. It is an xs:attribute element referring to a global attribute declaration. It is not a direct child element of
 * the xs:schema root element.
 */
final case class AttributeReference(
    targetNamespaceOption: Option[String],
    attributes: AttributeReference.Attributes,
    annotationOption: Option[Annotation],
    anonymousSimpleTypeDefinitionOption: Option[AnonymousSimpleTypeDefinition])
    extends AttributeDeclarationOrReference
    with Reference {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ anonymousSimpleTypeDefinitionOption.toIndexedSeq
  }
}

// Type definitions.

/**
 * Type definition. It is either a complex or simple type definition, and it is also either a named or anonymous type definition.
 */
sealed trait TypeDefinition extends StandardSchemaContentElement {

  def attributes: TypeDefinition.Attributes

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

  def attributes: NamedTypeDefinition.Attributes

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
      findChildElemOfType(classTag[Restriction])(anyElem).headOption.flatMap(_.baseTypeOption)
    case _ => None
  }
}

/**
 * Complex type definition, so either a named complex type definition or an anonymous complex type definition.
 */
sealed trait ComplexTypeDefinition extends TypeDefinition {

  final def resolvedName: EName = ENames.XsComplexTypeEName

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
final case class NamedSimpleTypeDefinition(
    targetNamespaceOption: Option[String],
    attributes: NamedSimpleTypeDefinition.Attributes,
    annotationOption: Option[Annotation],
    contentElement: SchemaContentElement)
    extends NamedTypeDefinition
    with SimpleTypeDefinition {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ immutable.IndexedSeq(contentElement)
  }
}

/**
 * Anonymous simple type definition. It is a non-top-level xs:simpleType element without any name attribute.
 */
final case class AnonymousSimpleTypeDefinition(
    targetNamespaceOption: Option[String],
    attributes: AnonymousSimpleTypeDefinition.Attributes,
    annotationOption: Option[Annotation],
    contentElement: SchemaContentElement)
    extends AnonymousTypeDefinition
    with SimpleTypeDefinition {

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ immutable.IndexedSeq(contentElement)
  }
}

/**
 * Named complex type definition. It is a top-level xs:complexType element with a name attribute.
 */
final case class NamedComplexTypeDefinition(
    targetNamespaceOption: Option[String],
    attributes: NamedComplexTypeDefinition.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends NamedTypeDefinition
    with ComplexTypeDefinition

/**
 * Anonymous complex type definition. It is a non-top-level xs:complexType element without any name attribute.
 */
final case class AnonymousComplexTypeDefinition(
    targetNamespaceOption: Option[String],
    attributes: AnonymousComplexTypeDefinition.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends AnonymousTypeDefinition
    with ComplexTypeDefinition

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
    targetNamespaceOption: Option[String],
    attributes: AttributeGroupDefinition.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends AttributeGroupDefinitionOrReference
    with NamedDeclOrDef

/**
 * Attribute group reference, so a non-top-level xs:attributeGroup element with a ref attribute, referring to an attribute group definition.
 */
final case class AttributeGroupReference(
    targetNamespaceOption: Option[String],
    attributes: AttributeGroupReference.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends AttributeGroupDefinitionOrReference
    with Reference

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
    targetNamespaceOption: Option[String],
    attributes: ModelGroupDefinition.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ModelGroupDefinitionOrReference
    with NamedDeclOrDef

/**
 * Model group reference, so a non-top-level xs:group element with a ref attribute, referring to a model group definition.
 */
final case class ModelGroupReference(
    targetNamespaceOption: Option[String],
    attributes: ModelGroupReference.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ModelGroupDefinitionOrReference
    with Reference

// Ignoring identity constraints, notations, wildcards.

/**
 * Model group, so either a sequence, choice or all model group.
 */
sealed trait ModelGroup extends StandardSchemaContentElement

/**
 * Sequence model group, so an xs:sequence element.
 */
final case class SequenceModelGroup(
    targetNamespaceOption: Option[String],
    attributes: SequenceModelGroup.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ModelGroup {

  def resolvedName: EName = ENames.XsSequenceEName
}

/**
 * Choice model group, so an xs:choice element.
 */
final case class ChoiceModelGroup(
    targetNamespaceOption: Option[String],
    attributes: ChoiceModelGroup.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ModelGroup {

  def resolvedName: EName = ENames.XsChoiceEName
}

/**
 * All model group, so an xs:all element.
 */
final case class AllModelGroup(
    targetNamespaceOption: Option[String],
    attributes: AllModelGroup.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends ModelGroup {

  def resolvedName: EName = ENames.XsAllEName
}

/**
 * Either a restriction or an extension.
 */
sealed trait RestrictionOrExtension extends StandardSchemaContentElement {

  def attributes: RestrictionOrExtension.Attributes

  final def baseTypeOption: Option[EName] = attributes.baseTypeOption
}

/**
 * An xs:restriction element.
 */
final case class Restriction(
    targetNamespaceOption: Option[String],
    attributes: Restriction.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends RestrictionOrExtension {

  def resolvedName: EName = ENames.XsRestrictionEName
}

/**
 * An xs:extension element.
 */
final case class Extension(
    targetNamespaceOption: Option[String],
    attributes: Extension.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends RestrictionOrExtension {

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

    findChildElemOfType(classTag[Restriction])(anyElem)
      .orElse(findChildElemOfType(classTag[Extension])(anyElem))
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
    targetNamespaceOption: Option[String],
    attributes: SchemaContentElement.Attributes,
    annotationOption: Option[Annotation],
    restrictionOrExtension: RestrictionOrExtension)
    extends Content {

  def resolvedName: EName = ENames.XsSimpleContentEName

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ immutable.IndexedSeq(restrictionOrExtension)
  }
}

/**
 * An xs:complexContent element.
 */
final case class ComplexContent(
    targetNamespaceOption: Option[String],
    attributes: SchemaContentElement.Attributes,
    annotationOption: Option[Annotation],
    restrictionOrExtension: RestrictionOrExtension)
    extends Content {

  def resolvedName: EName = ENames.XsComplexContentEName

  def childElems: immutable.IndexedSeq[SchemaContentElement] = {
    annotationOption.toIndexedSeq ++ immutable.IndexedSeq(restrictionOrExtension)
  }
}

final case class Annotation(
    targetNamespaceOption: Option[String],
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends StandardSchemaContentElement {

  def resolvedName: EName = ENames.XsAnnotationEName
}

final case class Appinfo(
    targetNamespaceOption: Option[String],
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends StandardSchemaContentElement {

  def resolvedName: EName = ENames.XsAppinfoEName
}

final case class Documentation(
    targetNamespaceOption: Option[String],
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement])
    extends StandardSchemaContentElement {

  def resolvedName: EName = ENames.XsDocumentationEName
}

// TODO Sub-types of AppinfoContentElement, like role types and arcrole types.

/**
 * Any `StandardSchemaContentElement` not recognized as an instance of one of the other concrete `StandardSchemaContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `StandardSchemaContentElement` sub-type hierarchy, or it is
 * syntactically incorrect. As an example of the latter, an xs:element XML element with both a name and a ref attribute is clearly invalid.
 */
final case class OtherStandardSchemaContentElement(
    targetNamespaceOption: Option[String],
    resolvedName: EName,
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement],
    text: String)
    extends StandardSchemaContentElement

/**
 * Any `AppinfoContentElement` not recognized as an instance of one of the other concrete `AppinfoContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `AppinfoContentElement` sub-type hierarchy, or it is
 * syntactically incorrect.
 */
final case class OtherAppinfoContentElement(
    targetNamespaceOption: Option[String],
    resolvedName: EName,
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement],
    text: String)
    extends AppinfoContentElement

/**
 * Any `DocumentationContentElement` not recognized as an instance of one of the other concrete `DocumentationContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `DocumentationContentElement` sub-type hierarchy, or it is
 * syntactically incorrect.
 */
final case class OtherDocumentationContentElement(
    targetNamespaceOption: Option[String],
    resolvedName: EName,
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement],
    text: String)
    extends DocumentationContentElement

/**
 * Any schema content element that is not even any standard schema content, any appinfo content element or any documentation content element.
 * Probably this category refers to schema-invalid content.
 */
final case class OtherSchemaContentElement(
    targetNamespaceOption: Option[String],
    resolvedName: EName,
    attributes: SchemaContentElement.Attributes,
    childElems: immutable.IndexedSeq[SchemaContentElement],
    text: String)
    extends SchemaContentElement

// Companion objects

object SchemaContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = SchemaContentElement

  trait Attributes {
    def idOption: Option[String]
    def otherAttributes: Map[EName, String]
  }

  def attributes(rawAttributes: Map[EName, String]): SchemaContentElement.Attributes = {
    new SchemaContentElement.Attributes {
      def idOption: Option[String] = rawAttributes.get(ENames.IdEName)
      def otherAttributes: Map[EName, String] = rawAttributes.filter(_._1 != ENames.IdEName)
    }
  }

  def collectTopmostSchemaContent(rootElem: ScopedNodes.Elem): immutable.IndexedSeq[SchemaContentElement] = {
    require(
      rootElem.resolvedName == ENames.XsSchemaEName,
      s"Expected ${ENames.XsSchemaEName} but got ${rootElem.resolvedName}")

    val tnsOption = rootElem.attributeOption(ENames.TargetNamespaceEName)

    // Appinfo (top-level)

    val appinfoChildren =
      rootElem
        .filterChildElems(_.resolvedName == ENames.XsAnnotationEName)
        .flatMap(_.filterChildElems(_.resolvedName == ENames.XsAppinfoEName))
        .flatMap(_.findAllChildElems)

    val appinfoAncestorENames =
      immutable.IndexedSeq(ENames.XsAppinfoEName, ENames.XsAnnotationEName, ENames.XsSchemaEName)

    val appinfoContent = appinfoChildren.map(e => AppinfoContentElement.build(e, appinfoAncestorENames, tnsOption))

    // Documentation (top-level)

    val documentationChildren =
      rootElem
        .filterChildElems(_.resolvedName == ENames.XsAnnotationEName)
        .flatMap(_.filterChildElems(_.resolvedName == ENames.XsDocumentationEName))
        .flatMap(_.findAllChildElems)

    val documentationAncestorENames =
      immutable.IndexedSeq(ENames.XsDocumentationEName, ENames.XsAnnotationEName, ENames.XsSchemaEName)

    val documentationContent =
      documentationChildren.map(e => DocumentationContentElement.build(e, documentationAncestorENames, tnsOption))

    // Standard schema content (and descendant appinfo and documentation content)

    val topLevelChildren =
      rootElem.filterChildElems { e =>
        e.resolvedName.namespaceUriOption.contains(Namespaces.XsNamespace) &&
        !Set(ENames.XsImportEName, ENames.XsIncludeEName, ENames.XsAnnotationEName).contains(e.resolvedName)
      }

    val topLevelAncestorENames = immutable.IndexedSeq(ENames.XsSchemaEName)

    val topLevelContent =
      topLevelChildren.map(e => StandardSchemaContentElement.build(e, topLevelAncestorENames, tnsOption))

    appinfoContent ++ documentationContent ++ topLevelContent
  }

  def build(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String]): SchemaContentElementType = {

    // Recursive calls

    val childElems: immutable.IndexedSeq[SchemaContentElementSuperType] = elem.findAllChildElems.map { che =>
      build(che, elem.resolvedName +: ancestorENames, targetNamespaceOption)
    }

    opt(elem, ancestorENames, targetNamespaceOption, childElems).getOrElse {
      val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

      OtherSchemaContentElement(targetNamespaceOption, elem.resolvedName, attrs, childElems, elem.text)
    }
  }

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (ancestorENames.contains(ENames.XsAppinfoEName)) {
      AppinfoContentElement.opt(elem, ancestorENames, targetNamespaceOption, childElems)
    } else if (ancestorENames.contains(ENames.XsDocumentationEName)) {
      DocumentationContentElement.opt(elem, ancestorENames, targetNamespaceOption, childElems)
    } else {
      StandardSchemaContentElement.opt(elem, ancestorENames, targetNamespaceOption, childElems)
    }
  }
}

object StandardSchemaContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = StandardSchemaContentElement

  def build(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String]): SchemaContentElementType = {

    SchemaContentElement
      .build(elem, ancestorENames, targetNamespaceOption)
      .asInstanceOf[StandardSchemaContentElement]
  }

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (ancestorENames.contains(ENames.XsAppinfoEName) || ancestorENames.contains(ENames.XsDocumentationEName) ||
        !elem.resolvedName.namespaceUriOption.contains(Namespaces.XsNamespace)) {

      None
    } else {
      elem.resolvedName match {
        case ENames.XsSchemaEName | ENames.XsImportEName | ENames.XsIncludeEName =>
          // We do not model the xs:schema, xs:import and xs:include elements
          None
        case ENames.XsAnnotationEName =>
          val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)

          if (parentIsSchema) {
            None
          } else {
            val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

            Some(Annotation(targetNamespaceOption, attrs, childElems))
          }
        case ENames.XsAppinfoEName =>
          if (ancestorENames.take(2) == List(ENames.XsAnnotationEName, ENames.XsSchemaEName)) {
            None
          } else {
            val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

            Some(Appinfo(targetNamespaceOption, attrs, childElems))
          }
        case ENames.XsDocumentationEName =>
          if (ancestorENames.take(2) == List(ENames.XsAnnotationEName, ENames.XsSchemaEName)) {
            None
          } else {
            val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

            Some(Documentation(targetNamespaceOption, attrs, childElems))
          }
        case ENames.XsElementEName =>
          ElementDeclarationOrReference
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsAttributeEName =>
          AttributeDeclarationOrReference
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsSimpleTypeEName =>
          SimpleTypeDefinition
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsComplexTypeEName =>
          ComplexTypeDefinition
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsGroupEName =>
          ModelGroupDefinitionOrReference
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsAttributeGroupEName =>
          AttributeGroupDefinitionOrReference
            .opt(elem, ancestorENames, targetNamespaceOption, childElems)
            .orElse(Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems)))
        case ENames.XsSequenceEName =>
          SequenceModelGroup.optAttributes(elem.resolvedAttributes.toMap).map { attrs =>
            new SequenceModelGroup(targetNamespaceOption, attrs, childElems)
          }
        case ENames.XsChoiceEName =>
          ChoiceModelGroup.optAttributes(elem.resolvedAttributes.toMap).map { attrs =>
            new ChoiceModelGroup(targetNamespaceOption, attrs, childElems)
          }
        case ENames.XsAllEName =>
          AllModelGroup.optAttributes(elem.resolvedAttributes.toMap).map { attrs =>
            new AllModelGroup(targetNamespaceOption, attrs, childElems)
          }
        case ENames.XsRestrictionEName =>
          Restriction.optAttributes(elem.resolvedAttributes.toMap, elem.scope).map { attrs =>
            new Restriction(targetNamespaceOption, attrs, childElems)
          }
        case ENames.XsExtensionEName =>
          Extension.optAttributes(elem.resolvedAttributes.toMap, elem.scope).map { attrs =>
            new Extension(targetNamespaceOption, attrs, childElems)
          }
        case ENames.XsSimpleContentEName =>
          val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

          Try(
            new SimpleContent(
              targetNamespaceOption,
              attrs,
              childElems.collectFirst { case ann: Annotation => ann },
              childElems.collectFirst { case e: RestrictionOrExtension => e }.get)).toOption
        case ENames.XsComplexContentEName =>
          val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

          Try(
            new ComplexContent(
              targetNamespaceOption,
              attrs,
              childElems.collectFirst { case ann: Annotation => ann },
              childElems.collectFirst { case e: RestrictionOrExtension => e }.get)).toOption
        case _ =>
          Some(OtherStandardSchemaContentElement.apply(elem, targetNamespaceOption, childElems))
      }
    }
  }
}

object AppinfoContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AppinfoContentElement

  def build(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String]): SchemaContentElementType = {

    SchemaContentElement
      .build(elem, ancestorENames, targetNamespaceOption)
      .asInstanceOf[AppinfoContentElement]
  }

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (!ancestorENames.contains(ENames.XsAppinfoEName) || elem.resolvedName.namespaceUriOption.contains(
          Namespaces.XsNamespace)) {
      None
    } else {
      // TODO

      Some(
        new OtherAppinfoContentElement(
          targetNamespaceOption,
          elem.resolvedName,
          SchemaContentElement.attributes(elem.resolvedAttributes.toMap),
          childElems,
          elem.text))
    }
  }
}

object DocumentationContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = DocumentationContentElement

  def build(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String]): SchemaContentElementType = {

    SchemaContentElement
      .build(elem, ancestorENames, targetNamespaceOption)
      .asInstanceOf[DocumentationContentElement]
  }

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (!ancestorENames.contains(ENames.XsDocumentationEName) || elem.resolvedName.namespaceUriOption.contains(
          Namespaces.XsNamespace)) {
      None
    } else {
      // TODO

      Some(
        new OtherDocumentationContentElement(
          targetNamespaceOption,
          elem.resolvedName,
          SchemaContentElement.attributes(elem.resolvedAttributes.toMap),
          childElems,
          elem.text))
    }
  }
}

object CanBeAbstract {

  trait Attributes extends SchemaContentElement.Attributes {
    def isAbstract: Boolean
  }
}

object NamedDeclOrDef {

  trait Attributes extends SchemaContentElement.Attributes {
    def name: String
  }
}

object Reference {

  trait Attributes extends SchemaContentElement.Attributes {
    def ref: EName
  }
}

object Particle {

  trait Attributes extends SchemaContentElement.Attributes {

    /**
     * The minOccurs attribute value, defaulting to 1
     */
    def minOccurs: Int

    /**
     * The maxOccurs attribute as optional integer, defaulting to 1, but returning None for "unbounded"
     */
    def maxOccursOption: Option[Int]
  }
}

object ElementDeclarationOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ElementDeclarationOrReference

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsElementEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined
      val hasRef = elem.attributeOption(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        GlobalElementDeclaration.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(
            new GlobalElementDeclaration(
              targetNamespaceOption,
              attrs,
              childElems.collectFirst { case ann: Annotation            => ann },
              childElems.collectFirst { case t: AnonymousTypeDefinition => t },
              childElems.filter(
                e =>
                  e.resolvedName != ENames.XsAnnotationEName &&
                    e.resolvedName != ENames.XsSimpleTypeEName &&
                    e.resolvedName != ENames.XsComplexTypeEName)
            ))
        }
      } else if (!parentIsSchema && hasName && !hasRef) {
        LocalElementDeclaration.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(
            new LocalElementDeclaration(
              targetNamespaceOption,
              attrs,
              childElems.collectFirst { case ann: Annotation            => ann },
              childElems.collectFirst { case t: AnonymousTypeDefinition => t },
              childElems.filter(
                e =>
                  e.resolvedName != ENames.XsAnnotationEName &&
                    e.resolvedName != ENames.XsSimpleTypeEName &&
                    e.resolvedName != ENames.XsComplexTypeEName)
            ))
        }
      } else if (!parentIsSchema && !hasName && hasRef) {
        ElementReference.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new ElementReference(targetNamespaceOption, attrs, childElems))
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object ElementDeclaration {

  trait Attributes extends NamedDeclOrDef.Attributes {
    def typeOption: Option[EName]
  }
}

object GlobalElementDeclaration {

  final case class Attributes(
      idOption: Option[String],
      name: String,
      typeOption: Option[EName],
      substitutionGroupOption: Option[EName],
      isNillable: Boolean,
      isAbstract: Boolean,
      otherAttributes: Map[EName, String])
      extends ElementDeclaration.Attributes
      with CanBeAbstract.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        AttributeSupport.optType(rawAttributes, scope),
        AttributeSupport.optSubstitutionGroup(rawAttributes, scope),
        AttributeSupport.optNillable(rawAttributes).getOrElse(false),
        AttributeSupport.optAbstract(rawAttributes).getOrElse(false),
        rawAttributes.filter(
          kv =>
            rawAttributes.keySet
              .diff(
                Set(
                  ENames.IdEName,
                  ENames.NameEName,
                  ENames.TypeEName,
                  ENames.SubstitutionGroupEName,
                  ENames.NillableEName,
                  ENames.AbstractEName))
              .contains(kv._1))
      )).toOption
  }
}

object LocalElementDeclaration {

  final case class Attributes(
      idOption: Option[String],
      name: String,
      typeOption: Option[EName],
      minOccurs: Int,
      maxOccursOption: Option[Int],
      isNillable: Boolean,
      otherAttributes: Map[EName, String])
      extends ElementDeclaration.Attributes
      with Particle.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    val maxOccursOption: Option[String] = AttributeSupport.optMaxOccurs(rawAttributes)

    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        AttributeSupport.optType(rawAttributes, scope),
        AttributeSupport.optMinOccurs(rawAttributes).getOrElse(1),
        if (maxOccursOption.isEmpty) Some(1)
        else if (maxOccursOption.contains("unbounded")) None
        else maxOccursOption.map(_.toInt),
        AttributeSupport.optNillable(rawAttributes).getOrElse(false),
        rawAttributes.filter(
          kv =>
            rawAttributes.keySet
              .diff(
                Set(
                  ENames.IdEName,
                  ENames.NameEName,
                  ENames.TypeEName,
                  ENames.MinOccursEName,
                  ENames.MaxOccursEName,
                  ENames.NillableEName))
              .contains(kv._1))
      )).toOption
  }
}

object ElementReference {

  final case class Attributes(idOption: Option[String], ref: EName, otherAttributes: Map[EName, String])
      extends Reference.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optRef(rawAttributes, scope).get,
        rawAttributes
          .filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.RefEName)).contains(kv._1))
          .toMap
      )).toOption
  }
}

object AttributeDeclarationOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AttributeDeclarationOrReference

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsAttributeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined
      val hasRef = elem.attributeOption(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        GlobalAttributeDeclaration.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new GlobalAttributeDeclaration(targetNamespaceOption, attrs, childElems.collectFirst {
            case ann: Annotation                                             => ann
          }, childElems.collectFirst { case t: AnonymousSimpleTypeDefinition => t }))
        }
      } else if (!parentIsSchema && hasName && !hasRef) {
        LocalAttributeDeclaration.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new LocalAttributeDeclaration(targetNamespaceOption, attrs, childElems.collectFirst {
            case ann: Annotation                                             => ann
          }, childElems.collectFirst { case t: AnonymousSimpleTypeDefinition => t }))
        }
      } else if (!parentIsSchema && !hasName && hasRef) {
        AttributeReference.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new AttributeReference(targetNamespaceOption, attrs, childElems.collectFirst {
            case ann: Annotation                                             => ann
          }, childElems.collectFirst { case t: AnonymousSimpleTypeDefinition => t }))
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object AttributeDeclaration {

  trait Attributes extends NamedDeclOrDef.Attributes {
    def typeOption: Option[EName]
  }
}

object GlobalAttributeDeclaration {

  final case class Attributes(
      idOption: Option[String],
      name: String,
      typeOption: Option[EName],
      otherAttributes: Map[EName, String])
      extends AttributeDeclaration.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        AttributeSupport.optType(rawAttributes, scope),
        rawAttributes.filter(kv =>
          rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName, ENames.TypeEName)).contains(kv._1))
      )).toOption
  }
}

object LocalAttributeDeclaration {

  final case class Attributes(
      idOption: Option[String],
      name: String,
      typeOption: Option[EName],
      otherAttributes: Map[EName, String])
      extends AttributeDeclaration.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        AttributeSupport.optType(rawAttributes, scope),
        rawAttributes.filter(kv =>
          rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName, ENames.TypeEName)).contains(kv._1))
      )).toOption
  }
}

object AttributeReference {

  final case class Attributes(idOption: Option[String], ref: EName, otherAttributes: Map[EName, String])
      extends Reference.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optRef(rawAttributes, scope).get,
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.RefEName)).contains(kv._1))
      )).toOption
  }
}

object TypeDefinition {

  trait Attributes extends SchemaContentElement.Attributes
}

object NamedTypeDefinition {

  trait Attributes extends TypeDefinition.Attributes with NamedDeclOrDef.Attributes
}

object AnonymousTypeDefinition {

  trait Attributes extends TypeDefinition.Attributes
}

object SimpleTypeDefinition extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = SimpleTypeDefinition

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsSimpleTypeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined

      if (parentIsSchema && hasName) {
        NamedSimpleTypeDefinition.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Try(new NamedSimpleTypeDefinition(targetNamespaceOption, attrs, childElems.collectFirst {
            case ann: Annotation => ann
          }, childElems.find(_.resolvedName != ENames.XsAnnotationEName).get)).toOption
        }
      } else if (!parentIsSchema && !hasName) {
        AnonymousSimpleTypeDefinition.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Try(new AnonymousSimpleTypeDefinition(targetNamespaceOption, attrs, childElems.collectFirst {
            case ann: Annotation => ann
          }, childElems.find(_.resolvedName != ENames.XsAnnotationEName).get)).toOption
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object NamedSimpleTypeDefinition {

  final case class Attributes(idOption: Option[String], name: String, otherAttributes: Map[EName, String])
      extends NamedTypeDefinition.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName)).contains(kv._1))
      )).toOption
  }
}

object AnonymousSimpleTypeDefinition {

  final case class Attributes(idOption: Option[String], otherAttributes: Map[EName, String])
      extends AnonymousTypeDefinition.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName)).contains(kv._1)))).toOption
  }
}

object ComplexTypeDefinition extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ComplexTypeDefinition

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsComplexTypeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined

      if (parentIsSchema && hasName) {
        NamedComplexTypeDefinition.optAttributes(elem.resolvedAttributes.toMap).flatMap { attrs =>
          Some(new NamedComplexTypeDefinition(targetNamespaceOption, attrs, childElems))
        }
      } else if (!parentIsSchema && !hasName) {
        AnonymousComplexTypeDefinition.optAttributes(elem.resolvedAttributes.toMap).flatMap { attrs =>
          Some(new AnonymousComplexTypeDefinition(targetNamespaceOption, attrs, childElems))
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object NamedComplexTypeDefinition {

  final case class Attributes(
      idOption: Option[String],
      name: String,
      isAbstract: Boolean,
      otherAttributes: Map[EName, String])
      extends NamedTypeDefinition.Attributes
      with CanBeAbstract.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        AttributeSupport.optAbstract(rawAttributes).get,
        rawAttributes.filter(kv =>
          rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName, ENames.AbstractEName)).contains(kv._1))
      )).toOption
  }
}

object AnonymousComplexTypeDefinition {

  final case class Attributes(idOption: Option[String], otherAttributes: Map[EName, String])
      extends AnonymousTypeDefinition.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName)).contains(kv._1)))).toOption
  }
}

object ModelGroupDefinitionOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ModelGroupDefinitionOrReference

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsGroupEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined
      val hasRef = elem.attributeOption(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        ModelGroupDefinition.optAttributes(elem.resolvedAttributes.toMap).flatMap { attrs =>
          Some(new ModelGroupDefinition(targetNamespaceOption, attrs, childElems))
        }
      } else if (!parentIsSchema && !hasName && hasRef) {
        ModelGroupReference.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new ModelGroupReference(targetNamespaceOption, attrs, childElems))
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object ModelGroupDefinition {

  final case class Attributes(idOption: Option[String], name: String, otherAttributes: Map[EName, String])
      extends NamedDeclOrDef.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName)).contains(kv._1))
      )).toOption
  }
}

object ModelGroupReference {

  final case class Attributes(
      idOption: Option[String],
      ref: EName,
      minOccurs: Int,
      maxOccursOption: Option[Int],
      otherAttributes: Map[EName, String])
      extends Reference.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    val maxOccursOption: Option[String] = AttributeSupport.optMaxOccurs(rawAttributes)

    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optRef(rawAttributes, scope).get,
        AttributeSupport.optMinOccurs(rawAttributes).getOrElse(1),
        if (maxOccursOption.isEmpty) Some(1)
        else if (maxOccursOption.contains("unbounded")) None
        else maxOccursOption.map(_.toInt),
        rawAttributes.filter(
          kv =>
            rawAttributes.keySet
              .diff(Set(ENames.IdEName, ENames.RefEName, ENames.MinOccursEName, ENames.MaxOccursEName))
              .contains(kv._1))
      )).toOption
  }
}

object AttributeGroupDefinitionOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AttributeGroupDefinitionOrReference

  def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsAttributeGroupEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributeOption(ENames.NameEName).isDefined
      val hasRef = elem.attributeOption(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        AttributeGroupDefinition.optAttributes(elem.resolvedAttributes.toMap).flatMap { attrs =>
          Some(new AttributeGroupDefinition(targetNamespaceOption, attrs, childElems))
        }
      } else if (!parentIsSchema && !hasName && hasRef) {
        AttributeGroupReference.optAttributes(elem.resolvedAttributes.toMap, elem.scope).flatMap { attrs =>
          Some(new AttributeGroupReference(targetNamespaceOption, attrs, childElems))
        }
      } else {
        None
      }
    } else {
      None
    }
  }
}

object AttributeGroupDefinition {

  final case class Attributes(idOption: Option[String], name: String, otherAttributes: Map[EName, String])
      extends NamedDeclOrDef.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optName(rawAttributes).get,
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.NameEName)).contains(kv._1))
      )).toOption
  }
}

object AttributeGroupReference {

  final case class Attributes(idOption: Option[String], ref: EName, otherAttributes: Map[EName, String])
      extends Reference.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optRef(rawAttributes, scope).get,
        rawAttributes.filter(kv => rawAttributes.keySet.diff(Set(ENames.IdEName, ENames.RefEName)).contains(kv._1))
      )).toOption
  }
}

object SequenceModelGroup {

  final case class Attributes(
      idOption: Option[String],
      minOccurs: Int,
      maxOccursOption: Option[Int],
      otherAttributes: Map[EName, String])
      extends SchemaContentElement.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    val maxOccursOption: Option[String] = AttributeSupport.optMaxOccurs(rawAttributes)

    Some(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optMinOccurs(rawAttributes).getOrElse(1),
        if (maxOccursOption.isEmpty) Some(1)
        else if (maxOccursOption.contains("unbounded")) None
        else maxOccursOption.map(_.toInt),
        rawAttributes.filter {
          case (name, _) => name != ENames.IdEName && name != ENames.MinOccursEName && name != ENames.MaxOccursEName
        }
      ))
  }
}

object ChoiceModelGroup {

  final case class Attributes(
      idOption: Option[String],
      minOccurs: Int,
      maxOccursOption: Option[Int],
      otherAttributes: Map[EName, String])
      extends SchemaContentElement.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    val maxOccursOption: Option[String] = AttributeSupport.optMaxOccurs(rawAttributes)

    Some(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optMinOccurs(rawAttributes).getOrElse(1),
        if (maxOccursOption.isEmpty) Some(1)
        else if (maxOccursOption.contains("unbounded")) None
        else maxOccursOption.map(_.toInt),
        rawAttributes.filter {
          case (name, _) => name != ENames.IdEName && name != ENames.MinOccursEName && name != ENames.MaxOccursEName
        }
      ))
  }
}

object AllModelGroup {

  final case class Attributes(
      idOption: Option[String],
      minOccurs: Int,
      maxOccursOption: Option[Int],
      otherAttributes: Map[EName, String])
      extends SchemaContentElement.Attributes

  def optAttributes(rawAttributes: Map[EName, String]): Option[Attributes] = {
    val maxOccursOption: Option[String] = AttributeSupport.optMaxOccurs(rawAttributes)

    Some(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optMinOccurs(rawAttributes).getOrElse(1),
        if (maxOccursOption.isEmpty) Some(1)
        else if (maxOccursOption.contains("unbounded")) None
        else maxOccursOption.map(_.toInt),
        rawAttributes.filter {
          case (name, _) => name != ENames.IdEName && name != ENames.MinOccursEName && name != ENames.MaxOccursEName
        }
      ))
  }
}

object RestrictionOrExtension {

  trait Attributes extends SchemaContentElement.Attributes {
    def baseTypeOption: Option[EName]
  }
}

object Restriction {

  final case class Attributes(
      idOption: Option[String],
      baseTypeOption: Option[EName],
      otherAttributes: Map[EName, String])
      extends RestrictionOrExtension.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optBaseType(rawAttributes, scope),
        rawAttributes.filter { case (name, _) => name != ENames.IdEName && name != ENames.BaseEName }
      )).toOption
  }
}

object Extension {

  final case class Attributes(
      idOption: Option[String],
      baseTypeOption: Option[EName],
      otherAttributes: Map[EName, String])
      extends RestrictionOrExtension.Attributes

  def optAttributes(rawAttributes: Map[EName, String], scope: Scope): Option[Attributes] = {
    Try(
      Attributes(
        AttributeSupport.optId(rawAttributes),
        AttributeSupport.optBaseType(rawAttributes, scope),
        rawAttributes.filter { case (name, _) => name != ENames.IdEName && name != ENames.BaseEName }
      )).toOption
  }
}

object OtherStandardSchemaContentElement {

  def apply(
      elem: ScopedNodes.Elem,
      targetNamespaceOption: Option[String],
      children: immutable.IndexedSeq[SchemaContentElement]): OtherStandardSchemaContentElement = {

    val attrs: SchemaContentElement.Attributes = SchemaContentElement.attributes(elem.resolvedAttributes.toMap)

    OtherStandardSchemaContentElement(targetNamespaceOption, elem.resolvedName, attrs, children, elem.text)
  }
}
