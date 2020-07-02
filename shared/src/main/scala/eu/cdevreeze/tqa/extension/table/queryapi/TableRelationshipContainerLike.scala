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
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
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
 * Partial implementation of `TableRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait TableRelationshipContainerLike extends TableRelationshipContainerApi {

  // Abstract methods

  /**
   * Returns a collection of table relationships. Must be fast in order for this trait to be fast.
   */
  def tableRelationships: immutable.IndexedSeq[TableRelationship]

  /**
   * Returns a map from source resource fragment keys to table relationships. Must be fast in order for this trait to be fast.
   */
  def tableRelationshipsBySource: Map[XmlFragmentKey, immutable.IndexedSeq[TableRelationship]]

  // Concrete methods

  // Generic query API methods for table relationships

  final def findAllTableRelationshipsOfType[A <: TableRelationship](
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    tableRelationships.collect { case rel: A => rel }
  }

  final def filterTableRelationshipsOfType[A <: TableRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    findAllTableRelationshipsOfType(relationshipType).filter(p)
  }

  final def findAllOutgoingTableRelationshipsOfType[A <: TableRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterOutgoingTableRelationshipsOfType(sourceResource, relationshipType)(_ => true)
  }

  final def filterOutgoingTableRelationshipsOfType[A <: TableRelationship](
    sourceResource: XLinkResource,
    relationshipType: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = relationshipType
    tableRelationshipsBySource.getOrElse(sourceResource.key, Vector()).collect { case relationship: A if p(relationship) => relationship }
  }

  // Query API methods for specific table relationships (likely to be implemented in terms of the methods above)

  final def findAllTableBreakdownRelationships: immutable.IndexedSeq[TableBreakdownRelationship] = {
    findAllTableRelationshipsOfType(classTag[TableBreakdownRelationship])
  }

  final def filterTableBreakdownRelationships(
    p: TableBreakdownRelationship => Boolean): immutable.IndexedSeq[TableBreakdownRelationship] = {

    filterTableRelationshipsOfType(classTag[TableBreakdownRelationship])(p)
  }

  final def findAllOutgoingTableBreakdownRelationships(
    table: Table): immutable.IndexedSeq[TableBreakdownRelationship] = {

    findAllOutgoingTableRelationshipsOfType(table, classTag[TableBreakdownRelationship])
  }

  final def filterOutgoingTableBreakdownRelationships(
    table: Table)(p: TableBreakdownRelationship => Boolean): immutable.IndexedSeq[TableBreakdownRelationship] = {

    filterOutgoingTableRelationshipsOfType(table, classTag[TableBreakdownRelationship])(p)
  }

  final def findAllOutgoingBreakdownTreeRelationships(
    breakdown: TableBreakdown): immutable.IndexedSeq[BreakdownTreeRelationship] = {

    findAllOutgoingTableRelationshipsOfType(breakdown, classTag[BreakdownTreeRelationship])
  }

  final def filterOutgoingBreakdownTreeRelationships(
    breakdown: TableBreakdown)(p: BreakdownTreeRelationship => Boolean): immutable.IndexedSeq[BreakdownTreeRelationship] = {

    filterOutgoingTableRelationshipsOfType(breakdown, classTag[BreakdownTreeRelationship])(p)
  }

  final def findAllOutgoingDefinitionNodeSubtreeRelationships(
    node: DefinitionNode): immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] = {

    findAllOutgoingTableRelationshipsOfType(node, classTag[DefinitionNodeSubtreeRelationship])
  }

  final def filterOutgoingDefinitionNodeSubtreeRelationships(
    node: DefinitionNode)(p: DefinitionNodeSubtreeRelationship => Boolean): immutable.IndexedSeq[DefinitionNodeSubtreeRelationship] = {

    filterOutgoingTableRelationshipsOfType(node, classTag[DefinitionNodeSubtreeRelationship])(p)
  }

  final def findAllOutgoingTableFilterRelationships(
    table: Table): immutable.IndexedSeq[TableFilterRelationship] = {

    findAllOutgoingTableRelationshipsOfType(table, classTag[TableFilterRelationship])
  }

  final def filterOutgoingTableFilterRelationships(
    table: Table)(p: TableFilterRelationship => Boolean): immutable.IndexedSeq[TableFilterRelationship] = {

    filterOutgoingTableRelationshipsOfType(table, classTag[TableFilterRelationship])(p)
  }

  final def findAllOutgoingTableParameterRelationships(
    table: Table): immutable.IndexedSeq[TableParameterRelationship] = {

    findAllOutgoingTableRelationshipsOfType(table, classTag[TableParameterRelationship])
  }

  final def filterOutgoingTableParameterRelationships(
    table: Table)(p: TableParameterRelationship => Boolean): immutable.IndexedSeq[TableParameterRelationship] = {

    filterOutgoingTableRelationshipsOfType(table, classTag[TableParameterRelationship])(p)
  }
}
