package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.DependencyVisitor

/**
 * Created by sreinck on 01.08.18.
 */
operator fun Expr<Boolean>.not() = NotExpr(this)

infix fun Expr<Boolean>.and(other: Expr<Boolean>) = AndExpr(this, other)

infix fun Expr<Boolean>.or(other: Expr<Boolean>) = OrExpr(this, other)

class NotExpr(private val argument: Expr<Boolean>) : Expr<Boolean>() {

    override fun eval(): Boolean {
        val argumentResult = argument.eval()
        return !argumentResult
    }

    override fun toString(): String {
        val argumentResult = argument.asFactor()
        return "!$argumentResult"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, argument)
    }

}

class AndExpr(private val left: Expr<Boolean>, private val right: Expr<Boolean>) : Expr<Boolean>() {

    override fun asFactor(): String = "(${toString()})"

    override fun eval(): Boolean {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult and rightResult
    }

    override fun toString(): String {
        val leftResult = left.toString()
        val rightResult = right.toString()
        return "$leftResult and $rightResult"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}

class OrExpr(private val left: Expr<Boolean>, private val right: Expr<Boolean>) : Expr<Boolean>() {

    override fun asFactor(): String = "(${toString()})"

    override fun eval(): Boolean {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return leftResult or rightResult
    }

    override fun toString(): String {
        val leftResult = left.toString()
        val rightResult = right.toString()
        return "$leftResult or $rightResult"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}
