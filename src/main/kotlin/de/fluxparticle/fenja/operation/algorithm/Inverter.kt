package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationHandler
import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 08.08.18.
 */
class Inverter<T> : BuildingListOperationHandler<T, ListOperation<T>> {

    private val builder = ListOperationSequenceBuilder<T>()

    override fun add(value: T) {
        builder.remove(value)
    }

    override fun set(oldValue: T, newValue: T) {
        builder.set(newValue, oldValue)
    }

    override fun remove(oldValue: T) {
        builder.add(oldValue)
    }

    override fun retain(count: Int) {
        builder.retain(count)
    }

    override fun build(): ListOperation<T> {
        return builder.build()
    }

}
