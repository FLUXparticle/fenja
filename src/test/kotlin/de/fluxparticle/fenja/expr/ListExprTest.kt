package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.PropertyDelegateProvider
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import de.fluxparticle.fenja.operation.ListOperation
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by sreinck on 08.08.18.
 */
class ListExprTest {

    private val logger = PrintFenjaSystemLogger(System.out)

    private val listOperation by PropertyDelegateProvider<ListOperation<String>>()

    private val list = mutableListOf<String>()

    @Test
    fun add() {
        lateinit var op1: ListOperation<String>

        FenjaSystem.build(logger) {
            val change by changesOf(listOperation)

            val listExpr by change hold emptyList()
            op1 = listExpr.buildAddOperation("Peter")

            list bind listExpr
        }

        listOperation.value = op1

        assertThat(list.toString(), list, Matchers.contains("Peter"))
    }

}
