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

import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.base.taxonomybuilder.jvm.DefaultParallelDtsCollector
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.indexed.IndexedThreadSafeWrapperDocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.TaxonomyPackageUriResolvers
import eu.cdevreeze.tqa.docbuilder.saxon.ThreadSafeSaxonDocumentBuilder
import net.sf.saxon.s9api.Processor
import org.xml.sax.InputSource

/**
 * Taxonomy bootstrapping utility for the console programs.
 *
 * @author Chris de Vreeze
 */
object ConsoleUtil {

  def createTaxonomyBuilder(taxonomyPackage: ZipFile, useSaxon: Boolean, lenient: Boolean): TaxonomyBuilder = {
    // Exploiting parallelism, in DTS collection and relationship creation.

    val uriResolver: URI => InputSource = TaxonomyPackageUriResolvers.forTaxonomyPackage(taxonomyPackage)

    val processor = new Processor(false)

    val saxonDocBuilder: DocumentBuilder.ThreadSafeDocumentBuilder =
      ThreadSafeSaxonDocumentBuilder(processor, uriResolver)

    val documentBuilder: DocumentBuilder.ThreadSafeDocumentBuilder =
      if (useSaxon) {
        saxonDocBuilder
      } else {
        IndexedThreadSafeWrapperDocumentBuilder(saxonDocBuilder)
      }

    val relationshipFactory =
      if (lenient) DefaultParallelRelationshipFactory.LenientInstance
      else DefaultParallelRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder
        .withDocumentBuilder(documentBuilder)
        .withDocumentCollector(DefaultParallelDtsCollector())
        .withRelationshipFactory(relationshipFactory)
    taxoBuilder
  }
}
