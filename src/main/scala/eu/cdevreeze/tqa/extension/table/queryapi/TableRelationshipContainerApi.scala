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

package eu.cdevreeze.tqa.extension.table.queryapi

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.tqa.extension.table.dom.DefinitionNode
import eu.cdevreeze.tqa.extension.table.dom.Table
import eu.cdevreeze.tqa.extension.table.dom.TableBreakdown
import eu.cdevreeze.tqa.extension.table.relationship.BreakdownTreeRelationship
import eu.cdevreeze.tqa.extension.table.relationship.DefinitionNodeSubtreeRelationship
import eu.cdevreeze.tqa.extension.table.relationship.TableBreakdownRelationship
import eu.cdevreeze.tqa.extension.table.relationship.TableFilterRelationship
import eu.cdevreeze.tqa.extension.table.relationship.TableParameterRelationship
import eu.cdevreeze.tqa.extension.table.relationship.TableRelationship
import eu.cdevreeze.tqa.xlink.XLinkResource

/**
 * Purely abstract trait offering a table relationship query API.
 *
 * Implementations should make sure that looking up relationships by source resource is fast.
 *
 * Implementations may be strict or lenient in enforced requirements on the relationship container.
 *
 * @author Chris de Vreeze
 */
trait TableRelationshipContainerApi {

  // Generic query API methods for table relationships

  def findAllTableRelationshipsOfType[A <: TableRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  def filterTableRelationshipsOfType[A <: TableRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  /**
   * Finds all table relationships of the given type that are outgoing from the given resource.
   */
  def findAllOutgoingTableRelationshipsOfType[A <: TableRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A]

  /**
   * Filters table relationships of the given type that are outgoing from the given resource.
   */
  def filterOutgoingTableRelationshipsOfType[A <: TableRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A]

  // Query API methods for specific table relationships (likely to be implemented in terms of the methods above)

  def findAllTableBreakdownRelationships: immutable.IndexedSeq[TableBreakdownRelationship]

  def filterTableBreakdownRelationships(
    p: TableBreakdownRelationship => Boolean): immutable.IndexedSeq[TableBreakdownRelationship]

  /**
   * Finds all table-breakdown relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableBreakdownRelationships(
    table: Table): immutable.IndexedSeq[TableBreakdownRelationship]

  /**
   * Filters table-breakdown relationships that are outgoing from the given table.
   */
  def filterOutgoingTableBreakdownRelationships(
    table: Table)(p: TableBreakdownRelationship => Boolean): immutable.IndexedSeq[TableBreakdownRelationship]

  /**
   * Finds all breakdown-tree relationships that are outgoing from the given breakdown.
   */
  def findAllOutgoingBreakdownTreeRelationships(
    breakdown: TableBreakdown): immutable.IndexedSeq[BreakdownTreeRelationship]

  /**
   * Filters breakdown-tree relationships that are outgoing from the given breakdown.
   */
  def filterOutgoingBreakdownTreeRelationships(
    breakdown: TableBreakdown)(p: BreakdownTreeRelationship => Boolean): immutable.IndexedSeq[BreakdownTreeRelationship]

  /**
   * Finds all definition-node-subtree relationships that are outgoing from the given node.
   */
  def findAllOutgoingDefinitionNodeSubtreeRelationships(
    node: DefinitionNode): immutable.IndexedSeq[DefinitionNodeSubtreeRelationship]

  /**
   * Filters definition-node-subtree relationships that are outgoing from the given node.
   */
  def filterOutgoingDefinitionNodeSubtreeRelationships(
    node: DefinitionNode)(p: DefinitionNodeSubtreeRelationship => Boolean): immutable.IndexedSeq[DefinitionNodeSubtreeRelationship]

  /**
   * Finds all table-filter relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableFilterRelationships(
    table: Table): immutable.IndexedSeq[TableFilterRelationship]

  /**
   * Filters table-filter relationships that are outgoing from the given table.
   */
  def filterOutgoingTableFilterRelationships(
    table: Table)(p: TableFilterRelationship => Boolean): immutable.IndexedSeq[TableFilterRelationship]

  /**
   * Finds all table-parameter relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableParameterRelationships(
    table: Table): immutable.IndexedSeq[TableParameterRelationship]

  /**
   * Filters table-parameter relationships that are outgoing from the given table.
   */
  def filterOutgoingTableParameterRelationships(
    table: Table)(p: TableParameterRelationship => Boolean): immutable.IndexedSeq[TableParameterRelationship]
}
