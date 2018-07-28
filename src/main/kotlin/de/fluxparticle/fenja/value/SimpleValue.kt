package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class SimpleValue<T : Any> : ReadWriteValue<T>() {

    override lateinit var value: T

}
