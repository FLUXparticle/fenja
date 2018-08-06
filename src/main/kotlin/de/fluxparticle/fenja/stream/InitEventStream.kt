package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.DependencyVisitor

/**
 * Created by sreinck on 06.08.18.
 */
class InitEventStream<T>(private val source: EventStream<T>, private val initEvent: T) : EventStream<T>() {

    private var lastTransaction: Long = -1L

    override fun getTransaction(): Long {
        lastTransaction = if (lastTransaction < 0L) {
            0L
        } else {
            source.getTransaction()
        }
        return lastTransaction
    }

    override fun eval(): T {
        return if (lastTransaction == 0L) {
            initEvent
        } else {
            source.eval()
        }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

    override fun toString(): String {
        return source.toString()
    }

}
