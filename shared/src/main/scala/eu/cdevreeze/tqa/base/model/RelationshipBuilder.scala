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

import java.net.URI

import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.yaidom.core.EName

/**
 * API for builders of relationships.
 *
 * @author Chris de Vreeze
 */
trait RelationshipBuilder {

  type RelationshipType <: Relationship

  type SourceNodeType <: Node

  type TargetNodeType <: Node

  /**
   * Creates a relationship of the given type from the passed parameters, if applicable,
   * and otherwise returns None.
   */
  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: SourceNodeType,
    target: TargetNodeType,
    nonXLinkArcAttributes: Map[EName, String]): Option[RelationshipType]
}

object RelationshipBuilder {

  type Aux[R, S, T] = RelationshipBuilder {
    type RelationshipType = R
    type SourceNodeType = S
    type TargetNodeType = T
  }
}
