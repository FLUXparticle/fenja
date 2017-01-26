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

    final Cell<T> cell;

    private T value;

    Value(Cell<T> cell) {
        this.cell = cell;
        cell.listen(t -> {
            value = t;
            fireValueChangedEvent();
        });
    }

    // listen

    public void listen(Handler<T> action) {
        cell.listen(action);
    }

    // sample

    @Override
    public T getValue() {
        return value;
    }

    // updates

    public EventStream<T> updates() {
        return new EventStream<>(Operational.updates(cell));
    }

    // values

    public EventStream<T> values() {
        return new EventStream<>(Operational.value(cell));
    }

    // map

    public <R> Value<R> map(Lambda1<T, R> lambda) {
        return new Value<>(cell.map(lambda));
    }

    // lift

    public <U, R> Closure<Value<R>> power(Value<U> other) {
        return new ValueLifter2<>(this, other);
    }

    public <U, R> Value<R> lift(Value<U> param2, Lambda2<T, U, R> lambda) {
        return new Value<>(this.cell.lift(param2.cell, lambda));
    }

    public <U, V, R> Value<R> lift(Value<U> param2, Value<V> param3, Lambda3<T, U, V, R> lambda) {
        return new Value<>(this.cell.lift(param2.cell, param3.cell, lambda));
    }

    public <U, V, W, R> Value<R> lift(Value<U> param2, Value<V> param3, Value<W> param4, Lambda4<T, U, V, W, R> lambda) {
        return new Value<>(this.cell.lift(param2.cell, param3.cell, param4.cell, lambda));
    }

    public <U, V, W, X, R> Value<R> lift(Value<U> param2, Value<V> param3, Value<W> param4, Value<X> param5, Lambda5<T, U, V, W, X, R> lambda) {
        return new Value<>(this.cell.lift(param2.cell, param3.cell, param4.cell, param5.cell, lambda));
    }

    public <U, V, W, X, Y, R> Value<R> lift(Value<U> param2, Value<V> param3, Value<W> param4, Value<X> param5, Value<Y> param6, Lambda6<T, U, V, W, X, Y, R> lambda) {
        return new Value<>(this.cell.lift(param2.cell, param3.cell, param4.cell, param5.cell, param6.cell, lambda));
    }

    // apply

    public static <T, R> Value<R> apply(Value<Lambda1<T, R>> function, Value<T> value) {
        return new Value<>(Cell.apply(function.cell, value.cell));
    }

    // switchC

    public static <T> EventStream<T> switchS(Value<EventStream<T>> valueOfStream) {
        return new EventStream<>(Cell.switchS(valueOfStream.cell.map(es -> es.stream)));
    }

    // switchS

    public static <T> Value<T> switchV(Value<Value<T>> valueOfValue) {
        return new Value<>(Cell.switchC(valueOfValue.cell.map(es -> es.cell)));
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

        public <W> Closure<Value<R>> power(Value<W> other) {
            return new ValueLifter4<>(param1, param2, param3, other);
        }

        public Value<R> doCall(Lambda3<T, U, V, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, param3.cell, lambda));
        }

    }

    private static class ValueLifter4<T, U, V, W, R> extends Closure<Value<R>> {

        private final Value<T> param1;

        private final Value<U> param2;

        private final Value<V> param3;

        private final Value<W> param4;

        ValueLifter4(Value<T> param1, Value<U> param2, Value<V> param3, Value<W> param4) {
            super(param1);
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.param4 = param4;
        }

        public <X> Closure<Value<R>> power(Value<X> other) {
            return new ValueLifter5<>(param1, param2, param3, param4, other);
        }

        public Value<R> doCall(Lambda4<T, U, V, W, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, param3.cell, param4.cell, lambda));
        }

    }

    private static class ValueLifter5<T, U, V, W, X, R> extends Closure<Value<R>> {

        private final Value<T> param1;

        private final Value<U> param2;

        private final Value<V> param3;

        private final Value<W> param4;

        private final Value<X> param5;

        ValueLifter5(Value<T> param1, Value<U> param2, Value<V> param3, Value<W> param4, Value<X> param5) {
            super(param1);
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.param4 = param4;
            this.param5 = param5;
        }

        public <Y> Closure<Value<R>> power(Value<Y> other) {
            return new ValueLifter6<>(param1, param2, param3, param4, param5, other);
        }

        public Value<R> doCall(Lambda5<T, U, V, W, X, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, param3.cell, param4.cell, param5.cell, lambda));
        }

    }

    private static class ValueLifter6<T, U, V, W, X, Y, R> extends Closure<Value<R>> {

        private final Value<T> param1;

        private final Value<U> param2;

        private final Value<V> param3;

        private final Value<W> param4;

        private final Value<X> param5;

        private final Value<Y> param6;

        ValueLifter6(Value<T> param1, Value<U> param2, Value<V> param3, Value<W> param4, Value<X> param5, Value<Y> param6) {
            super(param1);
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.param4 = param4;
            this.param5 = param5;
            this.param6 = param6;
        }

        public Value<R> doCall(Lambda6<T, U, V, W, X, Y, R> lambda) {
            return new Value<>(param1.cell.lift(param2.cell, param3.cell, param4.cell, param5.cell, param6.cell, lambda));
        }

    }

}
