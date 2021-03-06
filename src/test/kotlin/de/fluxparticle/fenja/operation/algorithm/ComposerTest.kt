package de.fluxparticle.fenja.operation.algorithm

import org.hamcrest.Matchers.contains
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by sreinck on 03.08.18.
 */
class ComposerTest {

    @Test(expected = NoSuchElementException::class)
    fun exception1() {
        val op1 = listOperation(add("Maria"))
        val op2 = listOperation(add("Peter"), add("Paul"), retain(2))

        Composer.compose(op1, op2).asIterable()
    }

    @Test(expected = NoSuchElementException::class)
    fun exception2() {
        val op1 = listOperation(add("Peter"))
        val op2 = listOperation(remove("Peter"), remove("Paul"))

        Composer.compose(op1, op2).asIterable()
    }

    @Test
    fun simple1() {
        val op1 = listOperation(add("Maria"))
        val op2 = listOperation(add("Peter"), add("Paul"), retain(1))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Peter"), add("Paul"), add("Maria")))
    }

    @Test
    fun simple2() {
        val op1 = listOperation(add("Peter"), add("Paul"))
        val op2 = listOperation(retain(2), add("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Peter"), add("Paul"), add("Maria")))
    }

    @Test
    fun simple3() {
        val op1 = listOperation(add("Peter"), add("Paul"), add("Maria"))
        val op2 = listOperation(retain(2), remove("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Peter"), add("Paul")))
    }

    @Test
    fun simple4() {
        val op1 = listOperation(retain(1), remove("Paul"))
        val op2 = listOperation(retain(1), add("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(retain(1), set("Paul", "Maria")))
    }

    @Test
    fun simple5() {
        val op1 = listOperation(retain(1), remove("Paul"))
        val op2 = listOperation(remove("Peter"), add("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(remove("Peter"), set("Paul", "Maria")))
    }

    @Test
    fun simple6() {
        val op1 = listOperation(add("Peter"), retain(1))
        val op2 = listOperation(retain(2), add("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Peter"), retain(1), add("Maria")))
    }

    @Test
    fun simple7() {
        val op1 = listOperation(add("Peter"))
        val op2 = listOperation(set("Peter", "Paul"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Paul")))
    }

    @Test
    fun simple8() {
        val op1 = listOperation(retain(1))
        val op2 = listOperation(set("Peter", "Paul"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(set("Peter", "Paul")))
    }

    @Test
    fun simple9() {
        val op1 = listOperation(add("Peter"), set("Paul", "Maria"))
        val op2 = listOperation(retain(2))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(add("Peter"), set("Paul", "Maria")))
    }

    @Test
    fun simple10() {
        val op1 = listOperation(set("Peter", "Paul"))
        val op2 = listOperation(retain(1), add("Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(set("Peter", "Paul"), add("Maria")))
    }

    @Test
    fun simple11() {
        val op1 = listOperation(retain(1), set("Peter", "Paul"))
        val op2 = listOperation(retain(1), remove("Paul"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(retain(1), remove("Peter")))
    }

    @Test
    fun simple12() {
        val op1 = listOperation(set("Peter", "Paul"))
        val op2 = listOperation(set("Peter", "Maria"))

        val actual = Composer.compose(op1, op2).asIterable()

        assertThat(actual.toString(), actual, contains(set("Peter", "Maria")))
    }

}
