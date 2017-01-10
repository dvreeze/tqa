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

package eu.cdevreeze.tqa.queryapi

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.dom.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.dom.NamedTypeDefinition
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.yaidom.core.EName

/**
 * Purely abstract trait offering a schema API. It offers methods to regard a taxonomy as a collection
 * of schema documents, without any knowledge about XBRL in particular.
 *
 * Implementations should make sure that looking up schema content by EName is fast. Lookup up
 * global element declarations by URI must also be fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the schema. For example,
 * implementations are free to check or ignore that within a "schema" "target" expanded names of
 * global element declarations, type definitions etc. must be unique.
 *
 * Only methods for querying "global" schema content are offered. The returned objects themselves
 * can be used to query for nested content.
 *
 * @author Chris de Vreeze
 */
trait SchemaApi {

  // Schema root elements

  def findAllXsdSchemas: immutable.IndexedSeq[XsdSchema]

  def filterXsdSchemas(p: XsdSchema => Boolean): immutable.IndexedSeq[XsdSchema]

  def findXsdSchema(p: XsdSchema => Boolean): Option[XsdSchema]

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

  def filterGlobalElementDeclarationsInSubstitutionGroup(sg: EName): immutable.IndexedSeq[GlobalElementDeclaration]

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

  // TODO Methods to find ancestry of types

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
