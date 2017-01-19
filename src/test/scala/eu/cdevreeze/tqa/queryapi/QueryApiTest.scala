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

import scala.reflect.classTag
import scala.reflect.ClassTag
import scala.collection.immutable

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.Namespaces.XbrliNamespace
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom._
import eu.cdevreeze.tqa.relationship._
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import org.scalatest.junit.JUnitRunner

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

    val tns = "http://www.bizfinx.gov.sg/taxonomy/2013-09-13/elts/sg-dei"
    val plinkTop = EName(tns, "DisclosureOfFilingInformationAbstract")

    val prels = richTaxo.findAllInterConceptRelationshipsOfType(classTag[PresentationRelationship])

    assertResult(Set("http://www.xbrl.org/2003/arcrole/parent-child")) {
      prels.map(_.arcrole).toSet
    }

    val topENames = prels.map(_.sourceConceptEName).toSet.diff(prels.map(_.targetConceptEName).toSet)

    assertResult(Set(plinkTop)) {
      topENames
    }
    assertResult(prels.map(_.targetConceptEName).toSet) {
      val paths = richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(_ => true)
      paths.flatMap(_.relationships.map(_.targetConceptEName)).toSet
    }
    assertResult(prels.map(_.targetConceptEName).toSet.union(Set(plinkTop))) {
      val paths = richTaxo.filterLongestOutgoingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(_ => true)
      paths.flatMap(_.concepts).toSet
    }

    assertResult(Nil) {
      richTaxo.filterLongestIncomingInterConceptRelationshipPaths(plinkTop, classTag[PresentationRelationship])(_ => true)
    }

    assertResult(true) {
      val nonTopConcepts = prels.map(_.targetConceptEName).toSet
      nonTopConcepts.forall(c => richTaxo.filterLongestIncomingInterConceptRelationshipPaths(c, classTag[PresentationRelationship])(_ => true).nonEmpty)
    }
  }
}

object QueryApiTest {

  final class RichTaxonomy(
      val underlyingTaxo: Taxonomy,
      val substitutionGroupMap: SubstitutionGroupMap,
      val relationships: immutable.IndexedSeq[Relationship]) extends TaxonomySchemaLike with InterConceptRelationshipContainerLike {

    private val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)

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

    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration] = {
      (underlyingTaxo.globalElementDeclarationMap.toSeq collect {
        case (ename, decl) if conceptDeclarationBuilder.optConceptDeclaration(decl).isDefined =>
          (ename -> conceptDeclarationBuilder.optConceptDeclaration(decl).get)
      }).toMap
    }

    def findAllConceptDeclarations: immutable.IndexedSeq[ConceptDeclaration] = {
      findAllGlobalElementDeclarations.flatMap(decl => conceptDeclarationBuilder.optConceptDeclaration(decl))
    }

    def findConceptDeclaration(ename: EName): Option[ConceptDeclaration] = {
      conceptDeclarationsByEName.get(ename)
    }

    def findAllItemDeclarations: immutable.IndexedSeq[ItemDeclaration] = {
      findAllConceptDeclarations collect { case decl: ItemDeclaration => decl }
    }

    def findAllTupleDeclarations: immutable.IndexedSeq[TupleDeclaration] = {
      findAllConceptDeclarations collect { case decl: TupleDeclaration => decl }
    }

    def findAllPrimaryItemDeclarations: immutable.IndexedSeq[PrimaryItemDeclaration] = {
      findAllConceptDeclarations collect { case decl: PrimaryItemDeclaration => decl }
    }

    def findAllHypercubeDeclarations: immutable.IndexedSeq[HypercubeDeclaration] = {
      findAllConceptDeclarations collect { case decl: HypercubeDeclaration => decl }
    }

    def findAllDimensionDeclarations: immutable.IndexedSeq[DimensionDeclaration] = {
      findAllConceptDeclarations collect { case decl: DimensionDeclaration => decl }
    }

    def findAllExplicitDimensionDeclarations: immutable.IndexedSeq[ExplicitDimensionDeclaration] = {
      findAllConceptDeclarations collect { case decl: ExplicitDimensionDeclaration => decl }
    }

    def findAllTypedDimensionDeclarations: immutable.IndexedSeq[TypedDimensionDeclaration] = {
      findAllConceptDeclarations collect { case decl: TypedDimensionDeclaration => decl }
    }

    val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.sourceConceptEName)
    }

    val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.targetConceptEName)
    }

    def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

      implicit val clsTag = relationshipType
      relationships collect { case rel: A => rel }
    }

    def filterInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

      implicit val clsTag = relationshipType
      relationships collect { case rel: A if p(rel) => rel }
    }
  }

  object RichTaxonomy {

    def build(underlyingTaxo: Taxonomy, substitutionGroupMap: SubstitutionGroupMap): RichTaxonomy = {
      val relationships = DefaultRelationshipsFactory.LenientInstance.extractRelationships(underlyingTaxo, _ => true)

      new RichTaxonomy(underlyingTaxo, substitutionGroupMap, relationships)
    }
  }
}
