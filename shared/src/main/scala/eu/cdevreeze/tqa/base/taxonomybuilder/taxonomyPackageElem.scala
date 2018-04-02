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

package eu.cdevreeze.tqa.base.taxonomybuilder

import java.net.URI
import java.time.LocalDate

import scala.collection.immutable
import scala.reflect.classTag

import TaxonomyPackageElem._
import eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ScopedElemLike
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.queryapi.SubtypeAwareElemLike

/**
 * XML element inside a taxonomy package XML tree. This API is immutable, provided the backing element is immutable.
 *
 * The yaidom `SubtypeAwareElemApi` and `ScopedElemApi` query API is offered.
 *
 * Note that the package-private constructor contains redundant data, in order to speed up (yaidom-based) querying.
 *
 * It is not required that the taxonomy package elements are schema-valid. Construction of a taxonomy package element is indeed quite lenient.
 *
 * Note that the backing element implementation can be any implementation of yaidom query API trait `BackingNodes.Elem`.
 *
 * This class hierarchy depends on Java 8 or later, due to the use of Java 8 time API.
 *
 * @author Chris de Vreeze
 */
sealed class TaxonomyPackageElem private[taxonomybuilder] (
  val backingElem: BackingNodes.Elem,
  childElems:      immutable.IndexedSeq[TaxonomyPackageElem]) extends ScopedNodes.Elem with ScopedElemLike with SubtypeAwareElemLike {

  // TODO Restore old equality on the backing elements themselves (after JS DOM wrappers have appropriate equality)
  assert(
    childElems.map(_.backingElem).map(_.resolvedName) == backingElem.findAllChildElems.map(_.resolvedName),
    msg("Corrupt element!"))

  type ThisElem = TaxonomyPackageElem

  type ThisNode = TaxonomyPackageElem

  final def thisElem: ThisElem = this

  // We are not interested in non-element children

  final def children: immutable.IndexedSeq[ThisNode] = findAllChildElems

  /**
   * Very fast implementation of findAllChildElems, for fast querying
   */
  final def findAllChildElems: immutable.IndexedSeq[TaxonomyPackageElem] = childElems

  final def resolvedName: EName = backingElem.resolvedName

  final def resolvedAttributes: immutable.Iterable[(EName, String)] = backingElem.resolvedAttributes

  final def qname: QName = backingElem.qname

  final def attributes: immutable.Iterable[(QName, String)] = backingElem.attributes

  final def scope: Scope = backingElem.scope

  final def text: String = backingElem.text

  final override def equals(other: Any): Boolean = other match {
    case e: TaxonomyPackageElem => backingElem == e.backingElem
    case _                      => false
  }

  final override def hashCode: Int = backingElem.hashCode

  private def msg(s: String): String = s"${s} (${backingElem.key})"
}

/**
 * Taxonomy package root element.
 *
 * It does not check validity of the taxonomy package.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyPackage private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpTaxonomyPackageEName, s"Expected EName $TpTaxonomyPackageEName but found $resolvedName")
  require(backingElem.path.isEmpty, s"The TaxonomyPackage must be the root element")

  def filterEntryPoints(p: EntryPoint => Boolean): immutable.IndexedSeq[EntryPoint] = {
    filterElemsOfType(classTag[EntryPoint])(p)
  }

  def findEntryPoint(p: EntryPoint => Boolean): Option[EntryPoint] = {
    findElemOfType(classTag[EntryPoint])(p)
  }

  def getEntryPoint(p: EntryPoint => Boolean): EntryPoint = {
    findElemOfType(classTag[EntryPoint])(p).
      getOrElse(sys.error(s"Missing entryPoint obeying the given predicate"))
  }

  // Child elements

  def getIdentifier: Identifier = {
    getChildElemOfType(classTag[Identifier])(_ => true)
  }

  def findAllDocumentationGroups: immutable.IndexedSeq[DocumentationGroup] = {
    findAllChildElemsOfType(classTag[DocumentationGroup])
  }

  def findAllNames: immutable.IndexedSeq[Name] = {
    findAllChildElemsOfType(classTag[Name])
  }

  def findAllDescriptions: immutable.IndexedSeq[Description] = {
    findAllChildElemsOfType(classTag[Description])
  }

  def findVersion: Option[Version] = {
    findChildElemOfType(classTag[Version])(_ => true)
  }

  def findLicense: Option[License] = {
    findChildElemOfType(classTag[License])(_ => true)
  }

  def findAllPublishers: immutable.IndexedSeq[Publisher] = {
    findAllChildElemsOfType(classTag[Publisher])
  }

  def findPublisherUrl: Option[PublisherUrl] = {
    findChildElemOfType(classTag[PublisherUrl])(_ => true)
  }

  def findPublisherCountry: Option[PublisherCountry] = {
    findChildElemOfType(classTag[PublisherCountry])(_ => true)
  }

  def findPublicationDate: Option[PublicationDate] = {
    findChildElemOfType(classTag[PublicationDate])(_ => true)
  }

  def findEntryPointsElem: Option[EntryPointsElem] = {
    findChildElemOfType(classTag[EntryPointsElem])(_ => true)
  }

  def findSupersededTaxonomyPackagesElem: Option[SupersededTaxonomyPackagesElem] = {
    findChildElemOfType(classTag[SupersededTaxonomyPackagesElem])(_ => true)
  }

  def findVersioningReportsElem: Option[VersioningReportsElem] = {
    findChildElemOfType(classTag[VersioningReportsElem])(_ => true)
  }
}

/**
 * Identifier in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Identifier private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpIdentifierEName, s"Expected EName $TpIdentifierEName but found $resolvedName")

  def value: URI = URI.create(text)
}

/**
 * Documentation group, so either a Name or a Description
 *
 * @author Chris de Vreeze
 */
sealed trait DocumentationGroup extends TaxonomyPackageElem {

  def value: String
}

/**
 * Name in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Name private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) with DocumentationGroup {

  require(resolvedName == TpNameEName, s"Expected EName $TpNameEName but found $resolvedName")

  def value: String = text
}

/**
 * Description in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Description private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) with DocumentationGroup {

  require(resolvedName == TpDescriptionEName, s"Expected EName $TpDescriptionEName but found $resolvedName")

  def value: String = text
}

/**
 * Version in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Version private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpVersionEName, s"Expected EName $TpVersionEName but found $resolvedName")

  def value: String = text
}

/**
 * License in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class License private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpLicenseEName, s"Expected EName $TpLicenseEName but found $resolvedName")

  def href: URI = URI.create(attribute(HrefEName))

  def name: String = attribute(NameEName)

  def value: String = text
}

/**
 * Publisher in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Publisher private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpPublisherEName, s"Expected EName $TpPublisherEName but found $resolvedName")

  def value: String = text
}

/**
 * Publisher URL in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class PublisherUrl private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpPublisherURLEName, s"Expected EName $TpPublisherURLEName but found $resolvedName")

  def value: URI = URI.create(text)
}

/**
 * Publisher country in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class PublisherCountry private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpPublisherCountryEName, s"Expected EName $TpPublisherCountryEName but found $resolvedName")

  def value: String = text
}

/**
 * Publication date in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class PublicationDate private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpPublicationDateEName, s"Expected EName $TpPublicationDateEName but found $resolvedName")

  // TODO What about timezones?
  def value: LocalDate = LocalDate.parse(text)
}

/**
 * EntryPoints element in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class EntryPointsElem private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpEntryPointsEName, s"Expected EName $TpEntryPointsEName but found $resolvedName")

  def findAllEntryPoints: immutable.IndexedSeq[EntryPoint] = {
    findAllChildElemsOfType(classTag[EntryPoint])
  }
}

/**
 * Superseded taxonomy packages element in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class SupersededTaxonomyPackagesElem private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpSupersededTaxonomyPackagesEName, s"Expected EName $TpSupersededTaxonomyPackagesEName but found $resolvedName")

  def findAllTaxonomyPackageRefs: immutable.IndexedSeq[TaxonomyPackageRef] = {
    findAllChildElemsOfType(classTag[TaxonomyPackageRef])
  }
}

/**
 * Versioning reports element in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class VersioningReportsElem private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpVersioningReportsEName, s"Expected EName $TpVersioningReportsEName but found $resolvedName")

  def findAllVersioningReports: immutable.IndexedSeq[VersioningReport] = {
    findAllChildElemsOfType(classTag[VersioningReport])
  }
}

/**
 * EntryPoint in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class EntryPoint private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpEntryPointEName, s"Expected EName $TpEntryPointEName but found $resolvedName")

  def findAllEntryPointHrefs: immutable.IndexedSeq[URI] = {
    findAllEntryPointDocuments.map(_.href)
  }

  def findAllDocumentationGroups: immutable.IndexedSeq[DocumentationGroup] = {
    findAllChildElemsOfType(classTag[DocumentationGroup])
  }

  def findAllNames: immutable.IndexedSeq[Name] = {
    findAllChildElemsOfType(classTag[Name])
  }

  def findAllDescriptions: immutable.IndexedSeq[Description] = {
    findAllChildElemsOfType(classTag[Description])
  }

  def findVersion: Option[Version] = {
    findChildElemOfType(classTag[Version])(_ => true)
  }

  def findAllEntryPointDocuments: immutable.IndexedSeq[EntryPointDocument] = {
    findAllChildElemsOfType(classTag[EntryPointDocument])
  }

  def findLanguagesElem: Option[LanguagesElem] = {
    findChildElemOfType(classTag[LanguagesElem])(_ => true)
  }
}

/**
 * EntryPoint document in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class EntryPointDocument private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpEntryPointDocumentEName, s"Expected EName $TpEntryPointDocumentEName but found $resolvedName")

  def href: URI = URI.create(attribute(HrefEName))
}

/**
 * Versioning report in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class VersioningReport private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpVersioningReportEName, s"Expected EName $TpVersioningReportEName but found $resolvedName")

  def href: URI = URI.create(attribute(HrefEName))
}

/**
 * Languages element in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class LanguagesElem private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpLanguagesEName, s"Expected EName $TpLanguagesEName but found $resolvedName")

  def findAllLanguages: immutable.IndexedSeq[Language] = {
    findAllChildElemsOfType(classTag[Language])
  }
}

/**
 * Language in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class Language private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpLanguageEName, s"Expected EName $TpLanguageEName but found $resolvedName")

  def value: String = text
}

/**
 * Taxonomy package reference in a taxonomy package
 *
 * @author Chris de Vreeze
 */
final class TaxonomyPackageRef private[taxonomybuilder] (
  override val backingElem: BackingNodes.Elem,
  childElems:               immutable.IndexedSeq[TaxonomyPackageElem]) extends TaxonomyPackageElem(backingElem, childElems) {

  require(resolvedName == TpTaxonomyPackageRefEName, s"Expected EName $TpTaxonomyPackageRefEName but found $resolvedName")

  def value: URI = URI.create(text)
}

// Companion objects

object TaxonomyPackageElem {

  val TpNs = "http://xbrl.org/2016/taxonomy-package"

  val TpTaxonomyPackageEName = EName(TpNs, "taxonomyPackage")
  val TpIdentifierEName = EName(TpNs, "identifier")
  val TpVersionEName = EName(TpNs, "version")
  val TpLicenseEName = EName(TpNs, "license")
  val TpPublisherEName = EName(TpNs, "publisher")
  val TpPublisherURLEName = EName(TpNs, "publisherURL")
  val TpPublisherCountryEName = EName(TpNs, "publisherCountry")
  val TpPublicationDateEName = EName(TpNs, "publicationDate")
  val TpEntryPointsEName = EName(TpNs, "entryPoints")
  val TpEntryPointEName = EName(TpNs, "entryPoint")
  val TpSupersededTaxonomyPackagesEName = EName(TpNs, "supersededTaxonomyPackages")
  val TpVersioningReportsEName = EName(TpNs, "versioningReports")
  val TpEntryPointDocumentEName = EName(TpNs, "entryPointDocument")
  val TpLanguagesEName = EName(TpNs, "languages")
  val TpTaxonomyPackageRefEName = EName(TpNs, "taxonomyPackageRef")
  val TpVersioningReportEName = EName(TpNs, "versioningReport")
  val TpNameEName = EName(TpNs, "name")
  val TpDescriptionEName = EName(TpNs, "description")
  val TpLanguageEName = EName(TpNs, "language")

  val HrefEName = EName("href")
  val NameEName = EName("name")

  /**
   * Expensive method to create an TaxonomyPackageElem tree
   */
  def apply(elem: BackingNodes.Elem): TaxonomyPackageElem = {
    // Recursive calls
    val childElems = elem.findAllChildElems.map(e => apply(e))
    apply(elem, childElems)
  }

  private[taxonomybuilder] def apply(elem: BackingNodes.Elem, childElems: immutable.IndexedSeq[TaxonomyPackageElem]): TaxonomyPackageElem = {
    elem.resolvedName match {
      case TpTaxonomyPackageEName            => new TaxonomyPackage(elem, childElems)
      case TpIdentifierEName                 => new Identifier(elem, childElems)
      case TpVersionEName                    => new Version(elem, childElems)
      case TpLicenseEName                    => new License(elem, childElems)
      case TpPublisherEName                  => new Publisher(elem, childElems)
      case TpPublisherURLEName               => new PublisherUrl(elem, childElems)
      case TpPublisherCountryEName           => new PublisherCountry(elem, childElems)
      case TpPublicationDateEName            => new PublicationDate(elem, childElems)
      case TpEntryPointsEName                => new EntryPointsElem(elem, childElems)
      case TpEntryPointEName                 => new EntryPoint(elem, childElems)
      case TpSupersededTaxonomyPackagesEName => new SupersededTaxonomyPackagesElem(elem, childElems)
      case TpVersioningReportsEName          => new VersioningReportsElem(elem, childElems)
      case TpEntryPointDocumentEName         => new EntryPointDocument(elem, childElems)
      case TpLanguagesEName                  => new LanguagesElem(elem, childElems)
      case TpTaxonomyPackageRefEName         => new TaxonomyPackageRef(elem, childElems)
      case TpVersioningReportEName           => new VersioningReport(elem, childElems)
      case TpNameEName                       => new Name(elem, childElems)
      case TpDescriptionEName                => new Description(elem, childElems)
      case TpLanguageEName                   => new Language(elem, childElems)
      case _                                 => new TaxonomyPackageElem(elem, childElems)
    }
  }
}

object TaxonomyPackage {

  def apply(elem: BackingNodes.Elem): TaxonomyPackage = {
    require(elem.resolvedName == TpTaxonomyPackageEName)
    TaxonomyPackageElem.apply(elem).asInstanceOf[TaxonomyPackage]
  }
}
