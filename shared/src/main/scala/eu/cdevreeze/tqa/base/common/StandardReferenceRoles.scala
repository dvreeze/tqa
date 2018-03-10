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
 * Well-known standard reference roles.
 *
 * @author Chris de Vreeze
 */
object StandardReferenceRoles {

  val Reference = makeReferenceRole("reference")
  val DefinitionRef = makeReferenceRole("definitionRef")
  val DisclosureRef = makeReferenceRole("disclosureRef")
  val MandatoryDisclosureRef = makeReferenceRole("mandatoryDisclosureRef")
  val RecommendedDisclosureRef = makeReferenceRole("recommendedDisclosureRef")
  val UnspecifiedDisclosureRef = makeReferenceRole("unspecifiedDisclosureRef ")
  val PresentationRef = makeReferenceRole("presentationRef")
  val MeasurementRef = makeReferenceRole("measurementRef")
  val CommentaryRef = makeReferenceRole("commentaryRef")
  val ExampleRef = makeReferenceRole("exampleRef")

  /**
   * Alias for Reference. It is the default reference role, or, in other words, the standard reference role.
   */
  val StandardReference = Reference

  private def makeReferenceRole(suffix: String): String = {
    s"http://www.xbrl.org/2003/role/$suffix"
  }
}
