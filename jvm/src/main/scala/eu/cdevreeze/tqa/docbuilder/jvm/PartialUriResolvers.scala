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
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter

import org.xml.sax.InputSource

import eu.cdevreeze.tqa.docbuilder.SimpleCatalog

/**
 * Partial URI resolvers, converting an URI to a SAX InputSource.
 *
 * @author Chris de Vreeze
 */
object PartialUriResolvers {

  type PartialUriResolver = (URI => Option[InputSource])

  /**
   * Creates a PartialUriResolver from a partial URI converter. Typically the URI converter converts HTTP(S) URIs
   * to file protocol URIs.
   */
  def fromPartialUriConverter(partialUriConverter: URI => Option[URI]): PartialUriResolver = {
    def resolveUri(uri: URI): Option[InputSource] = {
      val mappedUriOption = partialUriConverter(uri)

      mappedUriOption map { mappedUri =>
        val is: InputStream =
          if (mappedUri.getScheme == "file") {
            new FileInputStream(new File(mappedUri))
          } else {
            mappedUri.toURL.openStream()
          }

        new InputSource(is)
      }
    }

    resolveUri _
  }

  /**
   * Creates a PartialUriResolver for a ZIP file, using the given partial URI converter. For each mapped URI that is
   * relative, resolution takes place inside the ZIP file. Otherwise resolution takes place outside the
   * ZIP file.
   */
  def forZipFile(zipFile: File, partialUriConverter: URI => Option[URI]): PartialUriResolver = {
    val zipFileAsZipFile = new ZipFile(zipFile)

    val zipEntriesByRelativeUri: Map[URI, ZipEntry] = computeZipEntryMap(zipFile)

    def resolveUri(uri: URI): Option[InputSource] = {
      val mappedUriOption = partialUriConverter(uri)

      mappedUriOption map { mappedUri =>
        if (mappedUri.isAbsolute) {
          val is: InputStream =
            if (mappedUri.getScheme == "file") {
              new FileInputStream(new File(mappedUri))
            } else {
              mappedUri.toURL.openStream()
            }

          new InputSource(is)
        } else {
          val optionalZipEntry: Option[ZipEntry] = zipEntriesByRelativeUri.get(mappedUri)

          require(optionalZipEntry.isDefined, s"Missing ZIP entry in ZIP file $zipFile with URI $mappedUri")

          val is = zipFileAsZipFile.getInputStream(optionalZipEntry.get)

          new InputSource(is)
        }
      }
    }

    resolveUri _
  }

  def fromCatalog(catalog: SimpleCatalog): PartialUriResolver = {
    fromPartialUriConverter(PartialUriConverters.fromCatalog(catalog))
  }

  def fromLocalMirrorRootDirectory(rootDir: File): PartialUriResolver = {
    fromPartialUriConverter(PartialUriConverters.fromLocalMirrorRootDirectory(rootDir))
  }

  def forZipFileContainingLocalMirror(zipFile: File): PartialUriResolver = {
    forZipFile(zipFile, PartialUriConverters.fromLocalMirror)
  }

  def forZipFileUsingCatalog(zipFile: File, catalog: SimpleCatalog): PartialUriResolver = {
    forZipFile(zipFile, PartialUriConverters.fromCatalog(catalog))
  }

  private def computeZipEntryMap(zipFile: File): Map[URI, ZipEntry] = {
    val zipFileAsZipFile = new ZipFile(zipFile)

    val zipEntries = zipFileAsZipFile.entries().asScala.toIndexedSeq

    val zipFileParent = zipFile.getParentFile

    zipEntries.map(e => (toRelativeUri(e, zipFileParent) -> e)).toMap
  }

  private def toRelativeUri(zipEntry: ZipEntry, zipFileParent: File): URI = {
    val adaptedZipEntryUri = (new File(zipFileParent, zipEntry.getName)).toURI
    val zipFileParentUri = URI.create(returnWithTrailingSlash(zipFileParent.toURI))
    val relativeZipEntryUri = zipFileParentUri.relativize(adaptedZipEntryUri)
    require(!relativeZipEntryUri.isAbsolute, s"Not a relative URI: $relativeZipEntryUri")

    relativeZipEntryUri
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }
}
