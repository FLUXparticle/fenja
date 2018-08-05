package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.DependencyVisitor

/**
 * Created by sreinck on 31.07.18.
 */
class ListExpr<T> internal constructor(): Expr<List<T>>() {

    override fun eval(): List<T> {
        TODO("not implemented")
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        TODO("not implemented")
    }

    override fun toString(): String {
        TODO("not implemented")
    }

}

class MinExpr(private val arguments: Iterable<Expr<Double>>) : Expr<Double>() {

    override fun eval(): Double {
        return arguments.map { it.eval() }.min() ?: 0.0
    }

    override fun toString(): String {
        return "min ${arguments.toList()}"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
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

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, *arguments.toList().toTypedArray())
    }

}
