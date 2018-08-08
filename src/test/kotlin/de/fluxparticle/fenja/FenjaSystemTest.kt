package de.fluxparticle.fenja

import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.UpdateExpr
import de.fluxparticle.fenja.expr.times
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import org.junit.Assert
import org.junit.Test
import java.lang.RuntimeException

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystemTest {

    private val system = FenjaSystem(PrintFenjaSystemLogger(System.out))

    private val a: InputExpr<Double> by system.InputExprDelegate()

    private val b: InputExpr<Double> by system.InputExprDelegate()

    private var c: UpdateExpr<Double> by system.UpdateExprDelegate()

    @Test
    fun answer() {
        a.setValue(6.0)
        b.setValue(7.0)
        c = a * b

        system.finish()

        c.sample() shouldEqual 42.0

        a.setValue(7.0)

        c.sample() shouldEqual 49.0

        b.setValue(6.0)

        c.sample() shouldEqual 42.0
    }

/*
    @Test
    @Ignore
    fun const() {
        c = ConstExpr(42.0)

        system.finish()

        c.sample() shouldEqual 42.0
    }
*/

    @Test(expected = RuntimeException::class)
    fun noRule() {
        c
        system.finish()
    }

}

private infix fun Any.shouldEqual(expected: Any) {
    Assert.assertEquals(expected, this)
}
