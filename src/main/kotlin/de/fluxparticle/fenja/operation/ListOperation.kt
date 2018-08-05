package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 05.08.18.
 */
class ListOperation<T>(private val components: Iterable<ListComponent<T>>) : Iterable<ListComponent<T>> {

    override fun iterator(): Iterator<ListComponent<T>> {
        return components.iterator()
    }

    fun <R> apply(builder: BuildingListOperationHandler<T, R>): R {
        forEach { it.apply(builder) }
        return builder.build()
    }

}
