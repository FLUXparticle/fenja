package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 08.08.18.
 */
/*
class FilterListExprTest {

    private val system = FenjaSystem(PrintFenjaSystemLogger(System.out))

    private val change: FenjaSystem.InputEventStream<ListOperation<String>> by system.InputEventStreamDelegate()

    private val predicate: FenjaSystem.InputExpr<(String) -> Boolean> by system.InputExprDelegate()

    private var list: FenjaSystem.ListExpr<String> by system.UpdateExprDelegate()

    private var filtered: FenjaSystem.ListExpr<String> by system.UpdateExprDelegate()

    @Test
    fun changeFilter() {
        list = change hold listOf("Peter", "Petra", "Maria")
        predicate.setValue { s: String -> s.startsWith("Pe") }

        filtered = list filter predicate

        system.finish()

        predicate.setValue { s: String -> s.startsWith("Pa") }
    }

}
*/
