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

package eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm

import java.net.URI

/**
 * Document URI together with the dependent document URIs, to be used in DTS discovery.
 * The dependencies do not contain the "self" document URI itself, and they contain no duplicates.
 *
 * TODO Consider making this part of the public API (shared by JVM/JS), and use it in DTS discovery.
 *
 * @author Chris de Vreeze
 */
private[jvm] final case class DocDependencyList private (docUri: URI, dependencies: Seq[URI])

private[jvm] object DocDependencyList {

  /**
   * Creates a DocDependencyList from the given document URI and dependencies. For the latter, the passed
   * document URI itself is removed, and so are duplicate URIs, before constructing the result.
   */
  def from(docUri: URI, dependencies: Seq[URI]): DocDependencyList = {
    apply(docUri, dependencies.filterNot(_ == docUri).distinct)
  }
}
