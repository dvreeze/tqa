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

import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.ENames.LinkCalculationLinkEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelLinkEName
import eu.cdevreeze.tqa.Namespaces.LinkNamespace
import eu.cdevreeze.yaidom.core.EName

/**
 * The key of a base set, made up by the EName and @xlink:arcrole of the arc, along with the EName and @xlink:role of
 * the parent extended link. Base set keys are essential for finding networks of relationships.
 *
 * @author Chris de Vreeze
 */
final case class BaseSetKey(
    val arcEName: EName,
    val arcrole: String,
    val extLinkEName: EName,
    val extLinkRole: String) {

  /**
   * Returns true if this key is for a standard arc in a standard extended link. This check looks at
   * arc and extended link ENames, not at (arc and extended link) roles.
   */
  def isStandard: Boolean = {
    arcEName.namespaceUriOption == Some(LinkNamespace) && extLinkEName.namespaceUriOption == Some(LinkNamespace)
  }
}

object BaseSetKey {

  def forSummationItemArc: BaseSetKey =
    BaseSetKey(LinkCalculationArcEName, "http://www.xbrl.org/2003/arcrole/summation-item", LinkCalculationLinkEName, "http://www.xbrl.org/2003/role/link")

  def forStandardConceptLabel: BaseSetKey =
    BaseSetKey(LinkLabelArcEName, "http://www.xbrl.org/2003/arcrole/concept-label", LinkLabelLinkEName, "http://www.xbrl.org/2003/role/link")
}
