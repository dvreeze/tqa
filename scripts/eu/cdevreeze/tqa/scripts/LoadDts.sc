
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.LoadDts"

// Taking TQA version 0.4.7-SNAPSHOT

import $ivy.`eu.cdevreeze.tqa::tqa:0.4.7-SNAPSHOT`

// Imports that (must) remain available after this initialization script

import java.net.URI
import java.io._

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.yaidom.core._
import eu.cdevreeze.yaidom

import net.sf.saxon.s9api.Processor

import eu.cdevreeze.tqa._
import eu.cdevreeze.tqa.taxonomy._
import eu.cdevreeze.tqa.queryapi._
import eu.cdevreeze.tqa.relationship._
import eu.cdevreeze.tqa.dom._

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

// TQA: DTS bootstrapping function

val processor = new Processor(false)

def loadDts(localRootDir: File, entrypointUris: Set[URI], docCacheSize: Int, lenient: Boolean): BasicTaxonomy = {
  val docBuilder =
    new backingelem.nodeinfo.SaxonDocumentBuilder(processor.newDocumentBuilder(), backingelem.UriConverters.uriToLocalUri(_, localRootDir))
  val documentBuilder =
    new backingelem.CachingDocumentBuilder(backingelem.CachingDocumentBuilder.createCache(docBuilder, docCacheSize))

  val documentCollector = taxonomybuilder.DefaultDtsCollector(entrypointUris)

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

  val basicTaxo = taxoBuilder.build()
  basicTaxo
}

def loadDts(localRootDir: File, entrypointUri: URI): BasicTaxonomy = {
  loadDts(localRootDir, Set(entrypointUri), 10000, false)
}

// Now the REPL has been set up for ad-hoc DTS querying (combined with ad-hoc yaidom usage)
// Do not forget to provide an implicit Scope if we want to create ENames with the "en" or "an" postfix operator!

println(s"Use loadDts(localRootDir, entrypointUri) to get a DTS as BasicTaxonomy")
println(s"If needed, use loadDts(localRootDir, entrypointUris, docCacheSize, lenient) instead")
println(s"Store the result in val taxo, and import taxo._")
