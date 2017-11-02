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

package eu.cdevreeze.tqa.extension.formula.queryapi

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.extension.formula.dom.Assertion
import eu.cdevreeze.tqa.extension.formula.dom.FactVariable
import eu.cdevreeze.tqa.extension.formula.dom.VariableSet
import eu.cdevreeze.tqa.extension.formula.relationship.AssertionMessageRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.FormulaRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableFilterRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetFilterRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetPreconditionRelationship
import eu.cdevreeze.tqa.extension.formula.relationship.VariableSetRelationship
import eu.cdevreeze.tqa.xlink.XLinkResource

/**
 * Partial implementation of `FormulaRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait FormulaRelationshipContainerLike extends FormulaRelationshipContainerApi {

  // Abstract methods

  /**
   * Returns a collection of formula relationships. Must be fast in order for this trait to be fast.
   */
  def formulaRelationships: immutable.IndexedSeq[FormulaRelationship]

  /**
   * Returns a map from source resource fragment keys to formula relationships. Must be fast in order for this trait to be fast.
   */
  def formulaRelationshipsBySource: Map[XmlFragmentKey, immutable.IndexedSeq[FormulaRelationship]]

  // Concrete methods

  // Generic query API methods for formula relationships

  final def findAllFormulaRelationshipsOfType[A <: FormulaRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    formulaRelationships collect { case rel: A => rel }
  }

  final def filterFormulaRelationshipsOfType[A <: FormulaRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllFormulaRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingFormulaRelationshipsOfType(sourceResource, relationshipType)(_ => true)
  }

  final def filterOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    formulaRelationshipsBySource.getOrElse(sourceResource.key, Vector()) collect { case relationship: A if p(relationship) => relationship }
  }

  // Query API methods for specific formula relationships (likely to be implemented in terms of the methods above)

  // Variable-set relationships

  final def findAllVariableSetRelationships: immutable.IndexedSeq[VariableSetRelationship] = {
    findAllFormulaRelationshipsOfType(classTag[VariableSetRelationship])
  }

  final def filterVariableSetRelationships(
    p: VariableSetRelationship => Boolean): immutable.IndexedSeq[VariableSetRelationship] = {

    filterFormulaRelationshipsOfType(classTag[VariableSetRelationship])(p)
  }

  final def findAllOutgoingVariableSetRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetRelationship] = {

    findAllOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetRelationship])
  }

  final def filterOutgoingVariableSetRelationships(
    variableSet: VariableSet)(p: VariableSetRelationship => Boolean): immutable.IndexedSeq[VariableSetRelationship] = {

    filterOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetRelationship])(p)
  }

  // Variable-filter relationships

  final def findAllVariableFilterRelationships: immutable.IndexedSeq[VariableFilterRelationship] = {
    findAllFormulaRelationshipsOfType(classTag[VariableFilterRelationship])
  }

  final def filterVariableFilterRelationships(
    p: VariableFilterRelationship => Boolean): immutable.IndexedSeq[VariableFilterRelationship] = {

    filterFormulaRelationshipsOfType(classTag[VariableFilterRelationship])(p)
  }

  final def findAllOutgoingVariableFilterRelationships(
    factVariable: FactVariable): immutable.IndexedSeq[VariableFilterRelationship] = {

    findAllOutgoingFormulaRelationshipsOfType(factVariable, classTag[VariableFilterRelationship])
  }

  final def filterOutgoingVariableFilterRelationships(
    factVariable: FactVariable)(p: VariableFilterRelationship => Boolean): immutable.IndexedSeq[VariableFilterRelationship] = {

    filterOutgoingFormulaRelationshipsOfType(factVariable, classTag[VariableFilterRelationship])(p)
  }

  // Variable-set-filter relationships

  final def findAllVariableSetFilterRelationships: immutable.IndexedSeq[VariableSetFilterRelationship] = {
    findAllFormulaRelationshipsOfType(classTag[VariableSetFilterRelationship])
  }

  final def filterVariableSetFilterRelationships(
    p: VariableSetFilterRelationship => Boolean): immutable.IndexedSeq[VariableSetFilterRelationship] = {

    filterFormulaRelationshipsOfType(classTag[VariableSetFilterRelationship])(p)
  }

  final def findAllOutgoingVariableSetFilterRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetFilterRelationship] = {

    findAllOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetFilterRelationship])
  }

  final def filterOutgoingVariableSetFilterRelationships(
    variableSet: VariableSet)(p: VariableSetFilterRelationship => Boolean): immutable.IndexedSeq[VariableSetFilterRelationship] = {

    filterOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetFilterRelationship])(p)
  }

  // Variable-set-precondition relationships

  final def findAllVariableSetPreconditionRelationships: immutable.IndexedSeq[VariableSetPreconditionRelationship] = {
    findAllFormulaRelationshipsOfType(classTag[VariableSetPreconditionRelationship])
  }

  final def filterVariableSetPreconditionRelationships(
    p: VariableSetPreconditionRelationship => Boolean): immutable.IndexedSeq[VariableSetPreconditionRelationship] = {

    filterFormulaRelationshipsOfType(classTag[VariableSetPreconditionRelationship])(p)
  }

  final def findAllOutgoingVariableSetPreconditionRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetPreconditionRelationship] = {

    findAllOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetPreconditionRelationship])
  }

  final def filterOutgoingVariableSetPreconditionRelationships(
    variableSet: VariableSet)(p: VariableSetPreconditionRelationship => Boolean): immutable.IndexedSeq[VariableSetPreconditionRelationship] = {

    filterOutgoingFormulaRelationshipsOfType(variableSet, classTag[VariableSetPreconditionRelationship])(p)
  }

  // Assertion message relationships

  final def findAllAssertionMessageRelationships: immutable.IndexedSeq[AssertionMessageRelationship] = {
    findAllFormulaRelationshipsOfType(classTag[AssertionMessageRelationship])
  }

  final def filterAssertionMessageRelationships(
    p: AssertionMessageRelationship => Boolean): immutable.IndexedSeq[AssertionMessageRelationship] = {

    filterFormulaRelationshipsOfType(classTag[AssertionMessageRelationship])(p)
  }

  final def findAllOutgoingAssertionMessageRelationships(
    assertion: Assertion): immutable.IndexedSeq[AssertionMessageRelationship] = {

    findAllOutgoingFormulaRelationshipsOfType(assertion, classTag[AssertionMessageRelationship])
  }

  final def filterOutgoingAssertionMessageRelationships(
    assertion: Assertion)(p: AssertionMessageRelationship => Boolean): immutable.IndexedSeq[AssertionMessageRelationship] = {

    filterOutgoingFormulaRelationshipsOfType(assertion, classTag[AssertionMessageRelationship])(p)
  }
}
