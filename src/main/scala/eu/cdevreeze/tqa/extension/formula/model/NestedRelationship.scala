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

import eu.cdevreeze.tqa.common.Use

/**
 * A "nested relationship" in a formula context. It contains the relationship target, and not the source.
 * It also contains relationship attributes such as order, priority and use. It also contains the ELR
 * of the parent extended link.
 *
 * @author Chris de Vreeze
 */
trait NestedRelationship[Target] {

  def elr: String

  def target: Target

  def order: BigDecimal

  def priority: Int

  def use: Use
}
