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

package eu.cdevreeze.tqa.taxonomy

import scala.collection.immutable

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.relationship.Relationship

/**
 * Basic implementation of a taxonomy that offers the TaxonomyApi query API. It does not enforce closure
 * under DTS discovery rules, or uniqueness of "target expanded names" of concept declarations etc.
 * It does not know anything about tables and formulas. It also does not know anything about networks
 * of relationships.
 *
 * @author Chris de Vreeze
 */
final class BasicTaxonomy(
  underlyingTaxo: Taxonomy,
  substitutionGroupMap: SubstitutionGroupMap,
  relationships: immutable.IndexedSeq[Relationship]) extends AbstractTaxonomy(underlyingTaxo, substitutionGroupMap, relationships)
