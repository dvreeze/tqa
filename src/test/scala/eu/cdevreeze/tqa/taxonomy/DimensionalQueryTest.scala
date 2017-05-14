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
import eu.cdevreeze.tqa.dom.LocalElementDeclaration
import eu.cdevreeze.tqa.dom.RoleRef
import eu.cdevreeze.tqa.dom.RoleType
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

  test("testHypercubeDimensionTargetRoleValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/hypercubeDimensionTargetRoleValid.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/hypercubeDimensionTargetRoleValid-definition.xml"))

    val hypercube = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(hypercube)

    val expectedTargetRole = "http://www.xbrl.org/dim/conf/role/role-products"

    assertResult(1) {
      hypercubeDimensions.size
    }
    assertResult(expectedTargetRole) {
      hypercubeDimensions.head.effectiveTargetRole
    }

    assertResult(true) {
      val rootElem = taxo.getRootElem(hypercubeDimensions.head.arc)
      val roleRefOption = rootElem.findElemOfType(classTag[RoleRef])(_.roleUri == expectedTargetRole)
      roleRefOption.nonEmpty
    }
    assertResult(true) {
      taxo.rootElems.flatMap(_.findElemOfType(classTag[RoleType])(_.roleUri == expectedTargetRole)).nonEmpty
    }

    assertResult(true) {
      taxo.filterDimensionalRelationshipsOfType(classTag[DimensionalRelationship])(_.elr == expectedTargetRole).nonEmpty
    }
  }

  test("testHypercubeDimensionTargetRoleNotResolved") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/hypercubeDimensionTargetRoleNotResolved.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/hypercubeDimensionTargetRoleNotResolved-definition.xml"))

    val hypercubeDimensions = taxo.findAllHypercubeDimensionRelationships

    val targetRole = "http://www.xbrl.org/dim/conf/role/foobar"

    assertResult(1) {
      hypercubeDimensions.size
    }
    assertResult(targetRole) {
      hypercubeDimensions.head.effectiveTargetRole
    }

    assertResult(true) {
      val rootElem = taxo.getRootElem(hypercubeDimensions.head.arc)
      val roleRefOption = rootElem.findElemOfType(classTag[RoleRef])(_.roleUri == targetRole)
      roleRefOption.isEmpty
    }
  }

  test("testHasHypercubeTargetRoleValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/hasHypercubeTargetRoleValid.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/hasHypercubeTargetRoleValid-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult("http://www.xbrl.org/2003/role/link") {
      hasHypercubes.head.elr
    }
    // Standard role. Not roleRef needed.
    assertResult(hasHypercubes.head.elr) {
      hasHypercubes.head.effectiveTargetRole
    }
  }

  test("testHasHypercubeTargetRoleNotResolved") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/hasHypercubeTargetRoleNotResolved.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/hasHypercubeTargetRoleNotResolved-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships

    val targetRole = "http://www.example.com/new/role/role-foobar"

    assertResult(1) {
      hasHypercubes.size
    }
    assertResult(targetRole) {
      hasHypercubes.head.effectiveTargetRole
    }
    assertResult(None) {
      taxo.getRootElem(hasHypercubes.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole)
    }
  }

  test("testDimensionDomainTargetRoleValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/dimensionDomainTargetRoleValid.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/dimensionDomainTargetRoleValid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }
    assertResult("http://www.example.com/new/role/role-cube") {
      dimensionDomains.head.elr
    }
    // Standard role. Not roleRef needed.
    assertResult("http://www.xbrl.org/2003/role/link") {
      dimensionDomains.head.effectiveTargetRole
    }
  }

  test("testDimensionDomainTargetRoleNotResolved") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/dimensionDomainTargetRoleNotResolved.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/dimensionDomainTargetRoleNotResolved-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    val targetRole = "http://www.xbrl.org/2003/role/foobar"

    assertResult(1) {
      dimensionDomains.size
    }
    assertResult("http://www.example.com/new/role/role-cube") {
      dimensionDomains.head.elr
    }
    assertResult(targetRole) {
      dimensionDomains.head.effectiveTargetRole
    }
    assertResult(None) {
      taxo.getRootElem(dimensionDomains.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole)
    }
  }

  test("testDomainMemberTargetRoleValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleValid.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleValid-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    val targetRole = "http://www.example.com/new/role/secondary"

    assertResult(Set("http://www.xbrl.org/2003/role/link", targetRole)) {
      domainMembers.map(_.elr).toSet
    }
    assertResult(Set(targetRole)) {
      domainMembers.map(_.effectiveTargetRole).toSet
    }
    assertResult(true) {
      taxo.getRootElem(domainMembers.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole).nonEmpty
    }
  }

  test("testDomainMemberTargetRoleNotResolved") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleNotResolved.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleNotResolved-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    val targetRole = "http://www.example.com/new/role/foobar"
    val knownRole = "http://www.example.com/new/role/secondary"

    assertResult(Set("http://www.xbrl.org/2003/role/link", knownRole)) {
      domainMembers.map(_.elr).toSet
    }
    assertResult(Set(targetRole, knownRole)) {
      domainMembers.map(_.effectiveTargetRole).toSet
    }
    assertResult(false) {
      taxo.getRootElem(domainMembers.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole).nonEmpty
    }
    assertResult(true) {
      taxo.getRootElem(domainMembers.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == knownRole).nonEmpty
    }
  }

  test("testNotAllHasHypercubeTargetRoleValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/notAllHasHypercubeTargetRoleValid.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/notAllHasHypercubeTargetRoleValid-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isNotAllRelationship)

    val targetRole = "http://www.xbrl.org/2003/role/link"

    // Standard role. No roleRef needed.
    assertResult(Set(targetRole)) {
      hasHypercubes.map(_.elr).toSet
    }
    assertResult(Set(targetRole)) {
      hasHypercubes.map(_.effectiveTargetRole).toSet
    }
    assertResult(false) {
      taxo.getRootElem(hasHypercubes.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole).nonEmpty
    }
  }

  test("testNotAllHasHypercubeTargetRoleNotResolved") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/notAllHasHypercubeTargetRoleNotResolved.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/notAllHasHypercubeTargetRoleNotResolved-definition.xml"))

    val hasHypercubes = taxo.findAllHasHypercubeRelationships.filter(_.isNotAllRelationship)

    val elr = "http://www.xbrl.org/2003/role/link"
    val targetRole = "http://www.example.com/new/role/role-foobar"

    // Standard role. No roleRef needed.
    assertResult(Set(elr)) {
      hasHypercubes.map(_.elr).toSet
    }
    // Non-standard role. RoleRef needed bu absent.
    assertResult(Set(targetRole)) {
      hasHypercubes.map(_.effectiveTargetRole).toSet
    }
    assertResult(false) {
      taxo.getRootElem(hasHypercubes.head.arc).findElemOfType(classTag[RoleRef])(_.roleUri == targetRole).nonEmpty
    }
  }

  test("testDomainMemberTargetRoleMissingRoleRef") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleMissingRoleRef.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleMissingRoleRef-definition.xml",
      "100-xbrldte/107-TargetRoleNotResolvedError/domainMemberTargetRoleMissingRoleRef-definition2.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    val standardElr = "http://www.xbrl.org/2003/role/link"
    val otherElr = "http://www.example.com/new/role/foobar"

    assertResult(Set(standardElr, otherElr)) {
      domainMembers.map(_.elr).toSet
    }
    assertResult(Set(otherElr)) {
      domainMembers.map(_.effectiveTargetRole).toSet
    }

    val rootElems = domainMembers.map(dm => taxo.getRootElem(dm.arc)).distinct

    assertResult(2) {
      rootElems.size
    }
    // Missing one roleRef for otherElr
    assertResult(1) {
      rootElems.flatMap(_.findElemOfType(classTag[RoleRef])(_.roleUri == otherElr)).size
    }
  }

  test("testUnconnectedDRS") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/107-TargetRoleNotResolvedError/unconnectedDRS.xsd",
      "100-xbrldte/107-TargetRoleNotResolvedError/unconnectedDRS-definition.xml"))

    val hypercube = EName("{http://www.xbrl.org/dim/conf}AllCube")

    val hypercubeDimensions = taxo.findAllOutgoingHypercubeDimensionRelationships(hypercube)

    assertResult(1) {
      hypercubeDimensions.size
    }

    val dimension = hypercubeDimensions.head.dimension

    val dimensionDomains = taxo.findAllOutgoingDimensionDomainRelationships(dimension)

    assertResult(1) {
      dimensionDomains.size
    }
    assertResult(false) {
      hypercubeDimensions.head.isFollowedBy(dimensionDomains.head)
    }
    assertResult(false) {
      hypercubeDimensions.head.effectiveTargetRole == dimensionDomains.head.elr
    }
  }

  test("testDimensionDeclarationValid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/108-DimensionElementIsNotAbstractError/dimensionValid.xsd"))

    val dimension = EName("{http://www.xbrl.org/dim/conf/110/dimensionValid}MyDimension")

    val dimDecl = taxo.getDimensionDeclaration(dimension)

    assertResult(true) {
      dimDecl.isAbstract
    }
    assertResult(false) {
      dimDecl.isConcrete
    }
    assertResult(true) {
      dimDecl.globalElementDeclaration.isAbstract
    }
  }

  test("testDimensionDeclarationNotAbstractInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/108-DimensionElementIsNotAbstractError/dimensionNotAbstract.xsd"))

    val dimension = EName("{http://www.xbrl.org/dim/conf/110/dimensionNotAbstract}MyDimension")

    val dimDecl = taxo.getDimensionDeclaration(dimension)

    assertResult(false) {
      dimDecl.isAbstract
    }
    assertResult(true) {
      dimDecl.isConcrete
    }
    assertResult(true) {
      dimDecl.globalElementDeclaration.isConcrete
    }
  }

  test("testTypedDomainRefvalid-1") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/109-TypedDomainRefError/typedDomainRefvalid.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimension = EName(tns, "dPhone")

    val dimDecl = taxo.getTypedDimensionDeclaration(dimension)

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      dimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(dimDecl.globalElementDeclaration.baseUri.resolve("#duriv_phone")) {
      dimDecl.typedDomainRef
    }
    assertResult(true) {
      taxo.findGlobalElementDeclarationByUri(dimDecl.typedDomainRef).nonEmpty
    }
    assertResult(None) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).substitutionGroupOption
    }
    assertResult(true) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).isConcrete
    }
    assertResult(EName(tns, "phone")) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).targetEName
    }
  }

  test("testTypedDomainRefvalid-2") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/109-TypedDomainRefError/typedDomainRefvalid2.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimension = EName(tns, "dPhone")

    val dimDecl = taxo.getTypedDimensionDeclaration(dimension)

    assertResult(Some(EName(tns, "headPhone"))) {
      dimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      taxo.getDimensionDeclaration(EName(tns, "headPhone")).globalElementDeclaration.substitutionGroupOption
    }

    assertResult(dimDecl.globalElementDeclaration.baseUri.resolve("#duriv_phone")) {
      dimDecl.typedDomainRef
    }
    assertResult(true) {
      taxo.findGlobalElementDeclarationByUri(dimDecl.typedDomainRef).nonEmpty
    }
    assertResult(None) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).substitutionGroupOption
    }
    assertResult(true) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).isConcrete
    }
    assertResult(EName(tns, "phone")) {
      taxo.getGlobalElementDeclarationByUri(dimDecl.typedDomainRef).targetEName
    }
  }

  test("testTypedDomainRefonNonItemDeclaration") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/109-TypedDomainRefError/typedDomainRefonNonItemDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val wrongDimension = EName(tns, "dPhone")

    val wrongDimDecl = taxo.getConceptDeclaration(wrongDimension)

    assertResult(true) {
      wrongDimDecl.globalElementDeclaration.attributeOption(ENames.XbrldtTypedDomainRefEName).isDefined
    }

    assertResult(true) {
      taxo.findTupleDeclaration(wrongDimension).nonEmpty
    }
    assertResult(false) {
      taxo.findItemDeclaration(wrongDimension).nonEmpty
    }
  }

  test("testTwotypedDomainRefattributesContainSameRef") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/109-TypedDomainRefError/TwotypedDomainRefattributesContainSameRef.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecls = taxo.findAllTypedDimensionDeclarations

    assertResult(List(EName(tns, "dPhone"), EName(tns, "dFax"))) {
      typedDimDecls.map(_.targetEName)
    }
    // Only 1 typed domain, shared by 2 typed dimensions
    assertResult(Set(typedDimDecls.head.globalElementDeclaration.baseUri.resolve("#duriv_phone"))) {
      typedDimDecls.map(_.typedDomainRef).toSet
    }

    assertResult(true) {
      taxo.findGlobalElementDeclarationByUri(typedDimDecls.head.typedDomainRef).nonEmpty
    }
  }

  test("testTwotypedDomainRefattributesContainRefsLocatingSameElement") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/109-TypedDomainRefError/TwotypedDomainRefattributesContainRefsLocatingSameElement.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecls = taxo.findAllTypedDimensionDeclarations

    assertResult(List(EName(tns, "dPhone"), EName(tns, "dFax"))) {
      typedDimDecls.map(_.targetEName)
    }
    // Only 1 typed domain, shared by 2 typed dimensions, although the (unresolved) typed domain references differ
    assertResult(Set(typedDimDecls.head.globalElementDeclaration.baseUri.resolve("#duriv_phone"))) {
      typedDimDecls.map(_.typedDomainRef).toSet
    }

    assertResult(true) {
      taxo.findGlobalElementDeclarationByUri(typedDimDecls.head.typedDomainRef).nonEmpty
    }
  }

  test("testTypedDomainReflocatesDeclarationInSameFile") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInSameFile.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      taxo.getDimensionDeclaration(EName(tns, "headPhone")).
        globalElementDeclaration.substitutionGroupOption
    }

    assertResult(true) {
      taxo.findGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef).nonEmpty
    }
  }

  test("testTypedDomainReftoAbstractItemDeclaration") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReftoAbstractItemDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }

    assertResult(false) {
      taxo.getGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef).isConcrete
    }
  }

  test("testTypedDomainReflocatesTypeDeclaration") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesTypeDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      taxo.getDimensionDeclaration(EName(tns, "headPhone")).
        globalElementDeclaration.substitutionGroupOption
    }

    assertResult(false) {
      taxo.findGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef).nonEmpty
    }
    assertResult(true) {
      taxo.findNamedTypeDefinition(_.idOption == Some(typedDimDecl.typedDomainRef).map(_.getFragment)).nonEmpty
    }
  }

  test("testTypedDomainRefLocatesNonGlobalElementDeclaration") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainRefLocatesNonGlobalElementDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }

    assertResult(false) {
      taxo.findGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef).nonEmpty
    }

    val localElemDeclOption =
      taxo.getRootElem(typedDimDecl.globalElementDeclaration).
        findElemOfType(classTag[LocalElementDeclaration])(_.idOption == Some(typedDimDecl.typedDomainRef).map(_.getFragment))

    assertResult(true) {
      localElemDeclOption.nonEmpty
    }
    assertResult(EName(tns, "country")) {
      EName(
        localElemDeclOption.get.schemaTargetNamespaceOption,
        localElemDeclOption.get.nameAttributeValue)
    }
  }

  test("testTypedDomainReflocatesDeclarationInDifferentFileWithImport") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInDifferentFileWithImport.xsd",
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInDifferentFile_File2.xsd"))

    val tns1 = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"
    val tns2 = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid_File2"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns1, "dPhone"))

    assertResult(Some(EName(tns1, "headPhone"))) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(true) {
      typedDimDecl.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtDimensionItemEName, taxo.substitutionGroupMap)
    }

    val typedDomainDeclOption = taxo.findGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef)

    assertResult(true) {
      typedDomainDeclOption.nonEmpty
    }
    assertResult(true) {
      typedDomainDeclOption.get.isConcrete
    }

    assertResult(EName(tns2, "phone")) {
      typedDomainDeclOption.get.targetEName
    }

    assertResult(Some(EName(tns2, "phoneType"))) {
      typedDomainDeclOption.get.typeOption
    }

    assertResult(true) {
      taxo.findNamedTypeDefinition(EName(tns2, "phoneType")).isDefined
    }
  }

  test("testTypedDomainRefHasNoFragment") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/111-TypedDimensionURIError/typedDomainRefHasNoFragment.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.globalElementDeclaration.substitutionGroupOption
    }
    assertResult(true) {
      typedDimDecl.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtDimensionItemEName, taxo.substitutionGroupMap)
    }

    assertResult(None) {
      Option(typedDimDecl.typedDomainRef.getFragment)
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
      taxo.findPrimaryItemDeclaration(primary).flatMap(_.globalElementDeclaration.substitutionGroupOption)
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
      taxo.findPrimaryItemDeclaration(domainMember).flatMap(_.globalElementDeclaration.substitutionGroupOption)
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

  test("testTargetDimensionDomainIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsDimensionInvalid.xsd",
      "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsDimensionInvalid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(dimensionDomains.map(_.targetConceptEName)) {
      dimensionDomains.map(_.domain)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimensionDomains.head.domain).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimensionDomains.head.domain).nonEmpty
    }
  }

  test("testTargetDimensionDomainIsHypercubeInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsHypercubeInvalid.xsd",
      "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsHypercubeInvalid-definition.xml"))

    val dimensionDomains = taxo.findAllDimensionDomainRelationships

    assertResult(1) {
      dimensionDomains.size
    }

    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimensionDomains.head.domain).nonEmpty
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(dimensionDomains.head.domain).nonEmpty
    }
  }

  test("testPrimaryItemPolymorphismDirectError") {
    val taxo = makeTestTaxonomy(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismError.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismDirectError-definition.xml"))

    val primaryTns = "http://www.xbrl.org/dim/conf/primary"
    val tns = "http://www.conformance-dimensions.com/xbrl/"

    val primary = EName(tns, "PrimaryItemsForCube")

    val hasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(primary)

    val dimMembers = taxo.findAllDimensionMembers(hasHypercubes.head)

    // Sales is in a dimension domain
    assertResult(Some(Set(
      EName(primaryTns, "IncomeStatement"),
      EName(primaryTns, "GrossProfit"),
      EName(primaryTns, "GrossProfitPresentation"),
      EName(primaryTns, "RevenueTotal"),
      EName(primaryTns, "CostOfSales"),
      EName(primaryTns, "Sales")))) {

      dimMembers.get(EName(tns, "BalanceDim"))
    }

    // Sales also inherits the hypercube
    assertResult(hasHypercubes.toSet) {
      taxo.findAllInheritedHasHypercubes(EName(primaryTns, "Sales")).toSet
    }

    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingDomainMemberRelationshipsOnElr(primary, hasHypercubes.head.elr).map(_.member).toSet
    }
    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
    }
  }

  test("testPrimaryItemPolymorphismIndirectError") {
    val taxo = makeTestTaxonomy(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismError.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismIndirectError-definition.xml"))

    val primaryTns = "http://www.xbrl.org/dim/conf/primary"
    val tns = "http://www.conformance-dimensions.com/xbrl/"

    val primary = EName(tns, "PrimaryItemsForCube")

    val hasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(primary)

    val dimMembers = taxo.findAllDimensionMembers(hasHypercubes.head)

    // Sales is in a dimension domain
    assertResult(Some(Set(
      EName(tns, "Domain"),
      EName(primaryTns, "IncomeStatement"),
      EName(primaryTns, "GrossProfit"),
      EName(primaryTns, "GrossProfitPresentation"),
      EName(primaryTns, "RevenueTotal"),
      EName(primaryTns, "CostOfSales"),
      EName(primaryTns, "Sales")))) {

      dimMembers.get(EName(tns, "BalanceDim"))
    }

    // Sales also inherits the hypercube
    assertResult(hasHypercubes.toSet) {
      taxo.findAllInheritedHasHypercubes(EName(primaryTns, "Sales")).toSet
    }

    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingDomainMemberRelationshipsOnElr(primary, hasHypercubes.head.elr).map(_.member).toSet
    }
    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterLongestOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
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
