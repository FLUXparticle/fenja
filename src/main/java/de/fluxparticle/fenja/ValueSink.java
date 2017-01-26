package de.fluxparticle.fenja;

import nz.sodium.CellSink;

/**
 * Created by sreinck on 22.12.16.
 */
public class ValueSink<T> extends Value<T> {

    public ValueSink(T initValue) {
        super(new CellSink<>(initValue));
    }

    public void setValue(T a) {
        ((CellSink<T>)this.cell).send(a);
    }

}
