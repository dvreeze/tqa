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

package eu.cdevreeze.tqa.instance.js

import java.time.LocalDate
import java.time.ZonedDateTime

import scala.reflect.classTag

import org.scalatest.funsuite.AnyFunSuite

import eu.cdevreeze.tqa.instance._
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.simple
import eu.cdevreeze.yaidom.jsdom.JsDomDocument
import org.scalajs.dom.experimental.domparser.DOMParser
import org.scalajs.dom.experimental.domparser.SupportedType

/**
 * XBRL instance test case.
 *
 * @author Chris de Vreeze
 */
class XbrlInstanceTest extends AnyFunSuite {

  test("testCreateAndQueryInstance") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(xbrlInstanceString, SupportedType.`text/xml`))

    val xbrlInstance: XbrlInstance = XbrlInstance.build(domDoc.documentElement)

    doTestCreateAndQueryInstance(xbrlInstance)
  }

  test("testCreateAndQueryInstanceWithTimezonesInPeriods") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(editedXbrlInstanceString, SupportedType.`text/xml`))

    val xbrlInstance: XbrlInstance = XbrlInstance.build(domDoc.documentElement)

    doTestCreateAndQueryInstance(xbrlInstance)
  }

  test("testParseNonXbrlInstance") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(nonInstanceString, SupportedType.`text/xml`))

    val nonInstance: XbrliElem = XbrliElem.build(domDoc.documentElement)

    assertResult(true) {
      nonInstance.findAllElemsOrSelf.forall(e => e.isInstanceOf[OtherXbrliElem] || e.isInstanceOf[StandardLoc])
    }
    assertResult(true) {
      nonInstance.filterElemsOrSelf(e => Fact.accepts(e.backingElem)).isEmpty
    }
  }

  test("testParseLayoutModel") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(layoutModelString, SupportedType.`text/xml`))

    val nonInstance: XbrliElem = XbrliElem.build(domDoc.documentElement)

    assertResult(true) {
      nonInstance.findAllElemsOrSelf
        .forall(e => e.isInstanceOf[OtherXbrliElem] || e.isInstanceOf[StartDate] || e.isInstanceOf[EndDate])
    }
    assertResult(true) {
      nonInstance.filterElemsOrSelf(e => Fact.accepts(e.backingElem)).isEmpty
    }

    val startDateParents = nonInstance.findAllElemsOfType(classTag[StartDate]).map(_.backingElem.parent)

    assertResult(2) {
      startDateParents.size
    }

    val copiedStartDateParents = startDateParents.map(e => XbrliElem.build(e))

    assertResult(startDateParents.map(e => resolved.Elem.from(e))) {
      copiedStartDateParents.map(e => resolved.Elem.from(e))
    }

    assertResult(startDateParents.map(_.path)) {
      copiedStartDateParents.map(_.backingElem.path)
    }

    assertResult(nonInstance.findAllElemsOfType(classTag[StartDate]).map(e => resolved.Elem.from(e))) {
      copiedStartDateParents.flatMap(_.findAllElemsOfType(classTag[StartDate])).map(e => resolved.Elem.from(e))
    }
  }

  test("testCreateAndQueryWrappedInstance") {
    val db = new DOMParser()
    val domDoc: JsDomDocument = JsDomDocument.wrapDocument(db.parseFromString(xbrlInstanceString, SupportedType.`text/xml`))

    val unwrappedXbrlInstance: XbrlInstance = XbrlInstance.build(domDoc.documentElement)

    val xbrliElem: XbrliElem = addWrapperRootElem(unwrappedXbrlInstance, emptyWrapperElem)
    val xbrlInstance: XbrlInstance = xbrliElem.findChildElemOfType(classTag[XbrlInstance])(_ => true).get

    doTestCreateAndQueryInstance(xbrlInstance)
  }

  private def doTestCreateAndQueryInstance(xbrlInstance: XbrlInstance): Unit = {
    val tns = "http://xbrl.org/together"

    assertResult(true) {
      xbrlInstance.filterElemsOrSelf(e => Fact.accepts(e.backingElem)).nonEmpty
    }

    assertResult(List.fill(4)(EName(tns, "Primary01"))) {
      xbrlInstance.findAllFacts.map(_.resolvedName)
    }

    assertResult(Set("F_Member03_Member04", "F_Member03", "F_Member04", "F")) {
      xbrlInstance.findAllContexts.groupBy(_.id).keySet
    }

    assertResult(xbrlInstance.findAllContexts.groupBy(_.id).keySet) {
      xbrlInstance.findAllTopLevelItems.map(_.contextRef).toSet
    }

    val fCtx = xbrlInstance.getContextById("F")

    assertResult(LocalDate.parse("2010-01-01").atStartOfDay) {
      fCtx.period.asInstantPeriod.instantDateTime match {
        case dt: ZonedDateTime => dt.toLocalDateTime
        case dt => dt
      }
    }

    val f_03_04Ctx = xbrlInstance.getContextById("F_Member03_Member04")

    assertResult(Map(
      EName(tns, "Dimension01") -> EName(tns, "Member03"),
      EName(tns, "Dimension02") -> EName(tns, "Member04"))) {

      f_03_04Ctx.explicitDimensionMembers
    }
  }

  private val nonInstanceString = """
<?xml version="1.0" encoding="utf-8"?>
<!--
  This file is part of the OCW taxonomy, which is an extension on the Dutch taxonomy (NT, Nederlandse Taxonomie) 
  Intellectual Property of the State of the Netherlands, Ministry of Culture, Education and Sciences (OCW, Ministerie van Onderwijs, Cultuur en Wetenschappen)
  Architecture: NT12
  Version: 20180221
  Released by: Dienst Uitvoering Onderwijs (DUO)
  Release date: Fri Dec 15 17:07:52 CET 2017
-->
<link:linkbase xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink">
	<link:labelLink xlink:role="http://www.xbrl.org/2003/role/link" xlink:type="extended">
		<link:loc xlink:href="ocw-axes.xsd#ocw-dim_ExpenseClaimsManagingDirectorsAxis" xlink:label="ocw-dim_ExpenseClaimsManagingDirectorsAxis_loc" xlink:type="locator"/>
		<link:label id="ocw-dim_ExpenseClaimsManagingDirectorsAxis_terse_en" xlink:type="resource" xlink:role="http://www.xbrl.org/2003/role/terseLabel" xlink:label="ocw-dim_ExpenseClaimsManagingDirectorsAxis_terse_en" xml:lang="en">Expense claims of managing directors</link:label>
		<link:labelArc xlink:from="ocw-dim_ExpenseClaimsManagingDirectorsAxis_loc" xlink:to="ocw-dim_ExpenseClaimsManagingDirectorsAxis_terse_en" xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label"/>
	</link:labelLink>
</link:linkbase>
   """.trim

  private val layoutModelString = """
<?xml version='1.0' encoding='utf-8'?>
<tableModel xmlns="http://xbrl.org/2014/table/model" xmlns:rend="http://www.xbrl.org/table-examples" xmlns:xbrli="http://www.xbrl.org/2003/instance" xml:base="..">
  <tableSet>
    <label>Concept relationship node with multiple relationship sources</label>
    <table>
      <headers axis="y">
        <group>
          <label>Presentation tree for primary items</label>
          <header>
            <cell>
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o2</value>
              </constraint>
            </cell>
            <cell span="4">
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o5</value>
              </constraint>
            </cell>
          </header>
          <header>
            <cell rollup="true"/>
            <cell rollup="true"/>
            <cell>
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o6</value>
              </constraint>
            </cell>
            <cell span="2">
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o7</value>
              </constraint>
            </cell>
          </header>
          <header>
            <cell rollup="true"/>
            <cell rollup="true"/>
            <cell rollup="true"/>
            <cell>
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o8</value>
              </constraint>
            </cell>
            <cell>
              <constraint>
                <aspect>concept</aspect>
                <value>rend:o9</value>
              </constraint>
            </cell>
          </header>
        </group>
      </headers>
      <headers axis="x">
        <group>
          <label>Two columns (periods 2001, 2002)</label>
          <header>
            <cell>
              <label>Label: 2001</label>
              <constraint>
                <aspect>period</aspect>
                <value>
                  <xbrli:startDate>2001-01-01T00:00:00.000Z</xbrli:startDate>
                  <xbrli:endDate>2002-01-01T00:00:00.000Z</xbrli:endDate>
                </value>
              </constraint>
            </cell>
            <cell>
              <label>Label: 2002</label>
              <constraint>
                <aspect>period</aspect>
                <value>
                  <xbrli:startDate>2002-01-01T00:00:00.000Z</xbrli:startDate>
                  <xbrli:endDate>2003-01-01T00:00:00.000Z</xbrli:endDate>
                </value>
              </constraint>
            </cell>
          </header>
        </group>
      </headers>
      <cells axis="z">
        <cells axis="y">
          <cells axis="x">
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o2_2001</fact>
            </cell>
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o2_2002</fact>
            </cell>
          </cells>
          <cells axis="x">
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o5_2001</fact>
            </cell>
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o5_2002</fact>
            </cell>
          </cells>
          <cells axis="x">
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o6_2001</fact>
            </cell>
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o6_2002</fact>
            </cell>
          </cells>
          <cells axis="x">
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o8_2001</fact>
            </cell>
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o8_2002</fact>
            </cell>
          </cells>
          <cells axis="x">
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o9_2001</fact>
            </cell>
            <cell>
              <fact>concept-relationship-node-multiple-sources-instance.xml#o9_2002</fact>
            </cell>
          </cells>
        </cells>
      </cells>
    </table>
  </tableSet>
</tableModel>
   """.trim

  private val xbrlInstanceString = """
<?xml version="1.0" encoding="US-ASCII"?>
<!-- Generated by Fujitsu XWand B0126C -->
<xbrli:xbrl xmlns:together="http://xbrl.org/together" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:iso4217="http://www.xbrl.org/2003/iso4217" xmlns:xbrldi="http://xbrl.org/2006/xbrldi" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xmlns:xbrli="http://www.xbrl.org/2003/instance">
  <link:schemaRef xlink:type="simple" xlink:href="together.xsd"/>
  <xbrli:context id="F_Member03_Member04">
    <xbrli:entity>
      <xbrli:identifier scheme="http://scheme">NA</xbrli:identifier>
      <xbrli:segment>
        <xbrldi:explicitMember dimension="together:Dimension01">together:Member03</xbrldi:explicitMember>
        <xbrldi:explicitMember dimension="together:Dimension02">together:Member04</xbrldi:explicitMember>
      </xbrli:segment>
    </xbrli:entity>
    <xbrli:period>
      <xbrli:instant>2009-12-31</xbrli:instant>
    </xbrli:period>
  </xbrli:context>
  <xbrli:context id="F_Member03">
    <xbrli:entity>
      <xbrli:identifier scheme="http://scheme">NA</xbrli:identifier>
      <xbrli:segment>
        <xbrldi:explicitMember dimension="together:Dimension01">together:Member03</xbrldi:explicitMember>
      </xbrli:segment>
    </xbrli:entity>
    <xbrli:period>
      <xbrli:instant>2009-12-31</xbrli:instant>
    </xbrli:period>
  </xbrli:context>
  <xbrli:context id="F_Member04">
    <xbrli:entity>
      <xbrli:identifier scheme="http://scheme">NA</xbrli:identifier>
      <xbrli:segment>
        <xbrldi:explicitMember dimension="together:Dimension02">together:Member04</xbrldi:explicitMember>
      </xbrli:segment>
    </xbrli:entity>
    <xbrli:period>
      <xbrli:instant>2009-12-31</xbrli:instant>
    </xbrli:period>
  </xbrli:context>
  <xbrli:context id="F">
    <xbrli:entity>
      <xbrli:identifier scheme="http://scheme">NA</xbrli:identifier>
    </xbrli:entity>
    <xbrli:period>
      <xbrli:instant>2009-12-31</xbrli:instant>
    </xbrli:period>
  </xbrli:context>
  <xbrli:unit id="Monetary">
    <xbrli:measure>iso4217:USD</xbrli:measure>
  </xbrli:unit>
  <together:Primary01 decimals="0" contextRef="F_Member03_Member04" unitRef="Monetary">4000</together:Primary01>
  <together:Primary01 decimals="0" contextRef="F_Member03" unitRef="Monetary">2000</together:Primary01>
  <together:Primary01 decimals="0" contextRef="F_Member04" unitRef="Monetary">4000</together:Primary01>
  <together:Primary01 decimals="0" contextRef="F" unitRef="Monetary">2000</together:Primary01>
</xbrli:xbrl>
    """.trim

  private val editedXbrlInstanceString =
    xbrlInstanceString.replace("2009-12-31", "2010-01-01T00:00:00Z").ensuring(_.length > xbrlInstanceString.length)

  private def addWrapperRootElem(rootElem: XbrliElem, emptyWrapperElem: simple.Elem): XbrliElem = {
    val simpleRootElem = simple.Elem.from(rootElem.backingElem)

    val wrapperElem = emptyWrapperElem.plusChild(simpleRootElem).notUndeclaringPrefixes(rootElem.scope)

    XbrliElem.build(indexed.Elem(wrapperElem))
  }

  private val emptyWrapperElem: simple.Elem = {
    simple.Node.emptyElem(QName("wrapperElem"), Scope.Empty)
  }
}
