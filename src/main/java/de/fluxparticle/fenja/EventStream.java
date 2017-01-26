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

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Created by sreinck on 10.06.16.
 */
public class EventStream<T> {

    public static <T> EventStream<T> never() {
        return new EventStream<>(new Stream<>());
    }

    public static <T extends Event> EventStream<T> streamOf(Node node, EventType<T> eventType) {
        StreamSink<T> sink = new StreamSink<>();

        node.addEventHandler(eventType, sink::send);

        return new EventStream<>(sink);
    }

    public static <T extends Event> EventStream<T> streamOf(Window window, EventType<T> eventType) {
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
        this.stream = stream;
    }

    // listen

    public void listen(Handler<T> handler) {
        stream.listen(handler);
    }

    public void listenOnce(Handler<T> handler) {
        stream.listenOnce(handler);
    }

    public void listenWeak(Handler<T> handler) {
        stream.listenWeak(handler);
    }

    // map

    public <R> EventStream<R> constant(R value) {
        return new EventStream<>(stream.mapTo(value));
    }

    public <R> EventStream<R> map(Closure<R> closure) {
        modifyClosure(closure);
        return map(closure::call);
    }

    public <R> EventStream<R> map(Lambda1<T, R> function) {
        return new EventStream<>(stream.map(function));
    }

    // hold

    public Value<T> hold(T init) {
        return new Value<>(stream.hold(init));
    }

    public Value<T> holdLazy(Closure<T> init) {
        modifyClosure(init);
        return holdLazy(init::call);
    }

    public Value<T> holdLazy(Lambda0<T> init) {
        Lazy<T> lazy = new Lazy<>(init);
        return new Value<>(stream.holdLazy(lazy));
    }

    // snapshot

    public <R> EventStream<R> snapshot(Value<R> value) {
        return new EventStream<>(stream.snapshot(value.cell));
    }

    // merge

    public EventStream<T> multiply(EventStream<T> other) {
        return orElse(other);
    }

    public EventStream<T> orElse(EventStream<T> other) {
        return new EventStream<>(stream.orElse(other.stream));
    }

    public Closure<EventStream<T>> power(EventStream<T> other) {
        return new Closure<EventStream<T>>(this) {
            public EventStream<T> doCall(Lambda2<T, T, T> lambda) {
                return merge(other, lambda);
            }
        };
    }

    public EventStream<T> merge(EventStream<T> other, Lambda2<T, T, T> lambda) {
        return new EventStream<>(stream.merge(other.stream, lambda));
    }

    public static <T> EventStream<T> mergeAll(Iterable<EventStream<T>> streams, Lambda2<T, T, T> lambda) {
        List<Stream<T>> streamList = StreamSupport.stream(streams.spliterator(), false)
                .map(s -> s.stream)
                .collect(toList());
        return new EventStream<>(Stream.merge(streamList, lambda));
    }

    // filter

    public EventStream<T> filter(Closure<Boolean> predicate) {
        modifyClosure(predicate);
        return filter(predicate::call);
    }

    public EventStream<T> filter(Lambda1<T, Boolean> predicate) {
        return new EventStream<>(stream.filter(predicate));
    }

    public static <T> EventStream<T> filterOptional(EventStream<Optional<T>> optionalStream) {
        return new EventStream<>(Stream.filterOptional(optionalStream.stream));
    }

    // gate

    public EventStream<T> gate(Value<Boolean> value) {
        return new EventStream<>(stream.gate(value.cell));
    }

    // accum

    public <S> Value<S> accum(S init, Closure<S> function) {
        return new Value<>(stream.accum(init, (t, s) -> function.call(t, s)));
    }

    public <S> Value<S> accum(S init, Lambda2<T, S, S> function) {
        return new Value<>(stream.accum(init, function));
    }

    // defer

    public EventStream<T> defer() {
        return new EventStream<>(Operational.defer(stream));
    }

    // split

    public static <A, C extends Iterable<A>> EventStream<A> split(EventStream<C> iterableStream) {
        return new EventStream<>(Operational.split(iterableStream.stream));
    }

    // *** private ***

    private static <R> void modifyClosure(Closure<R> closure) {
        Closure outerClosure = (Closure) closure.getOwner();

        GroovyObject owner = (GroovyObject) outerClosure.getOwner();
        GroovyObject delegate = (GroovyObject) outerClosure.getDelegate();

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
