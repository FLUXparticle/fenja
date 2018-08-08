package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.list.DelegatedList
import de.fluxparticle.fenja.list.LoopList
import de.fluxparticle.fenja.list.ReadList
import de.fluxparticle.fenja.list.ReadWriteList
import de.fluxparticle.fenja.operation.*
import de.fluxparticle.fenja.stream.EventStream
import de.fluxparticle.fenja.stream.InitEventStream

/**
 * Created by sreinck on 31.07.18.
 */
abstract class ListExpr<T> internal constructor() : UpdateExpr<List<T>>() {

    abstract override val dependency: ListDependency<T>

    internal abstract val source: EventStream<ListOperation<T>>

    protected val list: ReadList<T>
        get() = dependency.list

    infix fun filter(predicateExpr: Expr<(T) -> Boolean>): FilterListExpr<T> {
        return FilterListExpr(source, predicateExpr)
    }

    fun buildAddOperation(value: T): ListOperation<T> {
        val index = list.size

        val result = mutableListOf<ListComponent<T>>()
        if (index > 0) {
            result.add(ListRetainComponent(index))
        }
        result.add(ListAddComponent(value))

        return ListOperation(result)
    }

    fun buildAddOperation(index: Int, value: T): ListOperation<T> {
        val size = list.size

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

    fun buildSetOperation(index: Int, newValue: T): ListOperation<T> {
        val size = list.size

        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index: $index size: $size")
        }

        val result = mutableListOf<ListComponent<T>>()
        if (index > 0) {
            result.add(ListRetainComponent(index))
        }
        result.add(ListSetComponent(list.get(index), newValue))
        if (index < size - 1) {
            result.add(ListRetainComponent(size - 1 - index))
        }

        return ListOperation(result)
    }

    fun buildRemoveOperation(index: Int): ListOperation<T> {
        val size = list.size

        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index: $index size: $size")
        }

        val result = mutableListOf<ListComponent<T>>()
        if (index > 0) {
            result.add(ListRetainComponent(index))
        }
        result.add(ListRemoveComponent(list.get(index)))
        if (index < size - 1) {
            result.add(ListRetainComponent(size - 1 - index))
        }

        return ListOperation(result)
    }

}

internal abstract class ListDependency<T>(
        internal val source: Dependency<ListOperation<T>>
) : UpdateDependency<List<T>>() {

    private val loopList = LoopList<T>()

    internal val list: ReadList<T>
        get() = loopList

    internal fun loopList(list: ReadWriteList<T>) {
        loopList.loop(list)
    }

    init {
        buffer.setValue(-1L, LoopList())
    }

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {

            val value = source.getValue()
            val readWriteList = buffer.getValue() as ReadWriteList<T>
            value.apply(ReadWriteListAdapter(readWriteList))
            buffer.setValue(transaction, readWriteList)

        }
    }

    override fun updateLoop() {
        super.updateLoop()
        val value = source.getValue()
        value.apply(ReadWriteListAdapter(loopList))
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source)
    }

    override fun toUpdateString(): String {
        return source.toString()
    }

}

class HoldListExpr<T> internal constructor(
        override val source: EventStream<ListOperation<T>>
): ListExpr<T>() {

    override val dependency: ListDependency<T> = HoldListDependency(source.dependency)

    private class HoldListDependency<T>(source: Dependency<ListOperation<T>>) : ListDependency<T>(source) {

    }

}

infix fun <T> EventStream<ListOperation<T>>.hold(initList: List<T>): ListExpr<T> {
    val initEvent = ListOperation(initList.map { ListAddComponent(it) })
    val source = InitEventStream(this, initEvent)
    return HoldListExpr(source)
}

infix fun <T> MutableList<T>.bind(listExpr: ListExpr<T>) {
    listExpr.dependency.loopList(DelegatedList(this))
}

/*
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
*/

/*
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
*/
