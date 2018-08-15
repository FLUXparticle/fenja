package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import kotlin.math.max

/**
 * Created by sreinck on 31.07.18.
 */
operator fun Expr<Double>.unaryMinus() = NegateExpr(this)

operator fun Expr<Double>.plus(other: Expr<Double>) = PlusExpr(this, other)

operator fun Expr<Double>.plus(other: Double) = plus(ConstExpr(other))

operator fun Expr<Double>.minus(other: Expr<Double>) = MinusExpr(this, other)

operator fun Expr<Double>.minus(other: Double) = minus(ConstExpr(other))

operator fun Expr<Double>.times(other: Expr<Double>) = TimesExpr(this, other)

operator fun Expr<Double>.times(other: Double) = times(ConstExpr(other))

operator fun Expr<Double>.div(other: Expr<Double>) = DivExpr(this, other)

operator fun Expr<Double>.div(other: Double) = div(ConstExpr(other))

class NegateExpr(
        argument: Expr<Double>
) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = NegDependency(argument.dependency)

    private class NegDependency(
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

}

class PlusExpr(
        left: Expr<Double>,
        right: Expr<Double>
) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = PlusDependency(left.dependency, right.dependency)

    private class PlusDependency(
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

}

class MinusExpr(
        left: Expr<Double>,
        right: Expr<Double>
) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = MinusDependency(left.dependency, right.dependency)

    private class MinusDependency(
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

}

class TimesExpr(
        left: Expr<Double>,
        right: Expr<Double>
) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = TimesDependency(left.dependency, right.dependency)

    private class TimesDependency(
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

}

class DivExpr(
        left: Expr<Double>,
        right: Expr<Double>
) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = DivDependency(left.dependency, right.dependency)

    private class DivDependency(
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

}

class MinExpr(arguments: Sequence<Expr<Double>>) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = MinDependency(arguments.map { it.dependency })

    private class MinDependency(
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

}

class MaxExpr(arguments: Sequence<Expr<Double>>) : UpdateExpr<Double>() {

    override val dependency: UpdateDependency<Double> = MaxDependency(arguments.map { it.dependency })

    private class MaxDependency(
            private val arguments: Sequence<Dependency<Double>>
    ) : UpdateDependency<Double>() {

        override fun getDependencies(): Sequence<Dependency<*>> {
            return arguments
        }

        override fun update() {
            val transaction = arguments.map { it.getTransaction() }.max()!!
            if (transaction > buffer.getTransaction()) {
                val value = arguments.map { it.getValue() }.max()!!
                buffer.setValue(transaction, value)
            }
        }

        override fun toUpdateString(): String {
            return "max ${arguments.toList()}"
        }

    }

}
