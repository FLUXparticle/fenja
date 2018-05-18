package de.fluxparticle.fenja

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 18.05.18.
 */
class ValueDelegate<T> : ReadWriteProperty<Any, Value<T>> {

    private var value: Value<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): Value<T> {
        return value ?: ValueLoop<T>().also {
            value = it
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Value<T>) {
        val es = this.value
        when (es) {
            null         ->  this.value = value
            is ValueLoop ->  es.loop(value)
            else         ->  throw IllegalStateException()
        }
    }

}
