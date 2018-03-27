
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.LoadDts"

// Taking TQA version 0.8.0-M1

import $ivy.`net.sf.saxon:Saxon-HE:9.8.0-10`, net.sf.saxon.s9api.Processor
import $ivy.`eu.cdevreeze.tqa::tqa:0.8.0-M1`

// Imports that (must) remain available after this initialization script

import java.net.URI
import java.io._

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core._
import eu.cdevreeze.yaidom

import eu.cdevreeze.tqa._
import eu.cdevreeze.tqa.base._
import eu.cdevreeze.tqa.base.taxonomy._
import eu.cdevreeze.tqa.base.queryapi._
import eu.cdevreeze.tqa.base.relationship._
import eu.cdevreeze.tqa.base.dom._

// Yaidom: Easy creation of ENames and QNames

object ENameUtil {

  implicit class ToEName(val s: String) {

    /**
     * Returns the EName corresponding to the given QName as string, using the implicitly passed Scope.
     */
    def en(implicit qnameProvider: QNameProvider, enameProvider: ENameProvider, scope: Scope) = {
      val qname = qnameProvider.parseQName(s)
      val ename = scope.resolveQNameOption(qname)(enameProvider).get
      ename
    }

    /**
     * Returns the EName corresponding to the given QName as string, using the implicitly passed Scope,
     * but without default namespace. Use this method to get attribute ENames.
     */
    def an(implicit qnameProvider: QNameProvider, enameProvider: ENameProvider, scope: Scope) = {
      val qname = qnameProvider.parseQName(s)
      val ename = scope.withoutDefaultNamespace.resolveQNameOption(qname)(enameProvider).get
      ename
    }
  }
}

import ENameUtil._

// Yaidom: Easy creation of element predicates, even implicitly from ENames

import yaidom.queryapi.HasENameApi._

// Utilities

def toFormulaTaxo(taxo: BasicTaxonomy): extension.formula.taxonomy.BasicFormulaTaxonomy = {
  extension.formula.taxonomy.BasicFormulaTaxonomy.build(taxo)
}

def toTableTaxo(taxo: BasicTaxonomy): extension.table.taxonomy.BasicTableTaxonomy = {
  extension.table.taxonomy.BasicTableTaxonomy.build(taxo)
}

def toVariableSetConverter(formulaTaxo: extension.formula.taxonomy.BasicFormulaTaxonomy): extension.formula.taxonomymodel.VariableSetConverter = {
  new extension.formula.taxonomymodel.VariableSetConverter(formulaTaxo)
}

def toTableConverter(tableTaxo: extension.table.taxonomy.BasicTableTaxonomy): extension.table.taxonomymodel.TableConverter = {
  new extension.table.taxonomymodel.TableConverter(tableTaxo)
}

// Printing a taxonomy element or backing element as simple element

def backingElemToSimpleElem(backingElem: yaidom.queryapi.BackingElemApi): yaidom.simple.Elem = {
  backingElem match {
    case e: yaidom.indexed.IndexedScopedNode.Elem[_] if e.underlyingElem.isInstanceOf[yaidom.simple.Elem] =>
      e.underlyingElem.asInstanceOf[yaidom.simple.Elem]
    case e: yaidom.saxon.SaxonElem =>
      yaidom.utils.saxon.SaxonElemToSimpleElemConverter.convertSaxonElem(e)
    case e =>
      sys.error(s"Unexpected element type: ${e.getClass}")
  }
}

def toSimpleElem(taxoElem: TaxonomyElem): yaidom.simple.Elem = {
  val backingElem = taxoElem.backingElem

  backingElemToSimpleElem(backingElem)
}

// TQA: DTS bootstrapping function

val processor = new Processor(false)

def loadDts(localRootDir: File, entrypointUris: Set[URI], docCacheSize: Int, lenient: Boolean): BasicTaxonomy = {
  val docBuilder =
    new docbuilder.saxon.SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      docbuilder.jvm.UriResolvers.fromLocalMirrorRootDirectory(localRootDir))

  val documentBuilder =
    new docbuilder.jvm.CachingDocumentBuilder(docbuilder.jvm.CachingDocumentBuilder.createCache(docBuilder, docCacheSize))

  val documentCollector = taxonomybuilder.DefaultDtsCollector()

  val relationshipFactory =
    if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

  def filterArc(arc: XLinkArc): Boolean = {
    if (lenient) RelationshipFactory.AnyArcHavingArcrole(arc) else RelationshipFactory.AnyArc(arc)
  }

  val taxoBuilder =
    taxonomybuilder.TaxonomyBuilder.
      withDocumentBuilder(documentBuilder).
      withDocumentCollector(documentCollector).
      withRelationshipFactory(relationshipFactory).
      withArcFilter(filterArc _)

  val basicTaxo = taxoBuilder.build(entrypointUris)
  basicTaxo
}

def loadDts(localRootDir: File, entrypointUri: URI): BasicTaxonomy = {
  loadDts(localRootDir, Set(entrypointUri), 10000, false)
}

def loadLocalTaxonomyDocs(localDocUris: Set[URI]): BasicTaxonomy = {
  val documentBuilder =
    new docbuilder.saxon.SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      docbuilder.jvm.UriResolvers.fromUriConverter(docbuilder.jvm.UriConverters.identity))

  val documentCollector = taxonomybuilder.TrivialDocumentCollector

  val taxoBuilder =
    taxonomybuilder.TaxonomyBuilder.
      withDocumentBuilder(documentBuilder).
      withDocumentCollector(documentCollector).
      withRelationshipFactory(DefaultRelationshipFactory.LenientInstance).
      withArcFilter(RelationshipFactory.AnyArcHavingArcrole)

  val basicTaxo = taxoBuilder.build(localDocUris)
  basicTaxo
}

// Now the REPL has been set up for ad-hoc DTS querying (combined with ad-hoc yaidom usage)
// Do not forget to provide an implicit Scope if we want to create ENames with the "en" or "an" postfix operator!

println(s"Use loadDts(localRootDir, entrypointUri) to get a DTS as BasicTaxonomy")
println(s"If needed, use loadDts(localRootDir, entrypointUris, docCacheSize, lenient) instead")
println(s"If you want to load only a few local taxonomy documents, use loadLocalTaxonomyDocs(localDocUris) instead")
println(s"Store the result in val taxo, and import taxo._")
