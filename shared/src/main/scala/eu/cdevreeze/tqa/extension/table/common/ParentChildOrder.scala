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
 * Parent-child order, so either parent-first or children-first.
 *
 * @author Chris de Vreeze
 */
sealed trait ParentChildOrder

object ParentChildOrder {

  case object ParentFirst extends ParentChildOrder { override def toString: String = "parent-first" }
  case object ChildrenFirst extends ParentChildOrder { override def toString: String = "children-first" }

  def fromString(s: String): ParentChildOrder = s match {
    case "parent-first"   => ParentFirst
    case "children-first" => ChildrenFirst
    case _                => sys.error(s"Not a valid 'ParentChildOrder': $s")
  }
}
