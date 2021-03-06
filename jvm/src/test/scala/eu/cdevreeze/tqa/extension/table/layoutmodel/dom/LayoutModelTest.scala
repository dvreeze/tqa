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

package eu.cdevreeze.tqa.extension.table.layoutmodel.dom

import java.net.URI

import scala.reflect.classTag

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.Namespaces.XbrliNamespace
import eu.cdevreeze.tqa.extension.table.common.TableAxis
import eu.cdevreeze.tqa.extension.table.layoutmodel.common.LayoutModelAspects
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.simple

/**
 * Layout model test case. The test data comes from the XBRL table conformance suite.
 *
 * @author Chris de Vreeze
 */
class LayoutModelTest extends AnyFunSuite {

  private val docParser = DocumentParserUsingStax.newInstance()

  private val tableModel1 = {
    val uri = classOf[LayoutModelTest].getResource("breakdown-defines-no-aspects-2-no-data.xml").toURI
    TableModel.build(indexed.Elem(docParser.parse(uri).documentElement))
  }

  private val tableModel2 = {
    val uri = classOf[LayoutModelTest].getResource("breakdown-defines-no-aspects-2.xml").toURI
    TableModel.build(indexed.Elem(docParser.parse(uri).documentElement))
  }

  private val tableModel3 = {
    val uri = classOf[LayoutModelTest].getResource("concept-relationship-node-multiple-sources.xml").toURI
    TableModel.build(indexed.Elem(docParser.parse(uri).documentElement))
  }

  test("testCreatedTypedLayoutModel") {
    testCreatedTypedLayoutModel(tableModel1)
    testCreatedTypedLayoutModel(tableModel2)
    testCreatedTypedLayoutModel(tableModel3)
  }

  test("testTotalSpans") {
    testTotalSpans(tableModel1)
    testTotalSpans(tableModel2)
    testTotalSpans(tableModel3)
  }

  test("testCellCoordinates") {
    testCellCoordinates(tableModel1)
    testCellCoordinates(tableModel2)
    testCellCoordinates(tableModel3)
  }

  test("testFindSpecificSliceAndConstraints") {
    val ns = "http://www.xbrl.org/table-examples"

    val table = tableModel3.firstTable

    val rendO8HeaderCells =
      table.filterHeaderCells { headerCell =>
        headerCell.findAllConceptAspectConstraints.exists { c =>
          c.conceptAspectValue == EName(ns, "o8")
        }
      }

    assertResult(1) {
      rendO8HeaderCells.size
    }

    assertResult(1) {
      rendO8HeaderCells.head.span
    }

    val sliceIndex = 3

    assertResult(sliceIndex) {
      rendO8HeaderCells.head.minSliceIndex
    }
    assertResult(sliceIndex) {
      rendO8HeaderCells.head.maxSliceIndex
    }

    assertResult(TableAxis.YAxis) {
      rendO8HeaderCells.head.axis
    }

    assertResult(List(LayoutModelAspects.Aspect.Concept)) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.findAllConstraints).map(_.aspect).distinct
    }

    val tagSelectors = Set.empty[String]

    assertResult(List(LayoutModelAspects.Aspect.Concept)) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.filterConstraintsMatchingTagSelectors(tagSelectors)).map(_.aspect).distinct
    }

    assertResult(List(EName(ns, "o5"), EName(ns, "o7"), EName(ns, "o8"))) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.findAllConceptAspectConstraints).map(_.conceptAspectValue)
    }

    assertResult(List("o8_2001", "o8_2002")) {
      val interestingCells =
        for {
          z <- 0 until table.totalSpanForAxis(TableAxis.ZAxis)
          y <- Vector(sliceIndex)
          x <- 0 until table.totalSpanForAxis(TableAxis.XAxis)
        } yield {
          table.getCellAtCoordinates(x, y, z)
        }

      interestingCells.map(_.findAllFactElems.head).map(_.text).map(s => URI.create(s).getFragment)
    }
  }

  test("testFindOtherSpecificSliceAndConstraints") {
    val ns = "http://www.xbrl.org/table-examples"

    val table = tableModel3.firstTable

    val rendO6HeaderCells =
      table.filterHeaderCells { headerCell =>
        headerCell.findAllConceptAspectConstraints.exists { c =>
          c.conceptAspectValue == EName(ns, "o6")
        }
      }

    assertResult(1) {
      rendO6HeaderCells.size
    }

    assertResult(1) {
      rendO6HeaderCells.head.span
    }

    val sliceIndex = 2

    assertResult(sliceIndex) {
      rendO6HeaderCells.head.minSliceIndex
    }
    assertResult(sliceIndex) {
      rendO6HeaderCells.head.maxSliceIndex
    }

    assertResult(TableAxis.YAxis) {
      rendO6HeaderCells.head.axis
    }

    assertResult(List(LayoutModelAspects.Aspect.Concept)) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.findAllConstraints).map(_.aspect).distinct
    }

    val tagSelectors = Set.empty[String]

    assertResult(List(LayoutModelAspects.Aspect.Concept)) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.filterConstraintsMatchingTagSelectors(tagSelectors)).map(_.aspect).distinct
    }

    assertResult(List(EName(ns, "o5"), EName(ns, "o6"))) {
      table.getHeadersElemByAxis(TableAxis.YAxis).getHeaderCellsAtSliceIndex(sliceIndex)
        .flatMap(_.findAllConceptAspectConstraints).map(_.conceptAspectValue)
    }

    assertResult(List("o6_2001", "o6_2002")) {
      val interestingCells =
        for {
          z <- 0 until table.totalSpanForAxis(TableAxis.ZAxis)
          y <- Vector(sliceIndex)
          x <- 0 until table.totalSpanForAxis(TableAxis.XAxis)
        } yield {
          table.getCellAtCoordinates(x, y, z)
        }

      interestingCells.map(_.findAllFactElems.head).map(_.text).map(s => URI.create(s).getFragment)
    }
  }

  test("testParseNonLayoutModel") {
    val uri = classOf[LayoutModelTest].getResource("/sample-instances/sample-xbrl-instance.xml").toURI

    val taxonomyPackageElem: LayoutModelElem =
      LayoutModelElem.build(indexed.Elem(docParser.parse(uri).documentElement))

    assertResult(true) {
      taxonomyPackageElem.findAllElemsOrSelf.forall(_.isInstanceOf[OtherLayoutModelElem])
    }
  }

  test("testQueryWrappedLayoutModel") {
    val unwrappedLayoutModel: TableModel = tableModel3

    val layoutModelElem: LayoutModelElem = addWrapperRootElem(unwrappedLayoutModel, emptyWrapperElem)
    val layoutModel: TableModel = layoutModelElem.findChildElemOfType(classTag[TableModel])(_ => true).get

    testCreatedTypedLayoutModel(layoutModel)
    testTotalSpans(layoutModel)
    testCellCoordinates(layoutModel)
  }

  private def testCreatedTypedLayoutModel(tableModel: TableModel): Unit = {
    val otherLayoutModelElems = tableModel.findAllElemsOrSelfOfType(classTag[OtherLayoutModelElem])

    assertResult(true) {
      otherLayoutModelElems.map(_.backingElem).flatMap(_.parentOption).map(_.resolvedName).toSet
        .subsetOf(Set(LayoutModelElem.ModelValueEName))
    }

    assertResult(true) {
      otherLayoutModelElems.map(_.resolvedName.namespaceUriOption).toSet
        .subsetOf(Set(Option(XbrliNamespace)))
    }
  }

  private def testTotalSpans(tableModel: TableModel): Unit = {
    testTotalSpans(tableModel, TableAxis.YAxis)
    testTotalSpans(tableModel, TableAxis.XAxis)
  }

  private def testCellCoordinates(tableModel: TableModel): Unit = {
    val firstTable = tableModel.firstTable

    val allCells =
      firstTable.getCellsElem.findAllElemsOrSelfOfType(classTag[NonHeaderCell])

    val cellsByCoordinates =
      for {
        z <- Vector(0)
        y <- 0 until firstTable.totalSpanForAxis(TableAxis.YAxis)
        x <- 0 until firstTable.totalSpanForAxis(TableAxis.XAxis)
      } yield {
        firstTable.getCellAtCoordinates(x, y, z)
      }

    assertResult(allCells.map(e => resolved.Elem.from(e))) {
      cellsByCoordinates.map(e => resolved.Elem.from(e))
    }
  }

  private def testTotalSpans(tableModel: TableModel, axis: TableAxis): Unit = {
    val headers =
      tableModel.firstTable.findHeadersElemByAxis(axis).toIndexedSeq
        .flatMap(_.findAllGroups).flatMap(_.findAllHeaders)

    assertResult(true) {
      headers.nonEmpty
    }

    assertResult(Set(headers.head.findAllHeaderCells.map(_.span).sum)) {
      headers.map(_.totalSpan).toSet
    }
  }

  private def addWrapperRootElem(rootElem: LayoutModelElem, emptyWrapperElem: simple.Elem): LayoutModelElem = {
    val simpleRootElem = simple.Elem.from(rootElem.backingElem)

    val wrapperElem = emptyWrapperElem.plusChild(simpleRootElem).notUndeclaringPrefixes(rootElem.scope)

    LayoutModelElem.build(indexed.Elem(wrapperElem))
  }

  private val emptyWrapperElem: simple.Elem = {
    simple.Node.emptyElem(QName("wrapperElem"), Scope.Empty)
  }
}
