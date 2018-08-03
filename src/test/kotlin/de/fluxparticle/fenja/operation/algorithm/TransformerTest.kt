package de.fluxparticle.fenja.operation.algorithm

import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

/**
 * Created by sreinck on 03.08.18.
 */
class TransformerTest {

    @Test
    fun simple1() {
        val clientOp = sequenceOf(add("Peter"), add("Paul"))
        val serverOp = sequenceOf(add("Maria"))

        val (actualClient, actualServer) = Transformer.transform(clientOp, serverOp)

        Assert.assertThat(actualClient.message(), actualClient.asIterable(), Matchers.contains(add("Peter"), add("Paul"), retain(1)))
        Assert.assertThat(actualServer.message(), actualServer.asIterable(), Matchers.contains(retain(2), add("Maria")))
    }

}

private fun <T> Sequence<T>.message(): String {
    return asIterable().message()
}
