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

package eu.cdevreeze.tqa.xpath.jaxp.saxon

import java.io.File
import java.io.FileInputStream
import java.net.URI

import javax.xml.transform.Source
import javax.xml.transform.URIResolver
import javax.xml.transform.stream.StreamSource

/**
 * URI resolver, populated from a mapping from original URIs to local URIs. This is inefficient in that each call
 * to method resolve may parse the same document again.
 *
 * @author Chris de Vreeze
 */
final class SimpleUriResolver(val uriToLocalUriMapper: URI => URI, val makeSource: (URI, URI) => Source) extends URIResolver {

  def this(uriToLocalUriMapper: URI => URI) = {
    this(
      uriToLocalUriMapper,
      { (originalUri, localUri) => new StreamSource(new FileInputStream(new File(localUri)), originalUri.toString) })
  }

  def resolve(href: String, base: String): Source = {
    val baseURI = new URI(Option(base).getOrElse(""))

    // Resolve the location if necessary
    val resolvedUri = baseURI.resolve(new URI(href))

    val localUri = uriToLocalUriMapper(resolvedUri)

    makeSource(resolvedUri, localUri)
  }
}
