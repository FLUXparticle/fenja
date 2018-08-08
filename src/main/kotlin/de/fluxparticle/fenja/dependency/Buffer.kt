package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 04.08.18.
 */
class Buffer<T> {

    private var transaction: Long = -1L

    private var value: T? = null

    fun getTransaction(): Long {
        return transaction
    }

    fun setValue(transaction: Long, value: T) {
        this.transaction = transaction
        this.value = value
    }

    fun getValue(): T {
        @Suppress("unchecked_cast")
        return value as T
    }

}
