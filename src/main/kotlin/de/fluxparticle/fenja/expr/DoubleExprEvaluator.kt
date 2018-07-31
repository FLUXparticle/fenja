package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
class DoubleExprEvaluator : ExprEvaluator<Double>(), DoubleExprVisitor<Double, Void?> {

    override fun visitNegateExpr(argument: Expr<Double>, data: Void?): Double {
        val argumentResult = argument.accept(this, null)
        return -argumentResult
    }

    override fun visitPlusExpr(left: Expr<Double>, right: Expr<Double>, data: Void?): Double {
        val leftResult = left.accept(this, null)
        val rightResult = right.accept(this, null)
        return leftResult + rightResult
    }

    override fun visitMinusExpr(left: Expr<Double>, right: Expr<Double>, data: Void?): Double {
        val leftResult = left.accept(this, null)
        val rightResult = right.accept(this, null)
        return leftResult - rightResult
    }

    override fun visitTimesExpr(left: Expr<Double>, right: Expr<Double>, data: Void?): Double {
        val leftResult = left.accept(this, null)
        val rightResult = right.accept(this, null)
        return leftResult * rightResult
    }

    override fun visitDivExpr(left: Expr<Double>, right: Expr<Double>, data: Void?): Double {
        val leftResult = left.accept(this, null)
        val rightResult = right.accept(this, null)
        return leftResult / rightResult
    }

}
