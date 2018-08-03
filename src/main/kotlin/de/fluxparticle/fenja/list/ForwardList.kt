package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
abstract class ForwardList<T>(private val source: MutableList<T> = mutableListOf()) : ReadWriteList<T>() {

    final override fun get(index: Int): T = source[index]

    final override fun size(): Int = source.size

    final override fun add(index: Int, element: T) {
        addForward(index, element)
        source.add(index, element)
    }

    final override fun set(index: Int, element: T) {
        setForward(index, element)
        source[index] = element
    }

    final override fun removeAt(index: Int) {
        removeAtForward(index)
        source.removeAt(index)
    }

    abstract fun addForward(index: Int, element: T)

    abstract fun setForward(index: Int, element: T)

    abstract fun removeAtForward(index: Int)

}
