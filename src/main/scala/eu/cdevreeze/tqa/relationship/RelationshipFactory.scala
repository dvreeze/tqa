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

package eu.cdevreeze.tqa.relationship

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.ExtendedLink
import eu.cdevreeze.tqa.dom.LabeledXLink
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.XLinkArc

/**
 * Extractor of relationships from a "taxonomy base".
 *
 * @author Chris de Vreeze
 */
trait RelationshipFactory {

  def extractRelationships(
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  def extractRelationshipsFromExtendedLink(
    extendedLink: ExtendedLink,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  def extractRelationshipsFromArc(
    arc: XLinkArc,
    labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
    taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship]

  def computeNetworks(
    relationships: immutable.IndexedSeq[Relationship],
    taxonomyBase: TaxonomyBase): Map[BaseSetKey, immutable.IndexedSeq[Relationship]]

  /**
   * Gets the key of the relationship that is the same (only) for equivalent relationships.
   */
  def getRelationshipKey(relationship: Relationship, taxonomyBase: TaxonomyBase): RelationshipKey
}

object RelationshipFactory {

  val AnyArc: XLinkArc => Boolean = (_ => true)

  final case class Config(
    val allowUnresolvedXLinkLabel: Boolean,
    val allowWrongXPointer: Boolean,
    val allowUnresolvedLocator: Boolean)

  object Config {

    /**
     * Accepts unresolved locators but also XPointer syntax errors and broken XLink labels (in XLink arcs).
     * Such erroneous locators and arcs are silently skipped.
     */
    val VeryLenient = Config(true, true, true)

    /**
     * Accepts unresolved locators but does not accept any (found) XPointer syntax errors or broken XLink labels.
     * Such unresolved locators are silently skipped.
     */
    val Lenient = Config(false, false, true)

    /**
     * Does not accept any unresolved locators or syntax errors (in XPointer) or broken XLink labels.
     * Exceptions will be thrown instead.
     */
    val Strict = Config(false, false, false)
  }
}