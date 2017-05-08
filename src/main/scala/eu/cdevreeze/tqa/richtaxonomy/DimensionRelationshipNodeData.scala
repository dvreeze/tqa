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

package eu.cdevreeze.tqa.richtaxonomy

import scala.collection.immutable

import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNodeFormulaAxis
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.yaidom.core.EName

/**
 * Wrapper around a DimensionRelationshipNode, which can extract the relevant data by evaluating XPath where needed.
 *
 * @author Chris de Vreeze
 */
final class DimensionRelationshipNodeData(val dimensionRelationshipNode: DimensionRelationshipNode) {

  // Below, make sure that the passed XPathEvaluator knows about the needed namespace bindings in the XPath expressions.

  /**
   * Returns the dimension as EName, by calling `dimensionRelationshipNode.dimensionName`.
   */
  def dimensionName: EName = dimensionRelationshipNode.dimensionName

  def relationshipSources(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[EName] = {
    val directlyMentionedSources = dimensionRelationshipNode.relationshipSources.map(_.source)

    val xpathResultSources =
      dimensionRelationshipNode.relationshipSourceExpressions.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsEName(xpathEvaluator.toXPathExpression(expr.xpathExpression), None)
      }

    directlyMentionedSources ++ xpathResultSources
  }

  def linkroleOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    dimensionRelationshipNode.linkroleOption.map(_.underlyingElem.text) orElse {
      dimensionRelationshipNode.linkroleExpressionOption.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr.xpathExpression), None)
      }
    }
  }

  def formulaAxis(implicit xpathEvaluator: XPathEvaluator): DimensionRelationshipNodeFormulaAxis.FormulaAxis = {
    dimensionRelationshipNode.formulaAxisOption.map(_.formulaAxis) orElse {
      dimensionRelationshipNode.formulaAxisExpressionOption.map(_.scopedXPathString) map { expr =>
        val resultAsString = xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr.xpathExpression), None)

        DimensionRelationshipNodeFormulaAxis.FormulaAxis.fromString(resultAsString)
      }
    } getOrElse (DimensionRelationshipNodeFormulaAxis.DescendantOrSelfAxis)
  }

  def generations(implicit xpathEvaluator: XPathEvaluator): Int = {
    val resultAsStringOption =
      dimensionRelationshipNode.generationsOption.map(_.underlyingElem.text) orElse {
        dimensionRelationshipNode.generationsExpressionOption.map(_.scopedXPathString) map { expr =>
          xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr.xpathExpression), None)
        }
      }

    resultAsStringOption.map(_.toInt).getOrElse(0)
  }
}
