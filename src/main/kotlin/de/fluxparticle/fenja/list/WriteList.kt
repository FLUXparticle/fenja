package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
interface WriteList<T> {

    fun add(index: Int, element: T)

    fun set(index: Int, element: T)

    fun removeAt(index: Int)

    fun clear()

}