package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationHandler
import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 03.08.18.
 */
internal class Decomposer<T> private constructor() : BuildingListOperationHandler<T, Pair<ListOperation<T>, ListOperation<T>>> {

    private val insertionOp = ListOperationSequenceBuilder<T>()

    private val nonInsertionOp = ListOperationSequenceBuilder<T>()

    override fun add(value: T) {
        insertionOp.add(value)
        nonInsertionOp.retain(1)
    }

    override fun set(oldValue: T, newValue: T) {
        remove(oldValue)
        add(newValue)
    }

    override fun remove(oldValue: T) {
        insertionOp.retain(1)
        nonInsertionOp.remove(oldValue)
    }

    override fun retain(count: Int) {
        insertionOp.retain(count)
        nonInsertionOp.retain(count)
    }

    override fun build(): Pair<ListOperation<T>, ListOperation<T>> {
        return Pair(insertionOp.build(), nonInsertionOp.build())
    }

    companion object {

        fun <T> decompose(op: ListOperation<T>): Pair<ListOperation<T>, ListOperation<T>> {
            return op.apply(Decomposer())
        }

    }

}
