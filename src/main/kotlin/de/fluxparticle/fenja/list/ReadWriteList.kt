package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 06.07.18.
 */
abstract class ReadWriteList<T> : AbstractList<T>(), WriteList<T>, ReadList<T> {

    fun add(element: T) {
        add(size, element)
    }

    final override fun clear() {
        (0 until size).reversed().forEach { idx ->
            removeAt(idx)
        }
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {

        var index: Int = 0

        override fun hasNext(): Boolean = index < size

        override fun next(): T = get(index++)

    }

    override fun toString(): String {
        return toList().toString()
    }

}
