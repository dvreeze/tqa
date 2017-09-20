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

  // TODO Model and use EQName

  final case class XPathExpr(expr: Expr)

  // Enclosed expressions

  final case class EnclosedExpr(expr: Expr)

  // Expressions

  final case class Expr(exprSingleSeq: immutable.IndexedSeq[ExprSingle])

  sealed trait ExprSingle

  final case class ForExpr(
    simpleForBindings: immutable.IndexedSeq[SimpleForBinding],
    returnExpr: ExprSingle) extends ExprSingle

  final case class LetExpr(
    simpleLetBindings: immutable.IndexedSeq[SimpleLetBinding],
    returnExpr: ExprSingle) extends ExprSingle

  final case class QuantifiedExpr(
    quantifier: Quantifier,
    simpleBindings: immutable.IndexedSeq[SimpleBindingInQuantifiedExpr],
    satisfiesExpr: ExprSingle) extends ExprSingle

  final case class IfExpr(
    condition: Expr,
    thenExpr: ExprSingle,
    elseExpr: ExprSingle) extends ExprSingle

  final case class OrExpr(andExprs: immutable.IndexedSeq[AndExpr]) extends ExprSingle

  final case class AndExpr(comparisonExprs: immutable.IndexedSeq[ComparisonExpr])

  sealed trait ComparisonExpr

  final case class SimpleComparisonExpr(stringConcatExpr: StringConcatExpr) extends ComparisonExpr

  final case class CompoundComparisonExpr(stringConcatExpr1: StringConcatExpr, comp: Comp, stringConcatExpr2: StringConcatExpr) extends ComparisonExpr

  final case class StringConcatExpr(rangeExprs: immutable.IndexedSeq[RangeExpr])

  sealed trait RangeExpr

  final case class SimpleRangeExpr(additiveExpr: AdditiveExpr) extends RangeExpr

  final case class CompoundRangeExpr(additiveExpr1: AdditiveExpr, additiveExpr2: AdditiveExpr) extends RangeExpr

  sealed trait AdditiveExpr

  final case class SimpleAdditiveExpr(expr: MultiplicativeExpr) extends AdditiveExpr

  final case class CompoundAdditiveExpr(headExpr: MultiplicativeExpr, op: AdditionOp, tailExpr: AdditiveExpr) extends AdditiveExpr

  sealed trait MultiplicativeExpr

  final case class SimpleMultiplicativeExpr(expr: UnionExpr) extends MultiplicativeExpr

  final case class CompoundMultiplicativeExpr(headExpr: UnionExpr, op: MultiplicativeOp, tailExpr: MultiplicativeExpr) extends MultiplicativeExpr

  final case class UnionExpr(intersectExceptExprs: immutable.IndexedSeq[IntersectExceptExpr])

  sealed trait IntersectExceptExpr

  final case class SimpleIntersectExceptExpr(expr: InstanceOfExpr) extends IntersectExceptExpr

  final case class CompoundIntersectExceptExpr(headExpr: InstanceOfExpr, op: IntersectExceptOp, tailExpr: IntersectExceptExpr) extends IntersectExceptExpr

  final case class InstanceOfExpr(treatExpr: TreatExpr, sequenceTypeOption: Option[SequenceType])

  final case class TreatExpr(castableExpr: CastableExpr, sequenceTypeOption: Option[SequenceType])

  final case class CastableExpr(castExpr: CastExpr, singleTypeOption: Option[SingleType])

  final case class CastExpr(unaryExpr: UnaryExpr, singleTypeOption: Option[SingleType])

  final case class UnaryExpr(ops: immutable.IndexedSeq[UnaryOp], valueExpr: ValueExpr)

  final case class ValueExpr(expr: SimpleMapExpr)

  final case class SimpleMapExpr(pathExprs: immutable.IndexedSeq[PathExpr])

  // Path and step expressions

  sealed trait PathExpr

  case object SlashOnlyPathExpr extends PathExpr

  final case class PathExprStartingWithSingleSlash(relativePathExpr: RelativePathExpr) extends PathExpr

  final case class PathExprStartingWithDoubleSlash(relativePathExpr: RelativePathExpr) extends PathExpr

  sealed trait RelativePathExpr extends PathExpr

  final case class SimpleRelativePathExpr(stepExpr: StepExpr) extends RelativePathExpr

  final case class CompoundRelativePathExpr(headExpr: StepExpr, op: StepOp, tailExpr: RelativePathExpr) extends RelativePathExpr

  sealed trait StepExpr

  final case class PostfixExpr(
    primaryExpr: PrimaryExpr,
    predicatesAndArgumentLists: immutable.IndexedSeq[PredicateOrArgumentList]) extends StepExpr

  sealed trait AxisStep extends StepExpr {

    def predicateList: immutable.IndexedSeq[Predicate]
  }

  final case class ForwardAxisStep(step: ForwardStep, predicateList: immutable.IndexedSeq[Predicate]) extends AxisStep

  final case class ReverseAxisStep(step: ReverseStep, predicateList: immutable.IndexedSeq[Predicate]) extends AxisStep

  sealed trait ForwardStep {

    def nodeTest: NodeTest
  }

  final case class NonAbbrevForwardStep(forwardAxis: ForwardAxis, nodeTest: NodeTest) extends ForwardStep

  sealed trait AbbrevForwardStep extends ForwardStep

  final case class SimpleAbbrevForwardStep(nodeTest: NodeTest) extends AbbrevForwardStep

  final case class AttributeAxisAbbrevForwardStep(nodeTest: NodeTest) extends AbbrevForwardStep

  sealed trait ReverseStep

  final case class NonAbbrevReverseStep(reverseAxis: ReverseAxis, nodeTest: NodeTest) extends ReverseStep

  case object AbbrevReverseStep extends ReverseStep

  sealed trait NodeTest

  sealed trait KindTest extends NodeTest

  sealed trait NameTest extends NodeTest

  final case class SimpleNameTest(name: QName) extends NameTest

  sealed trait Wildcard extends NameTest

  case object AnyWildcard extends Wildcard

  final case class PrefixWildcard(prefix: String) extends Wildcard

  final case class LocalNameWildcard(localName: String) extends Wildcard

  final case class NamespaceWildcard(namespace: String) extends Wildcard

  sealed trait DocumentTest extends KindTest

  case object SimpleDocumentTest extends DocumentTest

  final case class DocumentTestContainingElementTest(elementTest: ElementTest) extends DocumentTest

  final case class DocumentTestContainingSchemaElementTest(schemaElementTest: SchemaElementTest) extends DocumentTest

  sealed trait ElementTest extends KindTest

  case object AnyElementTest extends ElementTest

  final case class ElementNameTest(name: QName) extends ElementTest

  final case class ElementNameAndTypeTest(name: QName, tpe: QName) extends ElementTest

  final case class NillableElementNameAndTypeTest(name: QName, tpe: QName) extends ElementTest

  final case class ElementTypeTest(tpe: QName) extends ElementTest

  final case class NillableElementTypeTest(tpe: QName) extends ElementTest

  sealed trait AttributeTest extends KindTest

  case object AnyAttributeTest extends AttributeTest

  final case class AttributeNameTest(name: QName) extends AttributeTest

  final case class AttributeNameAndTypeTest(name: QName, tpe: QName) extends AttributeTest

  final case class AttributeTypeTest(tpe: QName) extends AttributeTest

  final case class SchemaElementTest(name: QName) extends KindTest

  final case class SchemaAttributeTest(name: QName) extends KindTest

  sealed trait PITest extends KindTest

  case object SimplePITest extends PITest

  final case class TargetPITest(target: String) extends PITest // TODO Is this correct?

  final case class DataPITest(data: StringLiteral) extends PITest // TODO Is this correct?

  case object CommentTest extends KindTest

  case object TextTest extends KindTest

  case object NamespaceNodeTest extends KindTest

  case object AnyKindTest extends KindTest

  // Primary expressions

  sealed trait PrimaryExpr

  sealed trait Literal extends PrimaryExpr

  final case class StringLiteral(value: String) extends Literal

  sealed trait NumericLiteral extends Literal

  final case class IntegerLiteral(value: Int) extends NumericLiteral

  final case class DecimalLiteral(value: BigDecimal) extends NumericLiteral

  final case class DoubleLiteral(value: Double) extends NumericLiteral

  final case class VarRef(varName: QName) extends PrimaryExpr

  final case class ParenthesizedExpr(exprOption: Option[Expr]) extends PrimaryExpr

  case object ContextItemExpr extends PrimaryExpr

  final case class FunctionCall(functionName: QName, argumentList: ArgumentList) extends PrimaryExpr

  sealed trait FunctionItemExpr extends PrimaryExpr

  final case class NamedFunctionRef(functionName: QName, arity: Int) extends FunctionItemExpr

  final case class InlineFunctionExpr(
    paramListOption: Option[ParamList],
    resultTypeOption: Option[SequenceType],
    body: EnclosedExpr) extends FunctionItemExpr

  sealed trait PredicateOrArgumentList

  final case class Predicate(expr: Expr) extends PredicateOrArgumentList

  final case class ArgumentList(arguments: immutable.IndexedSeq[Argument]) extends PredicateOrArgumentList

  final case class ParamList(params: immutable.IndexedSeq[Param])

  final case class Param(paramName: QName, typeDeclarationOption: Option[TypeDeclaration])

  sealed trait Argument

  final case class ExprSingleArgument(exprSingle: ExprSingle) extends Argument

  case object ArgumentPlaceholder extends Argument

  // Bindings

  final case class SimpleForBinding(varName: QName, expr: ExprSingle)

  final case class SimpleLetBinding(varName: QName, expr: ExprSingle)

  final case class SimpleBindingInQuantifiedExpr(varName: QName, expr: ExprSingle)

  // Types

  sealed trait SequenceType

  case object EmptySequenceType extends SequenceType

  final case class ExactlyOneSequenceType(itemType: ItemType) extends SequenceType

  final case class ZeroOrOneSequenceType(itemType: ItemType) extends SequenceType

  final case class ZeroOrMoreSequenceType(itemType: ItemType) extends SequenceType

  final case class OneOrMoreSequenceType(itemType: ItemType) extends SequenceType

  sealed trait SingleType

  final case class NonEmptySingleType(name: QName) extends SingleType

  final case class PotentiallyEmptySingleType(name: QName) extends SingleType

  sealed trait ItemType

  final case class KindTestItemType(kindTest: KindTest) extends ItemType

  case object AnyItemType extends ItemType

  sealed trait FunctionTest extends ItemType

  case object AnyFunctionTest extends FunctionTest

  final case class TypedFunctionTest(argumentTypes: immutable.IndexedSeq[SequenceType], resultType: SequenceType) extends FunctionTest

  final case class AtomicOrUnionType(tpe: QName) extends ItemType

  final case class ParenthesizedItemType(itemType: ItemType) extends ItemType

  final case class TypeDeclaration(tpe: SequenceType)

  // Axes

  sealed trait ForwardAxis

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

  sealed trait ReverseAxis

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

  sealed trait Comp
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

  sealed trait AdditionOp

  object AdditionOp {

    case object Plus extends AdditionOp { override def toString: String = "+" }
    case object Minus extends AdditionOp { override def toString: String = "-" }

    def parse(s: String): AdditionOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  sealed trait MultiplicativeOp

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

  sealed trait IntersectExceptOp

  object IntersectExceptOp {

    case object Intersect extends IntersectExceptOp { override def toString: String = "intersect" }
    case object Except extends IntersectExceptOp { override def toString: String = "except" }

    def parse(s: String): IntersectExceptOp = s match {
      case "intersect" => Intersect
      case "except"    => Except
    }
  }

  sealed trait UnaryOp

  object UnaryOp {

    case object Plus extends UnaryOp { override def toString: String = "+" }
    case object Minus extends UnaryOp { override def toString: String = "-" }

    def parse(s: String): UnaryOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  sealed trait StepOp

  object StepOp {

    case object SingleSlash extends StepOp { override def toString: String = "/" }
    case object DoubleSlash extends StepOp { override def toString: String = "//" }

    def parse(s: String): StepOp = s match {
      case "/"  => SingleSlash
      case "//" => DoubleSlash
    }
  }

  // Keywords etc.

  sealed trait Quantifier

  object Quantifier {

    case object SomeQuantifier extends Quantifier { override def toString: String = "some" }
    case object EveryQuantifier extends Quantifier { override def toString: String = "every" }

    def parse(s: String): Quantifier = s match {
      case "some"  => SomeQuantifier
      case "every" => EveryQuantifier
    }
  }
}
