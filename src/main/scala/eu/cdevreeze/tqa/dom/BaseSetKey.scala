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
import eu.cdevreeze.tqa.ENames.LinkDefinitionArcEName
import eu.cdevreeze.tqa.ENames.LinkDefinitionLinkEName
import eu.cdevreeze.tqa.ENames.LinkLabelArcEName
import eu.cdevreeze.tqa.ENames.LinkLabelLinkEName
import eu.cdevreeze.tqa.ENames.LinkPresentationArcEName
import eu.cdevreeze.tqa.ENames.LinkPresentationLinkEName
import eu.cdevreeze.tqa.ENames.LinkReferenceArcEName
import eu.cdevreeze.tqa.ENames.LinkReferenceLinkEName
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
    arcEName.namespaceUriOption.contains(LinkNamespace) && extLinkEName.namespaceUriOption.contains(LinkNamespace)
  }

  def withArcrole(newArcrole: String): BaseSetKey = this.copy(arcrole = newArcrole)

  def withExtLinkRole(newExtLinkRole: String): BaseSetKey = this.copy(extLinkRole = newExtLinkRole)
}

object BaseSetKey {

  val StandardElr = "http://www.xbrl.org/2003/role/link"

  def forSummationItemArc(elr: String): BaseSetKey =
    BaseSetKey(LinkCalculationArcEName, "http://www.xbrl.org/2003/arcrole/summation-item", LinkCalculationLinkEName, elr)

  def forParentChildArc(elr: String): BaseSetKey =
    BaseSetKey(LinkPresentationArcEName, "http://www.xbrl.org/2003/arcrole/parent-child", LinkPresentationLinkEName, elr)

  def forGeneralSpecialArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/general-special", LinkDefinitionLinkEName, elr)

  def forEssenceAliasArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/essence-alias", LinkDefinitionLinkEName, elr)

  def forSimilarTuplesArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/similar-tuples", LinkDefinitionLinkEName, elr)

  def forRequiresElementArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://www.xbrl.org/2003/arcrole/requires-element", LinkDefinitionLinkEName, elr)

  def forHypercubeDimensionArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/hypercube-dimension", LinkDefinitionLinkEName, elr)

  def forDimensionDomainArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-domain", LinkDefinitionLinkEName, elr)

  def forDomainMemberArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/domain-member", LinkDefinitionLinkEName, elr)

  def forDimensionDefaultArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/dimension-default", LinkDefinitionLinkEName, elr)

  def forAllArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/all", LinkDefinitionLinkEName, elr)

  def forNotAllArc(elr: String): BaseSetKey =
    BaseSetKey(LinkDefinitionArcEName, "http://xbrl.org/int/dim/arcrole/notAll", LinkDefinitionLinkEName, elr)

  def forConceptLabelArc(elr: String): BaseSetKey =
    BaseSetKey(LinkLabelArcEName, "http://www.xbrl.org/2003/arcrole/concept-label", LinkLabelLinkEName, elr)

  def forConceptReferenceArc(elr: String): BaseSetKey =
    BaseSetKey(LinkReferenceArcEName, "http://www.xbrl.org/2003/arcrole/concept-reference", LinkReferenceLinkEName, elr)

  def forConceptLabelArcWithStandardElr: BaseSetKey = forConceptLabelArc(StandardElr)

  def forConceptReferenceArcWithStandardElr: BaseSetKey = forConceptReferenceArc(StandardElr)
}
