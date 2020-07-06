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

import java.util.zip.ZipFile

import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.base.taxonomybuilder.jvm.TaxonomyBuilderSupport
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy bootstrapping utility for the console programs.
 *
 * @author Chris de Vreeze
 */
object ConsoleUtil {

  def createTaxonomyBuilder(taxonomyPackage: ZipFile, useSaxon: Boolean, lenient: Boolean): TaxonomyBuilder = {
    // Exploiting parallelism, in DTS collection and relationship creation.

    val processor = new Processor(false)

    val rawTaxonomyBuilder: TaxonomyBuilder =
      if (useSaxon) {
        TaxonomyBuilderSupport.forTaxonomyPackage(taxonomyPackage, processor)
      } else {
        TaxonomyBuilderSupport.forTaxonomyPackageUsingIndexedDocuments(taxonomyPackage, processor)
      }

    if (lenient) {
      rawTaxonomyBuilder.withRelationshipFactory(DefaultParallelRelationshipFactory.LenientInstance)
    } else {
      rawTaxonomyBuilder
    }
  }
}
