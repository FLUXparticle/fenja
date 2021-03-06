package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 07.07.18.
 */
class MappedValue<T : Any, R : Any>(
        private val destination: ReadWriteValue<R>,
        private val func : (T) -> R
) : ForwardValue<T>() {

    override fun setForward(value: T) {
        destination.value = func(value)
    }

}
