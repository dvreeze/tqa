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

import java.io.File
import java.net.URI

/**
 * URI converters, typically converting an HTTP or HTTPS URI to a local file URI.
 *
 * @author Chris de Vreeze
 */
object UriConverters {

  /**
   * Converts HTTP and HTTPS URIs to file URIs and otherwise returns the parameter URI.
   * Such a conversion assumes the existence of a local mirror of one or more internet sites,
   * where the host name in the parameter URI is an immediate sub-directory of the local root directory,
   * and where the URI scheme (such as HTTP) and port number, if any, do not occur in the local mirror.
   * The conversion then returns the URI in the local mirror that corresponds to the parameter URI.
   */
  def uriToLocalUri(uri: URI, rootDir: File): URI = {
    val relativePathOption: Option[String] = uri.getScheme match {
      case "http"  => Some(uri.toString.drop("http://".size))
      case "https" => Some(uri.toString.drop("https://".size))
      case _       => None
    }

    relativePathOption map { relativePath =>
      val f = new File(rootDir, relativePath.dropWhile(_ == '/'))
      f.toURI
    } getOrElse (uri)
  }
}
