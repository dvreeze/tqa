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

package eu.cdevreeze.tqa.utils

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.ExtendedLink
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.simple

/**
 * Simple taxonomy creation utility, useful for quickly creating ad-hoc test taxonomies. It does not start
 * from scratch, but expects complete taxonomy schemas and linkbases with empty extended links as starting
 * point. The functions in this utility then help further fill those linkbases. This utility certainly does
 * not add documents to the taxonomy; it only adds XLink content to already existing linkbase documents.
 *
 * @author Chris de Vreeze
 */
final class SimpleTaxonomyCreator(val startTaxonomy: BasicTaxonomy) {

  /**
   * Adds the given parent-child arcs, expecting an empty presentation link to start with.
   */
  def addParentChildArcs(docUri: URI, elr: String, arcs: immutable.IndexedSeq[SimpleTaxonomyCreator.ParentChildArc]): SimpleTaxonomyCreator = {
    // Do some validations

    validateExtendedLinkAndInterConceptArcs(docUri, elr, arcs, ENames.LinkPresentationLinkEName)

    val doc: TaxonomyDocument = startTaxonomy.taxonomyBase.taxonomyDocUriMap(docUri)
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val extLink: ExtendedLink =
      startLinkbase.findAllExtendedLinks.collect {
        case extLink: ExtendedLink if extLink.resolvedName == ENames.LinkPresentationLinkEName && extLink.role == elr => extLink
      }.last

    // Edit

    val endTaxo = addInterConceptArcs(startLinkbase, extLink, arcs, makeParentChildArc _)

    new SimpleTaxonomyCreator(endTaxo)
  }

  /**
   * Adds the given dimensional arcs, expecting an empty definition link to start with.
   */
  def addDimensionalArcs(docUri: URI, elr: String, arcs: immutable.IndexedSeq[SimpleTaxonomyCreator.DimensionalArc]): SimpleTaxonomyCreator = {
    // Do some validations

    validateExtendedLinkAndInterConceptArcs(docUri, elr, arcs, ENames.LinkDefinitionLinkEName)

    val doc: TaxonomyDocument = startTaxonomy.taxonomyBase.taxonomyDocUriMap(docUri)
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val extLink: ExtendedLink =
      startLinkbase.findAllExtendedLinks.collect {
        case extLink: ExtendedLink if extLink.resolvedName == ENames.LinkDefinitionLinkEName && extLink.role == elr => extLink
      }.last

    // Edit

    val endTaxo = addInterConceptArcs(startLinkbase, extLink, arcs, makeDimensionalArc _)

    new SimpleTaxonomyCreator(endTaxo)
  }

  // Private methods for adding (and pre-validating) any inter-concept arcs

  private def validateExtendedLinkAndInterConceptArcs(
    docUri: URI,
    elr: String,
    arcs: immutable.IndexedSeq[SimpleTaxonomyCreator.InterConceptArc],
    extLinkEName: EName): Unit = {

    val doc: TaxonomyDocument =
      startTaxonomy.taxonomyBase.taxonomyDocUriMap.getOrElse(docUri, sys.error(s"Missing document $docUri"))

    require(doc.documentElement.isInstanceOf[Linkbase], s"Not a linkbase: $docUri")
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val extLink: ExtendedLink =
      startLinkbase.findAllExtendedLinks.collect {
        case extLink: ExtendedLink if extLink.resolvedName == extLinkEName && extLink.role == elr => extLink
      }.lastOption.getOrElse(sys.error(s"Missing $extLinkEName link with ELR $elr"))

    require(extLink.findAllChildElems.isEmpty, s"Expected an empty presentation link for ELR $elr")

    val concepts: Set[EName] = arcs.flatMap(arc => List(arc.source, arc.target)).toSet
    require(
      concepts.forall(concept => startTaxonomy.findConceptDeclaration(concept).nonEmpty),
      s"Not all arc sources/targets found as concepts in the taxonomy")

    val conceptXLinkLabelMap: Map[EName, String] =
      concepts.toSeq.map(c => c -> makeXLinkLabelForLocatorToConcept(c, startTaxonomy)).toMap

    require(
      conceptXLinkLabelMap.values.toSet.size == conceptXLinkLabelMap.size,
      s"Not all concept locator XLink labels unique")

    val declaredNamespaces: Set[String] = startLinkbase.scope.withoutDefaultNamespace.namespaces
    require(
      arcs.flatMap(_.nonXLinkAttributes.keySet).distinct.filter(_.namespaceUriOption.nonEmpty)
        .forall(ename => declaredNamespaces.contains(ename.namespaceUriOption.get)),
      s"Not all needed (attribute) namespaces declared")
  }

  private def addInterConceptArcs[A <: SimpleTaxonomyCreator.InterConceptArc](
    linkbase: Linkbase,
    extendedLink: ExtendedLink,
    arcs: immutable.IndexedSeq[A],
    makeArcElem: (A, String, String) => resolved.Elem): BasicTaxonomy = {

    assert(linkbase.docUri == extendedLink.docUri)

    // Edit

    val startLinkbaseAsResolvedElem = resolved.Elem.from(linkbase)

    val extLinkPath = extendedLink.backingElem.path

    val endLinkbaseAsResolvedElem = startLinkbaseAsResolvedElem.updateElemOrSelf(extLinkPath) { oldExtLinkElem =>
      val arcElems = arcs.map { arc =>
        val sourceXLinkLabel = makeXLinkLabelForLocatorToConcept(arc.source, startTaxonomy)
        val targetXLinkLabel = makeXLinkLabelForLocatorToConcept(arc.target, startTaxonomy)

        makeArcElem(arc, sourceXLinkLabel, targetXLinkLabel)
      }

      val locatorElems = arcs.flatMap(arc => List(arc.source, arc.target)).distinct.map { concept =>
        makeLocatorToConcept(concept, startTaxonomy, linkbase.docUri)
      }

      oldExtLinkElem.plusChildren(arcElems).plusChildren(locatorElems)
    }

    // Create result

    val endLinkbaseAsSimpleElem = simple.Elem.from(endLinkbaseAsResolvedElem, linkbase.scope).prettify(2)
    val endLinkbaseAsIndexedElem = indexed.Elem(linkbase.docUri, endLinkbaseAsSimpleElem)
    val endLinkbaseDoc: TaxonomyDocument = TaxonomyDocument.build(indexed.Document(endLinkbaseAsIndexedElem))

    val endTaxonomyBase =
      TaxonomyBase.build(
        startTaxonomy.taxonomyDocs.filterNot(_.uri == linkbase.docUri) :+ endLinkbaseDoc)

    val endTaxo = BasicTaxonomy.build(
      endTaxonomyBase,
      startTaxonomy.extraSubstitutionGroupMap,
      DefaultRelationshipFactory.StrictInstance)

    endTaxo
  }

  // Private methods for creating (specific) inter-concept arcs

  private def makeInterConceptArc(
    arc: SimpleTaxonomyCreator.InterConceptArc,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    extLinkEName: EName,
    arcrole: String): resolved.Elem = {

    resolved.Node.emptyElem(extLinkEName, arc.nonXLinkAttributes)
      .plusAttribute(ENames.XLinkFromEName, sourceXLinkLabel)
      .plusAttribute(ENames.XLinkToEName, targetXLinkLabel)
      .plusAttribute(ENames.XLinkArcroleEName, arcrole)
      .plusAttribute(ENames.XLinkTypeEName, "arc")
  }

  private def makePresentationArc(
    arc: SimpleTaxonomyCreator.PresentationArc,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    arcrole: String): resolved.Elem = {

    makeInterConceptArc(arc, sourceXLinkLabel, targetXLinkLabel, ENames.LinkPresentationArcEName, arcrole)
  }

  private def makeDefinitionArc(
    arc: SimpleTaxonomyCreator.DefinitionArc,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    arcrole: String): resolved.Elem = {

    makeInterConceptArc(arc, sourceXLinkLabel, targetXLinkLabel, ENames.LinkDefinitionArcEName, arcrole)
  }

  private def makeParentChildArc(arc: SimpleTaxonomyCreator.ParentChildArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makePresentationArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://www.xbrl.org/2003/arcrole/parent-child")
  }

  private def makeDimensionalArc(arc: SimpleTaxonomyCreator.DimensionalArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    arc match {
      case arc: SimpleTaxonomyCreator.AllArc =>
        makeAllArc(arc, sourceXLinkLabel, targetXLinkLabel)
      case arc: SimpleTaxonomyCreator.NotAllArc =>
        makeNotAllArc(arc, sourceXLinkLabel, targetXLinkLabel)
      case arc: SimpleTaxonomyCreator.HypercubeDimensionArc =>
        makeHypercubeDimensionArc(arc, sourceXLinkLabel, targetXLinkLabel)
      case arc: SimpleTaxonomyCreator.DimensionDomainArc =>
        makeDimensionDomainArc(arc, sourceXLinkLabel, targetXLinkLabel)
      case arc: SimpleTaxonomyCreator.DomainMemberArc =>
        makeDomainMemberArc(arc, sourceXLinkLabel, targetXLinkLabel)
      case arc: SimpleTaxonomyCreator.DimensionDefaultArc =>
        makeDimensionDefaultArc(arc, sourceXLinkLabel, targetXLinkLabel)
    }
  }

  private def makeAllArc(arc: SimpleTaxonomyCreator.AllArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/all")
  }

  private def makeNotAllArc(arc: SimpleTaxonomyCreator.NotAllArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/notAll")
  }

  private def makeHypercubeDimensionArc(arc: SimpleTaxonomyCreator.HypercubeDimensionArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/hypercube-dimension")
  }

  private def makeDimensionDomainArc(arc: SimpleTaxonomyCreator.DimensionDomainArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/dimension-domain")
  }

  private def makeDomainMemberArc(arc: SimpleTaxonomyCreator.DomainMemberArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/domain-member")
  }

  private def makeDimensionDefaultArc(arc: SimpleTaxonomyCreator.DimensionDefaultArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(arc, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/dimension-default")
  }

  // Private helper methods for creating locators, Link labels etc.

  private def makeLocatorToConcept(concept: EName, taxo: BasicTaxonomy, baseUri: URI): resolved.Elem = {
    val conceptDecl = taxo.getConceptDeclaration(concept)

    val id = getConceptId(conceptDecl, taxo)

    val docUri = conceptDecl.globalElementDeclaration.docUri
    val absoluteElemUri = new URI(docUri.getScheme, docUri.getSchemeSpecificPart, id)
    val elemUri = makeRelativeIfApplicable(absoluteElemUri, baseUri)

    resolved.Node.emptyElem(ENames.LinkLocEName)
      .plusAttribute(ENames.XLinkHrefEName, elemUri.toString)
      .plusAttribute(ENames.XLinkLabelEName, makeXLinkLabelForLocatorToConcept(conceptDecl, taxo))
      .plusAttribute(ENames.XLinkTypeEName, "locator")
  }

  private def makeXLinkLabelForLocatorToConcept(concept: EName, taxo: BasicTaxonomy): String = {
    makeXLinkLabelForLocatorToConcept(taxo.getConceptDeclaration(concept), taxo)
  }

  private def makeXLinkLabelForLocatorToConcept(conceptDecl: ConceptDeclaration, taxo: BasicTaxonomy): String = {
    getConceptId(conceptDecl, taxo) + "_loc"
  }

  private def getConceptId(conceptDecl: ConceptDeclaration, taxo: BasicTaxonomy): String = {
    findConceptId(conceptDecl, taxo).getOrElse(sys.error(s"Missing ID for concept ${conceptDecl.targetEName}"))
  }

  private def findConceptId(conceptDecl: ConceptDeclaration, taxo: BasicTaxonomy): Option[String] = {
    conceptDecl.globalElementDeclaration.attributeOption(ENames.IdEName)
  }

  private def makeRelativeIfApplicable(uri: URI, baseUri: URI): URI = {
    if (!uri.isAbsolute || !baseUri.isAbsolute) {
      uri
    } else {
      // No NIO2, in order to target JS runtime as well

      val parentOfUri = uri.resolve("./")
      val parentOfBaseUri = baseUri.resolve("./")

      if (parentOfUri == parentOfBaseUri) {
        val uriWithoutFragment = parentOfBaseUri.relativize(uri)
        new URI(uriWithoutFragment.getScheme, uriWithoutFragment.getSchemeSpecificPart, uri.getFragment)
      } else {
        uri
      }
    }
  }
}

object SimpleTaxonomyCreator {

  def apply(startTaxonomy: BasicTaxonomy): SimpleTaxonomyCreator = {
    new SimpleTaxonomyCreator(startTaxonomy)
  }

  sealed trait StandardArc {

    def source: EName

    def nonXLinkAttributes: Map[EName, String]

    protected final def validate(): Unit = {
      require(
        nonXLinkAttributes.keySet.forall(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
        s"Expected non-XLink attributes, but got attributes ${nonXLinkAttributes.keySet}")
    }
  }

  sealed trait InterConceptArc extends StandardArc {

    def target: EName
  }

  sealed trait PresentationArc extends InterConceptArc

  sealed trait DefinitionArc extends InterConceptArc

  sealed trait DimensionalArc extends DefinitionArc {

    final def xbrldtAttributes: Map[EName, String] = {
      nonXLinkAttributes.filterKeys(_.namespaceUriOption.contains(Namespaces.XbrldtNamespace))
    }

    def withTargetRole(targetRole: String): DimensionalArc
  }

  sealed trait HasHypercubeArc extends DimensionalArc {

    def withContextElement(contextElement: String): HasHypercubeArc

    def withClosed(closed: Boolean): HasHypercubeArc
  }

  final case class ParentChildArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends PresentationArc {
    validate()
  }

  final case class AllArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends HasHypercubeArc {
    validate()

    def withTargetRole(targetRole: String): AllArc = {
      AllArc(source, target, nonXLinkAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
    }

    def withContextElement(contextElement: String): AllArc = {
      AllArc(source, target, nonXLinkAttributes + (ENames.XbrldtContextElementEName -> contextElement))
    }

    def withClosed(closed: Boolean): AllArc = {
      AllArc(source, target, nonXLinkAttributes + (ENames.XbrldtClosedEName -> closed.toString))
    }
  }

  final case class NotAllArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends HasHypercubeArc {
    validate()

    def withTargetRole(targetRole: String): NotAllArc = {
      NotAllArc(source, target, nonXLinkAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
    }

    def withContextElement(contextElement: String): NotAllArc = {
      NotAllArc(source, target, nonXLinkAttributes + (ENames.XbrldtContextElementEName -> contextElement))
    }

    def withClosed(closed: Boolean): NotAllArc = {
      NotAllArc(source, target, nonXLinkAttributes + (ENames.XbrldtClosedEName -> closed.toString))
    }
  }

  final case class HypercubeDimensionArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends DimensionalArc {
    validate()

    def withTargetRole(targetRole: String): HypercubeDimensionArc = {
      HypercubeDimensionArc(source, target, nonXLinkAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
    }
  }

  final case class DimensionDomainArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends DimensionalArc {
    validate()

    def withTargetRole(targetRole: String): DimensionDomainArc = {
      DimensionDomainArc(source, target, nonXLinkAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
    }

    def withUsable(usable: Boolean): DimensionDomainArc = {
      DimensionDomainArc(source, target, nonXLinkAttributes + (ENames.XbrldtUsableEName -> usable.toString))
    }
  }

  final case class DomainMemberArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends DimensionalArc {
    validate()

    def withTargetRole(targetRole: String): DomainMemberArc = {
      DomainMemberArc(source, target, nonXLinkAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
    }

    def withUsable(usable: Boolean): DomainMemberArc = {
      DomainMemberArc(source, target, nonXLinkAttributes + (ENames.XbrldtUsableEName -> usable.toString))
    }
  }

  final case class DimensionDefaultArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) extends DimensionalArc {
    validate()

    def withTargetRole(targetRole: String): DimensionDefaultArc = {
      // A no-op
      this
    }
  }
}
