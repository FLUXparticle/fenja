package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.*
import de.fluxparticle.fenja.logger.FenjaSystemLogger
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
abstract class NamedEventStream<T>(override val name: String) : EventStream<T>(), NamedDependency<T> {

    protected val buffer = Buffer<T>()

    override fun getTransaction(): Long {
        return buffer.getTransaction()
    }

    override fun eval(): T {
        return buffer.getValue()
    }

    override fun toString(): String {
        return name
    }

}

class EventStreamSource<T>(name: String, private val transactionProvider: TransactionProvider, private val logger: FenjaSystemLogger) : NamedEventStream<T>(name), SourceDependency<T> {

    override var updates: List<UpdateDependency<*>>? = null

    fun sendValue(value: T) {
        val transaction = transactionProvider.newTransaction()
        buffer.setValue(transaction, value)
        logger.updateSource(this, value)
        updates?.forEach { it.update() }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this)
    }

}

fun <T : Event> EventStreamSource<T>.bind(node: Node, eventType: EventType<T>) {
    node.addEventHandler(eventType) { sendValue(it) }
}

infix fun <T> EventStreamSource<T>.bind(observableValue: ObservableValue<T>) {
    observableValue.addListener { _, _, newValue -> sendValue(newValue) }
}

infix fun EventStreamSource<Unit>.ticker(duration: Duration) {
    val eventHandler = EventHandler<ActionEvent> { sendValue(Unit) }
    val keyFrame = KeyFrame(duration, eventHandler)
    val timeline = Timeline(keyFrame)
    timeline.cycleCount = Timeline.INDEFINITE
    timeline.play()
}

class EventStreamRelay<T>(name: String, private val logger: FenjaSystemLogger) : NamedEventStream<T>(name), UpdateDependency<T> {

    private lateinit var source: EventStream<T>

    internal var property: Property<T>? = null

    override fun getDependency(): Dependency<T>? {
        return if (this::source.isInitialized) source else null
    }

    fun setSource(source: EventStream<T>) {
        this.source = source
    }

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.eval()
            buffer.setValue(transaction, value)
            logger.executeUpdate(this, value)
            property?.value = value
        }
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source)
    }

}

infix fun <T> Property<T>.bind(eventStream: EventStream<T>) {
    when (eventStream) {
        is EventStreamRelay -> eventStream.property = this
        else -> throw RuntimeException("only EventStreamRelay can be bound")
    }
}
