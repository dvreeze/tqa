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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocumentBuilder
import eu.cdevreeze.tqa.dom.TaxonomyBase
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.relationship.DefaultRelationshipFactory
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
