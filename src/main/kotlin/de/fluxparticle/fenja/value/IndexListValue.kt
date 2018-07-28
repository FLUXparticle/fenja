package de.fluxparticle.fenja.value

import de.fluxparticle.fenja.list.ReadWriteList

/**
 * Created by sreinck on 28.07.18.
 */
class IndexListValue<T>(private val index: Int, private val destination: ReadWriteList<T>) : ReadWriteValue<T>() {

    override var value: T
        get() = destination.get(index)
        set(value) { destination.set(index, value) }

}
