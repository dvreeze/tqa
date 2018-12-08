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

import scala.collection.immutable

import eu.cdevreeze.tqa.base.model.Relationship

/**
 * Purely abstract trait offering a '''taxonomy query API'''. It combines several other purely abstract query
 * API traits. The query API concerns the taxonomy as taxonomy schema, and as container of relationships,
 * standard relationships, inter-concept relationships and dimensional relationships.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyApi
  extends TaxonomySchemaApi
  with StandardRelationshipContainerApi
  with NonStandardRelationshipContainerApi
  with InterConceptRelationshipContainerApi
  with PresentationRelationshipContainerApi
  with ConceptLabelRelationshipContainerApi
  with ConceptReferenceRelationshipContainerApi
  with ElementLabelRelationshipContainerApi
  with ElementReferenceRelationshipContainerApi
  with DimensionalRelationshipContainerApi {

  /**
   * Returns all relationships in the taxonomy
   */
  def relationships: immutable.IndexedSeq[Relationship]
}
