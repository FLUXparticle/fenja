package de.fluxparticle.fenja.expr

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

/**
 * Created by sreinck on 03.06.18.
 */
class ExprTest {

    private var a: Expr<Double> by OutputExprDelegate()

    private var b: Expr<Double> by OutputExprDelegate()

    private var c: Expr<Double> by OutputExprDelegate()

    private var d: Expr<Double> by OutputExprDelegate()

    @Test
    fun simple() {
        c = a * b
        print(c) shouldBeEqualTo "a * b"
    }

    @Test
    fun parentheses() {
        d = a * (b + c)
        print(d) shouldBeEqualTo "a * (b + c)"
    }

    @Test
    fun minus() {
        d = a + b - (c + a)
        print(d) shouldBeEqualTo "a + b - (c + a)"
    }

    @Test
    fun minusFactor() {
        d = a + (b - c) * a
        print(d) shouldBeEqualTo "a + (b - c) * a"
    }

    @Test
    fun map() {
        b = a { it + 2 }
        print(b) shouldBeEqualTo "a {}"
    }

    @Test
    fun combine() {
        c = (a..b) { u, v -> u + v }
        print(c) shouldBeEqualTo "(a..b) {}"
    }

    private fun print(expr: Expr<Double>) = expr.accept(DoubleExprPrinter(), false)

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
