package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.list.ForwardList
import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 03.08.18.
 */
class OperationList<T>(source: MutableList<T>, private val func: (ListOperation<T>) -> Unit) : ForwardList<T>(source) {

    override fun addForward(index: Int, element: T) {
        val list = mutableListOf<ListComponent<T>>()

        if (index > 0) {
            list.add(ListRetainComponent(index))
        }
        list.add(ListAddComponent(element))
        if (index < size) {
            list.add(ListRetainComponent(size - index))
        }

        func.invoke(ListOperation(list))
    }

    override fun setForward(index: Int, element: T) {
        val list = mutableListOf<ListComponent<T>>()

        if (index > 0) {
            list.add(ListRetainComponent(index))
        }
        list.add(ListSetComponent(get(index), element))
        if (index < size - 1) {
            list.add(ListRetainComponent(size - 1 - index))
        }

        func.invoke(ListOperation(list))
    }

    override fun removeAtForward(index: Int) {
        val list = mutableListOf<ListComponent<T>>()

        if (index > 0) {
            list.add(ListRetainComponent(index))
        }
        list.add(ListRemoveComponent(get(index)))
        if (index < size - 1) {
            list.add(ListRetainComponent(size - 1 - index))
        }

        func.invoke(ListOperation(list))
    }

}
