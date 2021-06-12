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

package eu.cdevreeze.tqa.base.taxonomy.customfactory.jvm

import java.net.URI
import java.util.zip.ZipInputStream

import scala.collection.immutable.ArraySeq
import scala.collection.immutable.ListMap

import eu.cdevreeze.tqa.SubstitutionGroupMap
import eu.cdevreeze.tqa.base.dom.TaxonomyBase
import eu.cdevreeze.tqa.base.dom.XLinkArc
import eu.cdevreeze.tqa.base.relationship.RelationshipFactory
import eu.cdevreeze.tqa.base.relationship.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomyFactory
import eu.cdevreeze.yaidom.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom.saxon.SaxonDocument

/**
 * BasicTaxonomy factory from a remote (or local) taxonomy package ZIP file. The ZIP does not have to be
 * a taxonomy package with META-INF/taxonomyPackage.xml file, but it does need to have a META-INF/catalog.xml
 * file.
 *
 * This class uses type TaxonomyBaseFactoryFromRemoteZip. As a consequence, this class is not usable if the catalog
 * is not invertible, or if the ZIP stream contains far more documents than required for the DTSes we are interested in!
 * It is also not usable if the reverse catalog is inconsistent with the document URIs found during DTS discovery.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyFactoryFromRemoteZip(
    val createZipInputStream: () => ZipInputStream,
    val transformDocument: SaxonDocument => BackingDocumentApi,
    val extraSubstitutionGroupMap: SubstitutionGroupMap,
    val relationshipFactory: RelationshipFactory,
    val arcFilter: XLinkArc => Boolean)
    extends BasicTaxonomyFactory {

  val taxonomyBaseFactory: TaxonomyBaseFactoryFromRemoteZip =
    TaxonomyBaseFactoryFromRemoteZip(createZipInputStream).withTransformDocuemnt(transformDocument)

  def withTransformDocument(newTransformDocument: SaxonDocument => BackingDocumentApi): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(
      createZipInputStream,
      newTransformDocument,
      extraSubstitutionGroupMap,
      relationshipFactory,
      arcFilter)
  }

  def withExtraSubstitutionGroupMap(
      newExtraSubstitutionGroupMap: SubstitutionGroupMap): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(
      createZipInputStream,
      transformDocument,
      newExtraSubstitutionGroupMap,
      relationshipFactory,
      arcFilter)
  }

  def withRelationshipFactory(newRelationshipFactory: RelationshipFactory): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(
      createZipInputStream,
      transformDocument,
      extraSubstitutionGroupMap,
      newRelationshipFactory,
      arcFilter)
  }

  def withArcFilter(newArcFilter: XLinkArc => Boolean): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(
      createZipInputStream,
      transformDocument,
      extraSubstitutionGroupMap,
      relationshipFactory,
      newArcFilter)
  }

  /**
   * Builds a `BasicTaxonomy` from the data available to this taxonomy factory, as well as the passed entrypoint URIs.
   * It first calls method readAllXmlDocuments, and then the other overloaded method "build".
   */
  def build(entryPointUris: Set[URI]): BasicTaxonomy = {
    val xmlByteArrays: ListMap[String, ArraySeq[Byte]] = readAllXmlDocuments()

    build(entryPointUris, xmlByteArrays)
  }

  /**
   * Calls the method with the same name on taxonomyBaseFactory. After the call, we can log the number of documents,
   * if we would like to do so.
   */
  def readAllXmlDocuments(): ListMap[String, ArraySeq[Byte]] = {
    taxonomyBaseFactory.readAllXmlDocuments()
  }

  /**
   * Builds a `BasicTaxonomy` from the data available to this taxonomy factory, as well as the passed entrypoint URIs.
   * This method no longer needs the ZIP input stream, and has all data (unparsed) in memory.
   *
   * The ZIP entry names are assumed to use Unix-style (file component) separators.
   */
  def build(entryPointUris: Set[URI], xmlByteArrays: ListMap[String, ArraySeq[Byte]]): BasicTaxonomy = {
    val taxonomyBase: TaxonomyBase = taxonomyBaseFactory.loadDts(entryPointUris, xmlByteArrays)

    BasicTaxonomy.build(taxonomyBase, extraSubstitutionGroupMap, relationshipFactory, arcFilter)
  }
}

object TaxonomyFactoryFromRemoteZip {

  /**
   * Creates a TaxonomyFactoryFromRemoteZip from the passed ZipInputStream creation function, using sensible
   * defaults for the other primary constructor arguments. The relationship factory is strict, and uses parallellism.
   */
  def apply(createZipInputStream: () => ZipInputStream): TaxonomyFactoryFromRemoteZip = {
    apply(
      createZipInputStream,
      identity,
      SubstitutionGroupMap.Empty,
      DefaultParallelRelationshipFactory.StrictInstance,
      _ => true)
  }

  def apply(
      createZipInputStream: () => ZipInputStream,
      transformDocument: SaxonDocument => BackingDocumentApi,
      extraSubstitutionGroupMap: SubstitutionGroupMap,
      relationshipFactory: RelationshipFactory,
      arcFilter: XLinkArc => Boolean): TaxonomyFactoryFromRemoteZip = {
    new TaxonomyFactoryFromRemoteZip(
      createZipInputStream,
      transformDocument,
      extraSubstitutionGroupMap,
      relationshipFactory,
      arcFilter)
  }
}
