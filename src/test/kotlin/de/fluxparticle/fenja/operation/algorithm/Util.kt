package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.operation.*

/**
 * Created by sreinck on 03.08.18.
 */
internal fun add(value: String): ListComponent<String> = ListAddComponent(value)

internal fun set(oldValue: String, newValue: String): ListComponent<String> = ListSetComponent(oldValue, newValue)

internal fun remove(oldValue: String): ListComponent<String> = ListRemoveComponent(oldValue)

internal fun retain(count: Int): ListComponent<String> = ListRetainComponent(count)

fun <T> listOperation(vararg components: ListComponent<T>): ListOperation<T> {
    return ListOperation(components.asList())
}
