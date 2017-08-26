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
import eu.cdevreeze.tqa.Namespaces
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

/**
 * An ea:existenceAssertion.
 */
final class ExistenceAssertion(underlyingResource: tqa.dom.NonStandardResource) extends VariableSetAssertion(underlyingResource) {
  requireResolvedName(ENames.EaExistenceAssertionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(underlyingResource.attribute(ENames.TestEName), underlyingResource.scope)
  }
}

/**
 * A ca:consistencyAssertion.
 */
final class ConsistencyAssertion(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource with Assertion {
  requireResolvedName(ENames.CaConsistencyAssertionEName)
}

/**
 * A variable:precondition.
 */
final class Precondition(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource {
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
sealed class Parameter(underlyingResource: tqa.dom.NonStandardResource) extends VariableOrParameter(underlyingResource)

/**
 * A variable:factVariable.
 */
final class FactVariable(underlyingResource: tqa.dom.NonStandardResource) extends Variable(underlyingResource) {
  requireResolvedName(ENames.VariableFactVariableEName)
}

/**
 * A variable:generalVariable.
 */
final class GeneralVariable(underlyingResource: tqa.dom.NonStandardResource) extends Variable(underlyingResource) {
  requireResolvedName(ENames.VariableFactVariableEName)
}

/**
 * An instance:instance.
 */
final class Instance(underlyingResource: tqa.dom.NonStandardResource) extends Parameter(underlyingResource) {
  requireResolvedName(ENames.InstancesInstanceEName)
}

/**
 * A variable:function.
 */
final class Function(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.VariableFunctionEName)
}

/**
 * A variable:equalityDefinition.
 */
final class EqualityDefinition(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource {
  requireResolvedName(ENames.VariableEqualityDefinitionEName)
}

/**
 * A filter.
 */
sealed abstract class Filter(val underlyingResource: tqa.dom.NonStandardResource) extends FormulaResource

// Specific filters

/**
 * A concept filter.
 */
sealed abstract class ConceptFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A cf:conceptName filter.
 */
final class ConceptNameFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptNameEName)
}

/**
 * A cf:conceptPeriodType filter.
 */
final class ConceptPeriodTypeFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptPeriodTypeEName)
}

/**
 * A cf:conceptBalance filter.
 */
final class ConceptBalanceFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptBalanceEName)
}

/**
 * A cf:conceptCustomAttribute filter.
 */
final class ConceptCustomAttributeFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptCustomAttributeEName)
}

/**
 * A cf:conceptDataType filter.
 */
final class ConceptDataTypeFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptDataTypeEName)
}

/**
 * A cf:conceptSubstitutionGroup filter.
 */
final class ConceptSubstitutionGroupFilter(underlyingResource: tqa.dom.NonStandardResource) extends ConceptFilter(underlyingResource) {
  requireResolvedName(ENames.CfConceptSubstitutionGroupEName)
}

/**
 * A boolean filter.
 */
sealed abstract class BooleanFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A bf:andFilter filter.
 */
final class AndFilter(underlyingResource: tqa.dom.NonStandardResource) extends BooleanFilter(underlyingResource) {
  requireResolvedName(ENames.BfAndFilterEName)
}

/**
 * A bf:orFilter filter.
 */
final class OrFilter(underlyingResource: tqa.dom.NonStandardResource) extends BooleanFilter(underlyingResource) {
  requireResolvedName(ENames.BfOrFilterEName)
}

/**
 * A dimension filter.
 */
sealed abstract class DimensionFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A df:explicitDimension filter.
 */
final class ExplicitDimensionFilter(underlyingResource: tqa.dom.NonStandardResource) extends DimensionFilter(underlyingResource) {
  requireResolvedName(ENames.DfExplicitDimensionEName)
}

/**
 * A df:typedDimension filter.
 */
final class TypedDimensionFilter(underlyingResource: tqa.dom.NonStandardResource) extends DimensionFilter(underlyingResource) {
  requireResolvedName(ENames.DfTypedDimensionEName)
}

/**
 * An entity filter.
 */
sealed abstract class EntityFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An ef:identifier filter.
 */
final class IdentifierFilter(underlyingResource: tqa.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfIdentifierEName)
}

/**
 * An ef:specificScheme filter.
 */
final class SpecificSchemeFilter(underlyingResource: tqa.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfSpecificSchemeEName)
}

/**
 * An ef:regexpScheme filter.
 */
final class RegexpSchemeFilter(underlyingResource: tqa.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfRegexpSchemeEName)
}

/**
 * An ef:specificIdentifier filter.
 */
final class SpecificIdentifierFilter(underlyingResource: tqa.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfSpecificIdentifierEName)
}

/**
 * An ef:regexpIdentifier filter.
 */
final class RegexpIdentifierFilter(underlyingResource: tqa.dom.NonStandardResource) extends EntityFilter(underlyingResource) {
  requireResolvedName(ENames.EfRegexpIdentifierEName)
}

/**
 * A general filter (gf:general).
 */
final class GeneralFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.GfGeneralEName)
}

/**
 * A match filter.
 */
sealed abstract class MatchFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An mf:matchConcept filter.
 */
final class MatchConceptFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchConceptEName)
}

/**
 * An mf:matchLocation filter.
 */
final class MatchLocationFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchLocationEName)
}

/**
 * An mf:matchUnit filter.
 */
final class MatchUnitFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchUnitEName)
}

/**
 * An mf:matchEntityIdentifier filter.
 */
final class MatchEntityIdentifierFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchEntityIdentifierEName)
}

/**
 * An mf:matchPeriod filter.
 */
final class MatchPeriodFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchPeriodEName)
}

/**
 * An mf:matchSegment filter.
 */
final class MatchSegmentFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchSegmentEName)
}

/**
 * An mf:matchScenario filter.
 */
final class MatchScenarioFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchScenarioEName)
}

/**
 * An mf:matchNonXDTSegment filter.
 */
final class MatchNonXDTSegmentFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchNonXDTSegmentEName)
}

/**
 * An mf:matchNonXDTScenario filter.
 */
final class MatchNonXDTScenarioFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchNonXDTScenarioEName)
}

/**
 * An mf:matchDimension filter.
 */
final class MatchDimensionFilter(underlyingResource: tqa.dom.NonStandardResource) extends MatchFilter(underlyingResource) {
  requireResolvedName(ENames.MfMatchDimensionEName)
}

/**
 * A period aspect filter.
 */
sealed abstract class PeriodAspectFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A pf:period filter.
 */
final class PeriodFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodEName)
}

/**
 * A pf:periodStart filter.
 */
final class PeriodStartFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodStartEName)
}

/**
 * A pf:periodEnd filter.
 */
final class PeriodEndFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodEndEName)
}

/**
 * A pf:periodInstant filter.
 */
final class PeriodInstantFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfPeriodInstantEName)
}

/**
 * A pf:forever filter.
 */
final class ForeverFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfForeverEName)
}

/**
 * A pf:instantDuration filter.
 */
final class InstantDurationFilter(underlyingResource: tqa.dom.NonStandardResource) extends PeriodAspectFilter(underlyingResource) {
  requireResolvedName(ENames.PfInstantDurationEName)
}

/**
 * A relative filter (rf:relativeFilter).
 */
final class RelativeFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.RfRelativeFilterEName)
}

/**
 * A segment scenario filter.
 */
sealed abstract class SegmentScenarioFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An ssf:segment filter.
 */
final class SegmentFilter(underlyingResource: tqa.dom.NonStandardResource) extends SegmentScenarioFilter(underlyingResource) {
  requireResolvedName(ENames.SsfSegmentEName)
}

/**
 * An ssf:scenario filter.
 */
final class ScenarioFilter(underlyingResource: tqa.dom.NonStandardResource) extends SegmentScenarioFilter(underlyingResource) {
  requireResolvedName(ENames.SsfScenarioEName)
}

/**
 * A tuple filter.
 */
sealed abstract class TupleFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A tf:parentFilter filter.
 */
final class ParentFilter(underlyingResource: tqa.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfParentFilterEName)
}

/**
 * A tf:ancestorFilter filter.
 */
final class AncestorFilter(underlyingResource: tqa.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfAncestorFilterEName)
}

/**
 * A tf:siblingFilter filter.
 */
final class SiblingFilter(underlyingResource: tqa.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfSiblingFilterEName)
}

/**
 * A tf:locationFilter filter.
 */
final class LocationFilter(underlyingResource: tqa.dom.NonStandardResource) extends TupleFilter(underlyingResource) {
  requireResolvedName(ENames.TfLocationFilterEName)
}

/**
 * A unit filter.
 */
sealed abstract class UnitFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * An uf:singleMeasure filter.
 */
final class SingleMeasureFilter(underlyingResource: tqa.dom.NonStandardResource) extends UnitFilter(underlyingResource) {
  requireResolvedName(ENames.UfSingleMeasureEName)
}

/**
 * An uf:generalMeasures filter.
 */
final class GeneralMeasuresFilter(underlyingResource: tqa.dom.NonStandardResource) extends UnitFilter(underlyingResource) {
  requireResolvedName(ENames.UfGeneralMeasuresEName)
}

/**
 * A value filter.
 */
sealed abstract class ValueFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource)

/**
 * A vf:nil filter.
 */
final class NilFilter(underlyingResource: tqa.dom.NonStandardResource) extends ValueFilter(underlyingResource) {
  requireResolvedName(ENames.VfNilEName)
}

/**
 * A vf:precision filter.
 */
final class PrecisionFilter(underlyingResource: tqa.dom.NonStandardResource) extends ValueFilter(underlyingResource) {
  requireResolvedName(ENames.VfPrecisionEName)
}

/**
 * An aspect cover filter (acf:aspectCover).
 */
final class AspectCoverFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.AcfAspectCoverEName)
}

/**
 * A concept relation filter (crf:conceptRelation).
 */
final class ConceptRelationFilter(underlyingResource: tqa.dom.NonStandardResource) extends Filter(underlyingResource) {
  requireResolvedName(ENames.CrfConceptRelationEName)
}

// Companion objects

object FormulaResource {

  /**
   * Lenient method to optionally create a FormulaResource from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[FormulaResource] = {
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
      case en if Namespaces.FormulaFilterNamespaces.contains(en.namespaceUriOption.getOrElse("")) =>
        Filter.opt(underlyingResource)
      case _ => None
    }
  }
}

object Filter {

  /**
   * Lenient method to optionally create a Filter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[Filter] = {
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
   * Lenient method to optionally create a ConceptFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[ConceptFilter] = {
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
   * Lenient method to optionally create a BooleanFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[BooleanFilter] = {
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
   * Lenient method to optionally create a DimensionFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[DimensionFilter] = {
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
   * Lenient method to optionally create an EntityFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[EntityFilter] = {
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
   * Lenient method to optionally create a GeneralFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[GeneralFilter] = {
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
   * Lenient method to optionally create a MatchFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[MatchFilter] = {
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
   * Lenient method to optionally create a PeriodAspectFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[PeriodAspectFilter] = {
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
   * Lenient method to optionally create a RelativeFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[RelativeFilter] = {
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
   * Lenient method to optionally create a SegmentScenarioFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[SegmentScenarioFilter] = {
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
   * Lenient method to optionally create a TupleFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[TupleFilter] = {
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
   * Lenient method to optionally create a UnitFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[UnitFilter] = {
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
   * Lenient method to optionally create a ValueFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[ValueFilter] = {
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
   * Lenient method to optionally create a AspectCoverFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[AspectCoverFilter] = {
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
   * Lenient method to optionally create a ConceptRelationFilter from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[ConceptRelationFilter] = {
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
