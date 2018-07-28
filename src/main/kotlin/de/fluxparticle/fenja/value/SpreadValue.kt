package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class SpreadValue<T : Any>(private vararg val destinations : ReadWriteValue<T>) : ForwardValue<T>() {

    override fun setForward(value: T) {
        destinations.forEach { dest ->
            dest.value = value
        }
    }

}