package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 02.08.18.
 */
sealed class ListOperation<T> {

    abstract fun <R, D> accept(visitor: ListOperationVisitor<T, R, D>, data: D): R

    abstract override fun toString(): String

}

data class ListSetOperation<T>(private val oldValue: T, private val newValue: T) : ListOperation<T>() {

    override fun <R, D> accept(visitor: ListOperationVisitor<T, R, D>, data: D): R {
        return visitor.visitSetOperation(oldValue, newValue, data)
    }

    override fun toString(): String = "=$newValue"

}

data class ListRemoveOperation<T>(private val oldValue: T) : ListOperation<T>() {

    override fun <R, D> accept(visitor: ListOperationVisitor<T, R, D>, data: D): R {
        return visitor.visitRemoveOperation(oldValue, data)
    }

    override fun toString(): String = "-$oldValue"

}

data class ListRetainOperation<T>(private val count: Int) : ListOperation<T>() {

    override fun <R, D> accept(visitor: ListOperationVisitor<T, R, D>, data: D): R {
        return visitor.visitRetainOperation(count, data)
    }

    override fun toString(): String = "_x${count}"

}

sealed class ListInitialization<T> : ListOperation<T>() {

    override fun <R, D> accept(visitor: ListOperationVisitor<T, R, D>, data: D): R {
        return accept(visitor as ListInitializationVisitor<T, R, D>, data)
    }

    abstract fun <R, D> accept(visitor: ListInitializationVisitor<T, R, D>, data: D): R

}

data class ListAddOperation<T>(private val value: T) : ListInitialization<T>() {

    override fun <R, D> accept(visitor: ListInitializationVisitor<T, R, D>, data: D): R {
        return visitor.visitAddOperation(value, data)
    }

    override fun toString(): String = "+$value"

}
