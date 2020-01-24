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
import java.io.FileInputStream
import java.net.URI
import java.util.zip.ZipFile

import scala.collection.immutable

import org.xml.sax.InputSource

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * URI resolvers, converting an URI to a SAX InputSource.
 *
 * Note that the only fundamental methods in this singleton object are fromPartialUriResolversWithoutFallback and fromPartialUriResolversWithFallback.
 *
 * @author Chris de Vreeze
 */
object UriResolvers {

  type UriResolver = URI => InputSource

  /**
   * Returns the URI resolver that for each input URI tries all given partial URI resolvers until a
   * matching one is found, returning the InputSource resolution result. If for an URI no matching partial URI
   * resolver is found, an exception is thrown.
   */
  def fromPartialUriResolversWithoutFallback(
    partialUriResolvers: immutable.IndexedSeq[PartialUriResolvers.PartialUriResolver]): UriResolver = {

    require(partialUriResolvers.nonEmpty, s"No partial URI resolvers given")

    def resolveUri(uri: URI): InputSource = {
      partialUriResolvers.drop(1).foldLeft(partialUriResolvers.head(uri)) {
        case (accOptInputSource, pur) =>
          accOptInputSource.orElse(pur(uri))
      } getOrElse {
        sys.error(s"Could not resolve URI $uri")
      }
    }

    resolveUri
  }

  /**
   * Returns the URI resolver that for each input URI tries all given partial URI resolvers until a
   * matching one is found, returning the InputSource resolution result. If for an URI no matching partial URI
   * resolver is found, the URI itself is "opened" as InputSource.
   */
  def fromPartialUriResolversWithFallback(
    partialUriResolvers: immutable.IndexedSeq[PartialUriResolvers.PartialUriResolver]): UriResolver = {

    require(partialUriResolvers.nonEmpty, s"No partial URI resolvers given")

    def resolveUri(uri: URI): InputSource = {
      partialUriResolvers.drop(1).foldLeft(partialUriResolvers.head(uri)) {
        case (accOptInputSource, pur) =>
          accOptInputSource.orElse(pur(uri))
      } getOrElse {
        val is =
          if (uri.getScheme == "file") {
            new FileInputStream(new File(uri))
          } else {
            uri.toURL.openStream()
          }

        new InputSource(is)
      }
    }

    resolveUri
  }

  /**
   * Returns `fromPartialUriResolversWithoutFallback(immutable.IndexedSeq(partialUriResolver))`.
   */
  def fromPartialUriResolverWithoutFallback(partialUriResolver: PartialUriResolvers.PartialUriResolver): UriResolver = {
    fromPartialUriResolversWithoutFallback(immutable.IndexedSeq(partialUriResolver))
  }

  /**
   * Returns `fromPartialUriResolversWithFallback(immutable.IndexedSeq(partialUriResolver))`.
   */
  def fromPartialUriResolverWithFallback(partialUriResolver: PartialUriResolvers.PartialUriResolver): UriResolver = {
    fromPartialUriResolversWithFallback(immutable.IndexedSeq(partialUriResolver))
  }

  /**
   * Returns the equivalent of `PartialUriResolvers.fromPartialUriConverter(liftedUriConverter).andThen(_.get)`.
   *
   * It can also be defined as:
   * {{{
   * fromPartialUriResolverWithoutFallback(
   *   PartialUriResolvers.fromPartialUriConverter(liftedUriConverter))
   * }}}
   */
  def fromUriConverter(uriConverter: URI => URI): UriResolver = {
    val pur: PartialUriResolvers.PartialUriResolver =
      PartialUriResolvers.fromPartialUriConverter(PartialUriConverters.fromUriConverter(uriConverter))

    fromPartialUriResolverWithoutFallback(pur)
  }

  /**
   * Returns the equivalent of `PartialUriResolvers.forZipFile(zipFile, liftedUriConverter).andThen(_.get)`, .
   *
   * It can also be defined as:
   * {{{
   * fromPartialUriResolverWithoutFallback(
   *   PartialUriResolvers.forZipFile(zipFile, liftedUriConverter))
   * }}}
   */
  def forZipFile(zipFile: ZipFile, uriConverter: URI => URI): UriResolver = {
    val pur: PartialUriResolvers.PartialUriResolver =
      PartialUriResolvers.forZipFile(zipFile, PartialUriConverters.fromUriConverter(uriConverter))

    fromPartialUriResolverWithoutFallback(pur)
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromCatalogFallingBackToIdentity(catalog))`.
   */
  def fromCatalogWithFallback(catalog: SimpleCatalog): UriResolver = {
    fromUriConverter(UriConverters.fromCatalogFallingBackToIdentity(catalog))
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromCatalogWithoutFallback(catalog))`.
   */
  def fromCatalogWithoutFallback(catalog: SimpleCatalog): UriResolver = {
    fromUriConverter(UriConverters.fromCatalogWithoutFallback(catalog))
  }

  /**
   * Returns an URI resolver that expects all files to be found in a local mirror, with the host name
   * of the URI mirrored under the given root directory. The protocol (HTTP or HTTPS) is not represented in
   * the local mirror.
   */
  def fromLocalMirrorRootDirectory(rootDir: File): UriResolver = {
    require(rootDir.isDirectory, s"Not a directory: $rootDir")
    require(rootDir.isAbsolute, s"Not an absolute path: $rootDir")

    def convertUri(uri: URI): URI = {
      require(uri.getHost != null, s"Missing host name in URI '$uri'")
      require(uri.getScheme == "http" || uri.getScheme == "https", s"Not an HTTP(S) URI: '$uri'")

      val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))
      val rewritePrefix = returnWithTrailingSlash(new File(rootDir, uri.getHost).toURI)

      val catalog =
        SimpleCatalog(
          None,
          Vector(SimpleCatalog.UriRewrite(None, uriStart, rewritePrefix)))

      val mappedUri = catalog.findMappedUri(uri).getOrElse(sys.error(s"No mapping found for URI '$uri'"))
      mappedUri
    }

    fromUriConverter(convertUri)
  }

  /**
   * Returns an URI resolver that expects all files to be found in a local mirror in a ZIP file, with the host name
   * of the URI mirrored under the given optional parent directory. The protocol (HTTP or HTTPS) is not represented in
   * the local mirror.
   */
  def forZipFileContainingLocalMirror(zipFile: ZipFile, parentPathOption: Option[URI]): UriResolver = {
    require(parentPathOption.forall(!_.isAbsolute), s"Not a relative URI: ${parentPathOption.get}")

    def convertUri(uri: URI): URI = {
      require(uri.getHost != null, s"Missing host name in URI '$uri'")
      require(uri.getScheme == "http" || uri.getScheme == "https", s"Not an HTTP(S) URI: '$uri'")

      val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))

      val hostAsRelativeUri = URI.create(uri.getHost + "/")

      val rewritePrefix =
        parentPathOption.map(pp => URI.create(returnWithTrailingSlash(pp)).resolve(hostAsRelativeUri)).
          getOrElse(hostAsRelativeUri).toString.ensuring(_.endsWith("/"))

      val catalog =
        SimpleCatalog(
          None,
          Vector(SimpleCatalog.UriRewrite(None, uriStart, rewritePrefix)))

      val mappedUri = catalog.findMappedUri(uri).getOrElse(sys.error(s"No mapping found for URI '$uri'"))
      mappedUri
    }

    forZipFile(zipFile, convertUri)
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromCatalogFallingBackToIdentity(catalog))`.
   */
  def forZipFileUsingCatalogWithFallback(zipFile: ZipFile, catalog: SimpleCatalog): UriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalogFallingBackToIdentity(catalog))
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromCatalogWithoutFallback(catalog))`.
   */
  def forZipFileUsingCatalogWithoutFallback(zipFile: ZipFile, catalog: SimpleCatalog): UriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalogWithoutFallback(catalog))
  }

  val default: UriResolver = {
    fromUriConverter(UriConverters.identity)
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }
}
