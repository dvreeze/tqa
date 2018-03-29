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

import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.simple

/**
 * Utility to convert "resolved" elements to "simple" elements.
 * A "resolved" element tree can be created using a `ResolvedElemEditor`.
 *
 * TODO This object should live in yaidom, in the utils package.
 *
 * @author Chris de Vreeze
 */
object ResolvedElemUtil {

  /**
   * Calls `indexed.Elem(convertToSimpleElem(elem, scope))`.
   */
  def convertToIndexedElem(elem: resolved.Elem, scope: Scope): indexed.Elem = {
    indexed.Elem(convertToSimpleElem(elem, scope))
  }

  /**
   * Calls `convertToSimpleNode(elem, scope).asInstanceOf[simple.Elem]`.
   */
  def convertToSimpleElem(elem: resolved.Elem, scope: Scope): simple.Elem = {
    convertToSimpleNode(elem, scope).asInstanceOf[simple.Elem]
  }

  /**
   * Converts a "resolved" node to a "simple" node, using the passed scope.
   * The passed scope is used to find prefixes for namespaces in element and attribute ENames.
   *
   * The scope must have no default namespace (so QNames without prefix will have no namespace),
   * and it must find a prefix for all namespaces used in element names and attribute names.
   */
  def convertToSimpleNode(node: resolved.Node, scope: Scope): simple.Node = {
    require(scope.defaultNamespaceOption.isEmpty, s"No default namespace allowed, but got scope $scope")

    node match {
      case resolved.Text(t) =>
        simple.Text(t, false)
      case elem: resolved.Elem =>
        val attrs =
          elem.resolvedAttributes.toIndexedSeq
            .map { case (attrEName, attrValue) => ENameUtil.attributeENameToQName(attrEName, scope) -> attrValue }
            .sortBy { case (attrQName, attrValue) => attrQName.toString }

        // Recursive calls

        val children = elem.children.map(ch => convertToSimpleNode(ch, scope))

        simple.Elem(
          ENameUtil.elementENameToQName(elem.resolvedName, scope),
          attrs,
          scope,
          children)
    }
  }
}
