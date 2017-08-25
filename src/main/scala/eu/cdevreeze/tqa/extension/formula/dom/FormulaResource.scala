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

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.xlink.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import javax.xml.bind.DatatypeConverter

/**
 * XLink resource in a formula link. In other words, a variable:resource. See variable.xsd.
 *
 * @author Chris de Vreeze
 */
sealed trait FormulaResource extends tqa.dom.AnyTaxonomyElem with XLinkResource {

  def underlyingResource: tqa.dom.NonStandardResource

  final def backingElem: BackingElemApi = underlyingResource.backingElem

  final def xlinkType: String = underlyingResource.xlinkType

  final def xlinkAttributes: Map[EName, String] = underlyingResource.xlinkAttributes

  final def elr: String = underlyingResource.elr

  final def underlyingParentElem: BackingElemApi = underlyingResource.backingElem.parent

  final def xlinkLabel: String = underlyingResource.xlinkLabel

  final def roleOption: Option[String] = underlyingResource.roleOption

  final def key: XmlFragmentKey = underlyingResource.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingResource.resolvedName == ename,
      s"Expected $ename but found ${underlyingResource.resolvedName} in ${underlyingResource.docUri}")
  }

  protected[dom] def filterNonXLinkChildElemsOfType[A <: OtherFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingResource.findAllChildElemsOfType(classTag[tqa.dom.OtherElem]).
      flatMap(e => OtherFormulaElem.opt(e)) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfType[A <: OtherFormulaElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfType(cls)(_ => true)
  }
}

/**
 * A variable set. See variable.xsd.
 */
sealed abstract class VariableSet(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource {

  /**
   * Returns the mandatory implicitFiltering attribute as boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def implicitFiltering: Boolean = {
    val attrValue = underlyingResource.attribute(ENames.ImplicitFilteringEName)
    DatatypeConverter.parseBoolean(attrValue)
  }

  /**
   * Returns the mandatory aspectModel attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def aspectModel: String = underlyingResource.attribute(ENames.AspectModelEName)
}

/**
 * A variable or parameter. See variable.xsd.
 */
sealed abstract class VariableOrParameter(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource

/**
 * A variable. See variable.xsd.
 */
sealed abstract class Variable(underlyingResource: tqa.dom.NonStandardResource) extends VariableOrParameter(underlyingResource)

/**
 * An assertion. Either in substitution group validation:assertion or validation:variableSetAssertion. See validation.xsd.
 */
sealed trait Assertion extends FormulaResource

/**
 * A validation:assertionSet.
 */
final class AssertionSet(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.ValidationAssertionSetEName)
}

/**
 * A variable set assertion. See validation.xsd.
 */
sealed abstract class VariableSetAssertion(underlyingResource: tqa.dom.NonStandardResource) extends VariableSet(underlyingResource) with Assertion

/**
 * A va:valueAssertion.
 */
final class ValueAssertion(underlyingResource: tqa.dom.NonStandardResource) extends VariableSetAssertion(underlyingResource) {
  requireResolvedName(ENames.VaValueAssertionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A formula:formula.
 */
final class Formula(underlyingResource: tqa.dom.NonStandardResource) extends VariableSet(underlyingResource) {
  requireResolvedName(ENames.FormulaFormulaEName)

  // ...
}

// ...

// Companion objects

object FormulaResource {

  /**
   * Lenient method to optionally create a FormulaResource from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[FormulaResource] = {
    underlyingResource.resolvedName match {
      case ENames.ValidationAssertionSetEName => Some(new AssertionSet(underlyingResource))
      case ENames.VaValueAssertionEName       => Some(new ValueAssertion(underlyingResource))
      case ENames.FormulaFormulaEName         => Some(new Formula(underlyingResource))
      case _                                  => None
    }
  }
}
