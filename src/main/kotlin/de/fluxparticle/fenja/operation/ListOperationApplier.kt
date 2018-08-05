package de.fluxparticle.fenja.operation

class ListOperationApplier<T>(private val mutableList: MutableList<T>) : ListOperationHandler<T> {

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
