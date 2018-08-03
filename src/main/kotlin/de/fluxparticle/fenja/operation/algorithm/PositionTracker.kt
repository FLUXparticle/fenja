package de.fluxparticle.fenja.operation.algorithm

internal class PositionTracker {

    private var position = 0

    val positivePosition = object : RelativePosition {

        override fun increase(amount: Int) {
            position += amount
        }

        override fun get(): Int {
            return position
        }

    }

    val negativePosition = object : RelativePosition {

        override fun increase(amount: Int) {
            position -= amount
        }

        override fun get(): Int {
            return -position
        }

    }

    interface RelativePosition {

        fun increase(amount: Int)

        fun get(): Int

    }

}
