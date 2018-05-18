package de.fluxparticle.fenja

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 18.05.18.
 */
class EventStreamDelegate<T> : ReadWriteProperty<Any, EventStream<T>> {

    private var eventStream: EventStream<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): EventStream<T> {
        return eventStream ?: EventStreamLoop<T>().also {
            eventStream = it
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: EventStream<T>) {
        val es = eventStream
        when (es) {
            null               ->  eventStream = value
            is EventStreamLoop ->  es.loop(value)
            else               ->  throw IllegalStateException()
        }
    }

}
