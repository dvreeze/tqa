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

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * URI converters, typically converting an HTTP or HTTPS URI to a local file URI. The implementations
 * use `SimpleCatalog` objects to perform the actual URI conversions.
 *
 * @author Chris de Vreeze
 */
object UriConverters {

  type UriConverter = (URI => URI)

  def fromPartialUriConvertersWithoutFallback(
    partialUriConverters: immutable.IndexedSeq[PartialUriConverters.PartialUriConverter]): UriConverter = {

    def convertUri(uri: URI): URI = {
      partialUriConverters collectFirst { case f if f(uri).isDefined => f(uri).get } getOrElse {
        sys.error(s"Could not convert URI $uri")
      }
    }

    convertUri _
  }

  def fromPartialUriConvertersFallingBackToIdentity(
    partialUriConverters: immutable.IndexedSeq[PartialUriConverters.PartialUriConverter]): UriConverter = {

    def convertUri(uri: URI): URI = {
      partialUriConverters collectFirst { case f if f(uri).isDefined => f(uri).get } getOrElse (uri)
    }

    convertUri _
  }

  def identity: UriConverter = {
    PartialUriConverters.identity.andThen(_.get)
  }

  /**
   * Like `PartialUriConverters.fromLocalMirrorRootDirectory(rootDir)`, but otherwise the identity function.
   */
  def fromLocalMirrorRootDirectory(rootDir: File): UriConverter = {
    val delegate: PartialUriConverters.PartialUriConverter =
      PartialUriConverters.fromLocalMirrorRootDirectory(rootDir)

    def convertUri(uri: URI): URI = {
      delegate(uri).getOrElse(uri)
    }

    convertUri _
  }

  /**
   * Like `PartialUriConverters.fromLocalMirror`, but otherwise the identity function.
   */
  def fromLocalMirror: UriConverter = {
    val delegate: PartialUriConverters.PartialUriConverter =
      PartialUriConverters.fromLocalMirror

    def convertUri(uri: URI): URI = {
      delegate(uri).getOrElse(uri)
    }

    convertUri _
  }

  /**
   * Like `PartialUriConverters.fromCatalog(catalog)`, but otherwise the identity function.
   */
  def fromCatalog(catalog: SimpleCatalog): UriConverter = {
    val delegate: PartialUriConverters.PartialUriConverter =
      PartialUriConverters.fromCatalog(catalog)

    def convertUri(uri: URI): URI = {
      delegate(uri).getOrElse(uri)
    }

    convertUri _
  }
}
