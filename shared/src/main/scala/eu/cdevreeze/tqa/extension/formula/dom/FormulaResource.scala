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
import eu.cdevreeze.tqa.StringExpr
import eu.cdevreeze.tqa.StringValue
import eu.cdevreeze.tqa.StringValueOrExpr
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.aspect.AspectModel
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingNodes

/**
 * XLink resource in a formula link. In other words, a variable:resource. See variable.xsd.
 *
 * @author Chris de Vreeze
 */
sealed trait FormulaResource extends FormulaOrTableResource {

  def underlyingResource: tqa.base.dom.NonStandardResource

  final def backingElem: BackingNodes.Elem = underlyingResource.backingElem

  final def docUri: URI = underlyingResource.docUri

  final def xlinkType: String = underlyingResource.xlinkType

  final def xlinkAttributes: Map[EName, String] = underlyingResource.xlinkAttributes

  final def elr: String = underlyingResource.elr

  final def underlyingParentElem: BackingNodes.Elem = underlyingResource.backingElem.parent

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

    underlyingResource.findAllChildElemsOfType(classTag[tqa.base.dom.OtherNonXLinkElem]).
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
sealed abstract class VariableSet(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {

  /**
   * Returns the mandatory implicitFiltering attribute as boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def implicitFiltering: Boolean = {
    val attrValue = underlyingResource.attribute(ENames.ImplicitFilteringEName)
    XsdBooleans.parseBoolean(attrValue)
  }

  /**
   * Returns the mandatory aspectModel attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def aspectModel: AspectModel = {
    val aspectModelString = underlyingResource.attribute(ENames.AspectModelEName)

    aspectModelString match {
      case "dimensional" => AspectModel.DimensionalAspectModel
      case _             => AspectModel.NonDimensionalAspectModel
    }
  }
}

/**
 * A variable or parameter. See variable.xsd.
 */
sealed abstract class VariableOrParameter(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource

/**
 * A variable. See variable.xsd.
 */
sealed abstract class Variable(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableOrParameter(underlyingResource)

/**
 * An assertion. Either in substitution group validation:assertion or validation:variableSetAssertion. See validation.xsd.
 */
sealed trait Assertion extends FormulaResource

/**
 * A validation:assertionSet.
 */
final class AssertionSet(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.ValidationAssertionSetEName)
}

/**
 * A variable set assertion. See validation.xsd.
 */
sealed abstract class VariableSetAssertion(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableSet(underlyingResource) with Assertion

/**
 * A va:valueAssertion.
 */
final class ValueAssertion(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableSetAssertion(underlyingResource) {
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
final class Formula(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableSet(underlyingResource) {
  requireResolvedName(ENames.FormulaFormulaEName)

  def precisionElemOption: Option[PrecisionElem] = {
    findAllNonXLinkChildElemsOfType(classTag[PrecisionElem]).headOption
  }

  def decimalsElemOption: Option[DecimalsElem] = {
    findAllNonXLinkChildElemsOfType(classTag[DecimalsElem]).headOption
  }

  def formulaAspectsElems: immutable.IndexedSeq[FormulaAspectsElem] = {
    findAllNonXLinkChildElemsOfType(classTag[FormulaAspectsElem])
  }

  /**
   * Returns the mandatory value attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.ValueEName), underlyingResource.scope)
  }

  /**
   * Returns the source attribute as optional EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    val qnameOption = underlyingResource.attributeAsQNameOption(ENames.SourceEName)
    qnameOption.map(qn => underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get)
  }
}

/**
 * An ea:existenceAssertion.
 */
final class ExistenceAssertion(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableSetAssertion(underlyingResource) {
  requireResolvedName(ENames.EaExistenceAssertionEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TestEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A ca:consistencyAssertion.
 */
final class ConsistencyAssertion(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource with Assertion {
  requireResolvedName(ENames.CaConsistencyAssertionEName)

  /**
   * Returns the mandatory strict attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def strict: Boolean = {
    XsdBooleans.parseBoolean(underlyingResource.attribute(ENames.StrictEName))
  }

  def absoluteAcceptanceRadiusOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.AbsoluteAcceptanceRadiusEName).
      map(v => ScopedXPathString(v, underlyingResource.scope))
  }

  def proportionalAcceptanceRadiusOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.ProportionalAcceptanceRadiusEName).
      map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A variable:precondition.
 */
final class Precondition(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.VariablePreconditionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A variable:parameter. Not final, because an instance:instance is also a parameter.
 */
sealed class Parameter(underlyingResource: tqa.base.dom.NonStandardResource) extends VariableOrParameter(underlyingResource) {

  /**
   * Returns the mandatory name attribute as EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def name: EName = {
    underlyingResource.attributeAsResolvedQName(ENames.NameEName)
  }

  /**
   * Returns the optional select attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def selectExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.SelectEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }

  /**
   * Returns the optional "required" attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def requiredOption: Option[Boolean] = {
    underlyingResource.attributeOption(ENames.RequiredEName).map(v => XsdBooleans.parseBoolean(v))
  }

  /**
   * Returns the "as" attribute as optional EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def asOption: Option[EName] = {
    underlyingResource.attributeAsResolvedQNameOption(ENames.AsEName)
  }
}

/**
 * A variable:factVariable.
 */
final class FactVariable(underlyingResource: tqa.base.dom.NonStandardResource) extends Variable(underlyingResource) {
  requireResolvedName(ENames.VariableFactVariableEName)

  /**
   * Returns the optional nils attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nilsOption: Option[Boolean] = {
    underlyingResource.attributeOption(ENames.NilsEName).map(v => XsdBooleans.parseBoolean(v))
  }

  /**
   * Returns the optional matches attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def matchesOption: Option[Boolean] = {
    underlyingResource.attributeOption(ENames.MatchesEName).map(v => XsdBooleans.parseBoolean(v))
  }

  /**
   * Returns the optional fallbackValue attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def fallbackValueExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.FallbackValueEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }

  /**
   * Returns the mandatory bindAsSequence attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def bindAsSequence: Boolean = {
    XsdBooleans.parseBoolean(underlyingResource.attribute(ENames.BindAsSequenceEName))
  }
}

/**
 * A variable:generalVariable.
 */
final class GeneralVariable(underlyingResource: tqa.base.dom.NonStandardResource) extends Variable(underlyingResource) {
  requireResolvedName(ENames.VariableGeneralVariableEName)

  /**
   * Returns the mandatory select attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def selectExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.SelectEName), underlyingResource.scope)
  }

  /**
   * Returns the mandatory bindAsSequence attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def bindAsSequence: Boolean = {
    XsdBooleans.parseBoolean(underlyingResource.attribute(ENames.BindAsSequenceEName))
  }
}

/**
 * An instance:instance.
 */
final class Instance(underlyingResource: tqa.base.dom.NonStandardResource) extends Parameter(underlyingResource) {
  requireResolvedName(ENames.InstancesInstanceEName)
}

/**
 * A variable:function.
 */
final class Function(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.VariableFunctionEName)

  /**
   * Returns the mandatory name attribute as EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def name: EName = {
    underlyingResource.attributeAsResolvedQName(ENames.NameEName)
  }

  def functionInputs: immutable.IndexedSeq[FunctionInput] = {
    findAllNonXLinkChildElemsOfType(classTag[FunctionInput])
  }

  /**
   * Returns the mandatory output attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def output: String = {
    underlyingResource.attribute(ENames.OutputEName)
  }
}

/**
 * A variable:equalityDefinition.
 */
final class EqualityDefinition(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.VariableEqualityDefinitionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A cfi:implementation.
 */
final class FunctionImplementation(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.CfiImplementationEName)

  def inputs: immutable.IndexedSeq[FunctionImplementationInput] = {
    findAllNonXLinkChildElemsOfType(classTag[FunctionImplementationInput])
  }

  def steps: immutable.IndexedSeq[FunctionImplementationStep] = {
    findAllNonXLinkChildElemsOfType(classTag[FunctionImplementationStep])
  }

  def output: FunctionImplementationOutput = {
    findAllNonXLinkChildElemsOfType(classTag[FunctionImplementationOutput]).head
  }
}

/**
 * A msg:message, as used in a formula-related context. Strictly speaking messages are not just related
 * to formulas, but they are introduced here to avoid sub-classing the core DOM type NonStandardResource.
 */
final class Message(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.MsgMessageEName)

  /**
   * Returns the mandatory lang attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def lang: String = {
    underlyingResource.attribute(ENames.XmlLangEName)
  }
}

/**
 * A severity.
 */
sealed abstract class Severity(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource

/**
 * A sev:ok.
 */
final class OkSeverity(underlyingResource: tqa.base.dom.NonStandardResource) extends Severity(underlyingResource) {
  requireResolvedName(ENames.SevOkEName)
}

/**
 * A sev:warning.
 */
final class WarningSeverity(underlyingResource: tqa.base.dom.NonStandardResource) extends Severity(underlyingResource) {
  requireResolvedName(ENames.SevWarningEName)
}

/**
 * A sev:error.
 */
final class ErrorSeverity(underlyingResource: tqa.base.dom.NonStandardResource) extends Severity(underlyingResource) {
  requireResolvedName(ENames.SevErrorEName)
}

/**
 * A filter.
 */
sealed abstract class Filter(val underlyingResource: tqa.base.dom.NonStandardResource) extends FormulaResource

// Specific filters

/**
 * A concept filter.
 */
sealed abstract class ConceptFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A cf:conceptName filter.
 */
final class ConceptNameFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptNameEName)

  def concepts: immutable.IndexedSeq[ConceptFilterConcept] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterConcept])
  }
}

/**
 * A cf:conceptPeriodType filter.
 */
final class ConceptPeriodTypeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptPeriodTypeEName)

  def periodType: String = {
    underlyingResource.attribute(ENames.PeriodTypeEName)
  }
}

/**
 * A cf:conceptBalance filter.
 */
final class ConceptBalanceFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptBalanceEName)

  def balance: String = {
    underlyingResource.attribute(ENames.BalanceEName)
  }
}

/**
 * A cf:conceptCustomAttribute filter.
 */
final class ConceptCustomAttributeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptCustomAttributeEName)

  def customAttribute: ConceptFilterAttribute = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterAttribute]).head
  }

  /**
   * Returns the optional value attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.ValueEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A cf:conceptDataType filter.
 */
final class ConceptDataTypeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptDataTypeEName)

  def conceptDataType: ConceptFilterType = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterType]).head
  }

  def strict: Boolean = {
    XsdBooleans.parseBoolean(underlyingResource.attribute(ENames.StrictEName))
  }
}

/**
 * A cf:conceptSubstitutionGroup filter.
 */
final class ConceptSubstitutionGroupFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptSubstitutionGroupEName)

  def conceptSubstitutionGroup: ConceptFilterSubstitutionGroup = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptFilterSubstitutionGroup]).head
  }

  def strict: Boolean = {
    XsdBooleans.parseBoolean(underlyingResource.attribute(ENames.StrictEName))
  }
}

/**
 * A boolean filter.
 */
sealed abstract class BooleanFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A bf:andFilter filter.
 */
final class AndFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends BooleanFilter(underlyingResource) {
  requireResolvedName(ENames.BfAndFilterEName)
}

/**
 * A bf:orFilter filter.
 */
final class OrFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends BooleanFilter(underlyingResource) {
  requireResolvedName(ENames.BfOrFilterEName)
}

/**
 * A dimension filter.
 */
sealed abstract class DimensionFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {

  final def dimension: DimensionFilterDimension = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterDimension]).head
  }
}

/**
 * A df:explicitDimension filter.
 */
final class ExplicitDimensionFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends DimensionFilter(underlyingResource) {
  requireResolvedName(ENames.DfExplicitDimensionEName)

  def members: immutable.IndexedSeq[DimensionFilterMember] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionFilterMember])
  }
}

/**
 * A df:typedDimension filter.
 */
final class TypedDimensionFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends DimensionFilter(underlyingResource) {
  requireResolvedName(ENames.DfTypedDimensionEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TestEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * An entity filter.
 */
sealed abstract class EntityFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An ef:identifier filter.
 */
final class IdentifierFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfIdentifierEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * An ef:specificScheme filter.
 */
final class SpecificSchemeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfSpecificSchemeEName)

  /**
   * Returns the mandatory scheme attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def schemeExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.SchemeEName), underlyingResource.scope)
  }
}

/**
 * An ef:regexpScheme filter.
 */
final class RegexpSchemeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfRegexpSchemeEName)

  def pattern: String = {
    underlyingResource.attribute(ENames.PatternEName)
  }
}

/**
 * An ef:specificIdentifier filter.
 */
final class SpecificIdentifierFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfSpecificIdentifierEName)

  /**
   * Returns the mandatory scheme attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def schemeExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.SchemeEName), underlyingResource.scope)
  }

  /**
   * Returns the mandatory value attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.ValueEName), underlyingResource.scope)
  }
}

/**
 * An ef:regexpIdentifier filter.
 */
final class RegexpIdentifierFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfRegexpIdentifierEName)

  def pattern: String = {
    underlyingResource.attribute(ENames.PatternEName)
  }
}

/**
 * A general filter (gf:general).
 */
final class GeneralFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.GfGeneralEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TestEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A match filter.
 */
sealed abstract class MatchFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {

  /**
   * Returns the variable attribute, as expanded name.
   */
  final def variable: EName = {
    val qn = underlyingResource.attributeAsQName(ENames.VariableEName)
    underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  final def matchAny: Boolean = {
    underlyingResource.attributeOption(ENames.MatchAnyEName).map(s => XsdBooleans.parseBoolean(s)).getOrElse(false)
  }
}

/**
 * An mf:matchConcept filter.
 */
final class MatchConceptFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchConceptEName)
}

/**
 * An mf:matchLocation filter.
 */
final class MatchLocationFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchLocationEName)
}

/**
 * An mf:matchUnit filter.
 */
final class MatchUnitFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchUnitEName)
}

/**
 * An mf:matchEntityIdentifier filter.
 */
final class MatchEntityIdentifierFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchEntityIdentifierEName)
}

/**
 * An mf:matchPeriod filter.
 */
final class MatchPeriodFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchPeriodEName)
}

/**
 * An mf:matchSegment filter.
 */
final class MatchSegmentFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchSegmentEName)
}

/**
 * An mf:matchScenario filter.
 */
final class MatchScenarioFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchScenarioEName)
}

/**
 * An mf:matchNonXDTSegment filter.
 */
final class MatchNonXDTSegmentFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchNonXDTSegmentEName)
}

/**
 * An mf:matchNonXDTScenario filter.
 */
final class MatchNonXDTScenarioFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchNonXDTScenarioEName)
}

/**
 * An mf:matchDimension filter.
 */
final class MatchDimensionFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchDimensionEName)

  def dimension: EName = {
    underlyingResource.attributeAsResolvedQName(ENames.DimensionEName)
  }
}

/**
 * A period aspect filter.
 */
sealed abstract class PeriodAspectFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A pf:period filter.
 */
final class PeriodFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A pf:periodStart filter.
 */
final class PeriodStartFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodStartEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.DateEName), underlyingResource.scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TimeEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A pf:periodEnd filter.
 */
final class PeriodEndFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodEndEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.DateEName), underlyingResource.scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TimeEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A pf:periodInstant filter.
 */
final class PeriodInstantFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodInstantEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.DateEName), underlyingResource.scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TimeEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * A pf:forever filter.
 */
final class ForeverFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfForeverEName)
}

/**
 * A pf:instantDuration filter.
 */
final class InstantDurationFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfInstantDurationEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = underlyingResource.attributeAsQName(ENames.VariableEName)
    underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  def boundary: String = {
    underlyingResource.attribute(ENames.BoundaryEName)
  }
}

/**
 * A relative filter (rf:relativeFilter).
 */
final class RelativeFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.RfRelativeFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = underlyingResource.attributeAsQName(ENames.VariableEName)
    underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }
}

/**
 * A segment scenario filter.
 */
sealed abstract class SegmentScenarioFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TestEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

/**
 * An ssf:segment filter.
 */
final class SegmentFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends SegmentScenarioFilter(underlyingResource) {
  requireResolvedName(ENames.SsfSegmentEName)
}

/**
 * An ssf:scenario filter.
 */
final class ScenarioFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends SegmentScenarioFilter(underlyingResource) {
  requireResolvedName(ENames.SsfScenarioEName)
}

/**
 * A tuple filter.
 */
sealed abstract class TupleFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A tf:parentFilter filter.
 */
final class ParentFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfParentFilterEName)

  def parent: TupleFilterParent = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterParent]).head
  }
}

/**
 * A tf:ancestorFilter filter.
 */
final class AncestorFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfAncestorFilterEName)

  def ancestor: TupleFilterAncestor = {
    findAllNonXLinkChildElemsOfType(classTag[TupleFilterAncestor]).head
  }
}

/**
 * A tf:siblingFilter filter.
 */
final class SiblingFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfSiblingFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = underlyingResource.attributeAsQName(ENames.VariableEName)
    underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }
}

/**
 * A tf:locationFilter filter.
 */
final class LocationFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfLocationFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = underlyingResource.attributeAsQName(ENames.VariableEName)
    underlyingResource.scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  /**
   * Returns the mandatory location attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def locationExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.LocationEName), underlyingResource.scope)
  }
}

/**
 * A unit filter.
 */
sealed abstract class UnitFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An uf:singleMeasure filter.
 */
final class SingleMeasureFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends UnitFilter(underlyingResource) {
  requireResolvedName(ENames.UfSingleMeasureEName)

  def measure: UnitFilterMeasure = {
    findAllNonXLinkChildElemsOfType(classTag[UnitFilterMeasure]).head
  }
}

/**
 * An uf:generalMeasures filter.
 */
final class GeneralMeasuresFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends UnitFilter(underlyingResource) {
  requireResolvedName(ENames.UfGeneralMeasuresEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A value filter.
 */
sealed abstract class ValueFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A vf:nil filter.
 */
final class NilFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ValueFilter(underlyingResource) {
  requireResolvedName(ENames.VfNilEName)
}

/**
 * A vf:precision filter.
 */
final class PrecisionFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends ValueFilter(underlyingResource) {
  requireResolvedName(ENames.VfPrecisionEName)

  /**
   * Returns the mandatory minimum attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def minimumExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.MinimumEName), underlyingResource.scope)
  }
}

/**
 * An aspect cover filter (acf:aspectCover).
 */
final class AspectCoverFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.AcfAspectCoverEName)

  def aspects: immutable.IndexedSeq[AspectCoverFilterAspect] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterAspect])
  }

  def dimensions: immutable.IndexedSeq[AspectCoverFilterDimension] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterDimension])
  }

  def excludeDimensions: immutable.IndexedSeq[AspectCoverFilterExcludeDimension] = {
    findAllNonXLinkChildElemsOfType(classTag[AspectCoverFilterExcludeDimension])
  }
}

/**
 * A concept relation filter (crf:conceptRelation).
 */
final class ConceptRelationFilter(underlyingResource: tqa.base.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.CrfConceptRelationEName)

  /**
   * Returns `variableOption.orElse(qnameOption).orElse(qnameExpressionOption).get`.
   */
  def source: ConceptRelationFilterContentElem = {
    variableOption.orElse(qnameOption).orElse(qnameExpressionOption).getOrElse(sys.error(s"Missing variable, qname and qnameExpression"))
  }

  def variableOption: Option[ConceptRelationFilterVariable] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterVariable]).headOption
  }

  def qnameOption: Option[ConceptRelationFilterQName] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterQName]).headOption
  }

  def qnameExpressionOption: Option[ConceptRelationFilterQNameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterQNameExpression]).headOption
  }

  /**
   * Returns the source as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def sourceValueOrExpr: ENameValueOrExpr = {
    variableOption.map(_.name).map(v => ENameValue(v)).orElse(
      qnameOption.map(_.qnameValue).map(v => ENameValue(v)).orElse(
        qnameExpressionOption.map(_.expr).map(v => ENameExpr(v)))).get
  }

  /**
   * Returns `linkroleOption.orElse(linkroleExpressionOption).get`.
   */
  def linkroleOrLinkroleExpression: ConceptRelationFilterContentElem = {
    linkroleOption.orElse(linkroleExpressionOption).getOrElse(sys.error(s"Missing linkrole or linkroleExpression"))
  }

  def linkroleOption: Option[ConceptRelationFilterLinkrole] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterLinkrole]).headOption
  }

  def linkroleExpressionOption: Option[ConceptRelationFilterLinkroleExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterLinkroleExpression]).headOption
  }

  /**
   * Returns the linkrole as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def linkroleValueOrExpr: StringValueOrExpr = {
    linkroleOption.map(_.linkrole).map(v => StringValue(v)).orElse(
      linkroleExpressionOption.map(_.expr).map(v => StringExpr(v))).get
  }

  /**
   * Returns `linknameOption.orElse(linknameExpressionOption)`.
   */
  def linknameOrLinknameExpressionOption: Option[ConceptRelationFilterContentElem] = {
    linknameOption.orElse(linknameExpressionOption)
  }

  def linknameOption: Option[ConceptRelationFilterLinkname] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterLinkname]).headOption
  }

  def linknameExpressionOption: Option[ConceptRelationFilterLinknameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterLinknameExpression]).headOption
  }

  /**
   * Returns the linkname as optional ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def linknameValueOrExprOption: Option[ENameValueOrExpr] = {
    linknameOption.map(_.linknameValue).map(v => ENameValue(v)).orElse(
      linknameExpressionOption.map(_.expr).map(v => ENameExpr(v)))
  }

  /**
   * Returns `arcroleOption.orElse(arcroleExpressionOption).get`.
   */
  def arcroleOrArcroleExpression: ConceptRelationFilterContentElem = {
    arcroleOption.orElse(arcroleExpressionOption).getOrElse(sys.error(s"Missing arcrole or arcroleExpression"))
  }

  def arcroleOption: Option[ConceptRelationFilterArcrole] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterArcrole]).headOption
  }

  def arcroleExpressionOption: Option[ConceptRelationFilterArcroleExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterArcroleExpression]).headOption
  }

  /**
   * Returns the arcrole as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def arcroleValueOrExpr: StringValueOrExpr = {
    arcroleOption.map(_.arcrole).map(v => StringValue(v)).orElse(
      arcroleExpressionOption.map(_.expr).map(v => StringExpr(v))).get
  }

  /**
   * Returns `arcnameOption.orElse(arcnameExpressionOption)`.
   */
  def arcnameOrArcnameExpressionOption: Option[ConceptRelationFilterContentElem] = {
    arcnameOption.orElse(arcnameExpressionOption)
  }

  def arcnameOption: Option[ConceptRelationFilterArcname] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterArcname]).headOption
  }

  def arcnameExpressionOption: Option[ConceptRelationFilterArcnameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterArcnameExpression]).headOption
  }

  /**
   * Returns the arcname as optional ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def arcnameValueOrExprOption: Option[ENameValueOrExpr] = {
    arcnameOption.map(_.arcnameValue).map(v => ENameValue(v)).orElse(
      arcnameExpressionOption.map(_.expr).map(v => ENameExpr(v)))
  }

  def axis: ConceptRelationFilterAxis = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterAxis]).head
  }

  def generationsOption: Option[ConceptRelationFilterGenerations] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationFilterGenerations]).headOption
  }

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    underlyingResource.attributeOption(ENames.TestEName).map(v => ScopedXPathString(v, underlyingResource.scope))
  }
}

// Companion objects

object FormulaResource {

  /**
   * Lenient method to optionally create a FormulaResource from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[FormulaResource] = {
    underlyingResource.resolvedName match {
      case ENames.FormulaFormulaEName             => Some(new Formula(underlyingResource))
      case ENames.VaValueAssertionEName           => Some(new ValueAssertion(underlyingResource))
      case ENames.EaExistenceAssertionEName       => Some(new ExistenceAssertion(underlyingResource))
      case ENames.CaConsistencyAssertionEName     => Some(new ConsistencyAssertion(underlyingResource))
      case ENames.ValidationAssertionSetEName     => Some(new AssertionSet(underlyingResource))
      case ENames.VariablePreconditionEName       => Some(new Precondition(underlyingResource))
      case ENames.VariableParameterEName          => Some(new Parameter(underlyingResource))
      case ENames.VariableFactVariableEName       => Some(new FactVariable(underlyingResource))
      case ENames.VariableGeneralVariableEName    => Some(new GeneralVariable(underlyingResource))
      case ENames.VariableFunctionEName           => Some(new Function(underlyingResource))
      case ENames.VariableEqualityDefinitionEName => Some(new EqualityDefinition(underlyingResource))
      case ENames.InstancesInstanceEName          => Some(new Instance(underlyingResource))
      case ENames.MsgMessageEName                 => Some(new Message(underlyingResource))
      case ENames.SevOkEName                      => Some(new OkSeverity(underlyingResource))
      case ENames.SevWarningEName                 => Some(new WarningSeverity(underlyingResource))
      case ENames.SevErrorEName                   => Some(new ErrorSeverity(underlyingResource))
      case ENames.CfiImplementationEName          => Some(new FunctionImplementation(underlyingResource))
      case en if Namespaces.FormulaFilterNamespaces.contains(en.namespaceUriOption.getOrElse("")) =>
        Filter.opt(underlyingResource)
      case _ => None
    }
  }
}

object Filter {

  /**
   * Lenient method to optionally create a Filter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[Filter] = {
    underlyingResource.resolvedName.namespaceUriOption.getOrElse("") match {
      case Namespaces.CfNamespace  => ConceptFilter.opt(underlyingResource)
      case Namespaces.BfNamespace  => BooleanFilter.opt(underlyingResource)
      case Namespaces.DfNamespace  => DimensionFilter.opt(underlyingResource)
      case Namespaces.EfNamespace  => EntityFilter.opt(underlyingResource)
      case Namespaces.GfNamespace  => GeneralFilter.opt(underlyingResource)
      case Namespaces.MfNamespace  => MatchFilter.opt(underlyingResource)
      case Namespaces.PfNamespace  => PeriodAspectFilter.opt(underlyingResource)
      case Namespaces.RfNamespace  => RelativeFilter.opt(underlyingResource)
      case Namespaces.SsfNamespace => SegmentScenarioFilter.opt(underlyingResource)
      case Namespaces.TfNamespace  => TupleFilter.opt(underlyingResource)
      case Namespaces.UfNamespace  => UnitFilter.opt(underlyingResource)
      case Namespaces.VfNamespace  => ValueFilter.opt(underlyingResource)
      case Namespaces.AcfNamespace => AspectCoverFilter.opt(underlyingResource)
      case Namespaces.CrfNamespace => ConceptRelationFilter.opt(underlyingResource)
      case _                       => None
    }
  }
}

object ConceptFilter {

  /**
   * Lenient method to optionally create a ConceptFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[ConceptFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.CfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.CfConceptNameEName              => Some(new ConceptNameFilter(underlyingResource))
        case ENames.CfConceptPeriodTypeEName        => Some(new ConceptPeriodTypeFilter(underlyingResource))
        case ENames.CfConceptBalanceEName           => Some(new ConceptBalanceFilter(underlyingResource))
        case ENames.CfConceptCustomAttributeEName   => Some(new ConceptCustomAttributeFilter(underlyingResource))
        case ENames.CfConceptDataTypeEName          => Some(new ConceptDataTypeFilter(underlyingResource))
        case ENames.CfConceptSubstitutionGroupEName => Some(new ConceptSubstitutionGroupFilter(underlyingResource))
        case _                                      => None
      }
    } else {
      None
    }
  }
}

object BooleanFilter {

  /**
   * Lenient method to optionally create a BooleanFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[BooleanFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.BfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.BfAndFilterEName => Some(new AndFilter(underlyingResource))
        case ENames.BfOrFilterEName  => Some(new OrFilter(underlyingResource))
        case _                       => None
      }
    } else {
      None
    }
  }
}

object DimensionFilter {

  /**
   * Lenient method to optionally create a DimensionFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[DimensionFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.DfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.DfExplicitDimensionEName => Some(new ExplicitDimensionFilter(underlyingResource))
        case ENames.DfTypedDimensionEName    => Some(new TypedDimensionFilter(underlyingResource))
        case _                               => None
      }
    } else {
      None
    }
  }
}

object EntityFilter {

  /**
   * Lenient method to optionally create an EntityFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[EntityFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.EfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.EfIdentifierEName         => Some(new IdentifierFilter(underlyingResource))
        case ENames.EfSpecificSchemeEName     => Some(new SpecificSchemeFilter(underlyingResource))
        case ENames.EfRegexpSchemeEName       => Some(new RegexpSchemeFilter(underlyingResource))
        case ENames.EfSpecificIdentifierEName => Some(new SpecificIdentifierFilter(underlyingResource))
        case ENames.EfRegexpIdentifierEName   => Some(new RegexpIdentifierFilter(underlyingResource))
        case _                                => None
      }
    } else {
      None
    }
  }
}

object GeneralFilter {

  /**
   * Lenient method to optionally create a GeneralFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[GeneralFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.GfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.GfGeneralEName => Some(new GeneralFilter(underlyingResource))
        case _                     => None
      }
    } else {
      None
    }
  }
}

object MatchFilter {

  /**
   * Lenient method to optionally create a MatchFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[MatchFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.MfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.MfMatchConceptEName          => Some(new MatchConceptFilter(underlyingResource))
        case ENames.MfMatchLocationEName         => Some(new MatchLocationFilter(underlyingResource))
        case ENames.MfMatchUnitEName             => Some(new MatchUnitFilter(underlyingResource))
        case ENames.MfMatchEntityIdentifierEName => Some(new MatchEntityIdentifierFilter(underlyingResource))
        case ENames.MfMatchPeriodEName           => Some(new MatchPeriodFilter(underlyingResource))
        case ENames.MfMatchSegmentEName          => Some(new MatchSegmentFilter(underlyingResource))
        case ENames.MfMatchScenarioEName         => Some(new MatchScenarioFilter(underlyingResource))
        case ENames.MfMatchNonXDTSegmentEName    => Some(new MatchNonXDTSegmentFilter(underlyingResource))
        case ENames.MfMatchNonXDTScenarioEName   => Some(new MatchNonXDTScenarioFilter(underlyingResource))
        case ENames.MfMatchDimensionEName        => Some(new MatchDimensionFilter(underlyingResource))
        case _                                   => None
      }
    } else {
      None
    }
  }
}

object PeriodAspectFilter {

  /**
   * Lenient method to optionally create a PeriodAspectFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[PeriodAspectFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.PfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.PfPeriodEName          => Some(new PeriodFilter(underlyingResource))
        case ENames.PfPeriodStartEName     => Some(new PeriodStartFilter(underlyingResource))
        case ENames.PfPeriodEndEName       => Some(new PeriodEndFilter(underlyingResource))
        case ENames.PfPeriodInstantEName   => Some(new PeriodInstantFilter(underlyingResource))
        case ENames.PfForeverEName         => Some(new ForeverFilter(underlyingResource))
        case ENames.PfInstantDurationEName => Some(new InstantDurationFilter(underlyingResource))
        case _                             => None
      }
    } else {
      None
    }
  }
}

object RelativeFilter {

  /**
   * Lenient method to optionally create a RelativeFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[RelativeFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.RfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.RfRelativeFilterEName => Some(new RelativeFilter(underlyingResource))
        case _                            => None
      }
    } else {
      None
    }
  }
}

object SegmentScenarioFilter {

  /**
   * Lenient method to optionally create a SegmentScenarioFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[SegmentScenarioFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.SsfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.SsfSegmentEName  => Some(new SegmentFilter(underlyingResource))
        case ENames.SsfScenarioEName => Some(new ScenarioFilter(underlyingResource))
        case _                       => None
      }
    } else {
      None
    }
  }
}

object TupleFilter {

  /**
   * Lenient method to optionally create a TupleFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[TupleFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.TfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.TfParentFilterEName   => Some(new ParentFilter(underlyingResource))
        case ENames.TfAncestorFilterEName => Some(new AncestorFilter(underlyingResource))
        case ENames.TfSiblingFilterEName  => Some(new SiblingFilter(underlyingResource))
        case ENames.TfLocationFilterEName => Some(new LocationFilter(underlyingResource))
        case _                            => None
      }
    } else {
      None
    }
  }
}

object UnitFilter {

  /**
   * Lenient method to optionally create a UnitFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[UnitFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.UfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.UfSingleMeasureEName   => Some(new SingleMeasureFilter(underlyingResource))
        case ENames.UfGeneralMeasuresEName => Some(new GeneralMeasuresFilter(underlyingResource))
        case _                             => None
      }
    } else {
      None
    }
  }
}

object ValueFilter {

  /**
   * Lenient method to optionally create a ValueFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[ValueFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.VfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.VfNilEName       => Some(new NilFilter(underlyingResource))
        case ENames.VfPrecisionEName => Some(new PrecisionFilter(underlyingResource))
        case _                       => None
      }
    } else {
      None
    }
  }
}

object AspectCoverFilter {

  /**
   * Lenient method to optionally create a AspectCoverFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[AspectCoverFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.AcfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.AcfAspectCoverEName => Some(new AspectCoverFilter(underlyingResource))
        case _                          => None
      }
    } else {
      None
    }
  }
}

object ConceptRelationFilter {

  /**
   * Lenient method to optionally create a ConceptRelationFilter from an underlying tqa.base.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.base.dom.NonStandardResource): Option[ConceptRelationFilter] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.CrfNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.CrfConceptRelationEName => Some(new ConceptRelationFilter(underlyingResource))
        case _                              => None
      }
    } else {
      None
    }
  }
}
