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

package eu.cdevreeze.tqa.base.relationship.jvm

import java.net.URI

import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.dom.ExtendedLink
import eu.cdevreeze.tqa.base.dom.LabeledXLink
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.Relationship
import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.relationship.RelationshipKey

import scala.collection.immutable
import scala.collection.parallel.CollectionConverters._

/**
 * Default extractor of relationships from a "taxonomy base" that exploits parallelism. It is mostly like class
 * DefaultRelationshipFactory, except that relationship extraction for multiple documents exploits parallelism.
 *
 * When choosing for lenient processing, XLink arcs may be broken in that the XLink labels are corrupt, and XLink
 * locators may be broken in that the locator href URIs cannot be resolved. When choosing for strict processing,
 * XLink arcs and locators may not be broken.
 *
 * Lenient processing makes sense when the taxonomy base has not yet been validated. Strict processing makes sense when
 * the taxonomy is XBRL valid, and we want to use that fact, for example when validating XBRL instances against it.
 *
 * @author Chris de Vreeze
 */
final class DefaultParallelRelationshipFactory(val config: RelationshipFactory.Config) extends RelationshipFactory {

  private val defaultRepationshipFactory: DefaultRelationshipFactory = new DefaultRelationshipFactory(config)

  def extractRelationships(
      taxonomyBase: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    taxonomyBase.rootElems.par.flatMap { rootElem =>
      extractRelationshipsFromDocument(rootElem.docUri, taxonomyBase, arcFilter)
    }.seq.toIndexedSeq
  }

  def extractRelationshipsFromDocument(
      docUri: URI,
      taxonomyBase: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    defaultRepationshipFactory.extractRelationshipsFromDocument(docUri, taxonomyBase, arcFilter)
  }

  def extractRelationshipsFromExtendedLink(
      extendedLink: ExtendedLink,
      taxonomyBase: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    defaultRepationshipFactory.extractRelationshipsFromExtendedLink(extendedLink, taxonomyBase, arcFilter)
  }

  def extractRelationshipsFromArc(
      arc: XLinkArc,
      labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
      parentBaseUriOption: Option[URI],
      taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship] = {

    defaultRepationshipFactory.extractRelationshipsFromArc(arc, labeledXlinkMap, parentBaseUriOption, taxonomyBase)
  }

  def computeNetworks(
      relationships: immutable.IndexedSeq[Relationship],
      taxonomyBase: TaxonomyBase): Map[BaseSetKey, RelationshipFactory.NetworkComputationResult] = {

    defaultRepationshipFactory.computeNetworks(relationships, taxonomyBase)
  }

  def getRelationshipKey(relationship: Relationship, taxonomyBase: TaxonomyBase): RelationshipKey = {
    defaultRepationshipFactory.getRelationshipKey(relationship, taxonomyBase)
  }
}

object DefaultParallelRelationshipFactory {

  val VeryLenientInstance = new DefaultParallelRelationshipFactory(RelationshipFactory.Config.VeryLenient)

  val LenientInstance = new DefaultParallelRelationshipFactory(RelationshipFactory.Config.Lenient)

  val StrictInstance = new DefaultParallelRelationshipFactory(RelationshipFactory.Config.Strict)
}
