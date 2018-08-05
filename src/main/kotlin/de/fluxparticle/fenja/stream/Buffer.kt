package de.fluxparticle.fenja.stream

/**
 * Created by sreinck on 04.08.18.
 */
class Buffer<T> {

    private var transaction: Long = 0

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
