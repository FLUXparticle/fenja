package de.fluxparticle.fenja;

/**
 * Created by sreinck on 23.02.16.
 */
public interface ListChangeListener<T> {

    void visitUpdate(Integer index, T data);

}
