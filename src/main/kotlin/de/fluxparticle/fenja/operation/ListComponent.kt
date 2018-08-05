package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 02.08.18.
 */
sealed class ListComponent<T> {

    abstract fun apply(handler: ListOperationHandler<T>)

    abstract override fun toString(): String

}

data class ListSetComponent<T>(private val oldValue: T, private val newValue: T) : ListComponent<T>() {

    override fun apply(handler: ListOperationHandler<T>) {
        handler.set(oldValue, newValue)
    }

    override fun toString(): String = "=$newValue"

}

data class ListRemoveComponent<T>(private val oldValue: T) : ListComponent<T>() {

    override fun apply(handler: ListOperationHandler<T>) {
        handler.remove(oldValue)
    }

    override fun toString(): String = "-$oldValue"

}

data class ListRetainComponent<T>(private val count: Int) : ListComponent<T>() {

    override fun apply(handler: ListOperationHandler<T>) {
        handler.retain(count)
    }

    override fun toString(): String = "_x$count"

}

sealed class ListInitialization<T> : ListComponent<T>() {

    override fun apply(handler: ListOperationHandler<T>) {
        apply(handler as ListInitializationHandler<T>)
    }

    abstract fun apply(handler: ListInitializationHandler<T>)

}

data class ListAddComponent<T>(private val value: T) : ListInitialization<T>() {

    override fun apply(handler: ListInitializationHandler<T>) {
        handler.add(value)
    }

    override fun toString(): String = "+$value"

}
