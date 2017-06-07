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

package eu.cdevreeze.tqa.relationship

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkPresentationArcEName
import eu.cdevreeze.tqa.ENames.LinkReferenceArcEName
import eu.cdevreeze.tqa.ENames.MsgMessageEName
import eu.cdevreeze.tqa.ENames.XbrldtClosedEName
import eu.cdevreeze.tqa.ENames.XbrldtContextElementEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
import eu.cdevreeze.tqa.ENames.XbrldtUsableEName
import eu.cdevreeze.tqa.ENames.XLinkArcroleEName
import eu.cdevreeze.tqa.ENames.XmlLangEName
import eu.cdevreeze.tqa.dom.BaseSetKey
import eu.cdevreeze.tqa.dom.CalculationArc
import eu.cdevreeze.tqa.dom.ConceptLabelResource
import eu.cdevreeze.tqa.dom.ConceptReferenceResource
import eu.cdevreeze.tqa.dom.DefinitionArc
import eu.cdevreeze.tqa.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.dom.LabelArc
import eu.cdevreeze.tqa.dom.NonStandardArc
import eu.cdevreeze.tqa.dom.PresentationArc
import eu.cdevreeze.tqa.dom.ReferenceArc
import eu.cdevreeze.tqa.dom.StandardArc
import eu.cdevreeze.tqa.dom.TaxonomyElem
import eu.cdevreeze.tqa.dom.Use
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path
import javax.xml.bind.DatatypeConverter

/**
 * Any '''relationship'''. Relationships are like their underlying arcs, but resolving the locators.
 * Note that an underlying arc may represent more than 1 relationship.
 *
 * These objects must be very efficient to create.
 *
 * Like for the underlying taxonomy elements, relationship creation is designed not to fail, but the type may be
 * of an unexpected "catch-all relationship type". There is an exception to this leniency, though, and that is
 * that each arc must have an XLink arcole attribute, or else an exception is thrown. This can be circumvented
 * in practice by using an arc filter when instantiating a taxonomy object.
 *
 * Unlike relationship creation (with the missing arcrol exception), instance methods on relationships may fail, however.
 *
 * This relationship type hierarchy knows about standard relationships, including dimensional relationships.
 * It also knows about a few specific generic relationships. It does not know about table and formula relationships,
 * which are only seen as non-standard or unknown relationships.
 *
 * Each relationship is either a [[eu.cdevreeze.tqa.relationship.StandardRelationship]], a [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]],
 * or an [[eu.cdevreeze.tqa.relationship.UnknownRelationship]].
 *
 * @author Chris de Vreeze
 */
sealed abstract class Relationship(
    val arc: XLinkArc,
    val resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    val resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) {

  require(arc.from == resolvedFrom.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'from' not matching on label in $docUri")
  require(arc.to == resolvedTo.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'to' not matching on label in $docUri")
  require(arc.attributeOption(XLinkArcroleEName).isDefined, s"Missing arcrole attribute in ${arc.resolvedName} element in $docUri")

  final def validated: Relationship = {
    require(resolvedFrom.elr == arc.elr, s"Arc and 'from' not in same ELR in $docUri")
    require(resolvedTo.elr == arc.elr, s"Arc and 'to' not in same ELR in $docUri")
    this
  }

  final def sourceElem: TaxonomyElem = resolvedFrom.resolvedElem

  final def targetElem: TaxonomyElem = resolvedTo.resolvedElem

  final def docUri: URI = arc.docUri

  final def baseUri: URI = arc.baseUri

  final def elr: String = arc.elr

  final def arcrole: String = arc.arcrole

  final def arcPath: Path = arc.backingElem.path

  final def fromPath: Path = resolvedFrom.xlinkLocatorOrResource.backingElem.path

  final def toPath: Path = resolvedTo.xlinkLocatorOrResource.backingElem.path

  final def baseSetKey: BaseSetKey = arc.baseSetKey

  final def use: Use = arc.use

  final def priority: Int = arc.priority

  final def order: BigDecimal = arc.order

  final override def equals(obj: Any): Boolean = obj match {
    case other: Relationship =>
      (other.arc == this.arc) &&
        (other.resolvedFrom.resolvedElem == this.resolvedFrom.resolvedElem) &&
        (other.resolvedTo.resolvedElem == this.resolvedTo.resolvedElem)
    case _ => false
  }

  final override def hashCode: Int = (arc, resolvedFrom.resolvedElem, resolvedTo.resolvedElem).hashCode
}

/**
 * Standard relationship. Either an [[eu.cdevreeze.tqa.relationship.InterConceptRelationship]] or a
 * [[eu.cdevreeze.tqa.relationship.ConceptResourceRelationship]].
 */
sealed abstract class StandardRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo) {

  final def sourceGlobalElementDeclaration: GlobalElementDeclaration = resolvedFrom.resolvedElem

  final def sourceConceptEName: EName = sourceGlobalElementDeclaration.targetEName
}

/**
 * Non-standard relationship. Typically a generic relationship.
 */
sealed abstract class NonStandardRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo)

/**
 * Unknown relationship, so a relationship that is neither a standard nor a non-standard relationship. It may be
 * an invalid relationship.
 */
final class UnknownRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo)

/**
 * Standard inter-concept relationship. Either an [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]],
 * [[eu.cdevreeze.tqa.relationship.PresentationRelationship]], or a [[eu.cdevreeze.tqa.relationship.CalculationRelationship]].
 */
sealed abstract class InterConceptRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

  final def targetGlobalElementDeclaration: GlobalElementDeclaration = resolvedTo.resolvedElem

  final def targetConceptEName: EName = targetGlobalElementDeclaration.targetEName

  /**
   * For non-dimensional relationships, returns true if the target concept of this relationship matches the source
   * concept of the parameter relationship and the "types of relationships" are the same and the ELRs are the same.
   *
   * For dimensional relationships, returns true if this and the parameter relationships form a pair of consecutive
   * relationships.
   */
  final def isFollowedBy(rel: InterConceptRelationship): Boolean = {
    (this.targetConceptEName == rel.sourceConceptEName) && isFollowedByTypeOf(rel) && (effectiveTargetRole == rel.elr)
  }

  /**
   * Overridable method returning the effective target role, which is the ELR for non-dimensional relationships,
   * but respects the target role attribute for dimensional relationships.
   */
  def effectiveTargetRole: String = elr

  /**
   * Overridable method returning true if this and the other relationship are of "the same type".
   * It is used by method followedBy.
   */
  protected def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    (this.baseSetKey.arcEName == rel.baseSetKey.arcEName) &&
      (this.baseSetKey.arcrole == rel.baseSetKey.arcrole)
  }
}

/**
 * Standard concept-resource relationship. Either an [[eu.cdevreeze.tqa.relationship.ConceptLabelRelationship]], or a
 * [[eu.cdevreeze.tqa.relationship.ConceptReferenceRelationship]].
 */
sealed abstract class ConceptResourceRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: XLinkResource
}

/**
 * Concept-label relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.dom.LabelArc]].
 */
final class ConceptLabelRelationship(
    arc: LabelArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: ConceptLabelResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptLabelResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2003/role/label")

  def language: String = {
    resource.attributeOption(XmlLangEName).getOrElse(sys.error(s"Missing xml:lang in $toPath in $docUri"))
  }

  def labelText: String = resource.text
}

/**
 * Concept-reference relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.dom.ReferenceArc]].
 */
final class ConceptReferenceRelationship(
    arc: ReferenceArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: ConceptReferenceResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptReferenceResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2003/role/reference")

  def referenceElems: immutable.IndexedSeq[TaxonomyElem] = resource.findAllChildElems
}

// Standard presentation link relationships

/**
 * Presentation relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.dom.PresentationArc]].
 */
sealed class PresentationRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.PresentationRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/parent-child".
 */
final class ParentChildRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends PresentationRelationship(arc, resolvedFrom, resolvedTo)

// Standard calculation link relationships

/**
 * Calculation relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.dom.CalculationArc]].
 */
sealed class CalculationRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.CalculationRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/summation-item".
 */
final class SummationItemRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends CalculationRelationship(arc, resolvedFrom, resolvedTo)

// Standard definition link relationships, including dimensional ones

/**
 * Definition relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.dom.DefinitionArc]].
 */
sealed class DefinitionRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/general-special".
 */
final class GeneralSpecialRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/essence-alias".
 */
final class EssenceAliasRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/similar-tuples".
 */
final class SimilarTuplesRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/requires-element".
 */
final class RequiresElementRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * Dimensional definition relationship.
 */
sealed abstract class DimensionalRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo) {

  override def effectiveTargetRole: String = {
    arc.attributeOption(XbrldtTargetRoleEName).getOrElse(elr)
  }
}

/**
 * Either an [[eu.cdevreeze.tqa.relationship.AllRelationship]] or a [[eu.cdevreeze.tqa.relationship.NotAllRelationship]].
 */
sealed abstract class HasHypercubeRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  final def primary: EName = sourceConceptEName

  final def hypercube: EName = targetConceptEName

  def isAllRelationship: Boolean

  final def isNotAllRelationship: Boolean = !isAllRelationship

  final def closed: Boolean = {
    arc.attributeOption(XbrldtClosedEName).map(v => DatatypeConverter.parseBoolean(v)).getOrElse(false)
  }

  final def contextElement: String = {
    arc.attributeOption(XbrldtContextElementEName).getOrElse(
      sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in $docUri."))
  }

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[HypercubeDimensionRelationship]
  }
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/all".
 */
final class AllRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo) {

  def isAllRelationship: Boolean = true
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/notAll".
 */
final class NotAllRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo) {

  def isAllRelationship: Boolean = false
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/hypercube-dimension".
 */
final class HypercubeDimensionRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  def hypercube: EName = sourceConceptEName

  def dimension: EName = targetConceptEName

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DimensionDomainRelationship]
  }
}

/**
 * Either an [[eu.cdevreeze.tqa.relationship.DimensionDomainRelationship]] or a [[eu.cdevreeze.tqa.relationship.DomainMemberRelationship]].
 */
sealed abstract class DomainAwareRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  final def usable: Boolean = {
    arc.attributeOption(XbrldtUsableEName).map(v => DatatypeConverter.parseBoolean(v)).getOrElse(true)
  }
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/dimension-domain".
 */
final class DimensionDomainRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DomainAwareRelationship(arc, resolvedFrom, resolvedTo) {

  def dimension: EName = sourceConceptEName

  def domain: EName = targetConceptEName

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DomainMemberRelationship]
  }
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/domain-member".
 */
final class DomainMemberRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DomainAwareRelationship(arc, resolvedFrom, resolvedTo) {

  def domain: EName = sourceConceptEName

  def member: EName = targetConceptEName

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DomainMemberRelationship]
  }
}

/**
 * A [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/dimension-default".
 */
final class DimensionDefaultRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  def dimension: EName = sourceConceptEName

  def defaultOfDimension: EName = targetConceptEName

  override def effectiveTargetRole: String = elr
}

// Generic relationships

/**
 * Either an [[eu.cdevreeze.tqa.relationship.ElementLabelRelationship]] or an [[eu.cdevreeze.tqa.relationship.ElementReferenceRelationship]].
 */
sealed abstract class ElementResourceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]] with arcrole "http://xbrl.org/arcrole/2008/element-label".
 */
final class ElementLabelRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]] with arcrole "http://xbrl.org/arcrole/2008/element-reference".
 */
final class ElementReferenceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]] whose target is a msg:message element.
 */
final class ElementMessageRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]] not falling in the other categories of non-standard relationships.
 */
final class OtherNonStandardRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

// Companion objects

object Relationship {

  /**
   * Builds a [[eu.cdevreeze.tqa.relationship.Relationship]] from an underlying [[eu.cdevreeze.tqa.dom.XLinkArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]].
   */
  def apply(
    arc: XLinkArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Relationship = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    (arc, resolvedFrom.resolvedElem) match {
      case (arc: StandardArc, elemDecl: GlobalElementDeclaration) =>
        val from = resolvedFrom.asInstanceOf[ResolvedLocatorOrResource.Locator[GlobalElementDeclaration]]
        StandardRelationship.opt(arc, from, resolvedTo).getOrElse(new UnknownRelationship(arc, resolvedFrom, resolvedTo))
      case (arc: NonStandardArc, _) =>
        NonStandardRelationship.opt(arc, resolvedFrom, resolvedTo).getOrElse(new UnknownRelationship(arc, resolvedFrom, resolvedTo))
      case (_, _) =>
        new UnknownRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}

object StandardRelationship {

  import ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a [[eu.cdevreeze.tqa.relationship.StandardRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]],
   * and returning None otherwise.
   */
  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Option[StandardRelationship] = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    resolvedTo.resolvedElem match {
      case elemDecl: GlobalElementDeclaration =>
        InterConceptRelationship.opt(arc, resolvedFrom, resolvedTo.asInstanceOf[ResolvedLocatorOrResource.Locator[GlobalElementDeclaration]])
      case res: XLinkResource =>
        ConceptResourceRelationship.opt(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[XLinkResource]))
      case _ => None
    }
  }
}

object NonStandardRelationship {

  import ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a [[eu.cdevreeze.tqa.relationship.NonStandardRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.NonStandardArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]],
   * and returning None otherwise.
   */
  def opt(
    arc: NonStandardArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Option[NonStandardRelationship] = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    (arc.arcrole, arc, resolvedTo.resolvedElem) match {
      case ("http://xbrl.org/arcrole/2008/element-label", arc: NonStandardArc, res: XLinkResource) =>
        Some(new ElementLabelRelationship(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[XLinkResource])))
      case ("http://xbrl.org/arcrole/2008/element-reference", arc: NonStandardArc, res: XLinkResource) =>
        Some(new ElementReferenceRelationship(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[XLinkResource])))
      case (_, arc: NonStandardArc, res: XLinkResource) if resolvedTo.resolvedElem.resolvedName == MsgMessageEName =>
        Some(new ElementMessageRelationship(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[XLinkResource])))
      case _ =>
        Some(new OtherNonStandardRelationship(arc, resolvedFrom, resolvedTo))
    }
  }
}

object InterConceptRelationship {

  /**
   * Optionally builds a [[eu.cdevreeze.tqa.relationship.InterConceptRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]],
   * and returning None otherwise.
   */
  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]): Option[InterConceptRelationship] = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    (arc.resolvedName, arc) match {
      case (LinkDefinitionArcEName, arc: DefinitionArc) => Some(DefinitionRelationship(arc, resolvedFrom, resolvedTo))
      case (LinkPresentationArcEName, arc: PresentationArc) => Some(PresentationRelationship(arc, resolvedFrom, resolvedTo))
      case (LinkCalculationArcEName, arc: CalculationArc) => Some(CalculationRelationship(arc, resolvedFrom, resolvedTo))
      case _ => None
    }
  }
}

object ConceptResourceRelationship {

  import ResolvedLocatorOrResource.unsafeCastResource

  /**
   * Optionally builds a [[eu.cdevreeze.tqa.relationship.ConceptResourceRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]],
   * and returning None otherwise.
   */
  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]): Option[ConceptResourceRelationship] = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    (arc.resolvedName, arc, resolvedTo.resolvedElem) match {
      case (LinkLabelArcEName, arc: LabelArc, lbl: ConceptLabelResource) =>
        Some(new ConceptLabelRelationship(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[ConceptLabelResource])))
      case (LinkReferenceArcEName, arc: ReferenceArc, ref: ConceptReferenceResource) =>
        Some(new ConceptReferenceRelationship(arc, resolvedFrom, unsafeCastResource(resolvedTo, classTag[ConceptReferenceResource])))
      case _ => None
    }
  }
}

object DefinitionRelationship {

  /**
   * Builds a [[eu.cdevreeze.tqa.relationship.DefinitionRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.DefinitionArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]].
   */
  def apply(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]): DefinitionRelationship = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    arc.arcrole match {
      case "http://www.xbrl.org/2003/arcrole/general-special" => new GeneralSpecialRelationship(arc, resolvedFrom, resolvedTo)
      case "http://www.xbrl.org/2003/arcrole/essence-alias" => new EssenceAliasRelationship(arc, resolvedFrom, resolvedTo)
      case "http://www.xbrl.org/2003/arcrole/similar-tuples" => new SimilarTuplesRelationship(arc, resolvedFrom, resolvedTo)
      case "http://www.xbrl.org/2003/arcrole/requires-element" => new RequiresElementRelationship(arc, resolvedFrom, resolvedTo)
      case _ => DimensionalRelationship.opt(arc, resolvedFrom, resolvedTo).getOrElse(new DefinitionRelationship(arc, resolvedFrom, resolvedTo))
    }
  }
}

object DimensionalRelationship {

  /**
   * Optionally builds a [[eu.cdevreeze.tqa.relationship.DimensionalRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.DefinitionArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]],
   * and returning None otherwise.
   */
  def opt(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]): Option[DimensionalRelationship] = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    arc.arcrole match {
      case "http://xbrl.org/int/dim/arcrole/hypercube-dimension" => Some(new HypercubeDimensionRelationship(arc, resolvedFrom, resolvedTo))
      case "http://xbrl.org/int/dim/arcrole/dimension-domain" => Some(new DimensionDomainRelationship(arc, resolvedFrom, resolvedTo))
      case "http://xbrl.org/int/dim/arcrole/domain-member" => Some(new DomainMemberRelationship(arc, resolvedFrom, resolvedTo))
      case "http://xbrl.org/int/dim/arcrole/dimension-default" => Some(new DimensionDefaultRelationship(arc, resolvedFrom, resolvedTo))
      case "http://xbrl.org/int/dim/arcrole/all" => Some(new AllRelationship(arc, resolvedFrom, resolvedTo))
      case "http://xbrl.org/int/dim/arcrole/notAll" => Some(new NotAllRelationship(arc, resolvedFrom, resolvedTo))
      case _ => None
    }
  }
}

object PresentationRelationship {

  /**
   * Builds a [[eu.cdevreeze.tqa.relationship.PresentationRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.PresentationArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]].
   */
  def apply(
    arc: PresentationArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]): PresentationRelationship = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    arc.arcrole match {
      case "http://www.xbrl.org/2003/arcrole/parent-child" => new ParentChildRelationship(arc, resolvedFrom, resolvedTo)
      case _ => new PresentationRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}

object CalculationRelationship {

  /**
   * Builds a [[eu.cdevreeze.tqa.relationship.CalculationRelationship]] from an underlying [[eu.cdevreeze.tqa.dom.CalculationArc]],
   * a "from" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.relationship.ResolvedLocatorOrResource]].
   */
  def apply(
    arc: CalculationArc,
    resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]): CalculationRelationship = {

    require(
      arc.attributeOption(XLinkArcroleEName).isDefined,
      s"Missing arcrole attribute in ${arc.resolvedName} element. Document: ${arc.docUri}. Path: ${arc.backingElem.path}")

    arc.arcrole match {
      case "http://www.xbrl.org/2003/arcrole/summation-item" => new SummationItemRelationship(arc, resolvedFrom, resolvedTo)
      case _ => new CalculationRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}
