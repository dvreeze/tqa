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

package eu.cdevreeze.tqa.extension.formula.common

/**
 * OCC, so segment or scenario.
 *
 * @author Chris de Vreeze
 */
sealed trait Occ

object Occ {

  case object Segment extends Occ
  case object Scenario extends Occ

  def fromString(s: String): Occ = s match {
    case "segment"  => Segment
    case "scenario" => Scenario
    case _          => sys.error(s"Not a valid 'occ': $s")
  }
}
