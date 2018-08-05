package de.fluxparticle.fenja.operation.algorithm

import de.fluxparticle.fenja.list.DelegatedList
import de.fluxparticle.fenja.list.WriteList
import de.fluxparticle.fenja.operation.ListComponent
import de.fluxparticle.fenja.operation.ListOperation
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * Created by sreinck on 03.08.18.
 */
@RunWith(Parameterized::class)
class FilterTest(
        private val initList: List<String>,
        val operationStr: String,
        val filterStr: String,
        private val expected: List<String>,
        private val operation: (WriteList<String>) -> Unit,
        private val predicate: (String) -> Boolean
) {

    companion object {

        private val list = listOf("Peter", "Paul", "Maria")

        private val filters = list.map { it.substring(0, 1) }.distinct() + ""

        @JvmStatic
        @Parameters(name = "{0} {1} filter {2} -> {3}")
        fun data(): Collection<Array<Any>> {
            return filters.flatMap { filterStr ->
                fun build(initList: List<String>, operationStr: String, operation: (WriteList<String>) -> Unit, predicate: (String) -> Boolean): Array<Any> {
                    val expected = initList.toMutableList().let { operation.invoke(DelegatedList(it)) ; it.filter(predicate) }
                    return arrayOf(initList, operationStr, filterStr, expected, operation, predicate)
                }
                val predicate = { str: String -> str.startsWith(filterStr) }
                list.mapIndexed { index, element ->
                    val initList = list.minus(element)
                    val operation: (WriteList<String>) -> Unit = { it.add(index, element) }
                    build(initList, "add($index, $element)", operation, predicate)
                } + list.indices.map { index ->
                    val initList = list
                    val operation: (WriteList<String>) -> Unit = { it.removeAt(index) }
                    build(initList, "removeAt($index)", operation, predicate)
                } + list.flatMap { newElement ->
                    val initList = list.minus(newElement)
                    initList.indices.map { index ->
                        val operation: (WriteList<String>) -> Unit = { it.set(index, newElement) }
                        build(initList, "set($index, $newElement)", operation, predicate)
                    }
                }
            }
        }

        fun filterOp(op: ListOperation<String>, predicate: (String) -> Boolean): ListOperation<String> {
            val filter = Filter(predicate)
            op.forEach { it.apply(filter) }
            return filter.build()
        }

    }

    lateinit var op: ListOperation<String>

    @Test
    fun name() {
        val mutableList = initList.toMutableList()

        val initOp = ListOperation(initList.map { add(it) })
        val initFilterOp = ListOperation(initList.filter(predicate).map { add(it) })

        val diffOp1 = filterOp(initOp, predicate)

        println("initOp = ${initOp.toString()}")
        println("diffOp1 = ${diffOp1.toString()}")
        println("initFilterOp = ${initFilterOp.toString()}")

        val operationList = OperationList(mutableList) { this.op = it }

        operation.invoke(operationList)

        println("op = ${op.toString()}")

        val (filterOp1, diffOp2) = Transformer.transform(op, diffOp1)
        val filterOp2 = filterOp(filterOp1, predicate)

        val filterOp = Composer.compose(filterOp1, filterOp2)
        println("filterOp = ${filterOp.toString()}")

        val actual1 = Composer.compose(initFilterOp, filterOp)
        println("actual1 = ${actual1.toString()}")
        assertThat(actual1.toString(), actual1.asIterable(), contains(expected))

        val newInitOp = Composer.compose(initOp, op)
        println("newInitOp = ${newInitOp.toString()}")
        assertThat(newInitOp.toString(), newInitOp.asIterable(), contains(mutableList))

        println("diffOp2 = ${diffOp2.toString()}")
        println("filterOp2 = ${filterOp2.toString()}")

        val diffOp3 = Composer.compose(diffOp2, filterOp2)
        println("diffOp3 = ${diffOp3.toString()}")

        val actual2 = Composer.compose(newInitOp, diffOp3)
        println("actual2 = ${actual2.toString()}")
        assertThat(actual2.toString(), actual2.asIterable(), contains(expected))
    }

    private fun contains(list: List<String>) : Matcher<Iterable<ListComponent<String>>> = if (list.isEmpty()) {
        emptyIterable<ListComponent<String>>()
    } else {
        contains(list.map { Matchers.equalTo(add(it)) })
    }

}
