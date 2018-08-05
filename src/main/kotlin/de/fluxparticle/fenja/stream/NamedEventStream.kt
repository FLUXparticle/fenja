package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.*
import de.fluxparticle.fenja.logger.FenjaSystemLogger

/**
 * Created by sreinck on 05.08.18.
 */
abstract class NamedEventStream<T>(override val name: String) : EventStream<T>(), NamedDependency<T> {

    protected val buffer = Buffer<T>()

    override fun getTransaction(): Long {
        return buffer.getTransaction()
    }

    override fun eval(): T {
        return buffer.getValue()
    }

}

class EventStreamSource<T>(name: String, private val logger: FenjaSystemLogger) : NamedEventStream<T>(name), SourceDependency<T> {

    override var updates: List<UpdateDependency<*>>? = null

    fun sendValue(transaction: Long, value: T) {
        buffer.setValue(transaction, value)
        logger.updateSource(this)
        updates?.forEach { it.update() }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this)
    }

}

class EventStreamRelay<T>(name: String, private val logger: FenjaSystemLogger) : NamedEventStream<T>(name), UpdateDependency<T> {

    private lateinit var source: EventStream<T>

    override fun getDependency(): Dependency<T>? {
        return if (this::source.isInitialized) source else null
    }

    fun setSource(source: EventStream<T>) {
        this.source = source
    }

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.eval()
            buffer.setValue(transaction, value)
            logger.executeUpdate(this)
        }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

}
