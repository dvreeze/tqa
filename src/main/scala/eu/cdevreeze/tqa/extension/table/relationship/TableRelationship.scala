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

import scala.reflect.classTag

import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.dom.AnyTaxonomyElem
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.NonStandardArc
import eu.cdevreeze.tqa.dom.NonStandardResource
import eu.cdevreeze.tqa.dom.Use
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
import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource
import eu.cdevreeze.tqa.xlink.XLinkArc
import eu.cdevreeze.tqa.xlink.XLinkResource
import eu.cdevreeze.yaidom.core.Path

/**
 * Relationship in a table link.
 *
 * @author Chris de Vreeze
 */
sealed abstract class TableRelationship(
    val arc: TableArc,
    val resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem with XLinkResource],
    val resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem with XLinkResource]) {

  require(arc.from == resolvedFrom.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'from' not matching on label in $docUri")
  require(arc.to == resolvedTo.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'to' not matching on label in $docUri")

  final def validated: TableRelationship = {
    require(resolvedFrom.elr == arc.elr, s"Arc and 'from' not in same ELR in $docUri")
    require(resolvedTo.elr == arc.elr, s"Arc and 'to' not in same ELR in $docUri")
    this
  }

  final def sourceElem: AnyTaxonomyElem with XLinkResource = resolvedFrom.resolvedElem

  final def targetElem: AnyTaxonomyElem with XLinkResource = resolvedTo.resolvedElem

  final def docUri: URI = arc.underlyingArc.docUri

  final def baseUri: URI = arc.underlyingArc.baseUri

  final def elr: String = arc.elr

  final def arcrole: String = arc.arcrole

  final def arcPath: Path = arc.underlyingArc.backingElem.path

  final def fromPath: Path = resolvedFrom.xlinkLocatorOrResource.backingElem.path

  final def toPath: Path = resolvedTo.xlinkLocatorOrResource.backingElem.path

  final def baseSetKey: BaseSetKey = arc.underlyingArc.baseSetKey

  final def use: Use = arc.underlyingArc.use

  final def priority: Int = arc.underlyingArc.priority

  final def order: BigDecimal = arc.underlyingArc.order

  // TODO Method underlyingRelationship

  final override def equals(obj: Any): Boolean = obj match {
    case other: TableRelationship =>
      (other.arc == this.arc) &&
        (other.resolvedFrom.resolvedElem == this.resolvedFrom.resolvedElem) &&
        (other.resolvedTo.resolvedElem == this.resolvedTo.resolvedElem)
    case _ => false
  }

  final override def hashCode: Int = (arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem).hashCode

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
    arc: TableBreakdownArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Table],
    resolvedTo: ResolvedLocatorOrResource[_ <: TableBreakdown]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-breakdown")

  def table: Table = resolvedFrom.resolvedElem

  def breakdown: TableBreakdown = resolvedTo.resolvedElem
}

/**
 * A breakdown-tree relationship.
 */
final class BreakdownTreeRelationship(
    arc: BreakdownTreeArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: TableBreakdown],
    resolvedTo: ResolvedLocatorOrResource[_ <: DefinitionNode]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/breakdown-tree")

  def breakdown: TableBreakdown = resolvedFrom.resolvedElem

  def definitionNode: DefinitionNode = resolvedTo.resolvedElem
}

/**
 * A definition-node-subtree relationship.
 */
final class DefinitionNodeSubtreeRelationship(
    arc: DefinitionNodeSubtreeArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: DefinitionNode],
    resolvedTo: ResolvedLocatorOrResource[_ <: DefinitionNode]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/definition-node-subtree")

  def fromNode: DefinitionNode = resolvedFrom.resolvedElem

  def toNode: DefinitionNode = resolvedTo.resolvedElem
}

/**
 * A table-filter relationship.
 *
 * TODO Filter in formula DOM.
 */
final class TableFilterRelationship(
    arc: TableFilterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Table],
    resolvedTo: ResolvedLocatorOrResource[_ <: NonStandardResource]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-filter")

  def table: Table = resolvedFrom.resolvedElem

  def filter: NonStandardResource = resolvedTo.resolvedElem
}

/**
 * A table-parameter relationship.
 *
 * TODO Parameter in formula DOM.
 */
final class TableParameterRelationship(
    arc: TableParameterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Table],
    resolvedTo: ResolvedLocatorOrResource[_ <: NonStandardResource]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/table-parameter")

  def table: Table = resolvedFrom.resolvedElem

  def parameter: NonStandardResource = resolvedTo.resolvedElem
}

/**
 * An aspect-node-filter relationship.
 *
 * TODO Filter in formula DOM.
 */
final class AspectNodeFilterRelationship(
    arc: AspectNodeFilterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AspectNode],
    resolvedTo: ResolvedLocatorOrResource[_ <: NonStandardResource]) extends TableRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2014/aspect-node-filter")

  def aspectNode: AspectNode = resolvedFrom.resolvedElem

  def filter: NonStandardResource = resolvedTo.resolvedElem
}

object TableRelationship {

  /**
   * Optionally builds a `TableRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[TableRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      TableBreakdownRelationship.opt(arc, resolvedFrom, resolvedTo).
        orElse(BreakdownTreeRelationship.opt(arc, resolvedFrom, resolvedTo)).
        orElse(DefinitionNodeSubtreeRelationship.opt(arc, resolvedFrom, resolvedTo)).
        orElse(TableFilterRelationship.opt(arc, resolvedFrom, resolvedTo)).
        orElse(TableParameterRelationship.opt(arc, resolvedFrom, resolvedTo)).
        orElse(AspectNodeFilterRelationship.opt(arc, resolvedFrom, resolvedTo)).
        orElse(None)
    } else {
      None
    }
  }

  /**
   * Lenient method to optionally create a TableRelationship from an underlying tqa.relationship.StandardRelationship.
   */
  def opt(underlyingRelationship: NonStandardRelationship): Option[TableRelationship] = {
    if (!underlyingRelationship.resolvedFrom.isInstanceOf[XLinkResource] ||
      !underlyingRelationship.resolvedTo.isInstanceOf[XLinkResource]) {

      None
    } else {
      val tableArcOption: Option[TableArc] = toOptionalTableArc(underlyingRelationship.arc)

      tableArcOption flatMap { tableArc =>
        val resolvedFrom =
          ResolvedLocatorOrResource.unsafeTransformResource[AnyTaxonomyElem with XLinkResource](
            underlyingRelationship.resolvedFrom,
            { e => toOptionalTableResource(e).getOrElse(e.asInstanceOf[AnyTaxonomyElem with XLinkResource]) })

        val resolvedTo =
          ResolvedLocatorOrResource.unsafeTransformResource[AnyTaxonomyElem with XLinkResource](
            underlyingRelationship.resolvedTo,
            { e => toOptionalTableResource(e).getOrElse(e.asInstanceOf[AnyTaxonomyElem with XLinkResource]) })

        opt(tableArc, resolvedFrom, resolvedTo)
      }
    }
  }

  private def toOptionalTableArc(arc: XLinkArc): Option[TableArc] = {
    arc match {
      case arc: NonStandardArc => TableArc.opt(arc)
      case _                   => None
    }
  }

  private def toOptionalTableResource(taxoElem: AnyTaxonomyElem): Option[TableResource] = {
    taxoElem match {
      case res: NonStandardResource => TableResource.opt(res)
      case _                        => None
    }
  }
}

object TableBreakdownRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `TableBreakdownRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[TableBreakdownRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/table-breakdown", arc: TableBreakdownArc, source: Table, target: TableBreakdown) =>
          Some(new TableBreakdownRelationship(arc, unsafeCastResource(resolvedFrom, classTag[Table]), unsafeCastResource(resolvedTo, classTag[TableBreakdown])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object BreakdownTreeRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `BreakdownTreeRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[BreakdownTreeRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/breakdown-tree", arc: BreakdownTreeArc, source: TableBreakdown, target: DefinitionNode) =>
          Some(new BreakdownTreeRelationship(arc, unsafeCastResource(resolvedFrom, classTag[TableBreakdown]), unsafeCastResource(resolvedTo, classTag[DefinitionNode])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object DefinitionNodeSubtreeRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `DefinitionNodeSubtreeRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[DefinitionNodeSubtreeRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/definition-node-subtree", arc: DefinitionNodeSubtreeArc, source: DefinitionNode, target: DefinitionNode) =>
          Some(new DefinitionNodeSubtreeRelationship(arc, unsafeCastResource(resolvedFrom, classTag[DefinitionNode]), unsafeCastResource(resolvedTo, classTag[DefinitionNode])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object TableFilterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `TableFilterRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[TableFilterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/table-filter", arc: TableFilterArc, source: Table, target: NonStandardResource) =>
          Some(new TableFilterRelationship(arc, unsafeCastResource(resolvedFrom, classTag[Table]), unsafeCastResource(resolvedTo, classTag[NonStandardResource])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object TableParameterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `TableParameterRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[TableParameterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/table-parameter", arc: TableParameterArc, source: Table, target: NonStandardResource) =>
          Some(new TableParameterRelationship(arc, unsafeCastResource(resolvedFrom, classTag[Table]), unsafeCastResource(resolvedTo, classTag[NonStandardResource])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object AspectNodeFilterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `AspectNodeFilterRelationship` from an underlying `TableArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: TableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[AspectNodeFilterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2014/aspect-node-filter", arc: AspectNodeFilterArc, source: AspectNode, target: NonStandardResource) =>
          Some(new AspectNodeFilterRelationship(arc, unsafeCastResource(resolvedFrom, classTag[AspectNode]), unsafeCastResource(resolvedTo, classTag[NonStandardResource])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}
