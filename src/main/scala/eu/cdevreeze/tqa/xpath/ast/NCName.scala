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

/**
 * NCName, that is, a non-colon name.
 *
 * @author Chris de Vreeze
 */
final case class NCName(name: String) {
  require(!name.contains(':'), s"Not an NCName: '$name'")
}

object NCName {

  def canBeStartOfNCName(s: String): Boolean = {
    s.nonEmpty && canBeStartOfNCName(s.charAt(0)) && s.drop(1).forall(c => canBePartOfNCName(c))
  }

  def canBeNCName(s: String): Boolean = {
    canBeStartOfNCName(s)
  }

  // See https://stackoverflow.com/questions/1631396/what-is-an-xsncname-type-and-when-should-it-be-used
  def canBeStartOfNCName(c: Char): Boolean = {
    // By disallowing digits and dots as first characters of an NCName, an XPath parser does not confuse
    // NCNames with numeric literals.

    canBePartOfNCName(c) && !java.lang.Character.isDigit(c) && (c != '.') && (c != '-')
  }

  // See https://stackoverflow.com/questions/1631396/what-is-an-xsncname-type-and-when-should-it-be-used
  def canBePartOfNCName(c: Char): Boolean = {
    !DisallowedNonWhitespaceChars.contains(c) && !java.lang.Character.isWhitespace(c)
  }

  private val DisallowedNonWhitespaceChars: Set[Char] = {
    // The quote characters, if not disallowed, would confuse an XPath parser when a string literal must be found
    // instead of an NCName.

    Set(':', '@', '$', '%', '&', '/', '+', ',', ';', '(', ')', '[', ']', '{', '}', '<', '>', '\'', '"')
  }
}
