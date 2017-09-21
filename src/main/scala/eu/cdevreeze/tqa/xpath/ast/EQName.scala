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

final case class QNameAsEQName(qname: QName) extends EQName

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

  def canBeStartOfQNameAsEQName(s: String): Boolean = {
    val idx = s.indexOf(':')

    if (idx < 0) {
      NCName.canBeStartOfNCName(s)
    } else {
      NCName.canBeNCName(s.substring(0, idx)) && NCName.canBeStartOfNCName(s.substring(idx + 1))
    }
  }

  def canBeQNameAsEQName(s: String): Boolean = {
    canBeStartOfQNameAsEQName(s) && !s.endsWith(":")
  }

  def canBeStartOfQNameAsEQName(c: Char): Boolean = {
    NCName.canBeStartOfNCName(c)
  }

  def canBePartOfQNameAsEQName(c: Char): Boolean = {
    NCName.canBePartOfNCName(c) || (c == ':')
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

  def canBeStartOfURIQualifiedName(s: String): Boolean = {
    if (!s.startsWith("Q{")) {
      false
    } else {
      val idx = s.indexOf('}')

      if (idx < 0) {
        !s.drop(2).contains('{') // TODO What else?
      } else {
        !s.drop(2).contains('{') && NCName.canBeStartOfNCName(s.substring(idx + 1)) // TODO What else?
      }
    }
  }

  def canBeURIQualifiedName(s: String): Boolean = {
    canBeStartOfURIQualifiedName(s) && !s.endsWith("}")
  }

  def canBeStartOfURIQualifiedName(c: Char): Boolean = {
    c == 'Q'
  }

  def canBePartOfURIQualifiedName(c: Char): Boolean = {
    // TODO Just quessing here. At some point during parsing some whitespace is expected (to stop parsing the URI qualified name).

    !java.lang.Character.isWhitespace(c)
  }
}
