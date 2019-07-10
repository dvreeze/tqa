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

package eu.cdevreeze.tqa.base.model

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.common.StandardLabelRoles
import eu.cdevreeze.tqa.base.common.StandardReferenceRoles
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.tqa.common.schematypes.XsdDoubles
import eu.cdevreeze.yaidom.core.EName

/**
 * Any '''relationship''' in the model.
 *
 * This relationship type hierarchy knows about standard relationships, including dimensional relationships.
 * It also knows about a few specific generic relationships. It does not know about table and formula relationships,
 * which are only seen as non-standard relationships.
 *
 * Each relationship is either a [[eu.cdevreeze.tqa.base.model.StandardRelationship]] or a
 * [[eu.cdevreeze.tqa.base.model.NonStandardRelationship]]. There is no such thing as an UnknownRelationship in the model
 * (such relationships would not be created, but just ignored, unlike their counterparts in the XML-backed relationship API).
 *
 * Note that Relationship instances are easy to create on the fly, which is by design.
 *
 * @author Chris de Vreeze
 */
sealed trait Relationship {

  /**
   * The base set key, which consists of arcrole, arc name, (parent) linkrole and (parent) link name.
   */
  def baseSetKey: BaseSetKey

  /**
   * The source node of the relationship.
   */
  def source: Node

  /**
   * The target node of the relationship.
   */
  def target: Node

  /**
   * The attributes of the underlying arc, excluding XLink attributes.
   */
  def nonXLinkArcAttributes: Map[EName, String]

  // Derived "attributes"

  /**
   * The extended link role of the parent extended link, in the XML representation.
   * Equals `baseSetKey.extLinkRole`.
   */
  def elr: String

  /**
   * The arcrole of the underlying arc in the original XML representation. The arcrole gives semantics to the
   * relationship. Equals `baseSetKey.arcrole`.
   */
  def arcrole: String

  final def use: Use = {
    Use.fromString(nonXLinkArcAttributes.get(ENames.UseEName).getOrElse("optional"))
  }

  final def priority: Int = {
    nonXLinkArcAttributes.get(ENames.PriorityEName).getOrElse("0").toInt
  }

  final def order: BigDecimal = {
    BigDecimal(nonXLinkArcAttributes.get(ENames.OrderEName).getOrElse("1"))
  }

  final def validated: this.type = {
    require(
      nonXLinkArcAttributes.keySet.forall(!_.namespaceUriOption.contains(Namespaces.XLinkNamespace)),
      s"Expected non-XLink arcattributes, but got attributes ${nonXLinkArcAttributes.keySet}")

    this
  }
}

sealed trait StandardRelationship extends Relationship {

  def source: Node.GlobalElementDecl

  final def sourceConceptEName: EName = source.targetEName
}

sealed trait NonStandardRelationship extends Relationship {

  // TODO Preferred label
}

sealed trait InterConceptRelationship extends StandardRelationship {

  def target: Node.GlobalElementDecl

  final def targetConceptEName: EName = target.targetEName

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
  final def genericPreferredLabelOption: Option[String] = {
    nonXLinkArcAttributes.get(ENames.GplPreferredLabelEName)
  }
}

sealed trait ConceptResourceRelationship extends StandardRelationship {

  def target: Node.StandardDocumentationResource

  def resource: Node.StandardDocumentationResource
}

final case class ConceptLabelRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.ConceptLabelResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ConceptResourceRelationship {

  def resource: Node.ConceptLabelResource = target

  def arcrole: String = baseSetKey.arcrole

  def resourceRole: String = resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    resource.langOption.getOrElse(sys.error(s"Missing xml:lang in $target in ELR $elr"))
  }

  def labelText: String = resource.text

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forConceptLabelArc(elr)
  }
}

final case class ConceptReferenceRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.ConceptReferenceResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ConceptResourceRelationship {

  def resource: Node.ConceptReferenceResource = target

  def arcrole: String = baseSetKey.arcrole

  def resourceRole: String = resource.roleOption.getOrElse(StandardReferenceRoles.StandardReference)

  def parts: Map[EName, String] = resource.parts

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forConceptReferenceArc(elr)
  }
}

sealed trait PresentationRelationship extends InterConceptRelationship {

  final def preferredLabelOption: Option[String] = {
    nonXLinkArcAttributes.get(ENames.PreferredLabelEName)
  }
}

final case class ParentChildRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forParentChildArc(elr)
  }
}

final case class OtherPresentationRelationship(
  elr: String,
  arcrole: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship {

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forPresentationArc(arcrole, elr)
  }
}

sealed trait CalculationRelationship extends InterConceptRelationship {

  final def weight: Double = {
    nonXLinkArcAttributes.get(ENames.WeightEName).map(v => XsdDoubles.parseDouble(v))
      .getOrElse(sys.error(s"Missing attribute weight on calculation arc in ELR $elr."))
  }
}

final case class SummationItemRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forSummationItemArc(elr)
  }
}

final case class OtherCalculationRelationship(
  elr: String,
  arcrole: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship {

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forCalculationArc(arcrole, elr)
  }
}

sealed trait DefinitionRelationship extends InterConceptRelationship

final case class GeneralSpecialRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forGeneralSpecialArc(elr)
  }
}

final case class EssenceAliasRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forEssenceAliasArc(elr)
  }
}

final case class SimilarTuplesRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forSimilarTuplesArc(elr)
  }
}

final case class RequiresElementRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forRequiresElementArc(elr)
  }
}

sealed trait DimensionalRelationship extends DefinitionRelationship {

  final def xbrldtAttributes: Map[EName, String] = {
    nonXLinkArcAttributes.filter(_._1.namespaceUriOption.contains(Namespaces.XbrldtNamespace))
  }

  def withTargetRole(targetRole: String): DimensionalRelationship
}

sealed trait HasHypercubeRelationship extends DimensionalRelationship {

  final def primary: EName = sourceConceptEName

  final def hypercube: EName = targetConceptEName

  def isAllRelationship: Boolean

  final def isNotAllRelationship: Boolean = !isAllRelationship

  final def closed: Boolean = {
    nonXLinkArcAttributes.get(ENames.XbrldtClosedEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(false)
  }

  final def contextElement: ContextElement = {
    val attrValue = nonXLinkArcAttributes.get(ENames.XbrldtContextElementEName).getOrElse(
      sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in ELR $elr."))

    ContextElement.fromString(attrValue)
  }

  final override def effectiveTargetRole: String = {
    nonXLinkArcAttributes.get(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }

  def withContextElement(contextElement: String): HasHypercubeRelationship

  def withClosed(closed: Boolean): HasHypercubeRelationship
}

final case class AllRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = true

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forAllArc(elr)
  }

  def withTargetRole(targetRole: String): AllRelationship = {
    AllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
  }

  def withContextElement(contextElement: String): AllRelationship = {
    AllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtContextElementEName -> contextElement))
  }

  def withClosed(closed: Boolean): AllRelationship = {
    AllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtClosedEName -> closed.toString))
  }
}

final case class NotAllRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = false

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forNotAllArc(elr)
  }

  def withTargetRole(targetRole: String): NotAllRelationship = {
    NotAllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
  }

  def withContextElement(contextElement: String): NotAllRelationship = {
    NotAllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtContextElementEName -> contextElement))
  }

  def withClosed(closed: Boolean): NotAllRelationship = {
    NotAllRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtClosedEName -> closed.toString))
  }
}

final case class HypercubeDimensionRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def hypercube: EName = sourceConceptEName

  def dimension: EName = targetConceptEName

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(elr)
  }

  override def effectiveTargetRole: String = {
    nonXLinkArcAttributes.get(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }

  def withTargetRole(targetRole: String): HypercubeDimensionRelationship = {
    HypercubeDimensionRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
  }
}

sealed trait DomainAwareRelationship extends DimensionalRelationship {

  final def usable: Boolean = {
    nonXLinkArcAttributes.get(ENames.XbrldtUsableEName).map(v => XsdBooleans.parseBoolean(v)).getOrElse(true)
  }

  final override def effectiveTargetRole: String = {
    nonXLinkArcAttributes.get(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDomainMemberArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class DimensionDomainRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def dimension: EName = sourceConceptEName

  def domain: EName = targetConceptEName

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(elr)
  }

  def withTargetRole(targetRole: String): DimensionDomainRelationship = {
    DimensionDomainRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
  }

  def withUsable(targetRole: String): DimensionDomainRelationship = {
    DimensionDomainRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtUsableEName -> targetRole))
  }
}

final case class DomainMemberRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def domain: EName = sourceConceptEName

  def member: EName = targetConceptEName

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forDomainMemberArc(elr)
  }

  def withTargetRole(targetRole: String): DomainMemberRelationship = {
    DomainMemberRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtTargetRoleEName -> targetRole))
  }

  def withUsable(targetRole: String): DomainMemberRelationship = {
    DomainMemberRelationship(elr, source, target, nonXLinkArcAttributes + (ENames.XbrldtUsableEName -> targetRole))
  }
}

final case class DimensionDefaultRelationship(
  elr: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def dimension: EName = sourceConceptEName

  def defaultOfDimension: EName = targetConceptEName

  def arcrole: String = baseSetKey.arcrole

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDefaultArc(elr)
  }

  def withTargetRole(targetRole: String): DimensionDefaultRelationship = {
    // A no-op
    this
  }
}

final case class OtherDefinitionRelationship(
  elr: String,
  arcrole: String,
  source: Node.GlobalElementDecl,
  target: Node.GlobalElementDecl,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forDefinitionArc(arcrole, elr)
  }
}

// Generic relationships

sealed trait ElementResourceRelationship extends NonStandardRelationship {

  def target: Node.NonStandardDocumentationResource

  def resource: Node.NonStandardDocumentationResource
}

// TODO Element-label and element-reference relationships with other resource element name

final case class ElementLabelRelationship(
  elr: String,
  source: Node,
  target: Node.ElementLabelResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ElementResourceRelationship {

  def resource: Node.ElementLabelResource = target

  def arcrole: String = baseSetKey.arcrole

  // Assuming link name gen:link and arc name gen:arc

  def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.GenArcEName, "http://xbrl.org/arcrole/2008/element-label", ENames.GenLinkEName, elr)
  }

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2008/role/label")

  def language: String = {
    resource.langOption.getOrElse(sys.error(s"Missing xml:lang in $target in ELR $elr"))
  }

  def labelText: String = resource.text
}

final case class ElementReferenceRelationship(
  elr: String,
  source: Node,
  target: Node.ElementReferenceResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ElementResourceRelationship {

  def resource: Node.ElementReferenceResource = target

  def arcrole: String = baseSetKey.arcrole

  // Assuming link name gen:link and arc name gen:arc

  def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.GenArcEName, "http://xbrl.org/arcrole/2008/element-reference", ENames.GenLinkEName, elr)
  }

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2008/role/reference")

  def parts: Map[EName, String] = resource.parts
}

// TODO Element-message relationship

final case class OtherNonStandardRelationship(
  baseSetKey: BaseSetKey,
  source: Node,
  target: Node,
  nonXLinkArcAttributes: Map[EName, String]) extends NonStandardRelationship {

  def elr: String = baseSetKey.extLinkRole

  def arcrole: String = baseSetKey.arcrole
}

// Companion objects

object Relationship extends Relationships.Factory {

  type RelationshipType = Relationship
  type SourceNodeType = Node
  type TargetNodeType = Node

  def opt(
    baseSetKey: BaseSetKey,
    source: Node,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[Relationship] = {

    (baseSetKey.arcEName.namespaceUriOption, source) match {
      case (Some(Namespaces.LinkNamespace), sourceConcept @ Node.GlobalElementDecl(_)) =>
        StandardRelationship.opt(baseSetKey, sourceConcept, target, nonXLinkArcAttributes)
      case (Some(ns), _) if ns != Namespaces.LinkNamespace =>
        NonStandardRelationship.opt(baseSetKey, source, target, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object StandardRelationship extends Relationships.Factory {

  type RelationshipType = StandardRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[StandardRelationship] = {

    (baseSetKey.arcEName.namespaceUriOption, target) match {
      case (Some(Namespaces.LinkNamespace), targetConcept @ Node.GlobalElementDecl(_)) =>
        InterConceptRelationship.opt(baseSetKey, source, targetConcept, nonXLinkArcAttributes)
      case (Some(Namespaces.LinkNamespace), resource: Node.StandardDocumentationResource) =>
        ConceptResourceRelationship.opt(baseSetKey, source, resource, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object InterConceptRelationship extends Relationships.Factory {

  type RelationshipType = InterConceptRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.GlobalElementDecl

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.GlobalElementDecl,
    nonXLinkArcAttributes: Map[EName, String]): Option[InterConceptRelationship] = {

    baseSetKey.arcEName match {
      case ENames.LinkDefinitionArcEName =>
        DefinitionRelationship.opt(baseSetKey, source, target, nonXLinkArcAttributes)
      case ENames.LinkPresentationArcEName =>
        PresentationRelationship.opt(baseSetKey, source, target, nonXLinkArcAttributes)
      case ENames.LinkCalculationArcEName =>
        CalculationRelationship.opt(baseSetKey, source, target, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object ConceptResourceRelationship extends Relationships.Factory {

  type RelationshipType = ConceptResourceRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.StandardDocumentationResource

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.StandardDocumentationResource,
    nonXLinkArcAttributes: Map[EName, String]): Option[ConceptResourceRelationship] = {

    (baseSetKey.arcEName, target) match {
      case (ENames.LinkLabelArcEName, resource: Node.ConceptLabelResource) =>
        Some(ConceptLabelRelationship(baseSetKey.extLinkRole, source, resource, nonXLinkArcAttributes))
      case (ENames.LinkReferenceArcEName, resource: Node.ConceptReferenceResource) =>
        Some(ConceptReferenceRelationship(baseSetKey.extLinkRole, source, resource, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object DefinitionRelationship extends Relationships.Factory {

  type RelationshipType = DefinitionRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.GlobalElementDecl

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.GlobalElementDecl,
    nonXLinkArcAttributes: Map[EName, String]): Option[DefinitionRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/general-special") =>
        Some(GeneralSpecialRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/essence-alias") =>
        Some(EssenceAliasRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/similar-tuples") =>
        Some(SimilarTuplesRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/requires-element") =>
        Some(RequiresElementRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, _) =>
        DimensionalRelationship.opt(baseSetKey, source, target, nonXLinkArcAttributes)
          .orElse(Some(OtherDefinitionRelationship(baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes)))
      case _ =>
        None
    }
  }
}

object PresentationRelationship extends Relationships.Factory {

  type RelationshipType = PresentationRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.GlobalElementDecl

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.GlobalElementDecl,
    nonXLinkArcAttributes: Map[EName, String]): Option[PresentationRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkPresentationArcEName, "http://www.xbrl.org/2003/arcrole/parent-child") =>
        Some(ParentChildRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkPresentationArcEName, _) =>
        Some(OtherPresentationRelationship(baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object CalculationRelationship extends Relationships.Factory {

  type RelationshipType = CalculationRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.GlobalElementDecl

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.GlobalElementDecl,
    nonXLinkArcAttributes: Map[EName, String]): Option[CalculationRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkCalculationArcEName, "http://www.xbrl.org/2003/arcrole/summation-item") =>
        Some(SummationItemRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkCalculationArcEName, _) =>
        Some(OtherCalculationRelationship(baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object DimensionalRelationship extends Relationships.Factory {

  type RelationshipType = DimensionalRelationship
  type SourceNodeType = Node.GlobalElementDecl
  type TargetNodeType = Node.GlobalElementDecl

  def opt(
    baseSetKey: BaseSetKey,
    source: Node.GlobalElementDecl,
    target: Node.GlobalElementDecl,
    nonXLinkArcAttributes: Map[EName, String]): Option[DimensionalRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/hypercube-dimension") =>
        Some(HypercubeDimensionRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-domain") =>
        Some(DimensionDomainRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/domain-member") =>
        Some(DomainMemberRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-default") =>
        Some(DimensionDefaultRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/all") =>
        Some(AllRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/notAll") =>
        Some(NotAllRelationship(baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object NonStandardRelationship extends Relationships.Factory {

  type RelationshipType = NonStandardRelationship
  type SourceNodeType = Node
  type TargetNodeType = Node

  def opt(
    baseSetKey: BaseSetKey,
    source: Node,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[NonStandardRelationship] = {

    if (baseSetKey.arcEName.namespaceUriOption.contains(Namespaces.LinkNamespace)) {
      None
    } else {
      (baseSetKey.arcrole, target) match {
        case ("http://xbrl.org/arcrole/2008/element-label", res: Node.ElementLabelResource) =>
          Some(ElementLabelRelationship(baseSetKey.extLinkRole, source, res, nonXLinkArcAttributes))
        case ("http://xbrl.org/arcrole/2008/element-reference", res: Node.ElementReferenceResource) =>
          Some(ElementReferenceRelationship(baseSetKey.extLinkRole, source, res, nonXLinkArcAttributes))
        case _ =>
          Some(OtherNonStandardRelationship(baseSetKey, source, target, nonXLinkArcAttributes))
      }
    }
  }
}
