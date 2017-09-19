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
 * @author Chris de Vreeze
 */
object XPathExpressions {

  final case class XPathExpr(expr: Expr)

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

  // Bindings

  final case class SimpleForBinding(varName: QName, expr: ExprSingle)

  final case class SimpleLetBinding(varName: QName, expr: ExprSingle)

  final case class SimpleBindingInQuantifiedExpr(varName: QName, expr: ExprSingle)

  // Operators

  sealed trait Comp
  sealed trait ValueComp extends Comp
  sealed trait GeneralComp extends Comp
  sealed trait NodeComp extends Comp

  object ValueComp {

    case object Eq extends ValueComp
    case object Ne extends ValueComp
    case object Lt extends ValueComp
    case object Le extends ValueComp
    case object Gt extends ValueComp
    case object Ge extends ValueComp

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

    case object Eq extends GeneralComp
    case object Ne extends GeneralComp
    case object Lt extends GeneralComp
    case object Le extends GeneralComp
    case object Gt extends GeneralComp
    case object Ge extends GeneralComp

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

    case object Is extends NodeComp
    case object Precedes extends NodeComp
    case object Follows extends NodeComp

    def parse(s: String): NodeComp = s match {
      case "is" => Is
      case "<<" => Precedes
      case ">>" => Follows
    }
  }

  sealed trait AdditionOp

  object AdditionOp {

    case object Plus extends AdditionOp
    case object Minus extends AdditionOp

    def parse(s: String): AdditionOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  sealed trait MultiplicativeOp

  object MultiplicativeOp {

    case object Times extends MultiplicativeOp
    case object Div extends MultiplicativeOp
    case object IDiv extends MultiplicativeOp
    case object Mod extends MultiplicativeOp

    def parse(s: String): MultiplicativeOp = s match {
      case "*"    => Times
      case "div"  => Div
      case "idiv" => IDiv
      case "mod"  => Mod
    }
  }

  sealed trait IntersectExceptOp

  object IntersectExceptOp {

    case object Intersect extends IntersectExceptOp
    case object Except extends IntersectExceptOp

    def parse(s: String): IntersectExceptOp = s match {
      case "intersect" => Intersect
      case "except"    => Except
    }
  }

  sealed trait UnaryOp

  object UnaryOp {

    case object Plus extends UnaryOp
    case object Minus extends UnaryOp

    def parse(s: String): UnaryOp = s match {
      case "+" => Plus
      case "-" => Minus
    }
  }

  // Keywords etc.

  sealed trait Quantifier

  object Quantifier {

    case object SomeQuantifier extends Quantifier
    case object EveryQuantifier extends Quantifier

    def parse(s: String): Quantifier = s match {
      case "some"  => SomeQuantifier
      case "every" => EveryQuantifier
    }
  }
}
