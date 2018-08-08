package de.fluxparticle.fenja.dependency

import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.stream.TransactionProvider
import de.fluxparticle.fenja.value.LoopValue
import de.fluxparticle.fenja.value.ReadWriteValue

/**
 * Created by sreinck on 05.08.18.
 */
internal sealed class Dependency<T> {

    protected val buffer = Buffer<T>()

    fun getTransaction() = buffer.getTransaction()

    fun getValue() = buffer.getValue()

    abstract fun getDependencyNames(): Sequence<String>

    abstract fun getDependencies(): Sequence<Dependency<*>>

    abstract override fun toString(): String

}

internal abstract class UpdateDependency<T> : Dependency<T>() {

    var name: String? = null

    private val loopValue = LoopValue<T>()

    final override fun getDependencyNames(): Sequence<String> {
        return name
                ?.let { sequenceOf(it) }
                ?: getDependencies().flatMap { it.getDependencyNames() }
    }

    abstract fun update()

    fun loop(destination: ReadWriteValue<T>) {
        loopValue.loop(destination)
    }

    open fun updateLoop() {
        loopValue.value = buffer.getValue()
    }

    abstract fun toUpdateString(): String

    final override fun toString(): String {
        return name ?: toUpdateString()
    }

}

internal class SourceDependency<T>(
        val name: String,
        private val transactionProvider: TransactionProvider,
        private val logger: FenjaSystemLogger
) : Dependency<T>() {

    internal var updates: List<UpdateDependency<*>>? = null

    fun executeUpdates(value: T) {
        val transaction = transactionProvider.newTransaction()
        buffer.setValue(transaction, value)
        logger.updateSource(this)
        updates?.forEach {
            it.update()
            logger.executeUpdate(it)
        }
        updates?.forEach {
            it.updateLoop()
        }
    }

    override fun getDependencyNames(): Sequence<String> {
        return sequenceOf(name)
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return emptySequence()
    }

    override fun toString(): String {
        return name
    }

}

internal class MapDependency<T, R>(private val source: Dependency<T>, private val func: (T) -> R) : UpdateDependency<R>() {

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.getValue()
            val mapped = func.invoke(value)
            buffer.setValue(transaction, mapped)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source)
    }

    override fun toUpdateString(): String {
        return "$source map {}"
    }

}

/*
abstract class SourceDependency<T>(
        val name: String,
        private val transactionProvider: TransactionProvider,
        private val logger: FenjaSystemLogger
) : Dependency<T>() {

    var updates: List<UpdateDependency<*>>? = null

    fun sendValue(value: T) {
        val transaction = transactionProvider.newTransaction()
        buffer.setValue(transaction, value)
        logger.updateSource(this, value)
        updates?.forEach { it.update() }
    }

    final override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return name
    }

}
*/
