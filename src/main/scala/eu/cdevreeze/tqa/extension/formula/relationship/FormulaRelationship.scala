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

package eu.cdevreeze.tqa.extension.formula.relationship

import java.net.URI

import scala.reflect.classTag

import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.common.Use
import eu.cdevreeze.tqa.dom.AnyTaxonomyElem
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.NonStandardArc
import eu.cdevreeze.tqa.dom.NonStandardResource
import eu.cdevreeze.tqa.extension.formula.dom.Assertion
import eu.cdevreeze.tqa.extension.formula.dom.AssertionSet
import eu.cdevreeze.tqa.extension.formula.dom.BooleanFilter
import eu.cdevreeze.tqa.extension.formula.dom.ConsistencyAssertion
import eu.cdevreeze.tqa.extension.formula.dom.FactVariable
import eu.cdevreeze.tqa.extension.formula.dom.Filter
import eu.cdevreeze.tqa.extension.formula.dom.Formula
import eu.cdevreeze.tqa.extension.formula.dom.FormulaArc
import eu.cdevreeze.tqa.extension.formula.dom.FormulaResource
import eu.cdevreeze.tqa.extension.formula.dom.Instance
import eu.cdevreeze.tqa.extension.formula.dom.Message
import eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaArc
import eu.cdevreeze.tqa.extension.formula.dom.Parameter
import eu.cdevreeze.tqa.extension.formula.dom.Precondition
import eu.cdevreeze.tqa.extension.formula.dom.Severity
import eu.cdevreeze.tqa.extension.formula.dom.Variable
import eu.cdevreeze.tqa.extension.formula.dom.VariableArc
import eu.cdevreeze.tqa.extension.formula.dom.VariableFilterArc
import eu.cdevreeze.tqa.extension.formula.dom.VariableOrParameter
import eu.cdevreeze.tqa.extension.formula.dom.VariableSet
import eu.cdevreeze.tqa.extension.formula.dom.VariableSetFilterArc
import eu.cdevreeze.tqa.relationship.NonStandardRelationship
import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource
import eu.cdevreeze.tqa.xlink.XLinkArc
import eu.cdevreeze.tqa.xlink.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path

/**
 * Relationship in a formula link.
 *
 * @author Chris de Vreeze
 */
sealed abstract class FormulaRelationship(
    val arc: FormulaArc,
    val resolvedFrom: ResolvedLocatorOrResource[_ <: FormulaResource],
    val resolvedTo: ResolvedLocatorOrResource[_ <: FormulaResource]) {

  require(arc.from == resolvedFrom.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'from' not matching on label in $docUri")
  require(arc.to == resolvedTo.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'to' not matching on label in $docUri")

  final def validated: FormulaRelationship = {
    require(resolvedFrom.elr == arc.elr, s"Arc and 'from' not in same ELR in $docUri")
    require(resolvedTo.elr == arc.elr, s"Arc and 'to' not in same ELR in $docUri")
    this
  }

  final def sourceElem: FormulaResource = resolvedFrom.resolvedElem

  final def targetElem: FormulaResource = resolvedTo.resolvedElem

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
    case other: FormulaRelationship =>
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
 * A variable-set relationship.
 */
final class VariableSetRelationship(
    arc: VariableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: VariableSet],
    resolvedTo: ResolvedLocatorOrResource[_ <: VariableOrParameter]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/variable-set")

  def variableSet: VariableSet = resolvedFrom.resolvedElem

  def variableOrParameter: VariableOrParameter = resolvedTo.resolvedElem

  /**
   * Returns the name as EName. The default namespace of the arc is not used to resolve the QName.
   */
  def name: EName = arc.name
}

/**
 * A variable-filter relationship.
 */
final class VariableFilterRelationship(
    arc: VariableFilterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: FactVariable],
    resolvedTo: ResolvedLocatorOrResource[_ <: Filter]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/variable-filter")

  def factVariable: FactVariable = resolvedFrom.resolvedElem

  def filter: Filter = resolvedTo.resolvedElem

  /**
   * Returns the boolean complement attribute of the underlying arc.
   */
  def complement: Boolean = arc.complement

  /**
   * Returns the boolean cover attribute off the underlying arc.
   */
  def cover: Boolean = arc.cover
}

/**
 * A variable-set-filter relationship.
 */
final class VariableSetFilterRelationship(
    arc: VariableSetFilterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: VariableSet],
    resolvedTo: ResolvedLocatorOrResource[_ <: Filter]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/variable-set-filter")

  def variableSet: VariableSet = resolvedFrom.resolvedElem

  def filter: Filter = resolvedTo.resolvedElem

  /**
   * Returns the boolean complement attribute of the underlying arc.
   */
  def complement: Boolean = arc.complement
}

/**
 * A variable-filter relationship.
 */
final class BooleanFilterRelationship(
    arc: VariableFilterArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: BooleanFilter],
    resolvedTo: ResolvedLocatorOrResource[_ <: Filter]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/boolean-filter")

  def booleanFilter: BooleanFilter = resolvedFrom.resolvedElem

  def subFilter: Filter = resolvedTo.resolvedElem

  /**
   * Returns the boolean complement attribute of the underlying arc.
   */
  def complement: Boolean = arc.complement

  /**
   * Returns the boolean cover attribute off the underlying arc.
   */
  def cover: Boolean = arc.cover
}

/**
 * A consistency-assertion-parameter relationship. Note that it is backed by a VariableArc.
 */
final class ConsistencyAssertionParameterRelationship(
    arc: VariableArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: ConsistencyAssertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Parameter]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/consistency-assertion-parameter")

  def consistencyAssertion: ConsistencyAssertion = resolvedFrom.resolvedElem

  def parameter: Parameter = resolvedTo.resolvedElem

  /**
   * Returns the name as EName. The default namespace of the arc is not used to resolve the QName.
   */
  def name: EName = arc.name
}

/**
 * Another formula-related relationship, backed by an OtherFormulaArc.
 */
sealed abstract class OtherFormulaRelationship(
  arc: OtherFormulaArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: FormulaResource],
  resolvedTo: ResolvedLocatorOrResource[_ <: FormulaResource]) extends FormulaRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A variable-set-precondition relationship.
 */
final class VariableSetPreconditionRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: VariableSet],
    resolvedTo: ResolvedLocatorOrResource[_ <: Precondition]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/variable-set-precondition")

  def variableSet: VariableSet = resolvedFrom.resolvedElem

  def precondition: Precondition = resolvedTo.resolvedElem
}

/**
 * A consistency-assertion-formula relationship.
 */
final class ConsistencyAssertionFormulaRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: ConsistencyAssertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Formula]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/consistency-assertion-formula")

  def consistencyAssertion: ConsistencyAssertion = resolvedFrom.resolvedElem

  def formula: Formula = resolvedTo.resolvedElem
}

/**
 * An assertion-set relationship.
 */
final class AssertionSetRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AssertionSet],
    resolvedTo: ResolvedLocatorOrResource[_ <: Assertion]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2008/assertion-set")

  def assertionSet: AssertionSet = resolvedFrom.resolvedElem

  def assertion: Assertion = resolvedTo.resolvedElem
}

/**
 * An element-message relationship, as used in a formula-related context.
 */
sealed abstract class ElementMessageRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: FormulaResource],
    resolvedTo: ResolvedLocatorOrResource[_ <: Message]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  final def referredElement: FormulaResource = resolvedFrom.resolvedElem

  final def message: Message = resolvedTo.resolvedElem
}

/**
 * An AssertionSatisfiedMessageRelationship or AssertionUnsatisfiedMessageRelationship.
 */
sealed abstract class AssertionMessageRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Assertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Message]) extends ElementMessageRelationship(arc, resolvedFrom, resolvedTo) {

  final def assertion: Assertion = resolvedFrom.resolvedElem
}

/**
 * An assertion-satisfied-message relationship.
 */
final class AssertionSatisfiedMessageRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Assertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Message]) extends AssertionMessageRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2010/assertion-satisfied-message")
}

/**
 * An assertion-unsatisfied-message relationship.
 */
final class AssertionUnsatisfiedMessageRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Assertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Message]) extends AssertionMessageRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2010/assertion-unsatisfied-message")
}

/**
 * An instance-variable relationship.
 */
final class InstanceVariableRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Instance],
    resolvedTo: ResolvedLocatorOrResource[_ <: Variable]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2010/instance-variable")

  def instance: Instance = resolvedFrom.resolvedElem

  def variable: Variable = resolvedTo.resolvedElem
}

/**
 * A formula-instance relationship.
 */
final class FormulaInstanceRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Formula],
    resolvedTo: ResolvedLocatorOrResource[_ <: Instance]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2010/formula-instance")

  def formula: Formula = resolvedFrom.resolvedElem

  def instance: Instance = resolvedTo.resolvedElem
}

/**
 * An assertion-unsatisfied-severity relationship.
 */
final class AssertionUnsatisfiedSeverityRelationship(
    arc: OtherFormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: Assertion],
    resolvedTo: ResolvedLocatorOrResource[_ <: Severity]) extends OtherFormulaRelationship(arc, resolvedFrom, resolvedTo) {

  requireArcrole("http://xbrl.org/arcrole/2016/assertion-unsatisfied-severity")

  def assertion: Assertion = resolvedFrom.resolvedElem

  def severity: Severity = resolvedTo.resolvedElem
}

// Companion objects

object FormulaRelationship {

  /**
   * Optionally builds a `FormulaRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[FormulaRelationship] = {

    VariableSetRelationship.opt(arc, resolvedFrom, resolvedTo).
      orElse(VariableFilterRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(VariableSetFilterRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(VariableSetPreconditionRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(BooleanFilterRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(ConsistencyAssertionParameterRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(ConsistencyAssertionFormulaRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(AssertionSetRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(AssertionSatisfiedMessageRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(AssertionUnsatisfiedMessageRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(InstanceVariableRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(FormulaInstanceRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(AssertionUnsatisfiedSeverityRelationship.opt(arc, resolvedFrom, resolvedTo)).
      orElse(None)
  }

  /**
   * Lenient method to optionally create a FormulaRelationship from an underlying tqa.relationship.NonStandardRelationship.
   */
  def opt(underlyingRelationship: NonStandardRelationship): Option[FormulaRelationship] = {
    if (!underlyingRelationship.resolvedFrom.resolvedElem.isInstanceOf[XLinkResource] ||
      !underlyingRelationship.resolvedTo.resolvedElem.isInstanceOf[XLinkResource]) {

      None
    } else {
      val formulaArcOption: Option[FormulaArc] = toOptionalFormulaArc(underlyingRelationship.arc)

      formulaArcOption flatMap { formulaArc =>
        val resolvedFrom =
          ResolvedLocatorOrResource.unsafeTransformResource[AnyTaxonomyElem with XLinkResource](
            underlyingRelationship.resolvedFrom,
            { e => toOptionalFormulaResource(e).getOrElse(e.asInstanceOf[AnyTaxonomyElem with XLinkResource]) })

        val resolvedTo =
          ResolvedLocatorOrResource.unsafeTransformResource[AnyTaxonomyElem with XLinkResource](
            underlyingRelationship.resolvedTo,
            { e => toOptionalFormulaResource(e).getOrElse(e.asInstanceOf[AnyTaxonomyElem with XLinkResource]) })

        opt(formulaArc, resolvedFrom, resolvedTo)
      }
    }
  }

  private def toOptionalFormulaArc(arc: XLinkArc): Option[FormulaArc] = {
    arc match {
      case arc: NonStandardArc => FormulaArc.opt(arc)
      case _                   => None
    }
  }

  private def toOptionalFormulaResource(taxoElem: AnyTaxonomyElem): Option[FormulaResource] = {
    taxoElem match {
      case res: NonStandardResource => FormulaResource.opt(res)
      case _                        => None
    }
  }
}

object VariableSetRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `VariableSetRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[VariableSetRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2008/variable-set", arc: VariableArc, source: VariableSet, target: VariableOrParameter) =>
          Some(new VariableSetRelationship(
            arc, unsafeCastResource(resolvedFrom, classTag[VariableSet]), unsafeCastResource(resolvedTo, classTag[VariableOrParameter])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object VariableFilterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `VariableFilterRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[VariableFilterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2008/variable-filter", arc: VariableFilterArc, source: FactVariable, target: Filter) =>
          Some(new VariableFilterRelationship(
            arc, unsafeCastResource(resolvedFrom, classTag[FactVariable]), unsafeCastResource(resolvedTo, classTag[Filter])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object VariableSetFilterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `VariableSetFilterRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[VariableSetFilterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2008/variable-set-filter", arc: VariableSetFilterArc, source: VariableSet, target: Filter) =>
          Some(new VariableSetFilterRelationship(
            arc, unsafeCastResource(resolvedFrom, classTag[VariableSet]), unsafeCastResource(resolvedTo, classTag[Filter])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object BooleanFilterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `BooleanFilterRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[BooleanFilterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2008/boolean-filter", arc: VariableFilterArc, source: BooleanFilter, target: Filter) =>
          Some(new BooleanFilterRelationship(
            arc, unsafeCastResource(resolvedFrom, classTag[BooleanFilter]), unsafeCastResource(resolvedTo, classTag[Filter])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object ConsistencyAssertionParameterRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `ConsistencyAssertionParameterRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[ConsistencyAssertionParameterRelationship] = {

    if (arc.backingElem.resolvedName.namespaceUriOption.contains(Namespaces.VariableNamespace)) {
      (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
        case ("http://xbrl.org/arcrole/2008/consistency-assertion-parameter", arc: VariableArc, source: ConsistencyAssertion, target: Parameter) =>
          Some(new ConsistencyAssertionParameterRelationship(
            arc, unsafeCastResource(resolvedFrom, classTag[ConsistencyAssertion]), unsafeCastResource(resolvedTo, classTag[Parameter])))
        case (_, _, _, _) =>
          None
      }
    } else {
      None
    }
  }
}

object VariableSetPreconditionRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `VariableSetPreconditionRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[VariableSetPreconditionRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2008/variable-set-precondition", arc: OtherFormulaArc, source: VariableSet, target: Precondition) =>
        Some(new VariableSetPreconditionRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[VariableSet]), unsafeCastResource(resolvedTo, classTag[Precondition])))
      case (_, _, _, _) =>
        None
    }
  }
}

object ConsistencyAssertionFormulaRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `ConsistencyAssertionFormulaRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[ConsistencyAssertionFormulaRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2008/consistency-assertion-formula", arc: OtherFormulaArc, source: ConsistencyAssertion, target: Formula) =>
        Some(new ConsistencyAssertionFormulaRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[ConsistencyAssertion]), unsafeCastResource(resolvedTo, classTag[Formula])))
      case (_, _, _, _) =>
        None
    }
  }
}

object AssertionSetRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `AssertionSetRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[AssertionSetRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2008/assertion-set", arc: OtherFormulaArc, source: AssertionSet, target: Assertion) =>
        Some(new AssertionSetRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[AssertionSet]), unsafeCastResource(resolvedTo, classTag[Assertion])))
      case (_, _, _, _) =>
        None
    }
  }
}

object AssertionSatisfiedMessageRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `AssertionSatisfiedMessageRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[AssertionSatisfiedMessageRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2010/assertion-satisfied-message", arc: OtherFormulaArc, source: Assertion, target: Message) =>
        Some(new AssertionSatisfiedMessageRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[Assertion]), unsafeCastResource(resolvedTo, classTag[Message])))
      case (_, _, _, _) =>
        None
    }
  }
}

object AssertionUnsatisfiedMessageRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `AssertionUnsatisfiedMessageRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[AssertionUnsatisfiedMessageRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2010/assertion-unsatisfied-message", arc: OtherFormulaArc, source: Assertion, target: Message) =>
        Some(new AssertionUnsatisfiedMessageRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[Assertion]), unsafeCastResource(resolvedTo, classTag[Message])))
      case (_, _, _, _) =>
        None
    }
  }
}

object InstanceVariableRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `InstanceVariableRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[InstanceVariableRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2010/instance-variable", arc: OtherFormulaArc, source: Instance, target: Variable) =>
        Some(new InstanceVariableRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[Instance]), unsafeCastResource(resolvedTo, classTag[Variable])))
      case (_, _, _, _) =>
        None
    }
  }
}

object FormulaInstanceRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `FormulaInstanceRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[FormulaInstanceRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2010/formula-instance", arc: OtherFormulaArc, source: Formula, target: Instance) =>
        Some(new FormulaInstanceRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[Formula]), unsafeCastResource(resolvedTo, classTag[Instance])))
      case (_, _, _, _) =>
        None
    }
  }
}

object AssertionUnsatisfiedSeverityRelationship {

  import eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a `AssertionUnsatisfiedSeverityRelationship` from an underlying `FormulaArc`, a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]]
   * and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]], and returning None otherwise.
   */
  def opt(
    arc: FormulaArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: AnyTaxonomyElem]): Option[AssertionUnsatisfiedSeverityRelationship] = {

    (arc.arcrole, arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2016/assertion-unsatisfied-severity", arc: OtherFormulaArc, source: Assertion, target: Severity) =>
        Some(new AssertionUnsatisfiedSeverityRelationship(
          arc, unsafeCastResource(resolvedFrom, classTag[Assertion]), unsafeCastResource(resolvedTo, classTag[Severity])))
      case (_, _, _, _) =>
        None
    }
  }
}
