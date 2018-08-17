package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.PropertyDelegateProvider
import de.fluxparticle.fenja.logger.DelegateFenjaSystemLogger
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by sreinck on 04.08.18.
 */
class EventStreamTest {

    private val logger = DelegateFenjaSystemLogger(PrintFenjaSystemLogger(System.out))

    private val input by PropertyDelegateProvider<Int>()

    private val output by PropertyDelegateProvider<String>()

    @Test
    fun simple1() {
        FenjaSystem.build(logger) {
            val input by changesOf(input)

            val string by input map { it.toString() }

            output bind string
        }

        input.value = 42

        assertEquals("42", output.value)
    }

}
