package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
abstract class ForwardList<T> : ReadWriteList<T>() {

    private val source: MutableList<T> = mutableListOf()

    final override fun get(index: Int): T = source[index]

    final override fun size(): Int = source.size

    final override fun add(index: Int, element: T) {
        source.add(index, element)
        addForward(index, element)
    }

    final override fun set(index: Int, element: T) {
        source[index] = element
        setForward(index, element)
    }

    final override fun removeAt(index: Int) {
        source.removeAt(index)
        removeAtForward(index)
    }

    abstract fun addForward(index: Int, element: T)

    abstract fun setForward(index: Int, element: T)

    abstract fun removeAtForward(index: Int)

}
