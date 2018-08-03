package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationVisitor
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.PositionTracker.RelativePosition

/**
 * Created by sreinck on 03.08.18.
 */
class InsertionTransformer<T> private constructor(val relativePosition: RelativePosition) : BuildingListOperationVisitor<T, Sequence<ListOperation<T>>, Void?> {

    private lateinit var otherTransformer: InsertionTransformer<T>

    private val builder = ListOperationSequenceBuilder<T>()

    override fun visitAddOperation(value: T, data: Void?) {
        builder.visitAddOperation(value, data)
        otherTransformer.builder.visitRetainOperation(1, data)
    }

    override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
        throw IllegalArgumentException()
    }

    override fun visitRemoveOperation(oldValue: T, data: Void?) {
        throw IllegalArgumentException()
    }

    override fun visitRetainOperation(count: Int, data: Void?) {
        val oldPosition = relativePosition.get()
        relativePosition.increase(count)
        if (relativePosition.get() < 0) {
            builder.visitRetainOperation(count, data)
            otherTransformer.builder.visitRetainOperation(count, data)
        } else if (oldPosition < 0) {
            builder.visitRetainOperation(-oldPosition, data)
            otherTransformer.builder.visitRetainOperation(-oldPosition, data)
        }
    }

    override fun build(): Sequence<ListOperation<T>> {
        return builder.build()
    }

    companion object {

        fun <T> transformOperations(clientOp: Sequence<ListOperation<T>>, serverOp: Sequence<ListOperation<T>>): Pair<Sequence<ListOperation<T>>, Sequence<ListOperation<T>>> {
            val positionTracker = PositionTracker()

            val clientPosition = positionTracker.positivePosition
            val serverPosition = positionTracker.negativePosition

            val clientVisitor = InsertionTransformer<T>(clientPosition)
            val serverVisitor = InsertionTransformer<T>(serverPosition)

            clientVisitor.otherTransformer = serverVisitor
            serverVisitor.otherTransformer = clientVisitor

            val clientIt = clientOp.iterator()
            val serverIt = serverOp.iterator()
            while (clientIt.hasNext()) {
                clientIt.next().accept(clientVisitor, null)
                while (clientPosition.get() > 0) {
                    serverIt.next().accept(serverVisitor, null)
                }
            }
            while (serverIt.hasNext()) {
                serverIt.next().accept(serverVisitor, null)
            }
            return Pair(clientVisitor.build(), serverVisitor.build())
        }

    }

}
