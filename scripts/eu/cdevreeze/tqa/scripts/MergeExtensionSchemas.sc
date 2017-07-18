
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.MergeExtensionSchemas"

// Taking TQA version 0.4.6

import $ivy.`eu.cdevreeze.tqa::tqa:0.4.6`

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

val commonExtensionUri = URI.create("http://www.ebpi.nl/extensions/processing/")

def optExtensionFileName(uri: URI): Option[String] = {
  // Copied from TACT code

  val possiblyRelativeUri = commonExtensionUri.relativize(uri)
  
  if (possiblyRelativeUri.isAbsolute) {
    if (uri.toString.startsWith(commonExtensionUri.toString)) {
      // This is bad, but makes a corner case work.
      Some(uri.toString.stripPrefix(commonExtensionUri.toString))
    } else {
      None
    }
  } else {
    Some(possiblyRelativeUri.getPath.dropWhile(_ == '/'))
  }
}

def isExtensionUri(uri: URI): Boolean = {
  optExtensionFileName(uri).isDefined
}

def loadExtensionDts(extensionLocalRootDir: File, coreLocalRootDir: File, docCacheSize: Int, lenient: Boolean): BasicTaxonomy = {
  // Only XML files in this directory are seen. Sub-directories are neither expected nor processed.

  val extTaxoFiles = extensionLocalRootDir.listFiles.toVector.filter(f => f.getName.endsWith(".xsd") || f.getName.endsWith(".xml"))
  require(
    extTaxoFiles.filter(_.getName.endsWith(".xsd")).size >= 1, 
    s"Expected at least 1 XSD in $extensionLocalRootDir, but found ${extTaxoFiles.filter(_.getName.endsWith(".xsd")).size} ones")
    
  val extDocUris = extTaxoFiles.map(f => URI.create(commonExtensionUri.toString + f.getName)).toSet

  val docParser = yaidom.parse.DocumentParserUsingDom.newInstance()
  
  def uriToLocalUri(uri: URI): URI = {
    if (uri.toString.startsWith("http://www.ebpi.nl/")) {
      (new File(extensionLocalRootDir, (new File(uri.getPath)).getName)).toURI
    } else {
      backingelem.UriConverters.uriToLocalUri(uri, coreLocalRootDir)
    }
  }

  val entrypointUris: Set[URI] = extDocUris.filter(_.toString.endsWith(".xsd"))

  val docBuilder = new backingelem.indexed.IndexedDocumentBuilder(docParser, uriToLocalUri)

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

def loadExtensionDts(extensionLocalRootDir: File, coreLocalRootDir: File): BasicTaxonomy = {
  loadExtensionDts(extensionLocalRootDir, coreLocalRootDir, 10000, false)
}

def guessedScope(taxonomy: BasicTaxonomy): Scope = {
  taxonomy.rootElems.map(_.scope.withoutDefaultNamespace).foldLeft(Scope.Empty) { case (accScope, currScope) =>
    (accScope ++ currScope).ensuring(_.retainingDefaultNamespace.isEmpty)
  }
}

// Now the REPL has been set up for DTS schema merging, as well as ad-hoc DTS querying (combined with ad-hoc yaidom usage)
// Do not forget to provide an implicit Scope if we want to create ENames with the "en" or "an" postfix operator!

println(s"Use loadExtensionDts(extensionLocalRootDir, coreLocalRootDir) to get a DTS as BasicTaxonomy")
println(s"If needed, use loadExtensionDts(extensionLocalRootDir, coreLocalRootDir, docCacheSize, lenient) instead")
println(s"Store the result in val taxo, and import taxo._")
