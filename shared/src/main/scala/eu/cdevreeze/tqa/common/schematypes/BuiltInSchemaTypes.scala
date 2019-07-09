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

package eu.cdevreeze.tqa.common.schematypes

import eu.cdevreeze.tqa.common.names.ENames
import eu.cdevreeze.tqa.common.names.Namespaces
import eu.cdevreeze.yaidom.core.EName

/**
 * Built-in schema types.
 *
 * @author Chris de Vreeze
 */
object BuiltInSchemaTypes {

  import ENames._

  val baseTypes: Map[EName, EName] = Map(
    XsDateEName -> XsAnyAtomicTypeEName,
    XsTimeEName -> XsAnyAtomicTypeEName,
    XsDateTimeEName -> XsAnyAtomicTypeEName,
    XsGYearEName -> XsAnyAtomicTypeEName,
    XsGYearMonthEName -> XsAnyAtomicTypeEName,
    XsGMonthEName -> XsAnyAtomicTypeEName,
    XsGMonthDayEName -> XsAnyAtomicTypeEName,
    XsGDayEName -> XsAnyAtomicTypeEName,
    XsDurationEName -> XsAnyAtomicTypeEName,
    XsID_EName -> XsNCNameEName,
    XsIDREF_EName -> XsNCNameEName,
    XsENTITY_EName -> XsNCNameEName,
    XsNCNameEName -> XsNameEName,
    XsLanguageEName -> XsTokenEName,
    XsNameEName -> XsTokenEName,
    XsNMTOKEN_EName -> XsTokenEName,
    XsTokenEName -> XsNormalizedStringEName,
    XsNormalizedStringEName -> XsStringEName,
    XsStringEName -> XsAnyAtomicTypeEName,
    XsQNameEName -> XsAnyAtomicTypeEName,
    XsNOTATION_EName -> XsAnyAtomicTypeEName,
    XsFloatEName -> XsAnyAtomicTypeEName,
    XsDoubleEName -> XsAnyAtomicTypeEName,
    XsByteEName -> XsShortEName,
    XsShortEName -> XsIntEName,
    XsIntEName -> XsLongEName,
    XsLongEName -> XsIntegerEName,
    XsIntegerEName -> XsDecimalEName,
    XsNegativeIntegerEName -> XsNonPositiveIntegerEName,
    XsNonPositiveIntegerEName -> XsIntegerEName,
    XsUnsignedByteEName -> XsUnsignedShortEName,
    XsUnsignedShortEName -> XsUnsignedIntEName,
    XsUnsignedIntEName -> XsUnsignedLongEName,
    XsUnsignedLongEName -> XsNonNegativeIntegerEName,
    XsPositiveIntegerEName -> XsNonNegativeIntegerEName,
    XsNonNegativeIntegerEName -> XsIntegerEName,
    XsDecimalEName -> XsAnyAtomicTypeEName,
    XsBooleanEName -> XsAnyAtomicTypeEName,
    XsBase64BinaryEName -> XsAnyAtomicTypeEName,
    XsHexBinaryEName -> XsAnyAtomicTypeEName,
    XsAnyURI_EName -> XsAnyAtomicTypeEName,
    XsAnyAtomicTypeEName -> XsAnySimpleTypeEName,
    XsNMTOKENS_EName -> XsAnySimpleTypeEName,
    XsENTITIES_EName -> XsAnySimpleTypeEName,
    XsIDREFS_EName -> XsAnySimpleTypeEName,
    XsAnySimpleTypeEName -> XsAnyTypeEName
  )

  def isBuiltInSchemaType(ename: EName): Boolean = ename.namespaceUriOption.contains(Namespaces.XsNamespace)

  def findAncestorOrSelfTypes(ename: EName): List[EName] = {
    val baseOption = baseTypes.get(ename)

    if (baseOption.isEmpty) {
      List(ename)
    } else {
      // Recursive call
      ename :: findAncestorOrSelfTypes(baseOption.get)
    }
  }

  def findAncestorTypes(ename: EName): List[EName] = {
    val baseOption = baseTypes.get(ename)

    if (baseOption.isEmpty) {
      Nil
    } else {
      findAncestorOrSelfTypes(baseOption.get)
    }
  }

  def isBuiltInBooleanType(ename: EName): Boolean = (ename == XsBooleanEName)

  def isBuiltInFloatType(ename: EName): Boolean = {
    val ancestorOrSelfTypes = findAncestorOrSelfTypes(ename)

    ancestorOrSelfTypes.contains(XsFloatEName)
  }

  def isBuiltInDoubleType(ename: EName): Boolean = {
    val ancestorOrSelfTypes = findAncestorOrSelfTypes(ename)

    ancestorOrSelfTypes.contains(XsDoubleEName)
  }

  def isBuiltInDecimalType(ename: EName): Boolean = {
    val ancestorOrSelfTypes = findAncestorOrSelfTypes(ename)

    ancestorOrSelfTypes.contains(XsDecimalEName)
  }

  def isBuiltInNumberType(ename: EName): Boolean = {
    val ancestorOrSelfTypes = findAncestorOrSelfTypes(ename)

    val NumberTypes = Set(XsFloatEName, XsDoubleEName, XsDecimalEName)

    ancestorOrSelfTypes.exists(NumberTypes)
  }
}
