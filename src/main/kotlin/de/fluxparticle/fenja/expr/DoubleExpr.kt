package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
abstract class DoubleExpr : Expr<Double>() {
    
    override fun <R, D> accept(visitor: ExprVisitor<Double, R, D>, data: D): R {
        return accept(visitor as DoubleExprVisitor<R, D>, data)
    }

    abstract fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R

}

operator fun Expr<Double>.unaryMinus() = NegateExpr(this)

operator fun Expr<Double>.plus(other: Expr<Double>) = PlusExpr(this, other)

operator fun Expr<Double>.plus(other: Double) = plus(ConstExpr(other))

operator fun Expr<Double>.minus(other: Expr<Double>) = MinusExpr(this, other)

operator fun Expr<Double>.minus(other: Double) = minus(ConstExpr(other))

operator fun Expr<Double>.times(other: Expr<Double>) = TimesExpr(this, other)

operator fun Expr<Double>.times(other: Double) = times(ConstExpr(other))

operator fun Expr<Double>.div(other: Expr<Double>) = DivExpr(this, other)

operator fun Expr<Double>.div(other: Double) = div(ConstExpr(other))


class NegateExpr(val argument: Expr<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitNegateExpr(argument, data)
    }
}

class PlusExpr(val left: Expr<Double>, val right: Expr<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitPlusExpr(left, right, data)
    }
}

class MinusExpr(val left: Expr<Double>, val right: Expr<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitMinusExpr(left, right, data)
    }
}

class TimesExpr(val left: Expr<Double>, val right: Expr<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitTimesExpr(left, right, data)
    }
}

class DivExpr(val left: Expr<Double>, val right: Expr<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitDivExpr(left, right, data)
    }
}

/*
class MinExpr(val arguments: LoopList<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitPlusExpr(left, right, data)
    }
}

class MaxExpr(val arguments: LoopList<Double>) : DoubleExpr() {
    override fun <R, D> accept(visitor: DoubleExprVisitor<R, D>, data: D): R {
        return visitor.visitPlusExpr(left, right, data)
    }
}
*/
