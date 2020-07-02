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

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.GlobalAttributeDeclaration
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.NamedComplexTypeDefinition
import eu.cdevreeze.tqa.base.dom.NamedSimpleTypeDefinition
import eu.cdevreeze.tqa.base.dom.NamedTypeDefinition
import eu.cdevreeze.tqa.base.dom.XsdSchema
import eu.cdevreeze.yaidom.core.EName

/**
 * Partial implementation of trait `SchemaApi`.
 *
 * @author Chris de Vreeze
 */
trait SchemaLike extends SchemaApi {

  // Abstract methods

  def findAllXsdSchemas: immutable.IndexedSeq[XsdSchema]

  def substitutionGroupMap: SubstitutionGroupMap

  def findAllGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration]

  def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration]

  def findGlobalElementDeclarationByUri(uri: URI): Option[GlobalElementDeclaration]

  def findAllGlobalAttributeDeclarations: immutable.IndexedSeq[GlobalAttributeDeclaration]

  def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration]

  def findAllNamedTypeDefinitions: immutable.IndexedSeq[NamedTypeDefinition]

  def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition]

  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName]

  // Concrete methods

  // Schema root elements

  final def filterXsdSchemas(p: XsdSchema => Boolean): immutable.IndexedSeq[XsdSchema] = {
    findAllXsdSchemas.filter(p)
  }

  final def findXsdSchema(p: XsdSchema => Boolean): Option[XsdSchema] = {
    findAllXsdSchemas.find(p)
  }

  // Global element declarations, across documents

  final def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): immutable.IndexedSeq[GlobalElementDeclaration] = {
    findAllGlobalElementDeclarations.filter(p)
  }

  final def filterGlobalElementDeclarationsOnOwnSubstitutionGroup(p: EName => Boolean): immutable.IndexedSeq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(e => e.substitutionGroupOption.exists(sg => p(sg)))
  }

  final def filterGlobalElementDeclarationsOnOwnOrInheritedSubstitutionGroup(sg: EName): immutable.IndexedSeq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(e => e.hasSubstitutionGroup(sg, substitutionGroupMap))
  }

  final def findGlobalElementDeclaration(p: GlobalElementDeclaration => Boolean): Option[GlobalElementDeclaration] = {
    findAllGlobalElementDeclarations.find(p)
  }

  final def getGlobalElementDeclaration(ename: EName): GlobalElementDeclaration = {
    findGlobalElementDeclaration(ename).getOrElse(sys.error(s"Missing global element declaration for expanded name $ename"))
  }

  final def getGlobalElementDeclarationByUri(uri: URI): GlobalElementDeclaration = {
    findGlobalElementDeclarationByUri(uri).getOrElse(sys.error(s"Missing global element declaration with URI $uri"))
  }

  final def findNamedTypeOfGlobalElementDeclaration(ename: EName): Option[EName] = {
    val elemDeclOption: Option[GlobalElementDeclaration] = findGlobalElementDeclaration(ename)

    elemDeclOption.flatMap(_.typeOption).orElse {
      elemDeclOption.flatMap(_.substitutionGroupOption).flatMap { sg =>
        // Recursive call
        findNamedTypeOfGlobalElementDeclaration(sg)
      }
    }
  }

  // Global attribute declarations, across documents

  final def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): immutable.IndexedSeq[GlobalAttributeDeclaration] = {
    findAllGlobalAttributeDeclarations.filter(p)
  }

  final def findGlobalAttributeDeclaration(p: GlobalAttributeDeclaration => Boolean): Option[GlobalAttributeDeclaration] = {
    findAllGlobalAttributeDeclarations.find(p)
  }

  final def getGlobalAttributeDeclaration(ename: EName): GlobalAttributeDeclaration = {
    findGlobalAttributeDeclaration(ename).getOrElse(sys.error(s"Missing global attribute declaration for expanded name $ename"))
  }

  // Named type definitions, across documents

  final def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): immutable.IndexedSeq[NamedTypeDefinition] = {
    findAllNamedTypeDefinitions.filter(p)
  }

  final def findNamedTypeDefinition(p: NamedTypeDefinition => Boolean): Option[NamedTypeDefinition] = {
    findAllNamedTypeDefinitions.find(p)
  }

  final def getNamedTypeDefinition(ename: EName): NamedTypeDefinition = {
    findNamedTypeDefinition(ename).getOrElse(sys.error(s"Missing named type definition for expanded name $ename"))
  }

  // Named complex type definitions, across documents

  final def findAllNamedComplexTypeDefinitions: immutable.IndexedSeq[NamedComplexTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case t: NamedComplexTypeDefinition => t }
  }

  final def filterNamedComplexTypeDefinitions(p: NamedComplexTypeDefinition => Boolean): immutable.IndexedSeq[NamedComplexTypeDefinition] = {
    findAllNamedComplexTypeDefinitions.filter(p)
  }

  final def findNamedComplexTypeDefinition(p: NamedComplexTypeDefinition => Boolean): Option[NamedComplexTypeDefinition] = {
    findAllNamedComplexTypeDefinitions.find(p)
  }

  final def findNamedComplexTypeDefinition(ename: EName): Option[NamedComplexTypeDefinition] = {
    findNamedTypeDefinition(ename).collect { case t: NamedComplexTypeDefinition => t }
  }

  final def getNamedComplexTypeDefinition(ename: EName): NamedComplexTypeDefinition = {
    findNamedComplexTypeDefinition(ename).getOrElse(sys.error(s"Missing named type definition for expanded name $ename"))
  }

  // Named simple type definitions, across documents

  final def findAllNamedSimpleTypeDefinitions: immutable.IndexedSeq[NamedSimpleTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case t: NamedSimpleTypeDefinition => t }
  }

  final def filterNamedSimpleTypeDefinitions(p: NamedSimpleTypeDefinition => Boolean): immutable.IndexedSeq[NamedSimpleTypeDefinition] = {
    findAllNamedSimpleTypeDefinitions.filter(p)
  }

  final def findNamedSimpleTypeDefinition(p: NamedSimpleTypeDefinition => Boolean): Option[NamedSimpleTypeDefinition] = {
    findAllNamedSimpleTypeDefinitions.find(p)
  }

  final def findNamedSimpleTypeDefinition(ename: EName): Option[NamedSimpleTypeDefinition] = {
    findNamedTypeDefinition(ename).collect { case t: NamedSimpleTypeDefinition => t }
  }

  final def getNamedSimpleTypeDefinition(ename: EName): NamedSimpleTypeDefinition = {
    findNamedSimpleTypeDefinition(ename).getOrElse(sys.error(s"Missing named type definition for expanded name $ename"))
  }

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
