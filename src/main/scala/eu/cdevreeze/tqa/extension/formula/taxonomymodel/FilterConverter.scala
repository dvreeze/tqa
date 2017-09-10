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

import eu.cdevreeze.tqa.extension.formula.dom
import eu.cdevreeze.tqa.extension.formula.model
import eu.cdevreeze.tqa.extension.formula.taxonomy.BasicFormulaTaxonomy

/**
 * Converter from formula taxonomy filters to filters in the model layer.
 *
 * @author Chris de Vreeze
 */
final class FilterConverter(val formulaTaxonomy: BasicFormulaTaxonomy) {

  def convertFilter(domFilter: dom.Filter): model.Filter = {
    ???
  }

  def convertConceptFilter(domFilter: dom.ConceptFilter): model.ConceptFilter = {
    ???
  }

  def convertBooleanFilter(domFilter: dom.BooleanFilter): model.BooleanFilter = {
    ???
  }

  def convertDimensionFilter(domFilter: dom.DimensionFilter): model.DimensionFilter = {
    ???
  }

  def convertEntityFilter(domFilter: dom.EntityFilter): model.EntityFilter = {
    ???
  }

  def convertGeneralFilter(domFilter: dom.GeneralFilter): model.GeneralFilter = {
    ???
  }

  def convertMatchFilter(domFilter: dom.MatchFilter): model.MatchFilter = {
    ???
  }

  def convertPeriodAspectFilter(domFilter: dom.PeriodAspectFilter): model.PeriodAspectFilter = {
    ???
  }

  def convertRelativeFilter(domFilter: dom.RelativeFilter): model.RelativeFilter = {
    ???
  }

  def convertSegmentScenarioFilter(domFilter: dom.SegmentScenarioFilter): model.SegmentScenarioFilter = {
    ???
  }

  def convertTupleFilter(domFilter: dom.TupleFilter): model.TupleFilter = {
    ???
  }

  def convertUnitFilter(domFilter: dom.UnitFilter): model.UnitFilter = {
    ???
  }

  def convertValueFilter(domFilter: dom.ValueFilter): model.ValueFilter = {
    ???
  }

  def convertAspectCoverFilter(domFilter: dom.AspectCoverFilter): model.AspectCoverFilter = {
    ???
  }

  def convertConceptRelationFilter(domFilter: dom.ConceptRelationFilter): model.ConceptRelationFilter = {
    ???
  }
}
