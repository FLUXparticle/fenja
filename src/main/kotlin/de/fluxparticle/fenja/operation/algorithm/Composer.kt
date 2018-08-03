package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.ListOperationVisitor
import kotlin.math.min

/**
 * Created by sreinck on 03.08.18.
 */
class Composer<T> private constructor() {

    private abstract inner class State : ListOperationVisitor<T, Unit, Void?> {
        abstract val isPostState: Boolean
    }

    private abstract inner class PreState : State() {
        final override val isPostState: Boolean
            get() = false

        final override fun visitRemoveOperation(oldValue: T, data: Void?) {
            builder.visitRemoveOperation(oldValue, data)
        }
    }

    private inner class DefaultPreState : PreState() {
        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            state = SetPostState(oldValue, newValue)
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            state = RetainPostState(count)
        }

        override fun visitAddOperation(value: T, data: Void?) {
            state = AddPostState(value)
        }
    }

    private inner class RetainPreState(val count: Int) : PreState() {
        override fun visitAddOperation(value: T, data: Void?) {
            builder.visitAddOperation(value, data)
            consume(1)
        }

        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            builder.visitSetOperation(oldValue, newValue, data)
            consume(1)
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            builder.visitRetainOperation(min(count, this.count), data)
            consume(count)
        }

        private fun consume(count: Int) {
            state = when {
                count > this.count -> RetainPostState(count - this.count)
                count < this.count -> RetainPreState(this.count - count)
                else -> defaultPreState
            }
        }
    }

    private abstract inner class PostState : State() {
        final override val isPostState: Boolean
            get() = true

        final override fun visitAddOperation(value: T, data: Void?) {
            builder.visitAddOperation(value, data)
        }
    }

    private inner class AddPostState(private val value: T) : PostState() {
        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            builder.visitAddOperation(newValue, data)
            state = defaultPreState
        }

        override fun visitRemoveOperation(oldValue: T, data: Void?) {
            state = defaultPreState
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            builder.visitAddOperation(value, data)
            state = if (count > 1) RetainPreState(count - 1) else defaultPreState
        }
    }

    private inner class SetPostState(private val oldValue: T, private val newValue: T) : PostState() {
        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            builder.visitSetOperation(this.oldValue, newValue, data)
            state = defaultPreState
        }

        override fun visitRemoveOperation(oldValue: T, data: Void?) {
            builder.visitRemoveOperation(this.oldValue, data)
            state = defaultPreState
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            builder.visitSetOperation(oldValue, newValue, data)
            state = if (count > 1) RetainPreState(count - 1) else defaultPreState
        }
    }

    private inner class RetainPostState(val count: Int) : PostState() {
        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            builder.visitSetOperation(oldValue, newValue, data)
            consume(1)
        }

        override fun visitRemoveOperation(oldValue: T, data: Void?) {
            builder.visitRemoveOperation(oldValue, data)
            consume(1)
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            builder.visitRetainOperation(min(count, this.count), data)
            consume(count)
        }

        private fun consume(count: Int) {
            state = when {
                count > this.count -> RetainPreState(count - this.count)
                count < this.count -> RetainPostState(this.count - count)
                else -> defaultPreState
            }
        }
    }

    private inner class FinisherState : PostState() {
        override fun visitSetOperation(oldValue: T, newValue: T, data: Void?) {
            throw NoSuchElementException()
        }

        override fun visitRemoveOperation(oldValue: T, data: Void?) {
            throw NoSuchElementException()
        }

        override fun visitRetainOperation(count: Int, data: Void?) {
            throw NoSuchElementException()
        }
    }

    private val defaultPreState = DefaultPreState()

    private var state: State = defaultPreState

    private val builder = ListOperationSequenceBuilder<T>()

    private fun composeOperations(it1: Iterator<ListOperation<T>>, it2: Iterator<ListOperation<T>>): Sequence<ListOperation<T>> {
        while (it1.hasNext()) {
            it1.next().accept(state, null)
            while (state.isPostState) {
                it2.next().accept(state, null)
            }
        }
        if (state !== defaultPreState) {
            throw NoSuchElementException()
        }
        if (it2.hasNext()) {
            state = FinisherState()
            while (it2.hasNext()) {
                it2.next().accept(state, null)
            }
        }
        return builder.build()
    }

    companion object {

        fun <T> compose(op1: Sequence<ListOperation<T>>, op2: Sequence<ListOperation<T>>): Sequence<ListOperation<T>> {
            return Composer<T>().composeOperations(op1.iterator(), op2.iterator())
        }

    }

}
