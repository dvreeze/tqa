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

import eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa.base.relationship.ConceptReferenceRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingElemNodeApi

/**
 * Taxonomy relationship creation API.
 *
 * @author Chris de Vreeze
 */
trait RelationshipCreator {

  def currentTaxonomy: BasicTaxonomy

  def createConceptLabelRelationship(
    elr:                String,
    concept:            EName,
    otherArcAttributes: Map[EName, String],
    resourceRole:       String,
    languageOption:     Option[String],
    labelText:          String,
    scope:              Scope): ConceptLabelRelationship

  def createConceptReferenceRelationship(
    elr:                String,
    concept:            EName,
    otherArcAttributes: Map[EName, String],
    resourceRole:       String,
    rawReferenceElem:   BackingElemNodeApi,
    scope:              Scope): ConceptReferenceRelationship

  def createInterConceptRelationship(
    elr:                String,
    sourceConcept:      EName,
    targetConcept:      EName,
    arcrole:            String,
    otherArcAttributes: Map[EName, String],
    scope:              Scope): InterConceptRelationship

  // TODO Support for far more kinds of relationships
}
