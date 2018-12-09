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

/**
 * API for factories of schema content elements. This object does not depend on the schema content types and companion objects.
 *
 * @author Chris de Vreeze
 */
object SchemaContentElements {

  trait Factory {

    /**
     * Schema content element type, which is a sub-type of SchemaContentElement (but this is not specified here, in order to
     * prevent circular dependencies).
     */
    type SchemaContentElementType

    /**
     * Creates a relationship of the given type from the passed parameters, if applicable,
     * and otherwise returns None.
     */
    def opt(elem: ElemInSchema): Option[SchemaContentElementType]
  }

  object Factory {

    type Aux[E] = Factory {
      type SchemaContentElementType = E
    }
  }
}
