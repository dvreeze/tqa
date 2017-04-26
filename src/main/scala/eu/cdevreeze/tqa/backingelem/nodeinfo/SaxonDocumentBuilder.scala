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

package eu.cdevreeze.tqa.backingelem.nodeinfo

import java.io.File
import java.net.URI

import eu.cdevreeze.tqa.backingelem.DocumentBuilder
import net.sf.saxon.s9api

/**
 * Saxon document builder using a Saxon DocumentBuilder and URI converter.
 *
 * @author Chris de Vreeze
 */
final class SaxonDocumentBuilder(
    val docBuilder: s9api.DocumentBuilder,
    val uriConverter: URI => URI) extends DocumentBuilder {

  type BackingElem = SaxonElem

  def build(uri: URI): SaxonElem = {
    val localUri = uriConverter(uri)
    require(localUri.getScheme == "file", s"Expected local file URI but found $localUri")

    val node = docBuilder.build(new File(localUri)).getUnderlyingNode
    node.setSystemId(uri.toString)
    SaxonDocument.wrapDocument(node.getTreeInfo).documentElement
  }
}
