package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.DependencyVisitor

/**
 * Created by sreinck on 03.06.18.
 */
abstract class Expr<T> : Dependency<T> {

    operator fun <R> invoke(func: (T) -> R) : Expr<R> = MapExpr(this, func)

    operator fun <S> rangeTo(other: Expr<S>) = CombineExprBuilder2(this, other)


    open fun asFactor(): String = toString()

}

class ConstExpr<T>(private val value: T) : Expr<T>() {

    override fun eval(): T {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
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

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, argument)
    }

}

class CombineExpr2<A, B, R>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>,
        private val func: (A, B) -> R
) : Expr<R>() {

    override fun eval(): R {
        val resultA = paramA.eval()
        val resultB = paramB.eval()
        return func.invoke(resultA, resultB)
    }

    override fun asFactor(): String = "(${toString()})"

    override fun toString(): String {
        val resultA = paramA.asFactor()
        val resultB = paramB.asFactor()
        return "($resultA..$resultB) {}"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, paramA, paramB)
    }

}

class CombineExprBuilder2<A, B>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>
) {

    operator fun <R> invoke(func: (A, B) -> R) : Expr<R> = CombineExpr2(paramA, paramB, func)

    operator fun <C> rangeTo(next: Expr<C>) = CombineExprBuilder3(paramA, paramB, next)

}

class CombineExpr3<A, B, C, R>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>,
        private val paramC: Expr<C>,
        private val func: (A, B, C) -> R
) : Expr<R>() {

    override fun eval(): R {
        val resultA = paramA.eval()
        val resultB = paramB.eval()
        val resultC = paramC.eval()
        return func.invoke(resultA, resultB, resultC)
    }

    override fun asFactor(): String = "(${toString()})"

    override fun toString(): String {
        val resultA = paramA.asFactor()
        val resultB = paramB.asFactor()
        val resultC = paramC.asFactor()
        return "($resultA..$resultB..$resultC) {}"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, paramA, paramB, paramC)
    }

}

class CombineExprBuilder3<A, B, C>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>,
        private val paramC: Expr<C>
) {

    operator fun <R> invoke(func: (A, B, C) -> R) : Expr<R> = CombineExpr3(paramA, paramB, paramC, func)

}
