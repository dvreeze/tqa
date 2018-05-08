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

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import scala.collection.immutable
import scala.reflect.classTag

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.base.dom.DefinitionLink
import eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa.base.dom.Import
import eu.cdevreeze.tqa.base.dom.Include
import eu.cdevreeze.tqa.base.dom.Linkbase
import eu.cdevreeze.tqa.base.dom.PresentationLink
import eu.cdevreeze.tqa.base.dom.RoleType
import eu.cdevreeze.tqa.base.dom.SimpleLink
import eu.cdevreeze.tqa.base.dom.TaxonomyDocument
import eu.cdevreeze.tqa.base.dom.XLinkLocator
import eu.cdevreeze.tqa.base.dom.XsdSchema
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.resolved
import eu.cdevreeze.yaidom.simple
import net.sf.saxon.s9api.Processor

/**
 * Experimental taxonomy conversion test case. It uses test data from the NL taxonomy (EZ).
 * The conversion result is an experimental alternative XBRL-valid representation of XBRL taxonomies,
 * decoupling taxonomy documents (by removing URI references) but using "business keys" instead.
 * For example, an XLink locator to a concept is replaced by an XLink resource named lnk:concept.
 *
 * Hopefully this experiment really leads to a useful alternative XBRL taxonomy representation in XBRL
 * (using generic links instead of standard links). It must be trivial to map between real taxonomy
 * documents and these alternative taxonomy documents, and features like networks of relationships must
 * keep working in the alternative representation.
 *
 * TODO For XBRL validity of the experimental taxonomy representation, embed linkbases in schemas that
 * contain the roleTypes and arcroleTypes referred to by roleRefs and arcroleRefs in the same document
 * (so first make sure to keep those roleRefs and arcroleRefs, instead of throwing them away). Also make
 * sure that across documents roleTypes and arcroleTypes for the same URIs are the same. In this way we can
 * make the alternative XBRL taxonomy representation XBRL-valid. Note that this solution still requires
 * href URIs, but at least they are same-document URIs.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class TaxonomyConversionTest extends FunSuite {

  test("testTaxonomyConversion") {
    val convertedDocs: immutable.IndexedSeq[TaxonomyDocument] =
      referenceTaxonomy.taxonomyDocs
        .map { doc =>
          doc.uri match {
            case uri if isEzDocUri(uri) =>
              convertDoc(doc.uri, referenceTaxonomy)
            case _ =>
              doc
          }
        }

    val taxoBuilder = taxonomyBuilder(convertedDocs)

    val newDts = taxoBuilder.build(Set(entryPointUri))

    val docPrinter = eu.cdevreeze.yaidom.print.DocumentPrinterUsingSax.newInstance()

    newDts.taxonomyDocs.sortBy(_.uri.toString).foreach { doc =>
      val simpleDocElem = simple.Elem.from(doc.backingDocument.documentElement)
      val simpleDoc = simple.Document(simpleDocElem).withUriOption(Some(doc.uri))

      if (isEzDocUri(doc.uri)) {
        val xmlString = docPrinter.print(simpleDoc)
        println()
        println(s"Document URI: ${doc.uri}")
        println()
        println(xmlString)
      }
    }

    assertResult(referenceTaxonomy.taxonomyDocs.map(_.uri).toSet.size) {
      newDts.taxonomyDocs.map(_.uri).toSet.size
    }

    assertResult(referenceTaxonomy.findAllConceptDeclarations.map(_.targetEName).toSet) {
      newDts.findAllConceptDeclarations.map(_.targetEName).toSet
    }

    assertResult(
      referenceTaxonomy.findAllParentChildRelationships
        .map(rel => (rel.elr, rel.sourceConceptEName, rel.targetConceptEName)).toSet.ensuring(_.nonEmpty)) {

        val parentChildRels = newDts.filterNonStandardRelationships(_.arcrole == "http://www.xbrl.org/2003/arcrole/parent-child")

        parentChildRels
          .map { rel =>
            val sourceConcept: EName = EName.parse(rel.sourceElem.ensuring(_.resolvedName == AltLinkConceptEName).text)
            val targetConcept: EName = EName.parse(rel.targetElem.ensuring(_.resolvedName == AltLinkConceptEName).text)

            (rel.elr, sourceConcept, targetConcept)
          }
          .toSet
      }

    assertResult(
      referenceTaxonomy.findAllDimensionalRelationships
        .map(rel => (rel.elr, rel.sourceConceptEName, rel.targetConceptEName)).toSet.ensuring(_.nonEmpty)) {

        val dimRels =
          newDts.filterNonStandardRelationships { rel =>
            rel.arcrole == "http://xbrl.org/int/dim/arcrole/all" ||
              rel.arcrole == "http://xbrl.org/int/dim/arcrole/domain-member"
          }

        dimRels
          .map { rel =>
            val sourceConcept: EName = EName.parse(rel.sourceElem.ensuring(_.resolvedName == AltLinkConceptEName).text)
            val targetConcept: EName = EName.parse(rel.targetElem.ensuring(_.resolvedName == AltLinkConceptEName).text)

            (rel.elr, sourceConcept, targetConcept)
          }
          .toSet
      }

    // Essential tests, to show that the desired decoupling has been achieved.
    // In short: no locators, no simple links (except in entry point), and no xs:import schemaLocation (except in entry point)

    assertResult(Nil) {
      newDts.taxonomyDocs.filter(d => isEzDocUri(d.uri)).map(_.documentElement)
        .flatMap(e => e.findAllElemsOrSelfOfType(classTag[XLinkLocator]))
    }

    assertResult(Nil) {
      newDts.taxonomyDocs.filter(d => isEzDocUri(d.uri)).map(_.documentElement)
        .flatMap(e => e.findAllElemsOrSelfOfType(classTag[Include]))
    }

    assertResult(Set(entryPointUri)) {
      newDts.taxonomyDocs.filter(d => isEzDocUri(d.uri)).map(_.documentElement)
        .filter(e => e.filterElemsOrSelfOfType(classTag[Import])(_.attributeOption(ENames.SchemaLocationEName).nonEmpty).nonEmpty)
        .map(_.docUri).toSet
    }

    assertResult(Set(entryPointUri)) {
      newDts.taxonomyDocs.filter(d => isEzDocUri(d.uri)).map(_.documentElement)
        .filter(e => e.findAllElemsOrSelfOfType(classTag[SimpleLink]).nonEmpty)
        .map(_.docUri).toSet
    }
  }

  // Conversions to alternative XBRL taxonomy representation, assuming no nested linkbases and assuming only top-level schemas and linkbases

  private val AltLinkNs = "http://www.ebpi.nl/2018/linkbase"

  private val AltLinkConceptEName = EName(AltLinkNs, "concept")
  private val AltLinkRoleTypeEName = EName(AltLinkNs, "roleType")

  private val AltLinkPresentationLinkEName = EName(AltLinkNs, "presentationLink")
  private val AltLinkDefinitionLinkEName = EName(AltLinkNs, "definitionLink")

  private val AltLinkPresentationArcEName = EName(AltLinkNs, "presentationArc")
  private val AltLinkDefinitionArcEName = EName(AltLinkNs, "definitionArc")

  private def isEzDocUri(uri: URI): Boolean = {
    uri.toString.contains("www.nltaxonomie.nl") && uri.toString.contains("ez")
  }

  private def convertDoc(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    docUri match {
      case TaxonomyConversionTest.this.entryPointUri =>
        convertEntryPoint(docUri, dts)
      case _ =>
        val doc: TaxonomyDocument = dts.taxonomyBase.taxonomyDocUriMap.apply(docUri)

        doc.documentElement match {
          case rootElem: XsdSchema =>
            convertXsd(docUri, dts)
          case rootElem: Linkbase =>
            if (rootElem.findAllElemsOfType(classTag[PresentationLink]).nonEmpty) {
              convertPresentationLinkbase(docUri, dts)
            } else if (rootElem.findAllElemsOfType(classTag[DefinitionLink]).nonEmpty) {
              convertDefinitionLinkbase(docUri, dts)
            } else if (rootElem.filterElems(_.localName == "linkroleOrder").nonEmpty) {
              convertGenericLinkroleOrderLinkbase(docUri, dts)
            } else {
              sys.error(s"Cannot convert taxonomy document $docUri")
            }
          case _ =>
            sys.error(s"Cannot convert taxonomy document $docUri")
        }
    }
  }

  private def convertEntryPoint(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    // Check there are only linkbaseRefs and xs:import elements
    // Remove xlink:role from linkbaseRefs, and remove schemaLocation from xs:import elements
    // Add linkbaseRefs and xs:import elements in order to include all documents in the DTS

    val schema = dts.taxonomyBase.taxonomyDocUriMap(docUri).documentElement.asInstanceOf[XsdSchema]

    val scope = schema.scope ++ Scope.from("lnk" -> AltLinkNs)

    val schemaElem = resolved.Elem.from(schema)

    val schemaDocUris = dts.rootElems.filter(_.isInstanceOf[XsdSchema]).map(_.docUri).sortBy(_.toString)
    val linkbaseDocUris = dts.rootElems.filter(_.isInstanceOf[Linkbase]).map(_.docUri).sortBy(_.toString)

    // Assuming xs:appinfo exists

    val editedSchemaElem = schemaElem
      .transformElemsToNodeSeq { elm =>
        elm.resolvedName match {
          case ENames.XsAppinfoEName =>
            Vector(
              elm.withChildren(
                linkbaseDocUris.map { lkbUri =>
                  resolved.Node.emptyElem(ENames.LinkLinkbaseRefEName)
                    .plusAttribute(ENames.XLinkArcroleEName, "http://www.w3.org/1999/xlink/properties/linkbase")
                    .plusAttribute(ENames.XLinkHrefEName, lkbUri.toString)
                    .plusAttribute(ENames.XLinkRoleEName, "http://www.xbrl.org/2008/role/link")
                    .plusAttribute(ENames.XLinkTypeEName, "simple")
                }))
          case _ =>
            Vector(elm)
        }
      }
      .transformElemsOrSelf { elm =>
        elm.resolvedName match {
          case ENames.XsSchemaEName =>
            elm.filteringChildren {
              case e: resolved.Elem => e.resolvedName != ENames.XsImportEName
              case n => true
            }.plusChildren(
              schemaDocUris.map { xsdUri =>
                resolved.Node.emptyElem(ENames.XsImportEName)
                  .plusAttribute(
                    ENames.NamespaceEName,
                    dts.taxonomyBase.taxonomyDocUriMap(xsdUri).documentElement.attribute(ENames.TargetNamespaceEName))
                  .plusAttribute(ENames.SchemaLocationEName, xsdUri.toString)
              })
          case _ =>
            elm
        }
      }

    val editedSimpleSchemaElem = prettify(simple.Elem.from(editedSchemaElem, scope), scope)

    TaxonomyDocument.build(
      indexed.Document(docUri, simple.Document(editedSimpleSchemaElem)))
  }

  private def convertXsd(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    // May contain element declarations etc., and roleTypes etc. They can be left alone.
    // The xs:import elements must be adapted (removing the schemaLocation). LinkbaseRefs must be removed.

    val schema = dts.taxonomyBase.taxonomyDocUriMap(docUri).documentElement.asInstanceOf[XsdSchema]

    val scope = schema.scope ++ Scope.from("lnk" -> AltLinkNs)

    val schemaElem = resolved.Elem.from(schema)

    val editedSchemaElem = schemaElem
      .transformElemsToNodeSeq { elm =>
        elm.resolvedName match {
          case ENames.XsImportEName =>
            Vector(elm.minusAttribute(ENames.SchemaLocationEName))
          case ENames.LinkLinkbaseRefEName =>
            Vector()
          case _ =>
            Vector(elm)
        }
      }

    val editedSimpleSchemaElem = prettify(simple.Elem.from(editedSchemaElem, scope), scope)

    TaxonomyDocument.build(
      indexed.Document(docUri, simple.Document(editedSimpleSchemaElem)))
  }

  private def convertPresentationLinkbase(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    // Replace roleRefs and arcroleRefs, make extended link generic, make arcs generic arcs (adapting the arcrole?),
    // and replace locators to concepts by XLink resources with EName-valued lnk:concept child element text.

    val linkbase = dts.taxonomyBase.taxonomyDocUriMap(docUri).documentElement.asInstanceOf[Linkbase]

    // Assuming only 1 relationship per arc

    val scope = linkbase.scope ++ Scope.from("lnk" -> AltLinkNs, "gen" -> Namespaces.GenNamespace)

    val linkbaseElem = resolved.Elem.from(linkbase)

    val editedLinkbaseElem = linkbaseElem
      .transformElemsToNodeSeq { elm =>
        elm.resolvedName match {
          case ENames.LinkRoleRefEName =>
            Vector()
          case ENames.LinkArcroleRefEName =>
            Vector()
          case ENames.LinkPresentationLinkEName =>
            Vector(elm.copy(resolvedName = AltLinkPresentationLinkEName))
          case ENames.LinkPresentationArcEName =>
            Vector(elm.copy(resolvedName = AltLinkPresentationArcEName))
          case ENames.LinkLocEName =>
            // Assuming no XML Base usage
            val elemUri = docUri.resolve(URI.create(elm.attribute(ENames.XLinkHrefEName)))
            val conceptEName: EName =
              dts.taxonomyBase.elemUriMap(elemUri).asInstanceOf[GlobalElementDeclaration].targetEName

            Vector(
              resolved.Node.textElem(
                AltLinkConceptEName,
                Map(
                  ENames.XLinkLabelEName -> elm.attribute(ENames.XLinkLabelEName),
                  ENames.XLinkTypeEName -> "resource"),
                conceptEName.toString))
          case _ =>
            Vector(elm)
        }
      }

    val editedSimpleLinkbaseElem = prettify(simple.Elem.from(editedLinkbaseElem, scope), scope)

    TaxonomyDocument.build(
      indexed.Document(docUri, simple.Document(editedSimpleLinkbaseElem)))
  }

  private def convertDefinitionLinkbase(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    // See convertPresentationLinkbase

    val linkbase = dts.taxonomyBase.taxonomyDocUriMap(docUri).documentElement.asInstanceOf[Linkbase]

    // Assuming only dimensional relationships
    // Assuming only 1 relationship per arc

    val scope = linkbase.scope ++ Scope.from("lnk" -> AltLinkNs, "gen" -> Namespaces.GenNamespace)

    val linkbaseElem = resolved.Elem.from(linkbase)

    val editedLinkbaseElem = linkbaseElem
      .transformElemsToNodeSeq { elm =>
        elm.resolvedName match {
          case ENames.LinkRoleRefEName =>
            Vector()
          case ENames.LinkArcroleRefEName =>
            Vector()
          case ENames.LinkDefinitionLinkEName =>
            Vector(elm.copy(resolvedName = AltLinkDefinitionLinkEName))
          case ENames.LinkDefinitionArcEName =>
            Vector(elm.copy(resolvedName = AltLinkDefinitionArcEName))
          case ENames.LinkLocEName =>
            // Assuming no XML Base usage
            val elemUri = docUri.resolve(URI.create(elm.attribute(ENames.XLinkHrefEName)))
            val conceptEName: EName =
              dts.taxonomyBase.elemUriMap(elemUri).asInstanceOf[GlobalElementDeclaration].targetEName

            Vector(
              resolved.Node.textElem(
                AltLinkConceptEName,
                Map(
                  ENames.XLinkLabelEName -> elm.attribute(ENames.XLinkLabelEName),
                  ENames.XLinkTypeEName -> "resource"),
                conceptEName.toString))
          case _ =>
            Vector(elm)
        }
      }

    val editedSimpleLinkbaseElem = prettify(simple.Elem.from(editedLinkbaseElem, scope), scope)

    TaxonomyDocument.build(
      indexed.Document(docUri, simple.Document(editedSimpleLinkbaseElem)))
  }

  private def convertGenericLinkroleOrderLinkbase(docUri: URI, dts: BasicTaxonomy): TaxonomyDocument = {
    // See convertPresentationLinkbase, but adapted for generic links and role types as sources

    val linkbase = dts.taxonomyBase.taxonomyDocUriMap(docUri).documentElement.asInstanceOf[Linkbase]

    val scope = linkbase.scope ++ Scope.from("lnk" -> AltLinkNs)

    val linkbaseElem = resolved.Elem.from(linkbase)

    val editedLinkbaseElem = linkbaseElem
      .transformElemsToNodeSeq { elm =>
        elm.resolvedName match {
          case ENames.LinkRoleRefEName =>
            Vector()
          case ENames.LinkArcroleRefEName =>
            Vector()
          case ENames.LinkLocEName =>
            // Assuming no XML Base usage
            val elemUri = docUri.resolve(URI.create(elm.attribute(ENames.XLinkHrefEName)))
            val roleUri: String =
              dts.taxonomyBase.elemUriMap(elemUri).asInstanceOf[RoleType].roleUri

            Vector(
              resolved.Node.textElem(
                AltLinkRoleTypeEName,
                Map(
                  ENames.XLinkLabelEName -> elm.attribute(ENames.XLinkLabelEName),
                  ENames.XLinkTypeEName -> "resource"),
                roleUri))
          case _ =>
            Vector(elm)
        }
      }

    val editedSimpleLinkbaseElem = prettify(simple.Elem.from(editedLinkbaseElem, scope), scope)

    TaxonomyDocument.build(
      indexed.Document(docUri, simple.Document(editedSimpleLinkbaseElem)))
  }

  private def prettify(elem: simple.Elem, scope: Scope): simple.Elem = {
    elem
      .notUndeclaringPrefixes(scope)
      .prettify(2)
  }

  // Bootstrapping

  private val processor = new Processor(false)

  private def createDts(entryPointUri: URI, zipFileUri: URI): BasicTaxonomy = {
    val taxoBuilder = taxonomyBuilder(zipFileUri)

    taxoBuilder.build(Set(entryPointUri))
  }

  private def docBuilder(zipFileUri: URI): SaxonDocumentBuilder = {
    val zipFile = new File(zipFileUri)

    val catalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(None, "http://www.nltaxonomie.nl/", "taxonomie/www.nltaxonomie.nl/"),
          SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", "taxonomie/www.xbrl.org/"),
          SimpleCatalog.UriRewrite(None, "http://www.w3.org/", "taxonomie/www.w3.org/")))

    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      UriResolvers.forZipFileUsingCatalog(new ZipFile(zipFile), catalog))
  }

  private def taxonomyBuilder(zipFileUri: URI): TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder(zipFileUri)).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    taxoBuilder
  }

  private def taxonomyBuilder(taxoDocs: immutable.IndexedSeq[TaxonomyDocument]): TaxonomyBuilder = {
    val documentCollector = DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoDocMap: Map[URI, TaxonomyDocument] = taxoDocs.groupBy(_.uri).mapValues(_.head)

    val docBuilder = new DocumentBuilder {
      type BackingDoc = BackingDocumentApi

      def build(uri: URI): BackingDoc = {
        taxoDocMap.get(uri).map(_.backingDocument).getOrElse(sys.error(s"Missing document $uri"))
      }
    }

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(docBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    taxoBuilder
  }

  // "Reference taxonomy"

  private val entryPointUri =
    URI.create("http://www.nltaxonomie.nl/nt12/ez/20170714.a/entrypoints/ez-rpt-ncgc-nederlandse-corporate-governance-code.xsd")

  private val referenceTaxonomy: BasicTaxonomy = {
    val zipFileUri = classOf[TaxonomyConversionTest].getResource("/taxonomy-zip-files/taxonomie-ez-no-labels.zip").toURI

    val dts = createDts(entryPointUri, zipFileUri)

    dts.ensuring(_.relationships.size > 200, s"Expected more than 200 relationships but found ${dts.relationships.size} relationships")
  }
}
