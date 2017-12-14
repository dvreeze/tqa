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

import scala.collection.immutable

import org.xml.sax.InputSource

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * URI resolvers, converting an URI to a SAX InputSource.
 *
 * @author Chris de Vreeze
 */
object UriResolvers {

  type UriResolver = (URI => InputSource)

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

    resolveUri _
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

    resolveUri _
  }

  /**
   * Like `PartialUriResolvers.fromPartialUriConverter(liftedUriConverter).andThen(_.get)`, .
   */
  def fromUriConverter(uriConverter: URI => URI): UriResolver = {
    val delegate: PartialUriResolvers.PartialUriResolver =
      PartialUriResolvers.fromPartialUriConverter(uriConverter.andThen(u => Some(u)))

    delegate.andThen(_.ensuring(_.isDefined).get)
  }

  /**
   * Like `PartialUriResolvers.forZipFile(zipFile, liftedUriConverter).andThen(_.get)`, .
   */
  def forZipFile(zipFile: File, uriConverter: URI => URI): UriResolver = {
    val delegate: PartialUriResolvers.PartialUriResolver =
      PartialUriResolvers.forZipFile(zipFile, uriConverter.andThen(u => Some(u)))

    delegate.andThen(_.ensuring(_.isDefined).get)
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromCatalog(catalog))`.
   */
  def fromCatalog(catalog: SimpleCatalog): UriResolver = {
    fromUriConverter(UriConverters.fromCatalog(catalog))
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromLocalMirrorRootDirectory(rootDir))`.
   */
  def fromLocalMirrorRootDirectory(rootDir: File): UriResolver = {
    fromUriConverter(UriConverters.fromLocalMirrorRootDirectory(rootDir))
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromCatalog(catalog))`.
   */
  def forZipFileUsingCatalog(zipFile: File, catalog: SimpleCatalog): UriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalog(catalog))
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromLocalMirror(parentPathOption))`.
   */
  def forZipFileContainingLocalMirror(zipFile: File, parentPathOption: Option[URI]): UriResolver = {
    forZipFile(zipFile, UriConverters.fromLocalMirror(parentPathOption))
  }
}
