package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

/**
 * Created by sreinck on 31.07.18.
 */
abstract class NamedExpr<T>(protected val name: String) : Expr<T>() {

}

class OutputExpr<T>(name: String, initValue: T? = null) : NamedExpr<T>(name) {

    lateinit var rule: Expr<T>

    private val property: ObjectProperty<T> = SimpleObjectProperty(initValue)

    fun setValue(value: T) {
        property.value = value
    }

    override fun <R, D> accept(visitor: ExprVisitor<T, R, D>, data: D): R {
        val rule = if (::rule.isInitialized) rule else null
        return visitor.visitOutputExpr(name, rule, property, data)
    }

}
