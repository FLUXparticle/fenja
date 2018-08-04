package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationHandler
import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 03.08.18.
 */
class Filter<T>(private val predicate: (T) -> Boolean) : BuildingListOperationHandler<T, Sequence<ListOperation<T>>> {

    private val builder = ListOperationSequenceBuilder<T>()

    override fun add(value: T) {
        if (predicate.invoke(value)) {
            builder.retain(1)
        } else {
            builder.remove(value)
        }
    }

    override fun set(oldValue: T, newValue: T) {
        if (predicate.invoke(newValue)) {
            builder.retain(1)
        } else {
            builder.remove(newValue)
        }
    }

    override fun remove(oldValue: T) {
        // empty
    }

    override fun retain(count: Int) {
        builder.retain(count)
    }

    override fun build(): Sequence<ListOperation<T>> {
        return builder.build()
    }

}
