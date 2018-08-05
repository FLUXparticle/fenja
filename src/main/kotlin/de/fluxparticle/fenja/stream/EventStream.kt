package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.DependencyVisitor
import de.fluxparticle.fenja.expr.Expr
import kotlin.math.max

/**
 * Created by sreinck on 04.08.18.
 */
abstract class EventStream<T> : Dependency<T> {

    internal abstract fun getTransaction(): Long

    infix fun <R> map(func: (T) -> R): EventStream<R> = MapEventStream(this, func)

    infix fun hold(initValue: T): Expr<T> = EventStreamHoldExpr(this, initValue)

    infix fun filter(predicate: (T) -> Boolean): EventStream<T> = FilterEventStream(this, predicate)

    infix fun orElse(other: EventStream<T>): EventStream<T> = OrElseEventStream(this, other)

    infix fun <S> zipWith(other: EventStream<S>) = ZipWithEventStreamBuilder(this, other)

    infix fun <S> snapshot(expr: Expr<S>) = SnapshotBuilder2(this, expr)

    infix fun gate(expr: Expr<Boolean>): EventStream<T> = GateEventStream(this, expr)

}

class GateEventStream<T>(private val source: EventStream<T>, private val expr: Expr<Boolean>) : EventStream<T>() {

    private var lastTransaction: Long = 0

    private val buffer = Buffer<T>()

    override fun getTransaction(): Long {
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {
            val predicate = expr.eval()
            if (predicate) {
                val value = source.eval()
                buffer.setValue(transaction, value)
            }
            lastTransaction = transaction
        }
        return buffer.getTransaction()
    }

    override fun eval(): T {
        return buffer.getValue()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source) // expr
    }

    override fun toString(): String {
        return "$source gate $expr"
    }

}

fun <T> EventStream<T?>.filterNotNull(): EventStream<T> {
    @Suppress("unchecked_cast")
    return filter { it != null } as EventStream<T>
}

infix fun <T, R> EventStream<T?>.mapNotNull(func: (T) -> R): EventStream<R?> {
    @Suppress("unchecked_cast")
    return map { it?.let(func) }
}

class SnapshotBuilder2<T, A>(private val source: EventStream<T>, private val exprA: Expr<A>) {

    operator fun <R> invoke(func: (T, A) -> R): EventStream<R> = SnapshotEventStream2(source, exprA, func)

    infix fun <B> and(exprB: Expr<B>) = SnapshotBuilder3(source, exprA, exprB)

}

class SnapshotEventStream2<T, A, R>(private val source: EventStream<T>, private val exprA: Expr<A>, private val func: (T, A) -> R) : EventStream<R>() {

    private val buffer = Buffer<R>()

    override fun getTransaction(): Long {
        return source.getTransaction()
    }

    override fun eval(): R {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val valueT = source.eval()
            val valueA = exprA.eval()
            val value = func.invoke(valueT, valueA)
            buffer.setValue(transaction, value)
        }
        return buffer.getValue()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source) // exprA
    }

    override fun toString(): String {
        return "($source snapshot $exprA) {}"
    }

}

class SnapshotBuilder3<T, A, B>(private val source: EventStream<T>, private val exprA: Expr<A>, private val exprB: Expr<B>) {

    operator fun <R> invoke(func: (T, A, B) -> R): EventStream<R> = SnapshotEventStream3(source, exprA, exprB, func)

}

class SnapshotEventStream3<T, A, B, R>(private val source: EventStream<T>, private val exprA: Expr<A>, private val exprB: Expr<B>, private val func: (T, A, B) -> R) : EventStream<R>() {

    private val buffer = Buffer<R>()

    override fun getTransaction(): Long {
        return source.getTransaction()
    }

    override fun eval(): R {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val valueT = source.eval()
            val valueA = exprA.eval()
            val valueB = exprB.eval()
            val value = func.invoke(valueT, valueA, valueB)
            buffer.setValue(transaction, value)
        }
        return buffer.getValue()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source) // exprA exprB
    }

    override fun toString(): String {
        return "($source snapshot $exprA and $exprB) {}"
    }

}

class EventStreamHoldExpr<T>(private val source: EventStream<T>, initValue: T) : Expr<T>() {

    private val buffer = Buffer<T>()

    init {
        buffer.setValue(0, initValue)
    }

    override fun eval(): T {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.eval()
            buffer.setValue(transaction, value)
        }
        return buffer.getValue()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

    override fun toString(): String {
        return source.toString()
    }

}

class ZipWithEventStreamBuilder<T, S>(private val source1: EventStream<T>, private val source2: EventStream<S>) {

    operator fun <R> invoke(func: (T, S) -> R): EventStream<R> = ZipWithEventStream(source1, source2, func)

}

class ZipWithEventStream<T, S, R>(private val source1: EventStream<T>, private val source2: EventStream<S>, private val func: (T, S) -> R) : EventStream<R>() {

    override fun getTransaction(): Long {
        val transaction1 = source1.getTransaction()
        val transaction2 = source2.getTransaction()
        return when {
            transaction1 > transaction2 -> throw IllegalStateException("only source1 had fired in $transaction1")
            transaction1 < transaction2 -> throw IllegalStateException("only source2 had fired in $transaction2")
            else -> transaction1
        }
    }

    override fun eval(): R {
        val value1 = source1.eval()
        val value2 = source2.eval()
        return func.invoke(value1, value2)
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source1, source2)
    }

    override fun toString(): String {
        return "$source1 zipWith $source2 {}"
    }

}

class OrElseEventStream<T>(private val source1: EventStream<T>, private val source2: EventStream<T>) : EventStream<T>() {

    override fun getTransaction(): Long {
        val transaction1 = source1.getTransaction()
        val transaction2 = source2.getTransaction()
        return max(transaction1, transaction2)
    }

    override fun eval(): T {
        val transaction1 = source1.getTransaction()
        val transaction2 = source2.getTransaction()
        return if (transaction1 >= transaction2) {
            source1.eval()
        } else {
            source2.eval()
        }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source1, source2)
    }

    override fun toString(): String {
        return "$source1 orElse $source2"
    }

}

class FilterEventStream<T>(private val source: EventStream<T>, private val predicate: (T) -> Boolean) : EventStream<T>() {

    private var lastTransaction: Long = 0

    private val buffer = Buffer<T>()

    override fun getTransaction(): Long {
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {
            val value = source.eval()
            if (predicate.invoke(value)) {
                buffer.setValue(transaction, value)
            }
            lastTransaction = transaction
        }
        return buffer.getTransaction()
    }

    override fun eval(): T {
        return buffer.getValue()
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

    override fun toString(): String {
        return "$source filter {}"
    }

}

class MapEventStream<T, R>(private val source: EventStream<T>, private val func: (T) -> R) : EventStream<R>() {

    override fun getTransaction(): Long {
        return source.getTransaction()
    }

    override fun eval(): R {
        val value = source.eval()
        return func.invoke(value)
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

    override fun toString(): String {
        return "$source {}"
    }

}
