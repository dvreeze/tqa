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
 * TQA bootstrapping.
 *
 * First of all, bootstrapping needs an URI converter, that converts original (http or https) URIs to
 * local file system URIs. Next, we need that URI converter and a Saxon or yaidom document parser to
 * configure a DocumentBuilder. Next we need a discovery strategy for obtaining the root elements of
 * the taxonomy. This is typically DTS discovery (the details of which can be somewhat tweaked).
 * Finally we need a RelationshipFactory (and maybe an arc filter) to create a BasicTaxonomy.
 *
 * Once a BasicTaxonomy is created, it can be used as basis for wrapper taxonomy objects that know
 * about networks of relationships, tables/formulas, etc.
 *
 * A typical URI converter originates from a catalog file, as restricted in the Taxonomy Packages specification.
 * Typical discovery strategies ("taxonomy root element collectors") use taxonomy packages, and typically
 * they are backed by thread-safe (Google Guava) root element caches.
 *
 * The bootstrapping process is inherently flexible in supporting the loading of more or less broken
 * taxonomies. For example, backing element builders can be made to post-process broken input before
 * the taxonomy DOM is instantiated. As another example, relationships resolution can be as lenient as
 * desired.
 *
 * This package unidirectionally depends on the [[eu.cdevreeze.tqa.taxonomy]], [[eu.cdevreeze.tqa.queryapi]],
 * [[eu.cdevreeze.tqa.relationship]] and [[eu.cdevreeze.tqa.dom]] packages.
 *
 * @author Chris de Vreeze
 */
package object taxonomybuilder
