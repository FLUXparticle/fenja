package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import org.junit.Assert
import org.junit.Test

/**
 * Created by sreinck on 03.06.18.
 */
class ExprTest {

    private val system = FenjaSystem()

    private var a: Expr<Double> by system.OutputExprDelegate()

    private var b: Expr<Double> by system.OutputExprDelegate()

    private var c: Expr<Double> by system.OutputExprDelegate()

    private var r: Expr<Double> by system.OutputExprDelegate()

    @Test
    fun simple() {
        r = a * b
        ruleToString(r) shouldBeEqualTo "a * b"
    }

    private fun ruleToString(expr: Expr<Double>) =
            (expr as OutputExpr<Double>).getDependency().toString()

    @Test
    fun parentheses() {
        r = a * (b + c)
        ruleToString(r) shouldBeEqualTo "a * (b + c)"
    }

    @Test
    fun minus() {
        r = a + b - (c + a)
        ruleToString(r) shouldBeEqualTo "a + b - (c + a)"
    }

    @Test
    fun minusFactor() {
        r = a + (b - c) * a
        ruleToString(r) shouldBeEqualTo "a + (b - c) * a"
    }

    @Test
    fun map() {
        r = a map { it + 2 }
        ruleToString(r) shouldBeEqualTo "a {}"
    }

    @Test
    fun combine2() {
        r = (a combine b) { a, b -> a + b }
        ruleToString(r) shouldBeEqualTo "(a combine b) {}"
    }

    @Test
    fun combine3() {
        r = (a combine b combine c) { a, b, c -> a + b + c }
        ruleToString(r) shouldBeEqualTo "(a combine b combine c) {}"
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

private infix fun String.shouldBeEqualTo(expected: String) {
    Assert.assertEquals(expected, this)
}
