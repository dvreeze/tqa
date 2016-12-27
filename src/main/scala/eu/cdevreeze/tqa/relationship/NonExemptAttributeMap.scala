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

package eu.cdevreeze.tqa.relationship

import eu.cdevreeze.tqa.ENames.OrderEName
import eu.cdevreeze.tqa.ENames.PriorityEName
import eu.cdevreeze.tqa.ENames.UseEName
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.yaidom.core.EName

/**
 * The non-exempt attributes in a relationship key. It is designed for (fast) value equality, thus
 * supporting fast value equality for relationship keys.
 *
 * Equality of non-exempt attributes is sensitive. Why?
 * <ul>
 * <li>Attributes can be default or fixed attributes, according to the corresponding schema.</li>
 * <li>Attributes have types, so equality should take their types into account.</li>
 * <li>Some attributes, such as the "order" attribute, are implicitly always there, even if the schema does not say so.</li>
 * </ul>
 *
 * @author Chris de Vreeze
 */
final class NonExemptAttributeMap private (val attrs: Map[EName, TypedAttributeValue]) {
  require(attrs.keySet.forall(attrName =>
    attrName != UseEName &&
      attrName != PriorityEName &&
      attrName.namespaceUriOption != Some(XLinkNamespace)), s"Not all attributes non-exempt in ${attrs.keySet}")
  require(attrs.keySet.contains(OrderEName), s"Missing order attribute as non-exempt attribute in ${attrs.keySet}")

  override def equals(other: Any): Boolean = other match {
    case other: NonExemptAttributeMap => this.attrs == other.attrs
    case _                            => false
  }

  override def hashCode: Int = attrs.hashCode
}

object NonExemptAttributeMap {

  def from(attrs: Map[EName, TypedAttributeValue]): NonExemptAttributeMap = {
    val editedAttrs = attrs.updated(OrderEName, attrs.getOrElse(OrderEName, DecimalAttributeValue(1)))
    new NonExemptAttributeMap(editedAttrs)
  }
}
