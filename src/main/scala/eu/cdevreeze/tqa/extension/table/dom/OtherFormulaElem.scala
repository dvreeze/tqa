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

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.yaidom.core.EName
import javax.xml.bind.DatatypeConverter

/**
 * Non-XLink element in a table link but in the formula namespace.
 *
 * @author Chris de Vreeze
 */
sealed trait OtherFormulaElem extends OtherTableOrFormulaElem {

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
 * An aspect.
 */
sealed abstract class FormulaAspect(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def sourceOption: Option[EName] = {
    val scope = underlyingElem.scope.withoutDefaultNamespace
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).map(qn => scope.resolveQNameOption(qn).get)
  }
}

/**
 * A formula:concept.
 */
final class ConceptAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaConceptEName)

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

  def foreverElemOption: Option[ForeverElem] = {
    findAllNonXLinkChildElemsOfType(classTag[ForeverElem]).headOption
  }

  def instantElemOption: Option[InstantElem] = {
    findAllNonXLinkChildElemsOfType(classTag[InstantElem]).headOption
  }

  def durationElemOption: Option[DurationElem] = {
    findAllNonXLinkChildElemsOfType(classTag[DurationElem]).headOption
  }
}

/**
 * A formula:unit.
 */
final class UnitAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaUnitEName)

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
 * A formula:forever.
 */
final class ForeverElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaForeverEName)
}

/**
 * A formula:instant.
 */
final class InstantElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaInstantEName)

  def valueExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.ValueEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }
}

/**
 * A formula:duration.
 */
final class DurationElem(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaElem {
  requireResolvedName(ENames.FormulaDurationEName)

  def startExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.StartEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }

  def endExprOption: Option[ScopedXPathString] = {
    underlyingElem.attributeOption(ENames.EndEName).map(v => ScopedXPathString(v, underlyingElem.scope))
  }
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
