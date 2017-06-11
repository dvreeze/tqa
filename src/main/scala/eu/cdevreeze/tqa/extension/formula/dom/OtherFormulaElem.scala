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
import eu.cdevreeze.tqa.Aspect
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.dom.PeriodType
import eu.cdevreeze.yaidom.core.EName
import javax.xml.bind.DatatypeConverter

/**
 * Non-XLink element in a formula (or table) link and in one of the formula-related namespaces.
 *
 * @author Chris de Vreeze
 */
sealed trait OtherFormulaElem extends tqa.dom.AnyTaxonomyElem {

  def underlyingElem: tqa.dom.OtherElem

  final def key: XmlFragmentKey = underlyingElem.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingElem.resolvedName == ename,
      s"Expected $ename but found ${underlyingElem.resolvedName} in ${underlyingElem.docUri}")
  }

  protected[dom] def filterNonXLinkChildElemsOfType[A <: OtherFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingElem.findAllChildElemsOfType(classTag[tqa.dom.OtherElem]).
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
sealed abstract class FunctionContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A variable:input child element of a variable:function.
 */
final class FunctionInput(underlyingElem: tqa.dom.OtherElem) extends FunctionContentElem(underlyingElem) {

  /**
   * Returns the type attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def inputType: String = underlyingElem.attribute(ENames.TypeEName)
}

/**
 * A descendant element of a concept filter.
 */
sealed abstract class ConceptFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A cf:concept child element of a concept filter.
 */
final class ConceptFilterConcept(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }
}

/**
 * A cf:attribute child element of a concept filter.
 */
final class ConceptFilterAttribute(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }
}

/**
 * A cf:type child element of a concept filter.
 */
final class ConceptFilterType(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }
}

/**
 * A cf:substitutionGroup child element of a concept filter.
 */
final class ConceptFilterSubstitutionGroup(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[ConceptFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterQNameExpression]).headOption
  }
}

/**
 * A cf:qname descendant element of a concept filter.
 */
final class ConceptFilterQName(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

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
final class ConceptFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a tuple filter.
 */
sealed abstract class TupleFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A tf:parent child element of a concept filter.
 */
final class TupleFilterParent(underlyingElem: tqa.dom.OtherElem) extends TupleFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[TupleFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQNameExpression]).headOption
  }
}

/**
 * A tf:ancestor child element of a concept filter.
 */
final class TupleFilterAncestor(underlyingElem: tqa.dom.OtherElem) extends TupleFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[TupleFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterQNameExpression]).headOption
  }
}

/**
 * A cf:qname descendant element of a tuple filter.
 */
final class TupleFilterQName(underlyingElem: tqa.dom.OtherElem) extends TupleFilterContentElem(underlyingElem) {

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A cf:qnameExpression descendant element of a tuple filter.
 */
final class TupleFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends TupleFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a dimension filter.
 */
sealed abstract class DimensionFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A df:dimension child element of a dimension filter.
 */
final class DimensionFilterDimension(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[DimensionFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQNameExpression]).headOption
  }
}

/**
 * A df:member child element of a dimension filter.
 */
final class DimensionFilterMember(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem) {

  def variableElemOption: Option[DimensionFilterVariable] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterVariable]).headOption
  }

  def qnameElemOption: Option[DimensionFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterQNameExpression]).headOption
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
final class DimensionFilterVariable(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem) {

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
final class DimensionFilterLinkrole(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem)

/**
 * A df:arcrole descendant element of a dimension filter.
 */
final class DimensionFilterArcrole(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem)

/**
 * A df:axis descendant element of a dimension filter.
 */
final class DimensionFilterAxis(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem)

/**
 * A df:qname descendant element of a dimension filter.
 */
final class DimensionFilterQName(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem) {

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
final class DimensionFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends DimensionFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a unit filter.
 */
sealed abstract class UnitFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A uf:measure child element of a dimension filter.
 */
final class UnitFilterMeasure(underlyingElem: tqa.dom.OtherElem) extends UnitFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[UnitFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[UnitFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[UnitFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[UnitFilterQNameExpression]).headOption
  }
}

/**
 * A uf:qname descendant element of a dimension filter.
 */
final class UnitFilterQName(underlyingElem: tqa.dom.OtherElem) extends UnitFilterContentElem(underlyingElem) {

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
final class UnitFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends UnitFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of an aspect cover filter.
 */
sealed abstract class AspectCoverFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * An acf:aspect descendant element of a dimension filter.
 */
final class AspectCoverFilterAspect(underlyingElem: tqa.dom.OtherElem) extends AspectCoverFilterContentElem(underlyingElem) {

  // TODO Parse Aspect
  def aspectStringValue: String = underlyingElem.text
}

/**
 * An acf:dimension child element of an aspect cover filter.
 */
final class AspectCoverFilterDimension(underlyingElem: tqa.dom.OtherElem) extends AspectCoverFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQNameExpression]).headOption
  }
}

/**
 * An acf:excludeDimension child element of an aspect cover filter.
 */
final class AspectCoverFilterExcludeDimension(underlyingElem: tqa.dom.OtherElem) extends AspectCoverFilterContentElem(underlyingElem) {

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQName]).headOption
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterQNameExpression]).headOption
  }
}

/**
 * An acf:qname descendant element of a dimension filter.
 */
final class AspectCoverFilterQName(underlyingElem: tqa.dom.OtherElem) extends AspectCoverFilterContentElem(underlyingElem) {

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
final class AspectCoverFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends AspectCoverFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A descendant element of a concept relation filter.
 */
sealed abstract class ConceptRelationFilterContentElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * A crf:axis descendant element of a concept relation filter.
 */
final class ConceptRelationFilterAxis(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  // TODO def axisValue, returning type-safe value
}

/**
 * A crf:generations descendant element of a concept relation filter.
 */
final class ConceptRelationFilterGenerations(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem)

/**
 * A crf:variable descendant element of a concept relation filter.
 */
final class ConceptRelationFilterVariable(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

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
final class ConceptRelationFilterLinkrole(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def linkrole: String = underlyingElem.text
}

/**
 * A crf:linkroleExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinkroleExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:linkname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterLinkname(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

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
final class ConceptRelationFilterLinknameExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:arcrole descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcrole(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def arcrole: String = underlyingElem.text
}

/**
 * A crf:arcroleExpression descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcroleExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:arcname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterArcname(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

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
final class ConceptRelationFilterArcnameExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A crf:qname descendant element of a concept relation filter.
 */
final class ConceptRelationFilterQName(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

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
final class ConceptRelationFilterQNameExpression(underlyingElem: tqa.dom.OtherElem) extends ConceptRelationFilterContentElem(underlyingElem) {

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * An aspect or aspects element.
 */
sealed abstract class FormulaAspectOrAspectsElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem

/**
 * An aspects element.
 */
final class FormulaAspectsElem(underlyingElem: tqa.dom.OtherElem) extends FormulaAspectOrAspectsElem(underlyingElem) {

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
sealed abstract class FormulaAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspectOrAspectsElem(underlyingElem) {

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
   * Returns the aspect value.
   */
  def aspect: Aspect
}

/**
 * A formula:concept.
 */
final class ConceptAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaConceptEName)

  def aspect: Aspect = Aspect.ConceptAspect

  def qnameElemOption: Option[QNameElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameElem]).headOption
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameExpressionElem]).headOption
  }
}

/**
 * A formula:entityIdentifier.
 */
final class EntityIdentifierAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaEntityIdentifierEName)

  def aspect: Aspect = Aspect.EntityIdentifierAspect

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
final class PeriodAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaPeriodEName)

  def aspect: Aspect = Aspect.PeriodAspect

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
final class UnitAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaUnitEName)

  def aspect: Aspect = Aspect.UnitAspect

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
    underlyingElem.attributeOption(ENames.AugmentEName).map(v => DatatypeConverter.parseBoolean(v))
  }
}

/**
 * An OCC aspect.
 */
sealed abstract class OccAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {

  final def aspect: Aspect.OccAspect = occ match {
    case Occ.Segment  => Aspect.SegmentOccAspect
    case Occ.Scenario => Aspect.ScenarioOccAspect
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
final class OccEmptyAspect(underlyingElem: tqa.dom.OtherElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccEmptyEName)
}

/**
 * A formula:occFragments.
 */
final class OccFragmentsAspect(underlyingElem: tqa.dom.OtherElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccFragmentsEName)
}

/**
 * A formula:occXpath.
 */
final class OccXpathAspect(underlyingElem: tqa.dom.OtherElem) extends OccAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaOccXpathEName)

  def selectExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.SelectEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }
}

/**
 * A dimension aspect.
 */
sealed abstract class DimensionAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {

  final def aspect: Aspect.DimensionAspect = Aspect.DimensionAspect(dimension)

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
final class ExplicitDimensionAspect(underlyingElem: tqa.dom.OtherElem) extends DimensionAspect(underlyingElem) {
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
final class TypedDimensionAspect(underlyingElem: tqa.dom.OtherElem) extends DimensionAspect(underlyingElem) {
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
final class QNameElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
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
final class QNameExpressionElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaQNameExpressionEName)

  def qnameExpr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A child element of a PeriodAspect.
 */
sealed abstract class PeriodElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {

  def periodType: PeriodType
}

/**
 * A formula:forever.
 */
final class ForeverElem(underlyingElem: tqa.dom.OtherElem) extends PeriodElem(underlyingElem) {
  requireResolvedName(ENames.FormulaForeverEName)

  def periodType: PeriodType = PeriodType.Duration
}

/**
 * A formula:instant.
 */
final class InstantElem(underlyingElem: tqa.dom.OtherElem) extends PeriodElem(underlyingElem) {
  requireResolvedName(ENames.FormulaInstantEName)

  def valueExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.ValueEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def periodType: PeriodType = PeriodType.Instant
}

/**
 * A formula:duration.
 */
final class DurationElem(underlyingElem: tqa.dom.OtherElem) extends PeriodElem(underlyingElem) {
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
final class MultiplyByElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
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
final class DivideByElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
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
final class MemberElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaMemberEName)

  def qnameElemOption: Option[QNameElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameElem]).headOption
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findAllNonXLinkChildElemsOfType(classTag[QNameExpressionElem]).headOption
  }
}

/**
 * A formula:omit.
 */
final class OmitElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaOmitEName)
}

/**
 * A formula:xpath.
 */
final class XpathElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaXpathEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A formula:value.
 */
final class ValueElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaValueEName)
}

// Companion objects

object OtherFormulaElem {

  /**
   * Lenient method to optionally create an OtherFormulaElem from an underlying tqa.dom.OtherElem.
   */
  def opt(underlyingElem: tqa.dom.OtherElem): Option[OtherFormulaElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.FormulaNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.FormulaConceptEName           => Some(new ConceptAspect(underlyingElem))
        case ENames.FormulaEntityIdentifierEName  => Some(new EntityIdentifierAspect(underlyingElem))
        case ENames.FormulaPeriodEName            => Some(new PeriodAspect(underlyingElem))
        case ENames.FormulaUnitEName              => Some(new UnitAspect(underlyingElem))
        case ENames.FormulaOccEmptyEName          => Some(new OccEmptyAspect(underlyingElem))
        case ENames.FormulaOccFragmentsEName      => Some(new OccFragmentsAspect(underlyingElem))
        case ENames.FormulaOccXpathEName          => Some(new OccXpathAspect(underlyingElem))
        case ENames.FormulaExplicitDimensionEName => Some(new ExplicitDimensionAspect(underlyingElem))
        case ENames.FormulaTypedDimensionEName    => Some(new TypedDimensionAspect(underlyingElem))
        case ENames.FormulaQNameEName             => Some(new QNameElem(underlyingElem))
        case ENames.FormulaQNameExpressionEName   => Some(new QNameExpressionElem(underlyingElem))
        case ENames.FormulaForeverEName           => Some(new ForeverElem(underlyingElem))
        case ENames.FormulaInstantEName           => Some(new InstantElem(underlyingElem))
        case ENames.FormulaDurationEName          => Some(new DurationElem(underlyingElem))
        case ENames.FormulaMultiplyByEName        => Some(new MultiplyByElem(underlyingElem))
        case ENames.FormulaDivideByEName          => Some(new DivideByElem(underlyingElem))
        case ENames.FormulaMemberEName            => Some(new MemberElem(underlyingElem))
        case ENames.FormulaOmitEName              => Some(new OmitElem(underlyingElem))
        case ENames.FormulaXpathEName             => Some(new XpathElem(underlyingElem))
        case ENames.FormulaValueEName             => Some(new ValueElem(underlyingElem))
        case _                                    => None
      }
    } else {
      None
    }
  }
}
