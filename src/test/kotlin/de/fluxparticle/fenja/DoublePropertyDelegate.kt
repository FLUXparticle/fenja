package de.fluxparticle.fenja

import javafx.beans.property.DoubleProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 17.08.18.
 */
class DoublePropertyDelegate(private val doubleProperty: DoubleProperty) : ReadOnlyProperty<Any?, DoubleProperty> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): DoubleProperty {
        return doubleProperty
    }

}
