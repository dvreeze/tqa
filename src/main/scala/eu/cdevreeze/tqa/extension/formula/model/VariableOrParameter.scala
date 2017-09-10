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

import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.yaidom.core.EName

/**
 * Variable or parameter.
 *
 * @author Chris de Vreeze
 */
sealed trait VariableOrParameter

sealed trait Variable extends VariableOrParameter

final case class FactVariable(
  bindAsSequence: Boolean,
  fallbackValueExprOption: Option[ScopedXPathString],
  matchesOption: Option[Boolean],
  nilsOption: Option[Boolean]) extends Variable

final case class GeneralVariable(
  bindAsSequence: Boolean,
  selectExpr: ScopedXPathString) extends Variable

final case class Parameter(
  name: EName,
  selectExprOption: Option[ScopedXPathString],
  requiredOption: Option[Boolean],
  asOption: Option[EName]) extends VariableOrParameter
