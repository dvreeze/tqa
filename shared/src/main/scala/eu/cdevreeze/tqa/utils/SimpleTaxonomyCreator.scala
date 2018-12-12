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
import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.ExtendedLink
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.model
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
   * Adds the given parent-child relationships, expecting an empty presentation link to start with.
   */
  def addParentChildArcs(docUri: URI, elr: String, relationships: immutable.IndexedSeq[model.ParentChildRelationship]): SimpleTaxonomyCreator = {
    // Do some validations

    validateExtendedLinkAndInterConceptArcs(docUri, elr, relationships, ENames.LinkPresentationLinkEName)

    val doc: TaxonomyDocument = startTaxonomy.taxonomyBase.taxonomyDocUriMap(docUri)
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val extLink: ExtendedLink =
      startLinkbase.findAllExtendedLinks.collect {
        case extLink: ExtendedLink if extLink.resolvedName == ENames.LinkPresentationLinkEName && extLink.role == elr => extLink
      }.last

    // Edit

    val endTaxo = addInterConceptArcs(startLinkbase, extLink, relationships, makeParentChildArc _)

    new SimpleTaxonomyCreator(endTaxo)
  }

  /**
   * Adds the given dimensional relationships, expecting an empty definition link to start with.
   */
  def addDimensionalArcs(docUri: URI, elr: String, relationships: immutable.IndexedSeq[model.DimensionalRelationship]): SimpleTaxonomyCreator = {
    // Do some validations

    validateExtendedLinkAndInterConceptArcs(docUri, elr, relationships, ENames.LinkDefinitionLinkEName)

    val doc: TaxonomyDocument = startTaxonomy.taxonomyBase.taxonomyDocUriMap(docUri)
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val extLink: ExtendedLink =
      startLinkbase.findAllExtendedLinks.collect {
        case extLink: ExtendedLink if extLink.resolvedName == ENames.LinkDefinitionLinkEName && extLink.role == elr => extLink
      }.last

    // Edit

    val endTaxo = addInterConceptArcs(startLinkbase, extLink, relationships, makeDimensionalArc _)

    new SimpleTaxonomyCreator(endTaxo)
  }

  // Private methods for adding (and pre-validating) any inter-concept relationships

  private def validateExtendedLinkAndInterConceptArcs(
    docUri: URI,
    elr: String,
    relationships: immutable.IndexedSeq[model.InterConceptRelationship],
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

    val concepts: Set[EName] =
      relationships.flatMap { relationship =>
        List(
          relationship.source.asInstanceOf[model.Node.GlobalElementDecl].targetEName,
          relationship.target.asInstanceOf[model.Node.GlobalElementDecl].targetEName)
      }.toSet

    require(
      concepts.forall(concept => startTaxonomy.findConceptDeclaration(concept).nonEmpty),
      s"Not all relationship sources/targets found as concepts in the taxonomy")

    val conceptXLinkLabelMap: Map[EName, String] =
      concepts.toSeq.map(c => c -> makeXLinkLabelForLocatorToConcept(c, startTaxonomy)).toMap

    require(
      conceptXLinkLabelMap.values.toSet.size == conceptXLinkLabelMap.size,
      s"Not all concept locator XLink labels unique")

    val declaredNamespaces: Set[String] = startLinkbase.scope.withoutDefaultNamespace.namespaces
    require(
      relationships.flatMap(_.nonXLinkArcAttributes.keySet).distinct.filter(_.namespaceUriOption.nonEmpty)
        .forall(ename => declaredNamespaces.contains(ename.namespaceUriOption.get)),
      s"Not all needed (attribute) namespaces declared")
  }

  private def addInterConceptArcs[A <: model.InterConceptRelationship](
    linkbase: Linkbase,
    extendedLink: ExtendedLink,
    relationships: immutable.IndexedSeq[A],
    makeArcElem: (A, String, String) => resolved.Elem): BasicTaxonomy = {

    assert(linkbase.docUri == extendedLink.docUri)

    // Edit

    val startLinkbaseAsResolvedElem = resolved.Elem.from(linkbase)

    val extLinkPath = extendedLink.backingElem.path

    val endLinkbaseAsResolvedElem = startLinkbaseAsResolvedElem.updateElemOrSelf(extLinkPath) { oldExtLinkElem =>
      val arcElems = relationships.map { relationship =>
        val sourceXLinkLabel =
          makeXLinkLabelForLocatorToConcept(relationship.source.asInstanceOf[model.Node.GlobalElementDecl].targetEName, startTaxonomy)
        val targetXLinkLabel =
          makeXLinkLabelForLocatorToConcept(relationship.target.asInstanceOf[model.Node.GlobalElementDecl].targetEName, startTaxonomy)

        makeArcElem(relationship, sourceXLinkLabel, targetXLinkLabel)
      }

      val locatorElems = relationships.flatMap { relationship =>
        List(
          relationship.source.asInstanceOf[model.Node.GlobalElementDecl].targetEName,
          relationship.target.asInstanceOf[model.Node.GlobalElementDecl].targetEName)
      }.distinct.map { concept =>
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

  // Private methods for creating (specific) inter-concept relationships

  private def makeInterConceptArc(
    relationship: model.InterConceptRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    extLinkEName: EName,
    arcrole: String): resolved.Elem = {

    resolved.Node.emptyElem(extLinkEName, relationship.nonXLinkArcAttributes)
      .plusAttribute(ENames.XLinkFromEName, sourceXLinkLabel)
      .plusAttribute(ENames.XLinkToEName, targetXLinkLabel)
      .plusAttribute(ENames.XLinkArcroleEName, arcrole)
      .plusAttribute(ENames.XLinkTypeEName, "arc")
  }

  private def makePresentationArc(
    relationship: model.PresentationRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    arcrole: String): resolved.Elem = {

    makeInterConceptArc(relationship, sourceXLinkLabel, targetXLinkLabel, ENames.LinkPresentationArcEName, arcrole)
  }

  private def makeDefinitionArc(
    relationship: model.DefinitionRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String,
    arcrole: String): resolved.Elem = {

    makeInterConceptArc(relationship, sourceXLinkLabel, targetXLinkLabel, ENames.LinkDefinitionArcEName, arcrole)
  }

  private def makeParentChildArc(relationship: model.ParentChildRelationship, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makePresentationArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://www.xbrl.org/2003/arcrole/parent-child")
  }

  private def makeDimensionalArc(relationship: model.DimensionalRelationship, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    relationship match {
      case relationship: model.AllRelationship =>
        makeAllArc(relationship, sourceXLinkLabel, targetXLinkLabel)
      case relationship: model.NotAllRelationship =>
        makeNotAllArc(relationship, sourceXLinkLabel, targetXLinkLabel)
      case relationship: model.HypercubeDimensionRelationship =>
        makeHypercubeDimensionArc(relationship, sourceXLinkLabel, targetXLinkLabel)
      case relationship: model.DimensionDomainRelationship =>
        makeDimensionDomainArc(relationship, sourceXLinkLabel, targetXLinkLabel)
      case relationship: model.DomainMemberRelationship =>
        makeDomainMemberArc(relationship, sourceXLinkLabel, targetXLinkLabel)
      case relationship: model.DimensionDefaultRelationship =>
        makeDimensionDefaultArc(relationship, sourceXLinkLabel, targetXLinkLabel)
    }
  }

  private def makeAllArc(relationship: model.AllRelationship, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/all")
  }

  private def makeNotAllArc(relationship: model.NotAllRelationship, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/notAll")
  }

  private def makeHypercubeDimensionArc(
    relationship: model.HypercubeDimensionRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String): resolved.Elem = {

    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/hypercube-dimension")
  }

  private def makeDimensionDomainArc(
    relationship: model.DimensionDomainRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String): resolved.Elem = {

    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/dimension-domain")
  }

  private def makeDomainMemberArc(
    relationship: model.DomainMemberRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String): resolved.Elem = {

    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/domain-member")
  }

  private def makeDimensionDefaultArc(
    relationship: model.DimensionDefaultRelationship,
    sourceXLinkLabel: String,
    targetXLinkLabel: String): resolved.Elem = {

    makeDefinitionArc(relationship, sourceXLinkLabel, targetXLinkLabel, "http://xbrl.org/int/dim/arcrole/dimension-default")
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
}
