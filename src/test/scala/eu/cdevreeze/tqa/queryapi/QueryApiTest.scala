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

package eu.cdevreeze.tqa.queryapi

import java.net.URI

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.ConceptDeclaration
import eu.cdevreeze.tqa.dom.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.dom.NamedTypeDefinition
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.tqa.relationship.AllRelationship
import eu.cdevreeze.tqa.relationship.DefaultRelationshipsFactory
import eu.cdevreeze.tqa.relationship.DimensionalRelationship
import eu.cdevreeze.tqa.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.relationship.PresentationRelationship
import eu.cdevreeze.tqa.relationship.Relationship
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

    val taxo = Taxonomy.build(taxoRootElems)
    val richTaxo = QueryApiTest.RichTaxonomy.build(taxo, SubstitutionGroupMap.Empty)

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

    val taxo = Taxonomy.build(taxoRootElems)
    val richTaxo = QueryApiTest.RichTaxonomy.build(taxo, SubstitutionGroupMap.Empty)

    assertResult(true) {
      richTaxo.findAllGlobalElementDeclarations.size > 1600
    }
    assertResult(richTaxo.findAllGlobalElementDeclarations) {
      richTaxo.findAllConceptDeclarations.map(_.globalElementDeclaration)
    }

    assertResult(Nil) {
      richTaxo.findAllInterConceptRelationshipsOfType(classTag[AllRelationship])
    }

    val hdRels = richTaxo.findAllInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship])

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

    val ddRels = richTaxo.findAllInterConceptRelationshipsOfType(classTag[DimensionDomainRelationship])

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
      richTaxo.findAllHypercubeDeclarations.map(_.globalElementDeclaration.isAbstract).toSet
    }
    assertResult(Set(true)) {
      richTaxo.findAllDimensionDeclarations.map(_.globalElementDeclaration.isAbstract).toSet
    }
    assertResult(Set(false, true)) {
      richTaxo.findAllPrimaryItemDeclarations.map(_.globalElementDeclaration.isAbstract).toSet
    }

    assertResult(true) {
      richTaxo.filterPrimaryItemDeclarations(_.globalElementDeclaration.isAbstract).map(_.targetEName.localPart).contains(
        "ChangesInWorkingCapitalAbstract")
    }

    val hypercubes = richTaxo.findAllHypercubeDeclarations.map(_.targetEName).distinct

    val paths =
      hypercubes.flatMap(hc => richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(hc, classTag[DimensionalRelationship])(_.isElrValid))

    assertResult(richTaxo.findAllInterConceptRelationshipsOfType(classTag[HypercubeDimensionRelationship]).toSet) {
      paths.map(_.firstRelationship).toSet
    }
  }
}

object QueryApiTest {

  final class RichTaxonomy(
      val underlyingTaxo: Taxonomy,
      val substitutionGroupMap: SubstitutionGroupMap,
      val relationships: immutable.IndexedSeq[Relationship]) extends TaxonomySchemaLike with InterConceptRelationshipContainerLike {

    private val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)

    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration] = {
      (underlyingTaxo.globalElementDeclarationMap.toSeq collect {
        case (ename, decl) if conceptDeclarationBuilder.optConceptDeclaration(decl).isDefined =>
          (ename -> conceptDeclarationBuilder.optConceptDeclaration(decl).get)
      }).toMap
    }

    val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.sourceConceptEName)
    }

    val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.targetConceptEName)
    }

    def findAllXsdSchemas: immutable.IndexedSeq[XsdSchema] = {
      underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[XsdSchema]))
    }

    def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
      underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration]))
    }

    def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration] = {
      underlyingTaxo.findGlobalElementDeclarationByEName(ename)
    }

    def findGlobalElementDeclarationByUri(uri: URI): Option[GlobalElementDeclaration] = {
      underlyingTaxo.findElemByUri(uri) collectFirst { case decl: GlobalElementDeclaration => decl }
    }

    def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration] = {
      underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalAttributeDeclaration]))
    }

    def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration] = {
      underlyingTaxo.findGlobalAttributeDeclarationByEName(ename)
    }

    def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition] = {
      underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[NamedTypeDefinition]))
    }

    def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition] = {
      underlyingTaxo.findNamedTypeDefinitionByEName(ename)
    }

    def findConceptDeclaration(ename: EName): Option[ConceptDeclaration] = {
      conceptDeclarationsByEName.get(ename)
    }

    def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

      implicit val clsTag = relationshipType
      relationships collect { case rel: A => rel }
    }
  }

  object RichTaxonomy {

    def build(underlyingTaxo: Taxonomy, substitutionGroupMap: SubstitutionGroupMap): RichTaxonomy = {
      val relationships = DefaultRelationshipsFactory.LenientInstance.extractRelationships(underlyingTaxo, _ => true)

      new RichTaxonomy(underlyingTaxo, substitutionGroupMap, relationships)
    }
  }
}
