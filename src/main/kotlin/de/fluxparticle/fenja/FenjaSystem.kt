package de.fluxparticle.fenja

import de.fluxparticle.fenja.expr.*
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.SilentFenjaSystemLogger
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

    private val inputExpressions = TreeMap<String, InputExpr<*>>()

    private val outputExpressions = TreeMap<String, OutputExpr<*>>()

    private var finished: Boolean = false

    private val updates: MutableMap<String, MutableList<String>> = HashMap()

    fun <T> createInputExpr(name: String): InputExpr<T> {
        checkNotFinished()
        checkName(name)
        val variable = InputExpr<T>(name, logger)
        inputExpressions[name] = variable
        return variable
    }

    fun <T> createOutputExpr(name: String): OutputExpr<T> {
        checkNotFinished()
        checkName(name)
        val variable = OutputExpr<T>(name, logger)
        outputExpressions[name] = variable
        return variable
    }

    fun finish() {
        checkNotFinished()

        // TODO cycle detection

        outputExpressions.forEach { _, expr ->
            (expr.rule ?: throw RuntimeException("variable " + expr.name + " does not have a rule"))
                    .accept(NamedExprExtractor())
                    .forEach { factor -> updates.getOrPut(factor.name) { ArrayList() }.add(expr.name) }
        }

        logger.ruleLists("updates", updates);

        inputExpressions.forEach { _ , expr ->
            expr.value ?: throw RuntimeException("variable " + expr.name + " does not have a value")
            expr.outputExpressions = TopologicalSorting().sort(expr).result
        }

        inputExpressions
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

        val result = LinkedList<OutputExpr<*>>()

        private val visited = HashSet<String>()

        internal fun sort(expr: NamedExpr<*>): TopologicalSorting {
            updates[expr.name]?.forEach { visit(it) }
            return this
        }

        private fun visit(name: String) {
            if (!visited.contains(name)) {
                updates[name]?.forEach { visit(it) }
                visited.add(name)
                result.addFirst(outputExpressions[name])
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
            getOutputExpr(property.name).rule = value
        }

        private fun getOutputExpr(name: String): OutputExpr<T> {
            return outputExpr ?: createOutputExpr<T>(name).also {
                outputExpr = it
            }
        }

    }

}
