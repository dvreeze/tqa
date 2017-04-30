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

package eu.cdevreeze.tqa.extension.table.relationship

import java.net.URI

import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.dom.NonStandardArc
import eu.cdevreeze.tqa.dom.NonStandardResource
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.extension.table.dom.AspectNode
import eu.cdevreeze.tqa.extension.table.dom.AspectNodeFilterArc
import eu.cdevreeze.tqa.extension.table.dom.BreakdownTreeArc
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.DefinitionNodeSubtreeArc
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.dom.TableArc
import eu.cdevreeze.tqa.extension.table.dom.TableBreakdown
import eu.cdevreeze.tqa.extension.table.dom.TableBreakdownArc
import eu.cdevreeze.tqa.extension.table.dom.TableFilterArc
import eu.cdevreeze.tqa.extension.table.dom.TableParameterArc
import eu.cdevreeze.tqa.extension.table.dom.TableResource
import eu.cdevreeze.tqa.relationship.NonStandardRelationship

/**
 * Relationship in a table link.
 *
 * @author Chris de Vreeze
 */
sealed abstract class TableRelationship(
    val underlyingRelationship: NonStandardRelationship,
    val arc: TableArc,
    val sourceElem: AnyRef,
    val targetElem: AnyRef) {

  requireArcrole(arc.arcrole)

  def validated: this.type

  final def elr: String = underlyingRelationship.elr

  final def arcrole: String = underlyingRelationship.arcrole

  final def docUri: URI = underlyingRelationship.docUri

  final override def equals(obj: Any): Boolean = obj match {
    case other: TableRelationship =>
      other.underlyingRelationship == this.underlyingRelationship
    case _ => false
  }

  final override def hashCode: Int = underlyingRelationship.hashCode

  protected[relationship] def requireArcrole(arcrole: String): Unit = {
    require(
      this.arcrole == arcrole,
      s"Expected arcrole $arcrole but found ${this.arcrole} in $docUri")
  }
}

/**
 * A table-breakdown relationship.
 */
final class TableBreakdownRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: Table,
    targetElem: TableBreakdown) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-breakdown")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem.underlyingResource == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }

  def table: Table = sourceElem

  def breakdown: TableBreakdown = targetElem
}

/**
 * A breakdown-tree relationship.
 */
final class BreakdownTreeRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: TableBreakdown,
    targetElem: DefinitionNode) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/breakdown-tree")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem.underlyingResource == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }

  def breakdown: TableBreakdown = sourceElem

  def definitionNode: DefinitionNode = targetElem
}

/**
 * A definition-node-subtree relationship.
 */
final class DefinitionNodeSubtreeRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: DefinitionNode,
    targetElem: DefinitionNode) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/definition-node-subtree")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem.underlyingResource == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }
}

/**
 * A table-filter relationship.
 *
 * TODO Filter in formula DOM.
 */
final class TableFilterRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: Table,
    targetElem: NonStandardResource) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-filter")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }

  def table: Table = sourceElem

  def filter: NonStandardResource = targetElem
}

/**
 * A table-parameter relationship.
 *
 * TODO Parameter in formula DOM.
 */
final class TableParameterRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: Table,
    targetElem: NonStandardResource) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-parameter")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }

  def table: Table = sourceElem

  def parameter: NonStandardResource = targetElem
}

/**
 * An aspect-node-filter relationship.
 *
 * TODO Filter in formula DOM.
 */
final class AspectNodeFilterRelationship(
    underlyingRelationship: NonStandardRelationship,
    arc: TableArc,
    sourceElem: AspectNode,
    targetElem: NonStandardResource) extends TableRelationship(underlyingRelationship, arc, sourceElem, targetElem) {

  requireArcrole("http://xbrl.org/arcrole/2014/aspect-node-filter")

  final def validated: this.type = {
    require(sourceElem.underlyingResource == underlyingRelationship.sourceElem, s"Source element not matching source of underlying relationship in $docUri")
    require(targetElem == underlyingRelationship.targetElem, s"Target element not matching target of underlying relationship in $docUri")
    this
  }

  def aspectNode: AspectNode = sourceElem

  def filter: NonStandardResource = targetElem
}

object TableRelationship {

  /**
   * Lenient method to optionally create a TableRelationship from an underlying tqa.relationship.StandardRelationship.
   */
  def opt(underlyingRelationship: NonStandardRelationship): Option[TableRelationship] = {
    if (underlyingRelationship.arc.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      val arc = toOptionalTableArc(underlyingRelationship.arc).getOrElse(underlyingRelationship.arc)
      val sourceElem = toOptionalTableResource(underlyingRelationship.sourceElem).getOrElse(underlyingRelationship.sourceElem)
      val targetElem = toOptionalTableResource(underlyingRelationship.targetElem).getOrElse(underlyingRelationship.targetElem)

      (arc, sourceElem, targetElem, underlyingRelationship.arcrole) match {
        case (arc: TableBreakdownArc, source: Table, target: TableBreakdown, "http://xbrl.org/arcrole/2014/table-breakdown") =>
          Some(new TableBreakdownRelationship(underlyingRelationship, arc, source, target))
        case (arc: BreakdownTreeArc, source: TableBreakdown, target: DefinitionNode, "http://xbrl.org/arcrole/2014/breakdown-tree") =>
          Some(new BreakdownTreeRelationship(underlyingRelationship, arc, source, target))
        case (arc: DefinitionNodeSubtreeArc, source: DefinitionNode, target: DefinitionNode, "http://xbrl.org/arcrole/2014/definition-node-subtree") =>
          Some(new DefinitionNodeSubtreeRelationship(underlyingRelationship, arc, source, target))
        case (arc: TableFilterArc, source: Table, target: NonStandardResource, "http://xbrl.org/arcrole/2014/table-filter") =>
          Some(new TableFilterRelationship(underlyingRelationship, arc, source, target))
        case (arc: TableParameterArc, source: Table, target: NonStandardResource, "http://xbrl.org/arcrole/2014/table-parameter") =>
          Some(new TableParameterRelationship(underlyingRelationship, arc, source, target))
        case (arc: AspectNodeFilterArc, source: AspectNode, target: NonStandardResource, "http://xbrl.org/arcrole/2014/aspect-node-filter") =>
          Some(new AspectNodeFilterRelationship(underlyingRelationship, arc, source, target))
      }
    } else {
      None
    }
  }

  private def toOptionalTableArc(arc: XLinkArc): Option[TableArc] = {
    arc match {
      case arc: NonStandardArc => TableArc.opt(arc)
      case _                   => None
    }
  }

  private def toOptionalTableResource(taxoElem: TaxonomyElem): Option[TableResource] = {
    taxoElem match {
      case res: NonStandardResource => TableResource.opt(res)
      case _                        => None
    }
  }
}
