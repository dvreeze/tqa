=========
CHANGELOG
=========


0.7.1
=====

This version is almost the same as version 0.7.0.

This version added the following to the previous version:

* More query methods for querying dimensional tree inheritance, filtering on has-hypercube relationships
* Constants for standard label and reference roles

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* abstract method computeFilteredHasHypercubeInheritanceOrSelf(scala.Function1)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeFilteredHasHypercubeInheritanceOrSelf")
* abstract method computeHasHypercubeInheritanceForElr(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceForElr")
* abstract method computeHasHypercubeInheritanceForElrReturningPrimaries(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceForElrReturningPrimaries")
* abstract method computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries")
* abstract method computeHasHypercubeInheritanceOrSelfForElr(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceOrSelfForElr")
* abstract method computeFilteredHasHypercubeInheritance(scala.Function1)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeFilteredHasHypercubeInheritance")
* method apply(scala.Option,eu.cdevreeze.tqa.instance.XbrliElem)eu.cdevreeze.tqa.instance.XbrlInstanceDocument in object eu.cdevreeze.tqa.instance.XbrlInstanceDocument in current version does not have a correspondent with same parameter signature among (scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrlInstanceDocument, (scala.Option,eu.cdevreeze.tqa.instance.XbrlInstance)eu.cdevreeze.tqa.instance.XbrlInstanceDocument
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.apply")


0.7.0
=====

This version adds taxonomy documents and XBRL instance documents (that can contain top-level comments
and processing instructions besides the root element).

The major changes are:

* Using yaidom 1.7.1 ``BackingDocumentApi`` in API of ``SaxonDocument``
* Now ``DocumentBuilder`` returns documents instead of (root) elements
* Added ``TaxonomyDocument`` and using it in taxonomy classes (and document collectors) instead of (root) elements
* Added ``XbrlInstanceDocument``
* Renamed XbrlInstance ``apply`` method to ``build``

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.docbuilder.DocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingDocumentApi rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.docbuilder.DocumentBuilder.build")
* abstract method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingDocumentApi in interface eu.cdevreeze.tqa.docbuilder.DocumentBuilder is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.docbuilder.DocumentBuilder.build")
* method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.docbuilder.jvm.CachingDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingDocumentApi rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.docbuilder.jvm.CachingDocumentBuilder.build")
* abstract method taxonomyDocs()scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.TaxonomyApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.TaxonomyApi.taxonomyDocs")
* method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in object eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector.collectTaxonomyRootElems")
* abstract method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector.collectTaxonomyRootElems")
* abstract method collectTaxonomyDocuments(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector.collectTaxonomyDocuments")
* method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector's type is different in current version, where it is (eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set instead of (eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector.findAllUsedDocUris")
* method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.collectTaxonomyRootElems")
* method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector's type is different in current version, where it is (eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set instead of (eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.findAllUsedDocUris")
* abstract method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.findAllUsedDocUris")
* method build(java.net.URI)eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem in class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument rather than eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder.build")
* method build(java.net.URI)eu.cdevreeze.yaidom.indexed.IndexedScopedNode#Elem in class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.indexed.Document rather than eu.cdevreeze.yaidom.indexed.IndexedScopedNode#Elem
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder.build")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrliElem.apply")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrlInstance in object eu.cdevreeze.tqa.instance.XbrlInstance does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrlInstance.apply")


0.6.1
=====

Exactly the same as release 0.6.0. At first, deployment of release 0.6.0 to Maven Central seemed unsuccessful,
hence release 0.6.1. Both releases are now in Maven Central, and both releases can be used. They are
interchangeable.


0.6.0
=====

Version 0.6.0 of TQA redesigned DocumentBuilders and TaxonomyBuilders. It also added an XBRL instance
viewer (created with Scala.js). The redesign of DocumentBuilders and TaxonomyBuilders causes some breaking changes.

The major changes are:

* Redesign of ``DocumentBuilder`` implementations on the JVM

  * Document builders targeting the JVM now take a URI resolver as extra argument, turning a URI into a SAX InputSource
  * Such a URI resolver can parse documents in ZIP files, like the ones in a Taxonomy Package
  * URI resolvers can also take a URI converter (function from URI to URI)
  * An XML catalog (as restricted by Taxonomy Packages) can be offered as such a URI converter
  * URI converters can be combined, and so can URI resolvers
  * A large portion of the test code now also uses ZIP files as test taxonomy document input

* Redesign of ``TaxonomyBuilder``, in particular of ``DocumentCollector``

  * Document collectors no longer contain the (entry point) document URIs as state
  * Instead, the taxonomy builder ``build`` method now takes the (entry point) document URIs
  * This makes taxonomy builders re-usable across entry points
  * Taxonomy package XML files can now be parsed, and provide entry point URIs via an entry point name, for example

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

  * method uriToLocalUri(java.net.URI,java.io.File)java.net.URI in object eu.cdevreeze.tqa.docbuilder.jvm.UriConverters does not have a correspondent in current version
  * abstract method collectTaxonomyRootElems(eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector does not have a correspondent in current version
  * abstract method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector is present only in current version
  * method withDocumentCollector(eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder does not have a correspondent in current version
  * method build()eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder does not have a correspondent in current version
  * method this(scala.collection.immutable.Set)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector does not have a correspondent in current version
  * method apply(scala.collection.immutable.Set)eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector in object eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector does not have a correspondent in current version
  * method collectTaxonomyRootElems(eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method entrypointUris()scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method this(scala.collection.immutable.Set)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method uriConverter()scala.Function1 in class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder does not have a correspondent in current version
  * method uriConverter()scala.Function1 in class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder does not have a correspondent in current version


0.5.0
=====

Version 0.5.0 of TQA supports Scala.js as second target platform. The API should start to feel more stable than in
previous releases.


0.5.0-M2
========

This milestone release is a step towards release 0.5.0, which supports Scala.js and which should make the API more
stable than was the case for previous releases.

