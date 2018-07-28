package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class CombineValue<T : Any, S : Any, R : Any>(
        private val destination: ReadWriteValue<R>,
        func: (T, S) -> R
) : ForwardValue<R>() {

    override fun setForward(value: R) {
        destination.value = value
    }

    val left: ReadWriteValue<T> = OptionalMappedValue(this) { t ->
        try {
            func(t, right.value)
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

    val right: ReadWriteValue<S>  = OptionalMappedValue(this) { s ->
        try {
            func(left.value, s)
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

}
