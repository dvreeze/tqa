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

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.yaidom.core.EName

/**
 * Non-XLink element in a table link but in the formula namespace.
 *
 * @author Chris de Vreeze
 */
sealed trait OtherFormulaTableElem extends tqa.dom.AnyTaxonomyElem {

  def underlyingElem: tqa.dom.OtherElem

  final def key: XmlFragmentKey = underlyingElem.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingElem.resolvedName == ename,
      s"Expected $ename but found ${underlyingElem.resolvedName} in ${underlyingElem.docUri}")
  }
}

/**
 * An aspect.
 */
sealed abstract class FormulaAspect(val underlyingElem: tqa.dom.OtherElem) extends OtherFormulaTableElem {

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    val scope = underlyingElem.scope.withoutDefaultNamespace
    underlyingElem.attributeAsQNameOption(ENames.SourceEName).map(qn => scope.resolveQNameOption(qn).get)
  }
}

/**
 * A formula:concept.
 */
final class ConceptAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaConceptEName)

  // TODO Optional qname element, and optional qnameExpression element
}

/**
 * A formula:entityIdentifier.
 */
final class EntityIdentifierAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaEntityIdentifierEName)

  // TODO Option scheme and value attributes
}

/**
 * A formula:period.
 */
final class PeriodAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaPeriodEName)

  // TODO Optional forever, instant and duration elements
}

/**
 * A formula:unit.
 */
final class UnitAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {
  requireResolvedName(ENames.FormulaUnitEName)

  // TODO Optional multiplyBy and divideBy elements, and optional augment attribute
}

/**
 * An OCC aspect.
 */
sealed abstract class OccAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem)

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

  // TODO Optional select attribute
}

/**
 * A dimension aspect.
 */
sealed abstract class DimensionAspect(underlyingElem: tqa.dom.OtherElem) extends FormulaAspect(underlyingElem) {

  // TODO Dimension attribute
}

/**
 * A formula:explicitDimension.
 */
final class ExplicitDimensionAspect(underlyingElem: tqa.dom.OtherElem) extends DimensionAspect(underlyingElem) {

  // TODO Optional member and omit elements
}

/**
 * A formula:typedDimension.
 */
final class TypedDimensionAspect(underlyingElem: tqa.dom.OtherElem) extends DimensionAspect(underlyingElem) {

  // TODO Optional xpath, value and omit elements
}

// Companion objects

object OtherFormulaTableElem {

  /**
   * Lenient method to optionally create an OtherFormulaTableElem from an underlying tqa.dom.OtherElem.
   */
  def opt(underlyingElem: tqa.dom.OtherElem): Option[OtherFormulaTableElem] = {
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
        case _                                    => None
      }
    } else {
      None
    }
  }
}
