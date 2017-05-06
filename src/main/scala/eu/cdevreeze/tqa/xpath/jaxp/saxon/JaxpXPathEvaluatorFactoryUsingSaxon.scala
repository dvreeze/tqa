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

import eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory
import javax.xml.xpath
import net.sf.saxon.Configuration
import net.sf.saxon.event.Builder
import net.sf.saxon.om.NodeInfo

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
}

object JaxpXPathEvaluatorFactoryUsingSaxon {

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
}
