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

package eu.cdevreeze.tqa.dom

import java.net.URI

import eu.cdevreeze.tqa.ENames.XbrldtDimensionItemEName
import eu.cdevreeze.tqa.ENames.XbrldtHypercubeItemEName
import eu.cdevreeze.tqa.ENames.XbrldtTypedDomainRefEName
import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.ENames.XbrliTupleEName
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.yaidom.core.EName

/**
 * Concept declaration, wrapping a GlobalElementDeclaration. It must be in substitution group xbrli:item or xbrli:tuple,
 * either directly or indirectly.
 *
 * There are no sub-classes for dimension members, because as global element declarations they are not defined in the Dimensions specification.
 *
 * @author Chris de Vreeze
 */
sealed abstract class ConceptDeclaration(val globalElementDeclaration: GlobalElementDeclaration) extends AnyTaxonomyElem {

  final def key: XmlFragmentKey = {
    globalElementDeclaration.key
  }

  final def targetEName: EName = {
    globalElementDeclaration.targetEName
  }

  final override def equals(other: Any): Boolean = other match {
    case other: ConceptDeclaration => globalElementDeclaration == other.globalElementDeclaration
    case _                         => false
  }

  final override def hashCode: Int = {
    globalElementDeclaration.hashCode
  }
}

/**
 * Item declaration. It must be in the xbrli:item substitution group, directly or indirectly.
 */
sealed abstract class ItemDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends ConceptDeclaration(globalElementDeclaration)

/**
 * Tuple declaration. It must be in the xbrli:tuple substitution group, directly or indirectly.
 */
final class TupleDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends ConceptDeclaration(globalElementDeclaration)

/**
 * Primary item declaration. It must be in the xbrli:item substitution group but neither in the xbrldt:hypercubeItem nor in the xbrldt:dimensionItem substitution groups.
 *
 * A primary item may be used as explicit dimension member.
 *
 * Note that in the Dimensions specification, primary item declarations and domain-member declarations have exactly the same
 * definition! Although in a taxonomy the dimensional relationships make clear whether an item plays the role of primary item
 * or of domain-member, here we call each such item declaration a primary item declaration.
 */
final class PrimaryItemDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends ItemDeclaration(globalElementDeclaration)

/**
 * Hypercube declaration. It must be an abstract item declaration in the xbrldt:hypercubeItem substitution group.
 */
final class HypercubeDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends ItemDeclaration(globalElementDeclaration) {

  def hypercubeEName: EName = {
    targetEName
  }
}

/**
 * Dimension declaration. It must be an abstract item declaration in the xbrldt:dimensionItem substitution group.
 */
sealed abstract class DimensionDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends ItemDeclaration(globalElementDeclaration) {

  final def isTyped: Boolean = {
    globalElementDeclaration.attributeOption(XbrldtTypedDomainRefEName).isDefined
  }

  final def dimensionEName: EName = {
    targetEName
  }
}

/**
 * Explicit dimension declaration. It must be a dimension declaration without attribute xbrldt:typedDomainRef, among other requirements.
 */
final class ExplicitDimensionDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends DimensionDeclaration(globalElementDeclaration) {
  require(!isTyped, s"${globalElementDeclaration.targetEName} is typed and therefore not an explicit dimension")
}

/**
 * Typed dimension declaration. It must be a dimension declaration with an attribute xbrldt:typedDomainRef, among other requirements.
 */
final class TypedDimensionDeclaration(globalElementDeclaration: GlobalElementDeclaration) extends DimensionDeclaration(globalElementDeclaration) {
  require(isTyped, s"${globalElementDeclaration.targetEName} is not typed and therefore not a typed dimension")

  /**
   * Returns the value of the xbrldt:typedDomainRef attribute, as absolute (!) URI.
   */
  def typedDomainRef: URI = {
    val rawUri = URI.create(globalElementDeclaration.attribute(XbrldtTypedDomainRefEName))
    globalElementDeclaration.baseUri.resolve(rawUri)
  }
}

object ConceptDeclaration {

  /**
   * Builder of ConceptDeclaration objects, given a SubstitutionGroupMap object.
   */
  final class Builder(val substitutionGroupMap: SubstitutionGroupMap) {

    /**
     * Optionally turns the global element declaration into a ConceptDeclaration, if it is indeed a concept.
     * This creation cannot fail (assuming that the SubstitutionGroupMap cannot be corrupted).
     */
    def optConceptDeclaration(elemDecl: GlobalElementDeclaration): Option[ConceptDeclaration] = {
      val isHypercube = elemDecl.hasSubstitutionGroup(XbrldtHypercubeItemEName, substitutionGroupMap)
      val isDimension = elemDecl.hasSubstitutionGroup(XbrldtDimensionItemEName, substitutionGroupMap)
      val isItem = elemDecl.hasSubstitutionGroup(XbrliItemEName, substitutionGroupMap)
      val isTuple = elemDecl.hasSubstitutionGroup(XbrliTupleEName, substitutionGroupMap)

      require(!isItem || !isTuple, s"A concept (${elemDecl.targetEName}) cannot be both an item and tuple")
      require(!isHypercube || !isDimension, s"A concept (${elemDecl.targetEName}) cannot be both a hypercube and dimension")
      require(isItem || !isHypercube, s"A concept (${elemDecl.targetEName}) cannot be a hypercube but not an item")
      require(isItem || !isDimension, s"A concept (${elemDecl.targetEName}) cannot be a dimension but not an item")

      if (isTuple) {
        Some(new TupleDeclaration(elemDecl))
      } else if (isItem) {
        if (isHypercube) {
          Some(new HypercubeDeclaration(elemDecl))
        } else if (isDimension) {
          if (elemDecl.attributeOption(XbrldtTypedDomainRefEName).isDefined) {
            Some(new TypedDimensionDeclaration(elemDecl))
          } else {
            Some(new ExplicitDimensionDeclaration(elemDecl))
          }
        } else {
          Some(new PrimaryItemDeclaration(elemDecl))
        }
      } else {
        None
      }
    }
  }
}
