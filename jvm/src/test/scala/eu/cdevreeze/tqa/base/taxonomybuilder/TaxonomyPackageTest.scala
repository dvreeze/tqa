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

package eu.cdevreeze.tqa.base.taxonomybuilder

import java.net.URI

import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.simple
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy package element querying test case. It uses test data from the BT12 taxonomy package.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyPackageTest extends FunSuite {

  test("testQueryTaxonomyPackage") {
    val taxoPkgFileUri = classOf[TaxonomyPackageTest].getResource("/taxonomyPackage.xml").toURI

    val taxonomyPackage: TaxonomyPackage = TaxonomyPackage.build(docBuilder.build(taxoPkgFileUri).documentElement)

    doTestQueryTaxonomyPackage(taxonomyPackage)
  }

  test("testParseNonTaxonomyPackage") {
    val uri = classOf[TaxonomyPackageTest].getResource("/sample-instances/sample-xbrl-instance.xml").toURI

    val taxonomyPackageElem: TaxonomyPackageElem = TaxonomyPackageElem.build(docBuilder.build(uri).documentElement)

    assertResult(true) {
      taxonomyPackageElem.findAllElemsOrSelf.forall(_.isInstanceOf[OtherTaxonomyPackageElem])
    }
    assertResult(true) {
      taxonomyPackageElem.findAllElemsOrSelf.flatMap(_.relativePathOption).isEmpty
    }
  }

  test("testQueryWrappedTaxonomyPackage") {
    val taxoPkgFileUri = classOf[TaxonomyPackageTest].getResource("/taxonomyPackage.xml").toURI

    val unwrappedTaxonomyPackage: TaxonomyPackage = TaxonomyPackage.build(docBuilder.build(taxoPkgFileUri).documentElement)

    val taxonomyPackageElem: TaxonomyPackageElem = addWrapperRootElem(unwrappedTaxonomyPackage, emptyWrapperElem)
    val taxonomyPackage: TaxonomyPackage = taxonomyPackageElem.findChildElemOfType(classTag[TaxonomyPackage])(_ => true).get

    doTestQueryTaxonomyPackage(taxonomyPackage)
  }

  private def doTestQueryTaxonomyPackage(taxonomyPackage: TaxonomyPackage): Unit = {
    assertResult(URI.create("urn:banken-taxonomie-12")) {
      taxonomyPackage.getIdentifier.value
    }
    assertResult(List(
      "Bankentaxonomie 12",
      "De Bankentaxonomie bevat de bankenrapportages binnen Standard Business Reporting in Nederland.")) {

      taxonomyPackage.findAllDocumentationGroups.map(_.value)
    }
    assertResult(List("Bankentaxonomie 12")) {
      taxonomyPackage.findAllNames.map(_.value)
    }
    assertResult("20171201") {
      taxonomyPackage.findVersion.map(_.value).getOrElse("")
    }
    assertResult(List("Financiele Rapportage Cooperatief")) {
      taxonomyPackage.findAllPublishers.map(_.value)
    }
    assertResult(Some(URI.create("https://www.sbrbanken.nl"))) {
      taxonomyPackage.findPublisherUrl.map(_.value)
    }
    assertResult(Some("NL")) {
      taxonomyPackage.findPublisherCountry.map(_.value)
    }
    assertResult(Some("2017-12-01")) {
      taxonomyPackage.findPublicationDate.map(_.value.toString)
    }

    assertResult(32) {
      taxonomyPackage.filterEntryPoints(_ => true).size
    }
    assertResult(5) {
      taxonomyPackage.filterEntryPoints(
        _.findAllDocumentationGroups.exists(dg => dg.value.contains("IHZ") && dg.value.contains("aangifte"))).size
    }
    assertResult(true) {
      taxonomyPackage.findEntryPoint(
        _.findAllNames.map(_.value).contains("IHZ-aangifte 2017")).nonEmpty
    }

    assertResult(List(URI.create("https://www.sbrbanken.nl/bt12/frc/20171201/entrypoints/frc-rpt-ihz-aangifte-2017.xsd"))) {
      taxonomyPackage.getEntryPoint(
        _.findAllNames.map(_.value).contains("IHZ-aangifte 2017")).findAllEntryPointHrefs
    }

    val entryPoint = taxonomyPackage.getEntryPoint(_.findAllNames.headOption.map(_.value).contains("IHZ-aangifte 2017"))

    assertResult("IHZ-aangifte 2017") {
      entryPoint.findAllNames.headOption.map(_.value).getOrElse("")
    }
    assertResult("De inkomstenbelasting 2017 ter deponering bij de banken.") {
      entryPoint.findAllDescriptions.headOption.map(_.value).getOrElse("")
    }
    assertResult("20171201") {
      entryPoint.findVersion.map(_.value).getOrElse("")
    }
    assertResult(List(URI.create("https://www.sbrbanken.nl/bt12/frc/20171201/entrypoints/frc-rpt-ihz-aangifte-2017.xsd"))) {
      entryPoint.findAllEntryPointDocuments.map(_.href)
    }
    assertResult(List(URI.create("https://www.sbrbanken.nl/bt12/frc/20171201/entrypoints/frc-rpt-ihz-aangifte-2017.xsd"))) {
      entryPoint.findAllEntryPointHrefs
    }
  }

  private def addWrapperRootElem(rootElem: TaxonomyPackageElem, emptyWrapperElem: simple.Elem): TaxonomyPackageElem = {
    val simpleRootElem = simple.Elem.from(rootElem.backingElem)

    val wrapperElem = emptyWrapperElem.plusChild(simpleRootElem).notUndeclaringPrefixes(rootElem.scope)

    TaxonomyPackageElem.build(indexed.Elem(wrapperElem))
  }

  private val processor = new Processor(false)

  private val docBuilder: SaxonDocumentBuilder = {
    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      UriResolvers.fromUriConverter(UriConverters.identity))
  }

  private val emptyWrapperElem: simple.Elem = {
    simple.Node.emptyElem(QName("wrapperElem"), Scope.Empty)
  }
}
