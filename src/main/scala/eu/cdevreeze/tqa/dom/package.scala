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

package eu.cdevreeze.tqa

/**
 * Type-safe XBRL taxonomy DOM API. This contains DOM-like elements in taxonomy documents, as well as collections
 * of documents containing such type-safe elements ("taxonomies"). It offers the yaidom query API and more, and
 * wraps an underlying element that itself offers the yaidom query API, whatever the underlying element implementation.
 *
 * To get the model right, there are many sources to look at for inspiration. First of all, for schema content
 * there are APIs like the Xerces schema API (https://xerces.apache.org/xerces2-j/javadocs/xs/org/apache/xerces/xs/XSModel.html).
 * Also have a look at http://www.datypic.com/sc/xsd/s-xmlschema.xsd.html for the schema of XML Schema itself.
 * Next, there are many XBRL APIs that model (instance and) taxonomy data.
 *
 * @author Chris de Vreeze
 */
package object dom
