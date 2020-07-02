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

package eu.cdevreeze.tqa.extension.formula.taxonomymodel

import scala.collection.immutable

import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model

/**
 * Converter from formula DOM aspect rules to aspect rules in the model layer.
 *
 * @author Chris de Vreeze
 */
object AspectRuleConverter {

  def convertAspectRule(domAspectRule: dom.FormulaAspect): model.AspectRule = domAspectRule match {
    case fa: dom.ConceptAspect =>
      model.ConceptAspectRule(fa.sourceOption, fa.qnameValueOrExprOption)
    case fa: dom.EntityIdentifierAspect =>
      model.EntityIdentifierAspectRule(fa.sourceOption, fa.schemeExprOption, fa.valueExprOption)
    case fa: dom.PeriodAspect =>
      val periods: immutable.IndexedSeq[model.PeriodAspectRule.Period] = fa.periodElems.map {
        case pe: dom.ForeverElem  => model.PeriodAspectRule.ForeverPeriod
        case pe: dom.InstantElem  => model.PeriodAspectRule.InstantPeriod(pe.valueExprOption)
        case pe: dom.DurationElem => model.PeriodAspectRule.DurationPeriod(pe.startExprOption, pe.endExprOption)
      }

      model.PeriodAspectRule(fa.sourceOption, periods)
    case fa: dom.UnitAspect =>
      model.UnitAspectRule(
        fa.sourceOption,
        fa.multiplyByElems.map(e => model.MultiplyBy(e.sourceOption, e.measureExprOption)),
        fa.divideByElems.map(e => model.DivideBy(e.sourceOption, e.measureExprOption)),
        fa.augmentOption)
    case fa: dom.OccEmptyAspect =>
      model.OccEmptyAspectRule(fa.sourceOption, fa.occ)
    case fa: dom.OccFragmentsAspect =>
      model.OccFragmentsAspectRule(fa.sourceOption, fa.occ)
    case fa: dom.OccXpathAspect =>
      model.OccXPathAspectRule(fa.sourceOption, fa.occ, fa.selectExprOption)
    case fa: dom.ExplicitDimensionAspect =>
      model.ExplicitDimensionAspectRule(fa.sourceOption, fa.dimension, fa.memberElemOption.map(_.qnameValueOrExpr))
    case fa: dom.TypedDimensionAspect =>
      // TODO Typed dimension member!

      model.TypedDimensionAspectRule(fa.sourceOption, fa.dimension, None)
  }
}
