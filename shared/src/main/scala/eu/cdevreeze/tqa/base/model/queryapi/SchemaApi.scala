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

package eu.cdevreeze.tqa.base.model.queryapi

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.model.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.base.model.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.model.NamedComplexTypeDefinition
import eu.cdevreeze.tqa.base.model.NamedSimpleTypeDefinition
import eu.cdevreeze.tqa.base.model.NamedTypeDefinition
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a schema API. It offers methods to regard a taxonomy as a collection
 * of schema content, without any knowledge about XBRL in particular.
 *
 * Implementations should make sure that looking up schema content by EName is fast. Lookup up
 * global element declarations by URI must also be fast.
 *
 * Only methods for querying "global" schema content are offered. The returned objects themselves
 * can be used to query for nested content.
 *
 * @author Chris de Vreeze
 */
trait SchemaApi {

  // Known substitution groups

  /**
   * Returns the known substitution groups as SubstitutionGroupMap. If the taxonomy is closed under
   * DTS discovery, these substitution groups are found within the taxonomy. Otherwise they may
   * partly be external.
   *
   * Implementations should store this as a field, in order to make substitution group lookups as
   * fast as possible.
   */
  def substitutionGroupMap: SubstitutionGroupMap

  // Global element declarations, across documents

  def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration]

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): immutable.IndexedSeq[GlobalElementDeclaration]

  def filterGlobalElementDeclarationsOnOwnSubstitutionGroup(p: EName => Boolean): immutable.IndexedSeq[GlobalElementDeclaration]

  def filterGlobalElementDeclarationsOnOwnOrInheritedSubstitutionGroup(sg: EName): immutable.IndexedSeq[GlobalElementDeclaration]

  def findGlobalElementDeclaration(p: GlobalElementDeclaration => Boolean): Option[GlobalElementDeclaration]

  def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration]

  def getGlobalElementDeclaration(ename: EName): GlobalElementDeclaration

  def findGlobalElementDeclarationByUri(uri: URI): Option[GlobalElementDeclaration]

  def getGlobalElementDeclarationByUri(uri: URI): GlobalElementDeclaration

  // Global attribute declarations, across documents

  def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration]

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): immutable.IndexedSeq[GlobalAttributeDeclaration]

  def findGlobalAttributeDeclaration(p: GlobalAttributeDeclaration => Boolean): Option[GlobalAttributeDeclaration]

  def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration]

  def getGlobalAttributeDeclaration(ename: EName): GlobalAttributeDeclaration

  // Named type definitions, across documents

  def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition]

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): immutable.IndexedSeq[NamedTypeDefinition]

  def findNamedTypeDefinition(p: NamedTypeDefinition => Boolean): Option[NamedTypeDefinition]

  def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition]

  def getNamedTypeDefinition(ename: EName): NamedTypeDefinition

  // Named complex type definitions, across documents

  def findAllNamedComplexTypeDefinitions: immutable.IndexedSeq[NamedComplexTypeDefinition]

  def filterNamedComplexTypeDefinitions(p: NamedComplexTypeDefinition => Boolean): immutable.IndexedSeq[NamedComplexTypeDefinition]

  def findNamedComplexTypeDefinition(p: NamedComplexTypeDefinition => Boolean): Option[NamedComplexTypeDefinition]

  def findNamedComplexTypeDefinition(ename: EName): Option[NamedComplexTypeDefinition]

  def getNamedComplexTypeDefinition(ename: EName): NamedComplexTypeDefinition

  // Named simple type definitions, across documents

  def findAllNamedSimpleTypeDefinitions: immutable.IndexedSeq[NamedSimpleTypeDefinition]

  def filterNamedSimpleTypeDefinitions(p: NamedSimpleTypeDefinition => Boolean): immutable.IndexedSeq[NamedSimpleTypeDefinition]

  def findNamedSimpleTypeDefinition(p: NamedSimpleTypeDefinition => Boolean): Option[NamedSimpleTypeDefinition]

  def findNamedSimpleTypeDefinition(ename: EName): Option[NamedSimpleTypeDefinition]

  def getNamedSimpleTypeDefinition(ename: EName): NamedSimpleTypeDefinition

  // Finding ancestry of types, across documents

  /**
   * If the given type obeys the type predicate, returns it, wrapped in an Option.
   * Otherwise, returns the optional base type if that type obeys the type predicate, and so on,
   * until either the predicate holds or no further base type can be found in the taxonomy.
   */
  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName]
}
