package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.ListComponent
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.ListOperationHandler
import kotlin.math.min

/**
 * Created by sreinck on 03.08.18.
 */
class Composer<T> private constructor() {

    private abstract inner class State : ListOperationHandler<T> {
        abstract val isPostState: Boolean
    }

    private abstract inner class PreState : State() {
        final override val isPostState: Boolean
            get() = false

        final override fun remove(oldValue: T) {
            builder.remove(oldValue)
        }
    }

    private inner class DefaultPreState : PreState() {
        override fun set(oldValue: T, newValue: T) {
            state = SetPostState(oldValue, newValue)
        }

        override fun retain(count: Int) {
            state = RetainPostState(count)
        }

        override fun add(value: T) {
            state = AddPostState(value)
        }
    }

    private inner class RetainPreState(val count: Int) : PreState() {
        override fun add(value: T) {
            builder.add(value)
            consume(1)
        }

        override fun set(oldValue: T, newValue: T) {
            builder.set(oldValue, newValue)
            consume(1)
        }

        override fun retain(count: Int) {
            builder.retain(min(count, this.count))
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

        final override fun add(value: T) {
            builder.add(value)
        }
    }

    private inner class AddPostState(private val value: T) : PostState() {
        override fun set(oldValue: T, newValue: T) {
            builder.add(newValue)
            state = defaultPreState
        }

        override fun remove(oldValue: T) {
            state = defaultPreState
        }

        override fun retain(count: Int) {
            builder.add(value)
            state = if (count > 1) RetainPreState(count - 1) else defaultPreState
        }
    }

    private inner class SetPostState(private val oldValue: T, private val newValue: T) : PostState() {
        override fun set(oldValue: T, newValue: T) {
            builder.set(this.oldValue, newValue)
            state = defaultPreState
        }

        override fun remove(oldValue: T) {
            builder.remove(this.oldValue)
            state = defaultPreState
        }

        override fun retain(count: Int) {
            builder.set(oldValue, newValue)
            state = if (count > 1) RetainPreState(count - 1) else defaultPreState
        }
    }

    private inner class RetainPostState(val count: Int) : PostState() {
        override fun set(oldValue: T, newValue: T) {
            builder.set(oldValue, newValue)
            consume(1)
        }

        override fun remove(oldValue: T) {
            builder.remove(oldValue)
            consume(1)
        }

        override fun retain(count: Int) {
            builder.retain(min(count, this.count))
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
        override fun set(oldValue: T, newValue: T) {
            throw NoSuchElementException()
        }

        override fun remove(oldValue: T) {
            throw NoSuchElementException()
        }

        override fun retain(count: Int) {
            throw NoSuchElementException()
        }
    }

    private val defaultPreState = DefaultPreState()

    private var state: State = defaultPreState

    private val builder = ListOperationSequenceBuilder<T>()

    private fun composeOperations(it1: Iterator<ListComponent<T>>, it2: Iterator<ListComponent<T>>): ListOperation<T> {
        while (it1.hasNext()) {
            it1.next().apply(state)
            while (state.isPostState) {
                it2.next().apply(state)
            }
        }
        if (state !== defaultPreState) {
            throw NoSuchElementException()
        }
        if (it2.hasNext()) {
            state = FinisherState()
            while (it2.hasNext()) {
                it2.next().apply(state)
            }
        }
        return builder.build()
    }

    companion object {

        fun <T> compose(op1: ListOperation<T>, op2: ListOperation<T>): ListOperation<T> {
            return Composer<T>().composeOperations(op1.iterator(), op2.iterator())
        }

    }

}
