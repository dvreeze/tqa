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
 * Purely abstract trait offering a formula relationship query API.
 *
 * Implementations should make sure that looking up relationships by source resource is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait FormulaRelationshipContainerApi {

  // Generic query API methods for formula relationships

  def findAllFormulaRelationshipsOfType[A <: FormulaRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterFormulaRelationshipsOfType[A <: FormulaRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all formula relationships of the given type that are outgoing from the given resource.
   */
  def findAllOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters formula relationships of the given type that are outgoing from the given resource.
   */
  def filterOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  // Query API methods for specific formula relationships (likely to be implemented in terms of the methods above)

  // Variable-set relationships

  def findAllVariableSetRelationships: immutable.IndexedSeq[VariableSetRelationship]

  def filterVariableSetRelationships(
    p: VariableSetRelationship => Boolean): immutable.IndexedSeq[VariableSetRelationship]

  /**
   * Finds all variable-set relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetRelationship]

  /**
   * Filters variable-set relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetRelationships(
    variableSet: VariableSet)(p: VariableSetRelationship => Boolean): immutable.IndexedSeq[VariableSetRelationship]

  // Variable-filter relationships

  def findAllVariableFilterRelationships: immutable.IndexedSeq[VariableFilterRelationship]

  def filterVariableFilterRelationships(
    p: VariableFilterRelationship => Boolean): immutable.IndexedSeq[VariableFilterRelationship]

  /**
   * Finds all variable-filter relationships that are outgoing from the given FactVariable.
   */
  def findAllOutgoingVariableFilterRelationships(
    factVariable: FactVariable): immutable.IndexedSeq[VariableFilterRelationship]

  /**
   * Filters variable-filter relationships that are outgoing from the given FactVariable.
   */
  def filterOutgoingVariableFilterRelationships(
    factVariable: FactVariable)(p: VariableFilterRelationship => Boolean): immutable.IndexedSeq[VariableFilterRelationship]

  // Variable-set-filter relationships

  def findAllVariableSetFilterRelationships: immutable.IndexedSeq[VariableSetFilterRelationship]

  def filterVariableSetFilterRelationships(
    p: VariableSetFilterRelationship => Boolean): immutable.IndexedSeq[VariableSetFilterRelationship]

  /**
   * Finds all variable-set-filter relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetFilterRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetFilterRelationship]

  /**
   * Filters variable-set-filter relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetFilterRelationships(
    variableSet: VariableSet)(p: VariableSetFilterRelationship => Boolean): immutable.IndexedSeq[VariableSetFilterRelationship]

  // Variable-set-precondition relationships

  def findAllVariableSetPreconditionRelationships: immutable.IndexedSeq[VariableSetPreconditionRelationship]

  def filterVariableSetPreconditionRelationships(
    p: VariableSetPreconditionRelationship => Boolean): immutable.IndexedSeq[VariableSetPreconditionRelationship]

  /**
   * Finds all variable-set-precondition relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetPreconditionRelationships(
    variableSet: VariableSet): immutable.IndexedSeq[VariableSetPreconditionRelationship]

  /**
   * Filters variable-set-precondition relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetPreconditionRelationships(
    variableSet: VariableSet)(p: VariableSetPreconditionRelationship => Boolean): immutable.IndexedSeq[VariableSetPreconditionRelationship]

  // Assertion message relationships

  def findAllAssertionMessageRelationships: immutable.IndexedSeq[AssertionMessageRelationship]

  def filterAssertionMessageRelationships(
    p: AssertionMessageRelationship => Boolean): immutable.IndexedSeq[AssertionMessageRelationship]

  /**
   * Finds all assertion-message relationships that are outgoing from the given Assertion.
   */
  def findAllOutgoingAssertionMessageRelationships(
    assertion: Assertion): immutable.IndexedSeq[AssertionMessageRelationship]

  /**
   * Filters assertion-message relationships that are outgoing from the given Assertion.
   */
  def filterOutgoingAssertionMessageRelationships(
    assertion: Assertion)(p: AssertionMessageRelationship => Boolean): immutable.IndexedSeq[AssertionMessageRelationship]
}
