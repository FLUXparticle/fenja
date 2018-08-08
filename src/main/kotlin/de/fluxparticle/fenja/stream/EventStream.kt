package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.MapDependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.expr.UpdateExpr
import kotlin.math.max

/**
 * Created by sreinck on 04.08.18.
 */
abstract class EventStream<T> internal constructor(internal open val dependency: Dependency<T>) {

    infix fun <R> map(func: (T) -> R): UpdateEventStream<R> = MapEventStream(this, func)

    infix fun hold(initValue: T): UpdateExpr<T> = EventStreamHoldExpr(this, initValue)

    infix fun filter(predicate: (T) -> Boolean): UpdateEventStream<T> = FilterEventStream(this, predicate)

    infix fun orElse(other: EventStream<T>): UpdateEventStream<T> = OrElseEventStream(this, other)

    infix fun <S> zipWith(other: EventStream<S>) = ZipWithEventStreamBuilder(this, other)

}

abstract class UpdateEventStream<T> internal constructor(override val dependency: UpdateDependency<T>) : EventStream<T>(dependency) {

}

abstract class SourceEventStream<T> internal constructor(override val dependency: SourceDependency<T>) : EventStream<T>(dependency) {

}

fun <T> EventStream<T?>.filterNotNull(): UpdateEventStream<T> {
    @Suppress("unchecked_cast")
    return filter { it != null } as UpdateEventStream<T>
}

infix fun <T, R> EventStream<T?>.mapNotNull(func: (T) -> R): UpdateEventStream<R?> {
    @Suppress("unchecked_cast")
    return map { it?.let(func) }
}

class EventStreamHoldExpr<T>(
        source: EventStream<T>,
        initValue: T
) : UpdateExpr<T>(EventStreamHoldDependency(source.dependency, initValue)) {

    private class EventStreamHoldDependency<T>(
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
            return source.toString()
        }

    }

}

class ZipWithEventStreamBuilder<T, S>(private val source1: EventStream<T>, private val source2: EventStream<S>) {

    operator fun <R> invoke(func: (T, S) -> R): EventStream<R> = ZipWithEventStream(source1, source2, func)

}

class ZipWithEventStream<A, B, R>(
        sourceA: EventStream<A>,
        sourceB: EventStream<B>,
        func: (A, B) -> R
) : UpdateEventStream<R>(ZipWithDependency(sourceA.dependency, sourceB.dependency, func)) {

    private class ZipWithDependency<A, B, R>(
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

}

class OrElseEventStream<T>(
        source1: EventStream<T>,
        source2: EventStream<T>
) : UpdateEventStream<T>(OrElseDependency(source1.dependency, source2.dependency)) {

    private class OrElseDependency<T>(
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

}

class FilterEventStream<T>(
        source: EventStream<T>,
        predicate: (T) -> Boolean
) : UpdateEventStream<T>(FilterDependency(source.dependency, predicate)) {

    private class FilterDependency<T>(
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

}

class MapEventStream<T, R>(
        source: EventStream<T>,
        func: (T) -> R
) : UpdateEventStream<R>(MapDependency(source.dependency, func)) {

}
