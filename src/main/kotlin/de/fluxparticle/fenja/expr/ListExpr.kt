package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
class MinExpr(private val arguments: Iterable<Expr<Double>>) : Expr<Double>() {

    override fun eval(): Double {
        return arguments.map { it.eval() }.min() ?: 0.0
    }

    override fun toString(): String {
        return "min ${arguments.toList()}"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, *arguments.toList().toTypedArray())
    }

}

class MaxExpr(private val arguments: Iterable<Expr<Double>>) : Expr<Double>() {

    override fun eval(): Double {
        return arguments.map { it.eval() }.max() ?: 0.0
    }

    override fun toString(): String {
        return "max ${arguments.toList()}"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, *arguments.toList().toTypedArray())
    }

}
