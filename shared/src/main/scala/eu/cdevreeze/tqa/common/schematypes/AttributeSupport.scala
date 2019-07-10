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

package eu.cdevreeze.tqa.common.schematypes

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope

/**
 * Support for obtaining attributes in general, and well-known schema attributes in particular.
 *
 * @author Chris de Vreeze
 */
object AttributeSupport {

  def optStringAttribute(attrName: EName, attrs: Map[EName, String]): Option[String] = {
    attrs.get(attrName)
  }

  def optIntAttribute(attrName: EName, attrs: Map[EName, String]): Option[Int] = {
    attrs.get(attrName).map(_.toInt)
  }

  def optBooleanAttribute(attrName: EName, attrs: Map[EName, String]): Option[Boolean] = {
    attrs.get(attrName).map(v => XsdBooleans.parseBoolean(v))
  }

  def optENameAttribute(attrName: EName, attrs: Map[EName, String], scope: Scope): Option[EName] = {
    attrs.get(attrName).map(QName.parse).map(qn => scope.resolveQName(qn))
  }

  // Specific schema attributes

  def optId(attrs: Map[EName, String]): Option[String] = {
    optStringAttribute(ENames.IdEName, attrs)
  }

  def optAbstract(attrs: Map[EName, String]): Option[Boolean] = {
    optBooleanAttribute(ENames.AbstractEName, attrs)
  }

  def optName(attrs: Map[EName, String]): Option[String] = {
    optStringAttribute(ENames.NameEName, attrs)
  }

  def optNillable(attrs: Map[EName, String]): Option[Boolean] = {
    optBooleanAttribute(ENames.NillableEName, attrs)
  }

  def optSubstitutionGroup(attrs: Map[EName, String], scope: Scope): Option[EName] = {
    optENameAttribute(ENames.SubstitutionGroupEName, attrs, scope)
  }

  def optRef(attrs: Map[EName, String], scope: Scope): Option[EName] = {
    optENameAttribute(ENames.RefEName, attrs, scope)
  }

  def optType(attrs: Map[EName, String], scope: Scope): Option[EName] = {
    optENameAttribute(ENames.TypeEName, attrs, scope)
  }

  def optMinOccurs(attrs: Map[EName, String]): Option[Int] = {
    optIntAttribute(ENames.MinOccursEName, attrs)
  }

  def optMaxOccurs(attrs: Map[EName, String]): Option[String] = {
    optStringAttribute(ENames.MaxOccursEName, attrs)
  }

  def optBaseType(attrs: Map[EName, String], scope: Scope): Option[EName] = {
    optENameAttribute(ENames.BaseEName, attrs, scope)
  }
}
