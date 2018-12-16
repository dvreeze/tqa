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

package eu.cdevreeze.tqa.base.modelbuilder

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.base

/**
 * Converter from a BasicTaxonomy in the base taxonomy package to a BasicTaxonomy in the base model taxonomy
 * package.
 */
object TaxonomyConverter {

  def convertTaxonomy(taxo: base.taxonomy.BasicTaxonomy): base.model.taxonomy.BasicTaxonomy = {
    val schemaContentElems =
      taxo.rootElems.filter(_.asInstanceOf[base.dom.TaxonomyRootElem].isXsdSchema)
        .map(_.backingElem)
        .flatMap(base.model.SchemaContentElement.collectSchemaContent _)

    val relationships = taxo.relationships.flatMap(optionallyConvertRelationship _)

    base.model.taxonomy.BasicTaxonomy.build(
      schemaContentElems,
      taxo.extraSubstitutionGroupMap,
      relationships)
  }

  private def optionallyConvertRelationship(relationship: base.relationship.Relationship): Option[base.model.Relationship] = {
    base.model.Relationship.opt(
      relationship.baseSetKey,
      convertTaxonomyElemToNode(relationship.sourceElem, relationship.elr),
      convertTaxonomyElemToNode(relationship.targetElem, relationship.elr),
      relationship.arc.resolvedAttributes.toMap
        .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)))
  }

  private def convertTaxonomyElemToNode(elem: base.dom.TaxonomyElem, elr: String): base.model.Node = {
    elem match {
      case e: base.dom.GlobalElementDeclaration => base.model.Node.GlobalElementDecl(e.targetEName)
      case e: base.dom.RoleType => base.model.Node.RoleType(e.roleUri)
      case e: base.dom.ArcroleType => base.model.Node.ArcroleType(e.arcroleUri)
      case e: base.dom.NamedTypeDefinition => base.model.Node.NamedTypeDef(e.targetEName)
      case e: base.dom.XLinkResource => convertXLinkResourceToNode(e, elr)
      case e =>
        // TODO Is this a correct catch-all case?

        base.model.Node.OtherLocatorNode(
          base.model.SchemaContentElementKey(
            e.backingElem.rootElem.attributeOption(ENames.TargetNamespaceEName),
            e.resolvedName,
            e.attribute(ENames.IdEName)))
    }
  }

  private def convertXLinkResourceToNode(elem: base.dom.XLinkResource, elr: String): base.model.Node = {
    elem match {
      case e: base.dom.ConceptLabelResource =>
        base.model.Node.ConceptLabelResource(
          e.attributeOption(ENames.IdEName),
          elr,
          e.roleOption,
          e.resolvedAttributes.toMap
            .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
          e.text)
      case e: base.dom.ConceptReferenceResource =>
        base.model.Node.ConceptReferenceResource(
          e.attributeOption(ENames.IdEName),
          elr,
          e.roleOption,
          e.resolvedAttributes.toMap
            .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
          e.findAllChildElems.map(e => (e.resolvedName, e.text)).toMap)
      case e: base.dom.XLinkResource if e.resolvedName == ENames.LabelLabelEName =>
        base.model.Node.ElementLabelResource(
          e.attributeOption(ENames.IdEName),
          elr,
          e.roleOption,
          e.resolvedAttributes.toMap
            .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
          e.text)
      case e: base.dom.XLinkResource if e.resolvedName == ENames.ReferenceReferenceEName =>
        base.model.Node.ElementReferenceResource(
          e.attributeOption(ENames.IdEName),
          elr,
          e.roleOption,
          e.resolvedAttributes.toMap
            .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
          e.findAllChildElems.map(e => (e.resolvedName, e.text)).toMap)
      case e =>
        base.model.Node.OtherResourceNode(
          e.attributeOption(ENames.IdEName),
          elr,
          e.roleOption,
          e.resolvedAttributes.toMap
            .filterKeys(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)))
    }
  }
}
