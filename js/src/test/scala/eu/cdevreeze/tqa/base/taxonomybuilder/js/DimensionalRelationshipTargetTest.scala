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

package eu.cdevreeze.tqa.base.taxonomybuilder.js

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import org.scalajs.dom.experimental.domparser.DOMParser
import org.scalajs.dom.experimental.domparser.SupportedType
import org.scalatest.FunSuite

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.yaidom.convert.JsDomConversions
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.jsdom.JsDomDocument
import eu.cdevreeze.yaidom.simple

/**
 * Dimensional relationship target querying test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
class DimensionalRelationshipTargetTest extends FunSuite {

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
      taxo.findConceptDeclaration(targetName).flatMap(_.substitutionGroupOption)
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

  test("testTargetDomainMemberIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsDimensionInvalid.xsd",
      "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsDimensionInvalid-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    assertResult(1) {
      domainMembers.size
    }

    assertResult(domainMembers.map(_.targetConceptEName)) {
      domainMembers.map(_.member)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(domainMembers.head.member).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(domainMembers.head.member).nonEmpty
    }
  }

  test("testTargetDomainMemberIsHypercubeInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsHypercubeInvalid.xsd",
      "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsHypercubeInvalid-definition.xml"))

    val domainMembers = taxo.findAllDomainMemberRelationships

    assertResult(1) {
      domainMembers.size
    }

    assertResult(domainMembers.map(_.targetConceptEName)) {
      domainMembers.map(_.member)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(domainMembers.head.member).nonEmpty
    }
    assertResult(true) {
      taxo.findHypercubeDeclaration(domainMembers.head.member).nonEmpty
    }
  }

  test("testTargetDimensionDefaultIsDimensionInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultIsDimensionInvalid.xsd",
      "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultIsDimensionInvalid-definition.xml"))

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(1) {
      dimensionDefaults.size
    }

    assertResult(dimensionDefaults.map(_.targetConceptEName)) {
      dimensionDefaults.map(_.defaultOfDimension)
    }
    assertResult(false) {
      taxo.findPrimaryItemDeclaration(dimensionDefaults.head.defaultOfDimension).nonEmpty
    }
    assertResult(true) {
      taxo.findDimensionDeclaration(dimensionDefaults.head.defaultOfDimension).nonEmpty
    }
  }

  test("testTargetDimensionDefaultNotMemberInvalid") {
    val taxo = makeTestTaxonomy(Vector(
      "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultNotMemberInvalid.xsd",
      "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultNotMemberInvalid-definition.xml"))

    val dimensionDefaults = taxo.findAllDimensionDefaultRelationships

    assertResult(1) {
      dimensionDefaults.size
    }

    assertResult(dimensionDefaults.map(_.targetConceptEName)) {
      dimensionDefaults.map(_.defaultOfDimension)
    }
    assertResult(true) {
      taxo.findPrimaryItemDeclaration(dimensionDefaults.head.defaultOfDimension).nonEmpty
    }

    val hasHypercube = taxo.findAllHasHypercubeRelationships.head

    val members =
      taxo.findAllDimensionMembers(hasHypercube).getOrElse(dimensionDefaults.head.dimension, Set())

    val tns = "http://www.example.com/new"

    assertResult(EName(tns, "NotDomainMember")) {
      dimensionDefaults.head.defaultOfDimension
    }

    // The dimension default is not a member of the dimension for any hypercube
    assertResult(Set(EName(tns, "Domain"), EName(tns, "DomainMember"))) {
      members
    }
    assertResult(false) {
      members.contains(dimensionDefaults.head.defaultOfDimension)
    }
  }

  private def makeTestTaxonomy(relativeDocPaths: immutable.IndexedSeq[String]): BasicTaxonomy = {
    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(TrivialDocumentCollector).
        withRelationshipFactory(DefaultRelationshipFactory.StrictInstance)

    val richTaxo = taxoBuilder.build(relativeDocPaths.map(path => dummyRootDirUri.resolve(path)).toSet)

    richTaxo.ensuring(_.relationships.nonEmpty)
  }

  private val docBuilder: DocumentBuilder = {
    new DocumentBuilder {

      type BackingElem = indexed.Elem

      def build(uri: URI): BackingElem = {
        require(uri.isAbsolute, s"Document URI '$uri' is not absolute so rejected")
        val xmlString = docsByUri.getOrElse(uri, sys.error(s"Missing document $uri"))

        val db = new DOMParser()
        val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(xmlString, SupportedType.`text/xml`))

        // We have to convert to indexed elements, in order to keep the document URI with the elements.
        // Without those document URIs that are known to the elements themselves, TQA does not work!

        val simpleDoc: simple.Document =
          JsDomConversions.convertToDocument(domDoc.wrappedDocument).withUriOption(Some(uri))
        val rootElem: indexed.Elem = indexed.Document(simpleDoc).documentElement
        rootElem
      }
    }
  }

  private val dummyRootDirUri = URI.create("file:/dummyRootDir/")

  private val docsByUri: Map[URI, String] = Map(
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsItemInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:t="http://www.xbrl.org/dim/conf" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.xbrl.org/dim/conf" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsItemInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsItemInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="t_AllCube" name="AllCube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="t_ProdDim" name="ProdDim" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="t_RegionDim" name="RegionDim" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsItemInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsItemInvalid.xsd#t_AllCube" xlink:label="t_AllCube" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsItemInvalid.xsd#t_ProdDim" xlink:label="t_ProdDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_ProdDim" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsItemInvalid.xsd#t_RegionDim" xlink:label="t_RegionDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_RegionDim" order="2" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsTupleInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:t="http://www.xbrl.org/dim/conf" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.xbrl.org/dim/conf" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsTupleInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsTupleInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="t_AllCube" name="AllCube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="t_ProdDim" name="ProdDim" substitutionGroup="xbrli:tuple" abstract="true" nillable="true">
    <complexType>
      <complexContent>
        <restriction base="anyType">
          <sequence />
          <attribute name="id" type="ID" />
        </restriction>
      </complexContent>
    </complexType>
  </element>
  <element id="t_RegionDim" name="RegionDim" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsTupleInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsTupleInvalid.xsd#t_AllCube" xlink:label="t_AllCube" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsTupleInvalid.xsd#t_ProdDim" xlink:label="t_ProdDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_ProdDim" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsTupleInvalid.xsd#t_RegionDim" xlink:label="t_RegionDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_RegionDim" order="2" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsHypercubeInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:t="http://www.xbrl.org/dim/conf" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.xbrl.org/dim/conf" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsHypercubeInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHypercubeDimensionIsHypercubeInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="t_AllCube" name="AllCube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="t_ProdDim" name="ProdDim" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="t_RegionDim" name="RegionDim" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/103-HypercubeDimensionTargetError/targetHypercubeDimensionIsHypercubeInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsHypercubeInvalid.xsd#t_AllCube" xlink:label="t_AllCube" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsHypercubeInvalid.xsd#t_ProdDim" xlink:label="t_ProdDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_ProdDim" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetHypercubeDimensionIsHypercubeInvalid.xsd#t_RegionDim" xlink:label="t_RegionDim" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="t_AllCube" xlink:to="t_RegionDim" order="2" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsItemInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHasHypercubeIsItemInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHasHypercubeIsItemInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_PrimaryItem" name="PrimaryItem" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsItemInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetHasHypercubeIsItemInvalid.xsd#new_PrimaryItem" xlink:label="new_PrimaryItem" />
    <loc xlink:type="locator" xlink:href="targetHasHypercubeIsItemInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItem" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsDimensionInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHasHypercubeIsDimensionInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetHasHypercubeIsDimensionInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_PrimaryItem" name="PrimaryItem" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/105-HasHypercubeTargetError/targetHasHypercubeIsDimensionInvalid-definition.xml" -> """<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetHasHypercubeIsDimensionInvalid.xsd#new_PrimaryItem" xlink:label="new_PrimaryItem" />
    <loc xlink:type="locator" xlink:href="targetHasHypercubeIsDimensionInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItem" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsDimensionInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDomainIsDimensionInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDomainIsDimensionInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_DimensionDom" name="DimensionDom" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_Domain" name="Domain" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" nillable="true" abstract="true"/>
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_PrimaryItemMeasure" name="PrimaryItemMeasure" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_PrimaryItemMember" name="PrimaryItemMember" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsDimensionInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-domain" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-domain" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsDimensionInvalid.xsd#new_PrimaryItemMeasure" xlink:label="new_PrimaryItemMeasure" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsDimensionInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItemMeasure" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsDimensionInvalid.xsd#new_DimensionDom" xlink:label="new_DimensionDom" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="new_Hypercube" xlink:to="new_DimensionDom" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsDimensionInvalid.xsd#new_Domain" xlink:label="new_Domain" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-domain" xlink:from="new_DimensionDom" xlink:to="new_Domain" order="1" use="optional" xbrldt:usable="true" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsHypercubeInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDomainIsHypercubeInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDomainIsHypercubeInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_DimensionDom" name="DimensionDom" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_Domain" name="Domain" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" nillable="true" abstract="true"/>
  <element id="new_DomainMember" name="DomainMember" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_PrimaryItemMeasure" name="PrimaryItemMeasure" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
</schema>""".trim,
    "100-xbrldte/113-DimensionDomainTargetError/targetDimensionDomainIsHypercubeInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-domain" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-domain" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsHypercubeInvalid.xsd#new_PrimaryItemMeasure" xlink:label="new_PrimaryItemMeasure" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsHypercubeInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItemMeasure" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsHypercubeInvalid.xsd#new_DimensionDom" xlink:label="new_DimensionDom" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="new_Hypercube" xlink:to="new_DimensionDom" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetDimensionDomainIsHypercubeInvalid.xsd#new_Domain" xlink:label="new_Domain" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-domain" xlink:from="new_DimensionDom" xlink:to="new_Domain" order="1" use="optional" xbrldt:usable="true" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsHypercubeInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDomainMemberIsHypercubeInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDomainMemberIsHypercubeInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_PrimaryItem" name="PrimaryItem" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_PrimaryItemMemberChild" name="PrimaryItemMemberChild" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsHypercubeInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDomainMemberIsHypercubeInvalid.xsd#new_PrimaryItem" xlink:label="new_PrimaryItem" />
    <loc xlink:type="locator" xlink:href="targetDomainMemberIsHypercubeInvalid.xsd#new_PrimaryItemMemberChild" xlink:label="new_PrimaryItemMemberChild" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="new_PrimaryItem" xlink:to="new_PrimaryItemMemberChild" order="1" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsDimensionInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDomainMemberIsDimensionInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDomainMemberIsDimensionInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_PrimaryItem" name="PrimaryItem" type="xbrli:stringItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_PrimaryItemMemberChild" name="PrimaryItemMemberChild" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
</schema>""".trim,
    "100-xbrldte/117-DomainMemberTargetError/targetDomainMemberIsDimensionInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDomainMemberIsDimensionInvalid.xsd#new_PrimaryItem" xlink:label="new_PrimaryItem" />
    <loc xlink:type="locator" xlink:href="targetDomainMemberIsDimensionInvalid.xsd#new_PrimaryItemMemberChild" xlink:label="new_PrimaryItemMemberChild" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="new_PrimaryItem" xlink:to="new_PrimaryItemMemberChild" order="1" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultIsDimensionInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDefaultIsDimensionInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDefaultIsDimensionInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_Dimension2Dom" name="Dimension2Dom" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_DimensionDom" name="DimensionDom" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_Domain" name="Domain" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_DomainMember" name="DomainMember" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_PrimaryItemMeasure" name="PrimaryItemMeasure" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
</schema>""".trim,
    "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultIsDimensionInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-domain" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-domain" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-default" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-default" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_PrimaryItemMeasure" xlink:label="new_PrimaryItemMeasure" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItemMeasure" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_DimensionDom" xlink:label="new_DimensionDom" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="new_Hypercube" xlink:to="new_DimensionDom" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_Domain" xlink:label="new_Domain" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-domain" xlink:from="new_DimensionDom" xlink:to="new_Domain" order="1" use="optional" xbrldt:usable="true" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_DomainMember" xlink:label="new_DomainMember" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="new_Domain" xlink:to="new_DomainMember" order="1" use="optional" xbrldt:usable="true" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultIsDimensionInvalid.xsd#new_Dimension2Dom" xlink:label="new_Dimension2Dom" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-default" xlink:from="new_DimensionDom" xlink:to="new_Dimension2Dom" order="2" use="optional" />
  </definitionLink>
</linkbase>""".trim,
    "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultNotMemberInvalid.xsd" -> """
      <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:new="http://www.example.com/new" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" targetNamespace="http://www.example.com/new" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <annotation>
    <appinfo>
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDefaultNotMemberInvalid-label.xml" xlink:role="http://www.xbrl.org/2003/role/labelLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Label Links, all" />
      <link:linkbaseRef xlink:type="simple" xlink:href="targetDimensionDefaultNotMemberInvalid-definition.xml" xlink:role="http://www.xbrl.org/2003/role/definitionLinkbaseRef" xlink:arcrole="http://www.w3.org/1999/xlink/properties/linkbase" xlink:title="Definition Links, all" />
    </appinfo>
  </annotation>
  <import namespace="http://www.xbrl.org/2003/instance" schemaLocation="http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd" />
  <import namespace="http://xbrl.org/2005/xbrldt" schemaLocation="http://www.xbrl.org/2005/xbrldt-2005.xsd" />
  <element id="new_DimensionDom" name="DimensionDom" type="xbrli:stringItemType" substitutionGroup="xbrldt:dimensionItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_Domain" name="Domain" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_DomainMember" name="DomainMember" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_Hypercube" name="Hypercube" type="xbrli:stringItemType" substitutionGroup="xbrldt:hypercubeItem" xbrli:periodType="instant" abstract="true" nillable="true" />
  <element id="new_NotDomainMember" name="NotDomainMember" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
  <element id="new_PrimaryItemMeasure" name="PrimaryItemMeasure" type="xbrli:monetaryItemType" substitutionGroup="xbrli:item" xbrli:periodType="instant" nillable="true" />
</schema>""".trim,
    "100-xbrldte/123-DimensionDefaultTargetError/targetDimensionDefaultNotMemberInvalid-definition.xml" -> """
      <linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xsi:schemaLocation=" http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd http://xbrl.org/2005/xbrldt http://www.xbrl.org/2005/xbrldt-2005.xsd">
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#all" arcroleURI="http://xbrl.org/int/dim/arcrole/all" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#hypercube-dimension" arcroleURI="http://xbrl.org/int/dim/arcrole/hypercube-dimension" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-domain" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-domain" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member" />
  <arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#dimension-default" arcroleURI="http://xbrl.org/int/dim/arcrole/dimension-default" />
  <definitionLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_PrimaryItemMeasure" xlink:label="new_PrimaryItemMeasure" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_Hypercube" xlink:label="new_Hypercube" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" xlink:from="new_PrimaryItemMeasure" xlink:to="new_Hypercube" order="1" use="optional" xbrldt:contextElement="segment" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_DimensionDom" xlink:label="new_DimensionDom" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/hypercube-dimension" xlink:from="new_Hypercube" xlink:to="new_DimensionDom" order="1" use="optional" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_Domain" xlink:label="new_Domain" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-domain" xlink:from="new_DimensionDom" xlink:to="new_Domain" order="1" use="optional" xbrldt:usable="true" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_DomainMember" xlink:label="new_DomainMember" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="new_Domain" xlink:to="new_DomainMember" order="1" use="optional" xbrldt:usable="true" />
    <loc xlink:type="locator" xlink:href="targetDimensionDefaultNotMemberInvalid.xsd#new_NotDomainMember" xlink:label="new_NotDomainMember" />
    <definitionArc xlink:type="arc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/dimension-default" xlink:from="new_DimensionDom" xlink:to="new_NotDomainMember" order="2" use="optional" />
  </definitionLink>
</linkbase>""".trim).
    map(kv => (dummyRootDirUri.resolve(kv._1) -> kv._2))
}
