package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.MapDependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.value.PropertyValue
import de.fluxparticle.fenja.value.ReadWriteValue
import javafx.beans.property.Property
import kotlin.math.max

/**
 * Created by sreinck on 03.06.18.
 */
abstract class Expr<T> internal constructor() {

    internal abstract val dependency: Dependency<T>

    fun sample(): T = dependency.getValue()

    abstract fun identity(): UpdateExpr<T>

    infix fun <R> map(func: (T) -> R) : UpdateExpr<R> = MapExpr(this, func)

    infix fun <S> combine(other: Expr<S>) = CombineExprBuilder2(this, other)

}

abstract class UpdateExpr<T> internal constructor() : Expr<T>() {

    abstract override val dependency: UpdateDependency<T>

    final override fun identity() = this

    override fun toString(): String {
        return dependency.toUpdateString()
    }

}

infix fun <T> Property<T>.bind(expr: UpdateExpr<T>) {
    PropertyValue(this) bind expr
}

infix fun <T> ReadWriteValue<T>.bind(expr: UpdateExpr<T>) {
    expr.dependency.loop(this)
}

abstract class SourceExpr<T> internal constructor() : Expr<T>() {

    abstract override val dependency: SourceDependency<T>

    final override fun identity(): UpdateExpr<T> = IdentityExpr(this)

    override fun toString(): String {
        return dependency.toString()
    }

}

internal class ConstExpr<T>(initValue: T) : UpdateExpr<T>() {

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

internal class IdentityExpr<T>(argument: Expr<T>) : UpdateExpr<T>() {

    override val dependency: UpdateDependency<T> = IdentityDependency(argument.dependency)

    private class IdentityDependency<T>(
            private val argument: Dependency<T>
    ) : UpdateDependency<T>() {
        override fun getDependencies(): Sequence<Dependency<*>> {
            return sequenceOf(argument)
        }

        override fun update() {
            val transaction = argument.getTransaction()
            if (transaction > buffer.getTransaction()) {
                val value = argument.getValue()
                buffer.setValue(transaction, value)
            }
        }

        override fun toUpdateString(): String {
            return argument.toString()
        }

    }

}

internal class MapExpr<T, R>(
        argument: Expr<T>,
        func: (T) -> R
) : UpdateExpr<R>() {

    override val dependency: UpdateDependency<R> = MapDependency(argument.dependency, func)

}

class CombineExprBuilder2<A, B> internal constructor(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>
) {

    operator fun <R> invoke(func: (A, B) -> R) : UpdateExpr<R> = CombineExpr2(paramA, paramB, func)

    infix fun <C> combine(next: Expr<C>) = CombineExprBuilder3(paramA, paramB, next)

}

internal class CombineExpr2<A, B, R>(
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

class CombineExprBuilder3<A, B, C> internal constructor(
        private val paramA: Expr<A>,
        private val paramB: Expr<B>,
        private val paramC: Expr<C>
) {

    operator fun <R> invoke(func: (A, B, C) -> R) : UpdateExpr<R> = CombineExpr3(paramA, paramB, paramC, func)

}

internal class CombineExpr3<A, B, C, R>(
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

class LazyExpr<T> internal constructor(private val name: String) : UpdateExpr<T>() {

    override val dependency: UpdateDependency<T> = LazyDependency()

    private lateinit var argument: Expr<T>

    fun setExpr(argument: Expr<T>) {
        this.argument = argument
    }

    override fun toString(): String {
        return name
    }

    private inner class LazyDependency: UpdateDependency<T>() {

        private val argument: Dependency<T>
            get() = this@LazyExpr.argument.dependency

        override fun getDependencies(): Sequence<Dependency<*>> {
            return sequenceOf(argument)
        }

        override fun update() {
            val transaction = argument.getTransaction()
            if (transaction > buffer.getTransaction()) {
                val value = argument.getValue()
                buffer.setValue(transaction, value)
            }
        }

        override fun toUpdateString(): String {
            return this@LazyExpr.toString()
        }

    }

}
