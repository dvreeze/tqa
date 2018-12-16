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

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.yaidom.queryapi.ElemApi
import eu.cdevreeze.yaidom.queryapi.ElemLike
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.resolved

/**
 * Content (other than text) of a Resource, so not the Resource itself. It has no knowledge about its context,
 * other than the ELR (for element key determination), so it should not be used in isolation from the containing
 * Resource (with which it must share the same ELR).
 *
 * This resource content model offers the yaidom ElemApi API.
 *
 * @author Chris de Vreeze
 */
final class ResourceContentElement(
  val elr: String,
  val underlyingElem: ScopedNodes.Elem) extends ElemApi with ElemLike {

  type ThisElem = ResourceContentElement

  // Methods needed for completing the SubtypeAwareElemApi API

  final def thisElem: ResourceContentElement = this

  final def findAllChildElems: immutable.IndexedSeq[ResourceContentElement] = {
    underlyingElem.findAllChildElems.map(e => new ResourceContentElement(elr, e))
  }

  // Equality

  override def equals(other: Any): Boolean = other match {
    case other: ResourceContentElement =>
      elr == other.elr && toResolvedElem == other.toResolvedElem
  }

  override def hashCode: Int = (elr, toResolvedElem).hashCode

  // Other methods

  def idOption: Option[String] = underlyingElem.attributeOption(ENames.IdEName)

  def elementKeyOption: Option[ResourceKey] = {
    idOption.map { id =>
      ResourceKey(elr, underlyingElem.resolvedName, id)
    }
  }

  private def toResolvedElem: resolved.Elem = {
    resolved.Elem.from(underlyingElem).removeAllInterElementWhitespace.coalesceAndNormalizeAllText
  }
}

object ResourceContentElement {

  def apply(elr: String, underlyingElem: ScopedNodes.Elem): ResourceContentElement = {
    new ResourceContentElement(elr, underlyingElem)
  }
}
