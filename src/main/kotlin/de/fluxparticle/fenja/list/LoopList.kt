package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
class LoopList<T> : ReadWriteList<T>() {

    private var loop: ReadWriteList<T>

    init {
        loop = DelegatedList(mutableListOf())
    }

    fun loop(list: ReadWriteList<T>) {
        list.clear()
        loop.forEachIndexed { index, element -> list.add(index, element) }
        loop = list
    }

    override fun add(index: Int, element: T) = loop.add(index, element)

    override fun set(index: Int, element: T) = loop.set(index, element)

    override fun removeAt(index: Int) = loop.removeAt(index)

    override val size: Int
        get() = loop.size

    override fun get(index: Int): T = loop.get(index)

}
