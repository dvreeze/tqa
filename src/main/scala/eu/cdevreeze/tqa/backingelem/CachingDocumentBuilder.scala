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

package eu.cdevreeze.tqa.backingelem

import java.net.URI

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

import eu.cdevreeze.yaidom.queryapi.BackingElemApi

/**
 * DocumentBuilder using a Guava document cache. By all means, reuse the underlying document cache
 * as much as possible.
 *
 * @author Chris de Vreeze
 */
final class CachingDocumentBuilder[E <: BackingElemApi](
    val cache: LoadingCache[URI, E]) extends DocumentBuilder {

  type BackingElem = E

  def build(uri: URI): BackingElem = cache.get(uri)
}

object CachingDocumentBuilder {

  /**
   * Factory method to create a Google Guava BackingElem cache.
   */
  def createCache[E <: BackingElemApi](
    wrappedDocBuilder: DocumentBuilder.Aux[E],
    cacheSize: Int): LoadingCache[URI, E] = {

    val cacheBuilder: CacheBuilder[URI, E] =
      CacheBuilder.newBuilder().maximumSize(cacheSize).recordStats().asInstanceOf[CacheBuilder[URI, E]]

    val cacheLoader = new CacheLoader[URI, E] {

      def load(key: URI): E = {
        wrappedDocBuilder.build(key)
      }
    }

    val cache: LoadingCache[URI, E] = cacheBuilder.build(cacheLoader)
    cache
  }
}
