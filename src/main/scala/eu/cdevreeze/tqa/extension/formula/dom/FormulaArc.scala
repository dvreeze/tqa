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

package eu.cdevreeze.tqa.extension.formula.dom

import java.net.URI

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.xlink.XLinkArc
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import javax.xml.bind.DatatypeConverter

/**
 * XLink arc in a formula link. In other words, a generic XLink arc defined by one of the formula specifications.
 *
 * @author Chris de Vreeze
 */
sealed trait FormulaArc extends tqa.dom.AnyTaxonomyElem with XLinkArc {

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
 * A variable:variableArc.
 */
final class VariableArc(val underlyingArc: tqa.dom.NonStandardArc) extends FormulaArc {
  requireResolvedName(ENames.VariableVariableArcEName)

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
 * A variable:variableFilterArc.
 */
final class VariableFilterArc(val underlyingArc: tqa.dom.NonStandardArc) extends FormulaArc {
  requireResolvedName(ENames.VariableVariableFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def complement: Boolean = {
    DatatypeConverter.parseBoolean(underlyingArc.attribute(ENames.ComplementEName))
  }

  /**
   * Returns the boolean cover attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def cover: Boolean = {
    DatatypeConverter.parseBoolean(underlyingArc.attribute(ENames.CoverEName))
  }
}

/**
 * A variable:variableSetFilterArc.
 */
final class VariableSetFilterArc(val underlyingArc: tqa.dom.NonStandardArc) extends FormulaArc {
  requireResolvedName(ENames.VariableVariableSetFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def complement: Boolean = {
    DatatypeConverter.parseBoolean(underlyingArc.attribute(ENames.ComplementEName))
  }
}

/**
 * Another FormulaArc, with unknown arc name but with a known formula-related arcrole.
 */
final class OtherFormulaArc(val underlyingArc: tqa.dom.NonStandardArc) extends FormulaArc

// Companion objects

object FormulaArc {

  /**
   * Lenient method to optionally create a FormulaArc from an underlying tqa.dom.NonStandardArc.
   *
   * TODO Consistency-assertion-parameter arcs.
   */
  def opt(underlyingArc: tqa.dom.NonStandardArc): Option[FormulaArc] = {
    if (underlyingArc.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      underlyingArc.resolvedName match {
        case ENames.VariableVariableArcEName          => Some(new VariableArc(underlyingArc))
        case ENames.VariableVariableFilterArcEName    => Some(new VariableFilterArc(underlyingArc))
        case ENames.VariableVariableSetFilterArcEName => Some(new VariableSetFilterArc(underlyingArc))
        case _                                        => None
      }
    } else {
      underlyingArc.arcrole match {
        case "http://xbrl.org/arcrole/2008/variable-set-precondition" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2008/consistency-assertion-formula" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2008/assertion-set" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2010/assertion-satisfied-message" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2010/assertion-unsatisfied-message" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2010/instance-variable" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2010/formula-instance" => Some(new OtherFormulaArc(underlyingArc))
        case "http://xbrl.org/arcrole/2016/assertion-unsatisfied-severity" => Some(new OtherFormulaArc(underlyingArc))
        case _ => None
      }
    }
  }
}
