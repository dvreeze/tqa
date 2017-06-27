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

import eu.cdevreeze.yaidom.convert
import eu.cdevreeze.yaidom.simple
import net.sf.saxon.event.ReceivingContentHandler
import net.sf.saxon.s9api.Processor
import net.sf.saxon.tree.tiny.TinyBuilder

/**
 * Converter from yaidom simple elements and documents to Saxon wrapper elements and documents.
 *
 * We exploit the fact that yaidom simple elements and documents can be excellent builders for
 * Saxon wrapper elements and documents (or builders for other element implementations), especially
 * if they are relatively small.
 *
 * @author Chris de Vreeze
 */
final class YaidomSimpleToSaxonElemConverter(val processor: Processor) {
  require(processor ne null)

  def convertSimpleDocument(doc: simple.Document): SaxonDocument = {
    // See http://saxon-xslt-and-xquery-processor.13853.n7.nabble.com/Constructing-a-tiny-tree-from-SAX-events-td5192.html.
    // The idea is that yaidom can convert a simple Document or Elem to SAX events pushed on any SAX handler, and that the
    // SAX handler used here is a Saxon ReceivingContentHandler, which uses a Saxon TinyBuilder as Saxon Receiver.

    val pipe = processor.getUnderlyingConfiguration.makePipelineConfiguration()

    val builder = new TinyBuilder(pipe)

    val receivingContentHandler = new ReceivingContentHandler()
    receivingContentHandler.setPipelineConfiguration(pipe)
    receivingContentHandler.setReceiver(builder)

    val saxConversions = new convert.YaidomToSaxEventsConversions {}

    saxConversions.convertDocument(doc)(receivingContentHandler)

    val tree = builder.getTree
    val saxonDoc = SaxonDocument.wrapDocument(tree)
    saxonDoc
  }

  def convertSimpleElem(elem: simple.Elem): SaxonElem = {
    convertSimpleDocument(simple.Document(elem)).documentElement
  }
}
