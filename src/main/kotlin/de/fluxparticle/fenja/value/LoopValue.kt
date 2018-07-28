package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class LoopValue<T : Any> : ReadWriteValue<T>() {

    private var loop : ReadWriteValue<T> = SimpleValue()

    override var value: T
        get() = loop.value
        set(value) {
            loop.value = value
        }

    fun loop(value: ReadWriteValue<T>) {
        value.value = loop.value
        loop = value
    }

}
