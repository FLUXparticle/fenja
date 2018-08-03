package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationVisitor
import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 03.08.18.
 */
class Filter<T>(private val predicate: (T) -> Boolean) : BuildingListOperationVisitor<T, Sequence<ListOperation<T>>, Void?> {

    private val builder = ListOperationSequenceBuilder<T>()

    override fun visitAddOperation(value: T, data: Void?) {
        if (predicate.invoke(value)) {
            builder.visitRetainOperation(1, data)
        } else {
            builder.visitRemoveOperation(value, data)
        }
    }

    override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
        throw UnsupportedOperationException()
//        if (predicate.invoke(newValue)) {
//            builder.visitRetainOperation(1, data)
//        } else {
//            builder.visitRemoveOperation(newValue, data)
//        }
    }

    override fun visitRemoveOperation(oldValue: T, data: Void?) {
        // empty
    }

    override fun visitRetainOperation(count: Int, data: Void?) {
        builder.visitRetainOperation(count, data)
    }

    override fun build(): Sequence<ListOperation<T>> {
        return builder.build()
    }

}
