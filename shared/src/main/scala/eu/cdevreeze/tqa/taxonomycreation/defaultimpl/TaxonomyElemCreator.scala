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
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.dom.XsdElem
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.taxonomycreation.TaxonomyElemCreator
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.utils.ResolvedElemEditor

/**
 * Default taxonomy element creation API implementation.
 *
 * @author Chris de Vreeze
 */
final class DefaultTaxonomyElemCreator(
  val currentTaxonomy: BasicTaxonomy,
  val scope:           Scope) extends TaxonomyElemCreator {

  private val conceptDeclBuilder = new ConceptDeclaration.Builder(currentTaxonomy.substitutionGroupMap)

  def createConceptDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String]): ConceptDeclaration = {

    val elemDecl = createGlobalElementDeclaration(targetEName, typeOption, substitutionGroupOption, otherAttributes)

    conceptDeclBuilder.optConceptDeclaration(elemDecl).getOrElse(sys.error(s"Not a concept declaration: ${elemDecl.key}"))
  }

  def createGlobalElementDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String]): GlobalElementDeclaration = {

    val tns = targetEName.namespaceUriOption.getOrElse(sys.error(s"Missing namespace in '$targetEName'"))

    import eu.cdevreeze.yaidom.resolved.Node._

    val elemDeclResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsElementEName, otherAttributes))
        .plusResolvedAttribute(ENames.NameEName, targetEName.localPart)
        .plusResolvedAttributeOption(
          ENames.TypeEName,
          typeOption.map(tp => ResolvedElemUtil.attributeENameToQName(tp, scope).toString))
        .plusResolvedAttributeOption(
          ENames.SubstitutionGroupEName,
          substitutionGroupOption.map(sg => ResolvedElemUtil.attributeENameToQName(sg, scope).toString))
        .toElem

    val schemaResolvedElem =
      ResolvedElemEditor.wrap(emptyElem(ENames.XsSchemaEName))
        .plusResolvedAttribute(ENames.TargetNamespaceEName, tns)
        .plusChild(elemDeclResolvedElem)
        .toElem

    val schemaRoot =
      TaxonomyElem.build(ResolvedElemUtil.convertToIndexedElem(schemaResolvedElem, scope))
        .asInstanceOf[XsdElem]

    val elemDecl = schemaRoot.childElems.head.asInstanceOf[GlobalElementDeclaration]

    elemDecl
      .ensuring(_.targetEName == targetEName)
      .ensuring(_.typeOption == typeOption)
      .ensuring(_.substitutionGroupOption == substitutionGroupOption)
      .ensuring(_.resolvedAttributes.toMap.filterKeys(otherAttributes.keySet) == otherAttributes)
  }
}
