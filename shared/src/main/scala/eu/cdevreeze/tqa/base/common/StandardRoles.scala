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

package eu.cdevreeze.tqa.base.common

/**
 * Standard XBRL roles.
 *
 * @author Chris de Vreeze
 */
object StandardRoles {

  val StandardExtendedLinkRole = "http://www.xbrl.org/2003/role/link"

  val StandardLabelResourceRoles: Set[String] = StandardLabelRoles.allRoles

  val StandardReferenceResourceRoles: Set[String] = StandardReferenceRoles.allRoles

  val AllStandardResourceRoles: Set[String] = StandardLabelResourceRoles ++ StandardReferenceResourceRoles

  val AllStandardRoles: Set[String] = Set(StandardExtendedLinkRole) ++ AllStandardResourceRoles

  // Standard linkbaseRef roles
  val CalculationLinkbaseRefRole = "http://www.xbrl.org/2003/role/calculationLinkbaseRef"
  val DefinitionLinkbaseRefRole = "http://www.xbrl.org/2003/role/definitionLinkbaseRef"
  val LabelLinkbaseRefRole = "http://www.xbrl.org/2003/role/labelLinkbaseRef"
  val PresentationLinkbaseRefRole = "http://www.xbrl.org/2003/role/presentationLinkbaseRef"
  val ReferenceLinkbaseRefRole = "http://www.xbrl.org/2003/role/referenceLinkbaseRef"

  val FootnoteRole = "http://www.xbrl.org/2003/role/footnote"
}
