package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.Composer
import de.fluxparticle.fenja.operation.algorithm.Filter
import de.fluxparticle.fenja.operation.algorithm.Transformer
import de.fluxparticle.fenja.stream.UpdateEventStream

/**
 * Created by sreinck on 06.08.18.
 */
class FilterListExpr<T> internal constructor(
        source: FilterListOperationEventStream<T>
): ListExpr<T>(ListDependency(source.dependency)) {

}

internal class FilterListOperationEventStream<T>(
        listExpr: ListExpr<T>,
        predicateExpr: Expr<(T) -> Boolean>
) : UpdateEventStream<ListOperation<T>>(FilterListOperationDependency(listExpr.dependency, predicateExpr.dependency)) {

    private class FilterListOperationDependency<T>(
            listDependency: ListDependency<T>,
            private val predicateExpr: Dependency<(T) -> Boolean>
    ) : UpdateDependency<ListOperation<T>>() {

        private var diffOp: ListOperation<T> = ListOperation(emptyList())

        private val source = listDependency.source

//    private var lastTransaction: Long = -1L

//    private var lastChangeTransaction: Long = -1L

        override fun update() {
            val transaction = source.getTransaction()

            val op = source.getValue()
            val predicate = predicateExpr.getValue()

            val (filterOp1, diffOp2) = Transformer.transform(op, diffOp)
            val filterOp2 = filterOp1.apply(Filter(predicate))

            val filterOp = Composer.compose(filterOp1, filterOp2)
            val diffOp3 = Composer.compose(diffOp2, filterOp2)

            diffOp = diffOp3
            buffer.setValue(transaction, filterOp)
        }

        override fun getDependencies(): Sequence<Dependency<*>> {
            return sequenceOf(source, predicateExpr)
        }

        override fun toUpdateString(): String {
            return "$source filter $predicateExpr"
        }
    }

    /*
    fun <T> reverseTransform(operation: ListOperation<T>): ListOperation<T> {

    }
*/

}
