package de.fluxparticle.fenja;

import nz.sodium.CellLoop;

/**
 * Created by sreinck on 15.06.16.
 */
public class ValueLoop<T> extends Value<T> {

    ValueLoop() {
        super(new CellLoop<>());
    }

    void loop(Value<T> value) {
// LOG        System.out.println("loop value");
        ((CellLoop<T>) cell).loop(value.cell);
        fireValueChangedEvent();
    }

}
