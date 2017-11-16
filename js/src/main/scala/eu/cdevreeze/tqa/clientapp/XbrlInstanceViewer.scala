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

import eu.cdevreeze.tqa.instance.Fact
import eu.cdevreeze.tqa.instance.ItemFact
import eu.cdevreeze.tqa.instance.TupleFact
import eu.cdevreeze.tqa.instance.XbrlInstance
import eu.cdevreeze.tqa.instance.XbrliContext
import eu.cdevreeze.yaidom.convert.JsDomConversions
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.jsdom.JsDomElem
import scalatags.JsDom.all.OptionNode
import scalatags.JsDom.all.SeqFrag
import scalatags.JsDom.all.bindNode
import scalatags.JsDom.all.pre
import scalatags.JsDom.all.stringFrag
import scalatags.JsDom.all.table
import scalatags.JsDom.all.td
import scalatags.JsDom.all.th
import scalatags.JsDom.all.tr

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
        val idoc = indexed.Document(JsDomConversions.convertToDocument(parsedDoc))
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

  private def convertInstanceToTable(xbrlInstance: XbrlInstance): HTMLTableElement = {
    val dimensions = xbrlInstance.allContexts.flatMap(_.explicitDimensionMembers.keySet).distinct

    val headerRow: HTMLTableRowElement =
      tr(
        th("concept"),
        th("period"),
        dimensions.map(dim => th(dim.toString))).render

    val detailRows: Seq[HTMLTableRowElement] =
      xbrlInstance.findAllFacts map { fact =>
        val contextOption = findContext(fact, xbrlInstance)

        tr(
          td(fact.resolvedName.toString),
          td(
            contextOption.map(_.period) map {
              case p if p.isInstantPeriod =>
                p.asInstantPeriod.instantDateTime.toString
              case p if p.isStartEndDatePeriod =>
                p.asStartEndDatePeriod.startDateTime + " - " + p.asStartEndDatePeriod.endDateTime.toString
              case p =>
                ""
            }),
          dimensions map { dim =>
            val dimMemberOption: Option[String] = contextOption flatMap { context =>
              context.explicitDimensionMembers.get(dim).map(_.toString)
            }
            val dimMemberString = dimMemberOption.getOrElse("")

            td(dimMemberString)
          }).render
      }

    table(headerRow +: detailRows).render
  }

  private def findContext(fact: Fact, xbrlInstance: XbrlInstance): Option[XbrliContext] = {
    fact match {
      case item: ItemFact =>
        xbrlInstance.allContextsById.get(item.contextRef)
      case tuple: TupleFact =>
        None
    }
  }
}
