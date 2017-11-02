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

package eu.cdevreeze.tqa.base.relationship

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import javax.xml.bind.DatatypeConverter

/**
 * Typed attribute value, as used in the non-exempt attributes in a relationship key. The sub-classes are designed
 * for value equality.
 *
 * See http://www.xbrl.org/Specification/XBRL-2.1/REC-2003-12-31/XBRL-2.1-REC-2003-12-31+corrected-errata-2013-02-20.html#_4.10.
 *
 * @author Chris de Vreeze
 */
sealed trait TypedAttributeValue {

  type AttrValueType

  def value: AttrValueType
}

final case class BooleanAttributeValue(val value: Boolean) extends TypedAttributeValue {

  type AttrValueType = Boolean
}

final case class FloatAttributeValue(val value: Float) extends TypedAttributeValue {

  type AttrValueType = Float
}

final case class DoubleAttributeValue(val value: Double) extends TypedAttributeValue {

  type AttrValueType = Double
}

final case class DecimalAttributeValue(val value: BigDecimal) extends TypedAttributeValue {

  type AttrValueType = BigDecimal
}

final case class ENameAttributeValue(val value: EName) extends TypedAttributeValue {

  type AttrValueType = EName
}

final case class StringAttributeValue(val value: String) extends TypedAttributeValue {

  type AttrValueType = String
}

object BooleanAttributeValue {

  def parse(s: String): BooleanAttributeValue = {
    val value = DatatypeConverter.parseBoolean(s)
    BooleanAttributeValue(value)
  }
}

object FloatAttributeValue {

  def parse(s: String): FloatAttributeValue = {
    val value = DatatypeConverter.parseFloat(s)
    FloatAttributeValue(value)
  }
}

object DoubleAttributeValue {

  def parse(s: String): DoubleAttributeValue = {
    val value = DatatypeConverter.parseDouble(s)
    DoubleAttributeValue(value)
  }
}

object DecimalAttributeValue {

  def parse(s: String): DecimalAttributeValue = {
    val value = DatatypeConverter.parseDecimal(s)
    DecimalAttributeValue(value)
  }
}

object ENameAttributeValue {

  def parse(s: String, scope: Scope): ENameAttributeValue = {
    val qname = QName(s)
    val ename = scope.withoutDefaultNamespace.resolveQNameOption(qname).get
    ENameAttributeValue(ename)
  }
}

object StringAttributeValue {

  def parse(s: String): StringAttributeValue = {
    StringAttributeValue(s)
  }
}
