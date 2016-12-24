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

package eu.cdevreeze.tqa

import eu.cdevreeze.tqa.ENames.XbrldtDimensionItemEName
import eu.cdevreeze.tqa.ENames.XbrldtHypercubeItemEName
import eu.cdevreeze.tqa.ENames.XbrliItemEName
import eu.cdevreeze.tqa.ENames.XbrliTupleEName
import eu.cdevreeze.yaidom.core.EName

/**
 * A collection of substitution groups, typically extracted from a taxonomy. It maps substitution groups
 * to their own substitution groups, if any. Well-known substitution groups such as xbrli:item, xbrli:tuple,
 * xbrldt:hypercubeItem and xbrldt:dimensionItem must not occur as keys in the mapping, but typically do occur
 * as mapped values.
 *
 * Cycles are not allowed when following mappings, but this is not checked.
 *
 * @author Chris de Vreeze
 */
final case class SubstitutionGroupMap(val mappings: Map[EName, EName]) {
  require(
    mappings.keySet.filter(SubstitutionGroupMap.StandardConceptSubstitutionGroups).isEmpty,
    s"No standard substitution groups allowed as mapping keys")

  /**
   * A map from substitution groups to other substitution groups directly derived from them.
   * Hence, the reverse of `mappings`.
   */
  val substitutionGroupDerivations: Map[EName, Set[EName]] = {
    mappings.toSeq.groupBy(_._2).mapValues(grp => grp.map(_._1).toSet)
  }
}

object SubstitutionGroupMap {

  val StandardConceptSubstitutionGroups: Set[EName] =
    Set(XbrliItemEName, XbrliTupleEName, XbrldtHypercubeItemEName, XbrldtDimensionItemEName)

  val Empty = SubstitutionGroupMap(Map.empty)
}
