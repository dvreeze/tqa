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

  /**
   * The configuration used by this RelationshipFactory.
   */
  def config: RelationshipFactory.Config

  /**
   * Returns all relationships in the given `TaxonomyBase` passing the provided arc filter.
   */
  def extractRelationships(
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  /**
   * Returns all relationships in the given document in the given `TaxonomyBase` passing the provided arc filter.
   */
  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  /**
   * Returns all relationships in the given extended link in the given `TaxonomyBase` passing the provided arc filter.
   */
  def extractRelationshipsFromExtendedLink(
    extendedLink: ExtendedLink,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship]

  /**
   * Returns all relationships (typically one) having the given underlying XLink arc in the given `TaxonomyBase`.
   * For performance a mapping from XLink labels to XLink locators and resources must be provided, and this mapping
   * should be computed only once per extended link.
   *
   * This method must respect the configuration of this RelationshipFactory.
   */
  def extractRelationshipsFromArc(
    arc: XLinkArc,
    labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
    taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship]

  /**
   * Returns the networks of relationships, computed from the given collection of relationships. The `TaxonomyBase`
   * is passed as second parameter.
   */
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

  /**
   * Configuration object used by a RelationshipFactory. It says to what extent the RelationshipFactory
   * using it allows syntax errors in XLink arcs and locators, as well as "dead" locator href URIs.
   *
   * @constructor create a new configuration from the passed boolean flags
   * @param allowMissingArcrole if true, allows missing arcrole attribute in any arc
   * @param allowWrongXPointer if true, allows (found) XPointer syntax error in any locator href URI fragment
   * @param allowUnresolvedXLinkLabel if true, allows "dead" arc XLink "from" or "to" attributes within any extended link
   * @param allowUnresolvedLocator if true, allows "dead" locator href URIs within the taxonomy
   */
  final case class Config(
    val allowMissingArcrole: Boolean,
    val allowWrongXPointer: Boolean,
    val allowUnresolvedXLinkLabel: Boolean,
    val allowUnresolvedLocator: Boolean)

  object Config {

    /**
     * Accepts unresolved locators but also missing arcroles, XPointer syntax errors and broken XLink labels (in XLink arcs).
     * Such erroneous locators and arcs are silently skipped.
     */
    val VeryLenient = Config(true, true, true, true)

    /**
     * Accepts unresolved locators but does not accept any (found) XPointer syntax errors or broken XLink labels or missing arcroles.
     * Such unresolved locators are silently skipped.
     */
    val Lenient = Config(false, false, false, true)

    /**
     * Does not accept any unresolved locators or syntax errors (in XPointer), missing arcroles or broken XLink labels.
     * Exceptions will be thrown instead.
     */
    val Strict = Config(false, false, false, false)
  }
}
