package de.fluxparticle.fenja.expr

import org.junit.Assert

/**
 * Created by sreinck on 03.06.18.
 */
/*
class ExprTest {

    private val system = FenjaSystem()

    private val a: FenjaSystem.InputExpr<Double> by system.InputExprDelegate()

    private val b: FenjaSystem.InputExpr<Double> by system.InputExprDelegate()

    private val c: FenjaSystem.InputExpr<Double> by system.InputExprDelegate()

    private var r: UpdateExpr<Double> by system.UpdateExprDelegate()

    init {
        a.setValue(2.0)
        b.setValue(3.0)
        c.setValue(5.0)
    }

    @Test
    fun simple() {
        r = a * b

        system.finish()

        r.toString() shouldBeEqualTo "a * b"
        r.sample() shouldBeEqualTo 6.0
    }

    @Test
    @Ignore
    fun parentheses() {
        r = a * (b + c)

        system.finish()

        r.toString() shouldBeEqualTo "a * (b + c)"
        r.sample() shouldBeEqualTo 16.0
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

        system.finish()

        r.toString() shouldBeEqualTo "a map {}"
        r.sample() shouldBeEqualTo 4.0
    }

    @Test
    fun combine2() {
        r = (a combine b) { a, b -> a + b }

        system.finish()

        r.toString() shouldBeEqualTo "(a combine b) {}"
        r.sample() shouldBeEqualTo 5.0
    }

    @Test
    fun combine3() {
        r = (a combine b combine c) { a, b, c -> a + b + c }
        r.toString() shouldBeEqualTo "(a combine b combine c) {}"
    }

    @Test
    fun max() {
        val list = listOf(a, b, c)
        r = MaxExpr(list.asSequence())
        r.toString() shouldBeEqualTo "max [a, b, c]"
    }

}
*/

private infix fun String.shouldBeEqualTo(expected: String) {
    Assert.assertEquals(expected, this)
}

private infix fun Double.shouldBeEqualTo(expected: Double) {
    Assert.assertEquals(expected, this, 0.0)
}
