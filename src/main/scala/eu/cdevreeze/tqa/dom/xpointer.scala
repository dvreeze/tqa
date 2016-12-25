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

package eu.cdevreeze.tqa.dom

import scala.collection.immutable

import eu.cdevreeze.tqa.ENames.IdEName
import eu.cdevreeze.yaidom.queryapi.ScopedElemApi

/**
 * XPointer, with sub-types for shorthand pointers and element scheme pointers.
 *
 * Note that a locator href attribute may contain multiple element scheme pointers in succession.
 * Method `parseXPointers` in the XPointer companion object can parse those successive XPointers.
 *
 * @author Chris de Vreeze
 */
sealed trait XPointer {

  /**
   * Finds the optional element with this XPointer, relative to the passed root element.
   */
  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E): Option[E]
}

/**
 * Shorthand pointer, which is by far the most commonly used kind of XPointer in XBRL (and in general).
 */
final case class ShorthandPointer(id: String) extends XPointer {

  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E): Option[E] = {
    rootElem.findElemOrSelf(_.attributeOption(IdEName) == Some(id))
  }

  override def toString: String = id
}

sealed trait ElementSchemePointer extends XPointer

/**
 * Element-scheme XPointer containing only an ID. It is therefore semantically equivalent to a shorthand pointer.
 */
final case class IdPointer(id: String) extends ElementSchemePointer {

  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E): Option[E] = {
    rootElem.findElemOrSelf(_.attributeOption(IdEName) == Some(id))
  }

  override def toString: String = s"element(${id})"
}

/**
 * Element-scheme XPointer starting with an ID and followed by a child sequence. The indexes in the child
 * sequence are 1-based.
 */
final case class IdChildSequencePointer(id: String, childSequence: List[Int]) extends ElementSchemePointer {

  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E): Option[E] = {
    IdPointer(id).findElem(rootElem).flatMap(e => ChildSequencePointer(1 :: childSequence).findElem(e))
  }

  override def toString: String = {
    val dataString = s"${id}/" + childSequence.mkString("/")
    s"element(${dataString})"
  }
}

/**
 * Element-scheme XPointer containing (only) a child sequence. The indexes in the child sequence are 1-based.
 */
final case class ChildSequencePointer(childSequence: List[Int]) extends ElementSchemePointer {
  require(childSequence.size >= 1, "Empty child sequence not allowed")

  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E): Option[E] = {
    if (childSequence.headOption == Some(1)) findElem(rootElem, childSequence.tail) else None
  }

  private def findElem[E <: ScopedElemApi.Aux[E]](root: E, childSeq: List[Int]): Option[E] = childSeq match {
    case Nil => Some(root)
    case idx :: tail =>
      val childElems = root.findAllChildElems
      val currElemOption = if (idx >= 1 && idx <= childElems.size) Some(childElems(idx - 1)) else None
      // Recursive calls
      currElemOption.flatMap(e => findElem(e, tail))
  }

  override def toString: String = {
    val dataString = "/" + childSequence.mkString("/")
    s"element(${dataString})"
  }
}

object XPointer {

  /**
   * Fast method to recognize shorthand pointers, as opposed to element scheme pointers.
   */
  def mustBeShorthandPointer(s: String): Boolean = {
    s.indexOf("element(") < 0
  }

  /**
   * Finds the optional element with this XPointer sequence, relative to the passed root element.
   * The first of these XPointer that finds an element wins, if any.
   */
  def findElem[E <: ScopedElemApi.Aux[E]](rootElem: E, xpointers: immutable.Seq[XPointer]): Option[E] = {
    val elems = xpointers.flatMap(xp => xp.findElem(rootElem))
    elems.headOption
  }

  /**
   * Parses the given string into an XPointer, and throws an exception if parsing fails.
   */
  def parse(s: String): XPointer = s match {
    case s if !s.startsWith("element(") => ShorthandPointer(s)
    case s if s.endsWith(")") && s.indexOf('/') < 0 =>
      val data = parseElementSchemeData(s)
      IdPointer(data)
    case s if s.endsWith(")") =>
      val data = parseElementSchemeData(s)

      if (data.startsWith("/")) ChildSequencePointer(data.substring(1).split('/').toList.map(_.toInt))
      else {
        val parts = data.split('/').toList
        IdChildSequencePointer(parts.head, parts.tail.map(_.toInt))
      }
    case s => sys.error(s"Could not parse string '$s' as a shorthand or element scheme XPointer")
  }

  /**
   * Parses the given string into one or more successive XPointers, and throws an exception if parsing fails.
   */
  def parseXPointers(s: String): List[XPointer] = {
    val idx = s.indexOf(")element(")

    if (idx < 0) List(parse(s))
    else {
      // Recursive call
      parse(s.substring(0, idx + 1)) :: parseXPointers(s.substring(idx + 1))
    } ensuring { result =>
      result.nonEmpty
    }
  }

  private def parseElementSchemeData(s: String): String = {
    require(s.startsWith("element("), s"Expected element scheme pointer, but got '${s}'")
    require(s.endsWith(")"), s"Expected element scheme pointer, but got '${s}'")

    val withoutPrefix = s.substring("element(".length)
    val result = withoutPrefix.substring(0, withoutPrefix.length - 1)
    result
  }
}
