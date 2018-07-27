package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 07.07.18.
 */
abstract class ForwardValue<T : Any> : ReadWriteValue<T> {

    private lateinit var source: T

    override var value: T
        get() = source
        set(value) {
            source = value
            setForward(value)
        }

    abstract fun setForward(value: T)

}
