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

package eu.cdevreeze.tqa.base.relationship

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.NameEName
import eu.cdevreeze.tqa.ENames.OrderEName
import eu.cdevreeze.tqa.ENames.WeightEName
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.base.dom.BaseSetKey
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.XLinkLocator
import eu.cdevreeze.tqa.base.dom.XsdSchema
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

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-01-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-01-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-01-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr))) {
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

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    assertResult(Map(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr) -> Vector())) {
      networkMap
    }
  }

  test("testFailingProhibition") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-02-DifferentOrder.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-02-DifferentOrder-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-02-DifferentOrder-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr))) {
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

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val nonProhibitedRelationships = relationships.filter(_.arc.use == Use.Optional)

    assertResult(1) {
      nonProhibitedRelationships.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr) -> nonProhibitedRelationships)) {
      networkMap
    }
  }

  test("testProhibitionWithImplicitOrder") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-03-MissingOrder.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-03-MissingOrder-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-03-MissingOrder-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr))) {
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

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    assertResult(Map(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr) -> Vector())) {
      networkMap
    }
  }

  test("testProhibitionOfOneRelationship") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-04-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-04-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-04-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(3) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr))) {
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

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val floatingAssetsRels =
      calcRelationships.filter(_.targetConceptEName.localPart == "floatingAssets")

    assertResult(1) {
      floatingAssetsRels.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr) -> floatingAssetsRels)) {
      networkMap
    }
  }

  test("testProhibitionOfOneRelationshipAgain") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-05-RelationshipEquivalence.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-05-RelationshipEquivalence-calculation-1.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/210-05-RelationshipEquivalence-calculation-2.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    assertResult(3) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      calcRelationships.map(_.arc)
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(Set(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr))) {
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

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val floatingAssetsRels =
      calcRelationships.filter(_.targetConceptEName.localPart == "floatingAssets")

    assertResult(1) {
      floatingAssetsRels.size
    }

    assertResult(Map(BaseSetKey.forSummationItemArc(BaseSetKey.StandardElr) -> floatingAssetsRels)) {
      networkMap
    }
  }

  test("testCombineConceptLabelRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/ArcOverrideDisjointLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-01-ArcOverrideDisjointLinkbases-1-label.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-01-ArcOverrideDisjointLinkbases-2-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(2) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    assertResult(Map(BaseSetKey.forConceptLabelArc(BaseSetKey.StandardElr) -> relationships)) {
      networkMap
    }
  }

  test("testOverrideConceptLabelRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-02-ArcOverrideLabelLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-02-ArcOverrideLabelLinkbases-1-label.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-02-ArcOverrideLabelLinkbases-2-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(3) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val filteredRelationships = relationships.filter(_.arc.priority == 2)

    assertResult(Map(BaseSetKey.forConceptLabelArc(BaseSetKey.StandardElr) -> filteredRelationships)) {
      networkMap
    }
  }

  test("testWrongOverrideConceptLabelRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-03-ArcOverrideLabelLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-03-ArcOverrideLabelLinkbases-1-label.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-03-ArcOverrideLabelLinkbases-2-label.xml").toURI
    val linkbaseDocUri3 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-03-ArcOverrideLabelLinkbases-3-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))
    val linkbaseDoc3 = indexed.Document(docParser.parse(linkbaseDocUri3).withUriOption(Some(linkbaseDocUri3)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)
    val linkbase3 = Linkbase.build(linkbaseDoc3.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2, linkbase3))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(3) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(1) {
      relationshipKeys.distinct.size
    }

    // Assuming only concept-label relationships here

    val wrongRelationships =
      relationships.filter(rel => rel.resolvedTo.xlinkLocatorOrResource.isInstanceOf[XLinkLocator] && rel.arc.use == Use.Optional)

    assertResult(1) {
      wrongRelationships.size
    }

    // Useless network computation, because one arc is not allowed

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    assertResult(Map(BaseSetKey.forConceptLabelArc(BaseSetKey.StandardElr) -> relationships.filter(_.arc.priority == 2))) {
      networkMap
    }
  }

  test("testCombineDefinitionRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-04-ArcOverrideDisjointLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-04-ArcOverrideLinkbases-1-def.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-04-ArcOverrideLinkbases-2-def.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(2) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    assertResult(Map(BaseSetKey.forRequiresElementArc(BaseSetKey.StandardElr) -> relationships)) {
      networkMap
    }
  }

  test("testOverrideDefinitionRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-05-ArcOverrideDisjointLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-05-ArcOverrideLinkbases-1-def.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-05-ArcOverrideLinkbases-2-def.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(3) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(2) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val filteredRelationships =
      relationships.filter(_.resolvedTo.resolvedElem.attributeOption(NameEName).contains("fixedAssets"))

    assertResult(Map(BaseSetKey.forRequiresElementArc(BaseSetKey.StandardElr) -> filteredRelationships)) {
      networkMap
    }
  }

  test("testProhibitAndInsertDefinitionRelationships") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-06-ArcOverrideDisjointLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-06-ArcOverrideLinkbases-1-def.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-06-ArcOverrideLinkbases-2-def.xml").toURI
    val linkbaseDocUri3 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-06-ArcOverrideLinkbases-3-def.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))
    val linkbaseDoc3 = indexed.Document(docParser.parse(linkbaseDocUri3).withUriOption(Some(linkbaseDocUri3)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)
    val linkbase3 = Linkbase.build(linkbaseDoc3.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2, linkbase3))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(4) {
      relationships.size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    // Mind the different arcroles
    assertResult(3) {
      relationshipKeys.distinct.size
    }

    val networkMap = relationshipFactory.computeNetworks(relationships, taxo).mapValues(_.retainedRelationships)

    val filteredRequiresElementRelationships =
      relationships collect {
        case rel: RequiresElementRelationship => rel
      } filter (_.resolvedTo.resolvedElem.attributeOption(NameEName).contains("fixedAssets"))

    val filteredGeneralSpecialRelationships =
      relationships collect { case rel: GeneralSpecialRelationship => rel }

    assertResult(
      Map(
        BaseSetKey.forRequiresElementArc(BaseSetKey.StandardElr) -> filteredRequiresElementRelationships,
        BaseSetKey.forGeneralSpecialArc(BaseSetKey.StandardElr) -> filteredGeneralSpecialRelationships)) {

        networkMap
      }
  }

  test("testWrongConceptLabelRelationship") {
    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-08-ArcOverrideLabelLinkbases.xsd").toURI
    val linkbaseDocUri1 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-08-ArcOverrideLabelLinkbases-1-label.xml").toURI
    val linkbaseDocUri2 = classOf[RelationshipEquivalenceTest].getResource("/conf-suite/Common/200-linkbase/291-08-ArcOverrideLabelLinkbases-2-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc1 = indexed.Document(docParser.parse(linkbaseDocUri1).withUriOption(Some(linkbaseDocUri1)))
    val linkbaseDoc2 = indexed.Document(docParser.parse(linkbaseDocUri2).withUriOption(Some(linkbaseDocUri2)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase1 = Linkbase.build(linkbaseDoc1.documentElement)
    val linkbase2 = Linkbase.build(linkbaseDoc2.documentElement)

    val taxo = TaxonomyBase.build(Vector(xsdSchema, linkbase1, linkbase2))

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val relationships = relationshipFactory.extractRelationships(taxo, RelationshipFactory.AnyArc)

    assertResult(3) {
      relationships.size
    }
    assertResult(true) {
      relationships.forall(_.arc.resolvedName == LinkLabelArcEName)
    }
    assertResult(2) {
      (relationships collect { case rel: ConceptLabelRelationship => rel }).size
    }
    // The corrupt concept-label relationship pointing to a target concept instead of label
    assertResult(1) {
      (relationships collect { case rel: UnknownRelationship => rel }).size
    }

    val relationshipKeys = relationships.map(rel => relationshipFactory.getRelationshipKey(rel, taxo))

    assertResult(3) {
      relationshipKeys.distinct.size
    }
  }
}
