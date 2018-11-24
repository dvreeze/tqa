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

import java.net.URI

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.common.StandardLabelRoles
import eu.cdevreeze.tqa.base.common.Use
import eu.cdevreeze.yaidom.core.EName

/**
 * Any '''relationship''' in the model.
 *
 * This relationship type hierarchy knows about standard relationships, including dimensional relationships.
 * It also knows about a few specific generic relationships. It does not know about table and formula relationships,
 * which are only seen as non-standard or unknown relationships.
 *
 * Each relationship is either a [[eu.cdevreeze.tqa.base.model.StandardRelationship]] or a [[eu.cdevreeze.tqa.base.model.NonStandardRelationship]].
 * There is no such thing as an UnknownRelationship in the model (such relationships would not be created, but just
 * ignored, unlike their counterparts in the relationship API).
 *
 * @author Chris de Vreeze
 */
sealed trait Relationship {

  /**
   * The linkbase document URI from which this relationship originates. This URI does not say anything about
   * the location of the underlying arc in the document, not even the extended link parent element (strictly speaking).
   */
  def docUri: URI

  /**
   * The extended link role of the parent extended link, in the XML representation.
   */
  def elr: String

  /**
   * The arcrole of the underlying arc in the original XML representation. The arcrole gives semantics to the
   * relationship.
   */
  def arcrole: String

  /**
   * The source relationship node.
   */
  def source: RelationshipNode

  /**
   * The target relationship node.
   */
  def target: RelationshipNode

  /**
   * The attributes of the underlying arc, excluding XLink attributes.
   */
  def nonXLinkArcAttributes: Map[EName, String]

  // Derived "attributes"

  final def use: Use = {
    Use.fromString(nonXLinkArcAttributes.get(ENames.UseEName).getOrElse("optional"))
  }

  final def priority: Int = {
    nonXLinkArcAttributes.get(ENames.PriorityEName).getOrElse("0").toInt
  }

  final def order: BigDecimal = {
    BigDecimal(nonXLinkArcAttributes.get(ENames.OrderEName).getOrElse("1"))
  }

  def baseSetKey: BaseSetKey
}

sealed trait StandardRelationship extends Relationship {

  def source: RelationshipNode.Concept

  final def sourceConceptEName: EName = source.targetEName
}

sealed trait NonStandardRelationship extends Relationship {

  // TODO Preferred label
}

sealed trait InterConceptRelationship extends StandardRelationship {

  def target: RelationshipNode.Concept

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

  def target: RelationshipNode.Resource

  def resource: RelationshipNode.Resource
}

final case class ConceptLabelRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.ConceptLabelResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ConceptResourceRelationship {

  def resource: RelationshipNode.ConceptLabelResource = target

  def resourceRole: String = resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    resource.langOption.getOrElse(sys.error(s"Missing xml:lang in $target in $docUri"))
  }

  def labelText: String = resource.text

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forConceptLabelArc(elr)
  }
}

// TODO ConceptReferenceRelationship

sealed trait PresentationRelationship extends InterConceptRelationship {

  final def preferredLabelOption: Option[String] = {
    nonXLinkArcAttributes.get(ENames.PreferredLabelEName)
  }

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkPresentationArcEName, arcrole, ENames.LinkPresentationLinkEName, elr)
  }
}

final case class ParentChildRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship

final case class OtherPresentationRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship

sealed trait CalculationRelationship extends InterConceptRelationship {

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkCalculationArcEName, arcrole, ENames.LinkCalculationLinkEName, elr)
  }
}

final case class SummationItemRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship

final case class OtherCalculationRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship

sealed trait DefinitionRelationship extends InterConceptRelationship {

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkDefinitionArcEName, arcrole, ENames.LinkDefinitionLinkEName, elr)
  }
}

final case class GeneralSpecialRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

final case class EssenceAliasRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

final case class SimilarTuplesRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

final case class RequiresElementRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

sealed trait DimensionalRelationship extends DefinitionRelationship

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
      sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in $docUri."))

    ContextElement.fromString(attrValue)
  }

  final override def effectiveTargetRole: String = {
    nonXLinkArcAttributes.get(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class AllRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = true
}

final case class NotAllRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = false
}

final case class HypercubeDimensionRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def hypercube: EName = sourceConceptEName

  def dimension: EName = targetConceptEName

  override def effectiveTargetRole: String = {
    nonXLinkArcAttributes.get(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
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
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def dimension: EName = sourceConceptEName

  def domain: EName = targetConceptEName
}

final case class DomainMemberRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def domain: EName = sourceConceptEName

  def member: EName = targetConceptEName
}

final case class DimensionDefaultRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def dimension: EName = sourceConceptEName

  def defaultOfDimension: EName = targetConceptEName
}

final case class OtherDefinitionRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: RelationshipNode.Concept,
  target: RelationshipNode.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

// TODO Generic relationships, and factories for relationships.
