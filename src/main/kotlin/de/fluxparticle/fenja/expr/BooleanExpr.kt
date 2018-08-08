package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import kotlin.math.max

/**
 * Created by sreinck on 01.08.18.
 */
operator fun Expr<Boolean>.not() = NotExpr(this)

infix fun Expr<Boolean>.and(other: Expr<Boolean>) = AndExpr(this, other)

infix fun Expr<Boolean>.or(other: Expr<Boolean>) = OrExpr(this, other)

class NotExpr(
        argument: Expr<Boolean>
) : UpdateExpr<Boolean>() {

    override val dependency: UpdateDependency<Boolean> = NotDependency(argument.dependency)

    private class NotDependency(
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
}

class AndExpr(
        left: Expr<Boolean>,
        right: Expr<Boolean>
) : UpdateExpr<Boolean>() {

    override val dependency: UpdateDependency<Boolean> = AndDependency(left.dependency, right.dependency)

    private class AndDependency(
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
}

class OrExpr(
        left: Expr<Boolean>,
        right: Expr<Boolean>
) : UpdateExpr<Boolean>() {

    override val dependency: UpdateDependency<Boolean> = OrDependency(left.dependency, right.dependency)

    private class OrDependency(
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

}
