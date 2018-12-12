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

package eu.cdevreeze.tqa.utils

import java.io.File
import java.net.URI

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.base.model.AllRelationship
import eu.cdevreeze.tqa.base.model.HypercubeDimensionRelationship
import eu.cdevreeze.tqa.base.model.Node
import eu.cdevreeze.tqa.base.model.ParentChildRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * SimpleTaxonomyCreator test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class SimpleTaxonomyCreatorTest extends FunSuite {

  private def mkConceptNode(tns: String, localName: String): Node.GlobalElementDecl = {
    Node.GlobalElementDecl(EName(tns, localName))
  }

  test("testAddArcs") {
    val taxoBuilder = getTaxoBuilder()
    val taxo: BasicTaxonomy =
      taxoBuilder.build(Set(URI.create("http://www.test.com/test/entrypoint.xsd")))
        .ensuring(_.relationships.isEmpty)
        .ensuring(_.findAllPrimaryItemDeclarations.size >= 5)
        .ensuring(_.findAllHypercubeDeclarations.size >= 2)
        .ensuring(_.findAllExplicitDimensionDeclarations.size >= 2)
        .ensuring(_.findAllTypedDimensionDeclarations.isEmpty)

    val pElr = "urn:test:linkrole:my-report"

    val tns = "http://www.test.com/test/data"

    val presDocUri = URI.create("http://www.test.com/test/presentation.xml")
      .ensuring(u => taxo.taxonomyBase.taxonomyDocUriMap.contains(u))

    val pArcs = Vector(
      ParentChildRelationship(pElr, mkConceptNode(tns, "c1"), mkConceptNode(tns, "c2"), Map(ENames.OrderEName -> "1")),
      ParentChildRelationship(pElr, mkConceptNode(tns, "c2"), mkConceptNode(tns, "c3"), Map(ENames.OrderEName -> "2")),
      ParentChildRelationship(pElr, mkConceptNode(tns, "c2"), mkConceptNode(tns, "c4"), Map(ENames.OrderEName -> "3")),
      ParentChildRelationship(pElr, mkConceptNode(tns, "c4"), mkConceptNode(tns, "c5"), Map(ENames.OrderEName -> "4")),
      ParentChildRelationship(pElr, mkConceptNode(tns, "c4"), mkConceptNode(tns, "c6"), Map(ENames.OrderEName -> "5")))

    val hypercubeElr = "urn:test:linkrole:my-hypercubes"

    val hypercubeTns = "http://www.test.com/test/axes"

    val hypercubeDocUri = URI.create("http://www.test.com/test/hypercubes.xml")
      .ensuring(u => taxo.taxonomyBase.taxonomyDocUriMap.contains(u))

    val allArcs = Vector(
      AllRelationship(hypercubeElr, mkConceptNode(tns, "c1"), mkConceptNode(hypercubeTns, "Hypercube1"), Map(ENames.OrderEName -> "1")),
      AllRelationship(hypercubeElr, mkConceptNode(tns, "c2"), mkConceptNode(hypercubeTns, "Hypercube1"), Map(ENames.OrderEName -> "2")),
      AllRelationship(hypercubeElr, mkConceptNode(tns, "c3"), mkConceptNode(hypercubeTns, "Hypercube2"), Map(ENames.OrderEName -> "3")))

    val hdArcs = Vector(
      HypercubeDimensionRelationship(
        hypercubeElr,
        mkConceptNode(hypercubeTns, "Hypercube1"),
        mkConceptNode(hypercubeTns, "RegionAxis"),
        Map(ENames.OrderEName -> "1")),
      HypercubeDimensionRelationship(
        hypercubeElr,
        mkConceptNode(hypercubeTns, "Hypercube1"),
        mkConceptNode(hypercubeTns, "ProductAxis"),
        Map(ENames.OrderEName -> "2")),
      HypercubeDimensionRelationship(
        hypercubeElr,
        mkConceptNode(hypercubeTns, "Hypercube2"),
        mkConceptNode(hypercubeTns, "RegionAxis"),
        Map(ENames.OrderEName -> "3")))

    val taxoCreator: SimpleTaxonomyCreator =
      SimpleTaxonomyCreator(taxo)
        .addParentChildArcs(presDocUri, pElr, pArcs)
        .addDimensionalArcs(hypercubeDocUri, hypercubeElr, allArcs ++ hdArcs)

    assertResult(5) {
      taxoCreator.startTaxonomy.findAllParentChildRelationships.size
    }
    assertResult(3) {
      taxoCreator.startTaxonomy.computeHasHypercubeInheritanceOrSelf.keySet.size
    }
    assertResult(3) {
      taxoCreator.startTaxonomy.findAllHypercubeDimensionRelationships.size
    }
  }

  private def getTaxoBuilder(): TaxonomyBuilder = {
    val utilsDir =
      (new File(classOf[SimpleTaxonomyCreatorTest].getResource("axes.xsd").toURI)).getParentFile.ensuring(_.isDirectory)
    val utilsDirUri = URI.create(utilsDir.toURI.toString.stripSuffix("/") + "/")

    val xbrlDirUri = utilsDirUri.resolve("../../../../xbrl-and-w3/").ensuring(u => (new File(u)).isDirectory)

    val docParser = DocumentParserUsingStax.newInstance()

    val catalog: SimpleCatalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(None, "http://www.test.com/test/", utilsDirUri.toString),
          SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", xbrlDirUri.resolve("www.xbrl.org/").toString),
          SimpleCatalog.UriRewrite(None, "http://www.w3.org/", xbrlDirUri.resolve("www.w3.org/").toString)))

    val uriResolver = UriResolvers.fromCatalogWithoutFallback(catalog)

    val docBuilder: DocumentBuilder = new IndexedDocumentBuilder(docParser, uriResolver)

    TaxonomyBuilder
      .withDocumentBuilder(docBuilder)
      .withDocumentCollector(DefaultDtsCollector())
      .withStrictRelationshipFactory
  }
}
