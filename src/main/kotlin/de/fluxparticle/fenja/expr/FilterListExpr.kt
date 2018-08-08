package de.fluxparticle.fenja.expr

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.Composer
import de.fluxparticle.fenja.operation.algorithm.Filter
import de.fluxparticle.fenja.operation.algorithm.Inverter
import de.fluxparticle.fenja.operation.algorithm.Transformer
import de.fluxparticle.fenja.stream.EventStream
import de.fluxparticle.fenja.stream.UpdateEventStream

/**
 * Created by sreinck on 06.08.18.
 */
class FilterListExpr<T> internal constructor(
        override val source: FilterListOperationEventStream<T>,
        predicateExpr: Expr<(T) -> Boolean>
) : ListExpr<T>() {

    override val dependency: ListDependency<T> = FilterListDependency(source.dependency, predicateExpr.dependency)

    private class FilterListDependency<T>(
            source: Dependency<ListOperation<T>>,
            private val predicateExpr: Dependency<(T) -> Boolean>
            ) : ListDependency<T>(source) {

    }

    fun reverseTransform(operation: ListOperation<T>): ListOperation<T> {
        val inverseDiffOp = source.dependency.diffOp.apply(Inverter())
        val (reverseTransformation, _) = Transformer.transform(operation, inverseDiffOp)
        return reverseTransformation
    }

}

internal class FilterListOperationEventStream<T>(
        source: EventStream<ListOperation<T>>,
        predicateExpr: Expr<(T) -> Boolean>
) : UpdateEventStream<ListOperation<T>>() {

    override val dependency = FilterListOperationDependency(source.dependency, predicateExpr.dependency)

    internal class FilterListOperationDependency<T>(
            private val source: Dependency<ListOperation<T>>,
            private val predicateExpr: Dependency<(T) -> Boolean>
    ) : UpdateDependency<ListOperation<T>>() {

        internal var diffOp: ListOperation<T> = ListOperation(emptyList())

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

}
