package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.*
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 31.07.18.
 */
abstract class NamedExpr<T>(override val name: String) : Expr<T>(), NamedDependency<T> {

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

class InputExpr<T>(name: String, private val logger: FenjaSystemLogger) : NamedExpr<T>(name), SourceDependency<T> {

    override var updates: List<UpdateDependency<*>>? = null

    override var value: T
        get() = super.value
        set(value) {
            super.value = value
            logger.updateSource(this)
            updates?.forEach { it.update() }
        }

    infix fun bind(observableValue: ObservableValue<T>) {
        value = observableValue.value
        observableValue.addListener { _, _, newValue -> value = newValue }
    }

    override fun toString(): String {
        return name
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this)
    }

}

class OutputExpr<T>(name: String, private val logger: FenjaSystemLogger) : NamedExpr<T>(name), UpdateDependency<T> {

    private lateinit var rule: Expr<T>

    public override val property: ObjectProperty<T>
        get() = super.property

    override fun getDependency(): Dependency<T>? {
        return if (this::rule.isInitialized) rule else null
    }

    fun setRule(rule: Expr<T>) {
        this.rule = rule
    }

    override fun update() {
        value = rule.eval()
        logger.executeUpdate(this)
    }

    override fun toString(): String {
        return name
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, rule)
    }

}

infix fun <T> Property<T>.bind(expr: Expr<T>) {
    when (expr) {
        is OutputExpr -> bind(expr.property)
        else -> throw RuntimeException("only OutputExpr can be bound")
    }
}
