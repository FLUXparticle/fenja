package de.fluxparticle.fenja.operation

/**
 * Created by sreinck on 05.08.18.
 */
class ListOperation<T>(private val components: Iterable<ListComponent<T>>) : Iterable<ListComponent<T>> {

    override fun iterator(): Iterator<ListComponent<T>> {
        return components.iterator()
    }

    fun <R> apply(builder: BuildingListOperationHandler<T, R>): R {
        apply(builder as ListOperationHandler<T>)
        return builder.build()
    }

    fun apply(handler: ListOperationHandler<T>) {
        forEach { it.apply(handler) }
    }

    override fun toString(): String {
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

}
