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

package eu.cdevreeze.tqa.extension.table.layoutmodel.common

/**
 * Label source, so explicit or processor.
 *
 * @author Chris de Vreeze
 */
sealed trait LabelSource

object LabelSource {

  case object Explicit extends LabelSource { override def toString: String = "explicit" }
  case object Processor extends LabelSource { override def toString: String = "processor" }

  def fromString(s: String): LabelSource = s match {
    case "explicit" => Explicit
    case "processor" => Processor
    case _ => sys.error(s"Not a valid 'label source': $s")
  }
}
