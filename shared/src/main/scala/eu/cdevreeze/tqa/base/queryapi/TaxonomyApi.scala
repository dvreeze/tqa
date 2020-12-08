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

import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.yaidom.core.EName

import scala.collection.immutable

/**
 * Purely abstract trait offering a '''taxonomy query API'''. It combines several other purely abstract query
 * API traits. The query API concerns the taxonomy as taxonomy schema, and as container of relationships,
 * standard relationships, inter-concept relationships and dimensional relationships.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyApi
    extends TaxonomySchemaApi
    with RelationshipContainerApi
    with StandardRelationshipContainerApi
    with NonStandardRelationshipContainerApi
    with StandardInterConceptRelationshipContainerApi
    with PresentationRelationshipContainerApi
    with ConceptLabelRelationshipContainerApi
    with ConceptReferenceRelationshipContainerApi
    with ElementLabelRelationshipContainerApi
    with ElementReferenceRelationshipContainerApi
    with DimensionalRelationshipContainerApi
    with InterConceptRelationshipContainerApi {

  /**
   * Returns all taxonomy documents.
   */
  def taxonomyDocs: immutable.IndexedSeq[TaxonomyDocument]

  /**
   * Returns all (document) root elements. To find certain taxonomy elements across the taxonomy,
   * in taxonomy schemas and linkbases, the following pattern can be used:
   * {{{
   * rootElems.flatMap(_.filterElemsOrSelfOfType(classTag[E])(pred))
   * }}}
   */
  def rootElems: immutable.IndexedSeq[TaxonomyElem]

  /**
   * Returns the linkbase elements, which are typically top-level, but may be embedded in schemas.
   */
  def findAllLinkbases: immutable.IndexedSeq[Linkbase]

  // Enumeration 2.0

  /**
   * Finds all enumeration values in the domain of the given enumeration concept.
   * If attributes enum2:domain and/or enum2:linkrole are missing, an exception is thrown.
   *
   * This function invokes the following function call:
   * {{{
   * findAllMembers(domain, elr)
   * }}}
   * where domain and elr are taken from the above-mentioned attributes.
   */
  def findAllEnumerationValues(enumerationConcept: EName): Set[EName]

  /**
   * Finds all allowed enumeration values in the domain of the given enumeration concept.
   * If attributes enum2:domain and/or enum2:linkrole are missing, an exception is thrown.
   *
   * This function invokes the following function call:
   * {{{
   * findAllUsableMembers(domain, elr, headUsable)
   * }}}
   * where domain and elr are taken from the above-mentioned attributes, as well as optional
   * attribute enum2:headUsable (which defaults to false).
   */
  def findAllAllowedEnumerationValues(enumerationConcept: EName): Set[EName]
}
