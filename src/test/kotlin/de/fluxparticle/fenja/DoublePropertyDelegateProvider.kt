package de.fluxparticle.fenja

import javafx.beans.property.SimpleDoubleProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 17.08.18.
 */
class DoublePropertyDelegateProvider {

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DoublePropertyDelegate {
        val doubleProperty = SimpleDoubleProperty(null, property.name)
        return DoublePropertyDelegate(doubleProperty)
    }

}