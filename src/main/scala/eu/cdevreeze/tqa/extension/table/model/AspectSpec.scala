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

package eu.cdevreeze.tqa.extension.table.model

import eu.cdevreeze.tqa.aspect.Aspect
import eu.cdevreeze.yaidom.core.EName

/**
 * Aspect spec.
 *
 * @author Chris de Vreeze
 */
sealed trait AspectSpec {

  def aspect: Aspect
}

case object ConceptAspectSpec extends AspectSpec {

  def aspect: Aspect = Aspect.ConceptAspect
}

case object EntityIdentifierAspectSpec extends AspectSpec {

  def aspect: Aspect = Aspect.EntityIdentifierAspect
}

case object PeriodAspectSpec extends AspectSpec {

  def aspect: Aspect = Aspect.PeriodAspect
}

case object UnitAspectSpec extends AspectSpec {

  def aspect: Aspect = Aspect.UnitAspect
}

final case class DimensionAspectSpec(
    dimension: EName,
    isIncludeUnreportedValue: Boolean) extends AspectSpec {

  def aspect: Aspect = Aspect.DimensionAspect(dimension)
}
