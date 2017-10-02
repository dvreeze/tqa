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

package eu.cdevreeze.tqa.extension.table.dom

import java.net.URI

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.extension.table.common.TableAxis
import eu.cdevreeze.tqa.xlink.XLinkArc
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import javax.xml.bind.DatatypeConverter

/**
 * XLink arc in a table link.
 *
 * @author Chris de Vreeze
 */
sealed trait TableArc extends tqa.dom.AnyTaxonomyElem with XLinkArc {

  def underlyingArc: tqa.dom.NonStandardArc

  final def backingElem: BackingElemApi = underlyingArc.backingElem

  final def docUri: URI = underlyingArc.docUri

  final def xlinkType: String = underlyingArc.xlinkType

  final def xlinkAttributes: Map[EName, String] = underlyingArc.xlinkAttributes

  final def elr: String = underlyingArc.elr

  final def underlyingParentElem: BackingElemApi = underlyingArc.backingElem.parent

  final def arcrole: String = underlyingArc.arcrole

  final def from: String = underlyingArc.from

  final def to: String = underlyingArc.to

  final def key: XmlFragmentKey = underlyingArc.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingArc.resolvedName == ename,
      s"Expected $ename but found ${underlyingArc.resolvedName} in ${underlyingArc.docUri}")
  }
}

/**
 * A table:tableBreakdownArc.
 */
final class TableBreakdownArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableTableBreakdownArcEName)

  /**
   * Returns the axis attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def axis: TableAxis = {
    TableAxis.fromString(underlyingArc.attribute(ENames.AxisEName))
  }
}

/**
 * A table:breakdownTreeArc.
 */
final class BreakdownTreeArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableBreakdownTreeArcEName)
}

/**
 * A table:definitionNodeSubtreeArc.
 */
final class DefinitionNodeSubtreeArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableDefinitionNodeSubtreeArcEName)
}

/**
 * A table:tableFilterArc.
 */
final class TableFilterArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableTableFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   *
   * TODO Is the complement attribute mandatory? In that case, why has a TableFilterRelationship a default complement value (false)?
   */
  def complement: Boolean = {
    underlyingArc.attributeOption(ENames.ComplementEName).map(s => DatatypeConverter.parseBoolean(s)).getOrElse(false)
  }
}

/**
 * A table:tableParameterArc.
 */
final class TableParameterArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableTableParameterArcEName)

  /**
   * Returns the name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    val qname = underlyingArc.attributeAsQName(ENames.NameEName)
    underlyingArc.scope.withoutDefaultNamespace.resolveQNameOption(qname).get
  }
}

/**
 * A table:aspectNodeFilterArc.
 */
final class AspectNodeFilterArc(val underlyingArc: tqa.dom.NonStandardArc) extends TableArc {
  requireResolvedName(ENames.TableAspectNodeFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def complement: Boolean = {
    underlyingArc.attributeOption(ENames.ComplementEName).map(s => DatatypeConverter.parseBoolean(s)).getOrElse(false)
  }
}

// Companion objects

object TableArc {

  /**
   * Lenient method to optionally create a TableArc from an underlying tqa.dom.NonStandardArc.
   */
  def opt(underlyingArc: tqa.dom.NonStandardArc): Option[TableArc] = {
    if (underlyingArc.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      underlyingArc.resolvedName match {
        case ENames.TableTableBreakdownArcEName        => Some(new TableBreakdownArc(underlyingArc))
        case ENames.TableBreakdownTreeArcEName         => Some(new BreakdownTreeArc(underlyingArc))
        case ENames.TableDefinitionNodeSubtreeArcEName => Some(new DefinitionNodeSubtreeArc(underlyingArc))
        case ENames.TableTableFilterArcEName           => Some(new TableFilterArc(underlyingArc))
        case ENames.TableTableParameterArcEName        => Some(new TableParameterArc(underlyingArc))
        case ENames.TableAspectNodeFilterArcEName      => Some(new AspectNodeFilterArc(underlyingArc))
        case _                                         => None
      }
    } else {
      None
    }
  }
}
