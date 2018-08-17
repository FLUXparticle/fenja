package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.FenjaSystem.InputEventStream
import javafx.animation.KeyFrame
import javafx.animation.Timeline
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
