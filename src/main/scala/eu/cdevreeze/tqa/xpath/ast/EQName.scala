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

package eu.cdevreeze.tqa.xpath.ast

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName

/**
 * EQName, so either a URIQualifiedName or a QNameAsEQName.
 *
 * @author Chris de Vreeze
 */
sealed trait EQName

final case class QNameAsEQName(qname: QName) extends EQName {

  override def toString: String = qname.toString
}

final case class URIQualifiedName(ename: EName) extends EQName {

  override def toString: String = ename match {
    case EName(None, localPart)     => s"Q{}$localPart"
    case EName(Some(ns), localPart) => s"Q{$ns}$localPart"
  }
}

object EQName {

  def parse(s: String): EQName = {
    if (s.startsWith("Q{")) URIQualifiedName.parse(s) else QNameAsEQName.parse(s)
  }
}

object QNameAsEQName {

  def apply(s: String): QNameAsEQName = {
    parse(s)
  }

  def parse(s: String): QNameAsEQName = {
    QNameAsEQName(QName.parse(s))
  }
}

object URIQualifiedName {

  def parse(s: String): URIQualifiedName = {
    require(s.startsWith("Q{"), s"String '$s' is not a URIQualifiedName, because it does not start with 'Q{'")
    require(s.contains("}"), s"String '$s' is not a URIQualifiedName, because it does not contain '}'")
    require(!s.endsWith("}"), s"String '$s' is not a URIQualifiedName, because it ends with '}'")

    if (s.startsWith("Q{}")) {
      URIQualifiedName(EName.parse(s.drop(3)))
    } else {
      // Dropping the character "Q", we have James Clark notation to parse
      URIQualifiedName(EName.parse(s.drop(1)))
    }
  }
}
