package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import kotlin.math.max

/**
 * Created by sreinck on 01.08.18.
 */
internal class NotDependency(
        private val argument: Dependency<Boolean>
) : UpdateDependency<Boolean>() {

    override fun update() {
        val transaction = argument.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = argument.getValue()
            val notValue = !value
            buffer.setValue(transaction, notValue)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(argument)
    }

    override fun toUpdateString(): String {
        return "!$argument"
    }

}

internal class AndDependency(
        private val left: Dependency<Boolean>,
        private val right: Dependency<Boolean>
) : UpdateDependency<Boolean>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val combined = valueA and valueB
            buffer.setValue(transaction, combined)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left and $right"
    }
}

internal class OrDependency(
        private val left: Dependency<Boolean>,
        private val right: Dependency<Boolean>
) : UpdateDependency<Boolean>() {

    override fun update() {
        val transactionA = left.getTransaction()
        val transactionB = right.getTransaction()
        val transaction = max(transactionA, transactionB)
        if (transaction > buffer.getTransaction()) {
            val valueA = left.getValue()
            val valueB = right.getValue()
            val combined = valueA or valueB
            buffer.setValue(transaction, combined)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(left, right)
    }

    override fun toUpdateString(): String {
        return "$left or $right"
    }

}
