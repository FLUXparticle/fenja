package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.FenjaSystem.EventStream
import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import kotlin.math.max

/**
 * Created by sreinck on 04.08.18.
 */
fun <T> EventStream<T?>.filterNotNull(): FenjaSystem.UpdateEventStream<T> {
    @Suppress("unchecked_cast")
    return filter { it != null } as FenjaSystem.UpdateEventStream<T>
}

infix fun <T, R> EventStream<T?>.mapNotNull(func: (T) -> R): FenjaSystem.UpdateEventStream<R?> {
    @Suppress("unchecked_cast")
    return map { it?.let(func) }
}

internal class EventStreamHoldDependency<T>(
        private val source: Dependency<T>,
        initValue: T
) : UpdateDependency<T>() {

    init {
        buffer.setValue(0, initValue)
    }

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.getValue()
            buffer.setValue(transaction, value)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source)
    }

    override fun toUpdateString(): String {
        return "$source hold _"
    }

}

internal class ZipWithDependency<A, B, R>(
        private val sourceA: Dependency<A>,
        private val sourceB: Dependency<B>,
        private val func: (A, B) -> R
) : UpdateDependency<R>() {

    override fun update() {
        val transactionA = sourceA.getTransaction()
        val transactionB = sourceB.getTransaction()
        val transaction = max(transactionA, transactionB)
        when {
            transactionA < transaction -> throw IllegalStateException("sourceA had not fired in $transaction")
            transactionB < transaction -> throw IllegalStateException("sourceB had not fired in $transaction")
        }
        if (transaction > buffer.getTransaction()) {
            val valueA = sourceA.getValue()
            val valueB = sourceB.getValue()
            val zipped = func.invoke(valueA, valueB)
            buffer.setValue(transaction, zipped)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(sourceA, sourceB)
    }

    override fun toUpdateString(): String {
        return "$sourceA zipWith $sourceB {}"
    }

}

internal class OrElseDependency<T>(
        private val source1: Dependency<T>,
        private val source2: Dependency<T>
) : UpdateDependency<T>() {

    override fun update() {
        val transaction1 = source1.getTransaction()
        val transaction2 = source2.getTransaction()
        if (transaction1 > buffer.getTransaction()) {
            val value = source1.getValue()
            buffer.setValue(transaction1, value)
        } else if (transaction2 > buffer.getTransaction()) {
            val value = source2.getValue()
            buffer.setValue(transaction2, value)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source1, source2)
    }

    override fun toUpdateString(): String {
        return "$source1 orElse $source2"
    }
}

internal class FilterDependency<T>(
        private val source: Dependency<T>,
        private val predicate: (T) -> Boolean
) : UpdateDependency<T>() {

    private var lastTransaction: Long = -1L

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {
            val value = source.getValue()
            if (predicate.invoke(value)) {
                buffer.setValue(transaction, value)
            }
            lastTransaction = transaction
        }

    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source)
    }

    override fun toUpdateString(): String {
        return "$source filter {}"
    }

}
