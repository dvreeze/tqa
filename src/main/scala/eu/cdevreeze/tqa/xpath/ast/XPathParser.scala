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

import eu.cdevreeze.yaidom.core.QName
import fastparse.WhitespaceApi

/**
 * XPath 3.0 parsing support, using FastParse.
 *
 * Usage: XPathParser.xpathExpr.parse(xpathString)
 *
 * @author Chris de Vreeze
 */
object XPathParser {

  import XPathExpressions._

  private val White = WhitespaceApi.Wrapper {
    import fastparse.all._

    NoTrace(" ".rep) // TODO Adapt. Not only spaces are whitespace.
  }

  import White._
  import fastparse.noApi._

  val xpathExpr: P[XPathExpr] =
    P(expr) map (e => XPathExpr(e)) // TODO End

  private val expr: P[Expr] =
    P(exprSingle ~ ("," ~ exprSingle).rep) map {
      case (exprSingle, exprSingleSeq) =>
        Expr(exprSingle +: exprSingleSeq.toIndexedSeq)
    }

  private val exprSingle: P[ExprSingle] =
    P(forExpr | letExpr | quantifiedExpr | ifExpr | orExpr)

  private val forExpr: P[ForExpr] =
    P("for" ~ "$" ~ varName ~ "in" ~ exprSingle ~ ("," ~ "$" ~ varName ~ "in" ~ exprSingle).rep ~ "return" ~ exprSingle) map {
      case (qn, exp1, qnameExpSeq, returnExp) =>
        ForExpr(
          SimpleForBinding(qn, exp1) +: qnameExpSeq.toIndexedSeq.map(qnExpPair => SimpleForBinding(qnExpPair._1, qnExpPair._2)),
          returnExp)
    }

  private val letExpr: P[LetExpr] =
    P("let" ~ "$" ~ varName ~ ":=" ~ exprSingle ~ ("," ~ "$" ~ varName ~ ":=" ~ exprSingle).rep ~ "return" ~ exprSingle) map {
      case (qn, exp1, qnameExpSeq, returnExp) =>
        LetExpr(
          SimpleLetBinding(qn, exp1) +: qnameExpSeq.toIndexedSeq.map(qnExpPair => SimpleLetBinding(qnExpPair._1, qnExpPair._2)),
          returnExp)
    }

  private val quantifiedExpr: P[QuantifiedExpr] =
    P(("some" | "every").! ~ "$" ~ varName ~ "in" ~ exprSingle ~ ("," ~ "$" ~ varName ~ "in" ~ exprSingle).rep ~ "satisfies" ~ exprSingle) map {
      case (quant, qn, exp1, qnameExpSeq, satisfiesExp) =>
        QuantifiedExpr(
          Quantifier.parse(quant),
          SimpleBindingInQuantifiedExpr(qn, exp1) +: qnameExpSeq.toIndexedSeq.map(qnExpPair => SimpleBindingInQuantifiedExpr(qnExpPair._1, qnExpPair._2)),
          satisfiesExp)
    }

  private val ifExpr: P[IfExpr] =
    P("if" ~ "(" ~ expr ~ ")" ~ "then" ~ exprSingle ~ "else" ~ exprSingle) map {
      case (e1, e2, e3) =>
        IfExpr(e1, e2, e3)
    }

  private val orExpr: P[OrExpr] =
    P(andExpr ~ ("or" ~ andExpr).rep) map {
      case (andExp, andExpSeq) =>
        OrExpr(andExp +: andExpSeq.toIndexedSeq)
    }

  private val andExpr: P[AndExpr] =
    P(comparisonExpr ~ ("and" ~ comparisonExpr).rep) map {
      case (compExp, compExpSeq) =>
        AndExpr(compExp +: compExpSeq.toIndexedSeq)
    }

  private val comparisonExpr: P[ComparisonExpr] =
    P(stringConcatExpr ~ ((valueComp | generalComp | nodeComp) ~ stringConcatExpr).?) map {
      case (expr1, Some((op, expr2))) => CompoundComparisonExpr(expr1, op, expr2)
      case (expr, None)               => SimpleComparisonExpr(expr)
    }

  private val stringConcatExpr: P[StringConcatExpr] =
    P(rangeExpr ~ ("||" ~ rangeExpr).rep) map {
      case (rangeExpr, rangeExprSeq) =>
        StringConcatExpr(rangeExpr +: rangeExprSeq.toIndexedSeq)
    }

  private val rangeExpr: P[RangeExpr] =
    P(additiveExpr ~ ("to" ~ additiveExpr).?) map {
      case (additiveExp1, Some(additiveExp2)) => CompoundRangeExpr(additiveExp1, additiveExp2)
      case (additiveExp, None)                => SimpleRangeExpr(additiveExp)
    }

  private val additiveExpr: P[AdditiveExpr] =
    P(multiplicativeExpr ~ (("+" | "-").! ~ additiveExpr).?) map {
      case (expr, None) =>
        SimpleAdditiveExpr(expr)
      case (expr, Some(opAndExpr)) =>
        CompoundAdditiveExpr(expr, AdditionOp.parse(opAndExpr._1), opAndExpr._2)
    }

  private val multiplicativeExpr: P[MultiplicativeExpr] =
    P(unionExpr ~ (("*" | "div" | "idiv" | "mod").! ~ multiplicativeExpr).?) map {
      case (expr, None) =>
        SimpleMultiplicativeExpr(expr)
      case (expr, Some(opAndExpr)) =>
        CompoundMultiplicativeExpr(expr, MultiplicativeOp.parse(opAndExpr._1), opAndExpr._2)
    }

  private val unionExpr: P[UnionExpr] =
    P(intersectExceptExpr ~ (("union" | "|") ~ intersectExceptExpr).rep) map {
      case (expr, exprSeq) =>
        UnionExpr(expr +: exprSeq.toIndexedSeq)
    }

  private val intersectExceptExpr: P[IntersectExceptExpr] =
    P(instanceOfExpr ~ (("intersect" | "except").! ~ intersectExceptExpr).?) map {
      case (expr, None) =>
        SimpleIntersectExceptExpr(expr)
      case (expr, Some(opAndExpr)) =>
        CompoundIntersectExceptExpr(expr, IntersectExceptOp.parse(opAndExpr._1), opAndExpr._2)
    }

  private val instanceOfExpr: P[InstanceOfExpr] =
    P(treatExpr ~ ("instance" ~ "of" ~ sequenceType).?) map {
      case (expr, tpeOption) =>
        InstanceOfExpr(expr, tpeOption)
    }

  private val treatExpr: P[TreatExpr] =
    P(castableExpr ~ ("treat" ~ "as" ~ sequenceType).?) map {
      case (expr, tpeOption) =>
        TreatExpr(expr, tpeOption)
    }

  private val castableExpr: P[CastableExpr] =
    P(castExpr ~ ("castable" ~ "as" ~ singleType).?) map {
      case (expr, tpeOption) =>
        CastableExpr(expr, tpeOption)
    }

  private val castExpr: P[CastExpr] =
    P(unaryExpr ~ ("cast" ~ "as" ~ singleType).?) map {
      case (expr, tpeOption) =>
        CastExpr(expr, tpeOption)
    }

  private val unaryExpr: P[UnaryExpr] =
    P(("-" | "+").!.rep ~ valueExpr) map {
      case (ops, expr) =>
        UnaryExpr(ops.toIndexedSeq.map(op => UnaryOp.parse(op)), expr)
    }

  private val valueExpr: P[ValueExpr] =
    P(simpleMapExpr) map {
      case expr =>
        ValueExpr(expr)
    }

  private val simpleMapExpr: P[SimpleMapExpr] =
    P(pathExpr ~ ("|" ~ pathExpr).rep) map {
      case (expr, exprSeq) =>
        SimpleMapExpr(expr +: exprSeq.toIndexedSeq)
    }

  private val pathExpr: P[PathExpr] =
    P(slashOnlyPathExpr | pathExprStartingWithSingleSlash | pathExprStartingWithDoubleSlash | relativePathExpr)

  // Lookahead parsers

  private val canStartRelativePathExpr: P[Unit] =
    P(canStartAxisStep | canStartPostfixExpr)

  private val canStartAxisStep: P[Unit] =
    P(forwardAxis | reverseAxis).map(_ => ())

  private val canStartPostfixExpr: P[Unit] =
    P(literal | varRef | "(" | contextItemExpr | eqName | "function").map(_ => ())

  // Looking ahead to distinguish single slash from double slash, and to recognize start of relativePathExpr.
  // See xgc: leading-lone-slash constraint.

  private val slashOnlyPathExpr: P[PathExpr] =
    P("/" ~ !("/" | canStartRelativePathExpr)) map {
      case _ =>
        SlashOnlyPathExpr
    }

  // Looking ahead to distinguish single slash from double slash, and to recognize start of relativePathExpr.
  // See xgc: leading-lone-slash constraint. Note that canStartRelativePathExpr implies that the next token is not a slash!

  private val pathExprStartingWithSingleSlash: P[PathExpr] =
    P("/" ~ &(canStartRelativePathExpr) ~ relativePathExpr) map {
      case expr =>
        PathExprStartingWithSingleSlash(expr)
    }

  private val pathExprStartingWithDoubleSlash: P[PathExpr] =
    P("//" ~ relativePathExpr) map {
      case expr =>
        PathExprStartingWithDoubleSlash(expr)
    }

  private val relativePathExpr: P[RelativePathExpr] =
    P(stepExpr ~ (("/" | "//").! ~ relativePathExpr).?) map {
      case (expr, None) =>
        SimpleRelativePathExpr(expr)
      case (expr, Some(opAndExpr)) =>
        CompoundRelativePathExpr(expr, StepOp.parse(opAndExpr._1), opAndExpr._2)
    }

  private val stepExpr: P[StepExpr] =
    P(postfixExpr | axisStep)

  private val axisStep: P[AxisStep] =
    P(forwardAxisStep | reverseAxisStep)

  private val forwardAxisStep: P[ForwardAxisStep] =
    P(forwardStep ~ predicate.rep) map {
      case (forwardStep, predicates) =>
        ForwardAxisStep(forwardStep, predicates.toIndexedSeq)
    }

  private val reverseAxisStep: P[ReverseAxisStep] =
    P(reverseStep ~ predicate.rep) map {
      case (reverseStep, predicates) =>
        ReverseAxisStep(reverseStep, predicates.toIndexedSeq)
    }

  private val forwardStep: P[ForwardStep] =
    P(nonAbbrevForwardStep | abbrevForwardStep)

  private val abbrevForwardStep: P[AbbrevForwardStep] =
    P(simpleAbbrevForwardStep | attributeAxisAbbrevForwardStep)

  private val simpleAbbrevForwardStep: P[SimpleAbbrevForwardStep] =
    P(nodeTest) map {
      case nodeTest =>
        SimpleAbbrevForwardStep(nodeTest)
    }

  private val attributeAxisAbbrevForwardStep: P[AttributeAxisAbbrevForwardStep] =
    P("@" ~ nodeTest) map {
      case nodeTest =>
        AttributeAxisAbbrevForwardStep(nodeTest)
    }

  private val nonAbbrevForwardStep: P[NonAbbrevForwardStep] =
    P(forwardAxis ~ nodeTest) map {
      case (axis, nodeTest) =>
        NonAbbrevForwardStep(axis, nodeTest)
    }

  private val forwardAxis: P[ForwardAxis] =
    P(("child" | "descendant" | "attribute" | "self" | "descendant-or-self" | "following-sibling" | "following" | "namespace").! ~ "::") map {
      case "child"              => ForwardAxis.Child
      case "descendant"         => ForwardAxis.Descendant
      case "attribute"          => ForwardAxis.Attribute
      case "self"               => ForwardAxis.Self
      case "descendant-or-self" => ForwardAxis.DescendantOrSelf
      case "following-sibling"  => ForwardAxis.FollowingSibling
      case "following"          => ForwardAxis.Following
      case "namespace"          => ForwardAxis.Namespace
    }

  private val reverseStep: P[ReverseStep] =
    P(nonAbbrevReverseStep | abbrevReverseStep)

  private val abbrevReverseStep: P[AbbrevReverseStep.type] =
    P("..") map (_ => AbbrevReverseStep)

  private val nonAbbrevReverseStep: P[NonAbbrevReverseStep] =
    P(reverseAxis ~ nodeTest) map {
      case (axis, nodeTest) =>
        NonAbbrevReverseStep(axis, nodeTest)
    }

  private val reverseAxis: P[ReverseAxis] =
    P(("parent" | "ancestor" | "preceding-sibling" | "preceding" | "ancestor-or-self").! ~ "::") map {
      case "parent"            => ReverseAxis.Parent
      case "ancestor"          => ReverseAxis.Ancestor
      case "preceding-sibling" => ReverseAxis.PrecedingSibling
      case "preceding"         => ReverseAxis.Preceding
      case "ancestor-or-self"  => ReverseAxis.AncestorOrSelf
    }

  private val nodeTest: P[NodeTest] =
    P(kindTest | nameTest)

  private val nameTest: P[NameTest] =
    P(simpleNameTest | wildcard)

  private val simpleNameTest: P[SimpleNameTest] =
    P(eqName) map {
      case name =>
        SimpleNameTest(name)
    }

  // See ws: explicit constraint.

  private val wildcard: P[Wildcard] =
    P(???) map (v => ???) // TODO

  private val kindTest: P[KindTest] =
    P(documentTest | elementTest | attributeTest | schemaElementTest | schemaAttributeTest | piTest | commentTest | textTest | namespaceNodeTest | anyKindTest)

  private val documentTest: P[DocumentTest] =
    P(???) map (v => ???) // TODO

  private val elementTest: P[ElementTest] =
    P(???) map (v => ???) // TODO

  private val attributeTest: P[AttributeTest] =
    P(???) map (v => ???) // TODO

  private val schemaElementTest: P[SchemaElementTest] =
    P(???) map (v => ???) // TODO

  private val schemaAttributeTest: P[SchemaAttributeTest] =
    P(???) map (v => ???) // TODO

  private val piTest: P[PITest] =
    P(???) map (v => ???) // TODO

  private val commentTest: P[CommentTest.type] =
    P(???) map (v => ???) // TODO

  private val textTest: P[TextTest.type] =
    P(???) map (v => ???) // TODO

  private val namespaceNodeTest: P[NamespaceNodeTest.type] =
    P(???) map (v => ???) // TODO

  private val anyKindTest: P[AnyKindTest.type] =
    P(???) map (v => ???) // TODO

  private val postfixExpr: P[PostfixExpr] =
    P(???) map (v => ???) // TODO

  private val argumentList: P[ArgumentList] =
    P(???) map (v => ???) // TODO

  private val paramList: P[ParamList] =
    P(???) map (v => ???) // TODO

  private val param: P[Param] =
    P(???) map (v => ???) // TODO

  private val predicate: P[Predicate] =
    P(???) map (v => ???) // TODO

  // Primary expressions

  private val primaryExpr: P[PrimaryExpr] =
    P(literal | varRef | parenthesizedExpr | contextItemExpr | functionCall | functionItemExpr)

  private val literal: P[Literal] =
    P(???) map (v => ???) // TODO

  private val varRef: P[VarRef] =
    P(???) map (v => ???) // TODO

  private val parenthesizedExpr: P[ParenthesizedExpr] =
    P(???) map (v => ???) // TODO

  private val contextItemExpr: P[ContextItemExpr.type] =
    P(???) map (v => ???) // TODO

  private val functionCall: P[FunctionCall] =
    P(???) map (v => ???) // TODO

  private val functionItemExpr: P[FunctionItemExpr] =
    P(namedFunctionRef | inlineFunctionExpr)

  private val namedFunctionRef: P[NamedFunctionRef] =
    P(???) map (v => ???) // TODO

  private val inlineFunctionExpr: P[InlineFunctionExpr] =
    P(???) map (v => ???) // TODO

  // Types

  private val sequenceType: P[SequenceType] =
    P(???) map (v => ???) // TODO

  private val singleType: P[SingleType] =
    P(???) map (v => ???) // TODO

  // Operators etc.

  private val valueComp: P[ValueComp] =
    P(("eq" | "ne" | "lt" | "le" | "gt" | "ge").!) map (s => ValueComp.parse(s))

  private val generalComp: P[GeneralComp] =
    P(("=" | "!=" | "<" | "<=" | ">" | ">=").!) map (s => GeneralComp.parse(s))

  private val nodeComp: P[NodeComp] =
    P(("is" | "<<" | ">>").!) map (s => NodeComp.parse(s))

  // TODO
  private val eqName: P[QName] =
    P(CharsWhile(c => java.lang.Character.isJavaIdentifierPart(c) || (c == ':')).!) filter (s => !keywords.contains(s)) map (s => QName.parse(s))

  // TODO
  private val varName: P[QName] =
    P(CharsWhile(c => java.lang.Character.isJavaIdentifierPart(c) || (c == ':')).!) filter (s => !keywords.contains(s)) map (s => QName.parse(s))

  // TODO
  private val keywords: Set[String] = Set(
    "if",
    "in",
    "return",
    "some",
    "every",
    "satisfies")

  def main(args: Array[String]): Unit = {
    // Remove main method!!!
    val exprString = args(0)

    val parseResult = xpathExpr.parse(exprString)
    println(parseResult)
  }
}
