package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 02.08.18.
 */
interface ListOperationHandler<T> : ListInitializationHandler<T> {

    fun set(oldValue: T, newValue: T)

    fun remove(oldValue: T)

    fun retain(count: Int)

}
