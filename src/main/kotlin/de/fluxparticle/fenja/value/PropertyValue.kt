package de.fluxparticle.fenja.value

import de.fluxparticle.fenja.value.ReadWriteValue
import javafx.beans.property.Property

/**
 * Created by sreinck on 07.07.18.
 */
class PropertyValue<T>(private val observableValue: Property<T>) : ReadWriteValue<T> {

    override var value: T
        get() = observableValue.value
        set(value) { observableValue.value = value }

}
