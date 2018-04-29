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

import scala.collection.immutable
import scala.annotation.tailrec
import scala.reflect.classTag
import scala.util.Try

import LayoutModelElem._
import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.tqa.extension.table.common.TableAxis
import eu.cdevreeze.tqa.extension.table.layoutmodel.common.LabelSource
import eu.cdevreeze.tqa.extension.table.layoutmodel.common.LayoutModelAspects
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * XML element inside a table layout model. This API is immutable, provided the backing element is immutable.
 *
 * The yaidom `SubtypeAwareElemApi` and `ScopedElemApi` query API is offered.
 *
 * Note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying.
 *
 * It is not required that the layout model elements are schema-valid. Construction of a layout model element is indeed quite lenient.
 *
 * Note that the backing element implementation can be any implementation of yaidom query API trait `BackingNodes.Elem`.
 *
 * Creation of `LayoutModelElem` objects is designed not to fail, even if the XML element is not layout model content.
 * Of course, after creation many query methods may fail in such cases. It is also possible to use these data classes for
 * layout model elements embedded in other XML elements, or only for parts of layout models.
 *
 * @author Chris de Vreeze
 */
sealed abstract class LayoutModelElem private[dom] (
  val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends ScopedNodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  // TODO Restore old equality on the backing elements themselves (after JS DOM wrappers have appropriate equality)
  assert(
    childElems.map(_.backingElem).map(_.resolvedName) == backingElem.findAllChildElems.map(_.resolvedName),
    msg("Corrupt element!"))

  type ThisElem = LayoutModelElem

  type ThisNode = LayoutModelElem

  final def thisElem: ThisElem = this

  // We are not interested in non-element children

  final def children: immutable.IndexedSeq[ThisNode] = findAllChildElems

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[LayoutModelElem] = childElems

  final def resolvedName: EName = backingElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = backingElem.resolvedAttributes

  final def qname: QName = backingElem.qname

  final def attributes: immutable.Iterable[(QName, String)] = backingElem.attributes

  final def scope: Scope = backingElem.scope

  final def text: String = backingElem.text

  final override def equals(other: Any): Boolean = other match {
    case e: LayoutModelElem => backingElem == e.backingElem
    case _ => false
  }

  final override def hashCode: Int = backingElem.hashCode

  private def msg(s: String): String = s"${s} (${backingElem.key})"
}

/**
 * Table model root element.
 *
 * It does not check validity of the table model.
 *
 * @author Chris de Vreeze
 */
final class TableModel private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelTableModelEName, s"Expected EName $ModelTableModelEName but found $resolvedName")

  def findAllTableSets: immutable.IndexedSeq[TableSet] = {
    findAllChildElemsOfType(classTag[TableSet])
  }

  def firstTable: Table = {
    findAllTableSets.map(_.firstTable).head
  }
}

/**
 * Table set in a table model
 *
 * @author Chris de Vreeze
 */
final class TableSet private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelTableSetEName, s"Expected EName $ModelTableSetEName but found $resolvedName")

  def findAllTables: immutable.IndexedSeq[Table] = {
    findAllChildElemsOfType(classTag[Table])
  }

  def findAllLabels: immutable.IndexedSeq[Label] = {
    findAllChildElemsOfType(classTag[Label])
  }

  def firstTable: Table = {
    findAllTables.head
  }
}

/**
 * Table in a table model
 *
 * @author Chris de Vreeze
 */
final class Table private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelTableEName, s"Expected EName $ModelTableEName but found $resolvedName")

  def findAllHeadersElems: immutable.IndexedSeq[HeadersElem] = {
    findAllChildElemsOfType(classTag[HeadersElem])
  }

  /**
   * Returns the mandatory cells child element.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getCellsElem: CellsElem = {
    getChildElemOfType(classTag[CellsElem])(_ => true)
  }

  def findHeadersElemByAxis(axis: TableAxis): Option[HeadersElem] = {
    findChildElemOfType(classTag[HeadersElem])(_.axis == axis)
  }

  /**
   * Returns `findHeadersElemByAxis(axis).get`, failing with an exception of there is not headers element
   * for the given axis.
   */
  def getHeadersElemByAxis(axis: TableAxis): HeadersElem = {
    findHeadersElemByAxis(axis).getOrElse(sys.error(s"Missing headers element for axis '$axis'"))
  }

  /**
   * Filters the descendant header cell elements that obey the given predicate.
   *
   * This method is useful to find header cells having certain constraints, e.g. those that contain given
   * aspects or aspect values. The header cells themselves know their (min and max) slice indices, which
   * can be used in further queries.
   */
  def filterHeaderCells(p: HeaderCell => Boolean): immutable.IndexedSeq[HeaderCell] = {
    filterElemsOfType(classTag[HeaderCell])(p)
  }

  def totalSpanForAxis(tableAxis: TableAxis): Int = {
    findHeadersElemByAxis(tableAxis).map(_.totalSpan).getOrElse(1)
  }

  /**
   * Returns the non-header cell with the given zero-based coordinates.
   * If there is no Z-axis, the z-coordinate must be 0.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getCellAtCoordinates(x: Int, y: Int, z: Int): NonHeaderCell = {
    val parentCellsElem = getCellsElemAtYAndZCoordinates(y, z).ensuring(_.axis == TableAxis.XAxis)

    parentCellsElem.findAllContentChildElems.ensuring(_.forall(_.resolvedName == ModelCellEName))
      .collect { case cell: NonHeaderCell => cell }.apply(x)
  }

  /**
   * Returns the cells element with the given zero-based Y and Z coordinates.
   * If there is no Z-axis, the z-coordinate must be 0.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getCellsElemAtYAndZCoordinates(y: Int, z: Int): CellsElem = {
    val parentCellsElem = getCellsElemAtZCoordinate(z).ensuring(_.axis == TableAxis.YAxis)

    val resultElem =
      parentCellsElem.findAllContentChildElems.ensuring(_.forall(_.resolvedName == ModelCellsEName))
        .collect { case e: CellsElem => e }.apply(y)

    resultElem.ensuring(_.axis == TableAxis.XAxis, s"Corrupt layout model due to inproper nesting of axes")
  }

  /**
   * Returns the cells element with the given zero-based Z coordinate.
   * If there is no Z-axis, the z-coordinate must be 0 and the outermost cells element is returned.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getCellsElemAtZCoordinate(z: Int): CellsElem = {
    val outermostCellsElem = getCellsElem

    val resultElem =
      outermostCellsElem.axis match {
        case TableAxis.ZAxis =>
          outermostCellsElem.findAllContentChildElems.ensuring(_.forall(_.resolvedName == ModelCellsEName))
            .collect { case e: CellsElem => e }.apply(z)
        case TableAxis.YAxis =>
          require(z == 0, s"There is no Z-axis, yet the z-coordinate is not 0 but $z")
          outermostCellsElem
        case TableAxis.XAxis =>
          sys.error(s"Corrupt layout model due to the outermost level being the X-axis")
      }

    resultElem.ensuring(_.axis == TableAxis.YAxis, s"Corrupt layout model due to inproper nesting of axes")
  }
}

/**
 * Label in a table model
 *
 * @author Chris de Vreeze
 */
final class Label private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelLabelEName, s"Expected EName $ModelLabelEName but found $resolvedName")

  /**
   * Returns the optional labelSource attribute, with default "explicit".
   * This may fail with an exception if the document is not schema-valid.
   */
  def source: LabelSource = {
    attributeOption(SourceEName).map(v => LabelSource.fromString(v)).getOrElse(LabelSource.Explicit)
  }
}

// Headers and their content

/**
 * Headers element in a table model. It represents one axis of the table.
 *
 * @author Chris de Vreeze
 */
final class HeadersElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelHeadersEName, s"Expected EName $ModelHeadersEName but found $resolvedName")

  /**
   * Finds all groups, returning them in the correct order.
   */
  def findAllGroups: immutable.IndexedSeq[Group] = {
    findAllChildElemsOfType(classTag[Group])
  }

  /**
   * Returns the axis attribute.
   * This may fail with an exception if the document is not schema-valid.
   */
  def axis: TableAxis = {
    TableAxis.fromString(attribute(AxisEName))
  }

  /**
   * Returns the collection of header cells at the given zero-based slice index, one header cell per header.
   * The result is ordered, starting with the outermost header cells and ending with the innermost header cells.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getHeaderCellsAtSliceIndex(sliceIndex: Int): immutable.IndexedSeq[HeaderCell] = {
    findAllGroups.flatMap(_.getHeaderCellsAtSliceIndex(sliceIndex))
  }

  /**
   * Returns the total span of the first group.
   * This may fail with an exception if the document is not schema-valid.
   *
   * Note that all headers for the same axis must have the same total span.
   */
  def totalSpan: Int = {
    findAllGroups.headOption.map(_.totalSpan).getOrElse(1)
  }
}

/**
 * Group in a table model
 *
 * @author Chris de Vreeze
 */
final class Group private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelGroupEName, s"Expected EName $ModelGroupEName but found $resolvedName")

  /**
   * Finds all header elements, returning them in the correct order.
   */
  def findAllHeaders: immutable.IndexedSeq[Header] = {
    findAllChildElemsOfType(classTag[Header])
  }

  def findAllLabels: immutable.IndexedSeq[Label] = {
    findAllChildElemsOfType(classTag[Label])
  }

  /**
   * Returns the collection of header cells at the given zero-based slice index, one header cell per header.
   * The result is ordered, starting with the outermost header cells and ending with the innermost header cells.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getHeaderCellsAtSliceIndex(sliceIndex: Int): immutable.IndexedSeq[HeaderCell] = {
    findAllHeaders.map(_.getHeaderCellAtSliceIndex(sliceIndex))
  }

  /**
   * Returns the total span of the first header.
   * This may fail with an exception if the document is not schema-valid.
   *
   * Note that all headers for the same axis must have the same total span.
   */
  def totalSpan: Int = {
    findAllHeaders.headOption.map(_.totalSpan).getOrElse(1)
  }
}

/**
 * Header in a table model
 *
 * @author Chris de Vreeze
 */
final class Header private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelHeaderEName, s"Expected EName $ModelHeaderEName but found $resolvedName")

  def findAllHeaderCells: immutable.IndexedSeq[HeaderCell] = {
    findAllChildElemsOfType(classTag[HeaderCell])
  }

  /**
   * Returns the total span of the header.
   * This may fail with an exception if the document is not schema-valid.
   *
   * Note that all headers for the same axis must have the same total span.
   */
  def totalSpan: Int = {
    findAllHeaderCells.map(_.span).sum
  }

  /**
   * Returns the header cell at the given zero-based slice index.
   * This may fail with an exception if the document is not schema-valid.
   */
  def getHeaderCellAtSliceIndex(sliceIndex: Int): HeaderCell = {
    getHeaderCellAtRelativeSliceIndex(sliceIndex, findAllHeaderCells.toList)
  }

  @tailrec
  private def getHeaderCellAtRelativeSliceIndex(sliceIndex: Int, headerCells: List[HeaderCell]): HeaderCell = {
    require(sliceIndex >= 0)

    headerCells match {
      case Nil =>
        sys.error(s"Slice index out of bounds")
      case cell :: tail if sliceIndex < cell.span =>
        cell
      case cell :: tail =>
        // Recursive call
        getHeaderCellAtRelativeSliceIndex(sliceIndex - cell.span, tail)
    }
  }
}

/**
 * Header cell in a table model
 *
 * @author Chris de Vreeze
 */
final class HeaderCell private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelCellEName, s"Expected EName $ModelCellEName but found $resolvedName")

  def findAllConstraints: immutable.IndexedSeq[Constraint] = {
    findAllChildElemsOfType(classTag[Constraint])
  }

  def findAllConceptAspectConstraints: immutable.IndexedSeq[ConceptAspectConstraint] = {
    findAllChildElemsOfType(classTag[ConceptAspectConstraint])
  }

  def findAllEntityIdentifierAspectConstraints: immutable.IndexedSeq[EntityIdentifierAspectConstraint] = {
    findAllChildElemsOfType(classTag[EntityIdentifierAspectConstraint])
  }

  def findAllPeriodAspectConstraints: immutable.IndexedSeq[PeriodAspectConstraint] = {
    findAllChildElemsOfType(classTag[PeriodAspectConstraint])
  }

  def findAllUnitAspectConstraints: immutable.IndexedSeq[UnitAspectConstraint] = {
    findAllChildElemsOfType(classTag[UnitAspectConstraint])
  }

  def findAllSegmentAspectConstraints: immutable.IndexedSeq[SegmentAspectConstraint] = {
    findAllChildElemsOfType(classTag[SegmentAspectConstraint])
  }

  def findAllScenarioAspectConstraints: immutable.IndexedSeq[ScenarioAspectConstraint] = {
    findAllChildElemsOfType(classTag[ScenarioAspectConstraint])
  }

  def findAllDimensionAspectConstraints: immutable.IndexedSeq[DimensionAspectConstraint] = {
    findAllChildElemsOfType(classTag[DimensionAspectConstraint])
  }

  def findAllLabels: immutable.IndexedSeq[Label] = {
    findAllChildElemsOfType(classTag[Label])
  }

  /**
   * Returns the optional span attribute as integer, with default 1.
   * This may fail with an exception if the document is not schema-valid.
   */
  def span: Int = {
    attributeOption(SpanEName).map(_.toInt).getOrElse(1)
  }

  /**
   * Returns the optional rollup attribute as boolean, with default false.
   * This may fail with an exception if the document is not schema-valid.
   */
  def rollup: Boolean = {
    attributeOption(RollupEName).map(attrValue => XsdBooleans.parseBoolean(attrValue)).getOrElse(false)
  }

  /**
   * Returns the axis of the ancestor headers element.
   * This may fail with an exception if the document is not schema-valid.
   */
  def axis: TableAxis = {
    val axisAsString = backingElem.findAncestor(_.resolvedName == ModelHeadersEName)
      .ensuring(_.nonEmpty).get.attribute(AxisEName)

    TableAxis.fromString(axisAsString)
  }

  /**
   * Returns the minimal zero-based slice index of this header cell.
   * This may fail with an exception if the document is not schema-valid.
   */
  def minSliceIndex: Int = {
    val cellIdx = backingElem.path.lastEntry.ensuring(_.elementName == resolvedName).index

    val spanBeforeThisCell = backingElem.parent.ensuring(_.resolvedName == ModelHeaderEName)
      .filterChildElems(_.resolvedName == ModelCellEName)
      .take(cellIdx)
      .map(_.attributeOption(SpanEName).map(_.toInt).getOrElse(1))
      .sum

    spanBeforeThisCell
  }

  /**
   * Returns the maximum zero-based slice index of this header cell.
   * This may fail with an exception if the document is not schema-valid.
   */
  def maxSliceIndex: Int = {
    val cellIdx = backingElem.path.lastEntry.ensuring(_.elementName == resolvedName).index

    val spanBeforeThisCell = backingElem.parent.ensuring(_.resolvedName == ModelHeaderEName)
      .filterChildElems(_.resolvedName == ModelCellEName)
      .take(cellIdx)
      .map(_.attributeOption(SpanEName).map(_.toInt).getOrElse(1))
      .sum

    spanBeforeThisCell + (span - 1)
  }

  def aspects: Set[LayoutModelAspects.Aspect] = {
    findAllConstraints.map(_.aspect).toSet
  }

  /**
   * Filters the constraints by taking tag selectors into account. This is inspired by the table specification, section 7.6.
   * At most one constraint set is returned. Therefore the result can be used for its optional tag, which can be used in queries
   * for specific types of constraints.
   */
  def filterConstraintsMatchingTagSelectors(tagSelectors: Set[String]): immutable.IndexedSeq[Constraint] = {
    val constraintSetsByTagOption: Map[Option[String], immutable.IndexedSeq[Constraint]] =
      findAllConstraints.groupBy(_.tagOption)

    val overlappingTags: Set[String] =
      constraintSetsByTagOption.keySet.flatten.intersect(tagSelectors)

    if (overlappingTags.isEmpty) {
      constraintSetsByTagOption.getOrElse(None, immutable.IndexedSeq())
    } else if (overlappingTags.size == 1) {
      constraintSetsByTagOption.apply(Option(overlappingTags.head))
    } else {
      immutable.IndexedSeq()
    }
  }
}

/**
 * Constraint in a table model
 *
 * @author Chris de Vreeze
 */
sealed abstract class Constraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelConstraintEName, s"Expected EName $ModelConstraintEName but found $resolvedName")

  /**
   * Returns the mandatory aspect child element.
   * This may fail with an exception if the document is not schema-valid.
   */
  final def getAspectElem: AspectElem = {
    getChildElemOfType(classTag[AspectElem])(_ => true)
  }

  /**
   * Returns the mandatory value child element.
   * This may fail with an exception if the document is not schema-valid.
   */
  final def getValueElem: ValueElem = {
    getChildElemOfType(classTag[ValueElem])(_ => true)
  }

  final def tagOption: Option[String] = {
    attributeOption(TagEName)
  }

  final def aspect: LayoutModelAspects.Aspect = {
    getAspectElem.aspect
  }

  final def aspectInDimensionalAspectModel: tqa.aspect.Aspect = {
    aspect.aspectInDimensionalAspectModel
  }
}

/**
 * Concept aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class ConceptAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.Concept, s"Expected aspect ${LayoutModelAspects.Aspect.Concept} but found $aspect")

  /**
   * Returns the concept aspect value.
   * This may fail with an exception if the document is not (schema-)valid.
   */
  def conceptAspectValue: EName = getValueElem.textAsResolvedQName
}

/**
 * Entity identifier aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class EntityIdentifierAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.EntityIdentifier, s"Expected aspect ${LayoutModelAspects.Aspect.EntityIdentifier} but found $aspect")
}

/**
 * Period aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class PeriodAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.Period, s"Expected aspect ${LayoutModelAspects.Aspect.Period} but found $aspect")
}

/**
 * Unit aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class UnitAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.UnitAspect, s"Expected aspect ${LayoutModelAspects.Aspect.UnitAspect} but found $aspect")
}

/**
 * Segment aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class SegmentAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.Segment, s"Expected aspect ${LayoutModelAspects.Aspect.Segment} but found $aspect")
}

/**
 * Scenario aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class ScenarioAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  require(aspect == LayoutModelAspects.Aspect.Scenario, s"Expected aspect ${LayoutModelAspects.Aspect.Scenario} but found $aspect")
}

/**
 * Dimension aspect constraint in a table model
 *
 * @author Chris de Vreeze
 */
final class DimensionAspectConstraint private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends Constraint(backingElem, childElems) {

  /**
   * Returns the dimension aspect value, if it is an explicit dimension member.
   * This may fail with an exception if the document is not (schema-)valid.
   */
  def dimensionAspectENameValue: EName = getValueElem.textAsResolvedQName
}

/**
 * Aspect element in a table model
 *
 * @author Chris de Vreeze
 */
final class AspectElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelAspectEName, s"Expected EName $ModelAspectEName but found $resolvedName")

  def aspect: LayoutModelAspects.Aspect = {
    LayoutModelAspects.Aspect.fromDisplayString(text, scope)
  }
}

/**
 * Value element in a table model
 *
 * @author Chris de Vreeze
 */
final class ValueElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelValueEName, s"Expected EName $ModelValueEName but found $resolvedName")
}

// Non-header cells and their content

sealed trait CellsContentElem extends LayoutModelElem

/**
 * Cells element in a table model
 *
 * @author Chris de Vreeze
 */
final class CellsElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) with CellsContentElem {

  require(resolvedName == ModelCellsEName, s"Expected EName $ModelCellsEName but found $resolvedName")

  def findAllContentChildElems: immutable.IndexedSeq[CellsContentElem] = {
    findAllChildElemsOfType(classTag[CellsContentElem])
  }

  def findAllContentDescendantElems: immutable.IndexedSeq[CellsContentElem] = {
    findAllElemsOfType(classTag[CellsContentElem])
  }

  /**
   * Returns the axis attribute.
   * This may fail with an exception if the document is not schema-valid.
   */
  def axis: TableAxis = {
    TableAxis.fromString(attribute(AxisEName))
  }
}

/**
 * Non-header cell in a table model
 *
 * @author Chris de Vreeze
 */
final class NonHeaderCell private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) with CellsContentElem {

  require(resolvedName == ModelCellEName, s"Expected EName $ModelCellEName but found $resolvedName")

  def findAllFactElems: immutable.IndexedSeq[FactElem] = {
    findAllChildElemsOfType(classTag[FactElem])
  }

  /**
   * Returns the optional abstract attribute as boolean, with default false.
   * This may fail with an exception if the document is not schema-valid.
   */
  def isAbstract: Boolean = {
    attributeOption(AbstractEName).map(attrValue => XsdBooleans.parseBoolean(attrValue)).getOrElse(false)
  }
}

/**
 * Fact element in a table model
 *
 * @author Chris de Vreeze
 */
final class FactElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems) {

  require(resolvedName == ModelFactEName, s"Expected EName $ModelFactEName but found $resolvedName")
}

/**
 * Other layout model element in a table model
 *
 * @author Chris de Vreeze
 */
final class OtherLayoutModelElem private[dom] (
  override val backingElem: BackingNodes.Elem,
  childElems: immutable.IndexedSeq[LayoutModelElem]) extends LayoutModelElem(backingElem, childElems)

// Companion objects

object LayoutModelElem {

  val ModelNs = "http://xbrl.org/2014/table/model"

  val ModelTableModelEName = EName(ModelNs, "tableModel")
  val ModelTableSetEName = EName(ModelNs, "tableSet")
  val ModelTableEName = EName(ModelNs, "table")
  val ModelLabelEName = EName(ModelNs, "label")
  val ModelHeadersEName = EName(ModelNs, "headers")
  val ModelCellsEName = EName(ModelNs, "cells")
  val ModelGroupEName = EName(ModelNs, "group")
  val ModelHeaderEName = EName(ModelNs, "header")
  val ModelCellEName = EName(ModelNs, "cell")
  val ModelConstraintEName = EName(ModelNs, "constraint")
  val ModelAspectEName = EName(ModelNs, "aspect")
  val ModelValueEName = EName(ModelNs, "value")
  val ModelFactEName = EName(ModelNs, "fact")

  val SpanEName = EName("span")
  val RollupEName = EName("rollup")
  val SourceEName = EName("source")
  val TagEName = EName("tag")
  val AbstractEName = EName("abstract")
  val AxisEName = EName("axis")

  /**
   * Expensive method to create an LayoutModelElem tree
   */
  def build(elem: BackingNodes.Elem): LayoutModelElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => build(e))
    apply(elem, childElems)
  }

  private[dom] def apply(elem: BackingNodes.Elem, childElems: immutable.IndexedSeq[LayoutModelElem]): LayoutModelElem = {
    elem.resolvedName match {
      case ModelTableModelEName => new TableModel(elem, childElems)
      case ModelTableSetEName => new TableSet(elem, childElems)
      case ModelTableEName => new Table(elem, childElems)
      case ModelLabelEName => new Label(elem, childElems)
      case ModelHeadersEName => new HeadersElem(elem, childElems)
      case ModelCellsEName => new CellsElem(elem, childElems)
      case ModelGroupEName => new Group(elem, childElems)
      case ModelHeaderEName => new Header(elem, childElems)
      case ModelCellEName if elem.parentOption.map(_.resolvedName).contains(ModelHeaderEName) =>
        new HeaderCell(elem, childElems)
      case ModelCellEName => new NonHeaderCell(elem, childElems)
      case ModelConstraintEName =>
        Constraint.opt(elem, childElems).getOrElse(new OtherLayoutModelElem(elem, childElems))
      case ModelAspectEName => new AspectElem(elem, childElems)
      case ModelValueEName => new ValueElem(elem, childElems)
      case ModelFactEName => new FactElem(elem, childElems)
      case _ => new OtherLayoutModelElem(elem, childElems)
    }
  }
}

object Constraint {

  private[dom] def opt(elem: BackingNodes.Elem, childElems: immutable.IndexedSeq[LayoutModelElem]): Option[Constraint] = {
    elem.resolvedName match {
      case ModelConstraintEName =>
        val aspectOption: Option[LayoutModelAspects.Aspect] =
          elem.findChildElem(_.resolvedName == ModelAspectEName)
            .flatMap(e => Try(LayoutModelAspects.Aspect.fromDisplayString(e.text, e.scope)).toOption)

        aspectOption.map(_.aspectInDimensionalAspectModel) match {
          case Some(tqa.aspect.Aspect.ConceptAspect) => Some(new ConceptAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.EntityIdentifierAspect) => Some(new EntityIdentifierAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.PeriodAspect) => Some(new PeriodAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.UnitAspect) => Some(new UnitAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.NonXDTSegmentAspect) => Some(new SegmentAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.NonXDTScenarioAspect) => Some(new ScenarioAspectConstraint(elem, childElems))
          case Some(tqa.aspect.Aspect.DimensionAspect(dim)) => Some(new DimensionAspectConstraint(elem, childElems))
          case _ => None
        }
      case _ => None
    }
  }
}

object TableModel {

  def build(elem: BackingNodes.Elem): TableModel = {
    require(elem.resolvedName == ModelTableModelEName, s"Expected $ModelTableModelEName but found ${elem.resolvedName}")
    LayoutModelElem.build(elem).asInstanceOf[TableModel]
  }
}
