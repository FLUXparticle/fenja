package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class LoopValue<T> : ReadWriteValue<T>() {

    private var loop: ReadWriteValue<T> = SimpleValue()

    private var closed: Boolean = false

    override var value: T
        get() = loop.value
        set(value) {
            loop.value = value
        }

    fun loop(value: ReadWriteValue<T>) {
        if (closed) throw IllegalStateException("Loop already closed")
        closed = true

        value.value = loop.value
        loop = value
    }

}
