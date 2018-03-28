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

import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.core.EName

/**
 * Taxonomy element creation API.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyElemCreator {

  def currentTaxonomy: BasicTaxonomy

  def createConceptDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String]): ConceptDeclaration

  def createGlobalElementDeclaration(
    targetEName:             EName,
    typeOption:              Option[EName],
    substitutionGroupOption: Option[EName],
    otherAttributes:         Map[EName, String]): GlobalElementDeclaration

  // TODO Support for far more taxonomy elements
}
