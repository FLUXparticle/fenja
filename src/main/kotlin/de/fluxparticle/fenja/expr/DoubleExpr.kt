package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */

operator fun Expr<Double>.unaryMinus() = NegateExpr(this)

operator fun Expr<Double>.plus(other: Expr<Double>) = PlusExpr(this, other)

operator fun Expr<Double>.plus(other: Double) = plus(ConstExpr(other))

operator fun Expr<Double>.minus(other: Expr<Double>) = MinusExpr(this, other)

operator fun Expr<Double>.minus(other: Double) = minus(ConstExpr(other))

operator fun Expr<Double>.times(other: Expr<Double>) = TimesExpr(this, other)

operator fun Expr<Double>.times(other: Double) = times(ConstExpr(other))

operator fun Expr<Double>.div(other: Expr<Double>) = DivExpr(this, other)

operator fun Expr<Double>.div(other: Double) = div(ConstExpr(other))


class NegateExpr(private val argument: Expr<Double>) : Expr<Double>() {

    override fun eval(): Double {
        val argumentResult = argument.eval()
        return -argumentResult
    }

    override fun toString(): String {
        val argumentResult = argument.asFactor()
        return "-$argumentResult"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, argument)
    }

}

class PlusExpr(private val left: Expr<Double>, private val right: Expr<Double>) : Expr<Double>() {

    override fun asFactor(): String = "(${toString()})"

    override fun eval(): Double {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult + rightResult
    }

    override fun toString(): String {
        val leftResult = left.toString()
        val rightResult = right.toString()
        return "$leftResult + $rightResult"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}

class MinusExpr(private val left: Expr<Double>, private val right: Expr<Double>) : Expr<Double>() {

    override fun asFactor(): String = "(${toString()})"

    override fun eval(): Double {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult - rightResult
    }

    override fun toString(): String {
        val leftResult = left.toString()
        val rightResult = right.asFactor()
        return "$leftResult - $rightResult"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}

class TimesExpr(private val left: Expr<Double>, private val right: Expr<Double>) : Expr<Double>() {

    override fun eval(): Double {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult * rightResult
    }

    override fun toString(): String {
        val leftResult = left.asFactor()
        val rightResult = right.asFactor()
        return "$leftResult * $rightResult"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}

class DivExpr(private val left: Expr<Double>, private val right: Expr<Double>) : Expr<Double>() {

    override fun eval(): Double {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult / rightResult
    }

    override fun toString(): String {
        val leftResult = left.asFactor()
        val rightResult = right.asFactor()
        return "$leftResult / $rightResult"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}
