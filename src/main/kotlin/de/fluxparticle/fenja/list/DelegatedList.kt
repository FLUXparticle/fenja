package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
class DelegatedList<T>(private val delegate: MutableList<T>) : ReadWriteList<T>() {

    override fun get(index: Int): T = delegate[index]

    override val size: Int
        get() = delegate.size

    override fun add(index: Int, element: T) {
        delegate.add(index, element)
    }

    override fun set(index: Int, element: T) {
        delegate[index] = element
    }

    override fun removeAt(index: Int) {
        delegate.removeAt(index)
    }

}
