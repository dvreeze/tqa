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
 * Braced URI literal.
 *
 * @author Chris de Vreeze
 */
final case class BracedUriLiteral(namespaceOption: Option[String]) {

  override def toString: String = {
    val ns = namespaceOption.getOrElse("")
    s"Q{$ns}"
  }
}

object BracedUriLiteral {

  def parse(s: String): BracedUriLiteral = {
    require(s.startsWith("Q{"), s"A braced URI literal must start with 'Q{', but found: '$s'")
    require(s.endsWith("}"), s"A braced URI literal must end with '}', but found: '$s'")

    val rawNs = s.drop(2).dropRight(1)
    val nsOption = if (rawNs.isEmpty) None else Some(rawNs)
    BracedUriLiteral(nsOption)
  }
}
