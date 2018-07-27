package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
class MappedList<T, R>(
        private val destination: WriteList<R>,
        private val func : (T) -> R
) : ForwardList<T>() {

    override fun addForward(index: Int, element: T) {
        destination.add(index, func(element))
    }

    override fun setForward(index: Int, element: T) {
        destination.set(index, func(element))
    }

    override fun removeAtForward(index: Int) {
        destination.removeAt(index)
    }

}
