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

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope

/**
 * Utility to convert any EName to a QName, using a Scope.
 *
 * TODO This object should live in yaidom, in the utils package.
 *
 * @author Chris de Vreeze
 */
object ENameUtil {

  /**
   * Converts an EName to a QName, using the passed scope.
   *
   * The scope must have no default namespace (so a created QName without prefix will have no namespace),
   * and it must find a prefix for the namespaces used in the EName.
   */
  def elementENameToQName(ename: EName, scope: Scope): QName = {
    require(scope.defaultNamespaceOption.isEmpty, s"No default namespace allowed, but got scope $scope")

    // TODO Use QNameProvider

    ename.namespaceUriOption match {
      case None =>
        QName(ename.localPart)
      case Some(ns) =>
        val prefix = scope.prefixForNamespace(ns, () => sys.error(s"No prefix found for namespace '$ns'"))
        QName(prefix, ename.localPart)
    }
  }

  /**
   * Calls `elementNameToQName(ename, scope)`, knowing that there is no default namespace.
   */
  def attributeENameToQName(ename: EName, scope: Scope): QName = {
    require(scope.defaultNamespaceOption.isEmpty, s"No default namespace allowed, but got scope $scope")

    // TODO Use QNameProvider

    elementENameToQName(ename, scope)
  }
}
