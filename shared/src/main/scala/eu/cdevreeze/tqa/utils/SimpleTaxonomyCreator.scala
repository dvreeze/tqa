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
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.PresentationLink
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
 * from scratch, but expects complete taxonomy schemas and potentially almost empty linkbases as starting
 * point. The functions in this utility then help further fill those linkbases. This utility certainly does
 * not add documents to the taxonomy; it only adds content to already existing documents.
 *
 * @author Chris de Vreeze
 */
final class SimpleTaxonomyCreator(val startTaxonomy: BasicTaxonomy) {

  import SimpleTaxonomyCreator._

  /**
   * Adds the given parent-child arcs, expecting an empty presentation link to start with.
   */
  def addParentChildArcs(docUri: URI, elr: String, arcs: immutable.IndexedSeq[ParentChildArc]): SimpleTaxonomyCreator = {
    // Do some validations

    val doc: TaxonomyDocument =
      startTaxonomy.taxonomyBase.taxonomyDocUriMap.getOrElse(docUri, sys.error(s"Missing document $docUri"))

    require(doc.documentElement.isInstanceOf[Linkbase], s"Not a linkbase: $docUri")
    val startLinkbase = doc.documentElement.asInstanceOf[Linkbase]

    val presentationLink: PresentationLink =
      startLinkbase.findAllExtendedLinks.collect { case plink: PresentationLink if plink.role == elr => plink }
        .lastOption.getOrElse(sys.error(s"Missing presentation link with ELR $elr"))

    require(presentationLink.findAllChildElems.isEmpty, s"Expected an empty presentation link for ELR $elr")

    require(
      arcs.forall(arc => startTaxonomy.findConceptDeclaration(arc.source).nonEmpty),
      s"Not all arc sources found")
    require(
      arcs.forall(arc => startTaxonomy.findConceptDeclaration(arc.target).nonEmpty),
      s"Not all arc targets found")

    val concepts: Set[EName] = arcs.flatMap(arc => List(arc.source, arc.target)).toSet
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

    // Edit

    val startLinkbaseAsResolvedElem = resolved.Elem.from(startLinkbase)

    val plinkPath = presentationLink.backingElem.path

    val endLinkbaseAsResolvedElem = startLinkbaseAsResolvedElem.updateElemOrSelf(plinkPath) { oldPLinkElem =>
      val arcElems = arcs.map { arc =>
        val sourceXLinkLabel = makeXLinkLabelForLocatorToConcept(arc.source, startTaxonomy)
        val targetXLinkLabel = makeXLinkLabelForLocatorToConcept(arc.target, startTaxonomy)

        makeParentChildArc(arc, sourceXLinkLabel, targetXLinkLabel)
      }

      val locatorElems = arcs.flatMap(arc => List(arc.source, arc.target)).distinct.map { concept =>
        makeLocatorToConcept(concept, startTaxonomy)
      }

      oldPLinkElem.plusChildren(arcElems).plusChildren(locatorElems)
    }

    // Create result

    val endLinkbaseAsSimpleElem = simple.Elem.from(endLinkbaseAsResolvedElem, startLinkbase.scope).prettify(2)
    val endLinkbaseAsIndexedElem = indexed.Elem(startLinkbase.docUri, endLinkbaseAsSimpleElem)
    val endLinkbaseDoc: TaxonomyDocument = TaxonomyDocument.build(indexed.Document(endLinkbaseAsIndexedElem))

    val endTaxonomyBase =
      TaxonomyBase.build(
        startTaxonomy.taxonomyDocs.filterNot(_.uri == docUri) :+ endLinkbaseDoc)

    val endTaxo = BasicTaxonomy.build(
      endTaxonomyBase,
      startTaxonomy.extraSubstitutionGroupMap,
      DefaultRelationshipFactory.StrictInstance)

    new SimpleTaxonomyCreator(endTaxo)
  }

  private def makeParentChildArc(arc: ParentChildArc, sourceXLinkLabel: String, targetXLinkLabel: String): resolved.Elem = {
    import resolved.Node._

    emptyElem(ENames.LinkPresentationArcEName, arc.nonXLinkAttributes)
      .plusAttribute(ENames.XLinkFromEName, sourceXLinkLabel)
      .plusAttribute(ENames.XLinkToEName, targetXLinkLabel)
      .plusAttribute(ENames.XLinkArcroleEName, "http://www.xbrl.org/2003/arcrole/parent-child")
      .plusAttribute(ENames.XLinkTypeEName, "arc")
  }

  private def makeLocatorToConcept(concept: EName, taxo: BasicTaxonomy): resolved.Elem = {
    val conceptDecl = taxo.getConceptDeclaration(concept)

    require(conceptDecl.globalElementDeclaration.attributeOption(ENames.IdEName).nonEmpty, s"Missing ID for concept $concept")
    val id = conceptDecl.globalElementDeclaration.attribute(ENames.IdEName)

    val docUri = conceptDecl.globalElementDeclaration.docUri
    val elemUri = new URI(docUri.getScheme, docUri.getSchemeSpecificPart, id)

    import resolved.Node._

    emptyElem(ENames.LinkLocEName)
      .plusAttribute(ENames.XLinkHrefEName, elemUri.toString) // TODO Create relative URIs when applicable
      .plusAttribute(ENames.XLinkLabelEName, makeXLinkLabelForLocatorToConcept(concept, taxo))
      .plusAttribute(ENames.XLinkTypeEName, "locator")
  }

  private def makeXLinkLabelForLocatorToConcept(concept: EName, taxo: BasicTaxonomy): String = {
    val conceptDecl = taxo.getConceptDeclaration(concept)

    require(conceptDecl.globalElementDeclaration.attributeOption(ENames.IdEName).nonEmpty, s"Missing ID for concept $concept")
    val id = conceptDecl.globalElementDeclaration.attribute(ENames.IdEName)
    id + "_loc"
  }
}

object SimpleTaxonomyCreator {

  final case class ParentChildArc(source: EName, target: EName, nonXLinkAttributes: Map[EName, String]) {
    require(
      nonXLinkAttributes.keySet.forall(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
      s"Expected non-XLink attributes, but got attributes ${nonXLinkAttributes.keySet}")
  }
}
