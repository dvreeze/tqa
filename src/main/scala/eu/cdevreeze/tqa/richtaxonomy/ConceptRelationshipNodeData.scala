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

import eu.cdevreeze.tqa.ScopedXPathString
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode
import eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNodeFormulaAxis
import eu.cdevreeze.tqa.xpath.XPathEvaluator
import eu.cdevreeze.yaidom.core.EName

/**
 * Wrapper around a ConceptRelationshipNode, which can extract the relevant data by evaluating XPath where needed.
 *
 * @author Chris de Vreeze
 */
final class ConceptRelationshipNodeData(val conceptRelationshipNode: ConceptRelationshipNode) {

  def relationshipSources(implicit xpathEvaluator: XPathEvaluator): immutable.IndexedSeq[EName] = {
    val directlyMentionedSources = conceptRelationshipNode.relationshipSources.map(_.source)

    val xpathResultSources =
      conceptRelationshipNode.relationshipSourceExpressions.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsEName(xpathEvaluator.toXPathExpression(expr), None)
      }

    directlyMentionedSources ++ xpathResultSources
  }

  def linkroleOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    conceptRelationshipNode.linkroleOption.map(_.underlyingElem.text) orElse {
      conceptRelationshipNode.linkroleExpressionOption.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)
      }
    }
  }

  def arcroleOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    conceptRelationshipNode.arcroleOption.map(_.underlyingElem.text) orElse {
      conceptRelationshipNode.arcroleExpressionOption.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)
      }
    }
  }

  def linknameOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    conceptRelationshipNode.linknameOption.map(_.underlyingElem.text) orElse {
      conceptRelationshipNode.linknameExpressionOption.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)
      }
    }
  }

  def arcnameOption(implicit xpathEvaluator: XPathEvaluator): Option[String] = {
    conceptRelationshipNode.arcnameOption.map(_.underlyingElem.text) orElse {
      conceptRelationshipNode.arcnameExpressionOption.map(_.scopedXPathString) map { expr =>
        xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)
      }
    }
  }

  def formulaAxisOption(implicit xpathEvaluator: XPathEvaluator): Option[ConceptRelationshipNodeFormulaAxis.FormulaAxis] = {
    conceptRelationshipNode.formulaAxisOption.map(_.formulaAxis) orElse {
      conceptRelationshipNode.formulaAxisExpressionOption.map(_.scopedXPathString) map { expr =>
        val resultAsString = xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)

        ConceptRelationshipNodeFormulaAxis.FormulaAxis.fromString(resultAsString)
      }
    }
  }

  def generationsOption(implicit xpathEvaluator: XPathEvaluator): Option[Int] = {
    val resultAsStringOption =
      conceptRelationshipNode.generationsOption.map(_.underlyingElem.text) orElse {
        conceptRelationshipNode.generationsExpressionOption.map(_.scopedXPathString) map { expr =>
          xpathEvaluator.evaluateAsString(xpathEvaluator.toXPathExpression(expr), None)
        }
      }

    resultAsStringOption.map(_.toInt)
  }
}
