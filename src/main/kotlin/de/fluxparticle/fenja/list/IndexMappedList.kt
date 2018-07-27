package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
class IndexMappedList<T, R>(private val destination: ReadWriteList<R>, private val func : (Int, T) -> R) : ForwardList<T>() {

    override fun addForward(index: Int, element: T) {
        destination.add(index, func(index, element))
        update(index + 1)
    }

    override fun setForward(index: Int, element: T) {
        destination.set(index, func(index, element))
    }

    override fun removeAtForward(index: Int) {
        destination.removeAt(index)
        update(index)
    }

    private fun update(from: Int) {
        for (idx in from until destination.size()) {
            destination.set(idx, func(idx, get(idx)))
        }
    }

}
