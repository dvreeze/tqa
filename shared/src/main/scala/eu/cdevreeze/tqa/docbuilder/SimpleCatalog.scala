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

package eu.cdevreeze.tqa.docbuilder

import java.net.URI

import eu.cdevreeze.tqa.common.names.ENames
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.queryapi.ScopedNodes
import eu.cdevreeze.yaidom.simple

/**
 * Simple XML catalog, as used in XBRL taxonomy packages.
 *
 * @author Chris de Vreeze
 */
final case class SimpleCatalog(
    docUriOption: Option[URI],
    xmlBaseAttributeOption: Option[URI],
    uriRewrites: IndexedSeq[SimpleCatalog.UriRewrite]) {

  // The implementation uses the assumption that URI.resolve is a transitive operation!

  def withDocUriOption(newDocUriOption: Option[URI]): SimpleCatalog = {
    copy(docUriOption = newDocUriOption)
  }

  def xmlBaseOption: Option[URI] = {
    docUriOption.map(u => xmlBaseAttributeOption.map(b => u.resolve(b)).getOrElse(u)).orElse(xmlBaseAttributeOption)
  }

  /**
   * Applies the best matching rewrite rule to the given URI, if any, and returns the optional
   * resulting URI. Matching is done after normalizing the URI, as well as the URI start strings.
   *
   * The best matching rewrite rule, if any, is the one with the longest matching URI start string.
   */
  def findMappedUri(uri: URI): Option[URI] = {
    val sortedRewrites = uriRewrites.sortBy(_.normalizedUriStartString.length).reverse
    val normalizedUri = uri.normalize
    val normalizedUriString = normalizedUri.toString

    val uriRewriteOption =
      sortedRewrites.find(rewrite => normalizedUriString.startsWith(rewrite.normalizedUriStartString))

    uriRewriteOption.map { rewrite =>
      val relativeUri: URI =
        URI.create(rewrite.normalizedUriStartString).relativize(normalizedUri).ensuring(u => !u.isAbsolute)

      val effRewritePrefix: URI = URI.create(rewrite.effectiveRewritePrefix(xmlBaseOption))

      effRewritePrefix.resolve(relativeUri)
    }
  }

  /**
   * Returns the equivalent of `findMappedUri(uri).get`.
   */
  def getMappedUri(uri: URI): URI = {
    findMappedUri(uri).getOrElse(sys.error(s"Could not map URI '$uri'"))
  }

  /**
   * Returns the same simple catalog, but first resolving XML base attributes (without taking the optional
   * document URI into account). Therefore the result has no XML base attributes anywhere. The optional
   * document URI is the same in the result as in this catalog.
   */
  def netSimpleCatalog: SimpleCatalog = {
    val netUriRewrites: IndexedSeq[SimpleCatalog.UriRewrite] = uriRewrites.map { rewrite =>
      // Ignoring the optional document URI, but using the optional XML Base attribute of this catalog
      val effRewritePrefix: URI = URI.create(rewrite.effectiveRewritePrefix(xmlBaseAttributeOption))

      SimpleCatalog.UriRewrite(None, rewrite.uriStartString, effRewritePrefix.toString)
    }

    SimpleCatalog(docUriOption, None, netUriRewrites)
  }

  /**
   * Returns this simple catalog as the mapping of the net simple catalog, but also taking XML Base resolution including
   * the optional document URI into account.
   */
  def toMap: Map[String, String] = {
    val catalogXmlBaseOption: Option[URI] = xmlBaseOption

    uriRewrites.map { rewrite =>
      rewrite.uriStartString -> rewrite.effectiveRewritePrefix(catalogXmlBaseOption)
    }.toMap
  }

  /**
   * Returns this simple catalog as the mapping of the net simple catalog, ignoring the optional document URI.
   */
  def toMapIgnoringDocUri: Map[String, String] = {
    netSimpleCatalog.uriRewrites.map { rewrite =>
      rewrite.uriStartString -> rewrite.rewritePrefix
    }.toMap
  }

  /**
   * Tries to reverse this simple catalog (after XML Base resolution including the optional document URI), but if this simple catalog
   * is not invertible, the result is incorrect.
   */
  def reverse: SimpleCatalog = {
    val reverseMappings: Map[String, String] = toMap.toSeq.map(_.swap).toMap
    SimpleCatalog.from(reverseMappings)
  }

  def filter(p: SimpleCatalog.UriRewrite => Boolean): SimpleCatalog = {
    SimpleCatalog(docUriOption, xmlBaseAttributeOption, uriRewrites.filter(p))
  }

  /**
   * Converts this catalog to a yaidom simple element. The optional document URI is ignored.
   */
  def toElem: simple.Elem = {
    val scope = Scope.from("" -> SimpleCatalog.ErNamespace)

    import simple.Node._

    val uriRewriteElems = uriRewrites.map(_.toElem)

    emptyElem(QName("catalog"), scope)
      .plusAttributeOption(QName("xml:base"), xmlBaseAttributeOption.map(_.toString))
      .plusChildren(uriRewriteElems)
      .prettify(2)
  }
}

object SimpleCatalog {

  final case class UriRewrite(xmlBaseAttributeOption: Option[URI], uriStartString: String, rewritePrefix: String) {

    def xmlBaseOption(parentBaseUriOption: Option[URI]): Option[URI] = {
      parentBaseUriOption
        .map(u => xmlBaseAttributeOption.map(b => u.resolve(b)).getOrElse(u))
        .orElse(xmlBaseAttributeOption)
    }

    /**
     * Returns the normalized URI start string, which is used for matching against normalized URIs.
     */
    def normalizedUriStartString: String = {
      URI.create(uriStartString).normalize.toString
    }

    /**
     * Returns the rewrite prefix, but if this rewrite element contains an XML Base attribute, first
     * resolves the rewrite prefix against that XML Base attribute.
     */
    def effectiveRewritePrefix: String = {
      xmlBaseAttributeOption.map(_.resolve(rewritePrefix).toString).getOrElse(rewritePrefix)
    }

    /**
     * Returns the rewrite prefix, after XML Base resolution, given the passed optional parent base URI.
     */
    def effectiveRewritePrefix(parentBaseUriOption: Option[URI]): String = {
      xmlBaseOption(parentBaseUriOption).map(_.resolve(rewritePrefix).toString).getOrElse(rewritePrefix)
    }

    def toElem: simple.Elem = {
      val scope = Scope.from("" -> SimpleCatalog.ErNamespace)

      import simple.Node._

      emptyElem(QName("rewriteURI"), scope)
        .plusAttributeOption(QName("xml:base"), xmlBaseAttributeOption.map(_.toString))
        .plusAttribute(QName("uriStartString"), uriStartString)
        .plusAttribute(QName("rewritePrefix"), rewritePrefix)
    }
  }

  object UriRewrite {

    def apply(uriStartString: String, rewritePrefix: String): UriRewrite = {
      UriRewrite(None, uriStartString, rewritePrefix)
    }

    def fromElem(rewriteElem: ScopedNodes.Elem): UriRewrite = {
      require(
        rewriteElem.resolvedName == ErRewriteURIEName,
        s"Expected $ErRewriteURIEName but got ${rewriteElem.resolvedName}")

      val xmlBaseAttrOption: Option[URI] = rewriteElem.attributeOption(ENames.XmlBaseEName).map(URI.create)
      val uriStartString = rewriteElem.attribute(UriStartStringEName)
      val rewritePrefix = rewriteElem.attribute(RewritePrefixEName)

      UriRewrite(xmlBaseAttrOption, uriStartString, rewritePrefix)
    }
  }

  def apply(docUriOption: Option[URI], uriRewrites: IndexedSeq[SimpleCatalog.UriRewrite]): SimpleCatalog = {
    SimpleCatalog(docUriOption, None, uriRewrites)
  }

  def from(uriRewrites: Map[String, String]): SimpleCatalog = {
    SimpleCatalog(None, None, uriRewrites.toIndexedSeq.map {
      case (startString, rewritePrefix) => UriRewrite(None, startString, rewritePrefix)
    })
  }

  /**
   * Creates a SimpleCatalog from the given element. This optional document URI may be a relative URI (typically
   * within ZIP files).
   */
  def fromElem(catalogElem: BackingNodes.Elem): SimpleCatalog = {
    require(catalogElem.resolvedName == ErCatalogEName, s"Expected $ErCatalogEName but got ${catalogElem.resolvedName}")

    val docUriOption: Option[URI] = catalogElem.docUriOption
    val xmlBaseAttrOption: Option[URI] = catalogElem.attributeOption(ENames.XmlBaseEName).map(URI.create)

    val uriRewrites = catalogElem.filterChildElems(_.resolvedName == ErRewriteURIEName).map(e => UriRewrite.fromElem(e))

    SimpleCatalog(docUriOption, xmlBaseAttrOption, uriRewrites)
  }

  val ErNamespace = "urn:oasis:names:tc:entity:xmlns:xml:catalog"

  val ErCatalogEName = EName(ErNamespace, "catalog")
  val ErRewriteURIEName = EName(ErNamespace, "rewriteURI")

  val UriStartStringEName = EName("uriStartString")
  val RewritePrefixEName = EName("rewritePrefix")
}
