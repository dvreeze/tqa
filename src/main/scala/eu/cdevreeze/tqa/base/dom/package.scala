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

package eu.cdevreeze.tqa.base

/**
 * Type-safe XBRL '''taxonomy DOM API'''. This contains DOM-like elements in taxonomy documents. It offers the '''yaidom''' query API
 * and more, and wraps an underlying element that itself offers the yaidom query API, whatever the underlying element
 * implementation. This package does not offer any taxonomy abstractions that cross any document boundaries, except
 * for the light-weight `TaxonomyBase` abstraction at the type-safe DOM level.
 *
 * This package mainly contains:
 * <ul>
 * <li>Type [[eu.cdevreeze.tqa.base.dom.TaxonomyElem]] and its sub-types</li>
 * <li>Type [[eu.cdevreeze.tqa.base.dom.ConceptDeclaration]] and its sub-types; see below</li>
 * <li>Type [[eu.cdevreeze.tqa.base.dom.XPointer]] and its sub-types, modeling XPointer as used in XBRL</li>
 * </ul>
 *
 * This package has no knowledge about and dependency on XPath processing.
 *
 * ==Usage==
 *
 * Suppose we have an [[eu.cdevreeze.tqa.base.dom.XsdSchema]] called `schema`. Then we can find all global element declarations in
 * this schema as follows:
 *
 * {{{
 * import scala.reflect.classTag
 * import eu.cdevreeze.tqa.ENames
 * import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
 *
 * // Low level yaidom query, returning the result XML elements as TaxonomyElem elements
 * val globalElemDecls1 = schema.filterChildElems(_.resolvedName == ENames.XsElementEName)
 *
 * // Higher level yaidom query, querying for the type GlobalElementDeclaration
 * // Prefer this to the lower level yaidom query above
 * val globalElemDecls2 = schema.findAllChildElemsOfType(classTag[GlobalElementDeclaration])
 *
 * // The following query would have given the same result, because all global element declarations
 * // are child elements of the schema root. Instead of child elements, we now query for all
 * // descendant-or-self elements that are global element declarations
 * val globalElemDecls3 = schema.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration])
 *
 * // We can query the schema for global element declarations directly, so let's do that
 * val globalElemDecls4 = schema.findAllGlobalElementDeclarations
 * }}}
 *
 * Global element declarations in isolation do not know if they are (item or tuple) concept declarations. In order to
 * turn them into concept declarations, we need a `SubstitutionGroupMap` as context. For example:
 *
 * {{{
 * import eu.cdevreeze.tqa.dom.ConceptDeclaration
 *
 * // One-time creation of a ConceptDeclaration builder
 * val conceptDeclBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)
 *
 * val globalElemDecls = schema.findAllGlobalElementDeclarations
 *
 * val conceptDecls = globalElemDecls.flatMap(decl => conceptDeclBuilder.optConceptDeclaration(decl))
 * }}}
 *
 * Most TQA client code does not start with this package, however, but works with entire taxonomies instead of
 * individual type-safe DOM trees, and with relationships instead of the underlying XLink arcs.
 *
 * ==Leniency==
 *
 * See the remarks on leniency for type `TaxonomyElem` and its sub-types. This type-safe XBRL DOM model has been
 * designed to be very '''lenient on instantiation''' of the model. Therefore this TQA type-safe DOM model can also
 * be used for validating potentially erroneous taxonomy documents.
 *
 * On the other hand, if the instantiated model cannot be trusted to be schema-valid, one should be careful in
 * choosing the API calls that can safely be made on schema-invalid taxonomy content. Yaidom API level query
 * methods that return collections or options are typically safe to use on potentially schema-invalid taxonomy content.
 *
 * ==Other remarks==
 *
 * To get the model right, there are many sources to look at for inspiration. First of all, for schema content
 * there are APIs like the Xerces schema API (https://xerces.apache.org/xerces2-j/javadocs/xs/org/apache/xerces/xs/XSModel.html).
 * Also have a look at http://www.datypic.com/sc/xsd/s-xmlschema.xsd.html for the schema of XML Schema itself.
 * Moreover, there are many XBRL APIs that model (instance and) taxonomy data.
 *
 * On the other hand, this API (and the entirety of TQA) has its own design. Briefly, it starts bottom-up with yaidom,
 * and gradually offers higher level (partial) abstractions on top of that. It does not hide yaidom, however.
 *
 * TODO Support for built-in schema types, and built-in XBRL types.
 *
 * @author Chris de Vreeze
 */
package object dom
