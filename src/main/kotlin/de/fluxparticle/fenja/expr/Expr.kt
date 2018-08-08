package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.MapDependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.value.PropertyValue
import javafx.beans.property.Property
import kotlin.math.max

/**
 * Created by sreinck on 03.06.18.
 */
abstract class Expr<T> internal constructor() {

    internal abstract val dependency: Dependency<T>

    fun sample(): T = dependency.getValue()

    infix fun <R> map(func: (T) -> R) : UpdateExpr<R> = MapExpr(this, func)

    infix fun <S> combine(other: Expr<S>) = CombineExprBuilder2(this, other)

}

abstract class UpdateExpr<T> internal constructor() : Expr<T>() {

    abstract override val dependency: UpdateDependency<T>

    override fun toString(): String {
        return dependency.toUpdateString()
    }

}

infix fun <T> Property<T>.bind(expr: UpdateExpr<T>) {
    expr.dependency.loop(PropertyValue(this))
}

abstract class SourceExpr<T> internal constructor() : Expr<T>() {

    abstract override val dependency: SourceDependency<T>

    override fun toString(): String {
        return dependency.toString()
    }

}

class ConstExpr<T>(initValue: T) : UpdateExpr<T>() {

    override val dependency: UpdateDependency<T> = ConstDependency(initValue)

    private class ConstDependency<T>(initValue: T) : UpdateDependency<T>() {

        init {
            buffer.setValue(0L, initValue)
        }

        override fun update() {
            // empty
        }

        override fun getDependencies(): Sequence<Dependency<*>> {
            return emptySequence()
        }

        override fun toUpdateString(): String {
            return buffer.getValue().toString()
        }

    }

}

class MapExpr<T, R>(
        argument: Expr<T>,
        func: (T) -> R
) : UpdateExpr<R>() {

    override val dependency: UpdateDependency<R> = MapDependency(argument.dependency, func)

}

class CombineExprBuilder2<A, B>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>
) {

    operator fun <R> invoke(func: (A, B) -> R) : UpdateExpr<R> = CombineExpr2(paramA, paramB, func)

    infix fun <C> combine(next: Expr<C>) = CombineExprBuilder3(paramA, paramB, next)

}

class CombineExpr2<A, B, R>(
        paramA: Expr<A>,
        paramB: Expr<B>,
        func: (A, B) -> R
) : UpdateExpr<R>() {

    override val dependency: UpdateDependency<R> = CombineDependency2(paramA.dependency, paramB.dependency, func)

    private class CombineDependency2<A, B, R>(
            private val paramA: Dependency<A>,
            private val paramB: Dependency<B>,
            private val func: (A, B) -> R
    ) : UpdateDependency<R>() {

        override fun update() {
            val transactionA = paramA.getTransaction()
            val transactionB = paramB.getTransaction()
            val transaction = max(transactionA, transactionB)
            if (transaction > buffer.getTransaction()) {
                val valueA = paramA.getValue()
                val valueB = paramB.getValue()
                val combined = func.invoke(valueA, valueB)
                buffer.setValue(transaction, combined)
            }
        }

        override fun getDependencies(): Sequence<Dependency<*>> {
            return sequenceOf(paramA, paramB)
        }

        override fun toUpdateString(): String {
            return "($paramA combine $paramB) {}"
        }

    }

}

class CombineExprBuilder3<A, B, C>(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>,
        private val paramC: Expr<C>
) {

    operator fun <R> invoke(func: (A, B, C) -> R) : UpdateExpr<R> = CombineExpr3(paramA, paramB, paramC, func)

}

class CombineExpr3<A, B, C, R>(
        paramA: Expr<A>,
        paramB: Expr<B>,
        paramC: Expr<C>,
        func: (A, B, C) -> R
) : UpdateExpr<R>() {

    override val dependency: UpdateDependency<R> = CombineDependency3(paramA.dependency, paramB.dependency, paramC.dependency, func)

    private class CombineDependency3<A, B, C, R>(
            private val paramA: Dependency<A>,
            private val paramB: Dependency<B>,
            private val paramC: Dependency<C>,
            private val func: (A, B, C) -> R
    ) : UpdateDependency<R>() {

        override fun update() {
            val transactionA = paramA.getTransaction()
            val transactionB = paramB.getTransaction()
            val transactionC = paramC.getTransaction()
            val transaction = max(max(transactionA, transactionB), transactionC)
            if (transaction > buffer.getTransaction()) {
                val valueA = paramA.getValue()
                val valueB = paramB.getValue()
                val valueC = paramC.getValue()
                val combined = func.invoke(valueA, valueB, valueC)
                buffer.setValue(transaction, combined)
            }
        }

        override fun getDependencies(): Sequence<Dependency<*>> {
            return sequenceOf(paramA, paramB, paramC)
        }

        override fun toUpdateString(): String {
            return "($paramA combine $paramB combine $paramC) {}"
        }

    }

}
