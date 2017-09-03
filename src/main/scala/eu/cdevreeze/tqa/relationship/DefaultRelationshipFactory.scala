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
import eu.cdevreeze.tqa.ENames.CyclesAllowedEName
import eu.cdevreeze.tqa.ENames.IdEName
import eu.cdevreeze.tqa.ENames.OrderEName
import eu.cdevreeze.tqa.ENames.PreferredLabelEName
import eu.cdevreeze.tqa.ENames.PriorityEName
import eu.cdevreeze.tqa.ENames.RoleURIEName
import eu.cdevreeze.tqa.ENames.UseEName
import eu.cdevreeze.tqa.ENames.WeightEName
import eu.cdevreeze.tqa.ENames.XbrldtClosedEName
import eu.cdevreeze.tqa.ENames.XbrldtContextElementEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
import eu.cdevreeze.tqa.ENames.XbrldtTypedDomainRefEName
import eu.cdevreeze.tqa.ENames.XbrldtUsableEName
import eu.cdevreeze.tqa.ENames.XbrliBalanceEName
import eu.cdevreeze.tqa.ENames.XbrliPeriodTypeEName
import eu.cdevreeze.tqa.ENames.XLinkArcroleEName
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.ExtendedLink
import eu.cdevreeze.tqa.dom.LabeledXLink
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.Use
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XLinkLocator
import eu.cdevreeze.tqa.dom.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.ElemApi.anyElem

/**
 * Default extractor of relationships from a "taxonomy base".
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
final class DefaultRelationshipFactory(val config: RelationshipFactory.Config) extends RelationshipFactory {

  def extractRelationships(
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    taxonomyBase.rootElems flatMap { rootElem =>
      extractRelationshipsFromDocument(rootElem.docUri, taxonomyBase, arcFilter)
    }
  }

  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    val taxoRootElemOption = taxonomyBase.rootElemUriMap.get(docUri)

    if (taxoRootElemOption.isEmpty) {
      immutable.IndexedSeq()
    } else {
      val taxoRootElem = taxoRootElemOption.get

      val extendedLinks = taxoRootElem.findTopmostElemsOrSelfOfType(classTag[ExtendedLink])(anyElem)

      extendedLinks.flatMap(extLink => extractRelationshipsFromExtendedLink(extLink, taxonomyBase, arcFilter))
    }
  }

  def extractRelationshipsFromExtendedLink(
    extendedLink: ExtendedLink,
    taxonomyBase: TaxonomyBase,
    arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    val labeledXlinkMap = extendedLink.labeledXlinkMap

    extendedLink.arcs.filter(arcFilter) flatMap { arc =>
      extractRelationshipsFromArc(arc, labeledXlinkMap, taxonomyBase)
    }
  }

  def extractRelationshipsFromArc(
    arc: XLinkArc,
    labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
    taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship] = {

    if (config.allowMissingArcrole && arc.attributeOption(XLinkArcroleEName).isEmpty) {
      immutable.IndexedSeq()
    } else {
      val fromXLinkLabel = arc.from
      val toXLinkLabel = arc.to

      val fromXLinks = labeledXlinkMap.getOrElse(
        fromXLinkLabel,
        if (config.allowUnresolvedXLinkLabel) immutable.IndexedSeq() else sys.error(s"No locator/resource with XLink label $fromXLinkLabel. Document: ${arc.docUri}"))

      val toXLinks = labeledXlinkMap.getOrElse(
        toXLinkLabel,
        if (config.allowUnresolvedXLinkLabel) immutable.IndexedSeq() else sys.error(s"No locator/resource with XLink label $toXLinkLabel. Document: ${arc.docUri}"))

      val relationships =
        for {
          fromXLink <- fromXLinks.toIndexedSeq
          resolvedFrom <- optionallyResolve(fromXLink, taxonomyBase).toIndexedSeq
          toXLink <- toXLinks.toIndexedSeq
          resolvedTo <- optionallyResolve(toXLink, taxonomyBase).toIndexedSeq
        } yield {
          Relationship(arc, resolvedFrom, resolvedTo)
        }

      relationships
    }
  }

  def computeNetworks(
    relationships: immutable.IndexedSeq[Relationship],
    taxonomyBase: TaxonomyBase): Map[BaseSetKey, immutable.IndexedSeq[Relationship]] = {

    val baseSets = relationships.groupBy(_.arc.baseSetKey)

    baseSets.toSeq.map({
      case (baseSetKey, rels) =>
        (baseSetKey -> computeNetwork(baseSetKey, rels, taxonomyBase))
    }).toMap
  }

  def getRelationshipKey(relationship: Relationship, taxonomyBase: TaxonomyBase): RelationshipKey = {
    val nonExemptAttributes = extractNonExemptAttributeMap(relationship, taxonomyBase)

    RelationshipKey(
      relationship.arc.baseSetKey,
      relationship.resolvedFrom.xmlFragmentKey,
      relationship.resolvedTo.xmlFragmentKey,
      nonExemptAttributes)
  }

  private def optionallyResolve(
    xlink: LabeledXLink,
    taxonomyBase: TaxonomyBase): Option[ResolvedLocatorOrResource[_ <: TaxonomyElem]] = {

    xlink match {
      case res: XLinkResource =>
        Some(new ResolvedLocatorOrResource.Resource(res))
      case loc: XLinkLocator =>
        val elemUri = loc.baseUri.resolve(loc.rawHref)

        val optTaxoElem =
          Try(taxonomyBase.findElemByUri(elemUri)) match {
            case Success(result) => result
            case Failure(result) =>
              if (config.allowWrongXPointer) None else sys.error(s"Error in URI '${elemUri}'")
          }

        val optResolvedLoc = optTaxoElem.map(e => new ResolvedLocatorOrResource.Locator(loc, e))

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
    taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship] = {

    val filteredRelationships = relationships.filter(_.arc.baseSetKey == baseSetKey)

    val equivalentRelationshipsByKey =
      filteredRelationships.groupBy(rel => getRelationshipKey(rel, taxonomyBase))
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

  private def extractNonExemptAttributeMap(relationship: Relationship, taxonomyBase: TaxonomyBase): NonExemptAttributeMap = {
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
              // function taxonomyBase.findBaseTypeOrSelfUntil, and turn the attribute value into a typed one
              (attrName -> StringAttributeValue.parse(v))
          }
      }

    // If the order attribute is missing, it will now be added
    NonExemptAttributeMap.from(typedNonExemptAttrs)
  }
}

object DefaultRelationshipFactory {

  val VeryLenientInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.VeryLenient)

  val LenientInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.Lenient)

  val StrictInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.Strict)
}
