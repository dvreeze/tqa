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

import eu.cdevreeze.tqa.ENames.XmlLangEName
import eu.cdevreeze.tqa.dom.CalculationArc
import eu.cdevreeze.tqa.dom.ConceptLabelResource
import eu.cdevreeze.tqa.dom.ConceptReferenceResource
import eu.cdevreeze.tqa.dom.DefinitionArc
import eu.cdevreeze.tqa.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.dom.LabelArc
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

  final def docUri: URI = arc.backingElem.docUri

  final def baseUri: URI = arc.backingElem.baseUri

  final def elr: String = arc.elr

  final def arcrole: String = arc.arcrole

  final def arcPath: Path = arc.backingElem.path

  final def fromPath: Path = resolvedFrom.xlinkLocatorOrResource.backingElem.path

  final def toPath: Path = resolvedTo.xlinkLocatorOrResource.backingElem.path

  // TODO Key of the relationship (in the network), order, equals and hashCode.
}

sealed abstract class StandardRelationship(
  arc: StandardArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocatorOrResource[_ <: TaxonomyElem]) extends Relationship(arc, resolvedFrom, resolvedTo) {

  final def sourceGlobalElementDeclaration: GlobalElementDeclaration = resolvedFrom.resolvedElem

  final def sourceConceptEName: EName = sourceGlobalElementDeclaration.targetEName
}

sealed abstract class NonStandardRelationship(
  arc: XLinkArc,
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

  // TODO Effective target role and isFollowedBy, to be overridden by dimensional relationships.
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
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DefinitionRelationship(arc, resolvedFrom, resolvedTo)

sealed abstract class HasHypercubeRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo)

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
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo)

final class DimensionDomainRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo)

final class DomainMemberRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo)

final class DimensionDefaultRelationship(
  arc: DefinitionArc,
  resolvedFrom: ResolvedLocator[_ <: GlobalElementDeclaration],
  resolvedTo: ResolvedLocator[_ <: GlobalElementDeclaration]) extends DimensionalRelationship(arc, resolvedFrom, resolvedTo)

// Generic relationships

sealed abstract class ElementResourceRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)

final class ElementLabelRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

final class ElementReferenceRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends ElementResourceRelationship(arc, resolvedFrom, resolvedTo)

final class ElementMessageRelationship(
  arc: XLinkArc,
  resolvedFrom: ResolvedLocatorOrResource[_ <: TaxonomyElem],
  resolvedTo: ResolvedLocatorOrResource[_ <: XLinkResource]) extends NonStandardRelationship(arc, resolvedFrom, resolvedTo)
