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

package eu.cdevreeze.tqa.extension.table.common

/**
 * Table axis, so the x-axis, y-axis or z-axis.
 *
 * @author Chris de Vreeze
 */
sealed trait TableAxis

object TableAxis {

  case object XAxis extends TableAxis { override def toString: String = "x" }
  case object YAxis extends TableAxis { override def toString: String = "y" }
  case object ZAxis extends TableAxis { override def toString: String = "z" }

  def fromString(s: String): TableAxis = s match {
    case "x" => XAxis
    case "y" => YAxis
    case "z" => ZAxis
    case _   => sys.error(s"Not a valid 'axis': $s")
  }
}
