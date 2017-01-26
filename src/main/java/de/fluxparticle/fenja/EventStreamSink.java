package de.fluxparticle.fenja;

import nz.sodium.CellSink;
import nz.sodium.Stream;
import nz.sodium.StreamSink;

/**
 * Created by sreinck on 25.01.17.
 */
public class EventStreamSink<T> extends EventStream<T> {

    EventStreamSink() {
        super(new StreamSink<>());
    }

    public void sendEvent(T a) {
        ((StreamSink<T>)this.stream).send(a);
    }

}
