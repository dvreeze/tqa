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

package eu.cdevreeze.tqa.base.taxonomy

import java.io.File

import scala.collection.immutable
import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.DimensionalRelationship
import eu.cdevreeze.yaidom.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Dimensional relationship source querying test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class DimensionalRelationshipSourceTest extends FunSuite {

  test("testValidHypercubeDimension") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/hypercubeDimensionValid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/hypercubeDimensionValid-definition.xml"))

    val hypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(hypercubeName)

    assertResult(2) {
      hypercubeDimensions.size
    }
    assertResult(Set(hypercubeName)) {
      hypercubeDimensions.map(_.sourceConceptEName).toSet
    }
    assertResult(Set(hypercubeName)) {
      hypercubeDimensions.map(_.hypercube).toSet
    }

    assertResult(true) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.getHypercubeDeclaration(hypercubeDimensions.head.hypercube).isAbstract
    }

    assertResult(Set(
      EName("{http://www.xbrl.org/dim/conf}ProdDim"),
      EName("{http://www.xbrl.org/dim/conf}RegionDim"))) {

      hypercubeDimensions.map(_.dimension).toSet
    }
  }

  test("testHypercubeDimensionSubValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/hypercubeDimensionSubValid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/hypercubeDimensionSubValid-definition.xml"))

    val hypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(hypercubeName)

    assertResult(2) {
      hypercubeDimensions.size
    }
    assertResult(Set(hypercubeName)) {
      hypercubeDimensions.map(_.sourceConceptEName).toSet
    }
    assertResult(Set(hypercubeName)) {
      hypercubeDimensions.map(_.hypercube).toSet
    }

    assertResult(true) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.getHypercubeDeclaration(hypercubeDimensions.head.hypercube).isAbstract
    }

    // The hypercube is indirectly a hypercube, via a substitution group indirection.

    assertResult(Some(EName("{http://www.xbrl.org/dim/conf}ParentOfCube"))) {
      taxo.getHypercubeDeclaration(hypercubeDimensions.head.hypercube).substitutionGroupOption
    }
    assertResult(true) {
      taxo.getHypercubeDeclaration(hypercubeDimensions.head.hypercube).globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtHypercubeItemEName,
        taxo.substitutionGroupMap)
    }

    assertResult(Set(
      EName("{http://www.xbrl.org/dim/conf}ProdDim"),
      EName("{http://www.xbrl.org/dim/conf}RegioDim"))) {

      hypercubeDimensions.map(_.dimension).toSet
    }
  }

  test("testHypercubeDimensionIsItemInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsItemInvalid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsItemInvalid-definition.xml"))

    val invalidHypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(invalidHypercubeName)

    assertResult(1) {
      hypercubeDimensions.size
    }

    assertResult(true) {
      taxo.findItemDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findPrimaryItemDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
  }

  test("testHypercubeDimensionIsTupleInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsTupleInvalid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsTupleInvalid-definition.xml"))

    val invalidHypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(invalidHypercubeName)

    assertResult(1) {
      hypercubeDimensions.size
    }

    assertResult(false) {
      taxo.findItemDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findTupleDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
  }

  test("testHypercubeDimensionIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsDimensionInvalid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsDimensionInvalid-definition.xml"))

    val invalidHypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(invalidHypercubeName)

    assertResult(1) {
      hypercubeDimensions.size
    }

    assertResult(true) {
      taxo.findItemDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findTypedDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
  }

  test("testHypercubeDimensionIsDimensionSubInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsDimensionSubInvalid.xsd",
      "100-xbrldte/102-HypercubeDimensionSourceError/sourceHypercubeDimensionIsDimensionSubInvalid-definition.xml"))

    val invalidHypercubeName = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(invalidHypercubeName)

    assertResult(1) {
      hypercubeDimensions.size
    }

    assertResult(true) {
      taxo.findConceptDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findItemDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(false) {
      taxo.findTypedDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(hypercubeDimensions.head.hypercube).isDefined
    }

    // The invalid "hypercube" is indirectly a dimension, via a substitution group indirection.

    assertResult(Some(EName("{http://www.xbrl.org/dim/conf}ParentOfCube"))) {
      taxo.getExplicitDimensionDeclaration(hypercubeDimensions.head.hypercube).substitutionGroupOption
    }
    assertResult(true) {
      taxo.getItemDeclaration(hypercubeDimensions.head.hypercube).globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtDimensionItemEName,
        taxo.substitutionGroupMap)
    }
  }

  test("testHasHypercubeAllValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllValid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllValid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.xbrl.org/dim/conf/primary}Sales")
    val targetName = EName("{http://www.conformance-dimensions.com/xbrl/}Cube")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(6) {
      taxo.findAllDimensionalRelationshipsOfType(classTag[DimensionalRelationship]).size
    }

    assertResult(true) {
      taxo.findPrimaryItemDeclaration(sourceName).isDefined
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(targetName).isDefined
    }
  }

  test("testHasHypercubeNotAllValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeNotAllValid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeNotAllValid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.xbrl.org/dim/conf/primary}Sales")
    val targetName = EName("{http://www.conformance-dimensions.com/xbrl/}Cube")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isNotAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(6) {
      taxo.findAllDimensionalRelationshipsOfType(classTag[DimensionalRelationship]).size
    }

    assertResult(true) {
      taxo.findPrimaryItemDeclaration(sourceName).isDefined
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(targetName).isDefined
    }

    assertResult("segment") {
      hasHypercubes.head.contextElement
    }
    assertResult("http://www.xbrl.org/2003/role/Cube") {
      hasHypercubes.head.effectiveTargetRole
    }
  }

  test("testHasHypercubeAllAbsPriItemValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllAbsPriItemValid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllAbsPriItemValid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.example.com/new}AbstractPrimaryItem")
    val targetName = EName("{http://www.example.com/new}Hypercube")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(2) {
      taxo.findAllDimensionalRelationshipsOfType(classTag[DimensionalRelationship]).size
    }

    assertResult(Some(sourceName)) {
      taxo.findPrimaryItemDeclaration(sourceName).filter(_.isAbstract).map(_.targetEName)
    }
    assertResult(Some(targetName)) {
      taxo.findHypercubeDeclaration(targetName).map(_.targetEName)
    }
  }

  test("testHasHypercubeAllTwoCubesValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllTwoCubesValid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllTwoCubesValid-definition.xml",
      "lib/base/primary.xsd",
      "lib/base/products.xsd"))

    val salesEName = EName("{http://www.xbrl.org/dim/conf/primary}Sales")
    val excludedSalesCubeEName = EName("{http://www.conformance-dimensions.com/xbrl/}ExcludedSalesCube")
    val incomeStatementEName = EName("{http://www.xbrl.org/dim/conf/primary}IncomeStatement")
    val allProductsCubeEName = EName("{http://www.conformance-dimensions.com/xbrl/}AllProductsCube")
    val wineSalesDimEName = EName("{http://www.conformance-dimensions.com/xbrl/}WineSalesDim")
    val allProductsDimEName = EName("{http://www.conformance-dimensions.com/xbrl/}AllProductsDim")

    val salesHasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(salesEName)

    val salesHypercubeDimensions =
      salesHasHypercubes.flatMap(hh => taxo.findAllConsecutiveHypercubeDimensionRelationships(hh))

    val incomeStatementHasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(incomeStatementEName)

    val incomeStatementHypercubeDimensions =
      incomeStatementHasHypercubes.flatMap(hh => taxo.findAllConsecutiveHypercubeDimensionRelationships(hh))

    assertResult(1) {
      salesHasHypercubes.size
    }
    assertResult(1) {
      salesHypercubeDimensions.size
    }

    assertResult(1) {
      incomeStatementHasHypercubes.size
    }
    assertResult(1) {
      incomeStatementHypercubeDimensions.size
    }

    assertResult((salesEName, excludedSalesCubeEName, "http://www.xbrl.org/2003/role/link", "http://www.xbrl.org/2003/role/Cube")) {
      val hh = salesHasHypercubes.head
      (hh.primary, hh.hypercube, hh.elr, hh.effectiveTargetRole)
    }
    assertResult((excludedSalesCubeEName, wineSalesDimEName, "http://www.xbrl.org/2003/role/Cube", "http://www.xbrl.org/2003/role/Cube")) {
      val hd = salesHypercubeDimensions.head
      (hd.hypercube, hd.dimension, hd.elr, hd.effectiveTargetRole)
    }

    assertResult((incomeStatementEName, allProductsCubeEName, "http://www.xbrl.org/2003/role/link", "http://www.xbrl.org/2003/role/Cube")) {
      val hh = incomeStatementHasHypercubes.head
      (hh.primary, hh.hypercube, hh.elr, hh.effectiveTargetRole)
    }
    assertResult((allProductsCubeEName, allProductsDimEName, "http://www.xbrl.org/2003/role/Cube", "http://www.xbrl.org/2003/role/Cube")) {
      val hd = incomeStatementHypercubeDimensions.head
      (hd.hypercube, hd.dimension, hd.elr, hd.effectiveTargetRole)
    }

    assertResult(Map(wineSalesDimEName -> Set(EName("{http://www.xbrl.org/dim/conf/product}Wine")))) {
      taxo.findAllUsableDimensionMembers(salesHasHypercubes.head)
    }

    assertResult(Map(allProductsDimEName -> Set(EName("{http://www.xbrl.org/dim/conf/product}AllProducts")))) {
      taxo.findAllUsableDimensionMembers(incomeStatementHasHypercubes.head)
    }
  }

  test("testHasHypercubeAllSubsValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllSubsValid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/hasHypercubeAllSubsValid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.example.com/new}PrimaryItem")
    val targetName = EName("{http://www.example.com/new}Hypercube")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }

    assertResult(Some(sourceName)) {
      taxo.findPrimaryItemDeclaration(sourceName).map(_.targetEName)
    }
    assertResult(Some(EName("{http://www.example.com/new}PrimaryItemParent"))) {
      taxo.findPrimaryItemDeclaration(sourceName).flatMap(_.substitutionGroupOption)
    }
    assertResult(Some(ENames.XbrliItemEName)) {
      taxo.findPrimaryItemDeclaration(EName("{http://www.example.com/new}PrimaryItemParent")).flatMap(_.substitutionGroupOption)
    }

    assertResult(Some(targetName)) {
      taxo.findHypercubeDeclaration(targetName).map(_.targetEName)
    }
    assertResult(Some(EName("{http://www.example.com/new}HypercubeParent"))) {
      taxo.findHypercubeDeclaration(targetName).flatMap(_.substitutionGroupOption)
    }
    assertResult(Some(ENames.XbrldtHypercubeItemEName)) {
      taxo.findHypercubeDeclaration(EName("{http://www.example.com/new}HypercubeParent")).flatMap(_.substitutionGroupOption)
    }
  }

  test("testSourceHasHypercubeIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/sourceHasHypercubeIsDimensionInvalid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/sourceHasHypercubeIsDimensionInvalid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.example.com/new}PrimaryItem")

    val hasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(sourceName)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      taxo.findDimensionDeclaration(hasHypercubes.head.sourceConceptEName).
        flatMap(_.substitutionGroupOption)
    }
  }

  test("testSourceHasHypercubeIsDimensionSubInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/104-HasHypercubeSourceError/sourceHasHypercubeIsDimensionSubInvalid.xsd",
      "100-xbrldte/104-HasHypercubeSourceError/sourceHasHypercubeIsDimensionSubInvalid-definition.xml",
      "lib/base/primary.xsd"))

    val sourceName = EName("{http://www.example.com/new}PrimaryItem")

    val hasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(sourceName)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(Some(EName("{http://www.example.com/new}PrimaryItemParent"))) {
      taxo.findDimensionDeclaration(hasHypercubes.head.sourceConceptEName).
        flatMap(_.substitutionGroupOption)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(hasHypercubes.head.sourceConceptEName).isDefined
    }
    assertResult(false) {
      taxo.findTypedDimensionDeclaration(hasHypercubes.head.sourceConceptEName).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(hasHypercubes.head.sourceConceptEName).isDefined
    }
  }

  test("testDimensionDomainValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/112-DimensionDomainSourceError/dimensionDomainValid.xsd",
      "100-xbrldte/112-DimensionDomainSourceError/dimensionDomainValid-definition.xml"))

    val tns = "http://www.example.com/new"

    val primary = EName(tns, "PrimaryItemMeasure")
    val dimension = EName(tns, "DimensionDom")

    val dimensionDomains = taxo.findAllOutgoingDimensionDomainRelationships(dimension)

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(dimension) {
      dimensionDomains.head.sourceConceptEName
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findTupleDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findItemDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findConceptDeclaration(dimension).nonEmpty
    }

    val hasHypercube = taxo.findAllOutgoingHasHypercubeRelationships(primary).head

    assertResult(Map(dimension -> Set(EName(tns, "Domain"), EName(tns, "DomainMember")))) {
      taxo.findAllUsableDimensionMembers(hasHypercube)
    }
  }

  test("testDimensionDomainSubsValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/112-DimensionDomainSourceError/dimensionDomainSubsValid.xsd",
      "100-xbrldte/112-DimensionDomainSourceError/dimensionDomainSubsValid-definition.xml"))

    val tns = "http://www.example.com/new"

    val primary = EName(tns, "PrimaryItemMeasure")
    val dimension = EName(tns, "DimensionDom")

    val dimensionDomains = taxo.findAllOutgoingDimensionDomainRelationships(dimension)

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(dimension) {
      dimensionDomains.head.sourceConceptEName
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findTupleDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findItemDeclaration(dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findConceptDeclaration(dimension).nonEmpty
    }

    assertResult(true) {
      taxo.findPrimaryItemDeclaration(primary).isDefined
    }
    assertResult(Some(EName(tns, "PrimaryItemParent"))) {
      taxo.findPrimaryItemDeclaration(primary).flatMap(_.substitutionGroupOption)
    }
    assertResult(true) {
      taxo.getPrimaryItemDeclaration(primary).globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrliItemEName,
        taxo.substitutionGroupMap)
    }

    val domainMember = EName(tns, "DomainMember")

    assertResult(true) {
      taxo.findPrimaryItemDeclaration(domainMember).isDefined
    }
    assertResult(Some(EName(tns, "PrimaryItemParent"))) {
      taxo.findPrimaryItemDeclaration(domainMember).flatMap(_.substitutionGroupOption)
    }
    assertResult(true) {
      taxo.getPrimaryItemDeclaration(domainMember).globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrliItemEName,
        taxo.substitutionGroupMap)
    }

    val hasHypercube = taxo.findAllOutgoingHasHypercubeRelationships(primary).head

    assertResult(Map(dimension -> Set(EName(tns, "Domain"), EName(tns, "DomainMember")))) {
      taxo.findAllUsableDimensionMembers(hasHypercube)
    }
  }

  test("testSourceDimensionDomainIsItemInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/112-DimensionDomainSourceError/sourceDimensionDomainIsItemInvalid.xsd",
      "100-xbrldte/112-DimensionDomainSourceError/sourceDimensionDomainIsItemInvalid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(false) {
      taxo.findDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findExplicitDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findItemDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findPrimaryItemDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
  }

  test("testSourceDimensionDomainIsHypercubeInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/112-DimensionDomainSourceError/sourceDimensionDomainIsHypercubeInvalid.xsd",
      "100-xbrldte/112-DimensionDomainSourceError/sourceDimensionDomainIsHypercubeInvalid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(false) {
      taxo.findDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findExplicitDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findItemDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
  }

  test("testSourceExplicitDimensionIsTypedDomainInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/112-DimensionDomainSourceError/sourceExplicitDimensionIsTypedDomainInvalid.xsd",
      "100-xbrldte/112-DimensionDomainSourceError/sourceExplicitDimensionIsTypedDomainInvalid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(dimensionDomains.map(_.sourceConceptEName)) {
      dimensionDomains.map(_.dimension)
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findExplicitDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findTypedDimensionDeclaration(dimensionDomains.head.dimension).nonEmpty
    }
  }

  test("testSourceDomainMemberIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/116-DomainMemberSourceError/sourceDomainMemberIsDimensionInvalid.xsd",
      "100-xbrldte/116-DomainMemberSourceError/sourceDomainMemberIsDimensionInvalid-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    assertResult(1) {
      domainMembers.size
    }

    assertResult(domainMembers.map(_.sourceConceptEName)) {
      domainMembers.map(_.domain)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(domainMembers.head.domain).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(domainMembers.head.domain).nonEmpty
    }

    assertResult(domainMembers.map(_.domain).toSet) {
      domainMembers.map(_.domain).flatMap(dom => taxo.findAllOutgoingDomainMemberRelationships(dom)).map(_.domain).toSet
    }
  }

  test("testSourceDomainMemberIsHypercubeInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/116-DomainMemberSourceError/sourceDomainMemberIsHypercubeInvalid.xsd",
      "100-xbrldte/116-DomainMemberSourceError/sourceDomainMemberIsHypercubeInvalid-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    assertResult(1) {
      domainMembers.size
    }

    assertResult(domainMembers.map(_.sourceConceptEName)) {
      domainMembers.map(_.domain)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(domainMembers.head.domain).nonEmpty
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(domainMembers.head.domain).nonEmpty
    }
  }

  test("testDimensionDefaultValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/122-DimensionDefaultSourceError/dimensionDefaultValid.xsd",
      "100-xbrldte/122-DimensionDefaultSourceError/dimensionDefaultValid-definition.xml"))

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(1) {
      dimensionDefaults.size
    }

    assertResult(dimensionDefaults.map(_.sourceConceptEName)) {
      dimensionDefaults.map(_.dimension)
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }

    assertResult(dimensionDefaults.map(_.dimension).toSet) {
      dimensionDefaults.map(_.dimension).flatMap(dim => taxo.findAllOutgoingDimensionDefaultRelationships(dim)).map(_.dimension).toSet
    }
  }

  test("testSourceDimensionDefaultNotDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/122-DimensionDefaultSourceError/sourceDimensionDefaultNotDimensionInvalid.xsd",
      "100-xbrldte/122-DimensionDefaultSourceError/sourceDimensionDefaultNotDimensionInvalid-definition.xml"))

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(1) {
      dimensionDefaults.size
    }

    assertResult(dimensionDefaults.map(_.sourceConceptEName)) {
      dimensionDefaults.map(_.dimension)
    }
    assertResult(false) {
      taxo.findDimensionDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimensionDefaults.head.dimension).nonEmpty
    }
  }

  private def makeTestTaxonomy(relativeDocPaths: immutable.IndexedSeq[String]): BasicTaxonomy = {
    val rootDir = new File(classOf[DimensionalRelationshipSourceTest].getResource("/conf-suite-dim").toURI)
    val docFiles = relativeDocPaths.map(relativePath => new File(rootDir, relativePath))

    val rootElems = docFiles.map(f => docBuilder.build(f.toURI))

    val taxoRootElems = rootElems.map(e => TaxonomyElem.build(e))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo =
      BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.StrictInstance)
    richTaxo
  }

  private val processor = new Processor(false)

  private val docBuilder = new SaxonDocumentBuilder(processor.newDocumentBuilder(), (uri => uri))
}
