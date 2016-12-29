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

package eu.cdevreeze.tqa.relationship

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.OrderEName
import eu.cdevreeze.tqa.ENames.WeightEName
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.LabelArc
import eu.cdevreeze.tqa.dom.Linkbase
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.tqa.dom.Use
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Relationship equivalence test case. It uses test data from the XBRL Core Conformance Suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class RelationshipEquivalenceTest extends FunSuite {

  test("testProhibition") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("210-01-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("210-01-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("210-01-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = Taxonomy.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipsFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc)) {
      relationshipKeys.map(_.baseSetKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "fixedAssets")).get.key)) {
      relationshipKeys.map(_.sourceKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "changeInRetainedEarnings")).get.key)) {
      relationshipKeys.map(_.targetKey).toSet
    }
    assertResult(Set(NonExemptAttributeMap.from(Map(
      WeightEName -> DecimalAttributeValue(1),
      OrderEName -> DecimalAttributeValue(2))))) {

      relationshipKeys.map(_.nonExemptAttributes).toSet
    }

    // Both relationships are equivalent
    assertResult(1) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipsFactory.computeNetworks(relationships, taxo)

    assertResult(Map(BaseSetKey.forSummationItemArc -> Vector())) {
      networkMap
    }
  }

  test("testFailingProhibition") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("210-02-DifferentOrder.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("210-02-DifferentOrder-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("210-02-DifferentOrder-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = Taxonomy.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipsFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc)) {
      relationshipKeys.map(_.baseSetKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "fixedAssets")).get.key)) {
      relationshipKeys.map(_.sourceKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "changeInRetainedEarnings")).get.key)) {
      relationshipKeys.map(_.targetKey).toSet
    }
    // Difference in the order attribute value, so no equivalent relationships
    assertResult(2) {
      relationshipKeys.map(_.nonExemptAttributes).toSet.size
    }

    // Both relationships are not equivalent
    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipsFactory.computeNetworks(relationships, taxo)

    val nonProhibitedRelationships = relationships.filter(_.arc.use == Use.Optional)

    assertResult(1) {
      nonProhibitedRelationships.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc -> nonProhibitedRelationships)) {
      networkMap
    }
  }

  test("testProhibitionWithImplicitOrder") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("210-03-MissingOrder.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("210-03-MissingOrder-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("210-03-MissingOrder-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = Taxonomy.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipsFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc)) {
      relationshipKeys.map(_.baseSetKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "fixedAssets")).get.key)) {
      relationshipKeys.map(_.sourceKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "changeInRetainedEarnings")).get.key)) {
      relationshipKeys.map(_.targetKey).toSet
    }
    // An implicit order has value 1
    assertResult(Set(NonExemptAttributeMap.from(Map(
      WeightEName -> DecimalAttributeValue(1),
      OrderEName -> DecimalAttributeValue(1))))) {

      relationshipKeys.map(_.nonExemptAttributes).toSet
    }

    // Both relationships are equivalent
    assertResult(1) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipsFactory.computeNetworks(relationships, taxo)

    assertResult(Map(BaseSetKey.forSummationItemArc -> Vector())) {
      networkMap
    }
  }

  test("testProhibitionOfOneRelationship") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("210-04-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("210-04-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("210-04-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = Taxonomy.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(3) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipsFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc)) {
      relationshipKeys.map(_.baseSetKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "fixedAssets")).get.key)) {
      relationshipKeys.map(_.sourceKey).toSet
    }
    assertResult(Set(
      taxo.findGlobalElementDeclarationByEName(EName(tns, "changeInRetainedEarnings")).get.key,
      taxo.findGlobalElementDeclarationByEName(EName(tns, "floatingAssets")).get.key)) {

      relationshipKeys.map(_.targetKey).toSet
    }
    assertResult(Set(NonExemptAttributeMap.from(Map(
      WeightEName -> DecimalAttributeValue(1),
      OrderEName -> DecimalAttributeValue(1.0))))) {

      relationshipKeys.map(_.nonExemptAttributes).toSet
    }

    // Two of three relationships are equivalent
    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipsFactory.computeNetworks(relationships, taxo)

    val floatingAssetsRels =
      calcRelationships.filter(_.targetConceptEName.localPart == "floatingAssets")

    assertResult(1) {
      floatingAssetsRels.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc -> floatingAssetsRels)) {
      networkMap
    }
  }

  test("testProhibitionOfOneRelationshipAgain") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("210-05-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("210-05-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("210-05-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = Taxonomy.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipsFactory = DefaultRelationshipsFactory.StrictInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(3) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipsFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc)) {
      relationshipKeys.map(_.baseSetKey).toSet
    }
    assertResult(Set(taxo.findGlobalElementDeclarationByEName(EName(tns, "fixedAssets")).get.key)) {
      relationshipKeys.map(_.sourceKey).toSet
    }
    assertResult(Set(
      taxo.findGlobalElementDeclarationByEName(EName(tns, "changeInRetainedEarnings")).get.key,
      taxo.findGlobalElementDeclarationByEName(EName(tns, "floatingAssets")).get.key)) {

      relationshipKeys.map(_.targetKey).toSet
    }
    assertResult(Set(NonExemptAttributeMap.from(Map(
      WeightEName -> DecimalAttributeValue(1),
      OrderEName -> DecimalAttributeValue(1.0))))) {

      relationshipKeys.map(_.nonExemptAttributes).toSet
    }

    // Two of three relationships are equivalent
    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipsFactory.computeNetworks(relationships, taxo)

    val floatingAssetsRels =
      calcRelationships.filter(_.targetConceptEName.localPart == "floatingAssets")

    assertResult(1) {
      floatingAssetsRels.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc -> floatingAssetsRels)) {
      networkMap
    }
  }
}
