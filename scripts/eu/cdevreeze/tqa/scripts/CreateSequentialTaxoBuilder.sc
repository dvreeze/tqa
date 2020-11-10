
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.CreateSequentialTaxoBuilder"

// Taking TQA version 0.9.1

import $ivy.`eu.cdevreeze.tqa::tqa:0.9.1`

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

val processor = new Processor(false)

def loadTaxonomyBuilder(tpZipFiles: IndexedSeq[ZipFile], docCacheSize: Int): TaxonomyBuilder = {
  val uriResolver = docbuilder.jvm.TaxonomyPackageUriResolvers.forTaxonomyPackages(tpZipFiles)

  val cache = docbuilder.jvm.CachingDocumentBuilder.createCache(
    docbuilder.saxon.SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver),
    docCacheSize)

  val docBuilder = new docbuilder.jvm.CachingDocumentBuilder(cache)

  TaxonomyBuilder
    .withDocumentBuilder(docBuilder)
    .withDocumentCollector(DefaultDtsCollector())
    .withRelationshipFactory(DefaultRelationshipFactory.StrictInstance)
}

def loadTaxonomyBuilder(tpZipFiles: IndexedSeq[ZipFile]): TaxonomyBuilder = {
  loadTaxonomyBuilder(tpZipFiles, 10000)
}

println(s"Use loadTaxonomyBuilder(tpZipFiles) to get a DTS as BasicTaxonomy")
println(s"If needed, use loadTaxonomyBuilder(tpZipFiles, docCacheSize) instead")
