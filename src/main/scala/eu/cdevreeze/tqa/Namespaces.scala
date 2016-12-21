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

/**
 * Well-known Namespaces.
 *
 * @author Chris de Vreeze
 */
object Namespaces {

  /** Namespace of xml:base, xml:lang etc. */
  val XmlNamespace = "http://www.w3.org/XML/1998/namespace"

  val XsNamespace = "http://www.w3.org/2001/XMLSchema"
  val XsiNamespace = "http://www.w3.org/2001/XMLSchema-instance"
  val XLinkNamespace = "http://www.w3.org/1999/xlink"
  val LinkNamespace = "http://www.xbrl.org/2003/linkbase"
  val XbrliNamespace = "http://www.xbrl.org/2003/instance"
  val XbrldtNamespace = "http://xbrl.org/2005/xbrldt"
  val GenNamespace = "http://xbrl.org/2008/generic"
  val LabelNamespace = "http://xbrl.org/2008/label"
  val ReferenceNamespace = "http://xbrl.org/2008/reference"
  val VariableNamespace = "http://xbrl.org/2008/variable"
  val FormulaNamespace = "http://xbrl.org/2008/formula"
  val ValidationNamespace = "http://xbrl.org/2008/validation"
  val InstancesNamespace = "http://xbrl.org/2010/variable/instance"
  val TableNamespace = "http://xbrl.org/2014/table"
  val MsgNamespace = "http://xbrl.org/2010/message"
  val SevNamespace = "http://xbrl.org/2016/assertion-severity"
  val SeveNamespace = "http://xbrl.org/2016/assertion-severity/error"
  val VaNamespace = "http://xbrl.org/2008/assertion/value"
  val EaNamespace = "http://xbrl.org/2008/assertion/existence"
  val CaNamespace = "http://xbrl.org/2008/assertion/consistency"
  val CfNamespace = "http://xbrl.org/2008/filter/concept"
  val BfNamespace = "http://xbrl.org/2008/filter/boolean"
  val DfNamespace = "http://xbrl.org/2008/filter/dimension"
  val EfNamespace = "http://xbrl.org/2008/filter/entity"
  val GfNamespace = "http://xbrl.org/2008/filter/general"
  val MfNamespace = "http://xbrl.org/2008/filter/match"
  val PfNamespace = "http://xbrl.org/2008/filter/period"
  val RfNamespace = "http://xbrl.org/2008/filter/relative"
  val SsfNamespace = "http://xbrl.org/2008/filter/segment-scenario"
  val TfNamespace = "http://xbrl.org/2008/filter/tuple"
  val UfNamespace = "http://xbrl.org/2008/filter/unit"
  val VfNamespace = "http://xbrl.org/2008/filter/value"
  val AcfNamespace = "http://xbrl.org/2010/filter/aspect-cover"
  val CrfNamespace = "http://xbrl.org/2010/filter/concept-relation"
  val GplNamespace = "http://xbrl.org/2013/preferred-label"

  // "Own" namespaces

  val LnkNamespace = "http://www.ebpi.nl/2017/linkbase"
}
