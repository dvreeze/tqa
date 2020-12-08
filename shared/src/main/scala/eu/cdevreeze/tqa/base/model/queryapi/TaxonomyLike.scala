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

package eu.cdevreeze.tqa.base.model.queryapi

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

  // TODO Enumeration 2.0 query methods findAllEnumerationValues and findAllAllowedEnumerationValues
}
