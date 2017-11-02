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
 * Utility holding a type for formula axes in concept relationship nodes.
 *
 * @author Chris de Vreeze
 */
object ConceptRelationshipNodes {

  /**
   * Formula axis in a concept relationship nodes.
   */
  sealed trait FormulaAxis {

    import FormulaAxis._

    def includesSelf: Boolean = {
      this == DescendantOrSelfAxis || this == ChildOrSelfAxis ||
        this == SiblingOrSelfAxis || this == SiblingOrDescendantOrSelfAxis
    }

    def includesDescendantsOrChildren: Boolean = {
      this == SiblingOrDescendantAxis || this == SiblingOrDescendantOrSelfAxis ||
        this == DescendantAxis || this == DescendantOrSelfAxis ||
        this == ChildAxis || this == ChildOrSelfAxis
    }

    def includesSiblings: Boolean = {
      this == SiblingAxis || this == SiblingOrSelfAxis ||
        this == SiblingOrDescendantAxis || this == SiblingOrDescendantOrSelfAxis
    }

    def includesChildrenButNotDeeperDescendants: Boolean = {
      this == ChildAxis || this == ChildOrSelfAxis
    }
  }

  object FormulaAxis {

    case object DescendantAxis extends FormulaAxis { override def toString: String = "descendant" }
    case object DescendantOrSelfAxis extends FormulaAxis { override def toString: String = "descendant-or-self" }
    case object ChildAxis extends FormulaAxis { override def toString: String = "child" }
    case object ChildOrSelfAxis extends FormulaAxis { override def toString: String = "child-or-self" }
    case object SiblingAxis extends FormulaAxis { override def toString: String = "sibling" }
    case object SiblingOrSelfAxis extends FormulaAxis { override def toString: String = "sibling-or-self" }
    case object SiblingOrDescendantAxis extends FormulaAxis { override def toString: String = "sibling-or-descendant" }
    case object SiblingOrDescendantOrSelfAxis extends FormulaAxis { override def toString: String = "sibling-or-descendant-or-self" }

    def fromString(s: String): FormulaAxis = s match {
      case "descendant"                    => DescendantAxis
      case "descendant-or-self"            => DescendantOrSelfAxis
      case "child"                         => ChildAxis
      case "child-or-self"                 => ChildOrSelfAxis
      case "sibling"                       => SiblingAxis
      case "sibling-or-self"               => SiblingOrSelfAxis
      case "sibling-or-descendant"         => SiblingOrDescendantAxis
      case "sibling-or-descendant-or-self" => SiblingOrDescendantOrSelfAxis
      case _                               => sys.error(s"Not a valid 'formula axis': $s")
    }
  }
}
