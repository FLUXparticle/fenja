package de.fluxparticle.fenja.expr

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 31.07.18.
 */
class OutputExprDelegate<T> : ReadWriteProperty<Any, Expr<T>> {

    lateinit var outputExpr: OutputExpr<T>

    override fun getValue(thisRef: Any, property: KProperty<*>): OutputExpr<T> {
        if (!::outputExpr.isInitialized) {
            outputExpr = OutputExpr(property.name)
        }
        return outputExpr
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Expr<T>) {
        outputExpr = OutputExpr(property.name)
        outputExpr.rule = value
    }

}
