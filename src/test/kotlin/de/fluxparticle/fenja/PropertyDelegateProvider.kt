package de.fluxparticle.fenja

import javafx.beans.property.SimpleObjectProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 17.08.18.
 */
class PropertyDelegateProvider<T> {

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): PropertyDelegate<T> {
        val tProperty = SimpleObjectProperty<T>(null, property.name)
        return PropertyDelegate(tProperty)
    }

}