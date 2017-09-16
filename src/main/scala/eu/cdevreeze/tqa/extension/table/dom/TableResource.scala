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
import eu.cdevreeze.tqa.extension.formula.dom.FormulaAspect
import eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaElem
import eu.cdevreeze.tqa.extension.table.common.ParentChildOrder
import eu.cdevreeze.tqa.xlink.XLinkResource
import eu.cdevreeze.tqa.BigDecimalExpr
import eu.cdevreeze.tqa.BigDecimalValue
import eu.cdevreeze.tqa.BigDecimalValueOrExpr
import eu.cdevreeze.tqa.ENameExpr
import eu.cdevreeze.tqa.ENameValue
import eu.cdevreeze.tqa.ENameValueOrExpr
import eu.cdevreeze.tqa.StringExpr
import eu.cdevreeze.tqa.StringValue
import eu.cdevreeze.tqa.StringValueOrExpr
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

  protected[dom] def filterNonXLinkChildElemsOfTableElemType[A <: OtherTableElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingResource.findAllChildElemsOfType(classTag[tqa.dom.OtherNonXLinkElem]).
      flatMap(e => OtherTableElem.opt(e)) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfTableElemType[A <: OtherTableElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfTableElemType(cls)(_ => true)
  }

  protected[dom] def filterNonXLinkChildElemsOfFormulaElemType[A <: OtherFormulaElem](
    cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {

    implicit val clsTag = cls

    underlyingResource.findAllChildElemsOfType(classTag[tqa.dom.OtherNonXLinkElem]).
      flatMap(e => OtherFormulaElem.opt(e)) collect { case e: A if p(e) => e }
  }

  protected[dom] def findAllNonXLinkChildElemsOfFormulaElemType[A <: OtherFormulaElem](
    cls: ClassTag[A]): immutable.IndexedSeq[A] = {

    filterNonXLinkChildElemsOfFormulaElemType(cls)(_ => true)
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
    findAllNonXLinkChildElemsOfFormulaElemType(classTag[FormulaAspect])
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
    findAllNonXLinkChildElemsOfTableElemType(classTag[RuleSet])
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
    findAllNonXLinkChildElemsOfTableElemType(classTag[RelationshipSource])
  }

  def relationshipSourceExpressions: immutable.IndexedSeq[RelationshipSourceExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[RelationshipSourceExpression])
  }

  /**
   * Returns the sources as collection of ENameValueOrExpr objects. This may fail if this element is not schema-valid.
   */
  def sourceValuesOrExpressions: immutable.IndexedSeq[ENameValueOrExpr] = {
    relationshipSources.map(_.source).map(v => ENameValue(v)) ++
      relationshipSourceExpressions.map(_.expr).map(v => ENameExpr(v))
  }

  def linkroleOption: Option[Linkrole] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Linkrole]).headOption
  }

  def linkroleExpressionOption: Option[LinkroleExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[LinkroleExpression]).headOption
  }

  /**
   * Returns the optional linkrole as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def linkroleValueOrExprOption: Option[StringValueOrExpr] = {
    linkroleOption.map(_.linkrole).map(v => StringValue(v)).orElse(
      linkroleExpressionOption.map(_.expr).map(v => StringExpr(v)))
  }

  def arcroleOption: Option[Arcrole] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Arcrole]).headOption
  }

  def arcroleExpressionOption: Option[ArcroleExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[ArcroleExpression]).headOption
  }

  /**
   * Returns the arcrole as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def arcroleValueOrExpr: StringValueOrExpr = {
    arcroleOption.map(_.arcrole).map(v => StringValue(v)).orElse(
      arcroleExpressionOption.map(_.expr).map(v => StringExpr(v))).get
  }

  def formulaAxisOption: Option[ConceptRelationshipNodeFormulaAxis] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[ConceptRelationshipNodeFormulaAxis]).headOption
  }

  def formulaAxisExpressionOption: Option[ConceptRelationshipNodeFormulaAxisExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[ConceptRelationshipNodeFormulaAxisExpression]).headOption
  }

  /**
   * Returns the optional formulaAxis as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def formulaAxisValueOrExprOption: Option[StringValueOrExpr] = {
    formulaAxisOption.map(_.formulaAxis).map(v => StringValue(v.toString)).orElse(
      formulaAxisExpressionOption.map(_.expr).map(v => StringExpr(v)))
  }

  def generationsOption: Option[Generations] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Generations]).headOption
  }

  def generationsExpressionOption: Option[GenerationsExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[GenerationsExpression]).headOption
  }

  /**
   * Returns the optional generations as BigDecimalValueOrExpr. This may fail if this element is not schema-valid.
   */
  def generationsValueOrExprOption: Option[BigDecimalValueOrExpr] = {
    generationsOption.map(_.generations).map(v => BigDecimalValue(v)).orElse(
      generationsExpressionOption.map(_.expr).map(v => BigDecimalExpr(v)))
  }

  def linknameOption: Option[Linkname] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Linkname]).headOption
  }

  def linknameExpressionOption: Option[LinknameExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[LinknameExpression]).headOption
  }

  /**
   * Returns the optional linkname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def linknameValueOrExprOption: Option[ENameValueOrExpr] = {
    linknameOption.map(_.linkname).map(v => ENameValue(v)).orElse(
      linknameExpressionOption.map(_.expr).map(v => ENameExpr(v)))
  }

  def arcnameOption: Option[Arcname] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Arcname]).headOption
  }

  def arcnameExpressionOption: Option[ArcnameExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[ArcnameExpression]).headOption
  }

  /**
   * Returns the optional arcname as ENameValueOrExpr. This may fail if this element is not schema-valid.
   */
  def arcnameValueOrExprOption: Option[ENameValueOrExpr] = {
    arcnameOption.map(_.arcname).map(v => ENameValue(v)).orElse(
      arcnameExpressionOption.map(_.expr).map(v => ENameExpr(v)))
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
    findAllNonXLinkChildElemsOfTableElemType(classTag[TableDimension]).head
  }

  def relationshipSources: immutable.IndexedSeq[RelationshipSource] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[RelationshipSource])
  }

  def relationshipSourceExpressions: immutable.IndexedSeq[RelationshipSourceExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[RelationshipSourceExpression])
  }

  /**
   * Returns the sources as collection of ENameValueOrExpr objects. This may fail if this element is not schema-valid.
   */
  def sourceValuesOrExpressions: immutable.IndexedSeq[ENameValueOrExpr] = {
    relationshipSources.map(_.source).map(v => ENameValue(v)) ++
      relationshipSourceExpressions.map(_.expr).map(v => ENameExpr(v))
  }

  def linkroleOption: Option[Linkrole] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Linkrole]).headOption
  }

  def linkroleExpressionOption: Option[LinkroleExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[LinkroleExpression]).headOption
  }

  /**
   * Returns the optional linkrole as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def linkroleValueOrExprOption: Option[StringValueOrExpr] = {
    linkroleOption.map(_.linkrole).map(v => StringValue(v)).orElse(
      linkroleExpressionOption.map(_.expr).map(v => StringExpr(v)))
  }

  def formulaAxisOption: Option[DimensionRelationshipNodeFormulaAxis] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[DimensionRelationshipNodeFormulaAxis]).headOption
  }

  def formulaAxisExpressionOption: Option[DimensionRelationshipNodeFormulaAxisExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[DimensionRelationshipNodeFormulaAxisExpression]).headOption
  }

  /**
   * Returns the optional formulaAxis as StringValueOrExpr. This may fail if this element is not schema-valid.
   */
  def formulaAxisValueOrExprOption: Option[StringValueOrExpr] = {
    formulaAxisOption.map(_.formulaAxis).map(v => StringValue(v.toString)).orElse(
      formulaAxisExpressionOption.map(_.expr).map(v => StringExpr(v)))
  }

  def generationsOption: Option[Generations] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[Generations]).headOption
  }

  def generationsExpressionOption: Option[GenerationsExpression] = {
    findAllNonXLinkChildElemsOfTableElemType(classTag[GenerationsExpression]).headOption
  }

  /**
   * Returns the optional generations as BigDecimalValueOrExpr. This may fail if this element is not schema-valid.
   */
  def generationsValueOrExprOption: Option[BigDecimalValueOrExpr] = {
    generationsOption.map(_.generations).map(v => BigDecimalValue(v)).orElse(
      generationsExpressionOption.map(_.expr).map(v => BigDecimalExpr(v)))
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
    findAllNonXLinkChildElemsOfTableElemType(classTag[AspectSpec]).head
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
