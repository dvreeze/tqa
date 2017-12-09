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
import eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.dom.LocalElementDeclaration
import eu.cdevreeze.tqa.base.dom.RoleRef
import eu.cdevreeze.tqa.base.dom.RoleType
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
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
    val taxo = makeTestDts(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeValid.xsd"))

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
    val taxo = makeTestDts(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstract.xsd"))

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
    val taxo = makeTestDts(Vector("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstractWithSGComplexities.xsd"))

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
      otherHypercube.substitutionGroupOption
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

  test("testHasHypercubeNoContextElementInvalid") {
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
      taxo.filterDimensionalRelationships(_.elr == expectedTargetRole).nonEmpty
    }
  }

  test("testHypercubeDimensionTargetRoleNotResolved") {
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/109-TypedDomainRefError/typedDomainRefvalid.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimension = EName(tns, "dPhone")

    val dimDecl = taxo.getTypedDimensionDeclaration(dimension)

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      dimDecl.substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/109-TypedDomainRefError/typedDomainRefvalid2.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimension = EName(tns, "dPhone")

    val dimDecl = taxo.getTypedDimensionDeclaration(dimension)

    assertResult(Some(EName(tns, "headPhone"))) {
      dimDecl.substitutionGroupOption
    }
    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      taxo.getDimensionDeclaration(EName(tns, "headPhone")).substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInSameFile.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReftoAbstractItemDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      typedDimDecl.substitutionGroupOption
    }

    assertResult(false) {
      taxo.getGlobalElementDeclarationByUri(typedDimDecl.typedDomainRef).isConcrete
    }
  }

  test("testTypedDomainReflocatesTypeDeclaration") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesTypeDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainRefLocatesNonGlobalElementDeclaration.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(ENames.XbrldtDimensionItemEName)) {
      typedDimDecl.substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInDifferentFileWithImport.xsd",
      "100-xbrldte/110-TypedDimensionError/typedDomainReflocatesDeclarationInDifferentFile_File2.xsd"))

    val tns1 = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"
    val tns2 = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid_File2"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns1, "dPhone"))

    assertResult(Some(EName(tns1, "headPhone"))) {
      typedDimDecl.substitutionGroupOption
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
    val taxo = makeTestDts(Vector(
      "100-xbrldte/111-TypedDimensionURIError/typedDomainRefHasNoFragment.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val typedDimDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      typedDimDecl.substitutionGroupOption
    }
    assertResult(true) {
      typedDimDecl.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtDimensionItemEName, taxo.substitutionGroupMap)
    }

    assertResult(None) {
      Option(typedDimDecl.typedDomainRef.getFragment)
    }
  }

  test("testPrimaryItemPolymorphismDirectError") {
    val taxo = makeTestDts(Vector(
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
      taxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
    }
  }

  test("testPrimaryItemPolymorphismIndirectError") {
    val taxo = makeTestDts(Vector(
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
      taxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
    }
  }

  test("testPrimaryItemPolymorphismDirectUsableFalse") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismError.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismDirectUnusableError-definition.xml"))

    val primaryTns = "http://www.xbrl.org/dim/conf/primary"
    val tns = "http://www.conformance-dimensions.com/xbrl/"

    val primary = EName(tns, "PrimaryItemsForCube")

    val hasHypercubes = taxo.findAllOutgoingHasHypercubeRelationships(primary)

    val dimMembers = taxo.findAllDimensionMembers(hasHypercubes.head)

    // Sales is in a dimension domain
    assertResult(Some(Set(
      EName(primaryTns, "Sales")))) {

      dimMembers.get(EName(tns, "BalanceDim"))
    }

    // Sales is not usable, but that does not matter
    assertResult(Some(Set())) {
      taxo.findAllUsableDimensionMembers(hasHypercubes.head).get(EName(tns, "BalanceDim"))
    }

    // Sales also inherits the hypercube
    assertResult(hasHypercubes.toSet) {
      taxo.findAllInheritedHasHypercubes(EName(primaryTns, "Sales")).toSet
    }

    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingDomainMemberRelationshipsOnElr(primary, hasHypercubes.head.elr).map(_.member).toSet
    }
    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
    }
  }

  test("testPrimaryItemPolymorphismIndirectUsableFalse") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismError.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismIndirectUnusableError-definition.xml"))

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

    // Sales is not usable, but that does not matter
    assertResult(Some(Set(
      EName(tns, "Domain"),
      EName(primaryTns, "IncomeStatement"),
      EName(primaryTns, "GrossProfit"),
      EName(primaryTns, "GrossProfitPresentation"),
      EName(primaryTns, "RevenueTotal"),
      EName(primaryTns, "CostOfSales")))) {

      taxo.findAllUsableDimensionMembers(hasHypercubes.head).get(EName(tns, "BalanceDim"))
    }

    // Sales also inherits the hypercube
    assertResult(hasHypercubes.toSet) {
      taxo.findAllInheritedHasHypercubes(EName(primaryTns, "Sales")).toSet
    }

    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingDomainMemberRelationshipsOnElr(primary, hasHypercubes.head.elr).map(_.member).toSet
    }
    assertResult(Set(EName(primaryTns, "Sales"))) {
      taxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(primary)(_.firstRelationship.elr == hasHypercubes.head.elr).
        flatMap(_.relationships).map(_.member).toSet
    }
  }

  test("testPrimaryItemPolymorphismDifferentSubGraphs") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismErrorDifferentSubgraph.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismErrorDifferentSubgraph-presentation.xml",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismErrorDifferentSubgraph-definition.xml"))

    val tns = "http://www.test.com/t"

    val biologicalAssets = EName(tns, "BiologicalAssets")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships

    assertResult(2) {
      hasHypercubes.size
    }
    assertResult(List(
      EName(tns, "MovementsAnalysisAbstract"),
      EName(tns, "Assets"))) {

      hasHypercubes.map(_.primary)
    }
    assertResult(List(
      EName(tns, "ClassesHypercube"),
      EName(tns, "ValuationHypercube"))) {

      hasHypercubes.map(_.hypercube)
    }

    val classesHasHypercube = hasHypercubes.filter(_.hypercube == EName(tns, "ClassesHypercube")).head
    val validationHasHypercube = hasHypercubes.filter(_.hypercube == EName(tns, "ValuationHypercube")).head

    // First consider the "classes" has-hypercube.
    // The biological assets concept is in a dimension domain, but not in the has-hypercube inheritance tree.

    val classesHypercubeDimMembers = taxo.findAllDimensionMembers(classesHasHypercube)

    assertResult(Map(
      EName(tns, "ClassesDimension") -> Set(
        EName(tns, "Assets"),
        EName(tns, "PPE"),
        EName(tns, "BiologicalAssets")))) {

      classesHypercubeDimMembers
    }

    val inheritingDomMemsForClassesHasHypercube =
      taxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(classesHasHypercube.primary) { path =>
        path.firstRelationship.elr == classesHasHypercube.elr
      }

    assertResult(false) {
      inheritingDomMemsForClassesHasHypercube.flatMap(_.concepts).contains(biologicalAssets)
    }
    assertResult(Set(EName(tns, "MovementsAnalysisAbstract"), EName(tns, "NetValue"), EName(tns, "Changes"), EName(tns, "Increases"), EName(tns, "Decreases"))) {
      inheritingDomMemsForClassesHasHypercube.flatMap(_.concepts).toSet
    }

    // Next consider the "validation" has-hypercube.
    // The biological assets concept is not in a dimension domain, but it is in the has-hypercube inheritance tree.

    val validationHypercubeDimMembers = taxo.findAllDimensionMembers(validationHasHypercube)

    assertResult(Map(
      EName(tns, "ValuationDimension") -> Set(
        EName(tns, "AtCost"),
        EName(tns, "FairValue")))) {

      validationHypercubeDimMembers
    }

    val ownOrInheritedHasHypercubes = taxo.findAllOwnOrInheritedHasHypercubes(biologicalAssets)

    assertResult(List(validationHasHypercube)) {
      ownOrInheritedHasHypercubes
    }
  }

  test("testPrimaryItemPolymorphismInherited") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismErrorInherited.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismError.xsd",
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismErrorInherited-definition.xml"))

    val tns = "http://www.conformance-dimensions.com/xbrl/inherited"

    val sales2 = EName(tns, "Sales2")

    val hasHypercubes = taxo.findAllHasHypercubeRelationships

    assertResult(1) {
      hasHypercubes.size
    }

    val dimension = EName("{http://www.conformance-dimensions.com/xbrl/}BalanceDim")

    // The sales2 concept is in a dimension domain of the has-hypercube

    assertResult(true) {
      taxo.findAllDimensionMembers(hasHypercubes.head).getOrElse(dimension, Set()).contains(sales2)
    }

    val hasHypercubeInheritanceOrSelf = taxo.computeHasHypercubeInheritanceOrSelf

    // And the sales2 concept also inherits that has-hypercube

    assertResult(true) {
      hasHypercubeInheritanceOrSelf exists {
        case (concept, hasHypercubeRels) =>
          concept == sales2 && hasHypercubeRels.contains(hasHypercubes.head)
      }
    }
  }

  test("testPolymorphismDefaultTest1") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismDefaultTest1.xsd",
      "100-xbrldte/115-PrimaryItemPolymorphismError/polymorphismDefaultTest1-definition.xml"))

    val tns = "http://www.conformance-dimensions.com/xbrl/"

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(1) {
      dimensionDefaults.size
    }

    assertResult(EName(tns, "dim")) {
      dimensionDefaults.head.dimension
    }

    assertResult(EName(tns, "item")) {
      dimensionDefaults.head.defaultOfDimension
    }
  }

  test("testDimensionDefaultSameRoleValid") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultSameRoleValid.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultSameRoleValid-definition.xml"))

    val tns = "http://www.example.com/new"

    // Two different dimensions
    val dimension1 = taxo.getDimensionDeclaration(EName(tns, "Dimension1"))
    val dimension2 = taxo.getDimensionDeclaration(EName(tns, "Dimension2"))

    val dimensionDefaults1 = taxo.findAllOutgoingDimensionDefaultRelationships(dimension1.targetEName)

    val dimensionDefaults2 = taxo.findAllOutgoingDimensionDefaultRelationships(dimension2.targetEName)

    assertResult(1) {
      dimensionDefaults1.size
    }

    assertResult(1) {
      dimensionDefaults2.size
    }

    assertResult(dimensionDefaults1.map(_.elr).toSet) {
      dimensionDefaults2.map(_.elr).toSet
    }

    assertResult((dimensionDefaults1 ++ dimensionDefaults2).toSet) {
      taxo.findAllDimensionDefaultRelationships.toSet
    }
  }

  test("testDimensionDefaultSameRoleInvalid") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultSameRoleInvalid.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultSameRoleInvalid-definition.xml"))

    val tns = "http://www.example.com/new"

    val dimension = taxo.getDimensionDeclaration(EName(tns, "DimensionDom"))

    val dimensionDefaults = taxo.findAllOutgoingDimensionDefaultRelationships(dimension.targetEName)

    assertResult(2) {
      dimensionDefaults.size
    }

    assertResult(Set("http://www.xbrl.org/2003/role/link")) {
      dimensionDefaults.map(_.elr).toSet
    }

    assertResult(2) {
      dimensionDefaults.map(_.defaultOfDimension).toSet.size
    }

    assertResult(dimensionDefaults.toSet) {
      taxo.findAllDimensionDefaultRelationships.toSet
    }
  }

  test("testDimensionDefaultDifferentRolesInvalid") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "lib/base/products.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultDifferentRolesInvalid.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultDifferentRolesInvalid-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"
    val prodTns = "http://www.xbrl.org/dim/conf/product"

    val dimension = taxo.getDimensionDeclaration(EName(tns, "ProductDim"))

    val dimensionDefaults = taxo.findAllOutgoingDimensionDefaultRelationships(dimension.targetEName)

    assertResult(2) {
      dimensionDefaults.size
    }

    assertResult(Set("http://www.xbrl.org/2003/role/link/H1", "http://www.xbrl.org/2003/role/link/H2")) {
      dimensionDefaults.map(_.elr).toSet
    }

    assertResult(Set(EName(prodTns, "AllProducts"), EName(prodTns, "Cars"))) {
      dimensionDefaults.map(_.defaultOfDimension).toSet
    }

    assertResult(dimensionDefaults.toSet) {
      taxo.findAllDimensionDefaultRelationships.toSet
    }

    // For fun, let's query entire dimensional trees

    val primaryTns = "http://www.xbrl.org/dim/conf/primary"

    val primary1 = EName(primaryTns, "IncomeStatement")
    val primary2 = EName(primaryTns, "Sales")

    val hasHypercubes1 = taxo.findAllOutgoingHasHypercubeRelationships(primary1)
    val hasHypercubes2 = taxo.findAllOutgoingHasHypercubeRelationships(primary2)

    assertResult(1)(hasHypercubes1.size)
    assertResult(1)(hasHypercubes2.size)

    val dimMembers1 = taxo.findAllUsableDimensionMembers(hasHypercubes1.head)

    assertResult(Set(dimension.targetEName)) {
      dimMembers1.keySet
    }

    val someMembers =
      Set(EName(prodTns, "AllProducts"), EName(prodTns, "Cars"), EName(prodTns, "Wine"))

    assertResult(someMembers) {
      dimMembers1.getOrElse(dimension.targetEName, Set()).filter(someMembers)
    }

    val dimMembers2 = taxo.findAllUsableDimensionMembers(hasHypercubes2.head)

    assertResult(Set(dimension.targetEName)) {
      dimMembers2.keySet
    }
    assertResult(someMembers) {
      dimMembers2.getOrElse(dimension.targetEName, Set()).filter(someMembers)
    }

    val hasHypercubeInheritanceOrSelf = taxo.computeHasHypercubeInheritanceOrSelf

    assertResult((hasHypercubes1 ++ hasHypercubes2).toSet) {
      hasHypercubeInheritanceOrSelf.getOrElse(EName(primaryTns, "Sales"), Set()).toSet
    }

    assertResult(hasHypercubes1.toSet) {
      hasHypercubeInheritanceOrSelf.getOrElse(EName(primaryTns, "CostOfSales"), Set()).toSet
    }
  }

  test("testDimensionDefaultOneArcWithTwoLocators") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultOneArcWithTwoLocators.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultOneArcWithTwoLocators-definition.xml"))

    val tns = "http://www.example.com/new"

    val dimension = taxo.getDimensionDeclaration(EName(tns, "DimensionDom"))

    val dimensionDefaults = taxo.findAllOutgoingDimensionDefaultRelationships(dimension.targetEName)

    // Two relationships

    assertResult(2) {
      dimensionDefaults.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.dimension).distinct.size
    }
    assertResult(2) {
      dimensionDefaults.map(_.defaultOfDimension).distinct.size
    }

    // Two relationships, but one shared arc (and locator XLink label)

    assertResult(1) {
      dimensionDefaults.map(_.arc).distinct.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.arc.from).distinct.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.arc.to).distinct.size
    }

    // One source locator, and two target locators

    assertResult(1) {
      dimensionDefaults.map(_.resolvedFrom.xlinkLocatorOrResource).distinct.size
    }
    assertResult(2) {
      dimensionDefaults.map(_.resolvedTo.xlinkLocatorOrResource).distinct.size
    }
  }

  test("testMultipleArcsResolveToSameDefault") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/multipleArcsResolveToSameDefault.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/multipleArcsResolveToSameDefault-definition.xml"))

    val tns = "http://www.example.com/new"

    val dimension = taxo.getDimensionDeclaration(EName(tns, "DimensionDom"))

    val dimensionDefaults = taxo.findAllOutgoingDimensionDefaultRelationships(dimension.targetEName)

    // Two relationships, sharing the same dimension and default member

    assertResult(2) {
      dimensionDefaults.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.dimension).distinct.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.defaultOfDimension).distinct.size
    }
    assertResult(2) {
      dimensionDefaults.map(_.elr).distinct.size
    }
  }

  test("testDimensionDefaultDifferentDomainsInvalid") {
    val taxo = makeTestDts(Vector(
      "lib/base/primary.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultDifferentDomainsInvalid.xsd",
      "100-xbrldte/124-TooManyDefaultMembersError/dimensionDefaultDifferentDomainsInvalid-definition.xml"))

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(2) {
      dimensionDefaults.size
    }

    assertResult(1) {
      dimensionDefaults.map(_.dimension).distinct.size
    }
    assertResult(2) {
      dimensionDefaults.map(_.defaultOfDimension).distinct.size
    }
    assertResult(1) {
      dimensionDefaults.map(_.elr).distinct.size
    }
  }

  test("testHypercubeDimensionUndirected") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/125-DRSUndirectedCycleError/schema.xsd",
      "100-xbrldte/125-DRSUndirectedCycleError/hypercubeDimensionUndirected-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val dimension = EName(tns, "dimension")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findDimensionDeclaration(dimension).isDefined)

    // Suppose the ELR and target role were insignificant, then there would be an undirected cycle

    val outgoingPaths =
      taxo.filterOutgoingInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(2)(outgoingPaths.size)

    assertResult(Set(2))(outgoingPaths.map(_.relationships.size).toSet)

    // Undirected cycle

    assertResult(List(dimension, dimension)) {
      outgoingPaths.map(_.lastRelationship.targetConceptEName)
    }

    // Not so if we follow consecutive relationships

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(2))(outgoingDimPaths.map(_.relationships.size).toSet)

    // No undirected cycle

    assertResult(List(dimension)) {
      outgoingDimPaths.map(_.lastRelationship.targetConceptEName)
    }
  }

  test("testDimensionDomainUndirected") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/125-DRSUndirectedCycleError/schema.xsd",
      "100-xbrldte/125-DRSUndirectedCycleError/dimensionDomainUndirected-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val hypercube = EName(tns, "hypercube")
    val domain = EName(tns, "domain")

    assertResult(true)(taxo.findHypercubeDeclaration(hypercube).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)

    // Suppose the ELR and target role were insignificant, then there would be an undirected cycle

    val outgoingPaths =
      taxo.filterOutgoingInterConceptRelationshipPaths(hypercube, classTag[DimensionalRelationship])(_ => true)

    assertResult(2)(outgoingPaths.size)

    assertResult(Set(2))(outgoingPaths.map(_.relationships.size).toSet)

    // Undirected cycle

    assertResult(List(domain, domain)) {
      outgoingPaths.map(_.lastRelationship.targetConceptEName)
    }

    // Not so if we follow consecutive relationships

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(hypercube, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(2))(outgoingDimPaths.map(_.relationships.size).toSet)

    // No undirected cycle

    assertResult(List(domain)) {
      outgoingDimPaths.map(_.lastRelationship.targetConceptEName)
    }
  }

  test("testDomainMemberUndirected") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/125-DRSUndirectedCycleError/schema.xsd",
      "100-xbrldte/125-DRSUndirectedCycleError/domainMemberUndirected-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(4))(outgoingDimPaths.map(_.relationships.size).toSet)

    // No undirected cycle

    assertResult(List(domainMember)) {
      outgoingDimPaths.map(_.lastRelationship.targetConceptEName)
    }
  }

  test("testDomainMemberDirected") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberDirected-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(5))(outgoingDimPaths.map(_.relationships.size).toSet)

    // A directed cycle

    assertResult(2) {
      outgoingDimPaths.head.concepts.filter(Set(domain)).size
    }
  }

  test("testDomainMemberDirected2") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberDirected2-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true) ensuring { paths =>
        // The check isMinimalIfHavingCycle is essential as stopping condition. This stopping condition already holds for all returned paths.
        paths.forall(_.isMinimalIfHavingCycle)
      }

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(5))(outgoingDimPaths.map(_.relationships.size).toSet)

    // A directed cycle

    assertResult(2) {
      outgoingDimPaths.head.concepts.filter(Set(domain)).size
    }
    assertResult(true) {
      outgoingDimPaths.head.hasCycle && outgoingDimPaths.head.isMinimalIfHavingCycle
    }
    assertResult(List(primary, EName(tns, "hypercube"), EName(tns, "dimension"), domain, domainMember, domain)) {
      outgoingDimPaths.head.concepts
    }
  }

  test("testDomainMemberDirectedWithoutHypercube") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberDirectedWithoutHypercube-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(domain, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(2))(outgoingDimPaths.map(_.relationships.size).toSet)

    // A directed cycle

    assertResult(2) {
      outgoingDimPaths.head.concepts.filter(Set(domain)).size
    }
  }

  test("testDomainMemberParallel") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberParallel-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val hypercube = EName(tns, "hypercube")
    val dimension = EName(tns, "dimension")
    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findHypercubeDeclaration(hypercube).isDefined)
    assertResult(true)(taxo.findDimensionDeclaration(dimension).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(2)(outgoingDimPaths.size)

    assertResult(Set(4))(outgoingDimPaths.map(_.relationships.size).toSet)

    // A directed cycle

    assertResult(Set(
      List(primary, hypercube, dimension, domain, domainMember),
      List(primary, hypercube, dimension, domainMember, domain))) {

      outgoingDimPaths.map(_.concepts).toSet
    }
  }

  test("testDomainMemberParallelWithoutHypercube") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberParallelWithoutHypercube-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val hypercube = EName(tns, "hypercube")
    val dimension = EName(tns, "dimension")
    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findHypercubeDeclaration(hypercube).isDefined)
    assertResult(true)(taxo.findDimensionDeclaration(dimension).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPathsFromPrimary =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    val outgoingDimPathsFromDomain =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(domain, classTag[DimensionalRelationship])(_ => true)

    val outgoingDimPathsFromDomainMember =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(domainMember, classTag[DimensionalRelationship])(_ => true)

    assertResult(1) {
      outgoingDimPathsFromPrimary.size
    }
    assertResult(Set(List(primary, hypercube, dimension))) {
      outgoingDimPathsFromPrimary.map(_.concepts).toSet
    }

    assertResult(1) {
      outgoingDimPathsFromDomain.size
    }
    assertResult(Set(List(domain, domainMember))) {
      outgoingDimPathsFromDomain.map(_.concepts).toSet
    }

    assertResult(1) {
      outgoingDimPathsFromDomainMember.size
    }
    assertResult(Set(List(domainMember, domain))) {
      outgoingDimPathsFromDomainMember.map(_.concepts).toSet
    }
  }

  test("testDomainMemberDirectedReverse") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberDirectedReverse-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val primary = EName(tns, "primaryItem")
    val hypercube = EName(tns, "hypercube")
    val dimension = EName(tns, "dimension")
    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(primary).isDefined)
    assertResult(true)(taxo.findHypercubeDeclaration(hypercube).isDefined)
    assertResult(true)(taxo.findDimensionDeclaration(dimension).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPaths =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(primary, classTag[DimensionalRelationship])(_ => true)

    assertResult(1)(outgoingDimPaths.size)

    assertResult(Set(4))(outgoingDimPaths.map(_.relationships.size).toSet)

    assertResult(Set(
      List(primary, hypercube, dimension, domain, domainMember))) {

      outgoingDimPaths.map(_.concepts).toSet
    }

    // The pseudo-cycle

    assertResult(Set(domainMember -> domain)) {
      taxo.findAllOutgoingDomainMemberRelationships(domainMember).
        map(rel => (rel.sourceConceptEName -> rel.targetConceptEName)).toSet
    }
  }

  test("testDomainMemberDirected2WithoutHypercube") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/126-DRSDirectedCycleError/schema.xsd",
      "100-xbrldte/126-DRSDirectedCycleError/domainMemberDirected2WithoutHypercube-definition.xml"))

    val tns = "http://xbrl.org/dims/conformance"

    val domain = EName(tns, "domain")
    val domainMember = EName(tns, "domainMember")

    assertResult(true)(taxo.findPrimaryItemDeclaration(domain).isDefined)
    assertResult(true)(taxo.findPrimaryItemDeclaration(domainMember).isDefined)

    val outgoingDimPathsFromDomain =
      taxo.filterOutgoingConsecutiveInterConceptRelationshipPaths(domain, classTag[DimensionalRelationship])(_ => true) ensuring { paths =>
        // The check isMinimalIfHavingCycle is essential as stopping condition. This stopping condition already holds for all returned paths.
        paths.forall(_.isMinimalIfHavingCycle)
      }

    assertResult(1)(outgoingDimPathsFromDomain.size)

    assertResult(Set(2))(outgoingDimPathsFromDomain.map(_.relationships.size).toSet)

    // If there were a hypercube and dimension, we would have a directed cycle.

    assertResult(Set(
      List(domain, domainMember, domain))) {

      outgoingDimPathsFromDomain.map(_.concepts).toSet
    }
  }

  test("testTypedDomainRefdoesnotlocateDeclarationInDifferentFile") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/127-OutOfDTSSchemaError/typedDomainRefdoesnotlocateDeclarationInDifferentFile.xsd"))

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimensionDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      dimensionDecl.substitutionGroupOption
    }

    val typedDomainRefUri = dimensionDecl.typedDomainRef

    assertResult(dimensionDecl.globalElementDeclaration.baseUri.resolve("typedDomainReflocatesDeclarationInDifferentFile_File2.xsd#duriv_phone")) {
      typedDomainRefUri
    }

    assertResult(false) {
      taxo.taxonomyBase.elemUriMap.get(typedDomainRefUri).nonEmpty
    }
    assertResult(true) {
      val baseUri = dimensionDecl.globalElementDeclaration.baseUri

      taxo.taxonomyBase.elemUriMap.get(baseUri.resolve("#duriv_dPhone")).nonEmpty
    }

    assertResult(false) {
      taxo.findMemberDeclarationOfTypedDimension(dimensionDecl.targetEName).nonEmpty
    }
  }

  test("testTypedDomainReflocatesDeclarationInDifferentFile") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/127-OutOfDTSSchemaError/typedDomainReflocatesDeclarationInDifferentFile.xsd"))

    // The file with the typed domain ref will be found during DTS discovery.

    val tns = "http://www.xbrl.org/dim/conf/190/dimensionURIvalid"

    val dimensionDecl = taxo.getTypedDimensionDeclaration(EName(tns, "dPhone"))

    assertResult(Some(EName(tns, "headPhone"))) {
      dimensionDecl.substitutionGroupOption
    }

    val typedDomainRefUri = dimensionDecl.typedDomainRef

    assertResult(dimensionDecl.globalElementDeclaration.baseUri.resolve("typedDomainReflocatesDeclarationInDifferentFile_File2.xsd#duriv_phone")) {
      typedDomainRefUri
    }

    assertResult(true) {
      taxo.taxonomyBase.elemUriMap.get(typedDomainRefUri).nonEmpty
    }
    assertResult(true) {
      val baseUri = dimensionDecl.globalElementDeclaration.baseUri

      taxo.taxonomyBase.elemUriMap.get(baseUri.resolve("#duriv_dPhone")).nonEmpty
    }

    assertResult(true) {
      taxo.findMemberDeclarationOfTypedDimension(dimensionDecl.targetEName).nonEmpty
    }
    assertResult(EName("{http://www.xbrl.org/dim/conf/190/dimensionURIvalid_File2}phone")) {
      taxo.getMemberDeclarationOfTypedDimension(dimensionDecl.targetEName).targetEName
    }
  }

  test("testCIQ-Integration-testcase-1") {
    val taxo = makeTestDts(Vector(
      "100-xbrldte/127-OutOfDTSSchemaError/XBRLTaxonomyA.xsd"))

    val tns = "http://www.xbrl.org/test/typed/Oasis"

    val dimensionDecl = taxo.getTypedDimensionDeclaration(EName(tns, "NameDimension"))

    val typedDomainRefUri = dimensionDecl.typedDomainRef

    assertResult(dimensionDecl.globalElementDeclaration.baseUri.resolve("xNL_Bridge_XBRL.xsd#PartyName")) {
      typedDomainRefUri
    }

    assertResult(false) {
      taxo.findMemberDeclarationOfTypedDimension(dimensionDecl.targetEName).nonEmpty
    }
  }

  // No test for V-04 (typed-dimension-schema-uses-redefine-3). We cannot handle xs:redefine.

  private def makeTestDts(relativeDocPaths: immutable.IndexedSeq[String]): BasicTaxonomy = {
    val rootDir = new File(classOf[DimensionalQueryTest].getResource("/conf-suite-dim").toURI)
    val docFiles = relativeDocPaths.map(relativePath => new File(rootDir, relativePath))

    val entryPointUris = docFiles.map(_.toURI).toSet

    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    val basicTaxo = taxoBuilder.build(entryPointUris)
    basicTaxo
  }

  private val processor = new Processor(false)

  private val docBuilder = {
    val otherRootDir = new File(classOf[DimensionalQueryTest].getResource("/xbrl-and-w3").toURI)

    SaxonDocumentBuilder.usingUriConverter(processor.newDocumentBuilder(), { uri =>
      if (uri.getScheme == "http" || uri.getScheme == "https") {
        UriConverters.uriToLocalUri(uri, otherRootDir)
      } else {
        uri
      }
    })
  }
}
