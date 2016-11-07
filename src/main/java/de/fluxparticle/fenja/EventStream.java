package de.fluxparticle.fenja;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.util.Duration;
import nz.sodium.*;

/**
 * Created by sreinck on 10.06.16.
 */
public class EventStream<T> {

    public static <T> EventStream<T> never() {
        return new EventStream<>(new Stream<>());
    }

    public static <T extends Event> EventStream<T> streamOf(Node node, EventType<T> eventType) {
// LOG       System.out.println("streamOf");
        StreamSink<T> sink = new StreamSink<>();

        node.addEventHandler(eventType, sink::send);

        return new EventStream<>(sink);
    }

    public static <T extends Event> EventStream<T> streamOf(Window window, EventType<T> eventType) {
// LOG       System.out.println("streamOf");
        StreamSink<T> sink = new StreamSink<>();

        window.addEventHandler(eventType, sink::send);

        return new EventStream<>(sink);
    }

    public static EventStream<Unit> ticker(Duration duration) {
        StreamSink<Unit> ticker = new StreamSink<>();

        Timeline timeline = new Timeline(new KeyFrame(duration, event -> ticker.send(Unit.UNIT)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return new EventStream<>(ticker);
    }

    final Stream<T> stream;

    EventStream(Stream<T> stream) {
// LOG       System.out.println("new EventStream");
        this.stream = stream;
// LOG       stream.listen(t -> System.out.println("stream(" + (t != null ? t.getClass().getSimpleName() : "") + "): " + t));
    }

    public EventStream<T> defer() {
        return new EventStream<>(Operational.defer(stream));
    }

    public EventStream<T> filter(Closure<Boolean> predicate) {
        modifyClosure(predicate);
        return filter(predicate::call);
    }

    public EventStream<T> filter(Lambda1<T, Boolean> predicate) {
        return new EventStream<>(stream.filter(predicate));
    }

    public <R> EventStream<R> map(Closure<R> closure) {
        modifyClosure(closure);
        return map(closure::call);
    }

    public <R> EventStream<R> map(Lambda1<T, R> function) {
        return new EventStream<>(stream.map(function));
    }

    public EventStream<T> gate(Value<Boolean> value) {
        return new EventStream<>(stream.gate(value.cell));
    }

    public <S, R> EventStream<R> snapshot(Value<S> s, Lambda2<T, S, R> biFunction) {
        return new EventStream<>(stream.snapshot(s.cell, biFunction));
    }

    public EventStream<T> multiply(EventStream<T> other) {
        return new EventStream<>(stream.orElse(other.stream));
    }

    public Value<T> hold(T init) {
        return new Value<T>(stream.hold(init));
    }

    public <S> Value<S> accum(S init, Closure<S> function) {
        return new Value<>(stream.accum(init, (t, s) -> function.call(t, s)));
    }

    public <S> Value<S> accum(S init, Lambda2<T, S, S> function) {
        return new Value<>(stream.accum(init, function));
    }

    public void listen(Closure closure) {
        stream.listen(closure::call);
    }

    private static <R> void modifyClosure(Closure<R> closure) {
        Closure outerClosure = (Closure) closure.getOwner();

        GroovyObject owner = (GroovyObject) outerClosure.getOwner();
        GroovyObject delegate = (GroovyObject) outerClosure.getDelegate();

// LOG        System.out.println("owner: " + owner.getClass());

        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.setDelegate(new GroovyObjectSupport() {
            public Object propertyMissing(String name) {
                try {
                    return owner.getProperty(name);
                } catch (MissingPropertyException e) {
                    return ((Value) delegate.getProperty(name)).sample();
                }
            }
        });
    }

}
