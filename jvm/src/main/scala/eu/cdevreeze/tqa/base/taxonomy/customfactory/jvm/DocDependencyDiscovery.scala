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

package eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm

import java.net.URI

import eu.cdevreeze.tqa.common.names.ENames
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.queryapi.BackingNodes

/**
 * Document dependency discovery, to be used in DTS discovery. It turns a document into a DocDependencyList.
 *
 * @author Chris de Vreeze
 */
private[jvm] object DocDependencyDiscovery {

  // TODO Refine implementation to prevent too many base URI computations

  /**
   * Returns the DocDependencyList for the given document.
   *
   * In a DTS discovery workflow, this method must be called first, one call per document.
   */
  def findDocDependencyList(doc: BackingDocumentApi): DocDependencyList = {
    require(doc.uriOption.nonEmpty, s"Document without URI not allowed")
    val docUri: URI = doc.uriOption.get

    val xlinkHrefElems: Seq[BackingNodes.Elem] = doc.documentElement.filterElems(isXlinkHrefElem)

    val xlinkHrefs: Seq[URI] = xlinkHrefElems.flatMap { elem =>
      val rawHref: URI = URI.create(elem.attribute(ENames.XLinkHrefEName))

      if (rawHref == EmptyUri) None else Some(makeAbsoluteWithoutFragment(rawHref, elem.baseUri))
    }

    val importOrIncludeElems: Seq[BackingNodes.Elem] = doc.documentElement.filterElems(isImportOrInclude)

    val schemaLocations: Seq[URI] = importOrIncludeElems.flatMap { elem =>
      val rawSchemaLocationOption: Option[URI] =
        elem.attributeOption(ENames.SchemaLocationEName).map(URI.create)

      rawSchemaLocationOption.map(u => makeAbsoluteWithoutFragment(u, elem.baseUri))
    }

    val dependencies: Seq[URI] = xlinkHrefs.appendedAll(schemaLocations)

    // The returned DocDependencyList contains no duplicates. Neither does it contain this document URI itself.
    DocDependencyList.from(docUri, dependencies)
  }

  private def isXlinkHrefElem(elem: BackingNodes.Elem): Boolean = {
    elem.resolvedName match {
      case ENames.LinkLocEName | ENames.LinkRoleRefEName | ENames.LinkArcroleRefEName | ENames.LinkLinkbaseRefEName =>
        true
      case _ => false
    }
  }

  private def isImportOrInclude(elem: BackingNodes.Elem): Boolean = {
    elem.resolvedName match {
      case ENames.XsImportEName | ENames.XsIncludeEName => true
      case _                                            => false
    }
  }

  private def makeAbsoluteWithoutFragment(uri: URI, baseUri: URI): URI = {
    removeFragment(baseUri.resolve(uri))
  }

  private def removeFragment(uri: URI): URI = {
    if (uri.getFragment == null) {
      // No need to create a new URI in this case
      uri
    } else {
      new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
    }
  }

  private val EmptyUri: URI = URI.create("")
}
