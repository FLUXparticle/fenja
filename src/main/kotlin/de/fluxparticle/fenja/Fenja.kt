package de.fluxparticle.fenja

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue

/**
 * Created by sreinck on 18.05.18.
 */
fun <T> eventStream(): EventStreamDelegate<T> {
    return EventStreamDelegate()
}

fun <T> value(): ValueDelegate<T> {
    return ValueDelegate()
}

fun fenjaBuilder(block: FenjaBuilder.() -> Unit) {
    FenjaBuilder().block()
}

infix fun <T> Property<T>.bindTo(observableValue: ObservableValue<out T>) {
    return this.bind(observableValue)
}

operator fun <T, U> Value<T>.times(other: Value<U>): ValueLifter2<T, U> {
    return ValueLifter2(this, other)
}

class ValueLifter2<T, U>(private val param1: Value<T>, private val param2: Value<U>) {
    operator fun <R> invoke(block: (T, U) -> R): Value<R> {
        return param1.lift(param2, block)
    }
    operator fun <V> times(other: Value<V>): ValueLifter3<T, U, V> {
        return ValueLifter3(param1, param2, other)
    }
}

class ValueLifter3<T, U, V>(private val param1: Value<T>, private val param2: Value<U>, private val param3: Value<V>) {
    operator fun <R> invoke(block: (T, U, V) -> R): Value<R> {
        return param1.lift(param2, param3, block)
    }
}
