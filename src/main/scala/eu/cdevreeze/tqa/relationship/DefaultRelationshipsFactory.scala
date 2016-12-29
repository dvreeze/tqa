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
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import eu.cdevreeze.tqa.ENames.ArcroleURIEName
import eu.cdevreeze.tqa.ENames.XbrliBalanceEName
import eu.cdevreeze.tqa.ENames.XbrldtClosedEName
import eu.cdevreeze.tqa.ENames.XbrldtContextElementEName
import eu.cdevreeze.tqa.ENames.CyclesAllowedEName
import eu.cdevreeze.tqa.ENames.IdEName
import eu.cdevreeze.tqa.ENames.OrderEName
import eu.cdevreeze.tqa.ENames.XbrliPeriodTypeEName
import eu.cdevreeze.tqa.ENames.PreferredLabelEName
import eu.cdevreeze.tqa.ENames.PriorityEName
import eu.cdevreeze.tqa.ENames.RoleURIEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
import eu.cdevreeze.tqa.ENames.XbrldtTypedDomainRefEName
import eu.cdevreeze.tqa.ENames.XbrldtUsableEName
import eu.cdevreeze.tqa.ENames.UseEName
import eu.cdevreeze.tqa.ENames.WeightEName
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.ExtendedLink
import eu.cdevreeze.tqa.dom.LabeledXLink
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.Use
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XLinkLocator
import eu.cdevreeze.tqa.dom.XLinkResource
import eu.cdevreeze.yaidom.core.EName
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
    taxonomy: Taxonomy,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    taxonomy.rootElems flatMap { rootElem =>
      extractRelationshipsFromDocument(rootElem.docUri, taxonomy, arcFilter)
    }
  }

  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomy: Taxonomy,
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
    taxonomy: Taxonomy,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    extendedLink.arcs.filter(arcFilter) flatMap { arc =>
      extractRelationshipsFromArc(arc, extendedLink, extendedLink.labeledXlinkMap, taxonomy)
    }
  }

  def extractRelationshipsFromArc(
    arc: XLinkArc,
    parentExtendedLink: ExtendedLink,
    taxonomy: Taxonomy): immutable.IndexedSeq[Relationship] = {

    extractRelationshipsFromArc(arc, parentExtendedLink, parentExtendedLink.labeledXlinkMap, taxonomy)
  }

  def computeNetworks(
    relationships: immutable.IndexedSeq[Relationship],
    taxonomy: Taxonomy): Map[BaseSetKey, immutable.IndexedSeq[Relationship]] = {

    val baseSets = relationships.groupBy(_.arc.baseSetKey)

    baseSets.toSeq.map({
      case (baseSetKey, rels) =>
        (baseSetKey -> computeNetwork(baseSetKey, rels, taxonomy))
    }).toMap
  }

  def getRelationshipKey(relationship: Relationship, taxonomy: Taxonomy): RelationshipKey = {
    val nonExemptAttributes = extractNonExemptAttributeMap(relationship, taxonomy)

    RelationshipKey(
      relationship.arc.baseSetKey,
      relationship.resolvedFrom.xmlFragmentKey,
      relationship.resolvedTo.xmlFragmentKey,
      nonExemptAttributes)
  }

  private def extractRelationshipsFromArc(
    arc: XLinkArc,
    parentExtendedLink: ExtendedLink,
    labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
    taxonomy: Taxonomy): immutable.IndexedSeq[Relationship] = {

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
    taxonomy: Taxonomy): Option[ResolvedLocatorOrResource[_ <: TaxonomyElem]] = {

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

  private def computeNetwork(
    baseSetKey: BaseSetKey,
    relationships: immutable.IndexedSeq[Relationship],
    taxonomy: Taxonomy): immutable.IndexedSeq[Relationship] = {

    val filteredRelationships = relationships.filter(_.arc.baseSetKey == baseSetKey)

    val equivalentRelationshipsByKey =
      filteredRelationships.groupBy(rel => getRelationshipKey(rel, taxonomy))
    val optResolvedRelationshipsByKey =
      equivalentRelationshipsByKey.mapValues(rels => resolveProhibitionAndOverridingForEquivalentRelationships(rels))
    val resolvedRelationshipsByKey =
      optResolvedRelationshipsByKey.filter(_._2.nonEmpty).mapValues(_.head)

    filteredRelationships.filter(resolvedRelationshipsByKey.values.toSet)
  }

  /**
   * Resolves prohibition and overriding, given some equivalent relationships as parameter.
   * The returned optional relationship is one of the input relationships.
   */
  private def resolveProhibitionAndOverridingForEquivalentRelationships(
    equivalentRelationships: immutable.IndexedSeq[Relationship]): Option[Relationship] = {

    if (equivalentRelationships.isEmpty) None
    else {
      val highestPriority = equivalentRelationships.map(_.arc.priority).max

      val relationshipsWithHighestPriority =
        equivalentRelationships.filter(_.arc.priority == highestPriority)

      if (relationshipsWithHighestPriority.exists(rel => rel.arc.use == Use.Prohibited)) None
      else {
        // Pick one of these equivalent relationships with the same priority, if any
        relationshipsWithHighestPriority.headOption
      }
    }
  }

  private def extractNonExemptAttributeMap(relationship: Relationship, taxonomy: Taxonomy): NonExemptAttributeMap = {
    // TODO This does not include default and fixed attributes!

    val nonExemptAttrs: Map[EName, String] =
      relationship.arc.resolvedAttributes.toMap filterKeys { attrName =>
        attrName.namespaceUriOption != Some(XLinkNamespace) &&
          attrName != UseEName &&
          attrName != PriorityEName
      }

    val typedNonExemptAttrs: Map[EName, TypedAttributeValue] =
      nonExemptAttrs map {
        case (attrName, v) =>
          attrName match {
            case OrderEName | PriorityEName | WeightEName =>
              (attrName -> DecimalAttributeValue.parse(v))
            case IdEName | UseEName | CyclesAllowedEName | RoleURIEName | ArcroleURIEName | PreferredLabelEName =>
              (attrName -> StringAttributeValue.parse(v))
            case XbrliPeriodTypeEName | XbrliBalanceEName =>
              (attrName -> StringAttributeValue.parse(v))
            case XbrldtContextElementEName | XbrldtTargetRoleEName | XbrldtTypedDomainRefEName =>
              (attrName -> StringAttributeValue.parse(v))
            case XbrldtClosedEName | XbrldtUsableEName =>
              (attrName -> BooleanAttributeValue.parse(v))
            case _ =>
              // TODO Not correct. Instead look up the attribute declaration, its type, call
              // function taxonomy.findBaseTypeOrSelfUntil, and turn the attribute value into a typed one
              (attrName -> StringAttributeValue.parse(v))
          }
      }

    // If the order attribute is missing, it will now be added
    NonExemptAttributeMap.from(typedNonExemptAttrs)
  }
}

object DefaultRelationshipsFactory {

  val VeryLenientInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.VeryLenient)

  val LenientInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.Lenient)

  val StrictInstance = new DefaultRelationshipsFactory(RelationshipsFactory.Config.Strict)
}
