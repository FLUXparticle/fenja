package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 06.07.18.
 */
abstract class ReadWriteList<T> : AbstractWriteList<T>(), ReadList<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {

        var index: Int = 0

        override fun hasNext(): Boolean = index < size()

        override fun next(): T = get(index++)

    }

}
