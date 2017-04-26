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

import scala.collection.immutable

/**
 * A parsed xsi:schemaLocation attribute value, as a mapping from namespace names to document URIs.
 *
 * It can not contain any duplicate namespace names, or else an exception is thrown during construction.
 *
 * @author Chris de Vreeze
 */
final class XsiSchemaLocation private (val namespaceXsdPairs: immutable.IndexedSeq[(String, URI)]) {
  require(
    namespaceXsdPairs.toMap.size == namespaceXsdPairs.size,
    s"Duplicate namespaces in xsi:schemaLocation ${namespaceXsdPairs}")

  override def equals(other: Any): Boolean = other match {
    case other: XsiSchemaLocation => this.namespaceXsdPairs.toMap == other.namespaceXsdPairs.toMap
    case _                        => false
  }

  override def hashCode: Int = this.namespaceXsdPairs.toMap.hashCode

  /**
   * Returns a string representation of the XsiSchemaLocation. It does not spread the string over multiple lines, for prettifying.
   * Keep this in mind when "serializing" an XsiSchemaLocation.
   */
  override def toString: String = {
    namespaceXsdPairs.map(kv => s"${kv._1} ${kv._2}").mkString(" ")
  }

  def filterKeys(p: String => Boolean): XsiSchemaLocation = {
    val nsUriPairs = namespaceXsdPairs.filter(kv => p(kv._1))
    new XsiSchemaLocation(nsUriPairs)
  }

  def map(f: (String, URI) => (String, URI)): XsiSchemaLocation = {
    val nsUriPairs = namespaceXsdPairs map { case (ns, uri) => f(ns, uri) }
    new XsiSchemaLocation(XsiSchemaLocation.removeDuplicateNamespaces(nsUriPairs))
  }

  def mapValues(f: URI => URI): XsiSchemaLocation = {
    val nsUriPairs = namespaceXsdPairs.map(kv => (kv._1 -> f(kv._2)))
    new XsiSchemaLocation(nsUriPairs)
  }
}
object XsiSchemaLocation {

  /**
   * Invokes the constructor, after calling method `removeDuplicateNamespaces`.
   */
  def apply(namespaceXsdPairs: immutable.IndexedSeq[(String, URI)]): XsiSchemaLocation = {
    new XsiSchemaLocation(removeDuplicateNamespaces(namespaceXsdPairs))
  }

  /**
   * Parses the schema location string into an XsiSchemaLocation object. The parsing is lenient in that this
   * method is equivalent to `parse(schemaLocation, true)`.
   */
  def parse(schemaLocation: String): XsiSchemaLocation = {
    parse(schemaLocation, true)
  }

  /**
   * Parses the schema location string into an XsiSchemaLocation object. If lenient, method `removeDuplicateNamespaces`
   * is called before constructing the XsiSchemaLocation object.
   */
  def parse(schemaLocation: String, lenient: Boolean): XsiSchemaLocation = {
    val nsXsdPairs = Whitespace.split(schemaLocation).toIndexedSeq.filterNot(_.trim.isEmpty).grouped(2).toIndexedSeq

    require(nsXsdPairs.forall(_.size == 2), s"Incorrect schemaLocation due to uneven number of 'list members': '$schemaLocation'")

    val rawResult: immutable.IndexedSeq[(String, URI)] = nsXsdPairs.map(p => (p(0) -> URI.create(p(1))))

    val result: immutable.IndexedSeq[(String, URI)] =
      if (lenient) {
        removeDuplicateNamespaces(rawResult)
      } else {
        rawResult
      }

    new XsiSchemaLocation(result)
  }

  /**
   * Returns the passed mapping from namespace names to schema document URIs, but if there are any duplicate
   * namespace names only one of those pairs (the last one) is part of the result.
   */
  def removeDuplicateNamespaces(
    namespaceXsdPairs: immutable.IndexedSeq[(String, URI)]): immutable.IndexedSeq[(String, URI)] = {

    immutable.ListMap(namespaceXsdPairs: _*).toIndexedSeq ensuring { result =>
      result.toMap.size == result.size
    }
  }

  private val Whitespace = """\s""".r.pattern
}
