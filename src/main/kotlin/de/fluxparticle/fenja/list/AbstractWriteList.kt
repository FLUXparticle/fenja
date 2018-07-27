package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
abstract class AbstractWriteList<T> : WriteList<T> {

    final override fun add(element: T) {
        add(size(), element)
    }

    final override fun clear() {
        (0 until size()).reversed().forEach { idx ->
            removeAt(idx)
        }
    }

}
