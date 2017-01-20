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

import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkPresentationArcEName
import eu.cdevreeze.tqa.ENames.LinkReferenceArcEName
import eu.cdevreeze.tqa.ENames.XbrldtTargetRoleEName
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
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Path

/**
 * Any relationship. Relationships are like their underlying arcs, but resolving the locators.
 * Note that an underlying arc may represent more than 1 relationship.
 *
 * These objects must be very efficient to create.
 *
 * Like for the underlying taxonomy elements, relationship creation is designed not to fail, but the type may be
 * of an unexpected "catch-all relationship type". Instance methods on relationships may fail, however.
 *
 * This relationship type hierarchy knows about standard relationships, including dimensional relationships.
 * It also knows about a few specific generic relationships. It does not know about table and formula relationships,
 * which are only seen as non-standard or unknown relationships.
 *
 * @author Chris de Vreeze
 */
sealed abstract class Relationship(
    val arc: XLinkArc,
    val resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    val resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) {

  require(arc.from == resolvedFrom.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'from' not matching on label in $docUri")
  require(arc.to == resolvedTo.xlinkLocatorOrResource.xlinkLabel, s"Arc and 'to' not matching on label in $docUri")

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

  // TODO Order, equals and hashCode.
}

sealed abstract class StandardRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo) {

  final def sourceGlobalElementDeclaration: GlobalElementDeclaration = resolvedFrom.resolvedElem

  final def sourceConceptEName: EName = sourceGlobalElementDeclaration.targetEName
}

sealed abstract class NonStandardRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo)

final class UnknownRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo)

sealed abstract class InterConceptRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

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
    this.baseSetKey.arcEName == rel.baseSetKey.arcEName
  }
}

sealed abstract class ConceptResourceRelationship(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends StandardRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: XLinkResource
}

final class ConceptLabelRelationship(
    arc: LabelArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: ConceptLabelResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptLabelResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2003/role/label")

  def language: String = {
    resource.attributeOption(XmlLangEName).getOrElse(s"Missing xml:lang in $toPath in $docUri")
  }

  def labelText: String = resource.text
}

final class ConceptReferenceRelationship(
    arc: ReferenceArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: ConceptReferenceResource]) extends ConceptResourceRelationship(arc, resolvedFrom, resolvedTo) {

  def resource: ConceptReferenceResource = resolvedTo.resolvedElem

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2003/role/reference")

  def referenceElems: immutable.IndexedSeq[TaxonomyElem] = resource.findAllChildElems
}

// Standard presentation link relationships

sealed class PresentationRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

final class ParentChildRelationship(
  arc: PresentationArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends PresentationRelationship(arc, resolvedFrom, resolvedTo)

// Standard calculation link relationships

sealed class CalculationRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

final class SummationItemRelationship(
  arc: CalculationArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends CalculationRelationship(arc, resolvedFrom, resolvedTo)

// Standard definition link relationships, including dimensional ones

sealed class DefinitionRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends InterConceptRelationship(arc, resolvedFrom, resolvedTo)

final class GeneralSpecialRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

final class EssenceAliasRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

final class SimilarTuplesRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

final class RequiresElementRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

sealed abstract class DimensionalRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo) {

  override def effectiveTargetRole: String = {
    arc.attributeOption(XbrldtTargetRoleEName).getOrElse(elr)
  }
}

sealed abstract class HasHypercubeRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[HypercubeDimensionRelationship]
  }
}

final class AllRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo)

final class NotAllRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends HasHypercubeRelationship(arc, resolvedFrom, resolvedTo)

final class HypercubeDimensionRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DimensionDomainRelationship]
  }
}

final class DimensionDomainRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DomainMemberRelationship]
  }
}

final class DomainMemberRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  protected override def isFollowedByTypeOf(rel: InterConceptRelationship): Boolean = {
    rel.isInstanceOf[DomainMemberRelationship]
  }
}

final class DimensionDefaultRelationship(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo) {

  override def effectiveTargetRole: String = elr
}

// Generic relationships

sealed abstract class ElementResourceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

final class ElementLabelRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

final class ElementReferenceRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

final class ElementMessageRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

final class OtherNonStandardRelationship(
  arc: NonStandardArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

// Companion objects

object Relationship {

  def apply(
    arc: XLinkArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Relationship = {

    (arc, resolvedFrom.resolvedElem) match {
      case (arc: StandardArc, elemDecl: GlobalElementDeclaration) =>
        val from = resolvedFrom.asInstanceOf[ResolvedLocator[GlobalElementDeclaration]]
        StandardRelationship.opt(arc, from, resolvedTo).getOrElse(new UnknownRelationship(arc, resolvedFrom, resolvedTo))
      case (arc: NonStandardArc, _) =>
        NonStandardRelationship.opt(arc, resolvedFrom, resolvedTo).getOrElse(new UnknownRelationship(arc, resolvedFrom, resolvedTo))
      case (_, _) =>
        new UnknownRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}

object StandardRelationship {

  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Option[StandardRelationship] = {

    resolvedTo.resolvedElem match {
      case elemDecl: GlobalElementDeclaration =>
        InterConceptRelationship.opt(arc, resolvedFrom, resolvedTo.asInstanceOf[ResolvedLocator[GlobalElementDeclaration]])
      case res: XLinkResource =>
        ConceptResourceRelationship.opt(arc, resolvedFrom, resolvedTo.asInstanceOf[ResolvedLocatorOrResource[XLinkResource]])
      case _ => None
    }
  }
}

object NonStandardRelationship {

  def opt(
    arc: NonStandardArc,
    resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
    resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]): Option[NonStandardRelationship] = {

    // TODO
    Some(new OtherNonStandardRelationship(arc, resolvedFrom, resolvedTo))
  }
}

object InterConceptRelationship {

  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]): Option[InterConceptRelationship] = {

    (arc.resolvedName, arc) match {
      case (LinkDefinitionArcEName, arc: DefinitionArc) => Some(DefinitionRelationship(arc, resolvedFrom, resolvedTo))
      case (LinkPresentationArcEName, arc: PresentationArc) => Some(PresentationRelationship(arc, resolvedFrom, resolvedTo))
      case (LinkCalculationArcEName, arc: CalculationArc) => Some(CalculationRelationship(arc, resolvedFrom, resolvedTo))
      case _ => None
    }
  }
}

object ConceptResourceRelationship {

  def opt(
    arc: StandardArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]): Option[ConceptResourceRelationship] = {

    (arc.resolvedName, arc, resolvedTo.resolvedElem) match {
      case (LinkLabelArcEName, arc: LabelArc, lbl: ConceptLabelResource) =>
        Some(new ConceptLabelRelationship(arc, resolvedFrom, resolvedTo.asInstanceOf[ResolvedLocatorOrResource[ConceptLabelResource]]))
      case (LinkReferenceArcEName, arc: ReferenceArc, ref: ConceptReferenceResource) =>
        Some(new ConceptReferenceRelationship(arc, resolvedFrom, resolvedTo.asInstanceOf[ResolvedLocatorOrResource[ConceptReferenceResource]]))
      case _ => None
    }
  }
}

object DefinitionRelationship {

  def apply(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]): DefinitionRelationship = {

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

  def opt(
    arc: DefinitionArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]): Option[DimensionalRelationship] = {

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

  def apply(
    arc: PresentationArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]): PresentationRelationship = {

    arc.arcrole match {
      case "http://www.xbrl.org/2003/arcrole/parent-child" => new ParentChildRelationship(arc, resolvedFrom, resolvedTo)
      case _ => new PresentationRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}

object CalculationRelationship {

  def apply(
    arc: CalculationArc,
    resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
    resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]): CalculationRelationship = {

    arc.arcrole match {
      case "http://www.xbrl.org/2003/arcrole/summation-item" => new SummationItemRelationship(arc, resolvedFrom, resolvedTo)
      case _ => new CalculationRelationship(arc, resolvedFrom, resolvedTo)
    }
  }
}
