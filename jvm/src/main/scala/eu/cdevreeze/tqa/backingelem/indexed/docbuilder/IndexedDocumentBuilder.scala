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

package eu.cdevreeze.tqa.backingelem.indexed.docbuilder

import java.net.URI

import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.yaidom.indexed.Document
import eu.cdevreeze.yaidom.indexed.Elem
import eu.cdevreeze.yaidom.parse.DocumentParser

/**
 * Indexed document builder using a yaidom DocumentParser and URI converter.
 *
 * @author Chris de Vreeze
 */
final class IndexedDocumentBuilder(
    val docParser: DocumentParser,
    val uriConverter: URI => URI) extends DocumentBuilder {

  type BackingElem = Elem

  def build(uri: URI): Elem = {
    val localUri = uriConverter(uri)

    val doc = docParser.parse(localUri).withUriOption(Some(uri))
    Document(doc).documentElement
  }
}
