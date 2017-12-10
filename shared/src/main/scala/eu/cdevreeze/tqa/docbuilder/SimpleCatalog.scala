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

package eu.cdevreeze.tqa.docbuilder

import java.net.URI

import scala.collection.immutable

/**
 * Simple XML catalog, as used in XBRL taxonomy packages.
 *
 * @author Chris de Vreeze
 */
final case class SimpleCatalog(
    baseUriOption: Option[URI],
    uriRewrites: immutable.IndexedSeq[SimpleCatalog.UriRewrite]) {

  def findMappedUri(uri: URI): Option[URI] = {
    val sortedRewrites = uriRewrites.sortBy(_.uriStartString.length).reverse
    val uriString = uri.toString

    val uriRewriteOption = sortedRewrites.find(rewrite => uriString.startsWith(rewrite.uriStartString))

    uriRewriteOption map { rewrite =>
      val relativeUri: URI =
        URI.create(rewrite.uriStartString).relativize(uri).ensuring(u => !u.isAbsolute)

      val effectiveRewritePrefix: URI =
        baseUriOption.map(_.resolve(rewrite.rewritePrefix)).getOrElse(URI.create(rewrite.rewritePrefix))

      effectiveRewritePrefix.resolve(relativeUri)
    }
  }
}

object SimpleCatalog {

  final case class UriRewrite(uriStartString: String, rewritePrefix: String)
}
