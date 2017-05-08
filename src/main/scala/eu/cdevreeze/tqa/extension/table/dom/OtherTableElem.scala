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
import eu.cdevreeze.tqa.Aspect
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.yaidom.core.EName
import javax.xml.bind.DatatypeConverter

/**
 * Non-XLink element in a table link.
 *
 * @author Chris de Vreeze
 */
sealed trait OtherTableElem extends OtherTableOrFormulaElem {

  def underlyingElem: tqa.dom.OtherElem

  final def key: XmlFragmentKey = underlyingElem.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingElem.resolvedName == ename,
      s"Expected $ename but found ${underlyingElem.resolvedName} in ${underlyingElem.docUri}")
  }

  protected[dom] def filterNonXLinkChildElemsOfType[A <: OtherTableOrFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingElem.findAllChildElemsOfType(classTag[tqa.dom.OtherElem]).
      flatMap(e => OtherTableElem.opt(e).orElse(OtherFormulaElem.opt(e))) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfType[A <: OtherTableOrFormulaElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfType(cls)(_ => true)
  }
}

/**
 * An aspect spec.
 */
sealed abstract class AspectSpec(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {

  def aspect: Aspect
}

/**
 * A table:conceptAspect.
 */
final class ConceptAspectSpec(underlyingElem: tqa.dom.OtherElem) extends AspectSpec(underlyingElem) {
  requireResolvedName(ENames.TableConceptAspectEName)

  def aspect: Aspect = Aspect.ConceptAspect
}

/**
 * A table:unitAspect.
 */
final class UnitAspectSpec(underlyingElem: tqa.dom.OtherElem) extends AspectSpec(underlyingElem) {
  requireResolvedName(ENames.TableUnitAspectEName)

  def aspect: Aspect = Aspect.UnitAspect
}

/**
 * A table:entityIdentifierAspect.
 */
final class EntityIdentifierAspectSpec(underlyingElem: tqa.dom.OtherElem) extends AspectSpec(underlyingElem) {
  requireResolvedName(ENames.TableEntityIdentifierAspectEName)

  def aspect: Aspect = Aspect.EntityIdentifierAspect
}

/**
 * A table:periodAspect.
 */
final class PeriodAspectSpec(underlyingElem: tqa.dom.OtherElem) extends AspectSpec(underlyingElem) {
  requireResolvedName(ENames.TablePeriodAspectEName)

  def aspect: Aspect = Aspect.PeriodAspect
}

/**
 * A table:dimensionAspect.
 */
final class DimensionAspectSpec(underlyingElem: tqa.dom.OtherElem) extends AspectSpec(underlyingElem) {
  requireResolvedName(ENames.TableDimensionAspectEName)

  def aspect: Aspect = Aspect.DimensionAspect(dimension)

  /**
   * Returns the dimension as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dimension: EName = {
    underlyingElem.textAsResolvedQName
  }

  /**
   * Returns the includeUnreportedValue attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def isIncludeUnreportedValue: Boolean = {
    underlyingElem.attributeOption(ENames.IncludeUnreportedValueEName).
      map(v => DatatypeConverter.parseBoolean(v)).getOrElse(false)
  }
}

/**
 * A table:ruleSet.
 */
final class RuleSet(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableRuleSetEName)

  def aspects: immutable.IndexedSeq[FormulaAspect] = {
    findAllNonXLinkChildElemsOfType(classTag[FormulaAspect])
  }

  def findAllAspectsOfType[A <: FormulaAspect](cls: ClassTag[A]): immutable.IndexedSeq[A] = {
    implicit val clsTag = cls
    aspects collect { case asp: A => asp }
  }

  /**
   * Returns the tag attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def tag: String = {
    underlyingElem.attribute(ENames.TagEName)
  }
}

/**
 * A table:relationshipSource.
 */
final class RelationshipSource(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableRelationshipSourceEName)

  /**
   * Returns the source as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def source: EName = {
    underlyingElem.textAsResolvedQName
  }
}

/**
 * A table:relationshipSourceExpression.
 */
final class RelationshipSourceExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableRelationshipSourceExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:linkrole.
 */
final class Linkrole(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableLinkroleEName)

  def linkrole: String = underlyingElem.text
}

/**
 * A table:linkroleExpression.
 */
final class LinkroleExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableLinkroleExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:arcrole.
 */
final class Arcrole(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableArcroleEName)

  def arcrole: String = underlyingElem.text
}

/**
 * A table:arcroleExpression.
 */
final class ArcroleExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableArcroleExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:formulaAxis in a table:conceptRelationshipNode.
 */
final class ConceptRelationshipNodeFormulaAxis(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableFormulaAxisEName)

  /**
   * Returns the value as FormulaAxis. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def formulaAxis: ConceptRelationshipNodeFormulaAxis.FormulaAxis = {
    ConceptRelationshipNodeFormulaAxis.FormulaAxis.fromString(underlyingElem.text)
  }
}

/**
 * A table:formulaAxisExpression in a table:conceptRelationshipNode.
 */
final class ConceptRelationshipNodeFormulaAxisExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableFormulaAxisExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:formulaAxis in a table:dimensionRelationshipNode.
 */
final class DimensionRelationshipNodeFormulaAxis(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableFormulaAxisEName)

  /**
   * Returns the value as FormulaAxis. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def formulaAxis: DimensionRelationshipNodeFormulaAxis.FormulaAxis = {
    DimensionRelationshipNodeFormulaAxis.FormulaAxis.fromString(underlyingElem.text)
  }
}

/**
 * A table:formulaAxisExpression in a table:dimensionRelationshipNode.
 */
final class DimensionRelationshipNodeFormulaAxisExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableFormulaAxisExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:generations.
 */
final class Generations(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableGenerationsEName)

  /**
   * Returns the value as integer. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def generations: Int = underlyingElem.text.toInt
}

/**
 * A table:generationsExpression.
 */
final class GenerationsExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableGenerationsExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:linkname.
 */
final class Linkname(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableLinknameEName)

  /**
   * Returns the value as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def linkname: EName = underlyingElem.textAsResolvedQName
}

/**
 * A table:linknameExpression.
 */
final class LinknameExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableLinknameExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:arcname.
 */
final class Arcname(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableArcnameEName)

  /**
   * Returns the value as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def arcname: EName = underlyingElem.textAsResolvedQName
}

/**
 * A table:arcnameExpression.
 */
final class ArcnameExpression(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableArcnameExpressionEName)

  /**
   * Returns the value as ScopedXPathString. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def scopedXPathString: ScopedXPathString = {
    ScopedXPathString(underlyingElem.text, underlyingElem.scope)
  }
}

/**
 * A table:dimension.
 */
final class TableDimension(val underlyingElem: tqa.dom.OtherElem) extends OtherTableElem {
  requireResolvedName(ENames.TableDimensionEName)

  /**
   * Returns the dimension as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dimension: EName = {
    underlyingElem.textAsResolvedQName
  }
}

// Companion objects

object OtherTableElem {

  /**
   * Lenient method to optionally create an OtherTableElem from an underlying tqa.dom.OtherElem.
   */
  def opt(underlyingElem: tqa.dom.OtherElem): Option[OtherTableElem] = {
    if (underlyingElem.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      underlyingElem.resolvedName match {
        case ENames.TableConceptAspectEName                => Some(new ConceptAspectSpec(underlyingElem))
        case ENames.TableUnitAspectEName                   => Some(new UnitAspectSpec(underlyingElem))
        case ENames.TableEntityIdentifierAspectEName       => Some(new EntityIdentifierAspectSpec(underlyingElem))
        case ENames.TablePeriodAspectEName                 => Some(new PeriodAspectSpec(underlyingElem))
        case ENames.TableDimensionAspectEName              => Some(new DimensionAspectSpec(underlyingElem))
        case ENames.TableRuleSetEName                      => Some(new RuleSet(underlyingElem))
        case ENames.TableRelationshipSourceEName           => Some(new RelationshipSource(underlyingElem))
        case ENames.TableRelationshipSourceExpressionEName => Some(new RelationshipSourceExpression(underlyingElem))
        case ENames.TableLinkroleEName                     => Some(new Linkrole(underlyingElem))
        case ENames.TableLinkroleExpressionEName           => Some(new LinkroleExpression(underlyingElem))
        case ENames.TableArcroleEName                      => Some(new Arcrole(underlyingElem))
        case ENames.TableArcroleExpressionEName            => Some(new ArcroleExpression(underlyingElem))
        case ENames.TableFormulaAxisEName =>
          underlyingElem.backingElem.parentOption.map(_.resolvedName) match {
            case Some(ENames.TableConceptRelationshipNodeEName) => Some(new ConceptRelationshipNodeFormulaAxis(underlyingElem))
            case Some(ENames.TableDimensionRelationshipNodeEName) => Some(new DimensionRelationshipNodeFormulaAxis(underlyingElem))
            case _ => None
          }
        case ENames.TableFormulaAxisExpressionEName =>
          underlyingElem.backingElem.parentOption.map(_.resolvedName) match {
            case Some(ENames.TableConceptRelationshipNodeEName) => Some(new ConceptRelationshipNodeFormulaAxisExpression(underlyingElem))
            case Some(ENames.TableDimensionRelationshipNodeEName) => Some(new DimensionRelationshipNodeFormulaAxisExpression(underlyingElem))
            case _ => None
          }
        case ENames.TableGenerationsEName           => Some(new Generations(underlyingElem))
        case ENames.TableGenerationsExpressionEName => Some(new GenerationsExpression(underlyingElem))
        case ENames.TableLinknameEName              => Some(new Linkname(underlyingElem))
        case ENames.TableLinknameExpressionEName    => Some(new LinknameExpression(underlyingElem))
        case ENames.TableArcnameEName               => Some(new Arcname(underlyingElem))
        case ENames.TableArcnameExpressionEName     => Some(new ArcnameExpression(underlyingElem))
        case ENames.TableDimensionEName             => Some(new TableDimension(underlyingElem))
        case _                                      => None
      }
    } else {
      None
    }
  }
}

object ConceptRelationshipNodeFormulaAxis {

  sealed trait FormulaAxis {

    def includesSelf: Boolean = {
      this == DescendantOrSelfAxis || this == ChildOrSelfAxis ||
        this == SiblingOrSelfAxis || this == SiblingOrDescendantOrSelfAxis
    }

    def includesDescendantsOrChildren: Boolean = {
      this == SiblingOrDescendantAxis || this == SiblingOrDescendantOrSelfAxis ||
        this == DescendantAxis || this == DescendantOrSelfAxis ||
        this == ChildAxis || this == ChildOrSelfAxis
    }

    def includesSiblings: Boolean = {
      this == SiblingAxis || this == SiblingOrSelfAxis ||
        this == SiblingOrDescendantAxis || this == SiblingOrDescendantOrSelfAxis
    }

    def includesChildrenButNotDeeperDescendants: Boolean = {
      this == ChildAxis || this == ChildOrSelfAxis
    }
  }

  case object DescendantAxis extends FormulaAxis
  case object DescendantOrSelfAxis extends FormulaAxis
  case object ChildAxis extends FormulaAxis
  case object ChildOrSelfAxis extends FormulaAxis
  case object SiblingAxis extends FormulaAxis
  case object SiblingOrSelfAxis extends FormulaAxis
  case object SiblingOrDescendantAxis extends FormulaAxis
  case object SiblingOrDescendantOrSelfAxis extends FormulaAxis

  object FormulaAxis {

    def fromString(s: String): FormulaAxis = s match {
      case "descendant"                    => DescendantAxis
      case "descendant-or-self"            => DescendantOrSelfAxis
      case "child"                         => ChildAxis
      case "child-or-self"                 => ChildOrSelfAxis
      case "sibling"                       => SiblingAxis
      case "sibling-or-self"               => SiblingOrSelfAxis
      case "sibling-or-descendant"         => SiblingOrDescendantAxis
      case "sibling-or-descendant-or-self" => SiblingOrDescendantOrSelfAxis
      case _                               => sys.error(s"Not a valid 'formula axis': $s")
    }
  }
}

object DimensionRelationshipNodeFormulaAxis {

  sealed trait FormulaAxis
  case object DescendantAxis extends FormulaAxis
  case object DescendantOrSelfAxis extends FormulaAxis
  case object ChildAxis extends FormulaAxis
  case object ChildOrSelfAxis extends FormulaAxis

  object FormulaAxis {

    def fromString(s: String): FormulaAxis = s match {
      case "descendant"         => DescendantAxis
      case "descendant-or-self" => DescendantOrSelfAxis
      case "child"              => ChildAxis
      case "child-or-self"      => ChildOrSelfAxis
      case _                    => sys.error(s"Not a valid 'formula axis': $s")
    }
  }
}
