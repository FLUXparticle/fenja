package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

/**
 * Created by sreinck on 03.06.18.
 */
class ExprTest {

    private val system = FenjaSystem()

    private var a: UpdateExpr<Double> by system.UpdateExprDelegate()

    private var b: UpdateExpr<Double> by system.UpdateExprDelegate()

    private var c: UpdateExpr<Double> by system.UpdateExprDelegate()

    private var r: UpdateExpr<Double> by system.UpdateExprDelegate()

    init {
        a = ConstExpr(2.0)
        b = ConstExpr(3.0)
        c = ConstExpr(5.0)
    }

    @Test
    fun simple() {
        r = a * b
        r.toString() shouldBeEqualTo "a * b"
    }

    @Test
    @Ignore
    fun parentheses() {
        r = a * (b + c)
        r.toString() shouldBeEqualTo "a * (b + c)"
    }

    @Test
    @Ignore
    fun minus() {
        r = a + b - (c + a)
        r.toString() shouldBeEqualTo "a + b - (c + a)"
    }

    @Test
    @Ignore
    fun minusFactor() {
        r = a + (b - c) * a
        r.toString() shouldBeEqualTo "a + (b - c) * a"
    }

    @Test
    fun map() {
        r = a map { it + 2 }
        r.toString() shouldBeEqualTo "a map {}"
    }

    @Test
    fun combine2() {
        r = (a combine b) { a, b -> a + b }
        r.toString() shouldBeEqualTo "(a combine b) {}"
    }

    @Test
    fun combine3() {
        r = (a combine b combine c) { a, b, c -> a + b + c }
        r.toString() shouldBeEqualTo "(a combine b combine c) {}"
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
