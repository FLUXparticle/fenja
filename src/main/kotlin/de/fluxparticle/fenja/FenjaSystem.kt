package de.fluxparticle.fenja

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.MapDependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import de.fluxparticle.fenja.expr.*
import de.fluxparticle.fenja.list.ReadList
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.SilentFenjaSystemLogger
import de.fluxparticle.fenja.operation.ListAddComponent
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.algorithm.Composer
import de.fluxparticle.fenja.operation.algorithm.Filter
import de.fluxparticle.fenja.operation.algorithm.Inverter
import de.fluxparticle.fenja.operation.algorithm.Transformer
import de.fluxparticle.fenja.stream.*
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystem private constructor(private val logger: FenjaSystemLogger) {

    companion object {

        @JvmStatic
        fun build(logger: FenjaSystemLogger = SilentFenjaSystemLogger(), buildFunc: FenjaSystem.() -> Unit) {
            val system = FenjaSystem(logger)
            buildFunc.invoke(system)
            system.finish()
        }

    }

    private val transactionProvider = TransactionProvider()

    private val names = HashSet<String>()

    private val sourceDependencies = TreeMap<String, SourceDependency<*>>()

    private val updateDependencies = mutableListOf<UpdateDependency<*>>()

    private var finished: Boolean = false

    private val updates: MutableMap<Dependency<*>, MutableList<UpdateDependency<*>>> = HashMap()

    fun <T> createInputExpr(name: String): InputExpr<T> {
        checkNotFinished()
        checkName(name)
        val inputExpr = InputExpr<T>(name)
        sourceDependencies[name] = inputExpr.dependency
        return inputExpr
    }

    fun <T> createInputEventStream(name: String): InputEventStream<T> {
        checkNotFinished()
        checkName(name)
        val eventStreamSource = InputEventStream<T>(name, transactionProvider, logger)
        sourceDependencies[name] = eventStreamSource.dependency
        return eventStreamSource
    }

/*
    TODO
    fun <T> createUpdateExpr(name: String): LazyExpr<T> {
        val lazyExpr = LazyExpr<T>(name)
        createUpdateDependency(name, lazyExpr.dependency)
        return lazyExpr
    }
*/

    private fun <T> createUpdateDependency(name: String, dependency: UpdateDependency<T>) {
        checkNotFinished()
        checkName(name)
        dependency.name = name
    }

    private fun finish() {
        checkNotFinished()

        // TODO cycle detection

        updateDependencies.forEach { expr ->
            expr.getDependencies().forEach { dep ->
                updates.getOrPut(dep) { ArrayList() }.add(expr)
            }
        }

        logger.ruleLists("updates", updates);

        sourceDependencies.forEach { _, source ->
            if (source is InputExpr<*> && source.getTransaction() < 0) {
                throw RuntimeException("variable " + source.name + " does not have a value")
            }
            source.updates = TopologicalSorting().sort(source).result.filterIsInstance<UpdateDependency<*>>()
        }

        sourceDependencies.values
                .reversed()
                .fold(TopologicalSorting(), TopologicalSorting::sort)
                .result
                .forEach {
                    when (it) {
                        is SourceDependency<*> -> logger.updateSource(it)
                        is UpdateDependency<*> -> {
                            it.update()
                            logger.executeUpdate(it)
                            it.updateLoop()
                        }
                    }
                }

        finished = true
    }

    private fun checkNotFinished() {
        if (finished) {
            throw IllegalStateException("already finished")
        }
    }

    private fun checkName(name: String) {
        if (names.contains(name)) {
            throw IllegalArgumentException("Variable $name already exists")
        }
        names.add(name)
    }

    private inner class TopologicalSorting {

        val result = LinkedList<Dependency<*>>()

        private val visited = HashSet<UpdateDependency<*>>()

        internal fun sort(source: SourceDependency<*>): TopologicalSorting {
            updates[source]?.forEach { visit(it) }
            result.addFirst(source)
            return this
        }

        private fun visit(updateDependency: UpdateDependency<*>) {
            if (!visited.contains(updateDependency)) {
                updates[updateDependency]?.forEach { visit(it) }
                visited.add(updateDependency)
                result.addFirst(updateDependency)
            }
        }

    }

    inner class InputExprDelegate<T> internal constructor(private val inputExpr: InputExpr<T>) : ReadOnlyProperty<Any?, InputExpr<T>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): InputExpr<T> {
            return inputExpr
        }
    }

    operator fun <T> ObservableValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): InputExprDelegate<T> {
        val inputExpr = createInputExpr<T>(property.name)
        inputExpr.setValue(value)
        addListener { _, _, newValue -> inputExpr.setValue(newValue) }
        return InputExprDelegate(inputExpr)
    }

    @JvmName("provideNumberDelegate")
    operator fun ObservableValue<Number>.provideDelegate(thisRef: Any?, property: KProperty<*>): InputExprDelegate<Double> {
        val inputExpr = createInputExpr<Double>(property.name)
        inputExpr.setValue(value.toDouble())
        addListener { _, _, newValue -> inputExpr.setValue(newValue.toDouble()) }
        return InputExprDelegate(inputExpr)
    }

    inner class InputEventStreamDelegate<T> : ReadOnlyProperty<Any, InputEventStream<T>> {

        private var sourceEventStream: InputEventStream<T>? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): InputEventStream<T> {
            return sourceEventStream ?: createInputEventStream<T>(property.name).also {
                sourceEventStream = it
            }
        }

    }

    inner class UpdateExprDelegate<E : UpdateExpr<T>, T>(private val updateDependency: E) : ReadOnlyProperty<Any?, E> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): E {
            return updateDependency
        }

    }

    inner class UpdateEventStreamDelegate<E : UpdateEventStream<T>, T> : ReadWriteProperty<Any, E> {

        private lateinit var updateDependency: E

        override fun getValue(thisRef: Any, property: KProperty<*>): E {
            return updateDependency
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: E) {
            checkNotFinished()
            if (this::updateDependency.isInitialized) {
                throw IllegalStateException("already assigned")
            }
            createUpdateDependency(property.name, value.dependency)
            updateDependency = value
        }

    }

    abstract inner class Expr<T> protected constructor() {

        internal abstract val dependency: Dependency<T>

        fun sample(): T = dependency.getValue()

        abstract fun identity(): UpdateExpr<T>

        infix fun <R> map(func: (T) -> R): UpdateExpr<R> = SimpleExpr(MapDependency(dependency, func))

        infix fun <S> combine(other: Expr<S>) = CombineExprBuilder2(this, other)

    }

    inner class CombineExprBuilder2<A, B> internal constructor(
            private val paramA: Expr<A>,
            private val paramB: Expr<B>
    ) {

        operator fun <R> invoke(func: (A, B) -> R): UpdateExpr<R> = SimpleExpr(CombineDependency2(paramA.dependency, paramB.dependency, func))

        infix fun <C> combine(next: Expr<C>) = CombineExprBuilder3(paramA, paramB, next)

    }

    inner class CombineExprBuilder3<A, B, C> internal constructor(
            private val paramA: Expr<A>,
            private val paramB: Expr<B>,
            private val paramC: Expr<C>
    ) {

        operator fun <R> invoke(func: (A, B, C) -> R) : FenjaSystem.UpdateExpr<R> = SimpleExpr(CombineDependency3(paramA.dependency, paramB.dependency, paramC.dependency, func))

    }

    abstract inner class SourceExpr<T> internal constructor() : Expr<T>() {

        abstract override val dependency: SourceDependency<T>

        final override fun identity(): UpdateExpr<T> = SimpleExpr(IdentityDependency(dependency))

        override fun toString(): String {
            return dependency.toString()
        }

    }

    inner class InputExpr<T> internal constructor(name: String) : SourceExpr<T>() {

        override val dependency = SourceDependency<T>(name, transactionProvider, logger)

        fun setValue(value: T) {
            dependency.executeUpdates(value)
        }

    }

    abstract inner class UpdateExpr<T> protected constructor() : Expr<T>() {

        abstract override val dependency: UpdateDependency<T>

        final override fun identity() = this

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): UpdateExprDelegate<UpdateExpr<T>, T> {
            createUpdateDependency(property.name, dependency)
            return UpdateExprDelegate(this)
        }

        override fun toString(): String {
            return dependency.toUpdateString()
        }

    }

    inner class SimpleExpr<T> internal constructor(override val dependency: UpdateDependency<T>) : UpdateExpr<T>() {

        init {
            updateDependencies.add(dependency)
        }

    }

    operator fun Expr<Boolean>.not(): UpdateExpr<Boolean> = SimpleExpr(NotDependency(dependency))

    infix fun Expr<Boolean>.and(other: Expr<Boolean>): UpdateExpr<Boolean> = SimpleExpr(AndDependency(this.dependency, other.dependency))

    infix fun Expr<Boolean>.or(other: Expr<Boolean>): UpdateExpr<Boolean> = SimpleExpr(OrDependency(this.dependency, other.dependency))

    operator fun Expr<Double>.unaryMinus(): UpdateExpr<Double> = SimpleExpr(NegDependency(dependency))

    operator fun Expr<Double>.plus(other: Expr<Double>): UpdateExpr<Double> = SimpleExpr(PlusDependency(this.dependency, other.dependency))

    operator fun Expr<Double>.plus(other: Double): UpdateExpr<Double> = SimpleExpr(PlusDependency(dependency, ConstDependency(other)))

    operator fun Expr<Double>.minus(other: Expr<Double>): UpdateExpr<Double> = SimpleExpr(MinusDependency(this.dependency, other.dependency))

    operator fun Expr<Double>.minus(other: Double): UpdateExpr<Double> = SimpleExpr(MinusDependency(dependency, ConstDependency(other)))

    operator fun Expr<Double>.times(other: Expr<Double>): UpdateExpr<Double> = SimpleExpr(TimesDependency(this.dependency, other.dependency))

    operator fun Expr<Double>.times(other: Double): UpdateExpr<Double> = SimpleExpr(TimesDependency(dependency, ConstDependency(other)))

    operator fun Expr<Double>.div(other: Expr<Double>): UpdateExpr<Double> = SimpleExpr(DivDependency(this.dependency, other.dependency))

    operator fun Expr<Double>.div(other: Double) = SimpleExpr(DivDependency(dependency, ConstDependency(other)))

    fun min(sequence: Sequence<Expr<Double>>): UpdateExpr<Double> = SimpleExpr(MinDependency(sequence.map { it.dependency }))

    fun max(sequence: Sequence<Expr<Double>>): UpdateExpr<Double> = SimpleExpr(MaxDependency(sequence.map { it.dependency }))

    infix fun <T> Property<in T>.bind(expr: FenjaSystem.UpdateExpr<T>) {
        SimpleExpr(PropertyDependency(expr.dependency, this))
    }

    abstract inner class ListExpr<T> internal constructor() : UpdateExpr<List<T>>() {

        abstract override val dependency: ListDependency<T>

        internal abstract val source: EventStream<ListOperation<T>>

        internal val list: ReadList<T>
            get() = dependency.list

    }

    inner class HoldListExpr<T> internal constructor(
            override val source: EventStream<ListOperation<T>>
    ): ListExpr<T>() {

        override val dependency: ListDependency<T> = ListDependency(source.dependency)

    }

    infix fun <T> EventStream<ListOperation<T>>.hold(initList: List<T>): ListExpr<T> {
        val initEvent = ListOperation(initList.map { ListAddComponent(it) })
        val source = SimpleEventStream(InitDependency(dependency, initEvent))
        return HoldListExpr(source)
    }

    inner class FilterListExpr<T> internal constructor(
            source: EventStream<ListOperation<T>>,
            predicateExpr: FenjaSystem.Expr<(T) -> Boolean>
    ) : ListExpr<T>() {

        override val source = FilterListOperationEventStream(source, predicateExpr)

        override val dependency: ListDependency<T> = ListDependency(this.source.dependency)

        fun reverseTransform(operation: ListOperation<T>): ListOperation<T> {
            val inverseDiffOp = source.dependency.diffOp.apply(Inverter())
            val (reverseTransformation, _) = Transformer.transform(operation, inverseDiffOp)
            return reverseTransformation
        }

        internal inner class FilterListOperationEventStream(
                source: EventStream<ListOperation<T>>,
                predicateExpr: FenjaSystem.Expr<(T) -> Boolean>
        ) : FenjaSystem.UpdateEventStream<ListOperation<T>>() {

            override val dependency = FilterListOperationDependency(source.dependency, predicateExpr.dependency)

        }

        internal inner class FilterListOperationDependency(
                private val source: Dependency<ListOperation<T>>,
                private val predicateExpr: Dependency<(T) -> Boolean>
        ) : UpdateDependency<ListOperation<T>>() {

            internal var diffOp: ListOperation<T> = ListOperation(emptyList())

            override fun update() {
                val sourceTransaction = source.getTransaction()
                val predicate = predicateExpr.getValue()

                if (sourceTransaction > buffer.getTransaction()) {
                    val op = source.getValue()

                    val (filterOp1, diffOp2) = Transformer.transform(op, diffOp)
                    val filterOp2 = filterOp1.apply(Filter(predicate))

                    val filterOp = Composer.compose(filterOp1, filterOp2)
                    val diffOp3 = Composer.compose(diffOp2, filterOp2)

                    diffOp = diffOp3
                    buffer.setValue(sourceTransaction, filterOp)
                } else {
                    val filterTransaction = predicateExpr.getTransaction()

                    val invOldDiff = diffOp.apply(Inverter())
                    val initFilteredOp = ListOperation(list.map { ListAddComponent(it) })
                    val initOp = Composer.compose(initFilteredOp, invOldDiff)
                    val newDiff = initOp.apply(Filter(predicate))
                    val filterOp = Composer.compose(invOldDiff, newDiff)

                    diffOp = newDiff
                    buffer.setValue(filterTransaction, filterOp)
                }
            }

            override fun getDependencies(): Sequence<Dependency<*>> {
                return sequenceOf(source, predicateExpr)
            }

            override fun toUpdateString(): String {
                return "$source filter $predicateExpr"
            }

        }

    }

    infix fun <T> ListExpr<T>.filter(predicateExpr: FenjaSystem.Expr<(T) -> Boolean>): FenjaSystem.FilterListExpr<T> {
        return FilterListExpr(source, predicateExpr)
    }

    abstract inner class EventStream<T> protected constructor() {

        internal abstract val dependency: Dependency<T>

        infix fun <R> map(func: (T) -> R): UpdateEventStream<R> = SimpleEventStream(MapDependency(dependency, func))

        infix fun hold(initValue: T): UpdateExpr<T> = SimpleExpr(EventStreamHoldDependency(dependency, initValue))

        infix fun filter(predicate: (T) -> Boolean): UpdateEventStream<T> = SimpleEventStream(FilterDependency(dependency, predicate))

        infix fun orElse(other: EventStream<T>): UpdateEventStream<T> = SimpleEventStream(OrElseDependency(this.dependency, other.dependency))

        infix fun <S> zipWith(other: EventStream<S>) = ZipWithEventStreamBuilder(this, other)

    }

    inner class ZipWithEventStreamBuilder<T, S>(private val source1: EventStream<T>, private val source2: EventStream<S>) {

        operator fun <R> invoke(func: (T, S) -> R): EventStream<R> = SimpleEventStream(ZipWithDependency(source1.dependency, source2.dependency, func))

    }

    abstract inner class SourceEventStream<T> protected constructor() : EventStream<T>() {

        abstract override val dependency: SourceDependency<T>

    }

    inner class InputEventStream<T> internal constructor(
            name: String,
            transactionProvider: TransactionProvider,
            logger: FenjaSystemLogger
    ) : SourceEventStream<T>() {

        override val dependency = SourceDependency<T>(name, transactionProvider, logger)

        fun sendValue(value: T) {
            dependency.executeUpdates(value)
        }

    }

    abstract inner class UpdateEventStream<T> protected constructor() : EventStream<T>() {

        abstract override val dependency: UpdateDependency<T>

    }

    inner class SimpleEventStream<T> internal constructor(override val dependency: UpdateDependency<T>) : UpdateEventStream<T>() {

        init {
            updateDependencies.add(dependency)
        }

    }

}
