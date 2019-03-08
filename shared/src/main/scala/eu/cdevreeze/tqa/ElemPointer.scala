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

package eu.cdevreeze.tqa

import java.net.URI

/**
 * Element pointer, consisting of a document URI and an XPointer. This combination points to an XML element
 * in a set of documents, such as a taxonomy.
 *
 * Note that different XPointers may point to the same element, given the document. Therefore equality of XPointers,
 * and by extension, element pointers, is not a reliable method for determining whether the elements pointed to are
 * the same.
 *
 * An element pointer corresponds to a URI containing the document URI and the XPointer as fragment part.
 *
 * @author Chris de Vreeze
 */
final case class ElemPointer(docUri: URI, xpointer: XPointer) {
  require(docUri.isAbsolute, s"The document URI must be absolute, but got '$docUri' instead")
  require(Option(docUri.getFragment).isEmpty, s"Expected no fragment in the document URI, but got '$docUri' instead")

  def toURI: URI = {
    new URI(docUri.getScheme, docUri.getSchemeSpecificPart, xpointer.toString)
  }
}

object ElemPointer {

  def fromUri(uri: URI): ElemPointer = {
    require(uri.isAbsolute, s"The URI must be absolute, but got '$uri' instead")
    require(Option(uri.getFragment).nonEmpty, s"Expected a fragment in the URI, but got '$uri' instead")

    ElemPointer(withoutFragment(uri), XPointer.parse(uri.getFragment))
  }

  private def withoutFragment(uri: URI): URI = {
    new URI(uri.getScheme, uri.getSchemeSpecificPart, null) // scalastyle:ignore null
  }
}
