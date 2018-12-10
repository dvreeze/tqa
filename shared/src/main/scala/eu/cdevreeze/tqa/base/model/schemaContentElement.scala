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

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.base.common.Variety
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemApi
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem

/**
 * Any '''taxonomy schema content element''' in the model. It is either standard schema content, which is a representation
 * of an element in the XML Schema namespace, or it is appinfo content. The schema root element, and imports and
 * linkbaseRefs are not represented in this model. Neither are the annotation and appinfo elements.
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
 * Schema content elements are easy to create on the fly, and could be part of the basis for easy programmatic taxonomy
 * creation. Note, however, that the taxonomy creation algorithm must at some point worry about closure with
 * respect to DTS discovery, and about containing the needed substitution group ancestry.
 *
 * @author Chris de Vreeze
 */
sealed trait SchemaContentElement extends SubtypeAwareElemApi with SubtypeAwareElemLike {

  type ThisElem = SchemaContentElement

  /**
   * Returns the underlying SchemaContentBackingElem
   */
  def elem: SchemaContentBackingElem

  final def docUri: URI = elem.docUri

  /**
   * The resolved name of the corresponding XML element.
   */
  final def resolvedName: EName = elem.resolvedName

  /**
   * The attributes in the corresponding XML element, but with QName values replaced by ENames in James Clark notation.
   */
  final def attributes: Map[EName, String] = elem.attributes

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
 * in the XML Schema namespace, or any of its desdendants.
 */
sealed trait StandardSchemaContentElement extends SchemaContentElement {

  final def targetNamespaceOption: Option[String] = elem.targetNamespaceOption
}

/**
 * Representation of an element in an xs:appinfo element (as descendant).
 */
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
sealed trait ElementDeclarationOrReference extends StandardSchemaContentElement

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
final case class GlobalElementDeclaration private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclaration with CanBeAbstract {

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
final case class LocalElementDeclaration private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclaration with Particle

/**
 * Element reference, referring to a global element declaration. Like local element declarations it is not a child element of
 * the xs:schema root element, but unlike global and local element declarations it has a ref attribute instead of a name attribute.
 */
final case class ElementReference private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ElementDeclarationOrReference with Reference

// Attribute declarations or references.

/**
 * Either an attribute declaration or an attribute reference.
 */
sealed trait AttributeDeclarationOrReference extends StandardSchemaContentElement

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
final case class GlobalAttributeDeclaration private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclaration {

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
final case class LocalAttributeDeclaration private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclaration

/**
 * Attribute reference. It is an xs:attribute element referring to a global attribute declaration. It is not a direct child element of
 * the xs:schema root element.
 */
final case class AttributeReference private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AttributeDeclarationOrReference with Reference

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
final case class NamedSimpleTypeDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends NamedTypeDefinition with SimpleTypeDefinition

/**
 * Anonymous simple type definition. It is a non-top-level xs:simpleType element without any name attribute.
 */
final case class AnonymousSimpleTypeDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AnonymousTypeDefinition with SimpleTypeDefinition

/**
 * Named complex type definition. It is a top-level xs:complexType element with a name attribute.
 */
final case class NamedComplexTypeDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends NamedTypeDefinition with ComplexTypeDefinition

/**
 * Anonymous complex type definition. It is a non-top-level xs:complexType element without any name attribute.
 */
final case class AnonymousComplexTypeDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AnonymousTypeDefinition with ComplexTypeDefinition

// Attribute group definitions and references.

/**
 * Attribute group definition or attribute group reference.
 */
sealed trait AttributeGroupDefinitionOrReference extends StandardSchemaContentElement

/**
 * Attribute group definition, so a top-level xs:attributeGroup element with a name attribute.
 */
final case class AttributeGroupDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AttributeGroupDefinitionOrReference with NamedDeclOrDef

/**
 * Attribute group reference, so a non-top-level xs:attributeGroup element with a ref attribute, referring to an attribute group definition.
 */
final case class AttributeGroupReference private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AttributeGroupDefinitionOrReference with Reference

// Model group definitions and references.

/**
 * Model group definition or model group reference.
 */
sealed trait ModelGroupDefinitionOrReference extends StandardSchemaContentElement

/**
 * Model group definition, so a top-level xs:group element with a name attribute.
 */
final case class ModelGroupDefinition private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroupDefinitionOrReference

/**
 * Model group reference, so a non-top-level xs:group element with a ref attribute, referring to a model group definition.
 */
final case class ModelGroupReference private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroupDefinitionOrReference with Reference

// Ignoring identity constraints, notations, wildcards.

/**
 * Model group, so either a sequence, choice or all model group.
 */
sealed trait ModelGroup extends StandardSchemaContentElement

/**
 * Sequence model group, so an xs:sequence element.
 */
final case class SequenceModelGroup private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup

/**
 * Choice model group, so an xs:choice element.
 */
final case class ChoiceModelGroup private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup

/**
 * All model group, so an xs:all element.
 */
final case class AllModelGroup private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends ModelGroup

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
final case class Restriction private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends RestrictionOrExtension

/**
 * An xs:extension element.
 */
final case class Extension private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends RestrictionOrExtension

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
final case class SimpleContent private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends Content

/**
 * An xs:complexContent element.
 */
final case class ComplexContent private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends Content

// TODO Sub-types of AppinfoContentElement, like role types and arcrole types.

/**
 * Any `StandardSchemaContentElement` not recognized as an instance of one of the other concrete `StandardSchemaContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `StandardSchemaContentElement` sub-type hierarchy, or it is
 * syntactically incorrect. As an example of the latter, an xs:element XML element with both a name and a ref attribute is clearly invalid.
 */
final case class OtherStandardSchemaContentElement private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends StandardSchemaContentElement

/**
 * Any `AppinfoContentElement` not recognized as an instance of one of the other concrete `AppinfoContentElement` sub-types.
 * This means that either this is valid schema content not modeled in the `AppinfoContentElement` sub-type hierarchy, or it is
 * syntactically incorrect.
 */
final case class OtherAppinfoContentElement private[model] (
  elem: SchemaContentBackingElem,
  childElems: immutable.IndexedSeq[SchemaContentElement]) extends AppinfoContentElement

// Companion objects

object SchemaContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = SchemaContentElement

  def collectSchemaContent(rootElem: SchemaContentBackingElem): immutable.IndexedSeq[SchemaContentElement] = {
    require(rootElem.resolvedName == ENames.XsSchemaEName, s"Expected ${ENames.XsSchemaEName} but got ${rootElem.resolvedName}")

    val appinfoChildren =
      rootElem.findTopmostElems(_.resolvedName == ENames.XsAppinfoEName).flatMap(_.findAllChildElems)

    val appinfoAncestorENames = immutable.IndexedSeq(ENames.XsSchemaEName, ENames.XsAnnotationEName, ENames.XsAppinfoEName)

    val appinfoContent = appinfoChildren.flatMap(e => AppinfoContentElement.optionallyBuild(e, appinfoAncestorENames))

    val topLevelChildren =
      rootElem.filterChildElems { e =>
        e.resolvedName.namespaceUriOption.contains(Namespaces.XsNamespace) &&
          !Set(ENames.XsImportEName, ENames.XsIncludeEName, ENames.XsAnnotationEName).contains(e.resolvedName)
      }

    val topLevelAncestorENames = immutable.IndexedSeq(ENames.XsSchemaEName)

    val topLevelContent = topLevelChildren.flatMap(e => StandardSchemaContentElement.optionallyBuild(e, topLevelAncestorENames))

    appinfoContent ++ topLevelContent
  }

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (ancestorENames.contains(ENames.XsAppinfoEName)) {
      AppinfoContentElement.opt(elem, ancestorENames, childElems)
    } else {
      StandardSchemaContentElement.opt(elem, ancestorENames, childElems)
    }
  }
}

object StandardSchemaContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = StandardSchemaContentElement

  def optionallyBuild(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName]): Option[SchemaContentElementType] = {

    // Recursive calls
    val childElems = elem.findAllChildElems.flatMap(che => optionallyBuild(che, elem.resolvedName +: ancestorENames))

    opt(elem, ancestorENames, childElems)
  }

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (ancestorENames.contains(ENames.XsAppinfoEName)) {
      None
    } else {
      elem.resolvedName match {
        case ENames.XsSchemaEName | ENames.XsAnnotationEName | ENames.XsAppinfoEName | ENames.XsDocumentationEName |
          ENames.XsImportEName | ENames.XsIncludeEName =>
          None
        case EName(nsOption, _) if !nsOption.contains(Namespaces.XsNamespace) =>
          None
        case ENames.XsElementEName =>
          ElementDeclarationOrReference.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsAttributeEName =>
          AttributeDeclarationOrReference.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsSimpleTypeEName =>
          SimpleTypeDefinition.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsComplexTypeEName =>
          ComplexTypeDefinition.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsGroupEName =>
          ModelGroupDefinitionOrReference.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsAttributeGroupEName =>
          AttributeGroupDefinitionOrReference.opt(elem, ancestorENames, childElems)
            .orElse(Some(new OtherStandardSchemaContentElement(elem, childElems)))
        case ENames.XsSequenceEName => Some(new SequenceModelGroup(elem, childElems))
        case ENames.XsChoiceEName => Some(new ChoiceModelGroup(elem, childElems))
        case ENames.XsAllEName => Some(new AllModelGroup(elem, childElems))
        case ENames.XsRestrictionEName => Some(new Restriction(elem, childElems))
        case ENames.XsExtensionEName => Some(new Extension(elem, childElems))
        case ENames.XsSimpleContentEName => Some(new SimpleContent(elem, childElems))
        case ENames.XsComplexContentEName => Some(new ComplexContent(elem, childElems))
        case _ => Some(new OtherStandardSchemaContentElement(elem, childElems))
      }
    }
  }
}

object AppinfoContentElement extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AppinfoContentElement

  def optionallyBuild(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName]): Option[SchemaContentElementType] = {

    // Recursive calls
    val childElems = elem.findAllChildElems.flatMap(che => optionallyBuild(che, elem.resolvedName +: ancestorENames))

    opt(elem, ancestorENames, childElems)
  }

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (!ancestorENames.contains(ENames.XsAppinfoEName)) {
      None
    } else {
      // TODO
      Some(new OtherAppinfoContentElement(elem, childElems))
    }
  }
}

object ElementDeclarationOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ElementDeclarationOrReference

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsElementEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined
      val hasRef = elem.attributes.get(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        Some(new GlobalElementDeclaration(elem, childElems))
      } else if (!parentIsSchema && hasName && !hasRef) {
        Some(new LocalElementDeclaration(elem, childElems))
      } else if (!parentIsSchema && !hasName && hasRef) {
        Some(new ElementReference(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}

object AttributeDeclarationOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AttributeDeclarationOrReference

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsAttributeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined
      val hasRef = elem.attributes.get(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        Some(new GlobalAttributeDeclaration(elem, childElems))
      } else if (!parentIsSchema && hasName && !hasRef) {
        Some(new LocalAttributeDeclaration(elem, childElems))
      } else if (!parentIsSchema && !hasName && hasRef) {
        Some(new AttributeReference(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}

object SimpleTypeDefinition extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = SimpleTypeDefinition

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsSimpleTypeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined

      if (parentIsSchema && hasName) {
        Some(new NamedSimpleTypeDefinition(elem, childElems))
      } else if (!parentIsSchema && !hasName) {
        Some(new AnonymousSimpleTypeDefinition(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}

object ComplexTypeDefinition extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ComplexTypeDefinition

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsComplexTypeEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined

      if (parentIsSchema && hasName) {
        Some(new NamedComplexTypeDefinition(elem, childElems))
      } else if (!parentIsSchema && !hasName) {
        Some(new AnonymousComplexTypeDefinition(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}

object ModelGroupDefinitionOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = ModelGroupDefinitionOrReference

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsGroupEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined
      val hasRef = elem.attributes.get(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        Some(new ModelGroupDefinition(elem, childElems))
      } else if (!parentIsSchema && !hasName && hasRef) {
        Some(new ModelGroupReference(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}

object AttributeGroupDefinitionOrReference extends SchemaContentElements.Factory {

  type SchemaContentElementSuperType = SchemaContentElement
  type SchemaContentElementType = AttributeGroupDefinitionOrReference

  def opt(
    elem: SchemaContentBackingElem,
    ancestorENames: immutable.IndexedSeq[EName],
    childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType] = {

    if (elem.resolvedName == ENames.XsAttributeGroupEName) {
      val parentIsSchema = ancestorENames.headOption.contains(ENames.XsSchemaEName)
      val hasName = elem.attributes.get(ENames.NameEName).isDefined
      val hasRef = elem.attributes.get(ENames.RefEName).isDefined

      if (parentIsSchema && hasName && !hasRef) {
        Some(new AttributeGroupDefinition(elem, childElems))
      } else if (!parentIsSchema && !hasName && hasRef) {
        Some(new AttributeGroupReference(elem, childElems))
      } else {
        None
      }
    } else {
      None
    }
  }
}
