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

  def fromPartialUriResolversWithoutFallback(
    partialUriResolvers: immutable.IndexedSeq[PartialUriResolvers.PartialUriResolver]): UriResolver = {

    def resolveUri(uri: URI): InputSource = {
      partialUriResolvers collectFirst { case f if f(uri).isDefined => f(uri).get } getOrElse {
        sys.error(s"Could not convert URI $uri")
      }
    }

    resolveUri _
  }

  def fromPartialUriResolversWithFallback(
    partialUriResolvers: immutable.IndexedSeq[PartialUriResolvers.PartialUriResolver]): UriResolver = {

    def resolveUri(uri: URI): InputSource = {
      partialUriResolvers collectFirst { case f if f(uri).isDefined => f(uri).get } getOrElse {
        new InputSource(uri.toURL.openStream())
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

    def resolveUri(uri: URI): InputSource = {
      delegate(uri).ensuring(_.isDefined).get
    }

    resolveUri _
  }

  /**
   * Like `PartialUriResolvers.forZipFile(zipFile, liftedUriConverter).andThen(_.get)`, .
   */
  def forZipFile(zipFile: File, uriConverter: URI => URI): UriResolver = {
    val delegate: PartialUriResolvers.PartialUriResolver =
      PartialUriResolvers.forZipFile(zipFile, uriConverter.andThen(u => Some(u)))

    def resolveUri(uri: URI): InputSource = {
      delegate(uri).ensuring(_.isDefined).get
    }

    resolveUri _
  }

  def fromCatalog(catalog: SimpleCatalog): UriResolver = {
    fromUriConverter(UriConverters.fromCatalog(catalog))
  }

  def fromLocalMirrorRootDirectory(rootDir: File): UriResolver = {
    fromUriConverter(UriConverters.fromLocalMirrorRootDirectory(rootDir))
  }

  def forZipFileContainingLocalMirror(zipFile: File): UriResolver = {
    forZipFile(zipFile, UriConverters.fromLocalMirror)
  }

  def forZipFileUsingCatalog(zipFile: File, catalog: SimpleCatalog): UriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalog(catalog))
  }
}
