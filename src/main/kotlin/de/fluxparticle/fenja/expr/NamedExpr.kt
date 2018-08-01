package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.logger.FenjaSystemLogger
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 31.07.18.
 */
abstract class NamedExpr<T>(val name: String) : Expr<T>() {

    internal open val property: ObjectProperty<T> = SimpleObjectProperty()

    open var value: T
        get() = property.value
        set(value) {
            property.value = value
        }

    override fun eval(): T {
        return value
    }

}

class InputExpr<T>(name: String, private val logger: FenjaSystemLogger) : NamedExpr<T>(name) {

    var outputExpressions: List<OutputExpr<*>>? = null

    override var value: T
        get() = super.value
        set(value) {
            super.value = value
            logger.updateVariable(this)
            outputExpressions?.forEach { it.update() }
        }

    infix fun bind(observableValue: ObservableValue<T>) {
        value = observableValue.value
        observableValue.addListener { _, _, newValue -> value = newValue }
    }

    override fun toString(): String {
        return name
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this)
    }

}

class OutputExpr<T>(name: String, private val logger: FenjaSystemLogger) : NamedExpr<T>(name) {

    public override val property: ObjectProperty<T>
        get() = super.property

    var rule: Expr<T>? = null

    fun update() {
        value = rule!!.eval()
        logger.evaluateRule(this)
    }

    override fun toString(): String {
        return name
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return rule
                ?.let { visitor.visit(this, it) }
                ?: visitor.visit(this)
    }

}

infix fun <T> Property<T>.bind(expr: Expr<T>) {
    when (expr) {
        is OutputExpr -> bind(expr.property)
        else -> throw RuntimeException("only OutputExpr can be bound")
    }
}
