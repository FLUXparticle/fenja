package de.fluxparticle.fenja.dependency

import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.stream.TransactionProvider

/**
 * Created by sreinck on 05.08.18.
 */
internal sealed class Dependency<T> {

    protected val buffer = Buffer<T>()

    fun getTransaction() = buffer.getTransaction()

    fun getValue(): T {
        if (getTransaction() < 0) {
            throw IllegalStateException("'${toString()}' is not set")
        }
        return buffer.getValue()
    }

    abstract fun getDependencies(): Sequence<Dependency<*>>

    abstract override fun toString(): String

}

internal class NoDependency<T> : UpdateDependency<T>() {

    override fun getDependencies(): Sequence<Dependency<*>> {
        return emptySequence()
    }

    override fun update() {
        // empty
    }

    override fun toUpdateString(): String {
        return "_"
    }

}

internal class SourceDependency<T>(
        val name: String,
        private val transactionProvider: TransactionProvider,
        private val logger: FenjaSystemLogger
) : Dependency<T>() {

    internal var updates: List<UpdateDependency<*>>? = null

    val isSet: Boolean
        get() = buffer.getTransaction() >= 0

    fun executeUpdates(value: T) {
        val transaction = transactionProvider.newTransaction()
        buffer.setValue(transaction, value)
        updates?.let { updates ->
            logger.updateSource(this)
            updates.forEach {
                it.update()
                logger.executeUpdate(it)
            }
            updates.forEach {
                it.updateLoop()
            }
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return emptySequence()
    }

    override fun toString(): String {
        return name
    }

}

internal abstract class UpdateDependency<T> : Dependency<T>() {

    var name: String? = null

    abstract fun update()

    open fun updateLoop() {
        // empty
    }

    abstract fun toUpdateString(): String

    final override fun toString(): String {
        return name ?: toUpdateString()
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
