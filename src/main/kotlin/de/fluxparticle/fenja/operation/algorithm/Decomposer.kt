package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationVisitor
import de.fluxparticle.fenja.operation.ListOperation

/**
 * Created by sreinck on 03.08.18.
 */
internal class Decomposer<T> private constructor() : BuildingListOperationVisitor<T, Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>>, Void?> {

    private val insertionOp = ListOperationSequenceBuilder<T>()

    private val nonInsertionOp = ListOperationSequenceBuilder<T>()

    override fun visitAddOperation(value: T, data: Void?) {
        insertionOp.visitAddOperation(value, data)
        nonInsertionOp.visitRetainOperation(1, data)
    }

    override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
        insertionOp.visitRetainOperation(1, data)
        nonInsertionOp.visitSetOperation(oldValue, newValue, data)
    }

    override fun visitRemoveOperation(oldValue: T, data: Void?) {
        insertionOp.visitRetainOperation(1, data)
        nonInsertionOp.visitRemoveOperation(oldValue, data)
    }

    override fun visitRetainOperation(count: Int, data: Void?) {
        insertionOp.visitRetainOperation(count, data)
        nonInsertionOp.visitRetainOperation(count, data)
    }

    override fun build(): Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> {
        return Pair(insertionOp.build(), nonInsertionOp.build())
    }

    companion object {

        fun <T> decompose(op: Sequence<ListOperation<T>>): Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> {
            val decomposer = Decomposer<T>()
            op.forEach { it.accept(decomposer, null) }

            return decomposer.build()
        }

    }

}
