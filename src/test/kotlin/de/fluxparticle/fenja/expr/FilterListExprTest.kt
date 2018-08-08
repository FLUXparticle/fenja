package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.stream.InputEventStream
import org.junit.Test

/**
 * Created by sreinck on 08.08.18.
 */
class FilterListExprTest {

    private val system = FenjaSystem(PrintFenjaSystemLogger(System.out))

    private val change: InputEventStream<ListOperation<String>> by system.InputEventStreamDelegate()

    private val predicate: InputExpr<(String) -> Boolean> by system.InputExprDelegate()

    private var list: ListExpr<String> by system.UpdateExprDelegate()

    private var filtered: ListExpr<String> by system.UpdateExprDelegate()

    @Test
    fun changeFilter() {
        list = change hold listOf("Peter", "Petra", "Maria")
        predicate.setValue { s: String -> s.startsWith("Pe") }

        filtered = list filter predicate

        system.finish()

        predicate.setValue { s: String -> s.startsWith("Pa") }
    }

}
