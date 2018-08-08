package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.stream.InputEventStream
import org.junit.Test

/**
 * Created by sreinck on 08.08.18.
 */
class ListExprTest {

    private val system = FenjaSystem(PrintFenjaSystemLogger(System.out))

    private val change: InputEventStream<ListOperation<String>> by system.InputEventStreamDelegate()

    private var list: ListExpr<String> by system.UpdateExprDelegate()

    @Test
    fun add() {
        list = change hold emptyList()

        system.finish()

        val op1 = list.buildAddOperation("Peter")
        change.sendValue(op1)
    }

}
