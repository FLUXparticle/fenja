package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.list.ForwardList
import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 03.08.18.
 */
class OperationList<T>(source: MutableList<T>, private val func: (Sequence<ListOperation<T>>) -> Unit) : ForwardList<T>(source) {

    override fun addForward(index: Int, element: T) {
        val list = mutableListOf<ListOperation<T>>()

        if (index > 0) {
            list.add(ListRetainOperation(index))
        }
        list.add(ListAddOperation(element))
        if (index < size()) {
            list.add(ListRetainOperation(size() - index))
        }

        func.invoke(list.asSequence())
    }

    override fun setForward(index: Int, element: T) {
        val list = mutableListOf<ListOperation<T>>()

        if (index > 0) {
            list.add(ListRetainOperation(index))
        }
//        list.add(ListRemoveOperation(get(index)))
//        list.add(ListAddOperation(element))
        list.add(ListSetOperation(get(index), element))
        if (index < size() - 1) {
            list.add(ListRetainOperation(size() - 1 - index))
        }

        func.invoke(list.asSequence())
    }

    override fun removeAtForward(index: Int) {
        val list = mutableListOf<ListOperation<T>>()

        if (index > 0) {
            list.add(ListRetainOperation(index))
        }
        list.add(ListRemoveOperation(get(index)))
        if (index < size() - 1) {
            list.add(ListRetainOperation(size() - 1 - index))
        }

        func.invoke(list.asSequence())
    }

}
