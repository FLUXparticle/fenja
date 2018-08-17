package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem.ListExpr
import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.list.DelegatedList
import de.fluxparticle.fenja.list.LoopList
import de.fluxparticle.fenja.list.ReadList
import de.fluxparticle.fenja.list.ReadWriteList
import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 31.07.18.
 */
fun <T> ListExpr<T>.buildAddOperation(value: T): ListOperation<T> {
    val index = list.size

    val result = mutableListOf<ListComponent<T>>()
    if (index > 0) {
        result.add(ListRetainComponent(index))
    }
    result.add(ListAddComponent(value))

    return ListOperation(result)
}

fun <T> ListExpr<T>.buildAddOperation(index: Int, value: T): ListOperation<T> {
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

fun <T> ListExpr<T>.buildSetOperation(index: Int, newValue: T): ListOperation<T> {
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

fun <T> ListExpr<T>.buildRemoveOperation(index: Int): ListOperation<T> {
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

internal class ListDependency<T>(
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

infix fun <T> MutableList<T>.bind(listExpr: ListExpr<T>) {
    listExpr.dependency.loopList(DelegatedList(this))
}
