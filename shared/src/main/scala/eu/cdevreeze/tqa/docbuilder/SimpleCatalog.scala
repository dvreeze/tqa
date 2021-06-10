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

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.queryapi.BackingNodes
import eu.cdevreeze.yaidom.simple

/**
 * Simple XML catalog, as used in XBRL taxonomy packages.
 *
 * @author Chris de Vreeze
 */
final case class SimpleCatalog(xmlBaseOption: Option[URI], uriRewrites: IndexedSeq[SimpleCatalog.UriRewrite]) {

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

      val effectiveRewritePrefix: URI =
        xmlBaseOption
          .map(_.resolve(rewrite.effectiveRewritePrefix))
          .getOrElse(URI.create(rewrite.effectiveRewritePrefix))

      effectiveRewritePrefix.resolve(relativeUri)
    }
  }

  /**
   * Returns the equivalent of `findMappedUri(uri).get`.
   */
  def getMappedUri(uri: URI): URI = {
    findMappedUri(uri).getOrElse(sys.error(s"Could not map URI '$uri'"))
  }

  /**
   * Returns the same simple catalog, but first resolving XML base attributes. Therefore the result has no XML base attributes anywhere.
   */
  def netSimpleCatalog: SimpleCatalog = {
    val netUriRewrites: IndexedSeq[SimpleCatalog.UriRewrite] = uriRewrites.map { rewrite =>
      val effectiveRewritePrefix: URI =
        xmlBaseOption
          .map(_.resolve(rewrite.effectiveRewritePrefix))
          .getOrElse(URI.create(rewrite.effectiveRewritePrefix))

      SimpleCatalog.UriRewrite(None, rewrite.uriStartString, effectiveRewritePrefix.toString)
    }

    SimpleCatalog(None, netUriRewrites)
  }

  /**
   * Returns this simple catalog as the mapping of the net simple catalog.
   */
  def toMap: Map[String, String] = {
    netSimpleCatalog.uriRewrites.map { rewrite =>
      rewrite.uriStartString -> rewrite.rewritePrefix
    }.toMap
  }

  /**
   * Tries to reverse this simple catalog (after converting it to the net simple catalog), but if this simple catalog
   * is not invertible, the result is incorrect.
   */
  def reverse: SimpleCatalog = {
    val reverseMappings: Map[String, String] = toMap.toSeq.map(_.swap).toMap
    SimpleCatalog.from(reverseMappings)
  }

  def filter(p: SimpleCatalog.UriRewrite => Boolean): SimpleCatalog = {
    SimpleCatalog(xmlBaseOption, uriRewrites.filter(p))
  }

  def toElem: simple.Elem = {
    val scope = Scope.from("" -> SimpleCatalog.ErNamespace)

    import simple.Node._

    val uriRewriteElems = uriRewrites.map(_.toElem)

    emptyElem(QName("catalog"), scope)
      .plusAttributeOption(QName("xml:base"), xmlBaseOption.map(_.toString))
      .plusChildren(uriRewriteElems)
      .prettify(2)
  }
}

object SimpleCatalog {

  final case class UriRewrite(xmlBaseAttributeOption: Option[URI], uriStartString: String, rewritePrefix: String) {

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

    def fromElem(rewriteElem: BackingNodes.Elem): UriRewrite = {
      require(
        rewriteElem.resolvedName == ErRewriteURIEName,
        s"Expected $ErRewriteURIEName but got ${rewriteElem.resolvedName}")

      // If the rewriteElem has an absolute document URI, it also has an absolute base URI (typically the same).
      // If the rewriteElem has a relative document URI, it either has a relative or absolute base URI, potentially from xml:base attributes.
      // If the rewriteElem has no document URI, it either has no base URI, or a relative or absolute one filled from xml:base attributes.
      val xmlBaseOption: Option[URI] = rewriteElem.baseUriOption
      val parentXmlBaseOption: Option[URI] = rewriteElem.parentBaseUriOption

      val xmlBaseAttrOption: Option[URI] =
        parentXmlBaseOption
          .flatMap(pb => xmlBaseOption.map(b => pb.relativize(b)))
          .orElse(xmlBaseOption)
          .filter(_.toString.nonEmpty)

      val uriStartString = rewriteElem.attribute(UriStartStringEName)
      val rewritePrefix = rewriteElem.attribute(RewritePrefixEName)

      UriRewrite(xmlBaseAttrOption, uriStartString, rewritePrefix)
    }
  }

  def from(uriRewrites: Map[String, String]): SimpleCatalog = {
    SimpleCatalog(None, uriRewrites.toIndexedSeq.map {
      case (startString, rewritePrefix) => UriRewrite(None, startString, rewritePrefix)
    })
  }

  /**
   * Creates a SimpleCatalog from the given element. The base URI, if any, of the element, becomes the filled
   * xmlBaseOption of the SimpleCatalog. This optional base URI may be a relative URI (typically within ZIP files).
   */
  def fromElem(catalogElem: BackingNodes.Elem): SimpleCatalog = {
    require(catalogElem.resolvedName == ErCatalogEName, s"Expected $ErCatalogEName but got ${catalogElem.resolvedName}")

    // If the catalogElem has an absolute document URI, it also has an absolute base URI (typically the same).
    // If the catalogElem has a relative document URI, it either has a relative or absolute base URI, potentially from xml:base attributes.
    // If the catalogElem has no document URI, it either has no base URI, or a relative or absolute one filled from xml:base attributes.
    val xmlBaseOption: Option[URI] = catalogElem.baseUriOption

    val uriRewrites = catalogElem.filterChildElems(_.resolvedName == ErRewriteURIEName).map(e => UriRewrite.fromElem(e))

    SimpleCatalog(xmlBaseOption, uriRewrites)
  }

  val ErNamespace = "urn:oasis:names:tc:entity:xmlns:xml:catalog"

  val ErCatalogEName = EName(ErNamespace, "catalog")
  val ErRewriteURIEName = EName(ErNamespace, "rewriteURI")

  val UriStartStringEName = EName("uriStartString")
  val RewritePrefixEName = EName("rewritePrefix")
}
