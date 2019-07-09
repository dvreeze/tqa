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

package eu.cdevreeze.tqa.common.xpath

import eu.cdevreeze.yaidom.core.EName

/**
 * EName value or XPath expression. If it is an ENameExpr, the XPath expression is expected to resolve
 * to schema type xs:QName. Hence, a very simple ENameExpr would syntactically look like a lexical
 * QName literal (and not like an EName in James Clark notation).
 *
 * @author Chris de Vreeze
 */
sealed trait ENameValueOrExpr extends ValueOrExpr[EName]

final case class ENameValue(value: EName) extends ENameValueOrExpr with Value[EName]

final case class ENameExpr(expr: ScopedXPathString) extends ENameValueOrExpr with Expr[EName]
