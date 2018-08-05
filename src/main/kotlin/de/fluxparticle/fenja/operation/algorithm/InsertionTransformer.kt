package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.BuildingListOperationHandler
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.PositionTracker.RelativePosition

/**
 * Created by sreinck on 03.08.18.
 */
class InsertionTransformer<T> private constructor(private val relativePosition: RelativePosition) : BuildingListOperationHandler<T, ListOperation<T>> {

    private lateinit var otherTransformer: InsertionTransformer<T>

    private val builder = ListOperationSequenceBuilder<T>()

    override fun add(value: T) {
        builder.add(value)
        otherTransformer.builder.retain(1)
    }

    override fun set(oldValue: T, newValue: T) {
        throw IllegalArgumentException()
    }

    override fun remove(oldValue: T) {
        throw IllegalArgumentException()
    }

    override fun retain(count: Int) {
        val oldPosition = relativePosition.get()
        relativePosition.increase(count)
        if (relativePosition.get() < 0) {
            builder.retain(count)
            otherTransformer.builder.retain(count)
        } else if (oldPosition < 0) {
            builder.retain(-oldPosition)
            otherTransformer.builder.retain(-oldPosition)
        }
    }

    override fun build(): ListOperation<T> {
        return builder.build()
    }

    companion object {

        fun <T> transformOperations(clientOp: ListOperation<T>, serverOp: ListOperation<T>): Pair<ListOperation<T>, ListOperation<T>> {
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
                clientIt.next().apply(clientVisitor)
                while (clientPosition.get() > 0) {
                    serverIt.next().apply(serverVisitor)
                }
            }
            while (serverIt.hasNext()) {
                serverIt.next().apply(serverVisitor)
            }
            return Pair(clientVisitor.build(), serverVisitor.build())
        }

    }

}
