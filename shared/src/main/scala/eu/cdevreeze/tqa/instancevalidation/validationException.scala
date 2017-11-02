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

package eu.cdevreeze.tqa.instancevalidation

import eu.cdevreeze.yaidom.core.EName

/**
 * Exception encountered during validation. Error xbrldie:PrimaryItemDimensionallyInvalidError does not count
 * as an exception here, but is treated as a normal Boolean validation result. Error xbrldie:IllegalTypedDimensionContentError
 * is absent due to the lack of schema validation (for typed dimension members) in this context.
 *
 * @author Chris de Vreeze
 */
sealed trait ValidationException extends RuntimeException

final case class DefaultValueUsedInInstanceError(dimension: EName) extends ValidationException

case object RepeatedDimensionInInstanceError extends ValidationException

final case class ExplicitMemberNotExplicitDimensionError(ename: EName) extends ValidationException

final case class TypedMemberNotTypedDimensionError(ename: EName) extends ValidationException

final case class ExplicitMemberUndefinedQNameError(ename: EName) extends ValidationException

final case class IllegalTypedDimensionContentError(ename: EName) extends ValidationException
