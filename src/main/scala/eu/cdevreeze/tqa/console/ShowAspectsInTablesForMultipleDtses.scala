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

package eu.cdevreeze.tqa.console

import java.io.File
import java.net.URI
import java.util.logging.Logger
import java.util.regex.Pattern

import scala.collection.immutable

/**
 * Program that calls `ShowAspectsInTables` for multiple DTSes.
 *
 * @author Chris de Vreeze
 */
object ShowAspectsInTablesForMultipleDtses {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size == 2, s"Usage: ShowAspectsInTablesForMultipleDtses <taxo root dir> <entrypoint regex>")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointPathRegex = Pattern.compile(args(1))

    val useHttp = System.getProperty("useHttp", "true").toBoolean

    val entrypointUris =
      findFiles(rootDir, entrypointPathRegex).map(f => localUriToOriginalUri(f.toURI, rootDir.toURI, useHttp))

    logger.info(s"Found ${entrypointUris.size} entrypoints")

    entrypointUris foreach { uri =>
      logger.info(s"Running program ShowAspectsInTables for entrypoint $uri")

      ShowAspectsInTables.main(Array(rootDir.toString, uri.toString))
    }

    logger.info("Ready (for all entrypoints)")
  }

  private def findFiles(dir: File, entrypointPathRegex: Pattern): immutable.IndexedSeq[File] = {
    // Recursive calls
    dir.listFiles.toIndexedSeq flatMap {
      case d: File if d.isDirectory => findFiles(d, entrypointPathRegex)
      case f: File if f.isFile      => Vector(f).filter(file => isEntrypoint(file, entrypointPathRegex))
      case f                        => Vector()
    }
  }

  private def isEntrypoint(f: File, entrypointPathRegex: Pattern): Boolean = {
    entrypointPathRegex.matcher(f.getAbsolutePath).matches
  }

  private def localUriToOriginalUri(localUri: URI, rootDir: URI, useHttp: Boolean): URI = {
    require(localUri.getScheme == "file")
    require(localUri.toString.startsWith(rootDir.toString))

    val rawHostPlusPath = localUri.toString.drop(rootDir.toString.size)
    val hostPlusPath = if (rawHostPlusPath.startsWith("/")) rawHostPlusPath.drop(1) else rawHostPlusPath
    assert(!hostPlusPath.startsWith("/"))

    val scheme = if (useHttp) "http" else "https"

    val resultUri = URI.create(s"${scheme}://${hostPlusPath}")
    resultUri
  }
}
