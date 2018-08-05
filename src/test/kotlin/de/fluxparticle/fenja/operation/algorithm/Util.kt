package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 03.08.18.
 */
internal fun add(value: String): ListComponent<String> = ListAddComponent(value)

internal fun set(oldValue: String, newValue: String): ListComponent<String> = ListSetComponent(oldValue, newValue)

internal fun remove(oldValue: String): ListComponent<String> = ListRemoveComponent(oldValue)

internal fun retain(count: Int): ListComponent<String> = ListRetainComponent(count)

fun <T> Sequence<T>.message(): String {
    return asIterable().message()
}

fun <T> Iterable<T>.message(): String {
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

fun <T> listOperation(vararg components: ListComponent<T>): ListOperation<T> {
    return ListOperation(components.asList())
}
