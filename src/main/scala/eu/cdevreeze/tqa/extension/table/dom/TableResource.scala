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

package eu.cdevreeze.tqa.extension.table.dom

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.classTag

import eu.cdevreeze.tqa
import eu.cdevreeze.tqa.ENames
import eu.cdevreeze.tqa.Namespaces
import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.xlink.XLinkResource
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.queryapi.BackingElemApi
import javax.xml.bind.DatatypeConverter

/**
 * XLink resource in a table link.
 *
 * @author Chris de Vreeze
 */
sealed trait TableResource extends tqa.dom.AnyTaxonomyElem with XLinkResource {

  def underlyingResource: tqa.dom.NonStandardResource

  final def backingElem: BackingElemApi = underlyingResource.backingElem

  final def xlinkType: String = underlyingResource.xlinkType

  final def xlinkAttributes: Map[EName, String] = underlyingResource.xlinkAttributes

  final def elr: String = underlyingResource.elr

  final def underlyingParentElem: BackingElemApi = underlyingResource.backingElem.parent

  final def xlinkLabel: String = underlyingResource.xlinkLabel

  final def roleOption: Option[String] = underlyingResource.roleOption

  final def key: XmlFragmentKey = underlyingResource.key

  protected[dom] def requireResolvedName(ename: EName): Unit = {
    require(
      underlyingResource.resolvedName == ename,
      s"Expected $ename but found ${underlyingResource.resolvedName} in ${underlyingResource.docUri}")
  }

  protected[dom] def filterNonXLinkChildElemsOfType[A <: OtherTableOrFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingResource.findAllChildElemsOfType(classTag[tqa.dom.OtherElem]).
      flatMap(e => OtherTableElem.opt(e).orElse(OtherFormulaElem.opt(e))) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfType[A <: OtherTableOrFormulaElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfType(cls)(_ => true)
  }
}

/**
 * A table:table.
 */
final class Table(val underlyingResource: tqa.dom.NonStandardResource) extends TableResource {
  requireResolvedName(ENames.TableTableEName)

  /**
   * Returns the parent-child-order, defaulting to parent-first.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def parentChildOrder: ParentChildOrder = {
    underlyingResource.attributeOption(ENames.ParentChildOrderEName).
      map(s => ParentChildOrder.fromString(s)).getOrElse(ParentChildOrder.ParentFirst)
  }
}

/**
 * A table:breakdown.
 */
final class TableBreakdown(val underlyingResource: tqa.dom.NonStandardResource) extends TableResource {
  requireResolvedName(ENames.TableBreakdownEName)

  /**
   * Returns the optional parent-child-order.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def parentChildOrderOption: Option[ParentChildOrder] = {
    underlyingResource.attributeOption(ENames.ParentChildOrderEName).map(s => ParentChildOrder.fromString(s))
  }
}

/**
 * A definition node.
 */
sealed abstract class DefinitionNode(val underlyingResource: tqa.dom.NonStandardResource) extends TableResource {

  final def tagSelectorOption: Option[String] = {
    underlyingResource.attributeOption(ENames.TagSelectorEName)
  }
}

/**
 * A closed definition node.
 */
sealed abstract class ClosedDefinitionNode(underlyingResource: tqa.dom.NonStandardResource) extends DefinitionNode(underlyingResource) {

  /**
   * Returns the optional parent-child-order.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def parentChildOrderOption: Option[ParentChildOrder] = {
    underlyingResource.attributeOption(ENames.ParentChildOrderEName).map(s => ParentChildOrder.fromString(s))
  }
}

/**
 * An open definition node.
 */
sealed abstract class OpenDefinitionNode(underlyingResource: tqa.dom.NonStandardResource) extends DefinitionNode(underlyingResource)

/**
 * A table:ruleNode.
 */
final class RuleNode(underlyingResource: tqa.dom.NonStandardResource) extends ClosedDefinitionNode(underlyingResource) {
  requireResolvedName(ENames.TableRuleNodeEName)

  def untaggedAspects: immutable.IndexedSeq[FormulaAspect] = {
    findAllNonXLinkChildElemsOfType(classTag[FormulaAspect])
  }

  def allAspects: immutable.IndexedSeq[FormulaAspect] = {
    untaggedAspects ++ (ruleSets.flatMap(_.aspects))
  }

  def findAllUntaggedAspectsOfType[A <: FormulaAspect](cls: ClassTag[A]): immutable.IndexedSeq[A] = {
    implicit val clsTag = cls
    untaggedAspects collect { case asp: A => asp }
  }

  def allAspectsByTagOption: Map[Option[String], immutable.IndexedSeq[FormulaAspect]] = {
    val taggedAspects: immutable.IndexedSeq[(FormulaAspect, Option[String])] =
      untaggedAspects.map(aspect => (aspect, None)) ++
        ruleSets.flatMap(ruleSet => ruleSet.aspects.map(aspect => (aspect, Some(ruleSet.tag))))

    taggedAspects.groupBy({ case (aspect, tagOption) => tagOption }).mapValues(taggedAspects => taggedAspects.map(_._1))
  }

  def ruleSets: immutable.IndexedSeq[RuleSet] = {
    findAllNonXLinkChildElemsOfType(classTag[RuleSet])
  }

  /**
   * Returns the abstract attribute as boolean, returning false if absent.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def isAbstract: Boolean =
    underlyingResource.attributeOption(ENames.AbstractEName).
      map(v => DatatypeConverter.parseBoolean(v)).getOrElse(false)

  /**
   * Returns the merge attribute as boolean, returning false if absent.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def isMerged: Boolean =
    underlyingResource.attributeOption(ENames.MergeEName).
      map(v => DatatypeConverter.parseBoolean(v)).getOrElse(false)
}

/**
 * A relationship node.
 */
sealed abstract class RelationshipNode(underlyingResource: tqa.dom.NonStandardResource) extends ClosedDefinitionNode(underlyingResource)

/**
 * A table:conceptRelationshipNode.
 */
final class ConceptRelationshipNode(underlyingResource: tqa.dom.NonStandardResource) extends RelationshipNode(underlyingResource) {
  requireResolvedName(ENames.TableConceptRelationshipNodeEName)

  def relationshipSources: immutable.IndexedSeq[RelationshipSource] = {
    findAllNonXLinkChildElemsOfType(classTag[RelationshipSource])
  }

  def relationshipSourceExpressions: immutable.IndexedSeq[RelationshipSourceExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[RelationshipSourceExpression])
  }

  def linkroleOption: Option[Linkrole] = {
    findAllNonXLinkChildElemsOfType(classTag[Linkrole]).headOption
  }

  def linkroleExpressionOption: Option[LinkroleExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[LinkroleExpression]).headOption
  }

  def arcroleOption: Option[Arcrole] = {
    findAllNonXLinkChildElemsOfType(classTag[Arcrole]).headOption
  }

  def arcroleExpressionOption: Option[ArcroleExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ArcroleExpression]).headOption
  }

  def formulaAxisOption: Option[ConceptRelationshipNodeFormulaAxis] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationshipNodeFormulaAxis]).headOption
  }

  def formulaAxisExpressionOption: Option[ConceptRelationshipNodeFormulaAxisExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ConceptRelationshipNodeFormulaAxisExpression]).headOption
  }

  def generationsOption: Option[Generations] = {
    findAllNonXLinkChildElemsOfType(classTag[Generations]).headOption
  }

  def generationsExpressionOption: Option[GenerationsExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[GenerationsExpression]).headOption
  }

  def linknameOption: Option[Linkname] = {
    findAllNonXLinkChildElemsOfType(classTag[Linkname]).headOption
  }

  def linknameExpressionOption: Option[LinknameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[LinknameExpression]).headOption
  }

  def arcnameOption: Option[Arcname] = {
    findAllNonXLinkChildElemsOfType(classTag[Arcname]).headOption
  }

  def arcnameExpressionOption: Option[ArcnameExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[ArcnameExpression]).headOption
  }
}

/**
 * A table:dimensionRelationshipNode.
 */
final class DimensionRelationshipNode(underlyingResource: tqa.dom.NonStandardResource) extends RelationshipNode(underlyingResource) {
  requireResolvedName(ENames.TableDimensionRelationshipNodeEName)

  /**
   * Returns the single table:dimension child element. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dimension: TableDimension = {
    findAllNonXLinkChildElemsOfType(classTag[TableDimension]).head
  }

  def relationshipSources: immutable.IndexedSeq[RelationshipSource] = {
    findAllNonXLinkChildElemsOfType(classTag[RelationshipSource])
  }

  def relationshipSourceExpressions: immutable.IndexedSeq[RelationshipSourceExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[RelationshipSourceExpression])
  }

  def linkroleOption: Option[Linkrole] = {
    findAllNonXLinkChildElemsOfType(classTag[Linkrole]).headOption
  }

  def linkroleExpressionOption: Option[LinkroleExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[LinkroleExpression]).headOption
  }

  def formulaAxisOption: Option[DimensionRelationshipNodeFormulaAxis] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionRelationshipNodeFormulaAxis]).headOption
  }

  def formulaAxisExpressionOption: Option[DimensionRelationshipNodeFormulaAxisExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[DimensionRelationshipNodeFormulaAxisExpression]).headOption
  }

  def generationsOption: Option[Generations] = {
    findAllNonXLinkChildElemsOfType(classTag[Generations]).headOption
  }

  def generationsExpressionOption: Option[GenerationsExpression] = {
    findAllNonXLinkChildElemsOfType(classTag[GenerationsExpression]).headOption
  }

  /**
   * Returns the dimension as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dimensionName: EName = dimension.dimension
}

/**
 * A table:aspectNode.
 */
final class AspectNode(underlyingResource: tqa.dom.NonStandardResource) extends OpenDefinitionNode(underlyingResource) {
  requireResolvedName(ENames.TableAspectNodeEName)

  final def aspectSpec: AspectSpec = {
    findAllNonXLinkChildElemsOfType(classTag[AspectSpec]).head
  }
}

// Companion objects

object TableResource {

  /**
   * Lenient method to optionally create a TableResource from an underlying tqa.dom.StandardResource.
   */
  def opt(underlyingResource: tqa.dom.NonStandardResource): Option[TableResource] = {
    if (underlyingResource.resolvedName.namespaceUriOption.contains(Namespaces.TableNamespace)) {
      underlyingResource.resolvedName match {
        case ENames.TableTableEName => Some(new Table(underlyingResource))
        case ENames.TableBreakdownEName => Some(new TableBreakdown(underlyingResource))
        case ENames.TableRuleNodeEName => Some(new RuleNode(underlyingResource))
        case ENames.TableConceptRelationshipNodeEName => Some(new ConceptRelationshipNode(underlyingResource))
        case ENames.TableDimensionRelationshipNodeEName => Some(new DimensionRelationshipNode(underlyingResource))
        case ENames.TableAspectNodeEName => Some(new AspectNode(underlyingResource))
        case _ => None
      }
    } else {
      None
    }
  }
}
