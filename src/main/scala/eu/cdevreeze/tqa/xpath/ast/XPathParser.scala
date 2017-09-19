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
    P(???) map (v => ???) // TODO

  private val unionExpr: P[UnionExpr] =
    P(???) map (v => ???) // TODO

  private val intersectExceptExpr: P[IntersectExceptExpr] =
    P(???) map (v => ???) // TODO

  private val instanceOfExpr: P[InstanceOfExpr] =
    P(???) map (v => ???) // TODO

  private val treatExpr: P[TreatExpr] =
    P(???) map (v => ???) // TODO

  private val castableExpr: P[CastableExpr] =
    P(???) map (v => ???) // TODO

  private val castExpr: P[CastExpr] =
    P(???) map (v => ???) // TODO

  private val unaryExpr: P[UnaryExpr] =
    P(???) map (v => ???) // TODO

  private val valueExpr: P[ValueExpr] =
    P(???) map (v => ???) // TODO

  private val simpleMapExpr: P[SimpleMapExpr] =
    P(???) map (v => ???) // TODO

  private val pathExpr: P[PathExpr] =
    P(???) map (v => ???) // TODO Also mind the single slash recognition

  private val relativePathExpr: P[RelativePathExpr] =
    P(???) map (v => ???) // TODO

  private val stepExpr: P[StepExpr] =
    P(???) map (v => ???) // TODO

  private val axisStep: P[AxisStep] =
    P(???) map (v => ???) // TODO

  private val valueComp: P[ValueComp] =
    P(("eq" | "ne" | "lt" | "le" | "gt" | "ge").!) map (s => ValueComp.parse(s))

  private val generalComp: P[GeneralComp] =
    P(("=" | "!=" | "<" | "<=" | ">" | ">=").!) map (s => GeneralComp.parse(s))

  private val nodeComp: P[NodeComp] =
    P(("is" | "<<" | ">>").!) map (s => NodeComp.parse(s))

  // TODO
  private val varName: P[QName] =
    P(CharsWhile(c => java.lang.Character.isJavaIdentifierPart(c) || (c == ':')).!) filter (s => !keywords.contains(s)) map (s => QName.parse(s))

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
