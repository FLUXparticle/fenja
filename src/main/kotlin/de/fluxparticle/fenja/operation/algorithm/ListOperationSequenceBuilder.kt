package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

class ListOperationSequenceBuilder<T> : BuildingListOperationVisitor<T, Sequence<ListOperation<T>>, Void?> {

    private val result = mutableListOf<ListOperation<T>>()

    private var retain: Int = 0

    private fun flush() {
        if (retain > 0) {
            result.add(ListRetainOperation(retain))
            retain = 0
        }
    }

    override fun visitAddOperation(value: T, data: Void?) {
        flush()
        result.add(ListAddOperation(value))
    }

    override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
        flush()
        result.add(ListSetOperation(oldValue, newValue))
    }

    override fun visitRemoveOperation(oldValue: T, data: Void?) {
        flush()
        result.add(ListRemoveOperation(oldValue))
    }

    override fun visitRetainOperation(count: Int, data: Void?) {
        retain += count
    }

    override fun build(): Sequence<ListOperation<T>> {
        flush()
        return result.asSequence()
    }

}
