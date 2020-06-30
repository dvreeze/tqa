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

package eu.cdevreeze.tqa.console

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy bootstrapping utility for the console programs.
 *
 * @author Chris de Vreeze
 */
object ConsoleUtil {

  def buildTaxonomy(
      rootDirOrZipFile: File,
      parentPathOption: Option[URI],
      entryPointUris: Set[URI],
      useSaxon: Boolean,
      useScheme: Boolean): BasicTaxonomy = {
    val documentBuilder = getDocumentBuilder(rootDirOrZipFile, parentPathOption, useSaxon, useScheme)
    val documentCollector = DefaultDtsCollector()

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder
        .withDocumentBuilder(documentBuilder)
        .withDocumentCollector(documentCollector)
        .withRelationshipFactory(relationshipFactory)

    val basicTaxo = taxoBuilder.build(entryPointUris)
    basicTaxo
  }

  def getDocumentBuilder(
      rootDirOrZipFile: File,
      parentPathOption: Option[URI],
      useSaxon: Boolean,
      useScheme: Boolean): DocumentBuilder = {
    val uriResolver =
      (rootDirOrZipFile.isDirectory, useScheme) match {
        case (true, true) =>
          UriResolvers.fromLocalMirrorRootDirectoryUsingScheme(rootDirOrZipFile)
        case (true, false) =>
          UriResolvers.fromLocalMirrorRootDirectoryWithoutScheme(rootDirOrZipFile)
        case (false, true) =>
          UriResolvers.forZipFileContainingLocalMirrorUsingScheme(new ZipFile(rootDirOrZipFile), parentPathOption)
        case (false, false) =>
          UriResolvers.forZipFileContainingLocalMirrorWithoutScheme(new ZipFile(rootDirOrZipFile), parentPathOption)
      }

    if (useSaxon) {
      val processor = new Processor(false)

      SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
    } else {
      IndexedDocumentBuilder(DocumentParserUsingStax.newInstance(), uriResolver)
    }
  }

}
