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

package eu.cdevreeze.tqa.xpathaware.extension.table

import scala.collection.immutable

import eu.cdevreeze.tqa.base.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.yaidom.core.EName

/**
 * Inter-concept relationship path in the context of concept relationship node resolution.
 *
 * @author Chris de Vreeze
 */
final case class ConceptRelationshipNodePath(
  startConceptOption: Option[EName],
  interConceptRelationshipPathOption: Option[InterConceptRelationshipPath[InterConceptRelationship]]) {

  require(
    startConceptOption.nonEmpty || interConceptRelationshipPathOption.nonEmpty,
    s"Both the start concept and the interconcept relationship path are absent, which is not allowed")

  /**
   * Returns the relationships of the path, which may be an empty collection
   */
  def relationships: immutable.IndexedSeq[InterConceptRelationship] = {
    interConceptRelationshipPathOption.map(_.relationships).getOrElse(immutable.IndexedSeq())
  }

  /**
   * Returns the source concept of the path
   */
  def sourceConcept: EName = {
    if (startConceptOption.isEmpty) {
      interConceptRelationshipPathOption.get.sourceConcept
    } else {
      startConceptOption.get
    }
  }

  /**
   * Returns the target concept of the path, which is the same as the source concept for paths without relationships
   */
  def targetConcept: EName = {
    if (interConceptRelationshipPathOption.isEmpty) {
      startConceptOption.get
    } else {
      interConceptRelationshipPathOption.get.targetConcept
    }
  }

  /**
   * Returns the relationship target concepts, if any, preceded by the start concept, if any.
   */
  def relationshipTargetConcepts: immutable.IndexedSeq[EName] = {
    if (startConceptOption.isEmpty) {
      interConceptRelationshipPathOption.get.relationshipTargetConcepts
    } else if (interConceptRelationshipPathOption.isEmpty) {
      immutable.IndexedSeq(startConceptOption.get)
    } else {
      startConceptOption.get +: interConceptRelationshipPathOption.get.relationshipTargetConcepts
    }
  }
}

object ConceptRelationshipNodePath {

  def apply(startConcept: EName): ConceptRelationshipNodePath = {
    ConceptRelationshipNodePath(Some(startConcept), None)
  }

  def apply(interConceptRelationshipPath: InterConceptRelationshipPath[InterConceptRelationship]): ConceptRelationshipNodePath = {
    ConceptRelationshipNodePath(None, Some(interConceptRelationshipPath))
  }

  def apply(
    startConcept: EName,
    interConceptRelationshipPath: InterConceptRelationshipPath[InterConceptRelationship]): ConceptRelationshipNodePath = {

    ConceptRelationshipNodePath(Some(startConcept), Some(interConceptRelationshipPath))
  }
}
