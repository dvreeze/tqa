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

import eu.cdevreeze.tqa.base.relationship.StandardInterConceptRelationship
import eu.cdevreeze.tqa.base.relationship.InterConceptRelationshipPath
import eu.cdevreeze.yaidom.core.EName

/**
 * Inter-concept relationship path in the context of concept relationship node resolution.
 *
 * @author Chris de Vreeze
 */
sealed trait ConceptRelationshipNodePath {

  /**
   * Returns the relationships of the path, which may be an empty collection
   */
  def relationships: immutable.IndexedSeq[StandardInterConceptRelationship]

  /**
   * Returns the source concept of the path
   */
  def sourceConcept: EName

  /**
   * Returns the target concept of the path, which is the same as the source concept for single concept paths
   */
  def targetConcept: EName

  /**
   * Returns the source concept of single concept paths and descendant-or-self paths wrapped in an Option, and None otherwise
   */
  def selfConceptOption: Option[EName]

  /**
   * Returns the source concept, target concept and all concepts in between
   */
  def concepts: immutable.IndexedSeq[EName]
}

object ConceptRelationshipNodePath {

  final case class SingleConceptPath(rootConcept: EName) extends ConceptRelationshipNodePath {

    def relationships: immutable.IndexedSeq[StandardInterConceptRelationship] = immutable.IndexedSeq()

    def sourceConcept: EName = rootConcept

    def targetConcept: EName = rootConcept

    def selfConceptOption: Option[EName] = Some(rootConcept)

    def concepts: immutable.IndexedSeq[EName] = immutable.IndexedSeq(rootConcept)
  }

  final case class DescendantPath(
    interConceptRelationshipPath: InterConceptRelationshipPath[StandardInterConceptRelationship]) extends ConceptRelationshipNodePath {

    def relationships: immutable.IndexedSeq[StandardInterConceptRelationship] = interConceptRelationshipPath.relationships

    def sourceConcept: EName = interConceptRelationshipPath.firstRelationship.targetConceptEName

    def targetConcept: EName = interConceptRelationshipPath.targetConcept

    def selfConceptOption: Option[EName] = None

    def concepts: immutable.IndexedSeq[EName] = {
      interConceptRelationshipPath.relationships.map(_.targetConceptEName)
    }
  }

  final case class DescendantOrSelfPath(
    interConceptRelationshipPath: InterConceptRelationshipPath[StandardInterConceptRelationship]) extends ConceptRelationshipNodePath {

    def relationships: immutable.IndexedSeq[StandardInterConceptRelationship] = interConceptRelationshipPath.relationships

    def sourceConcept: EName = interConceptRelationshipPath.sourceConcept

    def targetConcept: EName = interConceptRelationshipPath.targetConcept

    def selfConceptOption: Option[EName] = Some(sourceConcept)

    def concepts: immutable.IndexedSeq[EName] = {
      sourceConcept +: interConceptRelationshipPath.relationships.map(_.targetConceptEName)
    }
  }
}
