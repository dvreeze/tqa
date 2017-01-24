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

package eu.cdevreeze.tqa.taxonomy

import java.net.URI

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.ConceptDeclaration
import eu.cdevreeze.tqa.dom.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.dom.NamedTypeDefinition
import eu.cdevreeze.tqa.dom.Taxonomy
import eu.cdevreeze.tqa.dom.XLinkArc
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.tqa.queryapi.TaxonomyLike
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.relationship.Relationship
import eu.cdevreeze.tqa.relationship.RelationshipsFactory
import eu.cdevreeze.tqa.relationship.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Basic implementation of a taxonomy that offers the TaxonomyApi query API. It does not enforce closure
 * under DTS discovery rules, or uniqueness of "target expanded names" of concept declarations etc.
 * It does not know anything about tables and formulas. It also does not know anything about networks
 * of relationships.
 *
 * The passed relationships must be backed by XLink arcs in the underlying taxonomy, or else the
 * instance is corrupt. This is not checked by this class.
 *
 * This object is expensive to create (through the build method), primarily due to the mappings from source
 * and target concepts to standard relationships. Looking up schema content by EName (or by URI for global
 * element declarations) is also fast.
 *
 * @author Chris de Vreeze
 */
final class BasicTaxonomy private (
    val underlyingTaxo: Taxonomy,
    val substitutionGroupMap: SubstitutionGroupMap,
    val relationships: immutable.IndexedSeq[Relationship],
    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration],
    val standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]],
    val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]],
    val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]]) extends TaxonomyLike {

  final def findAllXsdSchemas: immutable.IndexedSeq[XsdSchema] = {
    underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[XsdSchema]))
  }

  final def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
    underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration]))
  }

  final def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration] = {
    underlyingTaxo.findGlobalElementDeclarationByEName(ename)
  }

  final def findGlobalElementDeclarationByUri(uri: URI): Option[GlobalElementDeclaration] = {
    underlyingTaxo.findElemByUri(uri) collectFirst { case decl: GlobalElementDeclaration => decl }
  }

  final def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration] = {
    underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalAttributeDeclaration]))
  }

  final def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration] = {
    underlyingTaxo.findGlobalAttributeDeclarationByEName(ename)
  }

  final def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition] = {
    underlyingTaxo.rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[NamedTypeDefinition]))
  }

  final def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition] = {
    underlyingTaxo.findNamedTypeDefinitionByEName(ename)
  }

  final def findConceptDeclaration(ename: EName): Option[ConceptDeclaration] = {
    conceptDeclarationsByEName.get(ename)
  }

  final def findAllStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    relationships collect { case rel: A => rel }
  }

  final def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    relationships collect { case rel: A => rel }
  }

  /**
   * Creates a "sub-taxonomy" in which only the given document URIs occur.
   * It can be used for a specific entrypoint DTS, or to make query methods (not taking an EName) cheaper.
   */
  def filterDocumentUris(docUris: Set[URI]): BasicTaxonomy = {
    new BasicTaxonomy(
      underlyingTaxo.filterDocumentUris(docUris),
      substitutionGroupMap,
      relationships.filter(rel => docUris.contains(rel.docUri)),
      conceptDeclarationsByEName.filter(kv => docUris.contains(kv._2.globalElementDeclaration.docUri)),
      standardRelationshipsBySource.mapValues(_.filter(rel => docUris.contains(rel.docUri))).filter(_._2.nonEmpty),
      interConceptRelationshipsBySource.mapValues(_.filter(rel => docUris.contains(rel.docUri))).filter(_._2.nonEmpty),
      interConceptRelationshipsByTarget.mapValues(_.filter(rel => docUris.contains(rel.docUri))).filter(_._2.nonEmpty))
  }

  /**
   * Creates a "sub-taxonomy" in which only relationships passing the filter occur.
   * Schema and linkbase DOM content remains the same. Only relationships are filtered.
   * It can be used to make query methods (not taking an EName) cheaper.
   */
  def filterRelationships(p: Relationship => Boolean): BasicTaxonomy = {
    new BasicTaxonomy(
      underlyingTaxo,
      substitutionGroupMap,
      relationships.filter(p),
      conceptDeclarationsByEName,
      standardRelationshipsBySource.mapValues(_.filter(p)).filter(_._2.nonEmpty),
      interConceptRelationshipsBySource.mapValues(_.filter(p)).filter(_._2.nonEmpty),
      interConceptRelationshipsByTarget.mapValues(_.filter(p)).filter(_._2.nonEmpty))
  }
}

object BasicTaxonomy {

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   * This method invokes the overloaded build method having as 4th parameter the arc filter that always returns true.
   */
  def build(
    underlyingTaxo: Taxonomy,
    substitutionGroupMap: SubstitutionGroupMap,
    relationshipsFactory: RelationshipsFactory): BasicTaxonomy = {

    build(underlyingTaxo, substitutionGroupMap, relationshipsFactory, _ => true)
  }

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   * This method first extracts relationships from the underlying taxonomy, and then calls the overloaded
   * build method that takes as parameters the underlying taxonomy, substitution group map, and extracted
   * relationships.
   *
   * The arc filter is only used during relationship extraction. It is not used to filter any taxonomy DOM content.
   */
  def build(
    underlyingTaxo: Taxonomy,
    substitutionGroupMap: SubstitutionGroupMap,
    relationshipsFactory: RelationshipsFactory,
    arcFilter: XLinkArc => Boolean): BasicTaxonomy = {

    val relationships = relationshipsFactory.extractRelationships(underlyingTaxo, arcFilter)
    build(underlyingTaxo, substitutionGroupMap, relationships)
  }

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   * Make sure that the relationships are backed by arcs in the underlying taxonomy. This is not checked.
   */
  def build(
    underlyingTaxo: Taxonomy,
    substitutionGroupMap: SubstitutionGroupMap,
    relationships: immutable.IndexedSeq[Relationship]): BasicTaxonomy = {

    val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)

    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration] = {
      (underlyingTaxo.globalElementDeclarationMap.toSeq collect {
        case (ename, decl) if conceptDeclarationBuilder.optConceptDeclaration(decl).isDefined =>
          (ename -> conceptDeclarationBuilder.optConceptDeclaration(decl).get)
      }).toMap
    }

    val standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]] = {
      relationships collect { case rel: StandardRelationship => rel } groupBy (_.sourceConceptEName)
    }

    val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.sourceConceptEName)
    }

    val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.targetConceptEName)
    }

    new BasicTaxonomy(
      underlyingTaxo,
      substitutionGroupMap,
      relationships,
      conceptDeclarationsByEName,
      standardRelationshipsBySource,
      interConceptRelationshipsBySource,
      interConceptRelationshipsByTarget)
  }
}
