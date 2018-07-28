package de.fluxparticle.fenja.list

import de.fluxparticle.fenja.value.IndexListValue
import de.fluxparticle.fenja.value.LoopValue
import de.fluxparticle.fenja.value.SimpleValue

/**
 * Created by sreinck on 28.07.18.
 */
class CombineList<T : Any>(private val destination: ReadWriteList<T>) : ForwardList<LoopValue<T>>() {

    override fun addForward(index: Int, element: LoopValue<T>) {
        destination.add(index, element.value)
        element.loop(IndexListValue(index, destination))
    }

    override fun setForward(index: Int, element: LoopValue<T>) {
        get(index).loop(SimpleValue())
        element.loop(IndexListValue(index, destination))
    }

    override fun removeAtForward(index: Int) {
        get(index).loop(SimpleValue())
        destination.removeAt(index)
    }

}
