package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 31.07.18.
 */
abstract class NamedExpr<T>(val name: String) : Expr<T>() {

    internal val property: ObjectProperty<T> = SimpleObjectProperty()

    open var value: T
        get() = property.value
        set(value) {
            property.value = value
        }

    override fun eval(): T {
        return value
    }

}

class InputExpr<T>(name: String) : NamedExpr<T>(name) {

    var outputExpressions: List<OutputExpr<*>>? = null

    override var value: T
        get() = super.value
        set(value) {
            super.value = value
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

class OutputExpr<T>(name: String) : NamedExpr<T>(name) {

    internal var rule: Expr<T>? = null

    fun update() {
        value = rule!!.eval()
    }

    override fun toString(): String {
        return rule?.toString() ?: name
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
