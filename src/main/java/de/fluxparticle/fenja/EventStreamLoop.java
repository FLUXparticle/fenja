package de.fluxparticle.fenja;

import nz.sodium.StreamLoop;

/**
 * Created by sreinck on 16.06.16.
 */
public class EventStreamLoop<T> extends EventStream<T> {

    EventStreamLoop() {
        super(new StreamLoop<>());
    }

    void loop(EventStream<T> eventStream) {
        ((StreamLoop<T>) stream).loop(eventStream.stream);
    }

}
