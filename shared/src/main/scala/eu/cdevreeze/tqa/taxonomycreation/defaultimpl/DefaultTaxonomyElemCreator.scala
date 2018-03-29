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

package eu.cdevreeze.tqa.taxonomycreation.defaultimpl

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.base.dom.AnonymousComplexTypeDefinition
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.dom.XsdElem
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.taxonomycreation.TaxonomyElemCreator
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.utils.ResolvedElemEditor

/**
 * Default taxonomy element creation API implementation.
 *
 * @author Chris de Vreeze
 */
final class DefaultTaxonomyElemCreator(
  val currentTaxonomy: BasicTaxonomy) extends TaxonomyElemCreator {

  private val conceptDeclBuilder = new ConceptDeclaration.Builder(currentTaxonomy.substitutionGroupMap)

  def createConceptDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String],
    scope:                   Scope): ConceptDeclaration = {

    val elemDecl =
      createGlobalElementDeclaration(targetEName, typeOption, substitutionGroupOption, otherAttributes, scope)

    conceptDeclBuilder.optConceptDeclaration(elemDecl).getOrElse(sys.error(s"Not a concept declaration: ${elemDecl.key}"))
  }

  def createGlobalElementDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String],
    scope:                   Scope): GlobalElementDeclaration = {

    val effectiveScope = scope.withoutDefaultNamespace ++ Scope.from("xs" -> Namespaces.XsNamespace)

    val usedENames =
      List(Some(targetEName), typeOption, substitutionGroupOption).flatten.toSet.union(otherAttributes.keySet)

    val usedNamespaces = usedENames.flatMap(_.namespaceUriOption)
    val unknownNamespaces = usedNamespaces.filter(ns => effectiveScope.prefixesForNamespace(ns).isEmpty)

    require(
      unknownNamespaces.isEmpty,
      s"Missing prefixes for namespace(s) ${unknownNamespaces.mkString(", ")}")

    import eu.cdevreeze.yaidom.resolved.Node._

    val elemDeclResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsElementEName, otherAttributes))
        .plusResolvedAttribute(ENames.NameEName, targetEName.localPart)
        .plusResolvedAttributeOption(
          ENames.TypeEName,
          typeOption.map(tp => ENameUtil.attributeENameToQName(tp, effectiveScope).toString))
        .plusResolvedAttributeOption(
          ENames.SubstitutionGroupEName,
          substitutionGroupOption.map(sg => ENameUtil.attributeENameToQName(sg, effectiveScope).toString))
        .toElem

    val schemaResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsSchemaEName))
        .plusResolvedAttributeOption(ENames.TargetNamespaceEName, targetEName.namespaceUriOption)
        .plusChild(elemDeclResolvedElem)
        .toElem

    val schemaRoot =
      TaxonomyElem.build(ResolvedElemUtil.convertToIndexedElem(schemaResolvedElem, effectiveScope))
        .asInstanceOf[XsdElem]

    val elemDecl = schemaRoot.childElems.head.asInstanceOf[GlobalElementDeclaration]

    elemDecl
      .ensuring(_.targetEName == targetEName)
      .ensuring(_.typeOption == typeOption)
      .ensuring(_.substitutionGroupOption == substitutionGroupOption)
      .ensuring(_.resolvedAttributes.toMap.filterKeys(otherAttributes.keySet) == otherAttributes)
  }

  def createConceptDeclarationWithNestedType(
    targetEName:             EName,
    typeDefinition:          AnonymousComplexTypeDefinition,
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String],
    scope:                   Scope): ConceptDeclaration = {

    val elemDecl =
      createGlobalElementDeclarationWithNestedType(targetEName, typeDefinition, substitutionGroupOption, otherAttributes, scope)

    conceptDeclBuilder.optConceptDeclaration(elemDecl).getOrElse(sys.error(s"Not a concept declaration: ${elemDecl.key}"))
  }

  def createGlobalElementDeclarationWithNestedType(
    targetEName:             EName,
    typeDefinition:          AnonymousComplexTypeDefinition,
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String],
    scope:                   Scope): GlobalElementDeclaration = {

    require(
      typeDefinition.schemaTargetNamespaceOption == targetEName.namespaceUriOption,
      s"Deviating optional target namespaces between type and 'target EName': " +
        s"'${typeDefinition.schemaTargetNamespaceOption}' versus '${targetEName.namespaceUriOption}'")

    val effectiveScope = scope.withoutDefaultNamespace ++ Scope.from("xs" -> Namespaces.XsNamespace)

    val usedENames =
      List(Some(targetEName), substitutionGroupOption).flatten.toSet.union(otherAttributes.keySet)

    val usedNamespaces =
      usedENames.flatMap(_.namespaceUriOption)
        .union(SimpleElemUtil.convertToSimpleElem(typeDefinition).scope.withoutDefaultNamespace.namespaces)

    val unknownNamespaces = usedNamespaces.filter(ns => effectiveScope.prefixesForNamespace(ns).isEmpty)

    require(
      unknownNamespaces.isEmpty,
      s"Missing prefixes for namespace(s) ${unknownNamespaces.mkString(", ")}")

    import eu.cdevreeze.yaidom.resolved.Node._

    val elemDeclResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsElementEName, otherAttributes))
        .plusResolvedAttribute(ENames.NameEName, targetEName.localPart)
        .plusResolvedAttributeOption(
          ENames.SubstitutionGroupEName,
          substitutionGroupOption.map(sg => ENameUtil.attributeENameToQName(sg, effectiveScope).toString))
        .plusChild(resolved.Elem(typeDefinition))
        .toElem

    val schemaResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsSchemaEName))
        .plusResolvedAttributeOption(ENames.TargetNamespaceEName, targetEName.namespaceUriOption)
        .plusChild(elemDeclResolvedElem)
        .toElem

    val schemaRoot =
      TaxonomyElem.build(ResolvedElemUtil.convertToIndexedElem(schemaResolvedElem, effectiveScope))
        .asInstanceOf[XsdElem]

    val elemDecl = schemaRoot.childElems.head.asInstanceOf[GlobalElementDeclaration]

    elemDecl
      .ensuring(_.targetEName == targetEName)
      .ensuring(_.substitutionGroupOption == substitutionGroupOption)
      .ensuring(_.resolvedAttributes.toMap.filterKeys(otherAttributes.keySet) == otherAttributes)
  }
}
