package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 03.08.18.
 */
internal fun add(value: String): ListOperation<String> = ListAddOperation(value)

internal fun set(oldValue: String, newValue: String): ListOperation<String> = ListSetOperation(oldValue, newValue)

internal fun remove(oldValue: String): ListOperation<String> = ListRemoveOperation(oldValue)

internal fun retain(count: Int): ListOperation<String> = ListRetainOperation(count)

internal fun <T> Iterable<T>.message(): String {
    val sb = StringBuilder("[")

    var delimiter = ""
    val iterator = iterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        sb.append(delimiter)
        sb.append(next.toString())
        delimiter = ", "
    }
    sb.append("]")

    return sb.toString()
}
