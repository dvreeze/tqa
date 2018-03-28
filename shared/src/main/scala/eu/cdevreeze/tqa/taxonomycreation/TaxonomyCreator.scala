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

package eu.cdevreeze.tqa.taxonomycreation

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa.base.relationship.ConceptReferenceRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy

/**
 * Taxonomy creation API, containing expensive bulk operations to grow a taxonomy.
 *
 * Typically the intermediate taxonomy results are not valid XBRL, and may not be a closed DTS.
 *
 * Creation must start with a taxonomy containing the required schema and linkbase documents, even if
 * they are practically empty at the beginning.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyCreator {

  def currentTaxonomy: BasicTaxonomy

  def addConceptDeclarations(
    docUri:              URI,
    conceptDeclarations: immutable.IndexedSeq[ConceptDeclaration]): TaxonomyCreator

  def addGlobalElementDeclarations(
    docUri:              URI,
    elementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration]): TaxonomyCreator

  def addConceptLabelRelationships(
    docUri:                    URI,
    conceptLabelRelationships: immutable.IndexedSeq[ConceptLabelRelationship]): TaxonomyCreator

  def addConceptReferenceRelationships(
    docUri:                        URI,
    conceptReferenceRelationships: immutable.IndexedSeq[ConceptReferenceRelationship]): TaxonomyCreator

  def addInterConceptRelationships(
    docUri:                    URI,
    interConceptRelationships: immutable.IndexedSeq[InterConceptRelationship]): TaxonomyCreator

  // TODO Support for far more taxonomy elements and kinds of relationships

  def postProcess(): TaxonomyCreator
}
