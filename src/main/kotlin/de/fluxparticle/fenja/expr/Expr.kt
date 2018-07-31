package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 03.06.18.
 */
abstract class Expr<T> {

    operator fun <R> invoke(func: (T) -> R) : Expr<R> = MapExpr(this, func)

    operator fun <S> rangeTo(other: Expr<S>) : CombineExprBuilder<T, S> = CombineExprBuilder(this, other)
    
    abstract fun <R, D> accept(visitor: ExprVisitor<T, R, D>, data: D): R

}

class ConstExpr<T>(private val value: T) : Expr<T>() {

    override fun <R, D> accept(visitor: ExprVisitor<T, R, D>, data: D): R {
        return visitor.visitConstExpr(value, data)
    }

}

class MapExpr<T, R>(private val argument: Expr<T>, private val func: (T) -> R) : Expr<R>() {

    override fun <Result, D> accept(visitor: ExprVisitor<R, Result, D>, data: D): Result {
        return visitor.visitMapExpr(argument, func, data)
    }

}

class CombineExpr<T, S, R>(private val left: Expr<T>, private val right: Expr<S>, private val func: (T, S) -> R) : Expr<R>() {

    override fun <Result, D> accept(visitor: ExprVisitor<R, Result, D>, data: D): Result {
        return visitor.visitCombineExpr(left, right, func, data)
    }

}

class CombineExprBuilder<T, S>(private val left: Expr<T>, private val right: Expr<S>) {

    operator fun <R> invoke(func: (T, S) -> R) : Expr<R> = CombineExpr(left, right, func)

}
