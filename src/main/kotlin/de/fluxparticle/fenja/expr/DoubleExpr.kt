package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import kotlin.math.max

/**
 * Created by sreinck on 31.07.18.
 */
internal class NegDependency(
        private val argument: Dependency<Double>
) : UpdateDependency<Double>() {

    override fun update() {
        val transaction = argument.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = argument.getValue()
            val negValue = -value
            buffer.setValue(transaction, negValue)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(argument)
    }

    override fun toUpdateString(): String {
        return "-$argument"
    }
}

internal class PlusDependency(
        private val left: Dependency<Double>,
        private val right: Dependency<Double>
) : UpdateDependency<Double>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val result = valueA + valueB
            buffer.setValue(transaction, result)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left + $right"
    }
}

internal class MinusDependency(
        private val left: Dependency<Double>,
        private val right: Dependency<Double>
) : UpdateDependency<Double>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val result = valueA - valueB
            buffer.setValue(transaction, result)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left - $right"
    }
}

internal class TimesDependency(
        private val left: Dependency<Double>,
        private val right: Dependency<Double>
) : UpdateDependency<Double>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val result = valueA * valueB
            buffer.setValue(transaction, result)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left * $right"
    }

}

internal class DivDependency(
        private val left: Dependency<Double>,
        private val right: Dependency<Double>
) : UpdateDependency<Double>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val result = valueA / valueB
            buffer.setValue(transaction, result)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left / $right"
    }

}

internal class MinDependency(
        private val arguments: Sequence<Dependency<Double>>
) : UpdateDependency<Double>() {

    override fun getDependencies(): Sequence<Dependency<*>> {
        return arguments
    }

    override fun update() {
        val transaction = arguments.map { it.getTransaction() }.max()!!
        if (transaction > buffer.getTransaction()) {
            val value = arguments.map { it.getValue() }.min()!!
            buffer.setValue(transaction, value)
        }
    }

    override fun toUpdateString(): String {
        return "min ${arguments.toList()}"
    }

}

internal class MaxDependency(
        private val arguments: Sequence<Dependency<Double>>
) : UpdateDependency<Double>() {

    override fun getDependencies(): Sequence<Dependency<*>> {
        return arguments
    }

    override fun update() {
        // TODO den Fall, dass die Liste leer ist, besser behandeln
        val transaction = arguments.map { it.getTransaction() }.max() ?: 0
        if (transaction > buffer.getTransaction()) {
            val value = arguments.map { it.getValue() }.max() ?: 0.0
            buffer.setValue(transaction, value)
        }
    }

    override fun toUpdateString(): String {
        return "max ${arguments.toList()}"
    }

}
