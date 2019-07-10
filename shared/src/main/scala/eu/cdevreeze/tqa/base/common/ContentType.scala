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

package eu.cdevreeze.tqa.base.common

/**
 * The content type of a complex (schema) type.
 *
 * @author Chris de Vreeze
 */
sealed trait ContentType

object ContentType {

  case object Simple extends ContentType
  case object ElementOnly extends ContentType
  case object Mixed extends ContentType
  case object Empty extends ContentType

  def fromString(s: String): ContentType = {
    Set(Simple, ElementOnly, Mixed, Empty).find(_.toString == s).getOrElse(sys.error(s"Not a valid content type: $s"))
  }
}
