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

package eu.cdevreeze.tqa.xpath.jaxp.saxon

import java.net.URI

import scala.collection.JavaConverters.setAsJavaSetConverter

import eu.cdevreeze.tqa.xpath.SimpleUriResolver
import eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory
import eu.cdevreeze.yaidom.core.Scope
import javax.xml.transform.URIResolver
import javax.xml.xpath
import net.sf.saxon.Configuration
import net.sf.saxon.event.Builder
import net.sf.saxon.om.NamespaceResolver
import net.sf.saxon.om.NodeInfo
import net.sf.saxon.pull.NamespaceContextImpl

/**
 * XPathEvaluatorFactory using the JAXP XPath API and backed by a Saxon implementation.
 *
 * The used Saxon Configuration must use the (default) tiny tree object model!
 *
 * See `JaxpXPathEvaluatorUsingSaxon` for more remarks about the used Saxon Configuration.
 *
 * See http://saxonica.com/html/documentation/xpath-api/jaxp-xpath/factory.html.
 *
 * @author Chris de Vreeze
 */
final class JaxpXPathEvaluatorFactoryUsingSaxon(
  val underlyingEvaluatorFactory: net.sf.saxon.xpath.XPathFactoryImpl) extends XPathEvaluatorFactory {

  require(
    underlyingEvaluatorFactory.getConfiguration.getTreeModel == Builder.TINY_TREE,
    s"Expected Saxon Configuration requiring the tiny tree model, but found tree model ${underlyingEvaluatorFactory.getConfiguration.getTreeModel}")

  type XPathExpression = xpath.XPathExpression

  type Node = NodeInfo

  type ContextItem = NodeInfo

  def config: Configuration = underlyingEvaluatorFactory.getConfiguration

  def newXPathEvaluator(): JaxpXPathEvaluatorUsingSaxon = {
    new JaxpXPathEvaluatorUsingSaxon(
      underlyingEvaluatorFactory.newXPath().asInstanceOf[net.sf.saxon.xpath.XPathEvaluator])
  }

  /**
   * Creates an XPathEvaluator from the provided (optional) document URI, scope and URI resolver.
   *
   * The scope is typically the scope of the root element of the document whose URI is provided, enhanced with the
   * minimal scope (for XPath evaluation).
   *
   * The URIResolver should build Saxon tiny trees using the same Configuration as the one underlying this factory.
   * Consider passing a SimpleUriResolver.
   */
  def newXPathEvaluator(
    docUriOption: Option[URI],
    scope:        Scope,
    uriResolver:  URIResolver): JaxpXPathEvaluatorUsingSaxon = {

    config.setURIResolver(uriResolver)

    val saxonXPathEvaluatorFactory = underlyingEvaluatorFactory

    val saxonXPathEvaluator =
      saxonXPathEvaluatorFactory.newXPath().asInstanceOf[net.sf.saxon.xpath.XPathEvaluator]

    // Just passing scope.toNamespaceContext will lead to an UnsupportedOperationException in
    // net.sf.saxon.xpath.JAXPXPathStaticContext.iteratePrefixes later on. Hence we create a Saxon NamespaceResolver
    // and turn that into a JAXP NamespaceContext that is also a Saxon NamespaceResolver.

    saxonXPathEvaluator.setNamespaceContext(
      new NamespaceContextImpl(JaxpXPathEvaluatorFactoryUsingSaxon.makeSaxonNamespaceResolver(scope)))

    docUriOption foreach { docUri =>
      saxonXPathEvaluator.getStaticContext().setBaseURI(docUri.toString)
    }

    new JaxpXPathEvaluatorUsingSaxon(saxonXPathEvaluator).ensuring(_.underlyingEvaluator.getConfiguration == config)
  }

  def newXPathEvaluator(
    docUriOption: Option[URI],
    scope:        Scope): JaxpXPathEvaluatorUsingSaxon = {

    newXPathEvaluator(docUriOption, scope, SimpleUriResolver.identity)
  }
}

object JaxpXPathEvaluatorFactoryUsingSaxon {

  /**
   * Minimal scope used for XPath processing.
   */
  val MinimalScope: Scope = {
    Scope.from(
      "" -> "http://www.w3.org/2005/xpath-functions",
      "fn" -> "http://www.w3.org/2005/xpath-functions",
      "math" -> "http://www.w3.org/2005/xpath-functions/math",
      "map" -> "http://www.w3.org/2005/xpath-functions/map",
      "array" -> "http://www.w3.org/2005/xpath-functions/array",
      "xfi" -> "http://www.xbrl.org/2008/function/instance",
      "xs" -> "http://www.w3.org/2001/XMLSchema")
  }

  /**
   * Creates an XPathEvaluatorFactory from the provided Saxon Configuration.
   *
   * The underlying Saxon XPathFactoryImpl can be configured afterwards, before using the JaxpXPathEvaluatorFactoryUsingSaxon object.
   * For example, functions and variables (shared by all created XPathEvaluators) can be registered.
   */
  def newInstance(config: Configuration): JaxpXPathEvaluatorFactoryUsingSaxon = {
    // Saxon XPathFactory
    val xpathEvaluatorFactory = new net.sf.saxon.xpath.XPathFactoryImpl(config)

    new JaxpXPathEvaluatorFactoryUsingSaxon(xpathEvaluatorFactory).ensuring(_.underlyingEvaluatorFactory.getConfiguration == config)
  }

  /**
   * Creates a Saxon NamespaceResolver from a yaidom Scope. The result can be wrapped in a NamespaceContextImpl,
   * which in turn can be set on a Saxon XPathEvaluator. This way of setting a NamespaceContext on the Saxon
   * XPathEvaluator ensures that JAXPXPathStaticContext.iteratePrefixes does not throw an UnsupportedOperationException.
   *
   * This method is called by function newXPathEvaluator, but can also be called by user code.
   */
  def makeSaxonNamespaceResolver(scope: Scope): NamespaceResolver = {
    new NamespaceResolver {

      override def iteratePrefixes(): java.util.Iterator[String] = {
        val prefixes = (scope.keySet + "xml")
        prefixes.asJava.iterator
      }

      override def getURIForPrefix(prefix: String, useDefault: Boolean): String = {
        val effectiveScope = if (useDefault) scope else scope.withoutDefaultNamespace

        prefix match {
          case "xml" =>
            "http://www.w3.org/XML/1998/namespace"
          case pref =>
            effectiveScope.prefixNamespaceMap.getOrElse(pref, "")
        }
      }
    }
  }
}
