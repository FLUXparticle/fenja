package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.DependencyVisitor
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.Composer
import de.fluxparticle.fenja.operation.algorithm.Filter
import de.fluxparticle.fenja.operation.algorithm.Transformer
import de.fluxparticle.fenja.stream.EventStream

/**
 * Created by sreinck on 06.08.18.
 */
/*
class FilterListExpr<T> internal constructor(
        source: EventStream<ListOperation<T>>,
        predicateExpr: Expr<(T) -> Boolean>
): ListExpr<T>(
        FilterListOperationEventStream(source, predicateExpr)
) {

}
*/

private class FilterListOperationEventStream<T>(
        private val source: EventStream<ListOperation<T>>,
        private val predicateExpr: Expr<(T) -> Boolean>
) : EventStream<ListOperation<T>>() {

    private var diffOp: ListOperation<T> = ListOperation(emptyList())

//    private var lastTransaction: Long = -1L

//    private var lastChangeTransaction: Long = -1L

    override fun getTransaction(): Long {
/*
        val transaction = source.getTransaction()
        if (transaction > lastTransaction) {

            lastTransaction = transaction
        }
        return lastChangeTransaction
*/
        return source.getTransaction()
    }

    override fun eval(): ListOperation<T> {
        val op = source.eval()
        val predicate = predicateExpr.eval()

        val (filterOp1, diffOp2) = Transformer.transform(op, diffOp)
        val filterOp2 = filterOp1.apply(Filter(predicate))

        val filterOp = Composer.compose(filterOp1, filterOp2)
        val diffOp3 = Composer.compose(diffOp2, filterOp2)

        diffOp = diffOp3
        return filterOp
    }

    override fun <R> accept(visitor: DependencyVisitor<R>): R {
        return visitor.visit(this, source, predicateExpr)
    }

    override fun toString(): String {
        return "$source filter $predicateExpr"
    }

}

infix fun <T> Expr<List<T>>.filter(predicateExpr: Expr<(T) -> Boolean>): ListExpr<T> {
    val listExpr = asListExpr()
    val eventStream = FilterListOperationEventStream(listExpr.source, predicateExpr)
    return ListExpr(eventStream)
}
