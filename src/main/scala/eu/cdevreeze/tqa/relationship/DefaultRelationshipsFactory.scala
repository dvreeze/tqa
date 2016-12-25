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
import scala.reflect.classTag
import scala.util.Success
import scala.util.Failure
import scala.util.Try

import eu.cdevreeze.tqa.dom.ExtendedLink
import eu.cdevreeze.tqa.dom.LabeledXLink
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.UriAwareTaxonomy
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XLinkLocator
import eu.cdevreeze.tqa.dom.XLinkResource
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem

/**
 * Default extractor of relationships from a "taxonomy".
 *
 * When choosing for lenient processing, XLink arcs may be broken in that the XLink labels are corrupt, and XLink
 * locators may be broken in that the locator href URIs cannot be resolved. When choosing for strict processing,
 * XLink arcs and locators may not be broken.
 *
 * Lenient processing makes sense when the taxonomy has not yet been validated. Strict processing makes sense when
 * the taxonomy is XBRL valid, and we want to use that fact, for example when validating XBRL instances against it.
 *
 * @author Chris de Vreeze
 */
final class DefaultRelationshipsFactory(val config: RelationshipsFactory.Config) extends RelationshipsFactory {

  def extractRelationships(
    taxonomy: UriAwareTaxonomy,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    taxonomy.rootElems flatMap { rootElem =>
      extractRelationshipsFromDocument(rootElem.docUri, taxonomy, arcFilter)
    }
  }

  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomy: UriAwareTaxonomy,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    val taxoRootElemOption = taxonomy.rootElemUriMap.get(docUri)

    if (taxoRootElemOption.isEmpty) {
      immutable.IndexedSeq()
    } else {
      val taxoRootElem = taxoRootElemOption.get

      val extendedLinks = taxoRootElem.findTopmostElemsOrSelfOfType(classTag[ExtendedLink])(anyElem)

      extendedLinks.flatMap(extLink => extractRelationshipsFromExtendedLink(extLink, taxonomy, arcFilter))
    }
  }

  def extractRelationshipsFromExtendedLink(
    extendedLink: ExtendedLink,
    taxonomy: UriAwareTaxonomy,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    extendedLink.arcs.filter(arcFilter) flatMap { arc =>
      extractRelationshipsFromArc(arc, extendedLink, extendedLink.labeledXlinkMap, taxonomy)
    }
  }

  def extractRelationshipsFromArc(
    arc: XLinkArc,
    parentExtendedLink: ExtendedLink,
    taxonomy: UriAwareTaxonomy): immutable.IndexedSeq[Relationship] = {

    extractRelationshipsFromArc(arc, parentExtendedLink, parentExtendedLink.labeledXlinkMap, taxonomy)
  }

  private def extractRelationshipsFromArc(
    arc: XLinkArc,
    parentExtendedLink: ExtendedLink,
    labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
    taxonomy: UriAwareTaxonomy): immutable.IndexedSeq[Relationship] = {

    val fromXLinkLabel = arc.from
    val toXLinkLabel = arc.to

    val lenient = config.allowUnresolvedXLinkLabel

    val fromXLinks = labeledXlinkMap.getOrElse(
      fromXLinkLabel,
      if (lenient) immutable.IndexedSeq() else sys.error(s"No locator/resource with XLink label $fromXLinkLabel. Document: ${arc.docUri}"))

    val toXLinks = labeledXlinkMap.getOrElse(
      toXLinkLabel,
      if (lenient) immutable.IndexedSeq() else sys.error(s"No locator/resource with XLink label $toXLinkLabel. Document: ${arc.docUri}"))

    val relationships =
      for {
        fromXLink <- fromXLinks.toIndexedSeq
        resolvedFrom <- optionallyResolve(fromXLink, taxonomy).toIndexedSeq
        toXLink <- toXLinks.toIndexedSeq
        resolvedTo <- optionallyResolve(toXLink, taxonomy).toIndexedSeq
      } yield {
        Relationship(arc, resolvedFrom, resolvedTo)
      }

    relationships
  }

  private def optionallyResolve(
    xlink: LabeledXLink,
    taxonomy: UriAwareTaxonomy): Option[ResolvedLocatorOrResource[_ <: TaxonomyElem]] = {

    xlink match {
      case res: XLinkResource => Some(new ResolvedResource(res))
      case loc: XLinkLocator =>
        val elemUri = loc.baseUri.resolve(loc.rawHref)

        val optTaxoElem =
          Try(taxonomy.findElemByUri(elemUri)) match {
            case Success(result) => result
            case Failure(result) =>
              if (config.allowWrongXPointer) None else sys.error(s"Error in URI '${elemUri}'")
          }

        val optResolvedLoc = optTaxoElem.map(e => new ResolvedLocator(loc, e))

        if (config.allowUnresolvedLocator) {
          optResolvedLoc
        } else {
          optResolvedLoc.orElse(sys.error(s"Could not resolve locator with XLink label ${xlink.xlinkLabel}. Document: ${xlink.docUri}"))
        }
    }
  }
}

object DefaultRelationshipsFactory {

  val VeryLenientInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.VeryLenient)

  val LenientInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.Lenient)

  val StrictInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.Strict)
}
