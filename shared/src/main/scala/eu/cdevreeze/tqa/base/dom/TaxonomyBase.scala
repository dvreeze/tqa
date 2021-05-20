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

package eu.cdevreeze.tqa.base.dom

import java.net.URI

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.ChildSequencePointer
import eu.cdevreeze.tqa.ENames.IdEName
import eu.cdevreeze.tqa.ENames.XmlBaseEName
import eu.cdevreeze.tqa.IdChildSequencePointer
import eu.cdevreeze.tqa.IdPointer
import eu.cdevreeze.tqa.ShorthandPointer
import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.XPointer
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.ENameProvider
import eu.cdevreeze.yaidom.core.Scope

/**
 * Very limited notion of a taxonomy, as a collection of taxonomy documents. It contains a map from URIs
 * (with fragments) to taxonomy documents, for quick element lookups based on URIs with fragments. It also contains
 * a map from ENames (names with target namespace) of global element declarations and named type definitions.
 *
 * It '''does not understand (resolved) relationships''', and it has no taxonomy query API, but it supports creation of such
 * a taxonomy that does know about relationships and does have a taxonomy query API. In that sense, the reason for this class to
 * exist is mainly its role in creating rich taxonomy objects.
 *
 * Not only does this class not understand (resolved) relationships, it also '''does not know about substitution
 * groups''' and therefore it does not know about concept declarations (unless all substitution groups are
 * in the taxonomy base and we are prepared to follow them all).
 *
 * This object is rather expensive to create (through the build method), building the maps that support fast querying based on URI
 * (with fragment) or "target EName".
 *
 * TaxonomyBase creation should never fail, if correct URIs are passed. Even the instance methods are very lenient and
 * should never fail. Typically, a taxonomy instantiated as an object of this class has not yet been validated.
 *
 * For the taxonomyDocUriMap and elemUriMap, we have that data is silently lost in those maps if there are any duplicate IDs (per document).
 * In a valid taxonomy (as XML document set) this duplication is not allowed.
 *
 * For the globalElementDeclarationMap, namedTypeDefinitionMap, etc., we also have that data is silently lost if there
 * is more than 1 global element declaration (or named type definition) with the same "target EName".
 * In a valid taxonomy (as XML schema) this duplication is not allowed.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBase private (
  val taxonomyDocs: immutable.IndexedSeq[TaxonomyDocument],
  val taxonomyDocUriMap: Map[URI, TaxonomyDocument],
  val elemUriMap: Map[URI, TaxonomyElem],
  val globalElementDeclarationsWithTargetENames: immutable.IndexedSeq[(GlobalElementDeclaration, EName)],
  val globalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration],
  val globalElementDeclarationMap: Map[EName, GlobalElementDeclaration],
  val namedTypeDefinitionMap: Map[EName, NamedTypeDefinition],
  val globalAttributeDeclarationMap: Map[EName, GlobalAttributeDeclaration],
  val derivedSubstitutionGroupMap: SubstitutionGroupMap) {

  require(
    taxonomyDocs.forall(_.uriOption.forall(_.getFragment == null)),
    s"Expected document URIs but got at least one URI with fragment")

  def rootElems: immutable.IndexedSeq[TaxonomyElem] = taxonomyDocs.map(_.documentElement)

  def rootElemUriMap: Map[URI, TaxonomyElem] = taxonomyDocUriMap.view.mapValues(_.documentElement).toMap

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
    require(elemUri.isAbsolute, s"URI '$elemUri' is not absolute")

    if (elemUri.getFragment == null) {
      taxonomyDocUriMap.get(elemUri).map(_.documentElement)
    } else {
      val xpointers = XPointer.parseXPointers(elemUri.getFragment)

      xpointers match {
        case ShorthandPointer(_) :: Nil =>
          // Do a fast map lookup on the entire URI with fragment
          elemUriMap.get(elemUri)
        case IdPointer(id) :: Nil =>
          val u = new URI(elemUri.getScheme, elemUri.getSchemeSpecificPart, id)

          // Do a fast map lookup on the URI with the id as fragment
          elemUriMap.get(u)
        case IdChildSequencePointer(id, childSeq) :: Nil =>
          val u = new URI(elemUri.getScheme, elemUri.getSchemeSpecificPart, id)

          // First do a fast map lookup on the URI with the id as fragment
          elemUriMap.get(u).flatMap(e => ChildSequencePointer(1 :: childSeq).findElem(e))
        case _ =>
          val rootElemOption = taxonomyDocUriMap.get(removeFragment(elemUri)).map(_.documentElement)
          rootElemOption.flatMap(e => XPointer.findElem(e, xpointers))
      }
    }
  }

  // Some finder methods for very frequently queried taxonomy elements.

  /**
   * Finds the (first) optional global element declaration with the given target EName (name with target namespace).
   *
   * This is a quick operation.
   */
  def findGlobalElementDeclarationByEName(targetEName: EName): Option[GlobalElementDeclaration] = {
    globalElementDeclarationMap.get(targetEName)
  }

  /**
   * Finds the (first) optional named type definition with the given target EName (name with target namespace).
   *
   * This is a quick operation.
   */
  def findNamedTypeDefinitionByEName(targetEName: EName): Option[NamedTypeDefinition] = {
    namedTypeDefinitionMap.get(targetEName)
  }

  /**
   * Finds the (first) optional global attribute declaration with the given target EName (name with target namespace).
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

  // Creating a "sub-taxonomy".

  /**
   * Creates a "sub-taxonomy" in which only the given document URIs occur.
   * It can be used for a specific entry point DTS, or to make query methods (not taking an EName) cheaper.
   */
  def filteringDocumentUris(docUris: Set[URI]): TaxonomyBase = {
    val filteredTaxonomyDocs = taxonomyDocs.filter(d => docUris.contains(d.uri))

    val filteredElemUris: Set[URI] =
      elemUriMap.keySet.toSeq.groupBy(removeFragment).filter(kv => docUris.contains(kv._1)).values.flatten.toSet

    val globalElementDeclarationENames: Set[EName] =
      filteredTaxonomyDocs.flatMap { d =>
        TaxonomyBase.getGlobalElementDeclarationMap(d.documentElement).keySet
      }.toSet

    val namedTypeDefinitionENames: Set[EName] =
      filteredTaxonomyDocs.flatMap { d =>
        TaxonomyBase.getNamedTypeDefinitionMap(d.documentElement).keySet
      }.toSet

    val globalAttributeDeclarationENames: Set[EName] =
      filteredTaxonomyDocs.flatMap { d =>
        TaxonomyBase.getGlobalAttributeDeclarationMap(d.documentElement).keySet
      }.toSet

    val filteredDerivedSubstitutionGroup = filterSubstitutionGroupMap(derivedSubstitutionGroupMap, docUris)

    val filteredGlobalElemDeclsWithTargetENames: immutable.IndexedSeq[(GlobalElementDeclaration, EName)] = {
      globalElementDeclarationsWithTargetENames.filter(p => globalElementDeclarationENames.contains(p._2))
    }

    val filteredGlobalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
      filteredGlobalElemDeclsWithTargetENames.map(_._1)
    }

    val filteredGlobalElementDeclarationMap: Map[EName, GlobalElementDeclaration] = {
      filteredGlobalElemDeclsWithTargetENames.groupBy(_._2).view.mapValues(_.head._1).toMap
    }

    new TaxonomyBase(
      filteredTaxonomyDocs,
      taxonomyDocUriMap.filter(kv => docUris.contains(kv._1)),
      elemUriMap.filter(kv => filteredElemUris.contains(kv._1)),
      filteredGlobalElemDeclsWithTargetENames,
      filteredGlobalElementDeclarations,
      filteredGlobalElementDeclarationMap,
      namedTypeDefinitionMap.filter(kv => namedTypeDefinitionENames.contains(kv._1)),
      globalAttributeDeclarationMap.filter(kv => globalAttributeDeclarationENames.contains(kv._1)),
      filteredDerivedSubstitutionGroup)
  }

  // Finding some "duplication errors".

  /**
   * Returns all duplicate ID attributes in the DOM tree with the given root element.
   * If the result is non-empty, the taxonomy is incorrect, and the map from URIs to elements loses data.
   *
   * The type of the ID attributes is not taken into account, although strictly speaking that is incorrect.
   */
  def findAllDuplicateIds(rootElem: TaxonomyElem): Set[String] = {
    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)
    val elemsGroupedById = elemsWithId.groupBy(_.attribute(IdEName))

    elemsGroupedById.filter(kv => kv._2.size >= 2).keySet
  }

  /**
   * Returns all duplicate global element declaration "target ENames" over all DOM trees combined.
   * If the result is non-empty, the taxonomy is incorrect, and the map from ENames to global element declarations loses data.
   */
  def findAllDuplicateGlobalElementDeclarationENames: Set[EName] = {
    val globalElementDeclarations =
      rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[GlobalElementDeclaration]))

    val globalElementDeclarationsGroupedByEName = globalElementDeclarations.groupBy(_.targetEName)

    globalElementDeclarationsGroupedByEName.filter(kv => kv._2.size >= 2).keySet
  }

  /**
   * Returns all duplicate named type definition "target ENames" over all DOM trees combined.
   * If the result is non-empty, the taxonomy is incorrect, and the map from ENames to named type definitions loses data.
   */
  def findAllDuplicateNamedTypeDefinitionENames: Set[EName] = {
    val namedTypeDefinitions =
      rootElems.flatMap(_.findAllElemsOrSelfOfType(classTag[NamedTypeDefinition]))

    val namedTypeDefinitionsGroupedByEName = namedTypeDefinitions.groupBy(_.targetEName)

    namedTypeDefinitionsGroupedByEName.filter(kv => kv._2.size >= 2).keySet
  }

  /**
   * Returns the "guessed Scope" from the documents in the taxonomy. This can be handy for finding
   * prefixes for namespace names, or for generating ENames from QNames.
   *
   * The resulting Scope is taken from the Scopes of the root elements, ignoring the default namespace,
   * if any. If different root element Scopes are conflicting, it is undetermined which one wins.
   */
  def guessedScope: Scope = {
    rootElems.map(_.scope.withoutDefaultNamespace).foldLeft(Scope.Empty) {
      case (accScope, currScope) =>
        (accScope ++ currScope).ensuring(_.retainingDefaultNamespace.isEmpty)
    }
  }

  private def filterSubstitutionGroupMap(substitutionGroupMap: SubstitutionGroupMap, docUris: Set[URI]): SubstitutionGroupMap = {
    val filteredMappings: Map[EName, EName] = substitutionGroupMap.mappings.filter { case (ename, _) =>
      globalElementDeclarationMap.get(ename).exists(e => docUris.contains(e.docUri))
    }

    SubstitutionGroupMap(filteredMappings)
  }

  private def removeFragment(uri: URI): URI = {
    if (uri.getFragment == null) {
      // No need to create a new URI in this case
      uri
    } else {
      new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
    }
  }
}

object TaxonomyBase {

  /**
   * Expensive build method (but the private constructor is cheap, and so are the Scala getters of the maps).
   *
   * It is the responsibility of the caller to pass different taxonomy documents.
   */
  def build(taxonomyDocs: immutable.IndexedSeq[TaxonomyDocument]): TaxonomyBase = {
    val taxonomyDocUriMap: Map[URI, TaxonomyDocument] = {
      taxonomyDocs.groupBy(_.uri).view.mapValues(_.head).toMap
    }

    val rootElems = taxonomyDocs.map(_.documentElement)

    val elemUriMap: Map[URI, TaxonomyElem] = {
      rootElems.flatMap(e => getElemUriMap(e).toSeq).toMap
    }

    // Below, I would prefer to exploit Scala 2.13 SeqMap instead

    val globalElemDeclsWithTargetENames: immutable.IndexedSeq[(GlobalElementDeclaration, EName)] = {
      rootElems.flatMap(e => getGlobalElementDeclarationsWithTargetENames(e))
    }

    val globalElementDeclarations: immutable.IndexedSeq[GlobalElementDeclaration] = {
      globalElemDeclsWithTargetENames.map(_._1)
    }

    val globalElementDeclarationMap: Map[EName, GlobalElementDeclaration] = {
      globalElemDeclsWithTargetENames.groupBy(_._2).view.mapValues(_.head._1).toMap
    }

    val namedTypeDefinitionMap: Map[EName, NamedTypeDefinition] = {
      rootElems.flatMap(e => getNamedTypeDefinitionMap(e).toSeq).toMap
    }

    val globalAttributeDeclarationMap: Map[EName, GlobalAttributeDeclaration] = {
      rootElems.flatMap(e => getGlobalAttributeDeclarationMap(e).toSeq).toMap
    }

    val derivedSubstitutionGroupMap: SubstitutionGroupMap =
      computeDerivedSubstitutionGroupMap(globalElementDeclarationMap)

    new TaxonomyBase(
      taxonomyDocs,
      taxonomyDocUriMap,
      elemUriMap,
      globalElemDeclsWithTargetENames,
      globalElementDeclarations,
      globalElementDeclarationMap,
      namedTypeDefinitionMap,
      globalAttributeDeclarationMap,
      derivedSubstitutionGroupMap)
  }

  def getGlobalElementDeclarationMap(rootElem: TaxonomyElem)(implicit enameProvider: ENameProvider): Map[EName, GlobalElementDeclaration] = {
    val globalElemDeclsWithTargetENames = getGlobalElementDeclarationsWithTargetENames(rootElem)(enameProvider)

    globalElemDeclsWithTargetENames.groupBy(_._2).view.mapValues(_.head._1).toMap
  }

  def getNamedTypeDefinitionMap(rootElem: TaxonomyElem)(implicit enameProvider: ENameProvider): Map[EName, NamedTypeDefinition] = {
    val xsdSchemaOption: Option[XsdSchema] =
      if (rootElem.isInstanceOf[Linkbase]) None else rootElem.findElemOrSelfOfType(classTag[XsdSchema])(_ => true)

    // For optimal performance, get the target namespace only once.

    val tnsOption: Option[String] = xsdSchemaOption.flatMap(_.targetNamespaceOption)

    val namedTypeDefinitions =
      xsdSchemaOption.toIndexedSeq.flatMap(_.findTopmostElemsOrSelfOfType(classTag[NamedTypeDefinition])(_ => true))

    namedTypeDefinitions.groupBy(e => enameProvider.getEName(tnsOption, e.nameAttributeValue)).view.mapValues(_.head).toMap
  }

  def getGlobalAttributeDeclarationMap(rootElem: TaxonomyElem)(implicit enameProvider: ENameProvider): Map[EName, GlobalAttributeDeclaration] = {
    val xsdSchemaOption: Option[XsdSchema] =
      if (rootElem.isInstanceOf[Linkbase]) None else rootElem.findElemOrSelfOfType(classTag[XsdSchema])(_ => true)

    // For optimal performance, get the target namespace only once.
    val tnsOption: Option[String] = xsdSchemaOption.flatMap(_.targetNamespaceOption)

    val globalAttributeDeclarations =
      xsdSchemaOption.toIndexedSeq.flatMap(_.findTopmostElemsOrSelfOfType(classTag[GlobalAttributeDeclaration])(_ => true))

    globalAttributeDeclarations.groupBy(e => enameProvider.getEName(tnsOption, e.nameAttributeValue)).view.mapValues(_.head).toMap
  }

  private def getGlobalElementDeclarationsWithTargetENames(rootElem: TaxonomyElem)(
    implicit enameProvider: ENameProvider): immutable.IndexedSeq[(GlobalElementDeclaration, EName)] = {

    val xsdSchemaOption: Option[XsdSchema] =
      if (rootElem.isInstanceOf[Linkbase]) None else rootElem.findElemOrSelfOfType(classTag[XsdSchema])(_ => true)

    // For optimal performance, get the target namespace only once.

    val tnsOption: Option[String] = xsdSchemaOption.flatMap(_.targetNamespaceOption)

    val globalElementDeclarations =
      xsdSchemaOption.toIndexedSeq.flatMap(_.findTopmostElemsOrSelfOfType(classTag[GlobalElementDeclaration])(_ => true))

    globalElementDeclarations.map(e => e -> enameProvider.getEName(tnsOption, e.nameAttributeValue))
  }

  /**
   * Returns the SubstitutionGroupMap that can be derived from this taxonomy base alone.
   * This is an expensive operation that should be performed only once, if possible.
   */
  private def computeDerivedSubstitutionGroupMap(globalElementDeclarationMap: Map[EName, GlobalElementDeclaration]): SubstitutionGroupMap = {
    val rawMappings: Map[EName, EName] =
      globalElementDeclarationMap.toSeq.flatMap {
        case (en, decl) => decl.substitutionGroupOption.map(sg => en -> sg)
      }.toMap

    val substGroups: Set[EName] = rawMappings.values.toSet

    val mappings: Map[EName, EName] = rawMappings.filter(kv => substGroups.contains(kv._1))

    SubstitutionGroupMap.from(mappings)
  }

  private def getElemUriMap(rootElem: TaxonomyElem): Map[URI, TaxonomyElem] = {
    val docUri = rootElem.docUri
    require(docUri.isAbsolute, s"Expected absolute URI but found '$docUri'")

    // Trying to make baseUri computation extremely efficient in the usual case that xml:base is not used
    val xmlBaseUsed = rootElem.findElemOrSelf(_.attributeOption(XmlBaseEName).nonEmpty).nonEmpty

    // The schema type of the ID attributes is not checked! That would be very expensive without any real advantage.

    val elemsWithId = rootElem.filterElemsOrSelf(_.attributeOption(IdEName).isDefined)

    elemsWithId.map { e =>
      val baseUri = if (xmlBaseUsed) e.baseUri else docUri
      makeUriWithIdFragment(baseUri, e.attribute(IdEName)) -> e
    }.toMap
  }

  private def makeUriWithIdFragment(baseUri: URI, idFragment: String): URI = {
    require(baseUri.isAbsolute, s"Expected absolute base URI but got '$baseUri'")
    new URI(baseUri.getScheme, baseUri.getSchemeSpecificPart, idFragment)
  }
}
