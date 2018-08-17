package de.fluxparticle.fenja

import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import org.junit.Assert
import org.junit.Test

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystemTest {

    private val logger = PrintFenjaSystemLogger(System.out)

    private val a by DoublePropertyDelegateProvider()

    private val b by DoublePropertyDelegateProvider()

    private val r by DoublePropertyDelegateProvider()

    @Test
    fun answer() {
        a.value = 6.0
        b.value = 7.0

        FenjaSystem.build(logger) {
            val a by a
            val b by b

            val c by a * b

            r bind c
        }

        r.value shouldEqual 42.0

        a.value = 7.0

        r.value shouldEqual 49.0

        b.value = 6.0

        r.value shouldEqual 42.0
    }

/*
    @Test
    @Ignore
    fun const() {
        r = ConstExpr(42.0)

        system.finish()

        r.sample() shouldEqual 42.0
    }
*/

/*
    @Test(expected = RuntimeException::class)
    fun noRule() {
        r
        system.finish()
    }
*/

}

private infix fun Any.shouldEqual(expected: Any) {
    Assert.assertEquals(expected, this)
}
