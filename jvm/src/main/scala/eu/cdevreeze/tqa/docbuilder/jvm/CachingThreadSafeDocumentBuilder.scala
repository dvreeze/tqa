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

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.CachingThreadSafeDocumentBuilder.LoadingCacheWrapper
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi

/**
 * ThreadSafeDocumentBuilder using a Caffeine document cache. By all means, reuse the underlying document cache
 * as much as possible.
 *
 * @author Chris de Vreeze
 */
final class CachingThreadSafeDocumentBuilder[D <: BackingDocumentApi](val cacheWrapper: LoadingCacheWrapper[D])
    extends DocumentBuilder.ThreadSafeDocumentBuilder {

  type BackingDoc = D

  def build(uri: URI): BackingDoc = cacheWrapper.cache.get(uri)
}

object CachingThreadSafeDocumentBuilder {

  /**
   * Wrapper around a LoadingCache, to make sure that the cache uses a ThreadSafeDocumentBuilder.
   */
  final case class LoadingCacheWrapper[D <: BackingDocumentApi] private (cache: LoadingCache[URI, D])

  /**
   * Factory method to create a Caffeine BackingDoc cache, returned as LoadingCacheWrapper.
   */
  def createCache[D <: BackingDocumentApi](
      wrappedDocBuilder: DocumentBuilder.ThreadSafeDocumentBuilder.Aux[D],
      cacheSize: Int): LoadingCacheWrapper[D] = {

    val cacheBuilder: Caffeine[URI, D] =
      Caffeine.newBuilder().maximumSize(cacheSize).recordStats().asInstanceOf[Caffeine[URI, D]]

    val cacheLoader = new CacheLoader[URI, D] {

      def load(key: URI): D = {
        wrappedDocBuilder.build(key)
      }
    }

    val cache: LoadingCache[URI, D] = cacheBuilder.build(cacheLoader)
    LoadingCacheWrapper(cache)
  }
}
