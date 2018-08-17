package de.fluxparticle.fenja

import javafx.beans.property.Property
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 17.08.18.
 */
class PropertyDelegate<T>(private val tProperty: Property<T>) : ReadOnlyProperty<Any?, Property<T>> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Property<T> {
        return tProperty
    }

}
