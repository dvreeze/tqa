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

package eu.cdevreeze.tqa.base.model.taxonomy

import scala.collection.immutable
import scala.collection.compat._
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.model.ConceptDeclaration
import eu.cdevreeze.tqa.base.model.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.base.model.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.model.NamedTypeDefinition
import eu.cdevreeze.tqa.base.model.queryapi.TaxonomyLike
import eu.cdevreeze.tqa.base.model.InterConceptRelationship
import eu.cdevreeze.tqa.base.model.Node
import eu.cdevreeze.tqa.base.model.NonStandardRelationship
import eu.cdevreeze.tqa.base.model.Relationship
import eu.cdevreeze.tqa.base.model.SchemaContentElement
import eu.cdevreeze.tqa.base.model.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Basic implementation of a taxonomy that offers the TaxonomyApi query API. It does not enforce
 * uniqueness of "target expanded names" of concept declarations etc.
 *
 * It knows nothing about DTSes and closure under DTS discovery rules.
 *
 * It does not know anything about tables and formulas. It also does not know anything about networks
 * of relationships.
 *
 * This object is expensive to create (through the build method), primarily due to the mappings from source
 * and target concepts to standard relationships. Looking up schema content by EName is also fast.
 *
 * @author Chris de Vreeze
 */
final class BasicTaxonomy private (
  val extraSubstitutionGroupMap: SubstitutionGroupMap,
  val netSubstitutionGroupMap: SubstitutionGroupMap,
  val topmostSchemaContentElements: immutable.IndexedSeq[SchemaContentElement],
  val topmostSchemaContentElementsByENameAndId: Map[EName, Map[String, immutable.IndexedSeq[SchemaContentElement]]],
  val relationships: immutable.IndexedSeq[Relationship],
  val globalElementDeclarationsByEName: Map[EName, GlobalElementDeclaration],
  val globalAttributeDeclarationsByEName: Map[EName, GlobalAttributeDeclaration],
  val namedTypeDefinitionsByEName: Map[EName, NamedTypeDefinition],
  val conceptDeclarationsByEName: Map[EName, ConceptDeclaration],
  val globalElementDeclarationsById: Map[String, immutable.IndexedSeq[GlobalElementDeclaration]],
  val standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]],
  val nonStandardRelationshipsBySource: Map[Node, immutable.IndexedSeq[NonStandardRelationship]],
  val nonStandardRelationshipsByTarget: Map[Node, immutable.IndexedSeq[NonStandardRelationship]],
  val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]],
  val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]]) extends TaxonomyLike {

  def substitutionGroupMap: SubstitutionGroupMap = netSubstitutionGroupMap

  def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
    topmostSchemaContentElements.collect { case e: GlobalElementDeclaration => e }
  }

  def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration] = {
    globalElementDeclarationsByEName.get(ename)
  }

  def findGlobalElementDeclarationById(id: String): Option[GlobalElementDeclaration] = {
    globalElementDeclarationsById.getOrElse(id, immutable.IndexedSeq()).headOption
  }

  def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration] = {
    topmostSchemaContentElements.collect { case e: GlobalAttributeDeclaration => e }
  }

  def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration] = {
    globalAttributeDeclarationsByEName.get(ename)
  }

  def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition] = {
    topmostSchemaContentElements.collect { case e: NamedTypeDefinition => e }
  }

  def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition] = {
    namedTypeDefinitionsByEName.get(ename)
  }

  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName] = {
    if (p(typeEName)) {
      Some(typeEName)
    } else {
      val typeDefinitionOption = findNamedTypeDefinition(typeEName)

      val baseTypeOption = typeDefinitionOption.flatMap(_.baseTypeOption)

      // Recursive call
      baseTypeOption.flatMap(baseType => findBaseTypeOrSelfUntil(baseType, p))
    }
  }

  def findConceptDeclaration(ename: EName): Option[ConceptDeclaration] = {
    conceptDeclarationsByEName.get(ename)
  }

  def findAllStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    relationships collect { case rel: A => rel }
  }

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    relationships collect { case rel: A => rel }
  }

  def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    relationships collect { case rel: A => rel }
  }

  // TODO Filtering taxonomies, returning "sub"-taxonomies.
  // TODO Prohibition/overrides.
}

object BasicTaxonomy {

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   */
  def build(
    topmostSchemaContentElements: immutable.IndexedSeq[SchemaContentElement],
    extraSubstitutionGroupMap: SubstitutionGroupMap,
    relationships: immutable.IndexedSeq[Relationship]): BasicTaxonomy = {

    val topmostSchemaContentElementsByENameAndId: Map[EName, Map[String, immutable.IndexedSeq[SchemaContentElement]]] =
      topmostSchemaContentElements.groupBy(_.resolvedName)
        .view.mapValues(_.filter(_.attributes.idOption.nonEmpty).groupBy(_.attributes.idOption.get)).toMap

    val globalElementDeclarationsByEName: Map[EName, GlobalElementDeclaration] =
      topmostSchemaContentElements.collect { case e: GlobalElementDeclaration => e }
        .view.groupBy(_.targetEName).view.mapValues(_.head).toMap

    val globalAttributeDeclarationsByEName: Map[EName, GlobalAttributeDeclaration] =
      topmostSchemaContentElements.collect { case e: GlobalAttributeDeclaration => e }
        .groupBy(_.targetEName).view.mapValues(_.head).toMap

    val namedTypeDefinitionsByEName: Map[EName, NamedTypeDefinition] =
      topmostSchemaContentElements.collect { case e: NamedTypeDefinition => e }
        .groupBy(_.targetEName).view.mapValues(_.head).toMap

    val derivedSubstitutionGroupMap: SubstitutionGroupMap =
      computeDerivedSubstitutionGroupMap(globalElementDeclarationsByEName)

    val netSubstitutionGroupMap = derivedSubstitutionGroupMap.append(extraSubstitutionGroupMap)

    val conceptDeclarationBuilder = new ConceptDeclaration.Builder(netSubstitutionGroupMap)

    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration] = {
      globalElementDeclarationsByEName.toSeq.flatMap {
        case (ename, decl) =>
          conceptDeclarationBuilder.optConceptDeclaration(decl).map(conceptDecl => ename -> conceptDecl)
      }.toMap
    }

    val standardRelationships = relationships.collect { case rel: StandardRelationship => rel }

    val standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]] = {
      standardRelationships groupBy (_.sourceConceptEName)
    }

    val nonStandardRelationships = relationships.collect { case rel: NonStandardRelationship => rel }

    val nonStandardRelationshipsBySource: Map[Node, immutable.IndexedSeq[NonStandardRelationship]] = {
      nonStandardRelationships.groupBy(_.source)
    }

    val nonStandardRelationshipsByTarget: Map[Node, immutable.IndexedSeq[NonStandardRelationship]] = {
      nonStandardRelationships.groupBy(_.target)
    }

    val interConceptRelationships = standardRelationships.collect { case rel: InterConceptRelationship => rel }

    val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      interConceptRelationships groupBy (_.sourceConceptEName)
    }

    val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
      interConceptRelationships groupBy (_.targetConceptEName)
    }

    val globalElementDeclarationsById: Map[String, immutable.IndexedSeq[GlobalElementDeclaration]] =
      topmostSchemaContentElementsByENameAndId.getOrElse(ENames.XsElementEName, Map())
        .view.mapValues(_.collect { case e: GlobalElementDeclaration => e }).toMap

    new BasicTaxonomy(
      extraSubstitutionGroupMap,
      netSubstitutionGroupMap,
      topmostSchemaContentElements,
      topmostSchemaContentElementsByENameAndId,
      relationships,
      globalElementDeclarationsByEName,
      globalAttributeDeclarationsByEName,
      namedTypeDefinitionsByEName,
      conceptDeclarationsByEName,
      globalElementDeclarationsById,
      standardRelationshipsBySource,
      nonStandardRelationshipsBySource,
      nonStandardRelationshipsByTarget,
      interConceptRelationshipsBySource,
      interConceptRelationshipsByTarget)
  }

  /**
   * Returns the SubstitutionGroupMap that can be derived from this taxonomy base alone.
   * This is an expensive operation that should be performed only once, if possible.
   */
  private def computeDerivedSubstitutionGroupMap(globalElementDeclarationMap: Map[EName, GlobalElementDeclaration]): SubstitutionGroupMap = {
    val rawMappings: Map[EName, EName] =
      (globalElementDeclarationMap.toSeq.flatMap {
        case (en, decl) => decl.substitutionGroupOption.map(sg => en -> sg)
      }).toMap

    val substGroups: Set[EName] = rawMappings.values.toSet

    val mappings: Map[EName, EName] = rawMappings.filter(kv => substGroups.contains(kv._1))

    SubstitutionGroupMap.from(mappings)
  }
}
