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
 * Well-known standard label roles.
 *
 * @author Chris de Vreeze
 */
object StandardLabelRoles {

  val Label = makeLabelRole("label")
  val TerseLabel = makeLabelRole("terseLabel")
  val VerboseLabel = makeLabelRole("verboseLabel")
  val PositiveLabel = makeLabelRole("positiveLabel")
  val PositiveTerseLabel = makeLabelRole("positiveTerseLabel")
  val PositiveVerboseLabel = makeLabelRole("positiveVerboseLabel")
  val NegativeLabel = makeLabelRole("negativeLabel")
  val NegativeTerseLabel = makeLabelRole("negativeTerseLabel")
  val NegativeVerboseLabel = makeLabelRole("negativeVerboseLabel")
  val ZeroLabel = makeLabelRole("zeroLabel")
  val ZeroTerseLabel = makeLabelRole("zeroTerseLabel")
  val ZeroVerboseLabel = makeLabelRole("zeroVerboseLabel")
  val TotalLabel = makeLabelRole("totalLabel")
  val PeriodStartLabel = makeLabelRole("periodStartLabel")
  val PeriodEndLabel = makeLabelRole("periodEndLabel")
  val Documentation = makeLabelRole("documentation")
  val DefinitionGuidance = makeLabelRole("definitionGuidance")
  val DisclosureGuidance = makeLabelRole("disclosureGuidance")
  val PresentationGuidance = makeLabelRole("presentationGuidance")
  val MeasurementGuidance = makeLabelRole("measurementGuidance")
  val CommentaryGuidance = makeLabelRole("commentaryGuidance")
  val ExampleGuidance = makeLabelRole("exampleGuidance")

  /**
   * Alias for Label. It is the default label role, or, in other words, the standard label role.
   */
  val StandardLabel = Label

  private def makeLabelRole(suffix: String): String = {
    s"http://www.xbrl.org/2003/role/$suffix"
  }

  val allRoles: Set[String] = Set(
    Label,
    TerseLabel,
    VerboseLabel,
    PositiveLabel,
    PositiveTerseLabel,
    PositiveVerboseLabel,
    NegativeLabel,
    NegativeTerseLabel,
    NegativeVerboseLabel,
    ZeroLabel,
    ZeroTerseLabel,
    ZeroVerboseLabel,
    TotalLabel,
    PeriodStartLabel,
    PeriodEndLabel,
    Documentation,
    DefinitionGuidance,
    DisclosureGuidance,
    PresentationGuidance,
    MeasurementGuidance,
    CommentaryGuidance,
    ExampleGuidance
  )
}
