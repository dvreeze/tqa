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

package eu.cdevreeze.tqa.taxonomy

import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.XbrldtDimensionItemEName
import eu.cdevreeze.tqa.ENames.XbrldtHypercubeItemEName
import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.queryapi.InterConceptRelationshipPath
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.relationship.PresentationRelationship
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Query API test case. It uses test data from https://acra.gov.sg/How_To_Guides/Filing_Financial_Statements/Downloads/.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class QueryApiTest extends FunSuite {

  test("testQueryPLink") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_pre.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-dei-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 20
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    val elr = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/sg-fsh-bfc/role/FilingInformation"

    val tns = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-dei"
    val plinkTop = EName(tns, "DisclosureOfFilingInformationAbstract")

    val prels = richTaxo.filterInterConceptRelationshipsOfType(classTag[PresentationRelationship])(_.elr == elr)

    assertResult(Set("http://www.xbrl.org/2003/arcrole/parent-child")) {
      prels.map(_.arcrole).toSet
    }

    assertResult(prels) {
      richTaxo.filterStandardRelationshipsOfType(classTag[PresentationRelationship])(_.elr == elr)
    }

    val topENames = prels.map(_.sourceConceptEName).toSet.diff(prels.map(_.targetConceptEName).toSet)

    def hasExpectedElr[A <: InterConceptRelationship](path: InterConceptRelationshipPath[A]): Boolean = {
      path.firstRelationship.elr == elr && path.isElrValid
    }

    assertResult(Set(plinkTop)) {
      topENames
    }
    assertResult(prels.map(_.targetConceptEName).toSet) {
      val paths = richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(hasExpectedElr)
      paths.flatMap(_.relationships.map(_.targetConceptEName)).toSet
    }
    assertResult(prels.map(_.targetConceptEName).toSet.union(Set(plinkTop))) {
      val paths = richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(hasExpectedElr)
      paths.flatMap(_.concepts).toSet
    }

    assertResult(Nil) {
      richTaxo.filterLongestIncomingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(hasExpectedElr)
    }

    assertResult(Set(plinkTop)) {
      val paths = prels.map(_.targetConceptEName).distinct flatMap { concept =>
        richTaxo.filterLongestIncomingInterConceptRelationshipPaths(concept, classTag[PresentationRelationship])(hasExpectedElr)
      }

      paths.map(_.sourceConcept).toSet
    }

    assertResult(true) {
      val nonTopConcepts = prels.map(_.targetConceptEName).toSet
      nonTopConcepts.forall(c => richTaxo.filterLongestIncomingInterConceptRelationshipPaths(c, classTag[PresentationRelationship])(hasExpectedElr).nonEmpty)
    }
  }

  test("testQueryDimensionalDLink") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-fsh-bfc/sg-fsh-bfc_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }
    assertResult(false) {
      richTaxo.taxonomyBase.hasDuplicateGlobalElementDeclarationENames
    }
    assertResult(false) {
      taxoRootElems.exists(e => richTaxo.taxonomyBase.hasDuplicateIds(e))
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
      hypercubes.flatMap(hc => richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(hc, classTag[DimensionalRelationship])(_.isElrValid))

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

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

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

    val hdRelsForElr = richTaxo.filterOutgoingHypercubeDimensionRelationships(hhRel.targetConceptEName) { rel =>
      hhRel.isFollowedBy(rel)
    }

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

    val ddPathsForElr = richTaxo.filterLongestOutgoingConsecutiveDomainAwareRelationshipPaths(hdRel.targetConceptEName) { path =>
      hdRel.isFollowedBy(path.firstRelationship)
    }

    val ddPathLeaves = ddPathsForElr.map(_.targetConcept).toSet

    val incomingPaths = ddPathLeaves.toIndexedSeq.flatMap(c =>
      richTaxo.filterLongestIncomingInterConceptRelationshipPaths(c, classTag[DimensionalRelationship])(_ => true))

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

      val dmRelPaths = richTaxo.filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(rootPrimary) {
        _.firstRelationship.elr == elr
      }

      dmRelPaths.flatMap(_.relationships).map(_.targetConceptEName).contains(anInheritingPrimary)
    }

    // Testing has-hypercube inheritance in bulk

    val inheritingPrimaries =
      hhRels.flatMap(hh => richTaxo.filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(hh.primary)(_.firstRelationship.elr == hh.elr)).
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

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

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
      dimensions.forall(dim => richTaxo.findGlobalElementDeclaration(dim).map(_.targetEName) == Some(dim))
    }
  }

  test("testDimensionalBulkQueries") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

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
  }

  /**
   * Testing that namespace prefixes are irrelevant, even the link, xlink and xsd prefixes.
   */
  test("testDimensionalBulkQueriesAfterEditingPrefixes") {
    val docParser = DocumentParserUsingStax.newInstance()

    val docUris = Vector(
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/fr/sg-se/sg-se_2013-09-13_def-with-edited-prefixes.xml").toURI,
      classOf[QueryApiTest].getResource("/taxonomies/acra/2013/elts/sg-as-cor_2013-09-13-with-edited-prefixes.xsd").toURI)

    val docs = docUris.map(uri => docParser.parse(uri).withUriOption(Some(uri)))

    val taxoRootElems = docs.map(d => TaxonomyElem.build(indexed.Document(d).documentElement))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo = BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)

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
  }
}
