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

    private var r: Expr<Double> by system.OutputExprDelegate()

    @Test
    fun simple() {
        r = a * b
        r.toString() shouldBeEqualTo "a * b"
    }

    @Test
    fun parentheses() {
        r = a * (b + c)
        r.toString() shouldBeEqualTo "a * (b + c)"
    }

    @Test
    fun minus() {
        r = a + b - (c + a)
        r.toString() shouldBeEqualTo "a + b - (c + a)"
    }

    @Test
    fun minusFactor() {
        r = a + (b - c) * a
        r.toString() shouldBeEqualTo "a + (b - c) * a"
    }

    @Test
    fun map() {
        r = a { it + 2 }
        r.toString() shouldBeEqualTo "a {}"
    }

    @Test
    fun combine2() {
        r = (a..b) { a, b -> a + b }
        r.toString() shouldBeEqualTo "(a..b) {}"
    }

    @Test
    fun combine3() {
        r = (a..b..c) { a, b, c -> a + b + c }
        r.toString() shouldBeEqualTo "(a..b..c) {}"
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
