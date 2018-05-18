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
