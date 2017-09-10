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

package eu.cdevreeze.tqa.extension.formula.model

import scala.collection.immutable

import eu.cdevreeze.tqa.AspectModel
import eu.cdevreeze.tqa.BigDecimalValueOrExpr
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.yaidom.core.EName

/**
 * Variable set, such as a value assertion or formula. The variable set does not know its ELR.
 *
 * @author Chris de Vreeze
 */
sealed trait VariableSet {

  def implicitFiltering: Boolean

  def aspectModel: AspectModel

  def variableSetFilters: immutable.IndexedSeq[VariableSetFilter]

  def variableSetPreconditions: immutable.IndexedSeq[VariableSetPrecondition]

  def variableSetVariablesOrParameters: immutable.IndexedSeq[VariableSetVariableOrParameter]
}

sealed trait VariableSetAssertion extends VariableSet with Assertion

final case class ValueAssertion(
  implicitFiltering: Boolean,
  aspectModel: AspectModel,
  testExpr: ScopedXPathString,
  variableSetFilters: immutable.IndexedSeq[VariableSetFilter],
  variableSetPreconditions: immutable.IndexedSeq[VariableSetPrecondition],
  variableSetVariablesOrParameters: immutable.IndexedSeq[VariableSetVariableOrParameter]) extends VariableSetAssertion

final case class ExistenceAssertion(
  implicitFiltering: Boolean,
  aspectModel: AspectModel,
  testExprOption: Option[ScopedXPathString],
  variableSetFilters: immutable.IndexedSeq[VariableSetFilter],
  variableSetPreconditions: immutable.IndexedSeq[VariableSetPrecondition],
  variableSetVariablesOrParameters: immutable.IndexedSeq[VariableSetVariableOrParameter]) extends VariableSetAssertion

final case class Formula(
  implicitFiltering: Boolean,
  aspectModel: AspectModel,
  sourceOption: Option[EName],
  valueExpr: ScopedXPathString,
  aspectRuleGroups: immutable.IndexedSeq[AspectRuleGroup],
  precisionValueOrExprOption: Option[BigDecimalValueOrExpr],
  decimalsValueOrExprOption: Option[BigDecimalValueOrExpr],
  variableSetFilters: immutable.IndexedSeq[VariableSetFilter],
  variableSetPreconditions: immutable.IndexedSeq[VariableSetPrecondition],
  variableSetVariablesOrParameters: immutable.IndexedSeq[VariableSetVariableOrParameter]) extends VariableSet

// TODO ConsistencyAssertion, but not in this file, because it is not a VariableSet
