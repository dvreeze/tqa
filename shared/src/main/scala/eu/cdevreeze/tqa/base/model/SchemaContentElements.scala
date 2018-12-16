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

package eu.cdevreeze.tqa.base.model

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.ScopedNodes

/**
 * API for factories of schema content elements. This object does not depend on the schema content types and companion objects.
 *
 * @author Chris de Vreeze
 */
object SchemaContentElements {

  trait Factory {

    /**
     * The common super-type of schema content element types. Should be SchemaContentElement in practice.
     */
    type SchemaContentElementSuperType

    /**
     * Specific schema content element type, which is a sub-type of SchemaContentElementSuperType.
     */
    type SchemaContentElementType <: SchemaContentElementSuperType

    /**
     * Creates a schema content element of the given type from the passed parameters, if applicable,
     * and returns None otherwise.
     */
    def opt(
      elem: ScopedNodes.Elem,
      ancestorENames: immutable.IndexedSeq[EName],
      targetNamespaceOption: Option[String],
      childElems: immutable.IndexedSeq[SchemaContentElementSuperType]): Option[SchemaContentElementType]
  }

  object Factory {

    type Aux[E] = Factory {
      type SchemaContentElementType = E
    }
  }
}
