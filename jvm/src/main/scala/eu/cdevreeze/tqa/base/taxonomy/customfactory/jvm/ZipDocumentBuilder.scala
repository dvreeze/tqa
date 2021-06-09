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

package eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm

import java.io.FilterInputStream
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipInputStream

import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.yaidom.saxon.SaxonDocument
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api
import org.xml.sax.InputSource

/**
 * DocumentBuilder iterating through a ZIP InputStream. It implements the DocumentBuilder API, but not in spirit,
 * because the passed URI in method build does not choose the InputStream to use. It must be created with a "fresh" ZipInputStream,
 * so for each iteration over a ZIP source a new ZipDocumentBuilder instance must be created with a new ZipInputStream.
 *
 * @author Chris de Vreeze
 */
private[jvm] final class ZipDocumentBuilder(
    val zis: ZipInputStream,
    val processor: s9api.Processor,
    val reverseCatalog: SimpleCatalog)
    extends DocumentBuilder {

  type BackingDoc = SaxonDocument

  def build(): SaxonDocument = {
    val entryName: String = zis.getNextEntry().getName()
    val uri: URI = reverseCatalog.getMappedUri(URI.create(entryName)) // TODO ???

    build(uri)
  }

  def build(uri: URI): SaxonDocument = {
    // Be careful not to close the ZipInputStream
    val filteredIs: InputStream = new FilterInputStream(zis) {
      override def close() { zis.closeEntry() }
    }
    val inputSource = new InputSource(filteredIs)
    inputSource.setSystemId(uri.toString)

    val src = convertInputSourceToSource(inputSource).ensuring(_.getSystemId == uri.toString)

    val docBuilder = processor.newDocumentBuilder()

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
