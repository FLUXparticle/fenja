package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

/**
 * Created by sreinck on 31.07.18.
 */
abstract class NamedExpr<T>(val name: String) : Expr<T>() {

    open var value: T? = null

    override fun eval(): T {
        return value!!
    }

}

class InputExpr<T>(name: String) : NamedExpr<T>(name) {

    var outputExpressions: List<OutputExpr<*>>? = null

    override var value: T?
        get() = super.value
        set(value) {
            super.value = value
            outputExpressions?.forEach { it.update() }
        }

    override fun toString(): String {
        return name
    }

    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visit(this)
    }

}

class OutputExpr<T>(name: String) : NamedExpr<T>(name) {

    var rule: Expr<T>? = null

    private val property: ObjectProperty<T> = SimpleObjectProperty()

    override var value: T?
        get() = super.value
        set(value) {
            super.value = value
            property.value = value
        }

    fun update() {
        value = rule?.eval()
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
