package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
class DoubleExprPrinter : ExprPrinter<Double>(), DoubleExprVisitor<String, Boolean> {

    override fun visitNegateExpr(argument: Expr<Double>, data: Boolean): String {
        val argumentResult = argument.accept(this, true)
        return "-$argumentResult"
    }

    override fun visitPlusExpr(left: Expr<Double>, right: Expr<Double>, data: Boolean): String {
        val leftResult = left.accept(this, false)
        val rightResult = right.accept(this, false)
        return "$leftResult + $rightResult".asFactor(data)
    }

    override fun visitMinusExpr(left: Expr<Double>, right: Expr<Double>, data: Boolean): String {
        val leftResult = left.accept(this, false)
        val rightResult = right.accept(this, true)
        return "$leftResult - $rightResult".asFactor(data)
    }

    override fun visitTimesExpr(left: Expr<Double>, right: Expr<Double>, data: Boolean): String {
        val leftResult = left.accept(this, true)
        val rightResult = right.accept(this, true)
        return "$leftResult * $rightResult"
    }

    override fun visitDivExpr(left: Expr<Double>, right: Expr<Double>, data: Boolean): String {
        val leftResult = left.accept(this, true)
        val rightResult = right.accept(this, true)
        return "$leftResult / $rightResult"
    }

}
