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
 * Taxonomy classes, containing type-safe DOM trees, and mixing in taxonomy query API traits. In particular,
 * the [[eu.cdevreeze.tqa.base.queryapi.TaxonomyApi]] trait is mixed in as taxonomy query API. See package
 * [[eu.cdevreeze.tqa.base.queryapi]] for more information about how to query XBRL taxonomy content.
 *
 * The term taxonomy is used here in a very general sense, namely as a collection of taxonomy documents.
 *
 * Various scenarios are supported. Taxonomies that are not closed (and not validated in any way) must be supported
 * in order for TQA to be useful for taxonomy validation. Closed taxonomies are supported for reliable taxonomy
 * querying. Taxonomies that model networks of relationships are also supported. Specific taxonomies knowing about
 * formulas and/or tables are also supported. Extension taxonomies are also supported.
 *
 * Some important operations on taxonomies are prohibition/overriding resolution (to find networks of relationships),
 * combining taxonomies (for building extension taxonomies, for example), filtering relationships (to ignore relationships
 * that we are not interested in).
 *
 * Each taxonomy class has at least the following state (directly or indirectly): a collection of taxonomy DOM root
 * elements, and a collection of relationships. The underlying arcs, locators and resources of those relationships
 * must exist in the collection of taxonomy DOM trees, or else the taxonomy is corrupt.
 *
 * TQA has no knowledge about XPath, so any XPath in taxonomies is just text, as far as TQA is concerned.
 *
 * This package unidirectionally depends on the [[eu.cdevreeze.tqa.base.queryapi]], [[eu.cdevreeze.tqa.base.relationship]] and
 * [[eu.cdevreeze.tqa.base.dom]] packages.
 *
 * @author Chris de Vreeze
 */
package object taxonomy
