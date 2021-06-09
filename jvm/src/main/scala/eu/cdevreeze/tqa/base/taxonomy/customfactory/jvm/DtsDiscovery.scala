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

import scala.annotation.tailrec

/**
 * DTS discovery, taking document dependencies for "all" documents as input.
 *
 * TODO Consider making this part of the public API (shared by JVM/JS).
 *
 * @author Chris de Vreeze
 */
private[jvm] final class DtsDiscovery(val docDependencies: Map[URI, DocDependencyList]) {
  require(docDependencies.forall(kv => kv._2.docUri == kv._1), s"Corrupt document dependencies map")

  def findDts(entrypointUris: Set[URI]): Set[URI] = {
    completeDts(entrypointUris, Set.empty).ensuring(result => entrypointUris.subsetOf(result))
  }

  /**
   * Breadth-first DTS discovery.
   */
  @tailrec
  private def completeDts(docUris: Set[URI], processedDocUris: Set[URI]): Set[URI] = {
    assert(processedDocUris.subsetOf(docUris))

    if (docUris.subsetOf(processedDocUris)) {
      assert(docUris == processedDocUris)
      docUris
    } else {
      // One step, processing all URIs currently known, and not yet processed
      val docUrisToProcessInThisStep: Set[URI] = docUris.diff(processedDocUris)

      val docUrisFoundInThisStep: Set[URI] = docUrisToProcessInThisStep.toSeq.flatMap(u => findAllUsedDocUris(u)).toSet

      val newDocUris: Set[URI] = docUris.union(docUrisFoundInThisStep)

      // Recursive call
      completeDts(newDocUris, docUris)
    }
  }

  private def findAllUsedDocUris(uri: URI): Seq[URI] = {
    val docDependencyList: DocDependencyList =
      docDependencies.getOrElse(uri, sys.error(s"Unknown dependencies for document URI '$uri'"))
    docDependencyList.dependencies
  }
}
