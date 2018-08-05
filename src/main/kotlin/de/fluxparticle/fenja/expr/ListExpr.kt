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

    private var mutableList = initList.toMutableList()

    internal val list: List<T>
        get() = mutableList

    internal fun replaceMutableList(mutableList: MutableList<T>) {
        mutableList.clear()
        mutableList.addAll(this.mutableList)
        this.mutableList = mutableList
    }

    override fun eval(): List<T> {
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {
            val value = source.eval()
            value.apply(ListOperationApplier(mutableList))
            lastTransaction = transaction
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

private fun <T> Expr<List<T>>.asListExpr(): ListExpr<T> {
    return when (this) {
        is OutputExpr -> getDependency() as ListExpr<T>
        else -> this as ListExpr<T>
    }
}

fun <T> Expr<List<T>>.buildAddOperation(value: T): ListOperation<T> {
    val listExpr = asListExpr()

    val index = listExpr.list.size

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListAddComponent(value))

    return ListOperation(result)
}

fun <T> Expr<List<T>>.buildAddOperation(index: Int, value: T): ListOperation<T> {
    val listExpr = asListExpr()

    val size = listExpr.list.size

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListAddComponent(value))
    if (index < size) {
        result.add(ListRetainComponent(size - index))
    }

    return ListOperation(result)
}

fun <T> Expr<List<T>>.buildSetOperation(index: Int, newValue: T): ListOperation<T> {
    val listExpr = asListExpr()

    val size = listExpr.list.size

    if (index < 0 || index >= size) {
        throw IndexOutOfBoundsException("index: $index size: $size")
    }

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListSetComponent(listExpr.list[index], newValue))
    if (index < size - 1) {
        result.add(ListRetainComponent(size - 1 - index))
    }

    return ListOperation(result)
}

fun <T> Expr<List<T>>.buildRemoveOperation(index: Int): ListOperation<T> {
    val listExpr = asListExpr()

    val size = listExpr.list.size

    if (index < 0 || index >= size) {
        throw IndexOutOfBoundsException("index: $index size: $size")
    }

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListRemoveComponent(listExpr.list[index]))
    if (index < size - 1) {
        result.add(ListRetainComponent(size - 1 - index))
    }

    return ListOperation(result)
}

// TODO Effekte nach außen erst ganz zum Schluss ausführen oder erneutes update verzögern
infix fun <T> MutableList<T>.bind(expr: Expr<List<T>>) {
    val listExpr = expr.asListExpr()
    listExpr.replaceMutableList(this)
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
