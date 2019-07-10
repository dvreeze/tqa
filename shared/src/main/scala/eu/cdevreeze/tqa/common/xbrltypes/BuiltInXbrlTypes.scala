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
import eu.cdevreeze.yaidom.core.EName

/**
 * Built-in XBRL (item) types.
 *
 * @author Chris de Vreeze
 */
object BuiltInXbrlTypes {

  import ENames._

  val xbrlNonFractionItemBaseTypes: Map[EName, EName] = Map(
    XbrliDecimalItemTypeEName -> XsDecimalEName,
    XbrliFloatItemTypeEName -> XsFloatEName,
    XbrliDoubleItemTypeEName -> XsDoubleEName,
    XbrliIntegerItemTypeEName -> XsIntegerEName,
    XbrliNonPositiveIntegerItemTypeEName -> XsNonPositiveIntegerEName,
    XbrliNegativeIntegerItemTypeEName -> XsNegativeIntegerEName,
    XbrliLongItemTypeEName -> XsLongEName,
    XbrliIntItemTypeEName -> XsIntEName,
    XbrliShortItemTypeEName -> XsShortEName,
    XbrliByteItemTypeEName -> XsByteEName,
    XbrliNonNegativeIntegerItemTypeEName -> XsNonNegativeIntegerEName,
    XbrliUnsignedLongItemTypeEName -> XsUnsignedLongEName,
    XbrliUnsignedIntItemTypeEName -> XsUnsignedIntEName,
    XbrliUnsignedShortItemTypeEName -> XsUnsignedShortEName,
    XbrliUnsignedByteItemTypeEName -> XsUnsignedByteEName,
    XbrliPositiveIntegerItemTypeEName -> XsPositiveIntegerEName,
    XbrliMonetaryItemTypeEName -> XbrliMonetaryEName, // Not an "xs" type
    XbrliSharesItemTypeEName -> XbrliSharesEName, // Not an "xs" type
    XbrliPureItemTypeEName -> XbrliPureEName, // Not an "xs" type
    XbrliStringItemTypeEName -> XsStringEName,
    XbrliBooleanItemTypeEName -> XsBooleanEName,
    XbrliHexBinaryItemTypeEName -> XsHexBinaryEName,
    XbrliBase64BinaryItemTypeEName -> XsBase64BinaryEName,
    XbrliAnyURIItemTypeEName -> XsAnyURI_EName,
    XbrliQNameItemTypeEName -> XsQNameEName,
    XbrliDurationItemTypeEName -> XsDurationEName,
    XbrliDateTimeItemTypeEName -> XbrliDateUnionEName, // Not an "xs" type, but a union type
    XbrliTimeItemTypeEName -> XsTimeEName,
    XbrliDateItemTypeEName -> XsDateEName,
    XbrliGYearMonthItemTypeEName -> XsGYearMonthEName,
    XbrliGYearItemTypeEName -> XsGYearEName,
    XbrliGMonthDayItemTypeEName -> XsGMonthDayEName,
    XbrliGDayItemTypeEName -> XsGDayEName,
    XbrliGMonthItemTypeEName -> XsGMonthEName,
    XbrliNormalizedStringItemTypeEName -> XsNormalizedStringEName,
    XbrliTokenItemTypeEName -> XsTokenEName,
    XbrliLanguageItemTypeEName -> XsLanguageEName,
    XbrliNameItemTypeEName -> XsNameEName,
    XbrliNCNameItemTypeEName -> XsNCNameEName)

  val FractionItemType = XbrliFractionItemTypeEName

  val XbrlNumericTypes = {
    Set(
      XbrliDecimalItemTypeEName,
      XbrliFloatItemTypeEName,
      XbrliDoubleItemTypeEName,
      XbrliIntegerItemTypeEName,
      XbrliNonPositiveIntegerItemTypeEName,
      XbrliNegativeIntegerItemTypeEName,
      XbrliLongItemTypeEName,
      XbrliIntItemTypeEName,
      XbrliShortItemTypeEName,
      XbrliByteItemTypeEName,
      XbrliNonNegativeIntegerItemTypeEName,
      XbrliUnsignedLongItemTypeEName,
      XbrliUnsignedIntItemTypeEName,
      XbrliUnsignedShortItemTypeEName,
      XbrliUnsignedByteItemTypeEName,
      XbrliPositiveIntegerItemTypeEName,
      XbrliMonetaryItemTypeEName,
      XbrliSharesItemTypeEName,
      XbrliPureItemTypeEName,
      FractionItemType)
  }

  def xbrlItemTypes: Set[EName] = (xbrlNonFractionItemBaseTypes.keySet + FractionItemType)
}
