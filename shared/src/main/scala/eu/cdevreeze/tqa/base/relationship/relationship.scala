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

package eu.cdevreeze.tqa.base.relationship

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.ENames.GplPreferredLabelEName
import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkPresentationArcEName
import eu.cdevreeze.tqa.ENames.LinkReferenceArcEName
import eu.cdevreeze.tqa.ENames.MsgMessageEName
import eu.cdevreeze.tqa.ENames.PreferredLabelEName
import eu.cdevreeze.tqa.ENames.XLinkArcroleEName
import eu.cdevreeze.tqa.ENames.XbrldtClosedEName
import eu.cdevreeze.tqa.ENames.XbrldtContextElementEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
import eu.cdevreeze.tqa.ENames.XbrldtUsableEName
import eu.cdevreeze.tqa.ENames.XmlLangEName
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.common.StandardLabelRoles
import eu.cdevreeze.tqa.base.common.StandardReferenceRoles
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.base.dom.CalculationArc
import eu.cdevreeze.tqa.base.dom.ConceptLabelResource
import eu.cdevreeze.tqa.base.dom.ConceptReferenceResource
import eu.cdevreeze.tqa.base.dom.DefinitionArc
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.LabelArc
import eu.cdevreeze.tqa.base.dom.NonStandardArc
import eu.cdevreeze.tqa.base.dom.PresentationArc
import eu.cdevreeze.tqa.base.dom.ReferenceArc
import eu.cdevreeze.tqa.base.dom.StandardArc
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.dom.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path

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
 * Unlike relationship creation (with the missing arcrole exception), instance methods on relationships may fail, however.
 *
 * This relationship type hierarchy knows about standard relationships, including dimensional relationships.
 * It also knows about a few specific generic relationships. It does not know about table and formula relationships,
 * which are only seen as non-standard or unknown relationships.
 *
 * Each relationship is either a [[eu.cdevreeze.tqa.base.relationship.StandardRelationship]], a [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]],
 * or an [[eu.cdevreeze.tqa.base.relationship.UnknownRelationship]].
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

  /**
   * Returns the source element path. This method is very fast.
   */
  final def fromPath: Path = sourceElem.path

  /**
   * Returns the target element path. This method is very fast.
   */
  final def toPath: Path = targetElem.path

  final def baseSetKey: BaseSetKey = arc.baseSetKey

  final def use: Use = arc.use

  final def priority: Int = arc.priority

  final def order: BigDecimal = arc.order

  final def uniqueKey: Relationship.UniqueKey = {
    Relationship.UniqueKey(arc.key, sourceElem.key, targetElem.key)
  }

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
 * Standard relationship. Either an [[eu.cdevreeze.tqa.base.relationship.InterConceptRelationship]] or a
 * [[eu.cdevreeze.tqa.base.relationship.ConceptResourceRelationship]].
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
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo) {

  /**
   * Returns the optional gpl:preferredLabel attribute on the underlying arc.
   */
  def genericPreferredLabelOption: Option[String] = {
    arc.attributeOption(GplPreferredLabelEName)
  }
}

/**
 * Unknown relationship, so a relationship that is neither a standard nor a non-standard relationship. It may be
 * an invalid relationship.
 */
final class UnknownRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo)

/**
 * Standard inter-concept relationship. Either an [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]],
 * [[eu.cdevreeze.tqa.base.relationship.PresentationRelationship]], or a [[eu.cdevreeze.tqa.base.relationship.CalculationRelationship]].
 */
sealed abstract class InterConceptRelationship(
  arc: StandardArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

  final def targetGlobalElementDeclaration: GlobalElementDeclaration = resolvedTo.resolvedElem

  final def targetConceptEName: EName = targetGlobalElementDeclaration.targetEName

  /**
   * For non-dimensional relationships, returns true if the target concept of this relationship matches the source
   * concept of the parameter relationship and both relationships are in the same base set.
   *
   * For dimensional relationships, returns true if this and the parameter relationship form a pair of consecutive
   * relationships.
   *
   * This method does not check for the target relationship type, although for non-dimensional inter-concept relationships
   * the relationship type remains the same, and for dimensional relationships the target relationship type is as expected
   * for consecutive relationships.
   */
  final def isFollowedBy(rel: InterConceptRelationship): Boolean = {
    (this.targetConceptEName == rel.sourceConceptEName) &&
      (this.effectiveTargetBaseSetKey == rel.baseSetKey)
  }

  /**
   * Overridable method returning the effective target role, which is the ELR for non-dimensional relationships,
   * but respects the target role attribute for dimensional relationships.
   */
  def effectiveTargetRole: String = elr

  /**
   * Overridable method returning the effective target BaseSetKey, which is the same BaseSetKey for non-dimensional relationships,
   * but respects the rules concerning consecutive relationships for dimensional relationships.
   * This method is used by method followedBy.
   */
  def effectiveTargetBaseSetKey: BaseSetKey = {
    this.baseSetKey.ensuring(_.extLinkRole == effectiveTargetRole)
  }

  /**
   * Returns the optional gpl:preferredLabel attribute on the underlying arc.
   */
  def genericPreferredLabelOption: Option[String] = {
    arc.attributeOption(GplPreferredLabelEName)
  }
}

/**
 * Standard concept-resource relationship. Either an [[eu.cdevreeze.tqa.base.relationship.ConceptLabelRelationship]], or a
 * [[eu.cdevreeze.tqa.base.relationship.ConceptReferenceRelationship]].
 */
sealed abstract class ConceptResourceRelationship(
  arc: StandardArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: XLinkResource
}

/**
 * Concept-label relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.base.dom.LabelArc]].
 */
final class ConceptLabelRelationship(
  arc: LabelArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource[_ <: ConceptLabelResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptLabelResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    resource.attributeOption(XmlLangEName).getOrElse(sys.error(s"Missing xml:lang in $toPath in $docUri"))
  }

  def labelText: String = resource.text
}

/**
 * Concept-reference relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.base.dom.ReferenceArc]].
 */
final class ConceptReferenceRelationship(
  arc: ReferenceArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource[_ <: ConceptReferenceResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptReferenceResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse(StandardReferenceRoles.StandardReference)

  def referenceElems: immutable.IndexedSeq[TaxonomyElem] = resource.findAllChildElems
}

// Standard presentation link relationships

/**
 * Presentation relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.base.dom.PresentationArc]].
 */
sealed class PresentationRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo) {

  /**
   * Returns the optional preferredLabel attribute on the underlying arc.
   */
  final def preferredLabelOption: Option[String] = {
    arc.attributeOption(PreferredLabelEName)
  }
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.PresentationRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/parent-child".
 */
final class ParentChildRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends PresentationRelationship(arc, resolvedFrom, resolvedTo)

// Standard calculation link relationships

/**
 * Calculation relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.base.dom.CalculationArc]].
 */
sealed class CalculationRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.CalculationRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/summation-item".
 */
final class SummationItemRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends CalculationRelationship(arc, resolvedFrom, resolvedTo)

// Standard definition link relationships, including dimensional ones

/**
 * Definition relationship. Its underlying arc is of type [[eu.cdevreeze.tqa.base.dom.DefinitionArc]].
 */
sealed class DefinitionRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/general-special".
 */
final class GeneralSpecialRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/essence-alias".
 */
final class EssenceAliasRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/similar-tuples".
 */
final class SimilarTuplesRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]] with arcrole "http://www.xbrl.org/2003/arcrole/requires-element".
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
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

/**
 * Either an [[eu.cdevreeze.tqa.base.relationship.AllRelationship]] or a [[eu.cdevreeze.tqa.base.relationship.NotAllRelationship]].
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
    arc.attributeOption(XbrldtClosedEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(false)
  }

  final def contextElement: ContextElement = {
    val attrValue = arc.attributeOption(XbrldtContextElementEName).getOrElse(
      sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in $docUri."))

    ContextElement.fromString(attrValue)
  }

  final override def effectiveTargetRole: String = {
    arc.attributeOption(XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/all".
 */
final class AllRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo) {

  def isAllRelationship: Boolean = true
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/notAll".
 */
final class NotAllRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo) {

  def isAllRelationship: Boolean = false
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/hypercube-dimension".
 */
final class HypercubeDimensionRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  def hypercube: EName = sourceConceptEName

  def dimension: EName = targetConceptEName

  override def effectiveTargetRole: String = {
    arc.attributeOption(XbrldtTargetRoleEName).getOrElse(elr)
  }

  override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

/**
 * Either an [[eu.cdevreeze.tqa.base.relationship.DimensionDomainRelationship]] or a [[eu.cdevreeze.tqa.base.relationship.DomainMemberRelationship]].
 */
sealed abstract class DomainAwareRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  final def usable: Boolean = {
    arc.attributeOption(XbrldtUsableEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(true)
  }

  final override def effectiveTargetRole: String = {
    arc.attributeOption(XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDomainMemberArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/dimension-domain".
 */
final class DimensionDomainRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DomainAwareRelationship(arc, resolvedFrom, resolvedTo) {

  def dimension: EName = sourceConceptEName

  def domain: EName = targetConceptEName
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/domain-member".
 */
final class DomainMemberRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DomainAwareRelationship(arc, resolvedFrom, resolvedTo) {

  def domain: EName = sourceConceptEName

  def member: EName = targetConceptEName
}

/**
 * A [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] with arcrole "http://xbrl.org/int/dim/arcrole/dimension-default".
 */
final class DimensionDefaultRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource.Locator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  def dimension: EName = sourceConceptEName

  def defaultOfDimension: EName = targetConceptEName
}

// Generic relationships

/**
 * Either an [[eu.cdevreeze.tqa.base.relationship.ElementLabelRelationship]] or an [[eu.cdevreeze.tqa.base.relationship.ElementReferenceRelationship]].
 */
sealed abstract class ElementResourceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]] with arcrole "http://xbrl.org/arcrole/2008/element-label".
 */
final class ElementLabelRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]] with arcrole "http://xbrl.org/arcrole/2008/element-reference".
 */
final class ElementReferenceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]] whose target is a msg:message element.
 */
final class ElementMessageRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

/**
 * A [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]] not falling in the other categories of non-standard relationships.
 */
final class OtherNonStandardRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

// Companion objects

object Relationship {

  /**
   * Unique key of a relationship. It is not to be confused with a relationship key that is the same for equivalent
   * relationships (used for determining networks of relationships).
   *
   * The unique relationship key consists of the key of the underlying arc, the source element key, and the
   * target element key. The latter two are needed because one arc can represent more than one relationship.
   *
   * This unique key can be useful as Map keys where the Map keys represent relationships. For example, Maps from
   * has-hypercube relationships to their dimension-members can use the has-hypercube unique relationship key
   * as Map key.
   */
  final case class UniqueKey(arcKey: XmlFragmentKey, sourceElemKey: XmlFragmentKey, targetElemKey: XmlFragmentKey)

  /**
   * Builds a [[eu.cdevreeze.tqa.base.relationship.Relationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.XLinkArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]].
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
   * Optionally builds a [[eu.cdevreeze.tqa.base.relationship.StandardRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]],
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
   * Optionally builds a [[eu.cdevreeze.tqa.base.relationship.NonStandardRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.NonStandardArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]],
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
   * Optionally builds a [[eu.cdevreeze.tqa.base.relationship.InterConceptRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]],
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
   * Optionally builds a [[eu.cdevreeze.tqa.base.relationship.ConceptResourceRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.StandardArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]],
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
   * Builds a [[eu.cdevreeze.tqa.base.relationship.DefinitionRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.DefinitionArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]].
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
   * Optionally builds a [[eu.cdevreeze.tqa.base.relationship.DimensionalRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.DefinitionArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]],
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
   * Builds a [[eu.cdevreeze.tqa.base.relationship.PresentationRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.PresentationArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]].
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
   * Builds a [[eu.cdevreeze.tqa.base.relationship.CalculationRelationship]] from an underlying [[eu.cdevreeze.tqa.base.dom.CalculationArc]],
   * a "from" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]] and a "to" [[eu.cdevreeze.tqa.base.relationship.ResolvedLocatorOrResource]].
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
