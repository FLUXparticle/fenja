package de.fluxparticle.fenja.value

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 28.07.18.
 */
class ListenerValue<T>(private val destination: ReadWriteValue<T>) : ChangeListener<T> {

    override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
        destination.value = newValue
    }

}
