package de.fluxparticle.fenja.operation

import de.fluxparticle.fenja.list.ReadWriteList

class ReadWriteListAdapter<T>(private val list: ReadWriteList<T>) : ListOperationHandler<T> {

    private var index: Int = 0

    override fun add(value: T) {
        list.add(index++, value)
    }

    override fun set(oldValue: T, newValue: T) {
        list.set(index++, newValue)
    }

    override fun remove(oldValue: T) {
        list.removeAt(index)
    }

    override fun retain(count: Int) {
        index += count
    }

}
