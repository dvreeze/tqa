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

package eu.cdevreeze.tqa.docbuilder

import java.net.URI

import eu.cdevreeze.yaidom.queryapi.BackingElemApi

/**
 * Any builder of a document root backing element. Typical document builders convert the document URI
 * to a local URI, parse the document using that local URI, and after parsing store the original
 * URI as document URI in the returned backing element. Some document builders are capable of parsing
 * documents inside ZIP files.
 *
 * Document builders can be stacked, for example to perform some "post-processing".
 * For example, some taxonomy document may have a broken link in the schema location attribute, and
 * such a post-processing step can fix that before the backing element is used to build a type-safe
 * taxonomy DOM tree from it.
 *
 * Other "decorating" document builders can cache parsed documents, for example.
 *
 * Note that document builders backed by typical XML parsers are not thread-safe in the JVM!
 *
 * Note that these builders return root elements and not documents, so top-level comments and processing
 * instructions are lost. It should be possible to parse the document later again, given the document URI,
 * if needed using StAX in order to get only the top-level comments, processing instructions and XML
 * declaration. Retrieval of these information items can be hidden behind strategy interfaces, so only
 * "application wiring" needs to worry about how to obtain this data.
 *
 * Note that this document builder abstraction is useful both in the JVM and in JavaScript runtimes.
 *
 * @author Chris de Vreeze
 */
trait DocumentBuilder {

  type BackingElem <: BackingElemApi

  /**
   * Returns the document that has the given URI as BackingElem. The URI is typically the canonical,
   * published URI of the document. This URI is also typically stored in the resulting root element as
   * document URI. If the document builder uses an XML catalog, the document is typically parsed from
   * a local (mirror) URI.
   */
  def build(uri: URI): BackingElem
}

object DocumentBuilder {

  type Aux[A] = DocumentBuilder { type BackingElem = A }
}
