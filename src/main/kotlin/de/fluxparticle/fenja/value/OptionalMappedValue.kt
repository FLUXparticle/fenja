package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 07.07.18.
 */
class OptionalMappedValue<T : Any, R : Any>(
        private val destination: ReadWriteValue<R>,
        private val func : (T) -> R?
) : ForwardValue<T>() {

    override fun setForward(value: T) {
        val result = func(value)
        if (result != null) {
            destination.value = result
        }
    }

}
