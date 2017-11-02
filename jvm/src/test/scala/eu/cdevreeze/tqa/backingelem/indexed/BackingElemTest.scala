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

package eu.cdevreeze.tqa.backingelem.indexed

import scala.collection.immutable

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.backingelem.AbstractBackingElemTest
import eu.cdevreeze.yaidom.indexed.Document
import eu.cdevreeze.yaidom.indexed.Elem
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.queryapi.Nodes

/**
 * Backing element test for yaidom indexed Elems.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class BackingElemTest extends AbstractBackingElemTest {

  val docElem: E = {
    val docUri = classOf[AbstractBackingElemTest].getResource("some-data.xsd").toURI
    val docParser = DocumentParserUsingStax.newInstance()
    val doc = docParser.parse(docUri).withUriOption(Some(docUri))
    Document(doc).documentElement
  }

  def getChildren(elem: E): immutable.IndexedSeq[Nodes.Node] = {
    // Cheating a little bit here.
    elem.asInstanceOf[Elem].underlyingElem.children
  }
}
