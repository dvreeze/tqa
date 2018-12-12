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

import eu.cdevreeze.yaidom.core.EName

/**
 * Element key, for schema content or resources. It must always contain a string ID, but typically contains more
 * data to make it sufficiently unique.
 *
 * It does not contain any URI, to make the elements having those keys as portable as possible.
 *
 * It is assumed that only the 2 sub-types SchemaContentElementKey and ResourceKey are needed, that is, that
 * each element (to refer to in a LocatorNode) for which there is no specific LocatorNode sub-type is either
 * schema content or a ResourceNode.
 *
 * @author Chris de Vreeze
 */
sealed trait ElementKey {

  def id: String
}

/**
 * Element key of a SchemaContentElement.
 */
final case class SchemaContentElementKey(
  targetNamespaceUriOption: Option[String],
  elementName: EName,
  id: String) extends ElementKey

/**
 * Element key of a ResourceNode.
 */
final case class ResourceKey(
  elr: String,
  roleOption: Option[String],
  id: String) extends ElementKey
