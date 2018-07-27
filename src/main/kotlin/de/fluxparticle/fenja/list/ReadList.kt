package de.fluxparticle.fenja.list

/**
 * Created by sreinck on 07.07.18.
 */
interface ReadList<T> : Iterable<T> {

    fun get(index: Int): T

    fun size(): Int

    fun <R : Comparable<R>> binarySearchBy(element: T, by: (T) -> R): Int {
        val comparable = by(element)
        var lo = 0
        var hi = size()
        while (lo < hi) {
            val mid = (lo + hi) / 2
            val cmp = comparable.compareTo(by(get(mid)))
            when {
                cmp < 0 -> hi = mid
                cmp > 0 -> lo = mid + 1
                else -> {
                    lo = mid
                    hi = mid
                }
            }
        }
        return if (lo < size() && element == get(lo)) {
            lo
        } else {
            -1 - lo
        }
    }

    companion object {

        fun <T : Comparable<T>> ReadList<T>.binarySearch(element: T): Int = binarySearchBy(element) { it }

    }

}
