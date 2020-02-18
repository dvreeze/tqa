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

package eu.cdevreeze.tqa.base.relationship

import java.net.URI

import scala.collection.immutable
import scala.collection.compat._
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
import eu.cdevreeze.tqa.ENames.XLinkArcroleEName
import eu.cdevreeze.tqa.ENames.XbrldtClosedEName
import eu.cdevreeze.tqa.ENames.XbrldtContextElementEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
import eu.cdevreeze.tqa.ENames.XbrldtTypedDomainRefEName
import eu.cdevreeze.tqa.ENames.XbrldtUsableEName
import eu.cdevreeze.tqa.ENames.XbrliBalanceEName
import eu.cdevreeze.tqa.ENames.XbrliPeriodTypeEName
import eu.cdevreeze.tqa.Namespaces.XLinkNamespace
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.base.dom.ExtendedLink
import eu.cdevreeze.tqa.base.dom.LabeledXLink
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.dom.XLinkLocator
import eu.cdevreeze.tqa.base.dom.XLinkResource
import eu.cdevreeze.tqa.common.schematypes.BuiltInSchemaTypes
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.XmlBaseSupport

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

    taxonomyBase.rootElems.flatMap { rootElem =>
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

      val extendedLinks = taxoRootElem.findTopmostElemsOrSelfOfType(classTag[ExtendedLink])(_ => true)

      extendedLinks.flatMap(extLink => extractRelationshipsFromExtendedLink(extLink, taxonomyBase, arcFilter))
    }
  }

  def extractRelationshipsFromExtendedLink(
      extendedLink: ExtendedLink,
      taxonomyBase: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): immutable.IndexedSeq[Relationship] = {

    val labeledXlinkMap = extendedLink.labeledXlinkMap
    val extLinkBaseUriOption = extendedLink.baseUriOption

    extendedLink.arcs.filter(arcFilter) flatMap { arc =>
      extractRelationshipsFromArc(arc, labeledXlinkMap, extLinkBaseUriOption, taxonomyBase)
    }
  }

  def extractRelationshipsFromArc(
      arc: XLinkArc,
      labeledXlinkMap: Map[String, immutable.IndexedSeq[LabeledXLink]],
      parentBaseUriOption: Option[URI],
      taxonomyBase: TaxonomyBase): immutable.IndexedSeq[Relationship] = {

    if (config.allowMissingArcrole && arc.attributeOption(XLinkArcroleEName).isEmpty) {
      immutable.IndexedSeq()
    } else {
      val fromXLinkLabel = arc.from
      val toXLinkLabel = arc.to

      val fromXLinks = labeledXlinkMap.getOrElse(
        fromXLinkLabel,
        if (config.allowUnresolvedXLinkLabel) {
          immutable.IndexedSeq()
        } else {
          sys.error(s"No locator/resource with XLink label $fromXLinkLabel. Document: ${arc.docUri}")
        }
      )

      val toXLinks = labeledXlinkMap.getOrElse(
        toXLinkLabel,
        if (config.allowUnresolvedXLinkLabel) {
          immutable.IndexedSeq()
        } else {
          sys.error(s"No locator/resource with XLink label $toXLinkLabel. Document: ${arc.docUri}")
        }
      )

      val relationships =
        for {
          fromXLink <- fromXLinks
          resolvedFrom <- optionallyResolve(fromXLink, parentBaseUriOption, taxonomyBase).toIndexedSeq
          toXLink <- toXLinks
          resolvedTo <- optionallyResolve(toXLink, parentBaseUriOption, taxonomyBase).toIndexedSeq
        } yield {
          Relationship(arc, resolvedFrom, resolvedTo)
        }

      relationships
    }
  }

  def computeNetworks(
      relationships: immutable.IndexedSeq[Relationship],
      taxonomyBase: TaxonomyBase): Map[BaseSetKey, RelationshipFactory.NetworkComputationResult] = {

    val baseSets = relationships.groupBy(_.baseSetKey)

    baseSets.toSeq
      .map({
        case (baseSetKey, rels) =>
          (baseSetKey -> computeNetwork(baseSetKey, rels, taxonomyBase))
      })
      .toMap
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
      parentBaseUriOption: Option[URI],
      taxonomyBase: TaxonomyBase): Option[ResolvedLocatorOrResource[_ <: TaxonomyElem]] = {

    xlink match {
      case res: XLinkResource =>
        Some(new ResolvedLocatorOrResource.Resource(res))
      case loc: XLinkLocator =>
        // Faster than loc.baseUri in general, which counts, because this method must be very fast

        val baseUri: URI =
          XmlBaseSupport
            .findBaseUriByParentBaseUri(parentBaseUriOption, loc)(XmlBaseSupport.JdkUriResolver)
            .getOrElse(DefaultRelationshipFactory.EmptyUri)

        val elemUri: URI = {
          val uri = baseUri.resolve(loc.rawHref)

          // See the XBRL conformance suite, test 321, variation V-01, with the Spanish characters in a locator.
          // The issue is that URI.getFragment may return the same fragment, but that does not make 2 otherwise equal
          // URIs equal. A workaround is to create a new URI from the one with the Spanish character in the fragment,
          // like is done below.

          if (uri.getFragment == uri.getRawFragment) {
            uri
          } else {
            new URI(uri.getScheme, uri.getSchemeSpecificPart, uri.getFragment)
          }
        }

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
          optResolvedLoc.orElse(
            sys.error(s"Could not resolve locator with XLink label ${xlink.xlinkLabel}. Document: ${xlink.docUri}"))
        }
    }
  }

  private def computeNetwork(
      baseSetKey: BaseSetKey,
      relationships: immutable.IndexedSeq[Relationship],
      taxonomyBase: TaxonomyBase): RelationshipFactory.NetworkComputationResult = {

    val filteredRelationships = relationships.filter(_.baseSetKey == baseSetKey)

    val equivalentRelationshipsByKey =
      filteredRelationships.groupBy(rel => getRelationshipKey(rel, taxonomyBase))
    val optResolvedRelationshipsByKey =
      equivalentRelationshipsByKey.view
        .mapValues(rels => resolveProhibitionAndOverridingForEquivalentRelationships(rels))
        .toMap
    val resolvedRelationshipsByKey =
      optResolvedRelationshipsByKey.filter(_._2.nonEmpty).view.mapValues(_.head).toMap

    val result = filteredRelationships.partition(resolvedRelationshipsByKey.values.toSet)
    RelationshipFactory.NetworkComputationResult(result._1, result._2)
  }

  /**
   * Resolves prohibition and overriding, given some equivalent relationships as parameter.
   * The returned optional relationship is one of the input relationships.
   */
  private def resolveProhibitionAndOverridingForEquivalentRelationships(
      equivalentRelationships: immutable.IndexedSeq[Relationship]): Option[Relationship] = {

    if (equivalentRelationships.isEmpty) {
      None
    } else {
      val highestPriority = equivalentRelationships.map(_.arc.priority).max

      val relationshipsWithHighestPriority =
        equivalentRelationships.filter(_.arc.priority == highestPriority)

      if (relationshipsWithHighestPriority.exists(rel => rel.arc.use == Use.Prohibited)) {
        None
      } else {
        // Pick one of these equivalent relationships with the same priority, if any
        relationshipsWithHighestPriority.headOption
      }
    }
  }

  private def extractNonExemptAttributeMap(
      relationship: Relationship,
      taxonomyBase: TaxonomyBase): NonExemptAttributeMap = {
    // TODO This does not include default and fixed attributes!

    val nonExemptAttrs: Map[EName, String] =
      relationship.arc.resolvedAttributes.toMap.filter {
        case (attrName, _) =>
          attrName.namespaceUriOption != Some(XLinkNamespace) &&
            attrName != UseEName &&
            attrName != PriorityEName
      }

    val typedNonExemptAttrs: Map[EName, TypedAttributeValue] =
      nonExemptAttrs.map {
        case (attrName, v) =>
          (attrName -> getTypedAttributeValue(attrName, v, taxonomyBase))
      }

    // If the order attribute is missing, it will now be added
    NonExemptAttributeMap.from(typedNonExemptAttrs)
  }

  private def getTypedAttributeValue(
      attrName: EName,
      attrValueAsString: String,
      taxonomyBase: TaxonomyBase): TypedAttributeValue = {

    attrName match {
      case OrderEName | PriorityEName | WeightEName =>
        DecimalAttributeValue.parse(attrValueAsString)
      case IdEName | UseEName | CyclesAllowedEName | RoleURIEName | ArcroleURIEName | PreferredLabelEName =>
        StringAttributeValue.parse(attrValueAsString)
      case XbrliPeriodTypeEName | XbrliBalanceEName =>
        StringAttributeValue.parse(attrValueAsString)
      case XbrldtContextElementEName | XbrldtTargetRoleEName | XbrldtTypedDomainRefEName =>
        StringAttributeValue.parse(attrValueAsString)
      case XbrldtClosedEName | XbrldtUsableEName =>
        BooleanAttributeValue.parse(attrValueAsString)
      case _ =>
        // Reasonably fast, but far from complete! For example, it is assumed that there is a global attribute declaration,
        // or else no schema type can be determined.

        val attrTypeOption: Option[EName] =
          taxonomyBase.findGlobalAttributeDeclarationByEName(attrName).flatMap(_.typeOption)

        attrTypeOption match {
          case Some(tp) if BuiltInSchemaTypes.isBuiltInFloatType(tp)   => FloatAttributeValue.parse(attrValueAsString)
          case Some(tp) if BuiltInSchemaTypes.isBuiltInDoubleType(tp)  => DoubleAttributeValue.parse(attrValueAsString)
          case Some(tp) if BuiltInSchemaTypes.isBuiltInDecimalType(tp) => DecimalAttributeValue.parse(attrValueAsString)
          case Some(tp) if BuiltInSchemaTypes.isBuiltInBooleanType(tp) => BooleanAttributeValue.parse(attrValueAsString)
          case _                                                       => StringAttributeValue.parse(attrValueAsString)
        }
    }
  }
}

object DefaultRelationshipFactory {

  val VeryLenientInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.VeryLenient)

  val LenientInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.Lenient)

  val StrictInstance = new DefaultRelationshipFactory(RelationshipFactory.Config.Strict)

  private val EmptyUri = URI.create("")
}
