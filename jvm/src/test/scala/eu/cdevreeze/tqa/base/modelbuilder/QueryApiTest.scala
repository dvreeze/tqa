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

package eu.cdevreeze.tqa.base.modelbuilder

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.ENames.XbrldtDimensionItemEName
import eu.cdevreeze.tqa.ENames.XbrldtHypercubeItemEName
import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base
import eu.cdevreeze.tqa.base.model.DimensionalRelationship
import eu.cdevreeze.tqa.base.model.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.model.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.model.PresentationRelationship
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Query API test case. It uses test data from https://acra.gov.sg/How_To_Guides/Filing_Financial_Statements/Downloads/.
 *
 * @author Chris de Vreeze
 */
class QueryApiTest extends AnyFunSuite {

  test("testQueryPLink") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_pre.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-dei-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo =
      base.taxonomy.BasicTaxonomy.build(
        underlyingTaxo,
        SubstitutionGroupMap.Empty,
        base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 20
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    val elr = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-fsh-bfc/role/FilingInformation"

    val tns = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-dei"
    val plinkTop = EName(tns, "DisclosureOfFilingInformationAbstract")

    val prels = richTaxo.filterPresentationRelationshipsOfType(classTag[PresentationRelationship])(_.elr == elr)

    assertResult(Set("http://www.xbrl.org/2003/arcrole/parent-child")) {
      prels.map(_.arcrole).toSet
    }

    assertResult(prels) {
      richTaxo.filterStandardRelationshipsOfType(classTag[PresentationRelationship])(_.elr == elr)
    }

    val topENames = prels.map(_.sourceConceptEName).toSet.diff(prels.map(_.targetConceptEName).toSet)

    assertResult(Set(plinkTop)) {
      topENames
    }
    assertResult(prels.map(_.targetConceptEName).toSet) {
      val paths = richTaxo.findAllOutgoingConsecutiveParentChildRelationshipPaths(plinkTop)
      paths.flatMap(_.relationships.map(_.targetConceptEName)).toSet
    }
    assertResult(prels.map(_.targetConceptEName).toSet.union(Set(plinkTop))) {
      val paths = richTaxo.findAllOutgoingConsecutiveParentChildRelationshipPaths(plinkTop)
      paths.flatMap(_.concepts).toSet
    }

    assertResult(Nil) {
      richTaxo.findAllIncomingConsecutiveParentChildRelationshipPaths(plinkTop)
    }

    assertResult(Set(plinkTop)) {
      val paths = prels.map(_.targetConceptEName).distinct flatMap { concept =>
        richTaxo.findAllIncomingConsecutiveParentChildRelationshipPaths(concept)
      }

      paths.map(_.sourceConcept).toSet
    }

    assertResult(true) {
      val nonTopConcepts = prels.map(_.targetConceptEName).toSet
      nonTopConcepts.forall(c => richTaxo.findAllIncomingConsecutiveParentChildRelationshipPaths(c).nonEmpty)
    }
  }

  test("testQueryDimensionalDLink") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo = base.taxonomy.BasicTaxonomy.build(
      underlyingTaxo,
      SubstitutionGroupMap.Empty,
      base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }
    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.groupBy(_.targetEName).filter(_._2.size >= 2).isEmpty
    }

    assertResult(Nil) {
      richTaxo.filterHasHypercubeRelationships(_.isAllRelationship)
    }

    val hdRels = richTaxo.findAllHypercubeDimensionRelationships

    assertResult(Set(
      "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-fsh-bfc/role/AxisRetrospectiveApplicationAndRetrospectiveRestatement",
      "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-fsh-bfc/role/AxisConsolidatedAndSeparateFinancialStatements")) {

      hdRels.map(_.elr).toSet
    }

    assertResult(true) {
      hdRels.map(_.sourceConceptEName).forall(c => richTaxo.findHypercubeDeclaration(c).isDefined)
    }
    assertResult(true) {
      hdRels.map(_.targetConceptEName).forall(c => richTaxo.findDimensionDeclaration(c).isDefined)
    }

    assertResult(true) {
      hdRels.map(_.sourceConceptEName).forall(c => richTaxo.findDimensionDeclaration(c).isEmpty)
    }
    assertResult(true) {
      hdRels.map(_.targetConceptEName).forall(c => richTaxo.findHypercubeDeclaration(c).isEmpty)
    }

    assertResult(true) {
      hdRels.map(_.sourceConceptEName).forall(c => richTaxo.findPrimaryItemDeclaration(c).isEmpty)
    }
    assertResult(true) {
      hdRels.map(_.targetConceptEName).forall(c => richTaxo.findPrimaryItemDeclaration(c).isEmpty)
    }

    val ddRels = richTaxo.findAllDimensionDomainRelationships

    assertResult(true) {
      ddRels.nonEmpty
    }

    assertResult(true) {
      ddRels.map(_.sourceConceptEName).forall(c => richTaxo.findExplicitDimensionDeclaration(c).isDefined)
    }
    assertResult(true) {
      ddRels.map(_.targetConceptEName).forall(c => richTaxo.findPrimaryItemDeclaration(c).isDefined)
    }

    assertResult(Set(true)) {
      richTaxo.findAllHypercubeDeclarations.map(_.isAbstract).toSet
    }
    assertResult(Set(true)) {
      richTaxo.findAllDimensionDeclarations.map(_.isAbstract).toSet
    }
    assertResult(Set(false, true)) {
      richTaxo.findAllPrimaryItemDeclarations.map(_.isAbstract).toSet
    }

    assertResult(true) {
      richTaxo.filterPrimaryItemDeclarations(_.isAbstract).map(_.targetEName.localPart).contains(
        "ChangesInWorkingCapitalAbstract")
    }

    val hypercubes = richTaxo.findAllHypercubeDeclarations.map(_.targetEName).distinct

    val paths =
      hypercubes.flatMap(hc => richTaxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(hc, classTag[DimensionalRelationship])(_ => true))

    assertResult(richTaxo.findAllHypercubeDimensionRelationships.toSet) {
      paths.map(_.firstRelationship).toSet
    }
  }

  test("testQueryDimensionalDLinkWithHasHypercubes") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo = base.taxonomy.BasicTaxonomy.build(
      underlyingTaxo,
      SubstitutionGroupMap.Empty,
      base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    assertResult(true) {
      richTaxo.filterHasHypercubeRelationships(_.isAllRelationship).nonEmpty
    }
    assertResult(true) {
      richTaxo.filterHasHypercubeRelationships(!_.isAllRelationship).isEmpty
    }

    val hhRels = richTaxo.findAllHasHypercubeRelationships

    val elr = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteFinanceLeaseLiabilities"

    val hhRelsForElr = richTaxo.filterHasHypercubeRelationships(_.elr == elr)

    assertResult(List(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DisclosureOfAmountsPayableUnderFinanceLeasesByLesseeAbstract"))) {
      hhRelsForElr.map(_.primary)
    }

    val hhRel = hhRelsForElr.head

    val hdRelsForElr = richTaxo.findAllConsecutiveHypercubeDimensionRelationships(hhRel)

    assertResult(hdRelsForElr) {
      richTaxo.findAllOutgoingStandardRelationshipsOfType(hhRel.targetConceptEName, classTag[HypercubeDimensionRelationship]) collect {
        case hd if hhRel.isFollowedBy(hd) => hd
      }
    }

    assertResult(List(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DisclosureOfAmountsPayableUnderFinanceLeasesByLesseeTable"))) {
      hdRelsForElr.map(_.hypercube)
    }
    assertResult(List(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}MaturityAxis"))) {
      hdRelsForElr.map(_.dimension)
    }

    val hdRel = hdRelsForElr.head

    val ddPathsForElr = richTaxo.filterOutgoingConsecutiveDomainAwareRelationshipPaths(hdRel.targetConceptEName) { path =>
      hdRel.isFollowedBy(path.firstRelationship)
    }

    val ddPathLeaves = ddPathsForElr.map(_.targetConcept).toSet

    val incomingPaths = ddPathLeaves.toIndexedSeq.flatMap(c =>
      richTaxo.filterIncomingConsecutiveInterConceptRelationshipPaths(c, classTag[DimensionalRelationship])(_ => true))

    assertResult(true) {
      incomingPaths.exists(_.firstRelationship == hhRel)
    }

    // Testing has-hypercube inheritance

    val anInheritingPrimary = EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}AmountDueToCustomersForConstructionContracts")

    val elrToPrimaryMap = richTaxo.findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(anInheritingPrimary)

    assertResult(Set("http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteTradeAndOtherPayables")) {
      elrToPrimaryMap.keySet
    }

    assertResult(true) {
      val (elr, primaries) = elrToPrimaryMap.iterator.next
      val rootPrimary = primaries.iterator.next

      val dmRelPaths = richTaxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(rootPrimary) {
        _.firstRelationship.elr == elr
      }

      dmRelPaths.flatMap(_.relationships).map(_.targetConceptEName).contains(anInheritingPrimary)
    }

    // Testing has-hypercube inheritance in bulk

    val inheritingPrimaries =
      hhRels.flatMap(hh => richTaxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(hh.primary)(_.firstRelationship.elr == hh.elr)).
        flatMap(_.concepts).toSet

    assertResult(true) {
      inheritingPrimaries.flatMap(e => richTaxo.findAllOwnOrInheritedHasHypercubes(e)).nonEmpty
    }

    assertResult(hhRels.toSet) {
      inheritingPrimaries.flatMap(e => richTaxo.findAllOwnOrInheritedHasHypercubes(e)).toSet
    }
  }

  test("testQueryConceptsInDimensionalDLinkWithHasHypercubes") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo = base.taxonomy.BasicTaxonomy.build(
      underlyingTaxo,
      SubstitutionGroupMap.Empty,
      base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    assertResult(richTaxo.filterGlobalElementDeclarationsOnOwnOrInheritedSubstitutionGroup(XbrliItemEName).map(_.targetEName).toSet) {
      richTaxo.findAllItemDeclarations.map(_.targetEName).toSet
    }

    val primaries = richTaxo.findAllPrimaryItemDeclarations.map(_.targetEName).toSet

    assertResult(true) {
      val dds = richTaxo.findAllDimensionDomainRelationships.map(e => e.targetConceptEName).toSet
      val dms = richTaxo.findAllDomainMemberRelationships.map(e => e.targetConceptEName).toSet
      val hhs = richTaxo.findAllHasHypercubeRelationships.map(e => e.primary).toSet

      dds.union(dms).union(hhs).subsetOf(primaries)
    }

    assertResult(richTaxo.filterGlobalElementDeclarations(e => primaries.contains(e.targetEName)).map(_.targetEName).toSet) {
      primaries
    }
    assertResult(richTaxo.filterItemDeclarations(e => primaries.contains(e.targetEName)).map(_.targetEName).toSet) {
      primaries
    }
    assertResult(richTaxo.filterGlobalElementDeclarationsOnOwnSubstitutionGroup(Set(XbrliItemEName)).map(_.targetEName).toSet) {
      primaries
    }

    val hypercubes = richTaxo.findAllHypercubeDeclarations.map(_.targetEName).toSet

    assertResult(true) {
      richTaxo.findAllHasHypercubeRelationships.map(_.hypercube).toSet.subsetOf(hypercubes)
    }
    assertResult(true) {
      richTaxo.findAllHypercubeDimensionRelationships.map(_.hypercube).toSet.subsetOf(hypercubes)
    }

    assertResult(richTaxo.filterGlobalElementDeclarations(e => hypercubes.contains(e.targetEName)).map(_.targetEName).toSet) {
      hypercubes
    }
    assertResult(richTaxo.filterItemDeclarations(e => hypercubes.contains(e.targetEName)).map(_.targetEName).toSet) {
      hypercubes
    }
    assertResult(richTaxo.filterGlobalElementDeclarationsOnOwnSubstitutionGroup(Set(XbrldtHypercubeItemEName)).map(_.targetEName).toSet) {
      hypercubes
    }

    val dimensions = richTaxo.findAllDimensionDeclarations.map(_.targetEName).toSet

    assertResult(true) {
      richTaxo.findAllHypercubeDimensionRelationships.map(_.dimension).toSet.subsetOf(dimensions)
    }
    assertResult(true) {
      richTaxo.findAllDimensionDomainRelationships.map(_.dimension).toSet.subsetOf(dimensions)
    }

    assertResult(richTaxo.filterGlobalElementDeclarations(e => dimensions.contains(e.targetEName)).map(_.targetEName).toSet) {
      dimensions
    }
    assertResult(richTaxo.filterItemDeclarations(e => dimensions.contains(e.targetEName)).map(_.targetEName).toSet) {
      dimensions
    }
    assertResult(richTaxo.filterGlobalElementDeclarationsOnOwnSubstitutionGroup(Set(XbrldtDimensionItemEName)).map(_.targetEName).toSet) {
      dimensions
    }

    assertResult(true) {
      dimensions.forall(dim => richTaxo.findGlobalElementDeclaration(dim).map(_.targetEName).contains(dim))
    }
  }

  test("testElementCount") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo = base.taxonomy.BasicTaxonomy.build(
      underlyingTaxo,
      SubstitutionGroupMap.Empty,
      base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    val originalSchemas = originalRichTaxo.taxonomyDocs.filter(_.isSchemaDocument).map(_.documentElement)

    val originalSchemaDescendantOrSelfCount = originalSchemas.flatMap(_.findAllElemsOrSelf).size

    val originalSchemaCount = originalSchemas.size
    val topLevelAnnotationCount =
      originalSchemas.flatMap(_.filterChildElems(_.resolvedName == ENames.XsAnnotationEName)).size
    val topLevelAnnotationChildCount =
      originalSchemas.flatMap(_.filterChildElems(_.resolvedName == ENames.XsAnnotationEName))
        .flatMap(_.findAllChildElems).size
    val importCount =
      originalSchemas.flatMap(_.filterChildElems(_.resolvedName == ENames.XsImportEName)).size
    val includeCount =
      originalSchemas.flatMap(_.filterChildElems(_.resolvedName == ENames.XsIncludeEName)).size

    val expectedElemCount =
      originalSchemaDescendantOrSelfCount - originalSchemaCount - topLevelAnnotationCount -
        topLevelAnnotationChildCount - importCount - includeCount

    assertResult(expectedElemCount) {
      richTaxo.topmostSchemaContentElements.flatMap(_.findAllElemsOrSelf).size
    }
  }

  test("testDimensionalBulkQueries") {
    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    testDimensionalBulkQueries(docUris)
  }

  /**
   * Testing that namespace prefixes are irrelevant, even the link, xlink and xsd prefixes.
   */
  test("testDimensionalBulkQueriesAfterEditingPrefixes") {
    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def-with-edited-prefixes.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13-with-edited-prefixes.xsd").toURI)

    testDimensionalBulkQueries(docUris)
  }

  private def testDimensionalBulkQueries(taxoDocUris: immutable.IndexedSeq[URI]): Unit = {
    val docParser = DocumentParserUsingStax.newInstance()

    val docs = taxoDocUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoDocs = docs.map(d => base.dom.TaxonomyDocument.build(indexed.Document(d)))

    val underlyingTaxo = base.dom.TaxonomyBase.build(taxoDocs)
    val originalRichTaxo = base.taxonomy.BasicTaxonomy.build(
      underlyingTaxo,
      SubstitutionGroupMap.Empty,
      base.relationship.DefaultRelationshipFactory.LenientInstance)

    val richTaxo: base.model.taxonomy.BasicTaxonomy = TaxonomyConverter.convertTaxonomy(originalRichTaxo)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    val hasHypercubeInheritanceOrSelf =
      richTaxo.computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps

    assertResult(true) {
      hasHypercubeInheritanceOrSelf.nonEmpty
    }

    assertResult(hasHypercubeInheritanceOrSelf.keySet.toSeq.
      map(concept => (concept -> richTaxo.findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept))).toMap) {

      hasHypercubeInheritanceOrSelf
    }

    val hasHypercubeInheritance =
      richTaxo.computeHasHypercubeInheritanceReturningElrToPrimariesMaps

    assertResult(true) {
      hasHypercubeInheritance.nonEmpty
    }

    assertResult(hasHypercubeInheritance.keySet.toSeq.
      map(concept => (concept -> richTaxo.findAllInheritedHasHypercubesAsElrToPrimariesMap(concept))).toMap) {

      hasHypercubeInheritance
    }

    assertResult(true) {
      hasHypercubeInheritanceOrSelf.keySet.size > 250
    }

    assertResult(Set.empty) {
      val primaries: Set[EName] =
        richTaxo.findAllPrimaryItemDeclarations.map(_.targetEName).toSet

      hasHypercubeInheritanceOrSelf.keySet.diff(primaries)
    }

    assertResult(Set.empty) {
      val someDimTrees: Set[Map[String, Set[EName]]] = {
        Set(
          Map(
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteShareBasedPaymentsb" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DescriptionOfInputsToOptionPricingModelShareOptionsGrantedAbstract"))),
          Map(
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/SOCIE" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}StatementOfChangesInEquityAbstract"))),
          Map(
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NotePropertyPlantAndEquipment" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DisclosureOfDetailedInformationAboutPropertyPlantAndEquipmentAbstract")),
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteIntangibleAssets" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DisclosureOfIntangibleAssetsAbstract"))),
          Map(
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteShareBasedPaymentsa" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}InformationAboutShareOptionsExercisedDuringPeriodAbstract"))),
          Map(
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/NoteShareCapital" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}DisclosureOfAmountOfShareCapitalAbstract")),
            "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-se/role/SOCIE" ->
              Set(EName("{http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-as}StatementOfChangesInEquityAbstract"))))
      }

      someDimTrees.diff(hasHypercubeInheritanceOrSelf.values.toSet)
    }

    assertResult(Set.empty) {
      val primariesHavingHypercube: Set[EName] =
        hasHypercubeInheritanceOrSelf.values.flatMap(_.values).flatten.toSet

      val primaries = richTaxo.findAllPrimaryItemDeclarations.map(_.targetEName).toSet

      primariesHavingHypercube.diff(primaries)
    }

    val dimTreeKeys: Set[Map[String, Set[EName]]] =
      hasHypercubeInheritanceOrSelf.values.toSet

    assertResult(true) {
      richTaxo.findAllHypercubeDeclarations.size > 40
    }

    assertResult(Set.empty) {
      val hasHypercubeKeys: Set[(String, Set[EName])] = dimTreeKeys.flatten

      val hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship] =
        hasHypercubeKeys.toIndexedSeq flatMap {
          case (elr, primaries) =>
            primaries.toIndexedSeq flatMap { prim =>
              richTaxo.filterOutgoingHasHypercubeRelationshipsOnElr(prim, elr)
            }
        }

      val allHypercubes = richTaxo.findAllHypercubeDeclarations.map(_.targetEName).toSet
      hasHypercubes.map(_.hypercube).toSet.diff(allHypercubes)
    }
  }
}
