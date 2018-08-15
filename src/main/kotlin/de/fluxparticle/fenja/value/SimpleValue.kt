package de.fluxparticle.fenja.value

/**
 * Created by sreinck on 28.07.18.
 */
class SimpleValue<T> : ReadWriteValue<T>() {

    var isSet: Boolean = false
        private set

    private var internValue: T? = null

    override var value: T
        @Suppress("unchecked_cast")
        get() {
            if (!isSet) {
                throw IllegalStateException()
            }
            return internValue as T
        }
        set(value) {
            internValue = value
            isSet = true
        }

}
