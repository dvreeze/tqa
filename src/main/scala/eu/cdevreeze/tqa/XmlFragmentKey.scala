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

package eu.cdevreeze.tqa

import java.net.URI

import eu.cdevreeze.yaidom.core.Path
import eu.cdevreeze.yaidom.queryapi.BackingElemApi

/**
 * A unique identifier of an XML fragment in a Taxonomy. It is made up by
 * the document URI and the Path within that document.
 *
 * @author Chris de Vreeze
 */
final case class XmlFragmentKey(val docUri: URI, val path: Path)

object XmlFragmentKey {

  implicit class XmlFragmentKeyAware(val backingElem: BackingElemApi) {

    def key: XmlFragmentKey =
      XmlFragmentKey(backingElem.docUri, backingElem.path)
  }
}
