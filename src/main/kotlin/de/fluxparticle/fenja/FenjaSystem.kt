package de.fluxparticle.fenja

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.UpdateExpr
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.SilentFenjaSystemLogger
import de.fluxparticle.fenja.stream.InputEventStream
import de.fluxparticle.fenja.stream.TransactionProvider
import de.fluxparticle.fenja.stream.UpdateEventStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystem(private val logger: FenjaSystemLogger = SilentFenjaSystemLogger()) {

    private val transactionProvider = TransactionProvider()

    private val names = HashSet<String>()

    private val sourceDependencies = TreeMap<String, SourceDependency<*>>()

    private val updateDependencies = TreeMap<String, UpdateDependency<*>>()

    private var finished: Boolean = false

    private val updates: MutableMap<Dependency<*>, MutableList<UpdateDependency<*>>> = HashMap()

    fun <T> createInputExpr(name: String): InputExpr<T> {
        checkNotFinished()
        checkName(name)
        val inputExpr = InputExpr<T>(name, transactionProvider, logger)
        sourceDependencies[name] = inputExpr.dependency
        return inputExpr
    }

    fun <T> createInputEventStream(name: String): InputEventStream<T> {
        checkNotFinished()
        checkName(name)
        val eventStreamSource = InputEventStream<T>(name, transactionProvider, logger)
        sourceDependencies[name] = eventStreamSource.dependency
        return eventStreamSource
    }

    private fun <T> createUpdateDependency(name: String, dependency: UpdateDependency<T>) {
        checkNotFinished()
        checkName(name)
        dependency.name = name
        updateDependencies[name] = dependency
    }

    fun finish() {
        checkNotFinished()

        // TODO cycle detection

        val visited = HashSet<UpdateDependency<*>>()
        updateDependencies.forEach { _, expr ->
            val queue = LinkedList<UpdateDependency<*>>()
            queue.add(expr)
            while (queue.isNotEmpty()) {
                val element = queue.remove()
                if (!visited.contains(element)) {
                    visited.add(element)
                    val dependencies = element.getDependencies()
                    dependencies.forEach { dep ->
                        updates.getOrPut(dep) { ArrayList() }.add(element)
                        if (dep is UpdateDependency<*>) {
                            queue.add(dep)
                        }
                    }
                }
            }
        }

        logger.ruleLists("updates", updates);

        sourceDependencies.forEach { _, source ->
            if (source is InputExpr<*> && source.getTransaction() < 0) {
                throw RuntimeException("variable " + source.name + " does not have a value")
            }
            source.updates = TopologicalSorting().sort(source).result
        }

        sourceDependencies
                .values.fold(TopologicalSorting(), TopologicalSorting::sort)
                .result.forEach {
            it.update()
            logger.executeUpdate(it)
            it.updateLoop()
        }

        finished = true
    }

    private fun checkNotFinished() {
        if (finished) {
            throw IllegalStateException("already finished")
        }
    }

    private fun checkName(name: String) {
        if (names.contains(name)) {
            throw IllegalArgumentException("Variable $name already exists")
        }
        names.add(name)
    }

    private inner class TopologicalSorting {

        val result = LinkedList<UpdateDependency<*>>()

        private val visited = HashSet<UpdateDependency<*>>()

        internal fun sort(source: SourceDependency<*>): TopologicalSorting {
            updates[source]?.forEach { visit(it) }
            return this
        }

        private fun visit(updateDependency: UpdateDependency<*>) {
            if (!visited.contains(updateDependency)) {
                updates[updateDependency]?.forEach { visit(it) }
                visited.add(updateDependency)
                result.addFirst(updateDependency)
            }
        }

    }

    inner class InputExprDelegate<T> : ReadOnlyProperty<Any, InputExpr<T>> {

        private var inputExpr: InputExpr<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): InputExpr<T> {
            return inputExpr ?: createInputExpr<T>(property.name).also {
                inputExpr = it
            }
        }

    }

    inner class InputEventStreamDelegate<T> : ReadOnlyProperty<Any, InputEventStream<T>> {

        private var sourceEventStream: InputEventStream<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): InputEventStream<T> {
            return sourceEventStream ?: createInputEventStream<T>(property.name).also {
                sourceEventStream = it
            }
        }

    }

    inner class UpdateExprDelegate<E : UpdateExpr<T>, T> : ReadWriteProperty<Any, E> {

        private lateinit var updateDependency: E

        override fun getValue(thisRef: Any, property: KProperty<*>): E {
            return updateDependency
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: E) {
            checkNotFinished()
            if (this::updateDependency.isInitialized) {
                throw IllegalStateException("already assigned")
            }
            createUpdateDependency(property.name, value.dependency)
            updateDependency = value
        }

    }

    inner class UpdateEventStreamDelegate<E : UpdateEventStream<T>, T> : ReadWriteProperty<Any, E> {

        private lateinit var updateDependency: E

        override fun getValue(thisRef: Any, property: KProperty<*>): E {
            return updateDependency
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: E) {
            checkNotFinished()
            if (this::updateDependency.isInitialized) {
                throw IllegalStateException("already assigned")
            }
            createUpdateDependency(property.name, value.dependency)
            updateDependency = value
        }

    }

}
