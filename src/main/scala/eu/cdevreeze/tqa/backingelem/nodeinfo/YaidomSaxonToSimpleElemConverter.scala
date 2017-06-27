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

import eu.cdevreeze.yaidom.parse.DefaultElemProducingSaxHandler
import eu.cdevreeze.yaidom.simple
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXResult
import net.jcip.annotations.NotThreadSafe

/**
 * Converter from yaidom Saxon wrapper elements and documents to yaidom simple elements and documents.
 * It is implemented by outputting SAX events to a yaidom DefaultElemProducingSaxHandler.
 *
 * @author Chris de Vreeze
 */
@NotThreadSafe
final class YaidomSaxonToSimpleElemConverter(val transformer: Transformer) {
  require(transformer ne null)

  def convertSaxonDocument(doc: SaxonDocument): simple.Document = {
    val saxonSource: Source = doc.wrappedTreeInfo

    val elemProducingContentHandler = new DefaultElemProducingSaxHandler {}

    transformer.transform(saxonSource, new SAXResult(elemProducingContentHandler))

    val resultDoc = elemProducingContentHandler.resultingDocument.withUriOption(doc.uriOption)
    resultDoc
  }

  def convertSaxonElem(elem: SaxonElem): simple.Elem = {
    val saxonSource: Source = elem.wrappedNode

    val elemProducingContentHandler = new DefaultElemProducingSaxHandler {}

    transformer.transform(saxonSource, new SAXResult(elemProducingContentHandler))

    val resultElem = elemProducingContentHandler.resultingElem
    resultElem
  }
}
