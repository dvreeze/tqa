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

import eu.cdevreeze.tqa.common.names.ENames
import eu.cdevreeze.yaidom.queryapi.BackingNodes

/**
 * Standard XBRL arcroles.
 *
 * @author Chris de Vreeze
 */
object StandardArcroles {

  def getStandardArcroles(standardArc: BackingNodes.Elem): Set[String] = {
    standardArc.resolvedName match {
      case ENames.LinkDefinitionArcEName   => StandardDefinitionArcroles
      case ENames.LinkPresentationArcEName => Set(StandardPresentationArcrole)
      case ENames.LinkCalculationArcEName  => Set(StandardCalculationArcrole)
      case ENames.LinkLabelArcEName        => Set(StandardLabelArcrole)
      case ENames.LinkReferenceArcEName    => Set(StandardReferenceArcrole)
      case _                               => Set.empty
    }
  }

  // XBRL 2.1 5.2.2.3
  val StandardLabelArcrole = "http://www.xbrl.org/2003/arcrole/concept-label"

  // XBRL 2.1 5.2.3.3
  val StandardReferenceArcrole = "http://www.xbrl.org/2003/arcrole/concept-reference"

  // XBRL 2.1 5.2.4.2
  val StandardPresentationArcrole = "http://www.xbrl.org/2003/arcrole/parent-child"

  // XBRL 2.1 5.2.5.2
  val StandardCalculationArcrole = "http://www.xbrl.org/2003/arcrole/summation-item"

  // XBRL 2.1 4.11.1.3.1
  val FootnoteLinkbaseArcrole = "http://www.xbrl.org/2003/arcrole/fact-footnote"

  // XBRL 2.1 5.2.6.2
  val GeneralSpecialRole = "http://www.xbrl.org/2003/arcrole/general-special"
  val EssenceAliasRole = "http://www.xbrl.org/2003/arcrole/essence-alias"
  val SimilarTuplesRole = "http://www.xbrl.org/2003/arcrole/similar-tuples"
  val RequiresElementRole = "http://www.xbrl.org/2003/arcrole/requires-element"

  val StandardDefinitionArcroles: Set[String] = Set(GeneralSpecialRole, EssenceAliasRole, SimilarTuplesRole, RequiresElementRole)

  val AllStandardArcroles: Set[String] =
    Set(StandardLabelArcrole, StandardReferenceArcrole, StandardPresentationArcrole, StandardCalculationArcrole) ++
      StandardDefinitionArcroles

  // Arcrole required by linkbaseRef, as defined in XBRL 2.1, 4.3.3
  val LinkbaseRefArcrole = "http://www.w3.org/1999/xlink/properties/linkbase"
}
