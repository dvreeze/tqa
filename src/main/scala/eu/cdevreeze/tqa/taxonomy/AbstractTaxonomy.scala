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
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.tqa.queryapi.TaxonomyLike
import eu.cdevreeze.tqa.relationship.InterConceptRelationship
import eu.cdevreeze.tqa.relationship.Relationship
import eu.cdevreeze.tqa.relationship.StandardRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Abstract super-class of different concrete taxonomy classes. It extends the TaxonomyLike partial
 * implementation of the TaxonomyApi query API trait.
 *
 * The passed relationships must be backed by XLink arcs in the underlying taxonomy, or else the
 * instance is corrupt. This is not checked by this abstract class.
 *
 * This class makes sure that looking up schema content by EName is fast. Lookup up global element
 * declarations by URI is also fast.
 *
 * Concrete sub-types may be strict or lenient in enforced requirements on the taxonomy. For example,
 * implementations are free to check or ignore that within a "schema" "target" expanded names of
 * global element declarations, type definitions etc. must be unique.
 *
 * @author Chris de Vreeze
 */
abstract class AbstractTaxonomy(
    val underlyingTaxo: Taxonomy,
    val substitutionGroupMap: SubstitutionGroupMap,
    val relationships: immutable.IndexedSeq[Relationship]) extends TaxonomyLike {

  protected val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)

  final val conceptDeclarationsByEName: Map[EName, ConceptDeclaration] = {
    (underlyingTaxo.globalElementDeclarationMap.toSeq collect {
      case (ename, decl) if conceptDeclarationBuilder.optConceptDeclaration(decl).isDefined =>
        (ename -> conceptDeclarationBuilder.optConceptDeclaration(decl).get)
    }).toMap
  }

  final val standardRelationshipsBySource: Map[EName, immutable.IndexedSeq[StandardRelationship]] = {
    relationships collect { case rel: StandardRelationship => rel } groupBy (_.sourceConceptEName)
  }

  final val interConceptRelationshipsBySource: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
    relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.sourceConceptEName)
  }

  final val interConceptRelationshipsByTarget: Map[EName, immutable.IndexedSeq[InterConceptRelationship]] = {
    relationships collect { case rel: InterConceptRelationship => rel } groupBy (_.targetConceptEName)
  }

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
}
