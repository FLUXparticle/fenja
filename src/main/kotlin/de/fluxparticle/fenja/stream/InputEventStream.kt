package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.value.PropertyValue
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.util.Duration

/**
 * Created by sreinck on 05.08.18.
 */
class InputEventStream<T> internal constructor(
        name: String,
        transactionProvider: TransactionProvider,
        logger: FenjaSystemLogger
) : SourceEventStream<T>(SourceDependency<T>(name, transactionProvider, logger)) {

    fun sendValue(value: T) {
        dependency.executeUpdates(value)
    }

}

fun <T : Event> InputEventStream<T>.bind(node: Node, eventType: EventType<T>) {
    node.addEventHandler(eventType) { sendValue(it) }
}

infix fun <T> InputEventStream<T>.bind(observableValue: ObservableValue<T>) {
    observableValue.addListener { _, _, newValue -> sendValue(newValue) }
}

infix fun InputEventStream<Unit>.ticker(duration: Duration) {
    val eventHandler = EventHandler<ActionEvent> { sendValue(Unit) }
    val keyFrame = KeyFrame(duration, eventHandler)
    val timeline = Timeline(keyFrame)
    timeline.cycleCount = Timeline.INDEFINITE
    timeline.play()
}

infix fun <T> Property<T>.bind(eventStream: UpdateEventStream<T>) {
    eventStream.dependency.loop(PropertyValue(this))
}
