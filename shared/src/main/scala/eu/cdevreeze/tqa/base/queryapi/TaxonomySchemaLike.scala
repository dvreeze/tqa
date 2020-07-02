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

package eu.cdevreeze.tqa.base.queryapi

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.base.dom.ConceptDeclaration
import eu.cdevreeze.tqa.base.dom.DimensionDeclaration
import eu.cdevreeze.tqa.base.dom.ExplicitDimensionDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.HypercubeDeclaration
import eu.cdevreeze.tqa.base.dom.ItemDeclaration
import eu.cdevreeze.tqa.base.dom.PrimaryItemDeclaration
import eu.cdevreeze.tqa.base.dom.TupleDeclaration
import eu.cdevreeze.tqa.base.dom.TypedDimensionDeclaration
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of trait `TaxonomySchemaApi`.
 *
 * @author Chris de Vreeze
 */
trait TaxonomySchemaLike extends TaxonomySchemaApi with SchemaLike {

  // Abstract methods

  def conceptDeclarations: immutable.IndexedSeq[ConceptDeclaration]

  /**
   * Finds the optional concept declaration with the given target expanded name (name plus target
   * namespace). Make sure the implementation of this method is very fast, thus ensuring that the
   * other lookup methods on target expanded name are very fast as well.
   */
  def findConceptDeclaration(ename: EName): Option[ConceptDeclaration]

  // Concept declarations, across documents

  final def findAllConceptDeclarations: immutable.IndexedSeq[ConceptDeclaration] = {
    conceptDeclarations
  }

  final def filterConceptDeclarations(p: ConceptDeclaration => Boolean): immutable.IndexedSeq[ConceptDeclaration] = {
    findAllConceptDeclarations.filter(p)
  }

  final def findConceptDeclaration(p: ConceptDeclaration => Boolean): Option[ConceptDeclaration] = {
    findAllConceptDeclarations.find(p)
  }

  final def getConceptDeclaration(ename: EName): ConceptDeclaration = {
    findConceptDeclaration(ename).getOrElse(sys.error(s"Missing concept declaration for expanded name $ename"))
  }

  // Item declarations, across documents

  final def findAllItemDeclarations: immutable.IndexedSeq[ItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ItemDeclaration => decl }
  }

  final def filterItemDeclarations(p: ItemDeclaration => Boolean): immutable.IndexedSeq[ItemDeclaration] = {
    findAllItemDeclarations.filter(p)
  }

  final def findItemDeclaration(p: ItemDeclaration => Boolean): Option[ItemDeclaration] = {
    findAllItemDeclarations.find(p)
  }

  final def findItemDeclaration(ename: EName): Option[ItemDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: ItemDeclaration => decl }
  }

  final def getItemDeclaration(ename: EName): ItemDeclaration = {
    findItemDeclaration(ename).getOrElse(sys.error(s"Missing item declaration for expanded name $ename"))
  }

  // Tuple declarations, across documents

  final def findAllTupleDeclarations: immutable.IndexedSeq[TupleDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TupleDeclaration => decl }
  }

  final def filterTupleDeclarations(p: TupleDeclaration => Boolean): immutable.IndexedSeq[TupleDeclaration] = {
    findAllTupleDeclarations.filter(p)
  }

  final def findTupleDeclaration(p: TupleDeclaration => Boolean): Option[TupleDeclaration] = {
    findAllTupleDeclarations.find(p)
  }

  final def findTupleDeclaration(ename: EName): Option[TupleDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: TupleDeclaration => decl }
  }

  final def getTupleDeclaration(ename: EName): TupleDeclaration = {
    findTupleDeclaration(ename).getOrElse(sys.error(s"Missing tuple declaration for expanded name $ename"))
  }

  // Primary item declarations, across documents

  final def findAllPrimaryItemDeclarations: immutable.IndexedSeq[PrimaryItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: PrimaryItemDeclaration => decl }
  }

  final def filterPrimaryItemDeclarations(p: PrimaryItemDeclaration => Boolean): immutable.IndexedSeq[PrimaryItemDeclaration] = {
    findAllPrimaryItemDeclarations.filter(p)
  }

  final def findPrimaryItemDeclaration(p: PrimaryItemDeclaration => Boolean): Option[PrimaryItemDeclaration] = {
    findAllPrimaryItemDeclarations.find(p)
  }

  final def findPrimaryItemDeclaration(ename: EName): Option[PrimaryItemDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: PrimaryItemDeclaration => decl }
  }

  final def getPrimaryItemDeclaration(ename: EName): PrimaryItemDeclaration = {
    findPrimaryItemDeclaration(ename).getOrElse(sys.error(s"Missing primary item declaration for expanded name $ename"))
  }

  // Hypercube declarations, across documents

  final def findAllHypercubeDeclarations: immutable.IndexedSeq[HypercubeDeclaration] = {
    findAllConceptDeclarations.collect { case decl: HypercubeDeclaration => decl }
  }

  final def filterHypercubeDeclarations(p: HypercubeDeclaration => Boolean): immutable.IndexedSeq[HypercubeDeclaration] = {
    findAllHypercubeDeclarations.filter(p)
  }

  final def findHypercubeDeclaration(p: HypercubeDeclaration => Boolean): Option[HypercubeDeclaration] = {
    findAllHypercubeDeclarations.find(p)
  }

  final def findHypercubeDeclaration(ename: EName): Option[HypercubeDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: HypercubeDeclaration => decl }
  }

  final def getHypercubeDeclaration(ename: EName): HypercubeDeclaration = {
    findHypercubeDeclaration(ename).getOrElse(sys.error(s"Missing hypercube declaration for expanded name $ename"))
  }

  // Dimension declarations, across documents

  final def findAllDimensionDeclarations: immutable.IndexedSeq[DimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: DimensionDeclaration => decl }
  }

  final def filterDimensionDeclarations(p: DimensionDeclaration => Boolean): immutable.IndexedSeq[DimensionDeclaration] = {
    findAllDimensionDeclarations.filter(p)
  }

  final def findDimensionDeclaration(p: DimensionDeclaration => Boolean): Option[DimensionDeclaration] = {
    findAllDimensionDeclarations.find(p)
  }

  final def findDimensionDeclaration(ename: EName): Option[DimensionDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: DimensionDeclaration => decl }
  }

  final def getDimensionDeclaration(ename: EName): DimensionDeclaration = {
    findDimensionDeclaration(ename).getOrElse(sys.error(s"Missing dimension declaration for expanded name $ename"))
  }

  // Explicit dimension declarations, across documents

  final def findAllExplicitDimensionDeclarations: immutable.IndexedSeq[ExplicitDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ExplicitDimensionDeclaration => decl }
  }

  final def filterExplicitDimensionDeclarations(p: ExplicitDimensionDeclaration => Boolean): immutable.IndexedSeq[ExplicitDimensionDeclaration] = {
    findAllExplicitDimensionDeclarations.filter(p)
  }

  final def findExplicitDimensionDeclaration(p: ExplicitDimensionDeclaration => Boolean): Option[ExplicitDimensionDeclaration] = {
    findAllExplicitDimensionDeclarations.find(p)
  }

  final def findExplicitDimensionDeclaration(ename: EName): Option[ExplicitDimensionDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: ExplicitDimensionDeclaration => decl }
  }

  final def getExplicitDimensionDeclaration(ename: EName): ExplicitDimensionDeclaration = {
    findExplicitDimensionDeclaration(ename).getOrElse(sys.error(s"Missing explicit dimension declaration for expanded name $ename"))
  }

  // Typed dimension declarations, across documents

  final def findAllTypedDimensionDeclarations: immutable.IndexedSeq[TypedDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TypedDimensionDeclaration => decl }
  }

  final def filterTypedDimensionDeclarations(p: TypedDimensionDeclaration => Boolean): immutable.IndexedSeq[TypedDimensionDeclaration] = {
    findAllTypedDimensionDeclarations.filter(p)
  }

  final def findTypedDimensionDeclaration(p: TypedDimensionDeclaration => Boolean): Option[TypedDimensionDeclaration] = {
    findAllTypedDimensionDeclarations.find(p)
  }

  final def findTypedDimensionDeclaration(ename: EName): Option[TypedDimensionDeclaration] = {
    findConceptDeclaration(ename).collectFirst { case decl: TypedDimensionDeclaration => decl }
  }

  final def getTypedDimensionDeclaration(ename: EName): TypedDimensionDeclaration = {
    findTypedDimensionDeclaration(ename).getOrElse(sys.error(s"Missing typed dimension declaration for expanded name $ename"))
  }

  // Typed dimension member declarations

  final def findMemberDeclarationOfTypedDimension(typedDimension: EName): Option[GlobalElementDeclaration] = {
    val typedDimensionDeclOption = findTypedDimensionDeclaration(typedDimension)

    val typedDomainRefOption: Option[URI] = typedDimensionDeclOption.map(e => e.typedDomainRef)

    typedDomainRefOption.flatMap(uri => findGlobalElementDeclarationByUri(uri))
  }

  final def getMemberDeclarationOfTypedDimension(typedDimension: EName): GlobalElementDeclaration = {
    findMemberDeclarationOfTypedDimension(typedDimension).getOrElse(sys.error(s"Missing member declaration for typed dimension $typedDimension"))
  }
}
