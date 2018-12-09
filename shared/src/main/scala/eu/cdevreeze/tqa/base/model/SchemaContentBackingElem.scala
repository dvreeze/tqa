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

package eu.cdevreeze.tqa.base.model

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.ClarkElemApi
import eu.cdevreeze.yaidom.queryapi.ClarkElemLike

/**
 * XML element in a schema, aware of the target namespace, if any, and the document URI.
 *
 * The attributes that in the original XML are QName-valued are here resolved ones and therefore EName-valued.
 * Make sure that this is indeed the case, because these elements contain no in-scope namespaces!
 *
 * @author Chris de Vreeze
 */
final case class SchemaContentBackingElem(
  docUri: URI,
  targetNamespaceOption: Option[String],
  resolvedName: EName,
  attributes: Map[EName, String],
  text: String,
  childElems: immutable.IndexedSeq[SchemaContentBackingElem]) extends ClarkElemApi with ClarkElemLike {

  type ThisElem = SchemaContentBackingElem

  // Methods needed for completing the ClarkElemApi API

  def thisElem: SchemaContentBackingElem = this

  def findAllChildElems: immutable.IndexedSeq[SchemaContentBackingElem] = childElems

  def resolvedAttributes: Map[EName, String] = attributes
}
