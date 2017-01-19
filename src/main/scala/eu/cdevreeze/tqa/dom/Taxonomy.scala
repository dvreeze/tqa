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

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.ENames.IdEName
import eu.cdevreeze.yaidom.core.EName

/**
 * Very limited notion of a taxonomy, as a collection of taxonomy root elements. It contains a map from URIs
 * (with fragments) to taxonomy elements, for quick element lookups based on URIs with fragments. It also contains
 * a map from ENames (names with target namespace) of global element declarations and named type definitions.
 *
 * This object is rather expensive to create, building the maps that support fast querying based on URI (with fragment)
 * or "target EName".
 *
 * Taxonomy creation should never fail, if correct URIs are passed. Even the instance methods are very lenient and
 * should never fail. Typically, a taxonomy instantiated as an object of this class has not yet been validated.
 * This taxonomy class has 2 main purposes: support for quick locator resolution when building relationships, and
 * support for quick relationship key computation when determining networks of relationships. Of course, this class
 * can also be used for taxonomy validations tasks, when the taxonomy has not yet been validated at all.
 *
 * @author Chris de Vreeze
 */
final class Taxonomy private (val rootElems: immutable.IndexedSeq[TaxonomyElem]) {
  require(
    rootElems.forall(e => e.docUri.getFragment == null),
    s"Expected document URIs but got at least one URI with fragment")

  /**
   * Somewhat expensive map from URIs without fragments to corresponding root elements. If there are duplicate IDs, data is lost.
   */
  val rootElemUriMap: Map[URI, TaxonomyElem] = {
    rootElems.groupBy(e => e.docUri).mapValues(_.head)
  }

  /**
   * Expensive map from URIs with ID fragments to corresponding elements. If there are duplicate IDs, data is lost.
   */
  val elemUriMap: Map[URI, TaxonomyElem] = {
    rootElems.flatMap(e => getElemUriMap(e).toSeq).toMap
  }

  /**
   * Expensive map from ENames (names with target namespace) of global element declarations to the global element declarations themselves.
   * If there are any global element declarations with duplicate "target ENames", data is lost.
   */
  val globalElementDeclarationMap: Map[EName, GlobalElementDeclaration] = {
    rootElems.flatMap(e => getGlobalElementDeclarationMap(e).toSeq).toMap
  }

  /**
   * Expensive map from ENames (names with target namespace) of named type definitions to the named type definitions themselves.
   * If there are any named type definitions with duplicate "target ENames", data is lost.
   */
  val namedTypeDefinitionMap: Map[EName, NamedTypeDefinition] = {
    rootElems.flatMap(e => getNamedTypeDefinitionMap(e).toSeq).toMap
  }

  /**
   * Expensive map from ENames (names with target namespace) of global attribute declarations to the global attribute declarations themselves.
   * If there are any global element declarations with duplicate "target ENames", data is lost.
   */
  val globalAttributeDeclarationMap: Map[EName, GlobalAttributeDeclaration] = {
    rootElems.flatMap(e => getGlobalAttributeDeclarationMap(e).toSeq).toMap
  }

  /**
   * Finds the (first) optional element with the given URI. The fragment, if any, must be an XPointer or sequence thereof.
   * Only shorthand pointers or non-empty sequences of element scheme XPointers are accepted. If there is no fragment,
   * the first root element with the given document URI is searched for.
   *
   * This is a quick operation for shorthand pointers, which are the most commonly used XPointers in URI fragments anyway.
   *
   * The schema type of the ID attributes is not taken into account, although strictly speaking that is incorrect.
   */
  def findElemByUri(elemUri: URI): Option[TaxonomyElem] = {
    require(elemUri.isAbsolute, s"URI '${elemUri}' is not absolute")

    if (elemUri.getFragment == null) {
      rootElemUriMap.get(elemUri)
    } else {
      val xpointers = XPointer.parseXPointers(elemUri.getFragment)

      xpointers match {
        case ShorthandPointer(_) :: Nil =>
          // Do a fast map lookup on the entire URI with fragment
          elemUriMap.get(elemUri)
        case _ =>
          val rootElemOption = rootElemUriMap.get(removeFragment(elemUri))
          rootElemOption.flatMap(e => XPointer.findElem(e, xpointers))
      }
    }
  }

  /**
   * Finds the (first) optional global element declaration with the given target EName (named with target namespace).
   *
   * This is a quick operation.
   */
  def findGlobalElementDeclarationByEName(targetEName: EName): Option[GlobalElementDeclaration] = {
    globalElementDeclarationMap.get(targetEName)
  }

  /**
   * Finds the (first) optional named type definition with the given target EName (named with target namespace).
   *
   * This is a quick operation.
   */
  def findNamedTypeDefinitionByEName(targetEName: EName): Option[NamedTypeDefinition] = {
    namedTypeDefinitionMap.get(targetEName)
  }

  /**
   * Finds the (first) optional global attribute declaration with the given target EName (named with target namespace).
   *
   * This is a quick operation.
   */
  def findGlobalAttributeDeclarationByEName(targetEName: EName): Option[GlobalAttributeDeclaration] = {
    globalAttributeDeclarationMap.get(targetEName)
  }

  /**
   * If the given type obeys the type predicate, returns it, wrapped in an Option.
   * Otherwise, returns the optional base type if that type obeys the type predicate, and so on,
   * until either the predicate holds or no further base type can be found in the taxonomy.
   */
  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName] = {
    if (p(typeEName)) {
      Some(typeEName)
    } else {
      val typeDefinitionOption = findNamedTypeDefinitionByEName(typeEName)

      val baseTypeOption = typeDefinitionOption.flatMap(_.baseTypeOption)

      // Recursive call
      baseTypeOption.flatMap(baseType => findBaseTypeOrSelfUntil(baseType, p))
    }
  }

  /**
   * Returns true if the DOM tree with the given root element has any duplicate ID attributes.
   * If so, the taxonomy is incorrect, and the map from URIs to elements loses data.
   *
   * The type of the ID attributes is not taken into account, although strictly speaking that is incorrect.
   */
  def hasDuplicateIds(rootElem: TaxonomyElem): Boolean = {
    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)
    val ids = elemsWithId.map(_.attribute(IdEName))

    ids.distinct.size < ids.size
  }

  /**
   * Returns true if the DOM trees combined have any duplicate global element declaration "target ENames".
   * If so, the taxonomy is incorrect, and the map from ENames to global element declarations loses data.
   */
  def hasDuplicateGlobalElementDeclarationENames: Boolean = {
    val globalElementDeclarations =
      rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration]))
    val globalElementDeclarationENames = globalElementDeclarations.map(_.targetEName)

    globalElementDeclarationENames.distinct.size < globalElementDeclarations.size
  }

  /**
   * Returns true if the DOM trees combined have any duplicate named type definition "target ENames".
   * If so, the taxonomy is incorrect, and the map from ENames to named type definitions loses data.
   */
  def hasDuplicateNamedTypeDefinitionENames: Boolean = {
    val namedTypeDefinitions =
      rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[NamedTypeDefinition]))
    val namedTypeDefinitionENames = namedTypeDefinitions.map(_.targetEName)

    namedTypeDefinitionENames.distinct.size < namedTypeDefinitions.size
  }

  private def getElemUriMap(rootElem: TaxonomyElem): Map[URI, TaxonomyElem] = {
    val docUri = rootElem.docUri
    assert(docUri.isAbsolute)

    // The schema type of the ID attributes is not checked! That would be very expensive without any real advantage.

    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)
    elemsWithId.map(e => (makeUriWithIdFragment(e.baseUri, e.attribute(IdEName)) -> e)).toMap
  }

  private def getGlobalElementDeclarationMap(rootElem: TaxonomyElem): Map[EName, GlobalElementDeclaration] = {
    // TODO Speed up by finding the target namespace (per xs:schema) only once.
    val globalElementDeclarations = rootElem.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration])

    globalElementDeclarations.groupBy(_.targetEName).mapValues(_.head)
  }

  private def getNamedTypeDefinitionMap(rootElem: TaxonomyElem): Map[EName, NamedTypeDefinition] = {
    // TODO Speed up by finding the target namespace (per xs:schema) only once.
    val namedTypeDefinitions = rootElem.findAllElemsOrSelfOfType(classTag[NamedTypeDefinition])

    namedTypeDefinitions.groupBy(_.targetEName).mapValues(_.head)
  }

  private def getGlobalAttributeDeclarationMap(rootElem: TaxonomyElem): Map[EName, GlobalAttributeDeclaration] = {
    // TODO Speed up by finding the target namespace (per xs:schema) only once.
    val globalAttributeDeclarations = rootElem.findAllElemsOrSelfOfType(classTag[GlobalAttributeDeclaration])

    globalAttributeDeclarations.groupBy(_.targetEName).mapValues(_.head)
  }

  private def makeUriWithIdFragment(baseUri: URI, idFragment: String): URI = {
    require(baseUri.isAbsolute, s"Expected absolute base URI but got '${baseUri}'")
    new URI(baseUri.getScheme, baseUri.getSchemeSpecificPart, idFragment)
  }

  private def removeFragment(uri: URI): URI = {
    new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
  }
}

object Taxonomy {

  def build(rootElems: immutable.IndexedSeq[TaxonomyElem]): Taxonomy = {
    new Taxonomy(rootElems)
  }
}
