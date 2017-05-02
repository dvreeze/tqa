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

package eu.cdevreeze.tqa.extension.table.taxonomy

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.extension.table.queryapi.TableRelationshipContainerLike
import eu.cdevreeze.tqa.extension.table.relationship.TableRelationship
import eu.cdevreeze.tqa.queryapi.TaxonomyApi
import eu.cdevreeze.tqa.relationship.NonStandardRelationship

/**
 * Basic implementation of a taxonomy that offers the TableRelationshipContainerApi query API, while wrapping
 * a taxonomy that offers the TaxonomyApi query API.
 *
 * @author Chris de Vreeze
 */
final class BasicTableTaxonomy private (
  val underlyingTaxonomy: TaxonomyApi,
  val tableRelationships: immutable.IndexedSeq[TableRelationship],
  val tableRelationshipsBySource: Map[XmlFragmentKey, immutable.IndexedSeq[TableRelationship]]) extends TableRelationshipContainerLike

object BasicTableTaxonomy {

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   */
  def build(underlyingTaxonomy: TaxonomyApi): BasicTableTaxonomy = {
    val nonStandardRelationships = underlyingTaxonomy.findAllNonStandardRelationshipsOfType(classTag[NonStandardRelationship])
    val tableRelationships = nonStandardRelationships.flatMap(rel => TableRelationship.opt(rel))

    val tableRelationshipsBySource = tableRelationships.groupBy(_.sourceElem.key)

    new BasicTableTaxonomy(underlyingTaxonomy, tableRelationships, tableRelationshipsBySource)
  }
}
