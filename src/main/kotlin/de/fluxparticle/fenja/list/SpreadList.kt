package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
class SpreadList<T>(private vararg val destinations : WriteList<T>) : ForwardList<T>() {

    override fun addForward(index: Int, element: T) {
        destinations.forEach { dest ->
            dest.add(index, element)
        }
    }

    override fun setForward(index: Int, element: T) {
        destinations.forEach { dest ->
            dest.set(index, element)
        }
    }

    override fun removeAtForward(index: Int) {
        destinations.forEach { dest ->
            dest.removeAt(index)
        }
    }

}