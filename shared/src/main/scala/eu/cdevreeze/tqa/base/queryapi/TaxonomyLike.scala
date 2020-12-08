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

package eu.cdevreeze.tqa.base.queryapi

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.dom.ItemDeclaration
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of `TaxonomyApi`, formed by combining partial implementations of other
 * query API traits.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyLike
    extends TaxonomyApi
    with TaxonomySchemaLike
    with RelationshipContainerLike
    with StandardRelationshipContainerLike
    with NonStandardRelationshipContainerLike
    with StandardInterConceptRelationshipContainerLike
    with PresentationRelationshipContainerLike
    with ConceptLabelRelationshipContainerLike
    with ConceptReferenceRelationshipContainerLike
    with ElementLabelRelationshipContainerLike
    with ElementReferenceRelationshipContainerLike
    with DimensionalRelationshipContainerLike
    with InterConceptRelationshipContainerLike {

  // Enumeration 2.0

  final def findAllEnumerationValues(enumerationConcept: EName): Set[EName] = {
    val itemDecl: ItemDeclaration = getItemDeclaration(enumerationConcept)
    val elemDecl = itemDecl.globalElementDeclaration

    val domain: EName = elemDecl.attributeAsResolvedQName(ENames.Enum2DomainEName)
    val elr: String = elemDecl.attribute(ENames.Enum2LinkroleEName)

    findAllMembers(domain, elr)
  }

  final def findAllAllowedEnumerationValues(enumerationConcept: EName): Set[EName] = {
    val itemDecl: ItemDeclaration = getItemDeclaration(enumerationConcept)
    val elemDecl = itemDecl.globalElementDeclaration

    val domain: EName = elemDecl.attributeAsResolvedQName(ENames.Enum2DomainEName)
    val elr: String = elemDecl.attribute(ENames.Enum2LinkroleEName)
    val headUsable: Boolean =
      elemDecl.attributeOption(ENames.Enum2HeadUsableEName).exists(XsdBooleans.parseBoolean)

    findAllUsableMembers(domain, elr, headUsable)
  }
}
