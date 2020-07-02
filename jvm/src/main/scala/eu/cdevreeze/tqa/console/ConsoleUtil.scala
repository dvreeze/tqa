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

import java.net.URI
import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.TaxonomyPackageUriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor
import org.xml.sax.InputSource

/**
 * Taxonomy bootstrapping utility for the console programs.
 *
 * @author Chris de Vreeze
 */
object ConsoleUtil {

  def createTaxonomyBuilder(taxonomyPackage: ZipFile, useSaxon: Boolean): TaxonomyBuilder = {
    val uriResolver: URI => InputSource = TaxonomyPackageUriResolvers.forTaxonomyPackage(taxonomyPackage)

    val documentBuilder: DocumentBuilder =
      if (useSaxon) {
        val processor = new Processor(false)

        SaxonDocumentBuilder(processor.newDocumentBuilder(), uriResolver)
      } else {
        IndexedDocumentBuilder(DocumentParserUsingStax.newInstance(), uriResolver)
      }

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder
        .withDocumentBuilder(documentBuilder)
        .withDefaultDtsCollector
        .withRelationshipFactory(relationshipFactory)
    taxoBuilder
  }
}
