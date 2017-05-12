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

import java.io.File

import scala.collection.immutable
import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Dimensional querying test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class DimensionalQueryTest extends FunSuite {

  test("testAbstractHypercube") {
    val taxo = makeTestTaxonomy(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeValid.xsd"))

    val hypercubeName = EName("{http://www.xbrl.org/dim/conf/100/hypercubeValid}MyHypercube")
    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)

    assertResult(hypercubeName) {
      hypercube.targetEName
    }
    assertResult(true) {
      hypercube.globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtHypercubeItemEName,
        taxo.substitutionGroupMap)
    }

    assertResult(true) {
      hypercube.isAbstract
    }
    assertResult(true) {
      hypercube.globalElementDeclaration.isAbstract
    }
  }

  test("testNonAbstractHypercube") {
    val taxo = makeTestTaxonomy(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstract.xsd"))

    val hypercubeName = EName("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyHypercube")
    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)

    assertResult(hypercubeName) {
      hypercube.targetEName
    }
    assertResult(true) {
      hypercube.globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtHypercubeItemEName,
        taxo.substitutionGroupMap)
    }

    assertResult(false) {
      hypercube.isAbstract
    }
    assertResult(false) {
      hypercube.globalElementDeclaration.isAbstract
    }
  }

  test("testNonAbstractHypercubeWithSGComplexities") {
    val taxo = makeTestTaxonomy(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstractWithSGComplexities.xsd"))

    val hypercubeName = EName("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyHypercube")
    val otherHypercubeName = EName("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyOtherHypercube")

    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)
    val otherHypercube = taxo.getHypercubeDeclaration(otherHypercubeName)

    assertResult(hypercubeName) {
      hypercube.targetEName
    }
    assertResult(otherHypercubeName) {
      otherHypercube.targetEName
    }
    assertResult(Some(hypercubeName)) {
      otherHypercube.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(true) {
      hypercube.globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtHypercubeItemEName,
        taxo.substitutionGroupMap)
    }
    assertResult(true) {
      otherHypercube.globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtHypercubeItemEName,
        taxo.substitutionGroupMap)
    }

    assertResult(true) {
      hypercube.isAbstract
    }
    assertResult(false) {
      otherHypercube.isAbstract
    }
    assertResult(true) {
      hypercube.globalElementDeclaration.isAbstract
    }
    assertResult(false) {
      otherHypercube.globalElementDeclaration.isAbstract
    }
  }

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
      taxo.getHypercubeDeclaration(hypercubeDimensions.head.hypercube).globalElementDeclaration.substitutionGroupOption
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
      taxo.getExplicitDimensionDeclaration(hypercubeDimensions.head.hypercube).globalElementDeclaration.substitutionGroupOption
    }
    assertResult(true) {
      taxo.getItemDeclaration(hypercubeDimensions.head.hypercube).globalElementDeclaration.hasSubstitutionGroup(
        ENames.XbrldtDimensionItemEName,
        taxo.substitutionGroupMap)
    }
  }

  test("testTargetHypercubeDimensionIsItemInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsItemInvalid.xsd",
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsItemInvalid-definition.xml"))

    val invalidDimensionName = EName("{http://www.xbrl.org/dim/conf}ProdDim")
    val validDimensionName = EName("{http://www.xbrl.org/dim/conf}RegionDim")

    val hypercubeDimensions = taxo.findAllHypercubeDimensionRelationships

    assertResult(2) {
      hypercubeDimensions.size
    }

    assertResult(Set(invalidDimensionName, validDimensionName)) {
      hypercubeDimensions.map(_.targetConceptEName).toSet
    }
    assertResult(Set(invalidDimensionName, validDimensionName)) {
      hypercubeDimensions.map(_.dimension).toSet
    }

    assertResult(true) {
      taxo.findItemDeclaration(invalidDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findItemDeclaration(validDimensionName).isDefined
    }

    // Not a dimension
    assertResult(false) {
      taxo.findDimensionDeclaration(invalidDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(validDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(validDimensionName).isDefined
    }
    assertResult(false) {
      taxo.findTypedDimensionDeclaration(validDimensionName).isDefined
    }

    // Not a dimension
    assertResult(true) {
      taxo.findPrimaryItemDeclaration(invalidDimensionName).isDefined
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(validDimensionName).isDefined
    }
  }

  test("testTargetHypercubeDimensionIsTupleInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsTupleInvalid.xsd",
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsTupleInvalid-definition.xml"))

    val invalidDimensionName = EName("{http://www.xbrl.org/dim/conf}ProdDim")
    val validDimensionName = EName("{http://www.xbrl.org/dim/conf}RegionDim")

    val hypercubeDimensions = taxo.findAllHypercubeDimensionRelationships

    assertResult(2) {
      hypercubeDimensions.size
    }

    assertResult(Set(invalidDimensionName, validDimensionName)) {
      hypercubeDimensions.map(_.targetConceptEName).toSet
    }
    assertResult(Set(invalidDimensionName, validDimensionName)) {
      hypercubeDimensions.map(_.dimension).toSet
    }

    assertResult(true) {
      taxo.findConceptDeclaration(invalidDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findConceptDeclaration(validDimensionName).isDefined
    }

    // Not a dimension
    assertResult(false) {
      taxo.findDimensionDeclaration(invalidDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(validDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(validDimensionName).isDefined
    }
    assertResult(false) {
      taxo.findTypedDimensionDeclaration(validDimensionName).isDefined
    }

    // Not a dimension
    assertResult(true) {
      taxo.findTupleDeclaration(invalidDimensionName).isDefined
    }
    assertResult(false) {
      taxo.findTupleDeclaration(validDimensionName).isDefined
    }
  }

  test("testTargetHypercubeDimensionIsHypercubeInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsHypercubeInvalid.xsd",
      "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsHypercubeInvalid-definition.xml"))

    val invalidDimensionName = EName("{http://www.xbrl.org/dim/conf}ProdDim")

    val incomingHypercubeDimensions =
      taxo.findAllIncomingInterConceptRelationshipsOfType(invalidDimensionName, classTag[HypercubeDimensionRelationship])

    assertResult(1) {
      incomingHypercubeDimensions.size
    }

    assertResult(true) {
      taxo.findConceptDeclaration(invalidDimensionName).isDefined
    }

    // Not a dimension
    assertResult(false) {
      taxo.findDimensionDeclaration(invalidDimensionName).isDefined
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(invalidDimensionName).isDefined
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
      salesHasHypercubes.flatMap(hh => taxo.filterOutgoingHypercubeDimensionRelationships(hh.hypercube)(hh.isFollowedBy(_)))

    val incomeStatementHasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(incomeStatementEName)

    val incomeStatementHypercubeDimensions =
      incomeStatementHasHypercubes.flatMap(hh => taxo.filterOutgoingHypercubeDimensionRelationships(hh.hypercube)(hh.isFollowedBy(_)))

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
      taxo.findPrimaryItemDeclaration(sourceName).flatMap(_.globalElementDeclaration.substitutionGroupOption)
    }
    assertResult(Some(ENames.XbrliItemEName)) {
      taxo.findPrimaryItemDeclaration(EName("{http://www.example.com/new}PrimaryItemParent")).flatMap(_.globalElementDeclaration.substitutionGroupOption)
    }

    assertResult(Some(targetName)) {
      taxo.findHypercubeDeclaration(targetName).map(_.targetEName)
    }
    assertResult(Some(EName("{http://www.example.com/new}HypercubeParent"))) {
      taxo.findHypercubeDeclaration(targetName).flatMap(_.globalElementDeclaration.substitutionGroupOption)
    }
    assertResult(Some(ENames.XbrldtHypercubeItemEName)) {
      taxo.findHypercubeDeclaration(EName("{http://www.example.com/new}HypercubeParent")).flatMap(_.globalElementDeclaration.substitutionGroupOption)
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
        flatMap(_.globalElementDeclaration.substitutionGroupOption)
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
        flatMap(_.globalElementDeclaration.substitutionGroupOption)
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

  test("testTargetHasHypercubeIsItemInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsItemInvalid.xsd",
      "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsItemInvalid-definition.xml"))

    val targetName = EName("{http://www.example.com/new}Hypercube")

    val hasHypercubes =
      taxo.findAllIncomingInterConceptRelationshipsOfType(targetName, classTag[HasHypercubeRelationship])

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(Some(ENames.XbrliItemEName)) {
      taxo.findConceptDeclaration(targetName).flatMap(_.globalElementDeclaration.substitutionGroupOption)
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(targetName).isDefined
    }
    assertResult(true) {
      taxo.findPrimaryItemDeclaration(targetName).isDefined
    }
  }

  test("testTargetHasHypercubeIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsDimensionInvalid.xsd",
      "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsDimensionInvalid-definition.xml"))

    val targetName = EName("{http://www.example.com/new}Hypercube")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(false) {
      taxo.findHypercubeDeclaration(targetName).isDefined
    }
    assertResult(true) {
      taxo.findExplicitDimensionDeclaration(targetName).isDefined
    }
  }

  test("testHasHypercubeNoContextElementInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/106-HasHypercubeMissingContextElementAttributeError/hasHypercubeNoContextElementInvalid.xsd",
      "100-xbrldte/106-HasHypercubeMissingContextElementAttributeError/hasHypercubeNoContextElementInvalid-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(true) {
      hasHypercubes.head.arc.attributeOption(ENames.XbrldtContextElementEName).isEmpty
    }
  }

  test("testNotAllHasHypercubeNoContextElementInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/106-HasHypercubeMissingContextElementAttributeError/hasHypercubeNoContextElementInvalid.xsd",
      "100-xbrldte/106-HasHypercubeMissingContextElementAttributeError/notAllHasHypercubeNoContextElementInvalid-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isNotAllRelationship)

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(true) {
      hasHypercubes.head.arc.attributeOption(ENames.XbrldtContextElementEName).isEmpty
    }
  }

  private def makeTestTaxonomy(relativeDocPaths: immutable.IndexedSeq[String]): BasicTaxonomy = {
    val rootDir = new File(classOf[DimensionalQueryTest].getResource("/conf-suite-dim").toURI)
    val docFiles = relativeDocPaths.map(relativePath => new File(rootDir, relativePath))

    val rootElems = docFiles.map(f => docBuilder.build(f.toURI))

    val taxoRootElems = rootElems.map(e => TaxonomyElem.build(e))

    val underlyingTaxo = TaxonomyBase.build(taxoRootElems)
    val richTaxo =
      BasicTaxonomy.build(underlyingTaxo, SubstitutionGroupMap.Empty, DefaultRelationshipFactory.LenientInstance)
    richTaxo
  }

  private val processor = new Processor(false)

  private val docBuilder = new SaxonDocumentBuilder(processor.newDocumentBuilder(), (uri => uri))
}
