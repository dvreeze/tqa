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
 * TQA bootstrapping. It works both in the JVM and in JavaScript runtime environments.
 *
 * First of all, bootstrapping needs a DocumentBuilder. Next we need a discovery strategy for obtaining
 * the root elements of the taxonomy, as DocumentCollector. This is typically DTS discovery (the details
 * of which can be somewhat tweaked). Finally we need a RelationshipFactory (and maybe an arc filter) to
 * create a BasicTaxonomy.
 *
 * Once a BasicTaxonomy is created, it can be used as basis for wrapper taxonomy objects that know
 * about networks of relationships, tables/formulas, etc.
 *
 * The DocumentCollector and DocumentBuilder abstractions play well with XBRL Taxonomy Packages.
 *
 * Specific DocumentCollectors and DocumentBuilders can be backed by thread-safe (Google Guava) caches in order
 * to prevent re-computations of the same data.
 *
 * The bootstrapping process is inherently flexible in supporting the loading of more or less broken
 * taxonomies. For example, backing element builders can be made to post-process broken input before
 * the taxonomy DOM is instantiated. As another example, relationships resolution can be as lenient as
 * desired.
 *
 * This package unidirectionally depends on the [[eu.cdevreeze.tqa.base.taxonomy]], [[eu.cdevreeze.tqa.base.queryapi]],
 * [[eu.cdevreeze.tqa.base.relationship]] and [[eu.cdevreeze.tqa.base.dom]] packages.
 *
 * @author Chris de Vreeze
 */
package object taxonomybuilder
