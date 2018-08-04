package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

class ListOperationSequenceBuilder<T> : BuildingListOperationVisitor<T, Sequence<ListOperation<T>>, Void?> {

    private abstract inner class Cache {

        open fun add(value: T) {
            flush()
            result.add(ListAddOperation(value))
            cache = emptyCache
        }

        fun remove(oldValue : T) {
            flush()
            cache = RemoveOperationCache(oldValue)
        }

        open fun retain(count: Int) {
            flush()
            cache = RetainOperationCache(count)
        }

        abstract fun flush()

    }

    private val emptyCache: Cache = object : Cache() {

        override fun flush() {}

    }

    private inner class RetainOperationCache(private var distance: Int) : Cache() {

        override fun retain(count: Int) {
            this.distance += count
        }

        override fun flush() {
            result.add(ListRetainOperation(distance))
            cache = emptyCache
        }

    }

    private inner class RemoveOperationCache(private val oldValue : T) : Cache() {

        override fun add(value: T) {
            result.add(ListSetOperation(oldValue, value))
            cache = emptyCache
        }

        override fun flush() {
            result.add(ListRemoveOperation(oldValue))
            cache = emptyCache
        }

    }

    private var cache = emptyCache

    private val result = mutableListOf<ListOperation<T>>()

    override fun visitAddOperation(value: T, data: Void?) {
        cache.add(value)
    }

    override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
        cache.flush()
        result.add(ListSetOperation(oldValue, newValue))
    }

    override fun visitRemoveOperation(oldValue: T, data: Void?) {
        cache.remove(oldValue)
    }

    override fun visitRetainOperation(count: Int, data: Void?) {
        cache.retain(count)
    }

    override fun build(): Sequence<ListOperation<T>> {
        cache.flush()
        return result.asSequence()
    }

}
