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

package eu.cdevreeze.tqa.base.taxonomybuilder.jvm

import java.net.URI

import eu.cdevreeze.tqa.ENames.SchemaLocationEName
import eu.cdevreeze.tqa.base.dom.ArcroleRef
import eu.cdevreeze.tqa.base.dom.Include
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.LinkbaseRef
import eu.cdevreeze.tqa.base.dom.RoleRef
import eu.cdevreeze.tqa.base.dom.StandardLoc
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.dom.TaxonomyElem
import eu.cdevreeze.tqa.base.dom.TaxonomyRootElem
import eu.cdevreeze.tqa.base.dom.XsdSchema

import scala.reflect.classTag

/**
 * Default parallel DTS discovery implementation. It will fail for all found URIs that cannot be resolved to
 * taxonomy documents.
 *
 * This document collector works well with XBRL Taxonomy Packages, passing an entry point in such a
 * taxonomy package, and using a document builder that uses the XML catalog of the taxonomy package.
 *
 * DTS discovery also works if one or more taxonomy schemas and/or linkbases have been combined in the
 * same XML document, below another (wrapper) root element. Embedded linkbases are also picked up.
 *
 * @author Chris de Vreeze
 */
final class DefaultParallelDtsCollector(acceptsEmptyUriSet: Boolean)
    extends AbstractParallelDtsCollector(acceptsEmptyUriSet) {

  def allowingEmptyUriSet(acceptsEmptyUriSet: Boolean): DefaultParallelDtsCollector = {
    DefaultParallelDtsCollector.acceptingEmptyUriSet(acceptsEmptyUriSet)
  }

  def findAllUsedDocUris(taxonomyDoc: TaxonomyDocument): Set[URI] = {
    val taxoRootElems =
      taxonomyDoc.documentElement.findTopmostElemsOrSelfOfType(classTag[TaxonomyRootElem])(_ => true)

    taxoRootElems.flatMap(e => findAllUsedDocUris(e)).toSet
  }

  def findAllUsedDocUris(taxonomyRootElem: TaxonomyRootElem): Set[URI] = {
    taxonomyRootElem match {
      case xsdSchema: XsdSchema =>
        // Minding embedded linkbases

        findAllUsedDocUrisInXsdSchema(xsdSchema).union {
          xsdSchema.findAllElemsOfType(classTag[Linkbase]).flatMap(lb => findAllUsedDocUrisInLinkbase(lb)).toSet
        }
      case linkbase: Linkbase =>
        findAllUsedDocUrisInLinkbase(linkbase)
    }
  }

  def findAllUsedDocUrisInXsdSchema(rootElem: XsdSchema): Set[URI] = {
    // Using the base URI instead of document URI for xs:import and xs:include (although XML Schema knows nothing about XML Base)

    val imports = rootElem.findAllImports
    val includes = rootElem.findAllElemsOfType(classTag[Include])
    val linkbaseRefs = rootElem.findAllElemsOfType(classTag[LinkbaseRef])

    val importUris =
      imports.flatMap(e => (e \@ SchemaLocationEName).map(u => makeAbsoluteWithoutFragment(URI.create(u), e)))
    val includeUris =
      includes.flatMap(e => (e \@ SchemaLocationEName).map(u => makeAbsoluteWithoutFragment(URI.create(u), e)))
    val linkbaseRefUris =
      linkbaseRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))

    (importUris ++ includeUris ++ linkbaseRefUris).toSet.diff(Set(rootElem.docUri))
  }

  def findAllUsedDocUrisInLinkbase(rootElem: Linkbase): Set[URI] = {
    // Only link:loc locators are used in DTS discovery.

    val locs = rootElem.findAllElemsOfType(classTag[StandardLoc])
    val roleRefs = rootElem.findAllElemsOfType(classTag[RoleRef])
    val arcroleRefs = rootElem.findAllElemsOfType(classTag[ArcroleRef])

    val locUris =
      locs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))
    val roleRefUris =
      roleRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))
    val arcroleRefUris =
      arcroleRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))

    (locUris ++ roleRefUris ++ arcroleRefUris).toSet.diff(Set(rootElem.docUri))
  }

  private def makeAbsoluteWithoutFragment(uri: URI, elem: TaxonomyElem): URI = {
    removeFragment(elem.baseUri.resolve(uri))
  }

  private def removeFragment(uri: URI): URI = {
    if (uri.getFragment == null) {
      // No need to create a new URI in this case
      uri
    } else {
      new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
    }
  }

  private val EmptyUri = URI.create("")
}

object DefaultParallelDtsCollector {

  /**
   * Returns a DefaultParallelDtsCollector that does not accept empty URI sets.
   */
  def apply(): DefaultParallelDtsCollector = {
    acceptingEmptyUriSet(false)
  }

  def acceptingEmptyUriSet(acceptsEmptyUriSet: Boolean): DefaultParallelDtsCollector = {
    new DefaultParallelDtsCollector(acceptsEmptyUriSet)
  }
}
