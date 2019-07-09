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

package eu.cdevreeze.tqa.common.xbrltypes

import eu.cdevreeze.tqa.common.names.ENames
import eu.cdevreeze.tqa.common.names.Namespaces
import eu.cdevreeze.yaidom.core.EName

/**
 * Built-in XBRL (item) types.
 *
 * @author Chris de Vreeze
 */
object BuiltInXbrlTypes {

  import ENames._
  import Namespaces._

  val xbrlNonFractionItemBaseTypes: Map[EName, EName] = Map(
    EName(XbrliNamespace, "decimalItemType") -> XsDecimalEName,
    EName(XbrliNamespace, "floatItemType") -> XsFloatEName,
    EName(XbrliNamespace, "doubleItemType") -> XsDoubleEName,
    EName(XbrliNamespace, "integerItemType") -> XsIntegerEName,
    EName(XbrliNamespace, "nonPositiveIntegerItemType") -> XsNonPositiveIntegerEName,
    EName(XbrliNamespace, "negativeIntegerItemType") -> XsNegativeIntegerEName,
    EName(XbrliNamespace, "longItemType") -> XsLongEName,
    EName(XbrliNamespace, "intItemType") -> XsIntEName,
    EName(XbrliNamespace, "shortItemType") -> XsShortEName,
    EName(XbrliNamespace, "byteItemType") -> XsByteEName,
    EName(XbrliNamespace, "nonNegativeIntegerItemType") -> XsNonNegativeIntegerEName,
    EName(XbrliNamespace, "unsignedLongItemType") -> XsUnsignedLongEName,
    EName(XbrliNamespace, "unsignedIntItemType") -> XsUnsignedIntEName,
    EName(XbrliNamespace, "unsignedShortItemType") -> XsUnsignedShortEName,
    EName(XbrliNamespace, "unsignedByteItemType") -> XsUnsignedByteEName,
    EName(XbrliNamespace, "positiveIntegerItemType") -> XsPositiveIntegerEName,
    EName(XbrliNamespace, "monetaryItemType") -> EName(XbrliNamespace, "monetary"),
    EName(XbrliNamespace, "sharesItemType") -> EName(XbrliNamespace, "shares"),
    EName(XbrliNamespace, "pureItemType") -> EName(XbrliNamespace, "pure"),
    EName(XbrliNamespace, "stringItemType") -> XsStringEName,
    EName(XbrliNamespace, "booleanItemType") -> XsBooleanEName,
    EName(XbrliNamespace, "hexBinaryItemType") -> XsHexBinaryEName,
    EName(XbrliNamespace, "base64BinaryItemType") -> XsBase64BinaryEName,
    EName(XbrliNamespace, "anyURIItemType") -> XsAnyURI_EName,
    EName(XbrliNamespace, "QNameItemType") -> XsQNameEName,
    EName(XbrliNamespace, "durationItemType") -> XsDurationEName,
    EName(XbrliNamespace, "dateTimeItemType") -> EName(XbrliNamespace, "dateUnion"),
    EName(XbrliNamespace, "timeItemType") -> XsTimeEName,
    EName(XbrliNamespace, "dateItemType") -> XsDateEName,
    EName(XbrliNamespace, "gYearMonthItemType") -> XsGYearMonthEName,
    EName(XbrliNamespace, "gYearItemType") -> XsGYearEName,
    EName(XbrliNamespace, "gMonthDayItemType") -> XsGMonthDayEName,
    EName(XbrliNamespace, "gDayItemType") -> XsGDayEName,
    EName(XbrliNamespace, "gMonthItemType") -> XsGMonthEName,
    EName(XbrliNamespace, "normalizedStringItemType") -> XsNormalizedStringEName,
    EName(XbrliNamespace, "tokenItemType") -> XsTokenEName,
    EName(XbrliNamespace, "languageItemType") -> XsLanguageEName,
    EName(XbrliNamespace, "NameItemType") -> XsNameEName,
    EName(XbrliNamespace, "NCNameItemType") -> XsNCNameEName)

  val FractionItemType = EName(XbrliNamespace, "fractionItemType")

  val XbrlNumericTypes = {
    Set(
      EName(XbrliNamespace, "decimalItemType"),
      EName(XbrliNamespace, "floatItemType"),
      EName(XbrliNamespace, "doubleItemType"),
      EName(XbrliNamespace, "integerItemType"),
      EName(XbrliNamespace, "nonPositiveIntegerItemType"),
      EName(XbrliNamespace, "negativeIntegerItemType"),
      EName(XbrliNamespace, "longItemType"),
      EName(XbrliNamespace, "intItemType"),
      EName(XbrliNamespace, "shortItemType"),
      EName(XbrliNamespace, "byteItemType"),
      EName(XbrliNamespace, "nonNegativeIntegerItemType"),
      EName(XbrliNamespace, "unsignedLongItemType"),
      EName(XbrliNamespace, "unsignedIntItemType"),
      EName(XbrliNamespace, "unsignedShortItemType"),
      EName(XbrliNamespace, "unsignedByteItemType"),
      EName(XbrliNamespace, "positiveIntegerItemType"),
      EName(XbrliNamespace, "monetaryItemType"),
      EName(XbrliNamespace, "sharesItemType"),
      EName(XbrliNamespace, "pureItemType"),
      FractionItemType)
  }

  def xbrlItemTypes: Set[EName] = (xbrlNonFractionItemBaseTypes.keySet + FractionItemType)
}
