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
 * Utility holding a type for axes in concept relation filter.
 *
 * @author Chris de Vreeze
 */
object ConceptRelationFilters {

  /**
   * Axis in an concept relation filter.
   */
  sealed trait Axis {

    import Axis._

    final override def toString: String = this match {
      case ChildAxis               => "child"
      case ChildOrSelfAxis         => "child-or-self"
      case DescendantAxis          => "descendant"
      case DescendantOrSelfAxis    => "descendant-or-self"
      case ParentAxis              => "parent"
      case ParentOrSelfAxis        => "parent-or-self"
      case AncestorAxis            => "ancestor"
      case AncestorOrSelfAxis      => "ancestor-or-self"
      case SiblingAxis             => "sibling"
      case SiblingOrSelfAxis       => "sibling-or-self"
      case SiblingOrDescendantAxis => "sibling-or-descendant"
    }
  }

  object Axis {

    case object ChildAxis extends Axis
    case object ChildOrSelfAxis extends Axis
    case object DescendantAxis extends Axis
    case object DescendantOrSelfAxis extends Axis
    case object ParentAxis extends Axis
    case object ParentOrSelfAxis extends Axis
    case object AncestorAxis extends Axis
    case object AncestorOrSelfAxis extends Axis
    case object SiblingAxis extends Axis
    case object SiblingOrSelfAxis extends Axis
    case object SiblingOrDescendantAxis extends Axis

    def fromString(s: String): Axis = s match {
      case "child"                 => ChildAxis
      case "child-or-self"         => ChildOrSelfAxis
      case "descendant"            => DescendantAxis
      case "descendant-or-self"    => DescendantOrSelfAxis
      case "parent"                => ParentAxis
      case "parent-or-self"        => ParentOrSelfAxis
      case "ancestor"              => AncestorAxis
      case "ancestor-or-self"      => AncestorOrSelfAxis
      case "sibling"               => SiblingAxis
      case "sibling-or-self"       => SiblingOrSelfAxis
      case "sibling-or-descendant" => SiblingOrDescendantAxis
      case _                       => sys.error(s"Not a valid axis: $s")
    }
  }
}
