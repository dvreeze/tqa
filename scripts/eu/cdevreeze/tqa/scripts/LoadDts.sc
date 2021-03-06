
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.LoadDts"

// Taking TQA version 0.13.0

import $ivy.`eu.cdevreeze.tqa::tqa:0.13.0`

// Imports that (must) remain available after this initialization script

import java.net.URI
import java.io._
import java.util.zip.ZipFile

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.util.chaining._

import net.sf.saxon.s9api.Processor

import eu.cdevreeze.yaidom.core._
import eu.cdevreeze.yaidom

import eu.cdevreeze.tqa._
import eu.cdevreeze.tqa.base._
import eu.cdevreeze.tqa.base.taxonomy._
import eu.cdevreeze.tqa.base.taxonomybuilder._
import eu.cdevreeze.tqa.base.queryapi._
import eu.cdevreeze.tqa.base.relationship._
import eu.cdevreeze.tqa.base.relationship.jvm._
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

import yaidom.queryapi.ClarkElemApi._

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

// TQA: DTS bootstrapping function

val processor = new Processor(false)

def loadTaxonomyBuilder(tpZipFiles: IndexedSeq[ZipFile], docCacheSize: Int, lenient: Boolean): TaxonomyBuilder = {
  val cacheWrapper = taxonomybuilder.jvm.TaxonomyBuilderSupport.createDocumentCache(tpZipFiles, processor, docCacheSize)

  import RelationshipFactory.AnyArcHavingArcrole
  import RelationshipFactory.AnyArc

  taxonomybuilder.jvm.TaxonomyBuilderSupport.usingDocumentCache(cacheWrapper)
    .pipe(tb => if (lenient) tb.withRelationshipFactory(DefaultParallelRelationshipFactory.LenientInstance) else tb)
    .pipe { tb => if (lenient)
      tb.withArcFilter(arc => AnyArcHavingArcrole(arc)) else tb.withArcFilter(arc => AnyArc(arc)) }
}

def loadTaxonomyBuilder(tpZipFiles: IndexedSeq[ZipFile]): TaxonomyBuilder = {
  loadTaxonomyBuilder(tpZipFiles, 10000, false)
}

def loadDts(tpZipFiles: IndexedSeq[ZipFile], entrypointUris: Set[URI], docCacheSize: Int, lenient: Boolean): BasicTaxonomy = {
  val taxoBuilder: TaxonomyBuilder = loadTaxonomyBuilder(tpZipFiles, docCacheSize, lenient)
  val basicTaxo = taxoBuilder.build(entrypointUris)
  basicTaxo
}

def loadDts(tpZipFiles: IndexedSeq[ZipFile], entrypointUri: URI): BasicTaxonomy = {
  loadDts(tpZipFiles, Set(entrypointUri), 10000, false)
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
      withRelationshipFactory(DefaultParallelRelationshipFactory.LenientInstance).
      withArcFilter(RelationshipFactory.AnyArcHavingArcrole)

  val basicTaxo = taxoBuilder.build(localDocUris)
  basicTaxo
}

// Now the REPL has been set up for ad-hoc DTS querying (combined with ad-hoc yaidom usage)
// Do not forget to provide an implicit Scope if we want to create ENames with the "en" or "an" postfix operator!

println(s"Use loadDts(tpZipFiles, entrypointUri) to get a DTS as BasicTaxonomy")
println(s"If needed, use loadDts(tpZipFiles, entrypointUris, docCacheSize, lenient) instead")
println(s"If you want to load only a few local taxonomy documents, use loadLocalTaxonomyDocs(localDocUris) instead")
println(s"Store the result in val taxo, and import taxo._")
println()
println(s"Use loadTaxonomyBuilder(tpZipFiles) instead to get a re-usable TaxonomyBuilder")
println(s"If needed, use loadTaxonomyBuilder(tpZipFiles, docCacheSize, lenient) instead")
