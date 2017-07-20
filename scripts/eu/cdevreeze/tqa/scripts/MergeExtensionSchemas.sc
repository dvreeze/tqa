
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa.scripts.MergeExtensionSchemas"

// This script merges at most extension schemas into one, in NT extension taxonomies (obeying the NT extension best practices).
// It is assumed that there are either 1 or 2 extension schemas beforehand, and that only global element declarations must be moved.

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

def uriToLocalUri(uri: URI)(implicit coreLocalRootDir: File, extensionLocalRootDir: File): URI = {
  if (uri.toString.startsWith("http://www.ebpi.nl/")) {
    (new File(extensionLocalRootDir, (new File(uri.getPath)).getName)).toURI
  } else {
    backingelem.UriConverters.uriToLocalUri(uri, coreLocalRootDir)
  }
}

def loadExtensionDts(docCacheSize: Int, lenient: Boolean)(implicit coreLocalRootDir: File, extensionLocalRootDir: File): BasicTaxonomy = {
  // Only XML files in this directory are seen. Sub-directories are neither expected nor processed.

  val extTaxoFiles = extensionLocalRootDir.listFiles.toVector.filter(f => f.getName.endsWith(".xsd") || f.getName.endsWith(".xml"))
  require(
    extTaxoFiles.filter(_.getName.endsWith(".xsd")).size >= 1, 
    s"Expected at least 1 XSD in $extensionLocalRootDir, but found ${extTaxoFiles.filter(_.getName.endsWith(".xsd")).size} ones")

  val extDocUris = extTaxoFiles.map(f => URI.create(commonExtensionUri.toString + f.getName)).toSet

  val docParser = yaidom.parse.DocumentParserUsingDom.newInstance()

  val entrypointUris: Set[URI] = extDocUris.filter(_.toString.endsWith(".xsd"))

  val docBuilder = new backingelem.indexed.IndexedDocumentBuilder(docParser, (uri => uriToLocalUri(uri)(coreLocalRootDir, extensionLocalRootDir)))

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

def loadExtensionDts(implicit coreLocalRootDir: File, extensionLocalRootDir: File): BasicTaxonomy = {
  loadExtensionDts(10000, false)(coreLocalRootDir, extensionLocalRootDir)
}

def guessedScope(taxonomy: BasicTaxonomy): Scope = {
  taxonomy.rootElems.map(_.scope.withoutDefaultNamespace).foldLeft(Scope.Empty) { case (accScope, currScope) =>
    (accScope ++ currScope).ensuring(_.retainingDefaultNamespace.isEmpty)
  }
}

val docParser = yaidom.parse.DocumentParserUsingDom.newInstance()

val docPrinter = yaidom.print.DocumentPrinterUsingDom.newInstance()

// Helper functions for editing extension taxonomies

def removeDocuments(inputTaxo: BasicTaxonomy, docUris: Set[URI]): BasicTaxonomy = {
  val taxoBase = TaxonomyBase.build(inputTaxo.taxonomyBase.rootElems.filterNot(e => docUris.contains(e.docUri)))
  BasicTaxonomy.build(taxoBase, inputTaxo.extraSubstitutionGroupMap, DefaultRelationshipFactory.LenientInstance)
}

def addOrUpdateDocuments(inputTaxo: BasicTaxonomy, rootElemsByUri: Map[URI, TaxonomyElem]): BasicTaxonomy = {
  val rootElems = (inputTaxo.taxonomyBase.rootElemUriMap ++ rootElemsByUri).values.toIndexedSeq
  val taxoBase = TaxonomyBase.build(rootElems)
  BasicTaxonomy.build(taxoBase, inputTaxo.extraSubstitutionGroupMap, DefaultRelationshipFactory.LenientInstance)
}

def findAllExtensionRootElems(inputTaxo: BasicTaxonomy): immutable.IndexedSeq[TaxonomyElem] = {
  inputTaxo.rootElems.filter(e => isExtensionUri(e.docUri))
}

def findAllExtensionSchemas(inputTaxo: BasicTaxonomy): immutable.IndexedSeq[XsdSchema] = {
  findAllExtensionRootElems(inputTaxo) collect { case e: XsdSchema => e }
}

def findAllExtensionLinkbases(inputTaxo: BasicTaxonomy): immutable.IndexedSeq[Linkbase] = {
  findAllExtensionRootElems(inputTaxo) collect { case e: Linkbase => e }
}

// The following function is very sensitive and opinionated about how to recognize entrypoint extension schemas!

def findEntrypointExtensionSchema(inputTaxo: BasicTaxonomy): Option[XsdSchema] = {
  val extensionSchemas = findAllExtensionSchemas(inputTaxo)

  // Very sensitive, of course
  val resultSchemas = extensionSchemas.filter(e => e.docUri.toString.contains("-entrypoint") || e.docUri.toString.contains("-rpt"))
  resultSchemas.headOption
}

def updateSchema(inputXsdSchema: XsdSchema, mapElem: yaidom.simple.Elem => yaidom.simple.Elem): XsdSchema = {
  val simpleRootElem = inputXsdSchema.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem

  val resultSimpleRootElem = simpleRootElem.transformElemsOrSelf(mapElem)

  XsdSchema.build(yaidom.indexed.Elem(inputXsdSchema.backingElem.docUriOption, resultSimpleRootElem))
}

def updateLinkbase(inputLinkbase: Linkbase, mapElem: yaidom.simple.Elem => yaidom.simple.Elem): Linkbase = {
  val simpleRootElem = inputLinkbase.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem

  val resultSimpleRootElem = simpleRootElem.transformElemsOrSelf(mapElem)

  Linkbase.build(yaidom.indexed.Elem(inputLinkbase.backingElem.docUriOption, resultSimpleRootElem))
}

def updateLinkbase(inputLinkbase: Linkbase, paths: Set[Path], mapElem: (yaidom.simple.Elem, Path) => yaidom.simple.Elem): Linkbase = {
  val simpleRootElem = inputLinkbase.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem

  val resultSimpleRootElem = simpleRootElem.updateElemsOrSelf(paths)(mapElem)

  Linkbase.build(yaidom.indexed.Elem(inputLinkbase.backingElem.docUriOption, resultSimpleRootElem))
}

// Editing extension taxonomies

def removeNonEntrypointExtensionSchemas(inputTaxo: BasicTaxonomy): BasicTaxonomy = {
  // Breaks DTS

  val extensionSchemas = findAllExtensionSchemas(inputTaxo)
  require(extensionSchemas.size >= 1, s"Expected at least one extension schema but found none")
  require(extensionSchemas.size <= 2, s"Expected at most 2 extension schemas but found ${extensionSchemas.size} ones")

  val entrypointExtensionSchemaOption = findEntrypointExtensionSchema(inputTaxo)
  require(entrypointExtensionSchemaOption.nonEmpty, s"Expected one entrypoint extension schema but found none")

  val nonEntrypointExtensionSchemas = extensionSchemas.filterNot(entrypointExtensionSchemaOption.toSet)

  val resultTaxo = removeDocuments(inputTaxo, nonEntrypointExtensionSchemas.map(_.docUri).toSet)
  resultTaxo
}

def setTargetNamespaceInExtensionSchema(tns: String, tnsPrefixOption: Option[String], inputTaxo: BasicTaxonomy): BasicTaxonomy = {
  // Breaks DTS

  val entrypointExtensionSchemaOption = findEntrypointExtensionSchema(inputTaxo)
  require(entrypointExtensionSchemaOption.nonEmpty, s"Expected one entrypoint extension schema but found none")

  val extensionSchema: XsdSchema = entrypointExtensionSchemaOption.get

  val newScope = extensionSchema.backingElem.scope ++ tnsPrefixOption.map(pref => Scope.from(pref -> tns)).getOrElse(Scope.Empty)

  def mapElem(elem: yaidom.simple.Elem): yaidom.simple.Elem = {
    if (elem.resolvedName == ENames.XsSchemaEName) {
      elem.copy(scope = newScope ++ elem.scope).plusAttribute(QName("targetNamespace"), tns)
    } else {
      elem.copy(scope = newScope ++ elem.scope)
    }
  }

  val resultSchema = updateSchema(extensionSchema, mapElem)

  val resultTaxo = addOrUpdateDocuments(inputTaxo, Map(extensionSchema.docUri -> resultSchema))
  resultTaxo
}

def addGlobalElementDeclarationsToExtensionSchema(
  elemDecls: immutable.IndexedSeq[GlobalElementDeclaration],
  inputTaxo: BasicTaxonomy): BasicTaxonomy = {

  // Breaks DTS

  val entrypointExtensionSchemaOption = findEntrypointExtensionSchema(inputTaxo)
  require(entrypointExtensionSchemaOption.nonEmpty, s"Expected one entrypoint extension schema but found none")

  val extensionSchema: XsdSchema = entrypointExtensionSchemaOption.get

  val tns = extensionSchema.targetNamespaceOption.getOrElse(sys.error(s"Missing target namespace in ${extensionSchema.docUri}"))

  require(
    elemDecls.forall(e => e.targetEName.namespaceUriOption.contains(tns)),
    s"Unexpected target namespaces: ${elemDecls.flatMap(_.targetEName.namespaceUriOption).toSet.diff(Set(tns))}")

  def mapElem(elem: yaidom.simple.Elem): yaidom.simple.Elem = {
    if (elem.resolvedName == ENames.XsSchemaEName) {
      elem.plusChildren(elemDecls.map(_.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem))
    } else {
      elem
    }
  }

  val resultSchema = updateSchema(extensionSchema, mapElem)

  val resultTaxo = addOrUpdateDocuments(inputTaxo, Map(extensionSchema.docUri -> resultSchema))
  resultTaxo
}

def fixReferencesToGlobalElementDeclarationsInExtension(
  elemDecls: immutable.IndexedSeq[GlobalElementDeclaration],
  inputTaxo: BasicTaxonomy,
  originalTaxo: BasicTaxonomy): BasicTaxonomy = {

  // Should repair the DTS

  require(
    elemDecls.forall(e => isExtensionUri(e.docUri)),
    s"Expected only extension element declarations, but found them in ${elemDecls.map(_.docUri).toSet}")

  val elemDeclENames = elemDecls.map(_.targetEName).toSet

  require(
    elemDeclENames.forall(en => inputTaxo.findGlobalElementDeclaration(en).nonEmpty),
    s"Not all global element declarations found in input taxonomy: ${elemDeclENames.filter(en => inputTaxo.findGlobalElementDeclaration(en).isEmpty)}")

  require(
    elemDeclENames.forall(en => originalTaxo.findGlobalElementDeclaration(en).nonEmpty),
    s"Not all global element declarations found in original taxonomy: ${elemDeclENames.filter(en => originalTaxo.findGlobalElementDeclaration(en).isEmpty)}")

  require(
    findAllExtensionLinkbases(inputTaxo).flatMap(_.findAllChildElemsOfType(classTag[ExtendedLink])).forall(el => el.labeledXlinkMap.values.forall(_.size == 1)),
    s"Unexpected reuse of XLink labels in extension extended links (in input taxonomy)")

  require(
    findAllExtensionLinkbases(originalTaxo).flatMap(_.findAllChildElemsOfType(classTag[ExtendedLink])).forall(el => el.labeledXlinkMap.values.forall(_.size == 1)),
    s"Unexpected reuse of XLink labels in extension extended links (in original taxonomy)")

  // Querying for affected (extension taxonomy) standard relationships and their locators in the original taxonomy

  val origStandardRels =
    elemDeclENames.toIndexedSeq.flatMap(en => originalTaxo.findAllOutgoingStandardRelationshipsOfType(en, classTag[StandardRelationship]))

  require(
    origStandardRels.forall(rel => isExtensionUri(rel.arc.docUri)),
    s"Expected only extension relationships, but found them in ${origStandardRels.map(_.arc.docUri).toSet}")

  val origInterConceptRels =
    elemDeclENames.toIndexedSeq.flatMap(en => originalTaxo.findAllIncomingInterConceptRelationshipsOfType(en, classTag[InterConceptRelationship]))

  require(
    origInterConceptRels.forall(rel => isExtensionUri(rel.arc.docUri)),
    s"Expected only extension relationships, but found them in ${origInterConceptRels.map(_.arc.docUri).toSet}")

  val origLocKeyConceptENameSeq: immutable.IndexedSeq[(XmlFragmentKey, EName)] =
    (origStandardRels.map(rel => (rel.resolvedFrom.xlinkLocatorOrResource.asInstanceOf[XLinkLocator].key -> rel.sourceConceptEName)) ++
      origInterConceptRels.map(rel => (rel.resolvedTo.xlinkLocatorOrResource.asInstanceOf[XLinkLocator].key -> rel.targetConceptEName))).distinct

  // Now turning to the input taxonomy, to collect the fixes to apply

  val extFragmentKeys: Set[XmlFragmentKey] = inputTaxo.rootElems.flatMap(_.findAllElemsOrSelf).map(_.key).toSet

  val locKeyConceptHrefSeq: immutable.IndexedSeq[(XmlFragmentKey, URI)] =
    origLocKeyConceptENameSeq map { case (locatorKey, conceptEName) =>
      require(extFragmentKeys.contains(locatorKey), s"Locator key $locatorKey not found in extension taxonomy")

      val conceptDecl = inputTaxo.findGlobalElementDeclaration(conceptEName).getOrElse(sys.error(s"Missing global element declaration for $conceptEName"))

      require(conceptDecl.idOption.nonEmpty, s"Missing ID attribute for concept $conceptEName")

      // TODO Make relative if applicable
      val conceptUri: URI = new URI(conceptDecl.docUri.getScheme, conceptDecl.docUri.getSchemeSpecificPart, conceptDecl.idOption.get)
      (locatorKey -> conceptUri)
    }

  val locKeyConceptHrefs: Map[XmlFragmentKey, URI] = locKeyConceptHrefSeq.groupBy(_._1).mapValues(_.head._2)

  val keysOfLocatorsToFix = locKeyConceptHrefs.keySet

  def mapElem(docUri: URI)(elem: yaidom.simple.Elem, path: Path): yaidom.simple.Elem = {
    val key = XmlFragmentKey(docUri, path)

    if (keysOfLocatorsToFix.contains(key)) {
      require(Scope.from("xlink" -> Namespaces.XLinkNamespace).subScopeOf(elem.scope), s"Missing prefix 'xlink' for XLink namespace")

      elem.plusAttribute(QName("xlink:href"), locKeyConceptHrefs(key).toString)
    } else {
      elem
    }
  }

  val resultLinkbases =
    findAllExtensionLinkbases(inputTaxo).map(lkb =>
      updateLinkbase(lkb, keysOfLocatorsToFix.filter(_.docUri == lkb.docUri).map(_.path).toSet, mapElem(lkb.docUri)))

  val resultTaxo = addOrUpdateDocuments(inputTaxo, resultLinkbases.map(lkb => (lkb.docUri -> lkb)).toMap)
  resultTaxo
}

def cleanUpExtensionDocuments(inputTaxo: BasicTaxonomy): BasicTaxonomy = {
  var simpleExtRootElemsByUri: Map[URI, yaidom.simple.Elem] =
    inputTaxo.rootElems.map(e => (e.docUri -> e.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem)).toMap

  simpleExtRootElemsByUri = simpleExtRootElemsByUri.mapValues(_.prettify(2)) // TODO More cleanup actions

  def toTaxonomyRootElem(docUri: URI, e: yaidom.simple.Elem): TaxonomyElem = {
    if (e.resolvedName == ENames.XsSchemaEName) {
      XsdSchema.build(yaidom.indexed.Elem(docUri, e))
    } else {
      Linkbase.build(yaidom.indexed.Elem(docUri, e))
    }
  }

  val resultRootElemsByUri: Map[URI, TaxonomyElem] =
    simpleExtRootElemsByUri.map(kv => (kv._1 -> toTaxonomyRootElem(kv._1, kv._2))).toMap

  val resultTaxo = addOrUpdateDocuments(inputTaxo, resultRootElemsByUri)
  resultTaxo
}

def validatingTaxonomy(taxo: BasicTaxonomy, originalTaxo: BasicTaxonomy): BasicTaxonomy = {
  val resultTaxo = BasicTaxonomy.build(taxo.taxonomyBase, taxo.extraSubstitutionGroupMap, DefaultRelationshipFactory.StrictInstance)

  require(
    resultTaxo.findAllGlobalElementDeclarations.map(_.targetEName).toSet == originalTaxo.findAllGlobalElementDeclarations.map(_.targetEName).toSet,
    s"Mismatch in global element declarations before and after the merge")

  require(
    resultTaxo.findAllNamedTypeDefinitions.map(_.targetEName).toSet == originalTaxo.findAllNamedTypeDefinitions.map(_.targetEName).toSet,
    s"Mismatch in named type definitions before and after the merge")

  resultTaxo
}

// The "main" method

def mergeExtensionSchemas(inputTaxo: BasicTaxonomy): BasicTaxonomy = {
  // Must not break DTS

  val originalTaxo = inputTaxo

  var currTaxo = originalTaxo

  currTaxo = removeNonEntrypointExtensionSchemas(inputTaxo)

  val extensionGlobalElemDecls = originalTaxo.filterGlobalElementDeclarations(e => isExtensionUri(e.docUri))

  // TODO Validate assumption that only global element declarations must be moved.

  val tnsSet = extensionGlobalElemDecls.flatMap(_.targetEName.namespaceUriOption).toSet

  require(tnsSet.size <= 1, s"Expected at most one extension global element declaration target namespace but found $tnsSet")

  val tnsOption = tnsSet.headOption
  val tnsPrefixOption =
    tnsOption.toSeq.flatMap(tns => extensionGlobalElemDecls.flatMap(_.scope.withoutDefaultNamespace.prefixesForNamespace(tns))).headOption

  if (tnsOption.nonEmpty) {
    setTargetNamespaceInExtensionSchema(tnsOption.get, tnsPrefixOption, currTaxo)
  }

  val elemDecls = originalTaxo.filterGlobalElementDeclarations(e => isExtensionUri(e.docUri))

  currTaxo = addGlobalElementDeclarationsToExtensionSchema(elemDecls, currTaxo)

  currTaxo = fixReferencesToGlobalElementDeclarationsInExtension(elemDecls, currTaxo, originalTaxo)

  // Fortunately no DTS discovery fixes needed

  currTaxo = cleanUpExtensionDocuments(currTaxo)

  currTaxo = validatingTaxonomy(currTaxo, originalTaxo)

  currTaxo
}

def saveExtensionTaxonomy(taxo: BasicTaxonomy, outputRootDir: File)(implicit coreLocalRootDir: File, extensionLocalRootDir: File): Unit = {
  outputRootDir.mkdirs()

  val extRootElems = taxo.rootElems.filter(e => isExtensionUri(e.docUri))

  extRootElems foreach { rootElem =>
    val localUri: URI = uriToLocalUri(rootElem.docUri)(coreLocalRootDir, extensionLocalRootDir)

    val f = new File(localUri)
    f.getParentFile.mkdirs()

    val doc = yaidom.simple.Document(Some(rootElem.docUri), rootElem.backingElem.asInstanceOf[yaidom.indexed.Elem].underlyingElem)

    docPrinter.print(doc, "UTF-8", new FileOutputStream(f))
  }
}


// Now the REPL has been set up for DTS schema merging, as well as ad-hoc DTS querying (combined with ad-hoc yaidom usage)
// Do not forget to provide an implicit Scope if we want to create ENames with the "en" or "an" postfix operator!

println(s"Use loadExtensionDts to get a DTS as BasicTaxonomy")
println(s"If needed, use loadExtensionDts(docCacheSize, lenient) instead")
println(s"For ad-hoc taxonomy querying, store the result in val taxo, and import taxo._")
println(s"Use method mergeExtensionSchemas(inputTaxo) to merge the extension schemas.")
println(s"Save the resulting taxonomy with method saveExtensionTaxonomy(taxo, rootDir)")
