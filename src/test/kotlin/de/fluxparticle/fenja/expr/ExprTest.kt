package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Created by sreinck on 03.06.18.
 */
class ExprTest {

    private val system = FenjaSystem()

    private var a: Expr<Double> by system.OutputExprDelegate()

    private var b: Expr<Double> by system.OutputExprDelegate()

    private var c: Expr<Double> by system.OutputExprDelegate()

    private var d: Expr<Double> by system.OutputExprDelegate()

    @Test
    fun simple() {
        c = a * b
        c.toString() shouldBeEqualTo "a * b"
    }

    @Test
    fun parentheses() {
        d = a * (b + c)
        d.toString() shouldBeEqualTo "a * (b + c)"
    }

    @Test
    fun minus() {
        d = a + b - (c + a)
        d.toString() shouldBeEqualTo "a + b - (c + a)"
    }

    @Test
    fun minusFactor() {
        d = a + (b - c) * a
        d.toString() shouldBeEqualTo "a + (b - c) * a"
    }

    @Test
    fun map() {
        b = a { it + 2 }
        b.toString() shouldBeEqualTo "a {}"
    }

    @Test
    fun combine() {
        c = (a..b) { u, v -> u + v }
        c.toString() shouldBeEqualTo "(a..b) {}"
    }

    /*
    @Test
    fun list() {
        val list = ListExpr<String>()
        listOf("a", "b", "c").forEach { list.elements.add(it) }
        val max = list.max()
        exprPrinter.printExpr(max) shouldBeEqualTo "max [a, b, c]"
    }
*/

}
