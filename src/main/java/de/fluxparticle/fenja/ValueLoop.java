package de.fluxparticle.fenja;

import nz.sodium.CellLoop;

/**
 * Created by sreinck on 15.06.16.
 */
public class ValueLoop<T> extends Value<T> {

    public ValueLoop() {
        super(new CellLoop<>());
    }

    public void loop(Value<T> value) {
        ((CellLoop<T>) cell).loop(value.cell);
        fireValueChangedEvent();
    }

}
