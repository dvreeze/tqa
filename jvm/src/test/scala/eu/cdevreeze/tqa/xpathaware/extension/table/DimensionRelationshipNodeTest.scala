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

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa.extension.table.common.DimensionRelationshipNodes.FormulaAxis
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.xpath.saxon.SaxonJaxpXPathEvaluator
import eu.cdevreeze.yaidom.xpath.saxon.SaxonJaxpXPathEvaluatorFactory
import net.sf.saxon.s9api.Processor

/**
 * Dimensional relationship node test case. It uses test data from the XBRL Tables conformance suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class DimensionRelationshipNodeTest extends FunSuite {

  import DimensionRelationshipNodeData.findAllResultPaths

  private val tableExampleNs = "http://www.xbrl.org/table-examples"

  private val relativeUriTo3200Dir = URI.create("table-linkbase-conf-2015-08-12/conf/tests/3200-dimension-relationship-node/")

  // 3210-dimension-relationship-node-relationship-source-testcase-v01i

  test("testDimensionRelationshipNodeWithRelationshipSourceOmitted") {
    val instance =
      makeTestInstance(
        relativeUriTo3200Dir.toString + "3210-dimension-relationship-node-relationship-source/dimension-relationship-node-source-omitted-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set.empty) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3210-dimension-relationship-node-relationship-source-testcase-v02i

  test("testDimensionRelationshipNodeForPartOfADomain") {
    val instance =
      makeTestInstance(
        relativeUriTo3200Dir.toString + "3210-dimension-relationship-node-relationship-source/dimension-relationship-node-partial-domain-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f4"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f4", "f5", "f6", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3210-dimension-relationship-node-relationship-source-testcase-v03i

  test("testDimensionRelationshipNodeWithMultipleRelationshipSources") {
    val instance =
      makeTestInstance(
        relativeUriTo3200Dir.toString + "3210-dimension-relationship-node-relationship-source/dimension-relationship-node-multiple-sources-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f1"), EName(tableExampleNs, "f7"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f1", "f2", "f3", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3210-dimension-relationship-node-relationship-source-testcase-v08i

  test("testDimensionRelationshipNodeWithRelationshipSourceExpression") {
    val instance =
      makeTestInstance(
        relativeUriTo3200Dir.toString + "3210-dimension-relationship-node-relationship-source/dimension-relationship-node-source-expression-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f4"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f4", "f5", "f6", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3210-dimension-relationship-node-relationship-source-testcase-v09i

  test("testDimensionRelationshipNodeWithRelationshipSourcesAndRelationshipSourceExpressions") {
    val instance =
      makeTestInstance(
        relativeUriTo3200Dir.toString + "3210-dimension-relationship-node-relationship-source/dimension-relationship-node-mixed-sources-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f1"), EName(tableExampleNs, "f6"), EName(tableExampleNs, "f7"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f1", "f2", "f3", "f6", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3220-dimension-relationship-node-linkrole-testcase-v01i

  test("testDimensionRelationshipNodeWithLinkroleOmitted") {
    val instance = makeTestInstance(relativeUriTo3200Dir.toString + "3220-dimension-relationship-node-linkrole/linkrole-omitted-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(None) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set.empty) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f0", "f1", "f2", "f3", "f4", "f5").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3220-dimension-relationship-node-linkrole-testcase-v02i

  test("testDimensionRelationshipNodeWithTargetRoleSwitch") {
    val instance = makeTestInstance(relativeUriTo3200Dir.toString + "3220-dimension-relationship-node-linkrole/target-role-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/more-dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f0"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f0", "f1", "f2", "f3").map(nm => EName(tableExampleNs, nm))) {
      members
    }
  }

  // 3220-dimension-relationship-node-linkrole-testcase-v07

  test("testDimensionRelationshipNodeWithSimpleLinkroleExpression") {
    val instance = makeTestInstance(relativeUriTo3200Dir.toString + "3220-dimension-relationship-node-linkrole/linkrole-expression-instance.xml")
    val basicTaxo = buildTaxonomy(instance)

    val tableTaxo = BasicTableTaxonomy.build(basicTaxo)

    val dimensionRelationshipNodes =
      tableTaxo.tableResources collect { case n: DimensionRelationshipNode => n }

    assertResult(1) {
      dimensionRelationshipNodes.size
    }

    val dimensionRelationshipNode = dimensionRelationshipNodes.head
    val dimensionRelationshipNodeData = new DimensionRelationshipNodeData(dimensionRelationshipNode)

    assertResult(EName(tableExampleNs, "F")) {
      dimensionRelationshipNode.dimensionName
    }

    assertResult(Some("http://www.xbrl.org/table-examples/dimension-domains")) {
      dimensionRelationshipNodeData.linkroleOption
    }

    assertResult(Set(EName(tableExampleNs, "f0"))) {
      dimensionRelationshipNodeData.relationshipSources.toSet
    }

    assertResult(FormulaAxis.DescendantOrSelfAxis) {
      dimensionRelationshipNodeData.formulaAxis
    }

    val members: Set[EName] =
      findAllResultPaths(dimensionRelationshipNode, tableTaxo).flatMap(_.relationshipTargetConcepts).toSet

    assertResult(Set("f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9").map(nm => EName(tableExampleNs, nm))) {
      members
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
    val otherRootDir = new File(classOf[DimensionRelationshipNodeTest].getResource("/xbrl-and-w3").toURI)
    val zipFile = new File(classOf[DimensionRelationshipNodeTest].getResource("/table-linkbase-conf-2015-08-12.zip").toURI)

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
