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

package eu.cdevreeze.tqa.xpath.ast

import scala.collection.immutable
import scala.reflect.ClassTag

import eu.cdevreeze.yaidom.core.QName

/**
 * XPath 3.0 AST.
 *
 * The purpose of this AST is as follows:
 * <ul>
 * <li>It must represent the syntax tree of a successfully parsed XPath expression</li>
 * <li>It is not annotated with more semantic information, like type information that is not included in the XPath expression</li>
 * <li>It does not know anything about the context in which it runs, like bound namespaces etc.</li>
 * <li>It is rich enough to be able to serialize the AST back to XPath, knowing exactly where to place parentheses, braces, etc.</li>
 * <li>It is rich enough to contain operator precedence in the AST itself</li>
 * <li>Serialization of the AST to XPath may lead to differences in whitespace (and operator aliases), but other than that the result must be the same</li>
 * <li>The AST class hierarchy does not have to use the exact same names as the XPath grammar</li>
 * </ul>
 *
 * Having such an AST of a successfully parsed XPath expression, it must be easy to reliably find used namespace prefixes, for example.
 *
 * TODO Improve several class names.
 *
 * @author Chris de Vreeze
 */
object XPathExpressions {

  val anyElem: XPathElem => Boolean = { _ => true }

  /**
   * Any XPath language element
   */
  sealed trait XPathElem {

    def children: immutable.IndexedSeq[XPathElem]

    final def findTopmostElems(p: XPathElem => Boolean): immutable.IndexedSeq[XPathElem] = {
      children.flatMap(_.findTopmostElemsOrSelf(p))
    }

    final def findAllTopmostElemsOfType[A <: XPathElem](cls: ClassTag[A]): immutable.IndexedSeq[A] = {
      findTopmostElemsOfType(cls)(anyElem)
    }

    final def findTopmostElemsOfType[A <: XPathElem](cls: ClassTag[A])(p: A => Boolean): immutable.IndexedSeq[A] = {
      implicit val tag = cls

      findTopmostElems {
        case e: A if p(e) => true
        case e            => false
      } collect {
        case e: A => e
      }
    }

    final def findElem(p: XPathElem => Boolean): Option[XPathElem] = {
      // Not very efficient

      findTopmostElems(p).headOption
    }

    final def findElemOfType[A <: XPathElem](cls: ClassTag[A])(p: A => Boolean): Option[A] = {
      implicit val tag = cls

      findElem {
        case e: A if p(e) => true
        case e            => false
      } collectFirst {
        case e: A => e
      }
    }

    private def findTopmostElemsOrSelf(p: XPathElem => Boolean): immutable.IndexedSeq[XPathElem] = {
      if (p(this)) {
        immutable.IndexedSeq(this)
      } else {
        // Recursive calls
        children.flatMap(_.findTopmostElemsOrSelf(p))
      }
    }
  }

  sealed trait LeafElem extends XPathElem {

    final def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq()
  }

  // TODO Model and use EQName

  final case class XPathExpr(expr: Expr) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  // Enclosed expressions

  final case class EnclosedExpr(expr: Expr) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  // Expressions

  final case class Expr(exprSingleSeq: immutable.IndexedSeq[ExprSingle]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = exprSingleSeq
  }

  sealed trait ExprSingle extends XPathElem

  final case class ForExpr(
      simpleForBindings: immutable.IndexedSeq[SimpleForBinding],
      returnExpr: ExprSingle) extends ExprSingle {

    def children: immutable.IndexedSeq[XPathElem] = simpleForBindings :+ returnExpr
  }

  final case class LetExpr(
      simpleLetBindings: immutable.IndexedSeq[SimpleLetBinding],
      returnExpr: ExprSingle) extends ExprSingle {

    def children: immutable.IndexedSeq[XPathElem] = simpleLetBindings :+ returnExpr
  }

  final case class QuantifiedExpr(
      quantifier: Quantifier,
      simpleBindings: immutable.IndexedSeq[SimpleBindingInQuantifiedExpr],
      satisfiesExpr: ExprSingle) extends ExprSingle {

    def children: immutable.IndexedSeq[XPathElem] = (quantifier +: simpleBindings) :+ satisfiesExpr
  }

  final case class IfExpr(
      condition: Expr,
      thenExpr: ExprSingle,
      elseExpr: ExprSingle) extends ExprSingle {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(condition, thenExpr, elseExpr)
  }

  final case class OrExpr(andExprs: immutable.IndexedSeq[AndExpr]) extends ExprSingle {

    def children: immutable.IndexedSeq[XPathElem] = andExprs
  }

  final case class AndExpr(comparisonExprs: immutable.IndexedSeq[ComparisonExpr]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = comparisonExprs
  }

  sealed trait ComparisonExpr extends XPathElem

  final case class SimpleComparisonExpr(stringConcatExpr: StringConcatExpr) extends ComparisonExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(stringConcatExpr)
  }

  final case class CompoundComparisonExpr(stringConcatExpr1: StringConcatExpr, comp: Comp, stringConcatExpr2: StringConcatExpr) extends ComparisonExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(stringConcatExpr1, comp, stringConcatExpr2)
  }

  final case class StringConcatExpr(rangeExprs: immutable.IndexedSeq[RangeExpr]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = rangeExprs
  }

  sealed trait RangeExpr extends XPathElem

  final case class SimpleRangeExpr(additiveExpr: AdditiveExpr) extends RangeExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(additiveExpr)
  }

  final case class CompoundRangeExpr(additiveExpr1: AdditiveExpr, additiveExpr2: AdditiveExpr) extends RangeExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(additiveExpr1, additiveExpr2)
  }

  sealed trait AdditiveExpr extends XPathElem

  final case class SimpleAdditiveExpr(expr: MultiplicativeExpr) extends AdditiveExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class CompoundAdditiveExpr(headExpr: MultiplicativeExpr, op: AdditionOp, tailExpr: AdditiveExpr) extends AdditiveExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(headExpr, op, tailExpr)
  }

  sealed trait MultiplicativeExpr extends XPathElem

  final case class SimpleMultiplicativeExpr(expr: UnionExpr) extends MultiplicativeExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class CompoundMultiplicativeExpr(headExpr: UnionExpr, op: MultiplicativeOp, tailExpr: MultiplicativeExpr) extends MultiplicativeExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(headExpr, op, tailExpr)
  }

  final case class UnionExpr(intersectExceptExprs: immutable.IndexedSeq[IntersectExceptExpr]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = intersectExceptExprs
  }

  sealed trait IntersectExceptExpr extends XPathElem

  final case class SimpleIntersectExceptExpr(expr: InstanceOfExpr) extends IntersectExceptExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class CompoundIntersectExceptExpr(headExpr: InstanceOfExpr, op: IntersectExceptOp, tailExpr: IntersectExceptExpr) extends IntersectExceptExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(headExpr, op, tailExpr)
  }

  final case class InstanceOfExpr(treatExpr: TreatExpr, sequenceTypeOption: Option[SequenceType]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = treatExpr +: sequenceTypeOption.toIndexedSeq
  }

  final case class TreatExpr(castableExpr: CastableExpr, sequenceTypeOption: Option[SequenceType]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = castableExpr +: sequenceTypeOption.toIndexedSeq
  }

  final case class CastableExpr(castExpr: CastExpr, singleTypeOption: Option[SingleType]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = castExpr +: singleTypeOption.toIndexedSeq
  }

  final case class CastExpr(unaryExpr: UnaryExpr, singleTypeOption: Option[SingleType]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = unaryExpr +: singleTypeOption.toIndexedSeq
  }

  final case class UnaryExpr(ops: immutable.IndexedSeq[UnaryOp], valueExpr: ValueExpr) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = ops :+ valueExpr
  }

  final case class ValueExpr(expr: SimpleMapExpr) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class SimpleMapExpr(pathExprs: immutable.IndexedSeq[PathExpr]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = pathExprs
  }

  // Path and step expressions

  sealed trait PathExpr extends XPathElem

  case object SlashOnlyPathExpr extends PathExpr with LeafElem

  final case class PathExprStartingWithSingleSlash(relativePathExpr: RelativePathExpr) extends PathExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(relativePathExpr)
  }

  final case class PathExprStartingWithDoubleSlash(relativePathExpr: RelativePathExpr) extends PathExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(relativePathExpr)
  }

  sealed trait RelativePathExpr extends PathExpr

  final case class SimpleRelativePathExpr(stepExpr: StepExpr) extends RelativePathExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(stepExpr)
  }

  final case class CompoundRelativePathExpr(headExpr: StepExpr, op: StepOp, tailExpr: RelativePathExpr) extends RelativePathExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(headExpr, op, tailExpr)
  }

  sealed trait StepExpr extends XPathElem

  final case class PostfixExpr(
      primaryExpr: PrimaryExpr,
      predicatesAndArgumentLists: immutable.IndexedSeq[PredicateOrArgumentList]) extends StepExpr {

    def children: immutable.IndexedSeq[XPathElem] = primaryExpr +: predicatesAndArgumentLists
  }

  sealed trait AxisStep extends StepExpr {

    def predicateList: immutable.IndexedSeq[Predicate]
  }

  final case class ForwardAxisStep(step: ForwardStep, predicateList: immutable.IndexedSeq[Predicate]) extends AxisStep {

    def children: immutable.IndexedSeq[XPathElem] = step +: predicateList
  }

  final case class ReverseAxisStep(step: ReverseStep, predicateList: immutable.IndexedSeq[Predicate]) extends AxisStep {

    def children: immutable.IndexedSeq[XPathElem] = step +: predicateList
  }

  sealed trait ForwardStep extends XPathElem {

    def nodeTest: NodeTest
  }

  final case class NonAbbrevForwardStep(forwardAxis: ForwardAxis, nodeTest: NodeTest) extends ForwardStep {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(forwardAxis, nodeTest)
  }

  sealed trait AbbrevForwardStep extends ForwardStep

  final case class SimpleAbbrevForwardStep(nodeTest: NodeTest) extends AbbrevForwardStep {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(nodeTest)
  }

  final case class AttributeAxisAbbrevForwardStep(nodeTest: NodeTest) extends AbbrevForwardStep {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(nodeTest)
  }

  sealed trait ReverseStep extends XPathElem

  final case class NonAbbrevReverseStep(reverseAxis: ReverseAxis, nodeTest: NodeTest) extends ReverseStep {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(reverseAxis, nodeTest)
  }

  case object AbbrevReverseStep extends ReverseStep {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq()
  }

  sealed trait NodeTest extends XPathElem

  sealed trait KindTest extends NodeTest

  sealed trait NameTest extends NodeTest

  final case class SimpleNameTest(name: QName) extends NameTest with LeafElem

  sealed trait Wildcard extends NameTest

  case object AnyWildcard extends Wildcard with LeafElem

  final case class PrefixWildcard(prefix: String) extends Wildcard with LeafElem

  final case class LocalNameWildcard(localName: String) extends Wildcard with LeafElem

  final case class NamespaceWildcard(namespace: String) extends Wildcard with LeafElem

  sealed trait DocumentTest extends KindTest

  case object SimpleDocumentTest extends DocumentTest with LeafElem

  final case class DocumentTestContainingElementTest(elementTest: ElementTest) extends DocumentTest {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(elementTest)
  }

  final case class DocumentTestContainingSchemaElementTest(schemaElementTest: SchemaElementTest) extends DocumentTest {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(schemaElementTest)
  }

  sealed trait ElementTest extends KindTest

  case object AnyElementTest extends ElementTest with LeafElem

  final case class ElementNameTest(name: QName) extends ElementTest with LeafElem

  final case class ElementNameAndTypeTest(name: QName, tpe: QName) extends ElementTest with LeafElem

  final case class NillableElementNameAndTypeTest(name: QName, tpe: QName) extends ElementTest with LeafElem

  final case class ElementTypeTest(tpe: QName) extends ElementTest with LeafElem

  final case class NillableElementTypeTest(tpe: QName) extends ElementTest with LeafElem

  sealed trait AttributeTest extends KindTest

  case object AnyAttributeTest extends AttributeTest with LeafElem

  final case class AttributeNameTest(name: QName) extends AttributeTest with LeafElem

  final case class AttributeNameAndTypeTest(name: QName, tpe: QName) extends AttributeTest with LeafElem

  final case class AttributeTypeTest(tpe: QName) extends AttributeTest with LeafElem

  final case class SchemaElementTest(name: QName) extends KindTest with LeafElem

  final case class SchemaAttributeTest(name: QName) extends KindTest with LeafElem

  sealed trait PITest extends KindTest

  case object SimplePITest extends PITest with LeafElem

  // TODO Is this correct?
  final case class TargetPITest(target: String) extends PITest with LeafElem

  // TODO Is this correct?
  final case class DataPITest(data: StringLiteral) extends PITest {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(data)
  }

  case object CommentTest extends KindTest with LeafElem

  case object TextTest extends KindTest with LeafElem

  case object NamespaceNodeTest extends KindTest with LeafElem

  case object AnyKindTest extends KindTest with LeafElem

  // Primary expressions

  sealed trait PrimaryExpr extends XPathElem

  sealed trait Literal extends PrimaryExpr

  final case class StringLiteral(value: String) extends Literal with LeafElem

  sealed trait NumericLiteral extends Literal

  final case class IntegerLiteral(value: Int) extends NumericLiteral with LeafElem

  final case class DecimalLiteral(value: BigDecimal) extends NumericLiteral with LeafElem

  final case class DoubleLiteral(value: Double) extends NumericLiteral with LeafElem

  final case class VarRef(varName: QName) extends PrimaryExpr with LeafElem

  final case class ParenthesizedExpr(exprOption: Option[Expr]) extends PrimaryExpr {

    def children: immutable.IndexedSeq[XPathElem] = exprOption.toIndexedSeq
  }

  case object ContextItemExpr extends PrimaryExpr with LeafElem

  final case class FunctionCall(functionName: QName, argumentList: ArgumentList) extends PrimaryExpr {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(argumentList)
  }

  sealed trait FunctionItemExpr extends PrimaryExpr

  final case class NamedFunctionRef(functionName: QName, arity: Int) extends FunctionItemExpr with LeafElem

  final case class InlineFunctionExpr(
      paramListOption: Option[ParamList],
      resultTypeOption: Option[SequenceType],
      body: EnclosedExpr) extends FunctionItemExpr {

    def children: immutable.IndexedSeq[XPathElem] = paramListOption.toIndexedSeq ++ resultTypeOption.toIndexedSeq :+ body
  }

  sealed trait PredicateOrArgumentList extends XPathElem

  final case class Predicate(expr: Expr) extends PredicateOrArgumentList {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class ArgumentList(arguments: immutable.IndexedSeq[Argument]) extends PredicateOrArgumentList {

    def children: immutable.IndexedSeq[XPathElem] = arguments
  }

  final case class ParamList(params: immutable.IndexedSeq[Param]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = params
  }

  final case class Param(paramName: QName, typeDeclarationOption: Option[TypeDeclaration]) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = typeDeclarationOption.toIndexedSeq
  }

  sealed trait Argument extends XPathElem

  final case class ExprSingleArgument(exprSingle: ExprSingle) extends Argument {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(exprSingle)
  }

  case object ArgumentPlaceholder extends Argument with LeafElem

  // Bindings

  final case class SimpleForBinding(varName: QName, expr: ExprSingle) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class SimpleLetBinding(varName: QName, expr: ExprSingle) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  final case class SimpleBindingInQuantifiedExpr(varName: QName, expr: ExprSingle) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(expr)
  }

  // Types

  sealed trait SequenceType extends XPathElem

  case object EmptySequenceType extends SequenceType with LeafElem

  final case class ExactlyOneSequenceType(itemType: ItemType) extends SequenceType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(itemType)
  }

  final case class ZeroOrOneSequenceType(itemType: ItemType) extends SequenceType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(itemType)
  }

  final case class ZeroOrMoreSequenceType(itemType: ItemType) extends SequenceType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(itemType)
  }

  final case class OneOrMoreSequenceType(itemType: ItemType) extends SequenceType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(itemType)
  }

  sealed trait SingleType extends XPathElem

  final case class NonEmptySingleType(name: QName) extends SingleType with LeafElem

  final case class PotentiallyEmptySingleType(name: QName) extends SingleType with LeafElem

  sealed trait ItemType extends XPathElem

  final case class KindTestItemType(kindTest: KindTest) extends ItemType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(kindTest)
  }

  case object AnyItemType extends ItemType with LeafElem

  sealed trait FunctionTest extends ItemType

  case object AnyFunctionTest extends FunctionTest with LeafElem

  final case class TypedFunctionTest(argumentTypes: immutable.IndexedSeq[SequenceType], resultType: SequenceType) extends FunctionTest {

    def children: immutable.IndexedSeq[XPathElem] = argumentTypes :+ resultType
  }

  final case class AtomicOrUnionType(tpe: QName) extends ItemType with LeafElem

  final case class ParenthesizedItemType(itemType: ItemType) extends ItemType {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(itemType)
  }

  final case class TypeDeclaration(tpe: SequenceType) extends XPathElem {

    def children: immutable.IndexedSeq[XPathElem] = immutable.IndexedSeq(tpe)
  }

  // Axes

  sealed trait ForwardAxis extends XPathElem with LeafElem

  object ForwardAxis {

    case object Child extends ForwardAxis { override def toString: String = "child" }
    case object Descendant extends ForwardAxis { override def toString: String = "descendant" }
    case object Attribute extends ForwardAxis { override def toString: String = "attribute" }
    case object Self extends ForwardAxis { override def toString: String = "self" }
    case object DescendantOrSelf extends ForwardAxis { override def toString: String = "descendant-or-self" }
    case object FollowingSibling extends ForwardAxis { override def toString: String = "following-sibling" }
    case object Following extends ForwardAxis { override def toString: String = "following" }
    case object Namespace extends ForwardAxis { override def toString: String = "namespace" }

    def parse(s: String): ForwardAxis = s match {
      case "child"              => Child
      case "descendant"         => Descendant
      case "attribute"          => Attribute
      case "self"               => Self
      case "descendant-or-self" => DescendantOrSelf
      case "following-sibling"  => FollowingSibling
      case "following"          => Following
      case "namespace"          => Namespace
    }
  }

  sealed trait ReverseAxis extends XPathElem with LeafElem

  object ReverseAxis {

    case object Parent extends ReverseAxis { override def toString: String = "parent" }
    case object Ancestor extends ReverseAxis { override def toString: String = "ancestor" }
    case object PrecedingSibling extends ReverseAxis { override def toString: String = "preceding-sibling" }
    case object Preceding extends ReverseAxis { override def toString: String = "preceding" }
    case object AncestorOrSelf extends ReverseAxis { override def toString: String = "ancestor-or-self" }

    def parse(s: String): ReverseAxis = s match {
      case "parent"            => Parent
      case "ancestor"          => Ancestor
      case "preceding-sibling" => PrecedingSibling
      case "preceding"         => Preceding
      case "ancestor-or-self"  => AncestorOrSelf
    }
  }

  // Operators

  sealed trait Comp extends XPathElem with LeafElem

  sealed trait ValueComp extends Comp
  sealed trait GeneralComp extends Comp
  sealed trait NodeComp extends Comp

  object ValueComp {

    case object Eq extends ValueComp { override def toString: String = "eq" }
    case object Ne extends ValueComp { override def toString: String = "ne" }
    case object Lt extends ValueComp { override def toString: String = "lt" }
    case object Le extends ValueComp { override def toString: String = "le" }
    case object Gt extends ValueComp { override def toString: String = "gt" }
    case object Ge extends ValueComp { override def toString: String = "ge" }

    def parse(s: String): ValueComp = s match {
      case "eq" => Eq
      case "ne" => Ne
      case "lt" => Lt
      case "le" => Le
      case "gt" => Gt
      case "ge" => Ge
    }
  }

  object GeneralComp {

    case object Eq extends GeneralComp { override def toString: String = "=" }
    case object Ne extends GeneralComp { override def toString: String = "!=" }
    case object Lt extends GeneralComp { override def toString: String = "<" }
    case object Le extends GeneralComp { override def toString: String = "<=" }
    case object Gt extends GeneralComp { override def toString: String = ">" }
    case object Ge extends GeneralComp { override def toString: String = ">=" }

    def parse(s: String): GeneralComp = s match {
      case "="  => Eq
      case "!=" => Ne
      case "<"  => Lt
      case "<=" => Le
      case ">"  => Gt
      case ">=" => Ge
    }
  }

  object NodeComp {

    case object Is extends NodeComp { override def toString: String = "is" }
    case object Precedes extends NodeComp { override def toString: String = "<<" }
    case object Follows extends NodeComp { override def toString: String = ">>" }

    def parse(s: String): NodeComp = s match {
      case "is" => Is
      case "<<" => Precedes
      case ">>" => Follows
    }
  }

  sealed trait AdditionOp extends XPathElem with LeafElem

  object AdditionOp {

    case object Plus extends AdditionOp { override def toString: String = "+" }
    case object Minus extends AdditionOp { override def toString: String = "-" }

    def parse(s: String): AdditionOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  sealed trait MultiplicativeOp extends XPathElem with LeafElem

  object MultiplicativeOp {

    case object Times extends MultiplicativeOp { override def toString: String = "*" }
    case object Div extends MultiplicativeOp { override def toString: String = "div" }
    case object IDiv extends MultiplicativeOp { override def toString: String = "idiv" }
    case object Mod extends MultiplicativeOp { override def toString: String = "mod" }

    def parse(s: String): MultiplicativeOp = s match {
      case "*"    => Times
      case "div"  => Div
      case "idiv" => IDiv
      case "mod"  => Mod
    }
  }

  sealed trait IntersectExceptOp extends XPathElem with LeafElem

  object IntersectExceptOp {

    case object Intersect extends IntersectExceptOp { override def toString: String = "intersect" }
    case object Except extends IntersectExceptOp { override def toString: String = "except" }

    def parse(s: String): IntersectExceptOp = s match {
      case "intersect" => Intersect
      case "except"    => Except
    }
  }

  sealed trait UnaryOp extends XPathElem with LeafElem

  object UnaryOp {

    case object Plus extends UnaryOp { override def toString: String = "+" }
    case object Minus extends UnaryOp { override def toString: String = "-" }

    def parse(s: String): UnaryOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  sealed trait StepOp extends XPathElem with LeafElem

  object StepOp {

    case object SingleSlash extends StepOp { override def toString: String = "/" }
    case object DoubleSlash extends StepOp { override def toString: String = "//" }

    def parse(s: String): StepOp = s match {
      case "/"  => SingleSlash
      case "//" => DoubleSlash
    }
  }

  // Keywords etc.

  sealed trait Quantifier extends XPathElem with LeafElem

  object Quantifier {

    case object SomeQuantifier extends Quantifier { override def toString: String = "some" }
    case object EveryQuantifier extends Quantifier { override def toString: String = "every" }

    def parse(s: String): Quantifier = s match {
      case "some"  => SomeQuantifier
      case "every" => EveryQuantifier
    }
  }
}
