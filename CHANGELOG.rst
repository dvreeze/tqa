=========
CHANGELOG
=========


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

