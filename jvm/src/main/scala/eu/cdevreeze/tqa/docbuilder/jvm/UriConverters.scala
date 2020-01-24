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

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * URI converters, typically converting an HTTP or HTTPS URI to a local file URI. The implementations
 * use `SimpleCatalog` objects to perform the actual URI conversions.
 *
 * Note that the only fundamental methods in this singleton object are fromPartialUriConvertersWithoutFallback and
 * fromPartialUriConvertersFallingBackToIdentity.
 *
 * @author Chris de Vreeze
 */
object UriConverters {

  type UriConverter = URI => URI

  /**
   * Returns the URI converter that for each input URI tries all given partial URI converters until a
   * matching one is found, returning the conversion result. If for an URI no matching partial URI
   * converter is found, an exception is thrown.
   */
  def fromPartialUriConvertersWithoutFallback(
    partialUriConverters: immutable.IndexedSeq[PartialUriConverters.PartialUriConverter]): UriConverter = {

    require(partialUriConverters.nonEmpty, s"No partial URI converters given")

    def convertUri(uri: URI): URI = {
      partialUriConverters.drop(1).foldLeft(partialUriConverters.head(uri)) {
        case (accOptUri, puc) =>
          accOptUri.orElse(puc(uri))
      } getOrElse {
        sys.error(s"Could not convert URI $uri")
      }
    }

    convertUri
  }

  /**
   * Returns the URI converter that for each input URI tries all given partial URI converters until a
   * matching one is found, returning the conversion result. If for an URI no matching partial URI
   * converter is found, the URI itself is returned.
   */
  def fromPartialUriConvertersFallingBackToIdentity(
    partialUriConverters: immutable.IndexedSeq[PartialUriConverters.PartialUriConverter]): UriConverter = {

    require(partialUriConverters.nonEmpty, s"No partial URI converters given")

    def convertUri(uri: URI): URI = {
      partialUriConverters.drop(1).foldLeft(partialUriConverters.head(uri)) {
        case (accOptUri, puc) =>
          accOptUri.orElse(puc(uri))
      }.getOrElse(uri)
    }

    convertUri
  }

  /**
   * Returns `fromPartialUriConvertersWithoutFallback(immutable.IndexedSeq(partialUriConverter))`.
   */
  def fromPartialUriConverterWithoutFallback(partialUriConverter: PartialUriConverters.PartialUriConverter): UriConverter = {
    fromPartialUriConvertersWithoutFallback(immutable.IndexedSeq(partialUriConverter))
  }

  /**
   * Returns `fromPartialUriConvertersFallingBackToIdentity(immutable.IndexedSeq(partialUriConverter))`.
   */
  def fromPartialUriConverterFallingBackToIdentity(partialUriConverter: PartialUriConverters.PartialUriConverter): UriConverter = {
    fromPartialUriConvertersFallingBackToIdentity(immutable.IndexedSeq(partialUriConverter))
  }

  val identity: UriConverter = {
    PartialUriConverters.identity.andThen(_.get)
  }

  /**
   * Like `PartialUriConverters.fromCatalog(catalog)`, but otherwise the identity function.
   */
  def fromCatalogFallingBackToIdentity(catalog: SimpleCatalog): UriConverter = {
    fromPartialUriConverterFallingBackToIdentity(
      PartialUriConverters.fromCatalog(catalog))
  }

  /**
   * Like `PartialUriConverters.fromCatalog(catalog)`, but otherwise throwing an exception.
   */
  def fromCatalogWithoutFallback(catalog: SimpleCatalog): UriConverter = {
    fromPartialUriConverterWithoutFallback(
      PartialUriConverters.fromCatalog(catalog))
  }
}
