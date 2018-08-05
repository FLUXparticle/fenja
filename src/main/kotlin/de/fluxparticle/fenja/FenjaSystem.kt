package de.fluxparticle.fenja

import de.fluxparticle.fenja.dependency.NamedDependencyExtractor
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.expr.Expr
import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.NamedExpr
import de.fluxparticle.fenja.expr.OutputExpr
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.SilentFenjaSystemLogger
import de.fluxparticle.fenja.stream.EventStream
import de.fluxparticle.fenja.stream.EventStreamRelay
import de.fluxparticle.fenja.stream.EventStreamSource
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystem(private val logger: FenjaSystemLogger = SilentFenjaSystemLogger()) {

    private val names = HashSet<String>()

    private val sourceDependencies = TreeMap<String, SourceDependency<*>>()

    private val updateDependencies = TreeMap<String, UpdateDependency<*>>()

    private var finished: Boolean = false

    private val updates: MutableMap<String, MutableList<String>> = HashMap()

    fun <T> createEventStreamSource(name: String): EventStreamSource<T> {
        checkNotFinished()
        checkName(name)
        val eventStreamSource = EventStreamSource<T>(name, logger)
        sourceDependencies[name] = eventStreamSource
        return eventStreamSource
    }

    fun <T> createEventStreamRelay(name: String): EventStreamRelay<T> {
        checkNotFinished()
        checkName(name)
        val variable = EventStreamRelay<T>(name, logger)
        updateDependencies[name] = variable
        return variable
    }

    fun <T> createInputExpr(name: String): InputExpr<T> {
        checkNotFinished()
        checkName(name)
        val variable = InputExpr<T>(name, logger)
        sourceDependencies[name] = variable
        return variable
    }

    fun <T> createOutputExpr(name: String): OutputExpr<T> {
        checkNotFinished()
        checkName(name)
        val variable = OutputExpr<T>(name, logger)
        updateDependencies[name] = variable
        return variable
    }

    fun finish() {
        checkNotFinished()

        // TODO cycle detection

        updateDependencies.forEach { _, expr ->
            (expr.getDependency() ?: throw RuntimeException("variable " + expr.name + " is not ready"))
                    .accept(NamedDependencyExtractor())
                    .forEach { factor -> updates.getOrPut(factor.name) { ArrayList() }.add(expr.name) }
        }

        logger.ruleLists("updates", updates);

        sourceDependencies.forEach { _, source ->
            if (source is NamedExpr<*>) {
                source.value ?: throw RuntimeException("variable " + source.name + " does not have a value")
            }
            source.updates = TopologicalSorting().sort(source).result
        }

        sourceDependencies
                .values.fold(TopologicalSorting(), TopologicalSorting::sort)
                .result.forEach { it.update() }

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

        private val visited = HashSet<String>()

        internal fun sort(source: SourceDependency<*>): TopologicalSorting {
            updates[source.name]?.forEach { visit(it) }
            return this
        }

        private fun visit(name: String) {
            if (!visited.contains(name)) {
                updates[name]?.forEach { visit(it) }
                visited.add(name)
                result.addFirst(updateDependencies[name])
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

    inner class OutputExprDelegate<T> : ReadWriteProperty<Any, Expr<T>> {

        private var outputExpr: OutputExpr<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): Expr<T> {
            return getOutputExpr(property.name)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Expr<T>) {
            checkNotFinished()
            getOutputExpr(property.name).setRule(value)
        }

        private fun getOutputExpr(name: String): OutputExpr<T> {
            return outputExpr ?: createOutputExpr<T>(name).also {
                outputExpr = it
            }
        }

    }

    inner class EventStreamSourceDelegate<T> : ReadOnlyProperty<Any, EventStreamSource<T>> {

        private var eventStreamSource: EventStreamSource<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): EventStreamSource<T> {
            return eventStreamSource ?: createEventStreamSource<T>(property.name).also {
                eventStreamSource = it
            }
        }

    }

    inner class EventStreamRelayDelegate<T> : ReadWriteProperty<Any, EventStream<T>> {

        private var eventStreamRelay: EventStreamRelay<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): EventStream<T> {
            return getEventStreamRelay(property.name)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: EventStream<T>) {
            getEventStreamRelay(property.name).setSource(value)
        }

        private fun getEventStreamRelay(name: String): EventStreamRelay<T> {
            return eventStreamRelay ?: createEventStreamRelay<T>(name).also {
                eventStreamRelay = it
            }
        }

    }

}
