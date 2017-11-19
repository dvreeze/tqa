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

package eu.cdevreeze.tqa.clientapp

import java.net.URI
import java.time.LocalDateTime

import scala.collection.immutable
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Failure
import scala.util.Success

import org.scalajs.dom.experimental.domparser.DOMParser
import org.scalajs.dom.experimental.domparser.SupportedType
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLDivElement
import org.scalajs.dom.raw.HTMLTableElement
import org.scalajs.dom.raw.HTMLTableRowElement
import org.scalajs.dom.raw.HTMLTableSectionElement
import org.scalajs.dom.raw.MouseEvent
import org.scalajs.dom.window

import eu.cdevreeze.tqa.aspect.Aspect
import eu.cdevreeze.tqa.instance.Fact
import eu.cdevreeze.tqa.instance.ItemFact
import eu.cdevreeze.tqa.instance.NumericItemFact
import eu.cdevreeze.tqa.instance.TupleFact
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.tqa.instance.XbrliContext
import eu.cdevreeze.tqa.instance.XbrliUnit
import eu.cdevreeze.yaidom.convert.JsDomConversions
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.jsdom.JsDomElem
import scalatags.JsDom.all.SeqFrag
import scalatags.JsDom.all.bindNode
import scalatags.JsDom.all.a
import scalatags.JsDom.all.caption
import scalatags.JsDom.all.cls
import scalatags.JsDom.all.href
import scalatags.JsDom.all.onclick
import scalatags.JsDom.all.pre
import scalatags.JsDom.all.stringAttr
import scalatags.JsDom.all.stringFrag
import scalatags.JsDom.all.table
import scalatags.JsDom.all.tbody
import scalatags.JsDom.all.td
import scalatags.JsDom.all.th
import scalatags.JsDom.all.thead
import scalatags.JsDom.all.tr
import scalatags.JsDom.all._

/**
 * Program that retrieves and shows an XBRL instance.
 *
 * Example XBRL instance: http://www.xbrlsite.com/examples/comprehensiveexample/2008-04-18/sample-Instance-Proof.xml.
 *
 * @author Chris de Vreeze
 */
@JSExportTopLevel("XbrlInstanceViewer")
object XbrlInstanceViewer {

  @JSExport("retrieveInstance")
  def retrieveInstance(xbrlInstanceUri: String, div: HTMLDivElement): Unit = {
    Ajax.get(xbrlInstanceUri) onComplete {
      case Success(xhr) =>
        // Yaidom can also help in manipulating the browser DOM
        JsDomElem(div).children.reverse.foreach(e => div.removeChild(e.wrappedNode))

        val responseXml = xhr.responseText

        println(s"Received response XML (${responseXml.length} characters)")

        val db = new DOMParser()
        val parsedDoc = db.parseFromString(responseXml, SupportedType.`text/xml`)

        // Converting the JS DOM tree to a native yaidom indexed tree improves performance,
        // because of much faster Path computations, which are used under the hood all the time
        // when querying the instance (for facts, for example).

        val idoc = indexed.Document(URI.create(xbrlInstanceUri), JsDomConversions.convertToDocument(parsedDoc))
        val xbrlInstance = XbrlInstance(idoc.documentElement)

        val facts = xbrlInstance.findAllFacts

        println(s"Number of XML elements in the instance: ${xbrlInstance.findAllElemsOrSelf.size}")
        println(s"Number of facts in the instance: ${facts.size}")

        val table = convertInstanceToTable(xbrlInstance)

        println(s"Created HTML table with ${table.rows.length} rows")

        div.appendChild(table)
      case Failure(xhr) =>
        println(s"Could not retrieve XBRL instance at URL '$xbrlInstanceUri'")

        div.appendChild(pre("<No XBRL instance found>").render)
    }
  }

  /**
   * Converts the XBRL instance to an HTML table, using Bootstrap for styling.
   */
  private def convertInstanceToTable(xbrlInstance: XbrlInstance): HTMLTableElement = {
    val tableHead: HTMLTableSectionElement =
      thead(
        tr(
          th(cls := "col-md-3")("concept"),
          th(cls := "col-md-9")("aspect values"))).render

    val detailRows: Seq[HTMLTableRowElement] =
      xbrlInstance.findAllFacts map { fact =>
        val contextOption = findContext(fact, xbrlInstance)
        val unitOption = findUnit(fact, xbrlInstance)

        val onclickHandler = { ev: MouseEvent =>
          window.alert(s"TODO Show minimal XBRL instance containing fact ${fact.resolvedName} in a popup")
        }

        tr(
          td(
            a(href := "#", onclick := onclickHandler)(fact.resolvedName.localPart)),
          td(convertAspectsToTable(fact, contextOption, unitOption))).render
      }

    table(cls := "table table-bordered table-condensed")(
      caption("XBRL Instance"),
      tableHead,
      tbody(detailRows)).render
  }

  private def findContext(fact: Fact, xbrlInstance: XbrlInstance): Option[XbrliContext] = {
    fact match {
      case item: ItemFact =>
        xbrlInstance.allContextsById.get(item.contextRef)
      case tuple: TupleFact =>
        None
    }
  }

  private def findUnit(fact: Fact, xbrlInstance: XbrlInstance): Option[XbrliUnit] = {
    fact match {
      case numericItem: NumericItemFact =>
        xbrlInstance.allUnitsById.get(numericItem.unitRef)
      case _ =>
        None
    }
  }

  private def convertAspectsToTable(
    fact: Fact,
    contextOption: Option[XbrliContext],
    unitOption: Option[XbrliUnit]): HTMLTableElement = {

    val coreAspectValues: Map[Aspect, Any] = extractCoreAspectValues(fact, contextOption, unitOption)

    val coreAspectValueSeq: immutable.IndexedSeq[(Aspect, Any)] =
      coreAspects.flatMap(as => coreAspectValues.get(as).map(v => as -> v))

    val dimensionMembers: Map[EName, EName] =
      contextOption.map(_.explicitDimensionMembers).getOrElse(Map())

    val dimensionMemberSeq =
      dimensionMembers.toIndexedSeq.sortBy(_._1.toString)

    table(cls := "table table-bordered table-condensed")(
      thead(
        tr(
          th(cls := "col-md-5")("aspect"),
          th(cls := "col-md-1")(""),
          th(cls := "col-md-6")("value"))),
      tbody(
        coreAspectValueSeq flatMap {
          case (Aspect.EntityIdentifierAspect, optionalSchemeValue) =>
            val optSchemeValue: Option[(String, String)] =
              optionalSchemeValue.asInstanceOf[Option[(String, String)]]

            val schemeString = optSchemeValue.map(_._1).getOrElse("")
            val valueString = optSchemeValue.map(_._2).getOrElse("")

            Seq(
              tr(
                td(Aspect.EntityIdentifierAspect.toString),
                td("scheme"),
                td(schemeString)),
              tr(
                td(""),
                td("value"),
                td(valueString)))
          case (Aspect.PeriodAspect, optStartOptEnd) =>
            val optionalStartOptionalEnd: (Option[LocalDateTime], Option[LocalDateTime]) =
              optStartOptEnd.asInstanceOf[(Option[LocalDateTime], Option[LocalDateTime])]

            val periodStartString = optionalStartOptionalEnd._1.map(_.toString).getOrElse("")
            val periodEndString = optionalStartOptionalEnd._2.map(_.toString).getOrElse("")

            val startDateLabel = (periodStartString, periodEndString) match {
              case ("", "") => ""
              case (_, "")  => "instant"
              case (_, _)   => "start"
            }

            val firstRow =
              tr(
                td(Aspect.PeriodAspect.toString),
                td(startDateLabel),
                td(periodStartString))

            val secondRowOption =
              if (periodEndString.isEmpty) {
                None
              } else {
                Some(tr(
                  td(""),
                  td("end"),
                  td(periodEndString)))
              }

            firstRow :: secondRowOption.toList
          case (Aspect.UnitAspect, numsDenoms) =>
            val numeratorsDenominators =
              numsDenoms.asInstanceOf[(immutable.IndexedSeq[EName], immutable.IndexedSeq[EName])]
            val numerators = numeratorsDenominators._1
            val denominators = numeratorsDenominators._2

            val firstMeasureString =
              if (numerators.isEmpty) "" else numerators.head.toString

            val firstRow =
              tr(
                td(Aspect.UnitAspect.toString),
                td("numerator"),
                td(firstMeasureString))

            val nonFirstNumeratorRows =
              numerators.drop(1) map { measure =>
                tr(
                  td(""),
                  td("numerator"),
                  td(measure.toString))
              }

            val denominatorRows =
              denominators map { measure =>
                tr(
                  td(""),
                  td("denominator"),
                  td(measure.toString))
              }

            firstRow +: (nonFirstNumeratorRows ++ denominatorRows)
          case (aspect, value) =>
            Seq(tr(
              td(aspect.toString),
              td(""),
              td(value.toString)))
        },
        dimensionMemberSeq map {
          case (dim, mem) =>
            tr(
              td(dim.toString),
              td(""),
              td(mem.toString))
        })).render
  }

  private def extractCoreAspectValues(
    fact: Fact,
    contextOption: Option[XbrliContext],
    unitOption: Option[XbrliUnit]): Map[Aspect, Any] = {

    import eu.cdevreeze.tqa.aspect.Aspect._

    val entityIdentifierOption =
      contextOption.map(ctx => (ctx.entity.identifierScheme, ctx.entity.identifierValue))

    val optStartOptEnd: (Option[LocalDateTime], Option[LocalDateTime]) =
      contextOption.map(_.period) map {
        case p if p.isInstantPeriod =>
          (Some(p.asInstantPeriod.instantDateTime), None)
        case p if p.isStartEndDatePeriod =>
          (Some(p.asStartEndDatePeriod.startDateTime), Some(p.asStartEndDatePeriod.endDateTime))
        case p =>
          (None, None)
      } getOrElse {
        (None, None)
      }

    val numsDenoms: (immutable.IndexedSeq[EName], immutable.IndexedSeq[EName]) =
      unitOption.map(u => (u.numeratorMeasures, u.denominatorMeasures)).
        getOrElse((immutable.IndexedSeq(), immutable.IndexedSeq()))

    Map[Aspect, Any](
      ConceptAspect -> fact.resolvedName,
      LocationAspect -> fact.path.toResolvedCanonicalXPath,
      EntityIdentifierAspect -> entityIdentifierOption,
      PeriodAspect -> optStartOptEnd,
      UnitAspect -> numsDenoms)
  }

  // TODO Add missing core aspects

  private val coreAspects: immutable.IndexedSeq[Aspect] = {
    import Aspect._

    Vector(
      ConceptAspect,
      LocationAspect,
      EntityIdentifierAspect,
      PeriodAspect,
      UnitAspect)
  }
}
