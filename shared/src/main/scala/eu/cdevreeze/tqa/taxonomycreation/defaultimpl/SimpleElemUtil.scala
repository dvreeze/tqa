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

package eu.cdevreeze.tqa.taxonomycreation.defaultimpl

import eu.cdevreeze.yaidom.queryapi.Nodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemNodeApi
import eu.cdevreeze.yaidom.simple

/**
 * Utility to convert any element to a "simple" element.
 *
 * TODO This object should live in yaidom, in the utils package. Better even is to have an apply factory
 * method taking any ScopedElemNodeApi, analogous to resolved.Elem.
 *
 * @author Chris de Vreeze
 */
object SimpleElemUtil {

  /**
   * Calls `convertToSimpleNode(elem).asInstanceOf[simple.Elem]`.
   */
  def convertToSimpleElem(elem: ScopedElemNodeApi with Nodes.Elem): simple.Elem = {
    convertToSimpleNode(elem).asInstanceOf[simple.Elem]
  }

  /**
   * Converts any node to a "simple" node.
   *
   * The scope must have no default namespace (so QNames without prefix will have no namespace),
   * and it must find a prefix for all namespaces used in element names and attribute names.
   */
  def convertToSimpleNode(node: Nodes.Node): simple.Node = {
    node match {
      case t: Nodes.Text =>
        simple.Text(t.text, false)
      case c: Nodes.Comment =>
        simple.Comment(c.text)
      case pi: Nodes.ProcessingInstruction =>
        simple.ProcessingInstruction(pi.target, pi.data)
      case er: Nodes.EntityRef =>
        simple.EntityRef(er.entity)
      case elem: Nodes.Elem with ScopedElemNodeApi =>
        // Recursive calls

        val children = elem.children.map(ch => convertToSimpleNode(ch.asInstanceOf[Nodes.Node]))

        simple.Elem(
          elem.qname,
          elem.attributes.toIndexedSeq,
          elem.scope,
          children)
    }
  }
}
