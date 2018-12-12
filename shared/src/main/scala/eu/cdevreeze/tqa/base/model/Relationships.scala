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

package eu.cdevreeze.tqa.base.model

import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.yaidom.core.EName

/**
 * API for factories of relationships. This object does not depend on the Relationship types and companion objects.
 *
 * @author Chris de Vreeze
 */
object Relationships {

  trait Factory {

    /**
     * Relationship type, which is a sub-type of Relationship (but this is not specified here, in order to
     * prevent circular dependencies).
     */
    type RelationshipType

    type SourceNodeType <: Node

    type TargetNodeType <: Node

    /**
     * Creates a relationship of the given type from the passed parameters, if applicable,
     * and otherwise returns None.
     */
    def opt(
      baseSetKey: BaseSetKey,
      source: SourceNodeType,
      target: TargetNodeType,
      nonXLinkArcAttributes: Map[EName, String]): Option[RelationshipType]
  }

  object Factory {

    type Aux[R, S, T] = Factory {
      type RelationshipType = R
      type SourceNodeType = S
      type TargetNodeType = T
    }
  }
}
