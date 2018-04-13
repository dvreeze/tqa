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

package eu.cdevreeze.tqa.xpathaware.extension.table

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.base.dom.BaseSetKey
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa.extension.table.common.ConceptRelationshipNodes.FormulaAxis
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.xpath.saxon.SaxonJaxpXPathEvaluator
import eu.cdevreeze.yaidom.xpath.saxon.SaxonJaxpXPathEvaluatorFactory
import net.sf.saxon.s9api.Processor

/**
 * Concept relationship node test case. It uses test data from the XBRL Tables conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class ConceptRelationshipNodeTest extends FunSuite {

  import ConceptRelationshipNodeData._

  private val tableExampleNs = "http://www.xbrl.org/table-examples"

  private val xfiNs = "http://www.xbrl.org/2008/function/instance"
  private val xfiRootEName = EName(xfiNs, "root")

  private val relativeUriTo3100Dir =
    URI.create("table-linkbase-conf-2015-08-12/conf/tests/3100-concept-relationship-node/")

  // 3110-concept-relationship-node-relationship-source-testcase-v01i

  test("testConceptRelationshipNodeWithRelationshipSourceXfiRoot") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3110-concept-relationship-node-relationship-source/concept-relationship-node-xfi-root-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(xfiRootEName)) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("base", "o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }
  }

  // 3110-concept-relationship-node-relationship-source-testcase-v02i

  test("testConceptRelationshipNodeWithSourceOmitted") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3110-concept-relationship-node-relationship-source/concept-relationship-node-source-omitted-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set.empty) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("base", "o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }
  }

  // 3110-concept-relationship-node-relationship-source-testcase-v03i

  test("testConceptRelationshipNodeForPartOfPresentationNetwork") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3110-concept-relationship-node-relationship-source/concept-relationship-node-partial-tree-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(EName(tableExampleNs, "o5"))) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }
  }

  // 3110-concept-relationship-node-relationship-source-testcase-v04i

  test("testConceptRelationshipNodeWithMultipleSources") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3110-concept-relationship-node-relationship-source/concept-relationship-node-multiple-sources-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(EName(tableExampleNs, "o2"), EName(tableExampleNs, "o5"))) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("o2", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }
  }

  // 3160-concept-relationship-node-formula-axis-testcase-v01i

  test("testConceptRelationshipNodeWithFormulaAxisOmitted") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3160-concept-relationship-node-formula-axis/axis-omitted-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(EName(tableExampleNs, "base"))) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("base", "o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }

    assertResult(Set(EName(tableExampleNs, "base"))) {
      findAllResultPaths(conceptRelationshipNode, tableTaxo).map(_.sourceConcept).toSet
    }
  }

  // 3160-concept-relationship-node-formula-axis-testcase-v02i

  test("testConceptRelationshipNodeWithFormulaAxisDescendantOrSelf") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3160-concept-relationship-node-formula-axis/descendant-or-self-axis-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(EName(tableExampleNs, "base"))) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("base", "o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }

    assertResult(Set(EName(tableExampleNs, "base"))) {
      findAllResultPaths(conceptRelationshipNode, tableTaxo).map(_.sourceConcept).toSet
    }
  }

  // 3160-concept-relationship-node-formula-axis-testcase-v03i

  test("testConceptRelationshipNodeWithFormulaAxisDescendant") {
    val instance =
      makeTestInstance(
        relativeUriTo3100Dir.toString +
          "3160-concept-relationship-node-formula-axis/descendant-axis-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    assertResult(true) {
      tableTaxo.underlyingTaxonomy.findAllParentChildRelationships.nonEmpty
    }

    val conceptRelationshipNodes =
      tableTaxo.tableResources collect { case n: ConceptRelationshipNode => n }

    assertResult(1) {
      conceptRelationshipNodes.size
    }

    val conceptRelationshipNode = conceptRelationshipNodes.head
    val conceptRelationshipNodeData = new ConceptRelationshipNodeData(conceptRelationshipNode)

    assertResult(Set(EName(tableExampleNs, "base"))) {
      conceptRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(None) {
      conceptRelationshipNodeData.linkroleOption
    }

    assertResult(BaseSetKey.forParentChildArc(BaseSetKey.StandardElr).arcrole) {
      conceptRelationshipNodeData.arcrole
    }

    assertResult(FormulaAxis.DescendantAxis) {
      conceptRelationshipNodeData.formulaAxis
    }

    val concepts: Set[EName] =
      findAllResultPaths(conceptRelationshipNode, tableTaxo).flatMap(_.concepts).toSet

    assertResult(Set("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9").map(nm => EName(tableExampleNs, nm))) {
      concepts
    }

    assertResult(Set("o1", "o2", "o3").map(nm => EName(tableExampleNs, nm))) {
      findAllResultPaths(conceptRelationshipNode, tableTaxo).map(_.sourceConcept).toSet
    }
  }

  // Helper methods

  private def buildTaxonomy(xbrlInstance: XbrlInstance, doResolveProhibitionAndOverriding: Boolean = false): BasicTaxonomy = {
    val entryPointHrefs =
      xbrlInstance.findAllSchemaRefs.map(_.resolvedHref) ++ xbrlInstance.findAllLinkbaseRefs.map(_.resolvedHref)

    buildTaxonomy(
      entryPointHrefs.toSet,
      doResolveProhibitionAndOverriding)
  }

  private def makeTestInstance(relativeDocPath: String): XbrlInstance = {
    // We expect no spaces in the path, so we can create a relative URI from it.

    val uri = dummyUriPrefix.resolve(relativeDocPath)

    XbrlInstance.build(docBuilder.build(uri).documentElement)
  }

  private def buildTaxonomy(entryPointUris: Set[URI], doResolveProhibitionAndOverriding: Boolean): BasicTaxonomy = {
    val basicTaxo = taxonomyBuilder.build(entryPointUris)

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val effectiveTaxo =
      if (doResolveProhibitionAndOverriding) {
        basicTaxo.resolveProhibitionAndOverriding(relationshipFactory)
      } else {
        basicTaxo
      }

    effectiveTaxo
  }

  private val processor = new Processor(false)

  private val dummyUriPrefix: URI = URI.create("http://www.example.com/")

  private val xpathEvaluatorFactory: SaxonJaxpXPathEvaluatorFactory =
    SaxonJaxpXPathEvaluatorFactory(processor.getUnderlyingConfiguration)

  private implicit val xpathEvaluator: SaxonJaxpXPathEvaluator = xpathEvaluatorFactory.newXPathEvaluator()

  private implicit val xpathScope: Scope = Scope.Empty

  private val docBuilder: SaxonDocumentBuilder = {
    val otherRootDir = new File(classOf[ConceptRelationshipNodeTest].getResource("/xbrl-and-w3").toURI)
    val zipFile = new File(classOf[ConceptRelationshipNodeTest].getResource("/table-linkbase-conf-2015-08-12.zip").toURI)

    val xbrlAndW3UriPartialResolver = PartialUriResolvers.fromLocalMirrorRootDirectory(otherRootDir)

    val catalog =
      SimpleCatalog(
        None,
        Vector(SimpleCatalog.UriRewrite(None, dummyUriPrefix.toString, "")))

    val zipFilePartialResolver = PartialUriResolvers.forZipFileUsingCatalog(new ZipFile(zipFile), catalog)

    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      UriResolvers.fromPartialUriResolversWithFallback(
        Vector(zipFilePartialResolver, xbrlAndW3UriPartialResolver)))
  }

  private val taxonomyBuilder: TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    taxoBuilder
  }
}
