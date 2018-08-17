package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import javafx.beans.property.Property
import kotlin.math.max

/**
 * Created by sreinck on 03.06.18.
 */

internal class PropertyDependency<T>(
        private val argument: Dependency<T>,
        private val property: Property<in T>
) : UpdateDependency<T>() {

    init {
        name = property.name
    }

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

    override fun updateLoop() {
        val value = buffer.getValue()
        property.value = value
    }

    override fun toUpdateString(): String {
        return argument.toString()
    }

}

internal class ConstDependency<T>(initValue: T) : UpdateDependency<T>() {

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

internal class IdentityDependency<T>(
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


internal class CombineDependency2<A, B, R>(
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

internal class CombineDependency3<A, B, C, R>(
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

internal class LazyDependency<T> : UpdateDependency<T>() {

    private lateinit var argument: Dependency<T>

    fun loop(dependency: Dependency<T>) {
        if (this::argument.isInitialized) {
            throw IllegalStateException("already closed")
        }
        argument = dependency
    }

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

/*
class LazyExpr<T> internal constructor(private val name: String) : FenjaSystem.UpdateExpr<T>() {

    override val dependency: UpdateDependency<T> = LazyDependency()

    fun setExpr(argument: Expr<T>) {
        (dependency as LazyDependency).argument = argument.dependency
    }

    override fun toString(): String {
        return name
    }


}
*/
