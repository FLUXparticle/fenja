package de.fluxparticle.fenja;

import groovy.lang.Closure;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import nz.sodium.*;

import static nz.sodium.Cell.lift;

/**
 * Created by sreinck on 10.06.16.
 */
public class Value<T> extends ObservableValueBase<T> {

    public static <T> Value<T> valueOf(ObservableValue<T> observableValue) {
        CellSink<T> sink = new CellSink<>(observableValue.getValue());

        observableValue.addListener((o, oldValue, newValue) -> {
            Platform.runLater(() -> sink.send(newValue));
        });

        return new Value<>(sink);
    }

    public static <T> Value<T> valueOfSilent(ObservableValue<T> observableValue) {
        CellSink<T> sink = new CellSink<>(observableValue.getValue());

        observableValue.addListener((o, oldValue, newValue) -> {
            try {
                sink.send(newValue);
            } catch (RuntimeException e) {
                // ignore RuntimeException
            }
        });

        return new Value<>(sink);
    }

    public static <T> Value<T> constValue(T init) {
        return new Value<>(new Cell<>(init));
    }

    public static <T> EventStream<T> switchS(Value<EventStream<T>> valueOfStream) {
        return new EventStream<>(Cell.switchS(valueOfStream.cell.map(es -> es.stream)));
    }

    final Cell<T> cell;

    private T value;

    Value(Cell<T> cell) {
// LOG        System.out.println("new Value");
        this.cell = cell;
// LOG        cell.listen(t -> System.out.println("value(" + (t != null ? t.getClass().getSimpleName() : "") + "): " + t));
        cell.listen(t -> {
            value = t;
            fireValueChangedEvent();
        });
    }

    @Override
    public T getValue() {
        return value;
    }

    public EventStream<T> values() {
        return new EventStream<>(Operational.value(cell));
    }

    public EventStream<T> updates() {
        return new EventStream<>(Operational.updates(cell));
    }

    public <R> Value<R> map(Closure<R> function) {
        return new Value<>(cell.map(function::call));
    }

    public <U, R> Closure<Value<R>> power(Value<U> other) {
        return new ValueLifter2<>(this, other);
    }

    public void listen(Handler<T> action) {
        cell.listen(action);
    }

    T sample() {
        return cell.sample();
    }

    private static class ValueLifter2<T, U, R> extends Closure<Value<R>> {

        private final Value<T> param1;

        private final Value<U> param2;

        ValueLifter2(Value<T> param1, Value<U> param2) {
            super(param1);
            this.param1 = param1;
            this.param2 = param2;
        }

        public <V> Closure<Value<R>> power(Value<V> other) {
            return new ValueLifter3<>(param1, param2, other);
        }

        public Value<R> doCall(Lambda2<T, U, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, lambda));
        }

    }

    private static class ValueLifter3<T, U, V, R> extends Closure<Value<R>> {

        private final Value<T> param1;

        private final Value<U> param2;

        private final Value<V> param3;

        ValueLifter3(Value<T> param1, Value<U> param2, Value<V> param3) {
            super(param1);
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
        }

        public Value<R> doCall(Lambda3<T, U, V, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, param3.cell, lambda));
        }

    }

}
