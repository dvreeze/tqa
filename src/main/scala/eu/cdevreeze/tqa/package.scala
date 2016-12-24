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

package eu.cdevreeze

/**
 * Root package of the Taxonomy Query API. This package itself contains commonly used data classes and many constants
 * for namespaces and expanded names.
 *
 * There are 3 levels of abstraction in TQA. The lowest layer is the type-safe taxonomy DOM model. It uses yaidom for its
 * "XML dialect support", where the XML dialect is XBRL taxonomy data. It knows only about individual DOM trees.
 *
 * On top of the type-safe DOM layer is the relationship layer. It resolves the arcs of the DOM layer as relationships.
 * Of course, to resolve arcs we need other documents as context.
 *
 * On top of the relationship layer is the taxonomy query API layer. It uses the underlying layers to offer a query
 * API in which taxonomy elements (such as concept declarations) and relationships can easily be queried. This layer
 * is the purpose of TQA.
 *
 * @author Chris de Vreeze
 */
package object tqa
