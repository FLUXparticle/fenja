package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

class ListOperationSequenceBuilder<T> : BuildingListOperationHandler<T, ListOperation<T>> {

    private abstract inner class Cache {

        open fun add(value: T) {
            flush()
            result.add(ListAddComponent(value))
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
            result.add(ListRetainComponent(distance))
            cache = emptyCache
        }

    }

    private inner class RemoveOperationCache(private val oldValue : T) : Cache() {

        override fun add(value: T) {
            result.add(ListSetComponent(oldValue, value))
            cache = emptyCache
        }

        override fun flush() {
            result.add(ListRemoveComponent(oldValue))
            cache = emptyCache
        }

    }

    private var cache = emptyCache

    private val result = mutableListOf<ListComponent<T>>()

    override fun add(value: T) {
        cache.add(value)
    }

    override fun set(oldValue: T, newValue: T) {
        cache.flush()
        result.add(ListSetComponent(oldValue, newValue))
    }

    override fun remove(oldValue: T) {
        cache.remove(oldValue)
    }

    override fun retain(count: Int) {
        cache.retain(count)
    }

    override fun build(): ListOperation<T> {
        cache.flush()
        return ListOperation(result)
    }

}
