package de.fluxparticle.fenja

import de.fluxparticle.fenja.expr.Expr
import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.times
import org.amshove.kluent.shouldEqual
import org.junit.Test
import java.lang.RuntimeException

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystemTest {

    private val system = FenjaSystem()

    private val a: InputExpr<Double> by system.InputExprDelegate()

    private val b: InputExpr<Double> by system.InputExprDelegate()

    private var c: Expr<Double> by system.OutputExprDelegate()

    @Test
    fun answer() {
        a.value = 6.0
        b.value = 7.0
        c = a * b

        system.finish()

        c.eval() shouldEqual 42.0

        a.value = 7.0

        c.eval() shouldEqual 49.0

        b.value = 6.0

        c.eval() shouldEqual 42.0
    }

    @Test(expected = RuntimeException::class)
    fun noRule() {
        c
        system.finish()
    }

}
