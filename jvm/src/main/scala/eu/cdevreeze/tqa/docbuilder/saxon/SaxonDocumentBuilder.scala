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

package eu.cdevreeze.tqa.docbuilder.saxon

import java.net.URI

import org.xml.sax.InputSource

import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api

/**
 * Saxon document builder using a Saxon DocumentBuilder and URI converter.
 *
 * The URI resolver is used for parsing the documents themselves (unlike SAX EntityResolver).
 * Typically the URI resolver takes HTTP(S) URIs and resolves them to resources in a local mirror.
 *
 * @author Chris de Vreeze
 */
final class SaxonDocumentBuilder(
  val docBuilder:  s9api.DocumentBuilder,
  val uriResolver: URI => InputSource) extends DocumentBuilder {

  type BackingDoc = SaxonDocument

  def build(uri: URI): SaxonDocument = {
    val is = uriResolver(uri)
    is.setSystemId(uri.toString)

    val src = convertInputSourceToSource(is).ensuring(_.getSystemId == uri.toString)

    val node = docBuilder.build(src).getUnderlyingNode.ensuring(_.getSystemId == uri.toString)
    SaxonDocument.wrapDocument(node.getTreeInfo)
  }

  private def convertInputSourceToSource(is: InputSource): Source = {
    assert(is.getSystemId != null) // scalastyle:ignore null

    if (is.getCharacterStream != null) { // scalastyle:ignore null
      val src = new StreamSource(is.getCharacterStream)
      Option(is.getSystemId).foreach(v => src.setSystemId(v))
      Option(is.getPublicId).foreach(v => src.setPublicId(v))
      src
    } else {
      require(is.getByteStream != null, s"Neither InputStream nor Reader set on InputSource") // scalastyle:ignore null
      val src = new StreamSource(is.getByteStream)
      Option(is.getSystemId).foreach(v => src.setSystemId(v))
      Option(is.getPublicId).foreach(v => src.setPublicId(v))
      // No encoding can be set
      src
    }
  }
}

object SaxonDocumentBuilder {

  /**
   * Creates a SaxonDocumentBuilder from an underlying Saxon s9api DocumentBuilder, and an URI resolver.
   * The URI resolver is typically obtained through the UriResolvers singleton object.
   */
  def apply(docBuilder: s9api.DocumentBuilder, uriResolver: URI => InputSource): SaxonDocumentBuilder = {
    new SaxonDocumentBuilder(docBuilder, uriResolver)
  }
}
