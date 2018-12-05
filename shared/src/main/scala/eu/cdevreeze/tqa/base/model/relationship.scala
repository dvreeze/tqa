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
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XsdBooleans
import eu.cdevreeze.tqa.base.common.BaseSetKey
import eu.cdevreeze.tqa.base.common.ContextElement
import eu.cdevreeze.tqa.base.common.StandardLabelRoles
import eu.cdevreeze.tqa.base.common.StandardReferenceRoles
import eu.cdevreeze.tqa.base.common.Use
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
 * @author Chris de Vreeze
 */
sealed trait Relationship {

  /**
   * The linkbase document URI from which this relationship originates.
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

  def source: Node.Concept

  final def sourceConceptEName: EName = source.targetEName
}

sealed trait NonStandardRelationship extends Relationship {

  // TODO Preferred label
}

sealed trait InterConceptRelationship extends StandardRelationship {

  def target: Node.Concept

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
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.ConceptLabelResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ConceptResourceRelationship {

  def resource: Node.ConceptLabelResource = target

  def arcrole: String = baseSetKey.arcrole

  def resourceRole: String = resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    resource.langOption.getOrElse(sys.error(s"Missing xml:lang in $target in $docUri"))
  }

  def labelText: String = resource.text

  def baseSetKey: BaseSetKey = {
    BaseSetKey.forConceptLabelArc(elr)
  }
}

final case class ConceptReferenceRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
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

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkPresentationArcEName, arcrole, ENames.LinkPresentationLinkEName, elr)
  }
}

final case class ParentChildRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship {

  def arcrole: String = BaseSetKey.forParentChildArc(elr).arcrole
}

final case class OtherPresentationRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends PresentationRelationship

sealed trait CalculationRelationship extends InterConceptRelationship {

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkCalculationArcEName, arcrole, ENames.LinkCalculationLinkEName, elr)
  }
}

final case class SummationItemRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship {

  def arcrole: String = BaseSetKey.forSummationItemArc(elr).arcrole
}

final case class OtherCalculationRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends CalculationRelationship

sealed trait DefinitionRelationship extends InterConceptRelationship {

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.LinkDefinitionArcEName, arcrole, ENames.LinkDefinitionLinkEName, elr)
  }
}

final case class GeneralSpecialRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = BaseSetKey.forGeneralSpecialArc(elr).arcrole
}

final case class EssenceAliasRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = BaseSetKey.forEssenceAliasArc(elr).arcrole
}

final case class SimilarTuplesRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = BaseSetKey.forSimilarTuplesArc(elr).arcrole
}

final case class RequiresElementRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship {

  def arcrole: String = BaseSetKey.forRequiresElementArc(elr).arcrole
}

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
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = true

  def arcrole: String = BaseSetKey.forAllArc(elr).arcrole
}

final case class NotAllRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = false

  def arcrole: String = BaseSetKey.forNotAllArc(elr).arcrole
}

final case class HypercubeDimensionRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def hypercube: EName = sourceConceptEName

  def dimension: EName = targetConceptEName

  def arcrole: String = BaseSetKey.forHypercubeDimensionArc(elr).arcrole

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
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def dimension: EName = sourceConceptEName

  def domain: EName = targetConceptEName

  def arcrole: String = BaseSetKey.forDimensionDomainArc(elr).arcrole
}

final case class DomainMemberRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DomainAwareRelationship {

  def domain: EName = sourceConceptEName

  def member: EName = targetConceptEName

  def arcrole: String = BaseSetKey.forDomainMemberArc(elr).arcrole
}

final case class DimensionDefaultRelationship(
  docUri: URI,
  elr: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DimensionalRelationship {

  def dimension: EName = sourceConceptEName

  def defaultOfDimension: EName = targetConceptEName

  def arcrole: String = BaseSetKey.forDimensionDefaultArc(elr).arcrole
}

final case class OtherDefinitionRelationship(
  docUri: URI,
  elr: String,
  arcrole: String,
  source: Node.Concept,
  target: Node.Concept,
  nonXLinkArcAttributes: Map[EName, String]) extends DefinitionRelationship

// Generic relationships

sealed trait ElementResourceRelationship extends NonStandardRelationship {

  def target: Node.NonStandardDocumentationResource

  def resource: Node.NonStandardDocumentationResource

  // Assuming link name gen:link and arc name gen:arc

  final def baseSetKey: BaseSetKey = {
    BaseSetKey(ENames.GenArcEName, arcrole, ENames.GenLinkEName, elr)
  }
}

// TODO Element-label and element-reference relationships with other resource element name

final case class ElementLabelRelationship(
  docUri: URI,
  elr: String,
  source: LocatorNode,
  target: Node.ElementLabelResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ElementResourceRelationship {

  def resource: Node.ElementLabelResource = target

  def arcrole: String = baseSetKey.arcrole

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2008/role/label")

  def language: String = {
    resource.langOption.getOrElse(sys.error(s"Missing xml:lang in $target in $docUri"))
  }

  def labelText: String = resource.text
}

final case class ElementReferenceRelationship(
  docUri: URI,
  elr: String,
  source: LocatorNode,
  target: Node.ElementReferenceResource,
  nonXLinkArcAttributes: Map[EName, String]) extends ElementResourceRelationship {

  def resource: Node.ElementReferenceResource = target

  def arcrole: String = baseSetKey.arcrole

  def resourceRole: String = resource.roleOption.getOrElse("http://www.xbrl.org/2008/role/reference")

  def parts: Map[EName, String] = resource.parts
}

// TODO Element-message relationship

final case class OtherNonStandardRelationship(
  docUri: URI,
  baseSetKey: BaseSetKey,
  source: Node,
  target: Node,
  nonXLinkArcAttributes: Map[EName, String]) extends NonStandardRelationship {

  def elr: String = baseSetKey.extLinkRole

  def arcrole: String = baseSetKey.arcrole
}

// Companion objects

object Relationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[Relationship] = {

    (baseSetKey.arcEName.namespaceUriOption, source) match {
      case (Some(Namespaces.LinkNamespace), sourceConcept @ Node.Concept(_)) =>
        StandardRelationship.opt(docUri, baseSetKey, sourceConcept, target, nonXLinkArcAttributes)
      case (Some(ns), _) if ns != Namespaces.LinkNamespace =>
        NonStandardRelationship.opt(docUri, baseSetKey, source, target, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object StandardRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[StandardRelationship] = {

    (baseSetKey.arcEName.namespaceUriOption, target) match {
      case (Some(Namespaces.LinkNamespace), targetConcept @ Node.Concept(_)) =>
        InterConceptRelationship.opt(docUri, baseSetKey, source, targetConcept, nonXLinkArcAttributes)
      case (Some(Namespaces.LinkNamespace), resource: Node.StandardDocumentationResource) =>
        ConceptResourceRelationship.opt(docUri, baseSetKey, source, resource, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object InterConceptRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.Concept,
    nonXLinkArcAttributes: Map[EName, String]): Option[InterConceptRelationship] = {

    baseSetKey.arcEName match {
      case ENames.LinkDefinitionArcEName =>
        DefinitionRelationship.opt(docUri, baseSetKey, source, target, nonXLinkArcAttributes)
      case ENames.LinkPresentationArcEName =>
        PresentationRelationship.opt(docUri, baseSetKey, source, target, nonXLinkArcAttributes)
      case ENames.LinkCalculationArcEName =>
        CalculationRelationship.opt(docUri, baseSetKey, source, target, nonXLinkArcAttributes)
      case _ =>
        None
    }
  }
}

object ConceptResourceRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.StandardDocumentationResource,
    nonXLinkArcAttributes: Map[EName, String]): Option[ConceptResourceRelationship] = {

    (baseSetKey.arcEName, target) match {
      case (ENames.LinkLabelArcEName, resource: Node.ConceptLabelResource) =>
        Some(ConceptLabelRelationship(docUri, baseSetKey.extLinkRole, source, resource, nonXLinkArcAttributes))
      case (ENames.LinkReferenceArcEName, resource: Node.ConceptReferenceResource) =>
        Some(ConceptReferenceRelationship(docUri, baseSetKey.extLinkRole, source, resource, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object DefinitionRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.Concept,
    nonXLinkArcAttributes: Map[EName, String]): Option[DefinitionRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/general-special") =>
        Some(GeneralSpecialRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/essence-alias") =>
        Some(EssenceAliasRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/similar-tuples") =>
        Some(SimilarTuplesRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/requires-element") =>
        Some(RequiresElementRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, _) =>
        DimensionalRelationship.opt(docUri, baseSetKey, source, target, nonXLinkArcAttributes)
          .orElse(Some(OtherDefinitionRelationship(docUri, baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes)))
      case _ =>
        None
    }
  }
}

object PresentationRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.Concept,
    nonXLinkArcAttributes: Map[EName, String]): Option[PresentationRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkPresentationArcEName, "http://www.xbrl.org/2003/arcrole/parent-child") =>
        Some(ParentChildRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkPresentationArcEName, _) =>
        Some(OtherPresentationRelationship(docUri, baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object CalculationRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.Concept,
    nonXLinkArcAttributes: Map[EName, String]): Option[CalculationRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkCalculationArcEName, "http://www.xbrl.org/2003/arcrole/summation-item") =>
        Some(SummationItemRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkCalculationArcEName, _) =>
        Some(OtherCalculationRelationship(docUri, baseSetKey.extLinkRole, baseSetKey.arcrole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object DimensionalRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node.Concept,
    target: Node.Concept,
    nonXLinkArcAttributes: Map[EName, String]): Option[DimensionalRelationship] = {

    (baseSetKey.arcEName, baseSetKey.arcrole) match {
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/hypercube-dimension") =>
        Some(HypercubeDimensionRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-domain") =>
        Some(DimensionDomainRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/domain-member") =>
        Some(DomainMemberRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-default") =>
        Some(DimensionDefaultRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/all") =>
        Some(AllRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case (ENames.LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/notAll") =>
        Some(NotAllRelationship(docUri, baseSetKey.extLinkRole, source, target, nonXLinkArcAttributes))
      case _ =>
        None
    }
  }
}

object NonStandardRelationship {

  def opt(
    docUri: URI,
    baseSetKey: BaseSetKey,
    source: Node,
    target: Node,
    nonXLinkArcAttributes: Map[EName, String]): Option[NonStandardRelationship] = {

    if (baseSetKey.arcEName.namespaceUriOption.contains(Namespaces.LinkNamespace)) {
      None
    } else {
      (baseSetKey.arcrole, source, target) match {
        case ("http://xbrl.org/arcrole/2008/element-label", sourceLoc: LocatorNode, res: Node.ElementLabelResource) =>
          Some(ElementLabelRelationship(docUri, baseSetKey.extLinkRole, sourceLoc, res, nonXLinkArcAttributes))
        case ("http://xbrl.org/arcrole/2008/element-reference", sourceLoc: LocatorNode, res: Node.ElementReferenceResource) =>
          Some(ElementReferenceRelationship(docUri, baseSetKey.extLinkRole, sourceLoc, res, nonXLinkArcAttributes))
        case _ =>
          Some(OtherNonStandardRelationship(docUri, baseSetKey, source, target, nonXLinkArcAttributes))
      }
    }
  }
}
