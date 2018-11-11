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

package eu.cdevreeze.tqa.docbuilder.jvm

import java.io.File
import java.net.URI

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * Partial URI converters, typically converting an HTTP or HTTPS URI to a local file URI. The implementations
 * use `SimpleCatalog` objects to perform the actual URI conversions.
 *
 * @author Chris de Vreeze
 */
object PartialUriConverters {

  type PartialUriConverter = (URI => Option[URI])

  def identity: PartialUriConverter = {
    def convertUri(uri: URI): Option[URI] = Some(uri)

    convertUri _
  }

  /**
   * Returns a partial URI converter that converts (absolute) HTTP and HTTPS URIs to file URIs.
   * Such a conversion assumes the existence of a local mirror of one or more internet sites,
   * where the host name in the parameter URI is an immediate sub-directory of the local root directory,
   * and where the URI scheme (such as HTTP) and port number, if any, do not occur in the local mirror.
   * The conversion then returns the URI in the local mirror that corresponds to the parameter URI.
   *
   * The partial URI converter is defined for all URIs that are HTTP(S) URIs (with host).
   *
   * For example, if the URI is "http://www.example.com/a/b/c.xml", then the URI is rewritten using
   * a `SimpleCatalog` which rewrites URI start "http://www.example.com/" to the rewrite prefix,
   * as file protocol URI, for sub-directory "www.example.com" of the given root directory.
   */
  // scalastyle:off null
  @deprecated(message = "This method has too little control over which URIs to convert. Use 'fromCatalog' instead.", since = "0.8.8")
  def fromLocalMirrorRootDirectory(rootDir: File): PartialUriConverter = {
    require(rootDir.isDirectory, s"Not a directory: $rootDir")
    require(rootDir.isAbsolute, s"Not an absolute path: $rootDir")

    def convertUri(uri: URI): Option[URI] = {
      if ((uri.getHost == null) || ((uri.getScheme != "http") && (uri.getScheme != "https"))) {
        None
      } else {
        val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))
        val rewritePrefix = returnWithTrailingSlash((new File(rootDir, uri.getHost)).toURI)

        val catalog =
          SimpleCatalog(
            None,
            Vector(SimpleCatalog.UriRewrite(None, uriStart, rewritePrefix)))

        catalog.findMappedUri(uri)
      }
    }

    convertUri _
  }

  /**
   * Like `fromLocalMirrorRootDirectory`, but the resulting partial URI converter returns relative URIs.
   * Such a partial URI converter is useful in ZIP files that contain mirrored sites.
   *
   * If the local mirror does not start at the root (of the ZIP file), parameter parentPathOption is
   * used to specify the relative path of the local mirror.
   */
  // scalastyle:off null
  @deprecated(message = "This method has too little control over which URIs to convert. Use 'fromCatalog' instead.", since = "0.8.8")
  def fromLocalMirrorInZipFile(parentPathOption: Option[URI]): PartialUriConverter = {
    require(parentPathOption.forall(!_.isAbsolute), s"Not a relative URI: ${parentPathOption.get}")

    def convertUri(uri: URI): Option[URI] = {
      if ((uri.getHost == null) || ((uri.getScheme != "http") && (uri.getScheme != "https"))) {
        None
      } else {
        val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))

        val hostAsRelativeUri = URI.create(uri.getHost + "/")

        val rewritePrefix =
          parentPathOption.map(pp => URI.create(returnWithTrailingSlash(pp)).resolve(hostAsRelativeUri)).
            getOrElse(hostAsRelativeUri).toString.ensuring(_.endsWith("/"))

        val catalog =
          SimpleCatalog(
            None,
            Vector(SimpleCatalog.UriRewrite(None, uriStart, rewritePrefix)))

        catalog.findMappedUri(uri)
      }
    }

    convertUri _
  }

  /**
   * Turns the given catalog into a partial URI converter. It can return absolute and/or relative
   * URIs. Relative URIs are typically meant to be resolved inside ZIP files.
   *
   * The partial URI converter is only defined for URIs matching URI start strings in the catalog.
   */
  def fromCatalog(catalog: SimpleCatalog): PartialUriConverter = {
    def convertUri(uri: URI): Option[URI] = {
      catalog.findMappedUri(uri)
    }

    convertUri _
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }
}
