package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 03.06.18.
 */
abstract class Expr<T> {

    operator fun <R> invoke(func: (T) -> R) : Expr<R> = MapExpr(this, func)

    operator fun <S> rangeTo(other: Expr<S>) : CombineExprBuilder<T, S> = CombineExprBuilder(this, other)

    abstract fun eval(): T

    abstract override fun toString(): String

    open fun asFactor(): String = toString()

    abstract fun <R> accept(visitor: ExprVisitor<R>): R

}

class ConstExpr<T>(private val value: T) : Expr<T>() {

    override fun eval(): T {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this)
    }

}

class MapExpr<T, R>(private val argument: Expr<T>, private val func: (T) -> R) : Expr<R>() {

    override fun eval(): R {
        val argumentResult = argument.eval()
        return func.invoke(argumentResult)
    }

    override fun asFactor(): String = "(${toString()})"

    override fun toString(): String {
        val argumentResult = argument.asFactor()
        return "$argumentResult {}"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, argument)
    }

}

class CombineExpr<T, S, R>(private val left: Expr<T>, private val right: Expr<S>, private val func: (T, S) -> R) : Expr<R>() {

    override fun eval(): R {
        val leftResult = left.eval()
        val rightResult = right.eval()
        return func.invoke(leftResult, rightResult)
    }

    override fun asFactor(): String = "(${toString()})"

    override fun toString(): String {
        val leftResult = left.asFactor()
        val rightResult = right.asFactor()
        return "($leftResult..$rightResult) {}"
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this, left, right)
    }

}

class CombineExprBuilder<T, S>(private val left: Expr<T>, private val right: Expr<S>) {

    operator fun <R> invoke(func: (T, S) -> R) : Expr<R> = CombineExpr(left, right, func)

}
