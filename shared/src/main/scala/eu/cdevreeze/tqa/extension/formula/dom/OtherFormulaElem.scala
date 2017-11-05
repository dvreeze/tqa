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

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENameExpr
import eu.cdevreeze.tqa.ENameValue
import eu.cdevreeze.tqa.ENameValueOrExpr
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.aspect.Aspect
import eu.cdevreeze.tqa.aspect.AspectModel
import eu.cdevreeze.tqa.base.common.PeriodType
import eu.cdevreeze.tqa.extension.formula.common.AspectCoverFilters
import eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters
import eu.cdevreeze.tqa.extension.formula.common.Occ
import eu.cdevreeze.yaidom.core.EName

/**
 * Non-XLink element in a formula (or table) link and in one of the formula-related namespaces.
 *
 * @author Chris de Vreeze
 */
sealed trait OtherFormulaElem extends tqa.base.dom.AnyTaxonomyElem {

  def underlyingElem: tqa.base.dom.OtherNonXLinkElem

  final def key: XmlFragmentKey = underlyingElem.key

  final def docUri: URI = underlyingElem.docUri

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingElem.resolvedName == ename,
      s"Expected $ename but found ${underlyingElem.resolvedName} in ${underlyingElem.docUri}")
  }

  protected[dom] def filterNonXLinkChildElemsOfType[A <: OtherFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingElem.findAllChildElemsOfType(classTag[tqa.base.dom.OtherNonXLinkElem]).
      flatMap(e => OtherFormulaElem.opt(e)) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfType[A <: OtherFormulaElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfType(cls)(_ => true)
  }
}

/**
 * A child element of a variable:function.
 */
sealed abstract class FunctionContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A variable:input child element of a variable:function.
 */
final class FunctionInput(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FunctionContentElem(underlyingElem) {
  requireResolvedName(ENames.VariableInputEName)

  /**
   * Returns the type attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def inputType: String = underlyingElem.attribute(ENames.TypeEName)
}

/**
 * A child element of a cfi:implementation.
 */
sealed abstract class FunctionImplementationContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A cfi:input child element of a cfi:implementation.
 */
final class FunctionImplementationInput(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FunctionImplementationContentElem(underlyingElem) {
  requireResolvedName(ENames.CfiInputEName)

  /**
   * Returns the mandatory name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    val qn = underlyingElem.attributeAsQName(ENames.NameEName)
    underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }
}

/**
 * A cfi:step child element of a cfi:implementation.
 */
final class FunctionImplementationStep(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FunctionImplementationContentElem(underlyingElem) {
  requireResolvedName(ENames.CfiStepEName)

  /**
   * Returns the mandatory name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    val qn = underlyingElem.attributeAsQName(ENames.NameEName)
    underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A cfi:output child element of a cfi:implementation.
 */
final class FunctionImplementationOutput(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FunctionImplementationContentElem(underlyingElem) {
  requireResolvedName(ENames.CfiOutputEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a concept filter.
 */
sealed abstract class ConceptFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A cf:concept child element of a concept filter.
 */
final class ConceptFilterConcept(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfConceptEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A cf:attribute child element of a concept filter.
 */
final class ConceptFilterAttribute(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfAttributeEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A cf:type child element of a concept filter.
 */
final class ConceptFilterType(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfTypeEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A cf:substitutionGroup child element of a concept filter.
 */
final class ConceptFilterSubstitutionGroup(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfSubstitutionGroupEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A cf:qname descendant element of a concept filter.
 */
final class ConceptFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A cf:qnameExpression descendant element of a concept filter.
 */
final class ConceptFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a tuple filter.
 */
sealed abstract class TupleFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A tf:parent child element of a concept filter.
 */
final class TupleFilterParent(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends TupleFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.TfParentEName)

  def qnameElemOption: Option[TupleFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A tf:ancestor child element of a concept filter.
 */
final class TupleFilterAncestor(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends TupleFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.TfAncestorEName)

  def qnameElemOption: Option[TupleFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A tf:qname descendant element of a tuple filter.
 */
final class TupleFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends TupleFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.TfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A tf:qnameExpression descendant element of a tuple filter.
 */
final class TupleFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends TupleFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.TfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a dimension filter.
 */
sealed abstract class DimensionFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A df:dimension child element of a dimension filter.
 */
final class DimensionFilterDimension(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfDimensionEName)

  def qnameElemOption: Option[DimensionFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A df:member child element of a dimension filter.
 */
final class DimensionFilterMember(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfMemberEName)

  def variableElemOption: Option[DimensionFilterVariable] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterVariable]).headOption
  }

  def qnameElemOption: Option[DimensionFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    variableElemOption.map(_.name).map(v => ENameValue(v)).orElse(
      qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
        qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v)))).get
  }

  def linkroleElemOption: Option[DimensionFilterLinkrole] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterLinkrole]).headOption
  }

  def arcroleElemOption: Option[DimensionFilterArcrole] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterArcrole]).headOption
  }

  def axisElemOption: Option[DimensionFilterAxis] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterAxis]).headOption
  }
}

/**
 * A df:variable descendant element of a dimension filter.
 */
final class DimensionFilterVariable(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfVariableEName)

  /**
   * Returns the text as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    val qname = underlyingElem.textAsQName
    underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qname).get
  }
}

/**
 * A df:linkrole descendant element of a dimension filter.
 */
final class DimensionFilterLinkrole(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfLinkroleEName)

  def linkrole: String = underlyingElem.text
}

/**
 * A df:arcrole descendant element of a dimension filter.
 */
final class DimensionFilterArcrole(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfArcroleEName)

  def arcrole: String = underlyingElem.text
}

/**
 * A df:axis descendant element of a dimension filter.
 */
final class DimensionFilterAxis(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfAxisEName)

  def axis: String = underlyingElem.text
}

/**
 * A df:qname descendant element of a dimension filter.
 */
final class DimensionFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A df:qnameExpression descendant element of a dimension filter.
 */
final class DimensionFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.DfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a unit filter.
 */
sealed abstract class UnitFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A uf:measure child element of a dimension filter.
 */
final class UnitFilterMeasure(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends UnitFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.UfMeasureEName)

  def qnameElemOption: Option[UnitFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[UnitFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[UnitFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[UnitFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * A uf:qname descendant element of a dimension filter.
 */
final class UnitFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends UnitFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.UfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A uf:qnameExpression descendant element of a dimension filter.
 */
final class UnitFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends UnitFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.UfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of an aspect cover filter.
 */
sealed abstract class AspectCoverFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * An acf:aspect descendant element of a dimension filter.
 */
final class AspectCoverFilterAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends AspectCoverFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.AcfAspectEName)

  def aspectValue: AspectCoverFilters.Aspect = {
    AspectCoverFilters.Aspect.fromString(underlyingElem.text)
  }
}

/**
 * An acf:dimension child element of an aspect cover filter.
 */
final class AspectCoverFilterDimension(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends AspectCoverFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.AcfDimensionEName)

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * An acf:excludeDimension child element of an aspect cover filter.
 */
final class AspectCoverFilterExcludeDimension(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends AspectCoverFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.AcfExcludeDimensionEName)

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQNameExpression]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.expr).map(v => ENameExpr(v))).get
  }
}

/**
 * An acf:qname descendant element of a dimension filter.
 */
final class AspectCoverFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends AspectCoverFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.AcfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * An acf:qnameExpression descendant element of a dimension filter.
 */
final class AspectCoverFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends AspectCoverFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.AcfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a concept relation filter.
 */
sealed abstract class ConceptRelationFilterContentElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * A crf:axis descendant element of a concept relation filter.
 */
final class ConceptRelationFilterAxis(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfAxisEName)

  def axisValue: ConceptRelationFilters.Axis = {
    ConceptRelationFilters.Axis.fromString(underlyingElem.text)
  }
}

/**
 * A crf:generations descendant element of a concept relation filter.
 */
final class ConceptRelationFilterGenerations(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfGenerationsEName)

  def intValue: Int = {
    underlyingElem.text.toInt
  }
}

/**
 * A crf:variable descendant element of a concept relation filter.
 */
final class ConceptRelationFilterVariable(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfVariableEName)

  /**
   * Returns the text as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    val qname = underlyingElem.textAsQName
    underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qname).get
  }
}

/**
 * A crf:linkrole descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinkrole(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfLinkroleEName)

  def linkrole: String = underlyingElem.text
}

/**
 * A crf:linkroleExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinkroleExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfLinkroleExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:linkname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinkname(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfLinknameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def linknameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A crf:linknameExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinknameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfLinknameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:arcrole descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcrole(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfArcroleEName)

  def arcrole: String = underlyingElem.text
}

/**
 * A crf:arcroleExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcroleExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfArcroleExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:arcname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcname(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfArcnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def arcnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A crf:arcnameExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcnameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfArcnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:qname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterQName(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A crf:qnameExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterQNameExpression(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends ConceptRelationFilterContentElem(underlyingElem) {
  requireResolvedName(ENames.CrfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * An aspect or aspects element.
 */
sealed abstract class FormulaAspectOrAspectsElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem

/**
 * An aspects element.
 */
final class FormulaAspectsElem(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspectOrAspectsElem(underlyingElem) {
  requireResolvedName(ENames.FormulaAspectsEName)

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    val scope = underlyingElem.scope.withoutDefaultNamespace
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).map(qn => scope.resolveQNameOption(qn).get)
  }

  /**
   * Returns the aspects themselves.
   */
  def formulaAspects: immutable.IndexedSeq[FormulaAspect] = {
    findAllNonXLinkChildElemsOfType(classTag[FormulaAspect])
  }
}

/**
 * An aspect.
 */
sealed abstract class FormulaAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspectOrAspectsElem(underlyingElem) {

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def sourceOption: Option[EName] = {
    val scope = underlyingElem.scope.withoutDefaultNamespace
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).map(qn => scope.resolveQNameOption(qn).get)
  }

  /**
   * Returns the aspect value, depending on the aspect model used.
   */
  def aspect(aspectModel: AspectModel): Aspect
}

/**
 * A formula:concept.
 */
final class ConceptAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaConceptEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.ConceptAspect

  def qnameElemOption: Option[QNameElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameElem]).headOption
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameExpressionElem]).headOption
  }

  /**
   * Returns the qname as optional ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExprOption: Option[ENameValueOrExpr] = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.qnameExpr).map(v => ENameExpr(v)))
  }
}

/**
 * A formula:entityIdentifier.
 */
final class EntityIdentifierAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaEntityIdentifierEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.EntityIdentifierAspect

  def schemeExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.SchemeEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def valueExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.ValueEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }
}

/**
 * A formula:period.
 */
final class PeriodAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaPeriodEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.PeriodAspect

  def foreverElemOption: Option[ForeverElem] = {
    findAllNonXLinkChildElemsOfType(classTag[ForeverElem]).headOption
  }

  def instantElemOption: Option[InstantElem] = {
    findAllNonXLinkChildElemsOfType(classTag[InstantElem]).headOption
  }

  def durationElemOption: Option[DurationElem] = {
    findAllNonXLinkChildElemsOfType(classTag[DurationElem]).headOption
  }

  def periodElems: immutable.IndexedSeq[PeriodElem] = {
    findAllNonXLinkChildElemsOfType(classTag[PeriodElem])
  }
}

/**
 * A formula:unit.
 */
final class UnitAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaUnitEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.UnitAspect

  def multiplyByElems: immutable.IndexedSeq[MultiplyByElem] = {
    findAllNonXLinkChildElemsOfType(classTag[MultiplyByElem])
  }

  def divideByElems: immutable.IndexedSeq[DivideByElem] = {
    findAllNonXLinkChildElemsOfType(classTag[DivideByElem])
  }

  /**
   * Returns the optional boolean augment attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def augmentOption: Option[Boolean] = {
    underlyingElem.attributeOption(ENames.AugmentEName).map(v => XsdBooleans.parseBoolean(v))
  }
}

/**
 * An OCC aspect.
 */
sealed abstract class OccAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {

  final def aspect(aspectModel: AspectModel): Aspect.OccAspect = (occ, aspectModel) match {
    case (Occ.Segment, AspectModel.DimensionalAspectModel)     => Aspect.NonXDTSegmentAspect
    case (Occ.Segment, AspectModel.NonDimensionalAspectModel)  => Aspect.CompleteSegmentAspect
    case (Occ.Scenario, AspectModel.DimensionalAspectModel)    => Aspect.NonXDTScenarioAspect
    case (Occ.Scenario, AspectModel.NonDimensionalAspectModel) => Aspect.CompleteScenarioAspect
  }

  /**
   * Returns the occ attribute as Occ. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def occ: Occ = {
    Occ.fromString(underlyingElem.attribute(ENames.OccEName))
  }
}

/**
 * A formula:occEmpty.
 */
final class OccEmptyAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccEmptyEName)
}

/**
 * A formula:occFragments.
 */
final class OccFragmentsAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccFragmentsEName)
}

/**
 * A formula:occXpath.
 */
final class OccXpathAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccXpathEName)

  def selectExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.SelectEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }
}

/**
 * A dimension aspect.
 */
sealed abstract class DimensionAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends FormulaAspect(underlyingElem) {

  final def aspect(aspectModel: AspectModel): Aspect.DimensionAspect = {
    require(
      aspectModel == AspectModel.DimensionalAspectModel,
      s"Only the dimensional aspect model supports dimension aspects")

    Aspect.DimensionAspect(dimension)
  }

  /**
   * Returns the dimension attribute as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def dimension: EName = {
    underlyingElem.attributeAsResolvedQName(ENames.DimensionEName)
  }
}

/**
 * A formula:explicitDimension.
 */
final class ExplicitDimensionAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaExplicitDimensionEName)

  def memberElemOption: Option[MemberElem] = {
    findAllNonXLinkChildElemsOfType(classTag[MemberElem]).headOption
  }

  def omitElemOption: Option[OmitElem] = {
    findAllNonXLinkChildElemsOfType(classTag[OmitElem]).headOption
  }
}

/**
 * A formula:typedDimension.
 */
final class TypedDimensionAspect(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends DimensionAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaTypedDimensionEName)

  def xpathElemOption: Option[XpathElem] = {
    findAllNonXLinkChildElemsOfType(classTag[XpathElem]).headOption
  }

  def valueElemOption: Option[ValueElem] = {
    findAllNonXLinkChildElemsOfType(classTag[ValueElem]).headOption
  }

  def omitElemOption: Option[OmitElem] = {
    findAllNonXLinkChildElemsOfType(classTag[OmitElem]).headOption
  }
}

/**
 * A formula:qname.
 */
final class QNameElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaQNameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A formula:qnameExpression.
 */
final class QNameExpressionElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaQNameExpressionEName)

  def qnameExpr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A child element of a PeriodAspect.
 */
sealed abstract class PeriodElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {

  def periodType: PeriodType
}

/**
 * A formula:forever.
 */
final class ForeverElem(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends PeriodElem(underlyingElem) {
  requireResolvedName(ENames.FormulaForeverEName)

  def periodType: PeriodType = PeriodType.Duration
}

/**
 * A formula:instant.
 */
final class InstantElem(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends PeriodElem(underlyingElem) {
  requireResolvedName(ENames.FormulaInstantEName)

  def valueExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.ValueEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def periodType: PeriodType = PeriodType.Instant
}

/**
 * A formula:duration.
 */
final class DurationElem(underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends PeriodElem(underlyingElem) {
  requireResolvedName(ENames.FormulaDurationEName)

  def startExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.StartEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def endExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.EndEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def periodType: PeriodType = PeriodType.Duration
}

/**
 * A formula:multiplyBy.
 */
final class MultiplyByElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaMultiplyByEName)

  def measureExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.MeasureEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).
      map(qn => underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qn).get)
  }
}

/**
 * A formula:divideBy.
 */
final class DivideByElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaDivideByEName)

  def measureExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.MeasureEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).
      map(qn => underlyingElem.scope.withoutDefaultNamespace.resolveQNameOption(qn).get)
  }
}

/**
 * A formula:member.
 */
final class MemberElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaMemberEName)

  def qnameElemOption: Option[QNameElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameElem]).headOption
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameExpressionElem]).headOption
  }

  /**
   * Returns the qname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: ENameValueOrExpr = {
    qnameElemOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
      qnameExpressionElemOption.map(_.qnameExpr).map(v => ENameExpr(v))).get
  }
}

/**
 * A formula:omit.
 */
final class OmitElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaOmitEName)
}

/**
 * A formula:xpath.
 */
final class XpathElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaXpathEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A formula:value.
 */
final class ValueElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaValueEName)
}

/**
 * A formula:precision.
 */
final class PrecisionElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaPrecisionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A formula:decimals.
 */
final class DecimalsElem(val underlyingElem: tqa.base.dom.OtherNonXLinkElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaDecimalsEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

// Companion objects

object OtherFormulaElem {

  /**
   * Lenient method to optionally create an OtherFormulaElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[OtherFormulaElem] = {
    underlyingElem.resolvedName.namespaceUriOption.getOrElse("") match {
      case Namespaces.FormulaNamespace =>
        underlyingElem.resolvedName match {
          case ENames.FormulaQNameEName           => Some(new QNameElem(underlyingElem))
          case ENames.FormulaQNameExpressionEName => Some(new QNameExpressionElem(underlyingElem))
          case ENames.FormulaForeverEName         => Some(new ForeverElem(underlyingElem))
          case ENames.FormulaInstantEName         => Some(new InstantElem(underlyingElem))
          case ENames.FormulaDurationEName        => Some(new DurationElem(underlyingElem))
          case ENames.FormulaMultiplyByEName      => Some(new MultiplyByElem(underlyingElem))
          case ENames.FormulaDivideByEName        => Some(new DivideByElem(underlyingElem))
          case ENames.FormulaMemberEName          => Some(new MemberElem(underlyingElem))
          case ENames.FormulaOmitEName            => Some(new OmitElem(underlyingElem))
          case ENames.FormulaXpathEName           => Some(new XpathElem(underlyingElem))
          case ENames.FormulaValueEName           => Some(new ValueElem(underlyingElem))
          case ENames.FormulaPrecisionEName       => Some(new PrecisionElem(underlyingElem))
          case ENames.FormulaDecimalsEName        => Some(new DecimalsElem(underlyingElem))
          case _                                  => FormulaAspectOrAspectsElem.opt(underlyingElem)
        }
      case Namespaces.VariableNamespace =>
        underlyingElem.resolvedName match {
          case ENames.VariableInputEName => Some(new FunctionInput(underlyingElem))
          case _                         => None
        }
      case Namespaces.CfNamespace =>
        ConceptFilterContentElem.opt(underlyingElem)
      case Namespaces.TfNamespace =>
        TupleFilterContentElem.opt(underlyingElem)
      case Namespaces.DfNamespace =>
        DimensionFilterContentElem.opt(underlyingElem)
      case Namespaces.UfNamespace =>
        UnitFilterContentElem.opt(underlyingElem)
      case Namespaces.AcfNamespace =>
        AspectCoverFilterContentElem.opt(underlyingElem)
      case Namespaces.CrfNamespace =>
        ConceptRelationFilterContentElem.opt(underlyingElem)
      case Namespaces.CfiNamespace =>
        underlyingElem.resolvedName match {
          case ENames.CfiInputEName  => Some(new FunctionImplementationInput(underlyingElem))
          case ENames.CfiStepEName   => Some(new FunctionImplementationStep(underlyingElem))
          case ENames.CfiOutputEName => Some(new FunctionImplementationOutput(underlyingElem))
          case _                     => None
        }
      case _ =>
        None
    }
  }
}

object FormulaAspectOrAspectsElem {

  /**
   * Lenient method to optionally create a FormulaAspectOrAspectsElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[FormulaAspectOrAspectsElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.FormulaNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.FormulaAspectsEName           => Some(new FormulaAspectsElem(underlyingElem))
        case ENames.FormulaConceptEName           => Some(new ConceptAspect(underlyingElem))
        case ENames.FormulaEntityIdentifierEName  => Some(new EntityIdentifierAspect(underlyingElem))
        case ENames.FormulaPeriodEName            => Some(new PeriodAspect(underlyingElem))
        case ENames.FormulaUnitEName              => Some(new UnitAspect(underlyingElem))
        case ENames.FormulaOccEmptyEName          => Some(new OccEmptyAspect(underlyingElem))
        case ENames.FormulaOccFragmentsEName      => Some(new OccFragmentsAspect(underlyingElem))
        case ENames.FormulaOccXpathEName          => Some(new OccXpathAspect(underlyingElem))
        case ENames.FormulaExplicitDimensionEName => Some(new ExplicitDimensionAspect(underlyingElem))
        case ENames.FormulaTypedDimensionEName    => Some(new TypedDimensionAspect(underlyingElem))
        case _                                    => None
      }
    } else {
      None
    }
  }
}

object ConceptFilterContentElem {

  /**
   * Lenient method to optionally create a ConceptFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[ConceptFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.CfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.CfConceptEName           => Some(new ConceptFilterConcept(underlyingElem))
        case ENames.CfAttributeEName         => Some(new ConceptFilterAttribute(underlyingElem))
        case ENames.CfTypeEName              => Some(new ConceptFilterType(underlyingElem))
        case ENames.CfSubstitutionGroupEName => Some(new ConceptFilterSubstitutionGroup(underlyingElem))
        case ENames.CfQnameEName             => Some(new ConceptFilterQName(underlyingElem))
        case ENames.CfQnameExpressionEName   => Some(new ConceptFilterQNameExpression(underlyingElem))
        case _                               => None
      }
    } else {
      None
    }
  }
}

object TupleFilterContentElem {

  /**
   * Lenient method to optionally create a TupleFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[TupleFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.TfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.TfParentEName          => Some(new TupleFilterParent(underlyingElem))
        case ENames.TfAncestorEName        => Some(new TupleFilterAncestor(underlyingElem))
        case ENames.TfQnameEName           => Some(new TupleFilterQName(underlyingElem))
        case ENames.TfQnameExpressionEName => Some(new TupleFilterQNameExpression(underlyingElem))
        case _                             => None
      }
    } else {
      None
    }
  }
}

object DimensionFilterContentElem {

  /**
   * Lenient method to optionally create a DimensionFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[DimensionFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.DfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.DfDimensionEName       => Some(new DimensionFilterDimension(underlyingElem))
        case ENames.DfMemberEName          => Some(new DimensionFilterMember(underlyingElem))
        case ENames.DfLinkroleEName        => Some(new DimensionFilterLinkrole(underlyingElem))
        case ENames.DfArcroleEName         => Some(new DimensionFilterArcrole(underlyingElem))
        case ENames.DfAxisEName            => Some(new DimensionFilterAxis(underlyingElem))
        case ENames.DfVariableEName        => Some(new DimensionFilterVariable(underlyingElem))
        case ENames.DfQnameEName           => Some(new DimensionFilterQName(underlyingElem))
        case ENames.DfQnameExpressionEName => Some(new DimensionFilterQNameExpression(underlyingElem))
        case _                             => None
      }
    } else {
      None
    }
  }
}

object UnitFilterContentElem {

  /**
   * Lenient method to optionally create a UnitFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[UnitFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.UfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.UfMeasureEName         => Some(new UnitFilterMeasure(underlyingElem))
        case ENames.UfQnameEName           => Some(new UnitFilterQName(underlyingElem))
        case ENames.UfQnameExpressionEName => Some(new UnitFilterQNameExpression(underlyingElem))
        case _                             => None
      }
    } else {
      None
    }
  }
}

object AspectCoverFilterContentElem {

  /**
   * Lenient method to optionally create a AspectCoverFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[AspectCoverFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.AcfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.AcfAspectEName           => Some(new AspectCoverFilterAspect(underlyingElem))
        case ENames.AcfDimensionEName        => Some(new AspectCoverFilterDimension(underlyingElem))
        case ENames.AcfExcludeDimensionEName => Some(new AspectCoverFilterExcludeDimension(underlyingElem))
        case ENames.AcfQnameEName            => Some(new AspectCoverFilterQName(underlyingElem))
        case ENames.AcfQnameExpressionEName  => Some(new AspectCoverFilterQNameExpression(underlyingElem))
        case _                               => None
      }
    } else {
      None
    }
  }
}

object ConceptRelationFilterContentElem {

  /**
   * Lenient method to optionally create a ConceptRelationFilterContentElem from an underlying tqa.base.dom.OtherNonXLinkElem.
   */
  def opt(underlyingElem: tqa.base.dom.OtherNonXLinkElem): Option[ConceptRelationFilterContentElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.CrfNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.CrfAxisEName               => Some(new ConceptRelationFilterAxis(underlyingElem))
        case ENames.CrfGenerationsEName        => Some(new ConceptRelationFilterGenerations(underlyingElem))
        case ENames.CrfVariableEName           => Some(new ConceptRelationFilterVariable(underlyingElem))
        case ENames.CrfQnameEName              => Some(new ConceptRelationFilterQName(underlyingElem))
        case ENames.CrfQnameExpressionEName    => Some(new ConceptRelationFilterQNameExpression(underlyingElem))
        case ENames.CrfLinkroleEName           => Some(new ConceptRelationFilterLinkrole(underlyingElem))
        case ENames.CrfLinkroleExpressionEName => Some(new ConceptRelationFilterLinkroleExpression(underlyingElem))
        case ENames.CrfLinknameEName           => Some(new ConceptRelationFilterLinkname(underlyingElem))
        case ENames.CrfLinknameExpressionEName => Some(new ConceptRelationFilterLinknameExpression(underlyingElem))
        case ENames.CrfArcroleEName            => Some(new ConceptRelationFilterArcrole(underlyingElem))
        case ENames.CrfArcroleExpressionEName  => Some(new ConceptRelationFilterArcroleExpression(underlyingElem))
        case ENames.CrfArcnameEName            => Some(new ConceptRelationFilterArcname(underlyingElem))
        case ENames.CrfArcnameExpressionEName  => Some(new ConceptRelationFilterArcnameExpression(underlyingElem))
        case _                                 => None
      }
    } else {
      None
    }
  }
}
