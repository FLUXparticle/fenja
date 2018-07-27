package de.fluxparticle.fenja.value

import de.fluxparticle.fenja.list.ForwardList
import de.fluxparticle.fenja.list.ReadList

/**
 * Created by sreinck on 07.07.18.
 */
class ReducedValue<T, R>(
        private val destination: ReadWriteValue<R>,
        private val func: (ReadList<T>) -> R
) : ForwardList<T>() {

    init {
        update()
    }

    override fun addForward(index: Int, element: T) {
        update()
    }

    override fun setForward(index: Int, element: T) {
        update()
    }

    override fun removeAtForward(index: Int) {
        update()
    }

    private fun update() {
        destination.value = func(this)
    }

}
