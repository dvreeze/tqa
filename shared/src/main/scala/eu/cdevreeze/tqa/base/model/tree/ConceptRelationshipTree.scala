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

package eu.cdevreeze.tqa.base.model.tree

import scala.collection.immutable
import scala.collection.compat._

import eu.cdevreeze.tqa.base.model.ConsecutiveRelationshipPath
import eu.cdevreeze.tqa.base.model.InterConceptRelationship
import eu.cdevreeze.yaidom.core.EName

/**
 * Tree of concepts connected by inter-concept relationships of some (inter-concept) relationship type.
 *
 * TODO Make more interesting.
 *
 * @author Chris de Vreeze
 */
final class ConceptRelationshipTree[R <: InterConceptRelationship] private (
  val sourceConcept: EName,
  val paths: immutable.IndexedSeq[ConsecutiveRelationshipPath[R]],
  val pathsIndexedByConcept: Map[EName, immutable.IndexedSeq[ConsecutiveRelationshipPath[R]]]) {

  assert(paths.forall(_.sourceConcept == sourceConcept))

}

object ConceptRelationshipTree {

  def build[R <: InterConceptRelationship](
    sourceConcept: EName,
    paths: immutable.IndexedSeq[ConsecutiveRelationshipPath[R]]): ConceptRelationshipTree[R] = {

    require(
      paths.forall(_.sourceConcept == sourceConcept),
      s"Not all paths start with the same concept $sourceConcept")

    val pathsIndexedByConcept: Map[EName, immutable.IndexedSeq[ConsecutiveRelationshipPath[R]]] =
      paths.flatMap(p => p.concepts.distinct.map(_ -> p)).groupBy(_._1).view.mapValues(_.map(_._2)).toMap

    new ConceptRelationshipTree(sourceConcept, paths, pathsIndexedByConcept)
  }
}
