package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.DependencyVisitor
import de.fluxparticle.fenja.operation.*
import de.fluxparticle.fenja.stream.EventStream

/**
 * Created by sreinck on 31.07.18.
 */
class ListExpr<T> internal constructor(
        private val source: EventStream<ListOperation<T>>,
        initList: List<T>
): Expr<List<T>>() {

    private var lastTransaction: Long = 0

    private val mutableList = initList.toMutableList()

    internal val list: List<T>
        get() = mutableList

    inner class ListOperationApplier : ListOperationHandler<T> {

        private var index: Int = 0

        override fun add(value: T) {
            mutableList.add(index++, value)
        }

        override fun set(oldValue: T, newValue: T) {
            mutableList[index++] = newValue
        }

        override fun remove(oldValue: T) {
            mutableList.removeAt(index)
        }

        override fun retain(count: Int) {
            index += count
        }

    }

    override fun eval(): List<T> {
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {
            val value = source.eval()
            value.apply(ListOperationApplier())
        }
        return mutableList
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

    override fun toString(): String {
        return source.toString()
    }

}

infix fun <T> EventStream<ListOperation<T>>.hold(initList: List<T>): ListExpr<T> {
    return ListExpr(this, initList)
}

fun <T> Expr<List<T>>.buildAddOperation(value: T): ListOperation<T> {
    val listExpr = when (this) {
        is OutputExpr -> getDependency() as ListExpr<T>
        else -> this as ListExpr<T>
    }

    val index = listExpr.list.size

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListAddComponent(value))

    return ListOperation(result)
}

class MinExpr(private val arguments: Iterable<Expr<Double>>) : Expr<Double>() {

    override fun eval(): Double {
        return arguments.map { it.eval() }.min() ?: 0.0
    }

    override fun toString(): String {
        return "min ${arguments.toList()}"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, *arguments.toList().toTypedArray())
    }

}

class MaxExpr(private val arguments: Iterable<Expr<Double>>) : Expr<Double>() {

    override fun eval(): Double {
        return arguments.map { it.eval() }.max() ?: 0.0
    }

    override fun toString(): String {
        return "max ${arguments.toList()}"
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, *arguments.toList().toTypedArray())
    }

}
