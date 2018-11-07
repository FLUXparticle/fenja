package de.fluxparticle.fenja

import de.fluxparticle.fenja.dependency.*
import de.fluxparticle.fenja.expr.*
import de.fluxparticle.fenja.list.LoopList
import de.fluxparticle.fenja.list.ReadList
import de.fluxparticle.fenja.list.ReadWriteList
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.SilentFenjaSystemLogger
import de.fluxparticle.fenja.operation.ListAddComponent
import de.fluxparticle.fenja.operation.ListOperation
import de.fluxparticle.fenja.operation.ReadWriteListAdapter
import de.fluxparticle.fenja.operation.algorithm.Composer
import de.fluxparticle.fenja.operation.algorithm.Filter
import de.fluxparticle.fenja.operation.algorithm.Inverter
import de.fluxparticle.fenja.operation.algorithm.Transformer
import de.fluxparticle.fenja.stream.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.util.Duration
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sreinck on 31.07.18.
 */
class FenjaSystem private constructor(private val logger: FenjaSystemLogger) {

    companion object {

        @JvmStatic
        fun <R> build(logger: FenjaSystemLogger = SilentFenjaSystemLogger(), buildFunc: FenjaSystem.() -> R): R {
            val system = FenjaSystem(logger)
            val result = buildFunc.invoke(system)
            system.finish()
            return result
        }

    }

    private val transactionProvider = TransactionProvider()

    private val names = HashSet<String>()

    private val sourceDependencies = TreeMap<String, SourceDependency<*>>()

    private val constExpressions = mutableMapOf<Any?, InputExpr<*>>()

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

    fun <T> const(value: T): InputExpr<T> {
        checkNotFinished()
        return constExpressions.getOrPut(value) {
            InputExpr<T>(value.toString()).apply { setValue(value) }
        } as InputExpr<T>
    }

    fun <T> createInputEventStream(name: String): InputEventStream<T> {
        checkNotFinished()
        checkName(name)
        val eventStreamSource = InputEventStream<T>(name)
        sourceDependencies[name] = eventStreamSource.dependency
        return eventStreamSource
    }

    fun <T> createUpdateExpr(name: String): LazyExpr<T> {
        val lazyExpr = LazyExpr<T>(name)
        createUpdateDependency(name, lazyExpr.dependency)
        return lazyExpr
    }

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
/*
            if (source.getTransaction() < 0) {
                throw RuntimeException("variable '${source.name}' does not have a value")
            }
*/
            source.updates = TopologicalSorting().sort(source).result.filterIsInstance<UpdateDependency<*>>()
        }

        val result = (sourceDependencies.values + constExpressions.values.map { it.dependency } )
                .asReversed()
                .fold(TopologicalSorting(), TopologicalSorting::sort)
                .result

        result.forEach {
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

        private val temporary = LinkedHashSet<UpdateDependency<*>>()

        internal fun sort(source: SourceDependency<*>): TopologicalSorting {
            updates[source]?.forEach { visit(it) }
            result.addFirst(source)
            return this
        }

        private fun visit(updateDependency: UpdateDependency<*>) {
            if (!visited.contains(updateDependency)) {
                if (temporary.contains(updateDependency)) {
                    val cycle = temporary.asSequence().dropWhile { it != updateDependency } + updateDependency
                    val str = (sequenceOf("cycle detected") + cycle.map { it.toString() })
                            .joinToString("\n")
                    throw IllegalArgumentException(str)
                }
                updateDependency.forEachChildren { visit(it) }
                visited.add(updateDependency)
                result.addFirst(updateDependency)
            }
        }

        private inline fun UpdateDependency<*>.forEachChildren(func: (UpdateDependency<*>) -> Unit) = also {
            temporary.add(it)
            updates[it]?.forEach(func)
            temporary.remove(it)
        }

    }

    inner class InputExprDelegate<T> internal constructor(private val inputExpr: InputExpr<T>) : ReadOnlyProperty<Any?, InputExpr<T>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): InputExpr<T> {
            return inputExpr
        }
    }

    operator fun <T> ObservableValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): InputExprDelegate<T> {
        val inputExpr = createInputExpr<T>(property.name)
        inputExpr.bind(this)
        return InputExprDelegate(inputExpr)
    }

    operator fun ReadOnlyDoubleProperty.provideDelegate(thisRef: Any?, property: KProperty<*>): InputExprDelegate<Double> {
        val inputExpr = createInputExpr<Double>(property.name)
        inputExpr.bind(this) { it.toDouble() }
        return InputExprDelegate(inputExpr)
    }

    operator fun ReadOnlyIntegerProperty.provideDelegate(thisRef: Any?, property: KProperty<*>): InputExprDelegate<Int> {
        val inputExpr = createInputExpr<Int>(property.name)
        inputExpr.bind(this) { it.toInt() }
        return InputExprDelegate(inputExpr)
    }

    inner class LoopExprDelegate<E : UpdateExpr<T>, T> internal constructor() : ReadWriteProperty<Any?, E> {

        private lateinit var expr: E

        override fun getValue(thisRef: Any?, property: KProperty<*>): E {
            return expr
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: E) {
            if (this::expr.isInitialized) {
                throw IllegalStateException("already set")
            }
            createUpdateDependency(property.name, value.dependency)
            expr = value
        }

    }

    fun <E : UpdateExpr<T>, T> loop(): LoopExprDelegate<E, T> {
        return LoopExprDelegate()
    }

    inner class InputEventStreamDelegate<T>(private val sourceEventStream: InputEventStream<T>) : ReadOnlyProperty<Any?, InputEventStream<T>> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): InputEventStream<T> {
            return sourceEventStream
        }

    }

    inner class PropertyEventStreamDelegateProvider<T>(private val tProperty: Property<T>) {

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): InputEventStreamDelegate<T> {
            val inputEventStream = createInputEventStream<T>(property.name)
            tProperty.addListener { _, _, newValue -> inputEventStream.sendValue(newValue) }
            return InputEventStreamDelegate(inputEventStream)
        }

    }

    inner class NodeEventStreamDelegateProvider<E : Event>(private val node: Node, private val eventType: EventType<E>) {

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): InputEventStreamDelegate<E> {
            val inputEventStream = eventsOf(property.name, node, eventType)
            return InputEventStreamDelegate(inputEventStream)
        }

    }

    fun <E : Event> eventsOf(name: String, node: Node, eventType: EventType<E>): InputEventStream<E> {
        val inputEventStream = createInputEventStream<E>(name)
        node.addEventHandler(eventType) {
            inputEventStream.sendValue(it)
            it.consume()
        }
        return inputEventStream
    }

    inner class UpdateExprDelegate<E : UpdateExpr<T>, T>(private val updateDependency: E) : ReadOnlyProperty<Any?, E> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): E {
            return updateDependency
        }

    }

    inner class UpdateEventStreamDelegate<E : UpdateEventStream<T>, T>(private val updateDependency: E) : ReadOnlyProperty<Any?, E> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): E {
            return updateDependency
        }

    }

    abstract inner class Expr<T> protected constructor() {

        internal abstract val dependency: Dependency<T>
        
        internal val system: FenjaSystem
            get() = this@FenjaSystem

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

        val isSet: Boolean
            get() = dependency.isSet


        fun setValue(value: T) {
            dependency.executeUpdates(value)
        }

        infix fun bind(observable: ObservableValue<T>) {
            setValue(observable.value)
            observable.addListener { _, _, newValue -> setValue(newValue) }
        }

        fun <I> bind(observable: ObservableValue<I>, func: (I) -> T) {
            setValue(func(observable.value))
            observable.addListener { _, _, newValue -> setValue(func(newValue)) }
        }

    }

    abstract inner class UpdateExpr<T> protected constructor() : Expr<T>() {

        abstract override val dependency: UpdateDependency<T>

        final override fun identity() = this

        override fun toString(): String {
            return dependency.toUpdateString()
        }

    }

    fun <T> changesOf(property: Property<T>): PropertyEventStreamDelegateProvider<T> {
        return PropertyEventStreamDelegateProvider(property)
    }

    fun <E : Event> eventsOf(node: Node, eventType: EventType<E>): NodeEventStreamDelegateProvider<E> {
        return NodeEventStreamDelegateProvider(node, eventType)
    }

    operator fun <E : UpdateExpr<T>, T> E.provideDelegate(thisRef: Any?, property: KProperty<*>): UpdateExprDelegate<E, T> {
        createUpdateDependency(property.name, dependency)
        return UpdateExprDelegate(this)
    }

    operator fun <E : UpdateEventStream<T>, T> E.provideDelegate(thisRef: Any?, property: KProperty<*>): UpdateEventStreamDelegate<E, T> {
        createUpdateDependency(property.name, dependency)
        return UpdateEventStreamDelegate(this)
    }

    open inner class SimpleExpr<T> internal constructor(final override val dependency: UpdateDependency<T>) : UpdateExpr<T>() {

        init {
            updateDependencies.add(dependency)
        }

    }

    fun min(sequence: Sequence<FenjaSystem.Expr<Double>>): FenjaSystem.UpdateExpr<Double> = SimpleExpr(MinDependency(sequence.map { it.dependency }))

    fun max(sequence: Sequence<FenjaSystem.Expr<Double>>): FenjaSystem.UpdateExpr<Double> = SimpleExpr(MaxDependency(sequence.map { it.dependency }))

    inner class LazyExpr<T> internal constructor(private val name: String) : SimpleExpr<T>(LazyDependency()) {

        val isLooped: Boolean
            get() = (dependency as LazyDependency).isLooped

        fun setExpr(argument: FenjaSystem.Expr<T>) {
            (dependency as LazyDependency).loop(argument.dependency)
        }

        override fun toString(): String {
            return name
        }

    }

    abstract inner class ListExpr<T> internal constructor() : UpdateExpr<List<T>>() {

        internal abstract val source: EventStream<ListOperation<T>>

        final override val dependency = ListDependency()

        internal val list: ReadList<T>
            get() = dependency.list

        init {
            updateDependencies.add(dependency)
        }

        internal inner class ListDependency : UpdateDependency<List<T>>() {

            private val source
                get() = this@ListExpr.source.dependency

            private val loopList = LoopList<T>()

            internal val list: ReadList<T>
                get() = loopList

            internal fun loopList(list: ReadWriteList<T>) {
                loopList.loop(list)
            }

            init {
                buffer.setValue(-1L, LoopList())
            }

            override fun update() {
                val transaction = source.getTransaction()
                if (transaction > buffer.getTransaction()) {

                    val value = source.getValue()
                    val readWriteList = buffer.getValue() as ReadWriteList<T>
                    value.apply(ReadWriteListAdapter(readWriteList))
                    buffer.setValue(transaction, readWriteList)

                }
            }

            override fun updateLoop() {
                val value = source.getValue()
                value.apply(ReadWriteListAdapter(loopList))
            }

            override fun getDependencies(): Sequence<Dependency<*>> {
                return sequenceOf(source)
            }

            override fun toUpdateString(): String {
                return source.toString()
            }

        }

    }

    inner class HoldListExpr<T> internal constructor(
            override val source: EventStream<ListOperation<T>>
    ): ListExpr<T>() {

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

        override val source = SimpleEventStream(FilterListOperationDependency(source.dependency, predicateExpr.dependency))

        fun reverseTransform(operation: ListOperation<T>): ListOperation<T> {
            val inverseDiffOp = (source.dependency as FilterListOperationDependency).diffOp.apply(Inverter())
            val (reverseTransformation, _) = Transformer.transform(operation, inverseDiffOp)
            return reverseTransformation
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

        internal val system: FenjaSystem
            get() = this@FenjaSystem

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

    infix fun ticker(duration: Duration): TickerEventStreamDelegateProvider {
        return TickerEventStreamDelegateProvider(duration)
    }

    fun <T> never(): EventStream<T> = SimpleEventStream(NoDependency())

    inner class TickerEventStreamDelegateProvider(private val duration: Duration) {

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): InputEventStreamDelegate<Unit> {
            val inputEventStream = createInputEventStream<Unit>(property.name)
            val eventHandler = EventHandler<ActionEvent> { inputEventStream.sendValue(Unit) }
            val keyFrame = KeyFrame(duration, eventHandler)
            val timeline = Timeline(keyFrame)
            timeline.cycleCount = Timeline.INDEFINITE
            timeline.play()
            return InputEventStreamDelegate(inputEventStream)
        }

    }

    inner class InputEventStream<T> internal constructor(
            name: String
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

operator fun FenjaSystem.Expr<Boolean>.not(): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(NotDependency(dependency))

infix fun FenjaSystem.Expr<Boolean>.and(other: FenjaSystem.Expr<Boolean>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(AndDependency(this.dependency, other.dependency))

infix fun FenjaSystem.Expr<Boolean>.or(other: FenjaSystem.Expr<Boolean>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(OrDependency(this.dependency, other.dependency))

infix fun FenjaSystem.Expr<Double>.lessThan(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(CombineDependency2(this.dependency, other.dependency) { a, b -> a < b })

infix fun FenjaSystem.Expr<Double>.lessThanOrEqual(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(CombineDependency2(this.dependency, other.dependency) { a, b -> a <= b })

infix fun FenjaSystem.Expr<Double>.greaterThan(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(CombineDependency2(this.dependency, other.dependency) { a, b -> a > b })

infix fun FenjaSystem.Expr<Double>.greaterThanOrEqual(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Boolean> = system.SimpleExpr(CombineDependency2(this.dependency, other.dependency) { a, b -> a >= b })

operator fun FenjaSystem.Expr<Double>.unaryMinus(): FenjaSystem.UpdateExpr<Double> = system.SimpleExpr(NegDependency(dependency))

operator fun FenjaSystem.Expr<Double>.plus(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Double> = system.SimpleExpr(PlusDependency(this.dependency, other.dependency))

operator fun FenjaSystem.Expr<Double>.plus(other: Double): FenjaSystem.UpdateExpr<Double> = plus(system.const(other))

operator fun FenjaSystem.Expr<Double>.minus(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Double> = system.SimpleExpr(MinusDependency(this.dependency, other.dependency))

operator fun FenjaSystem.Expr<Double>.minus(other: Double): FenjaSystem.UpdateExpr<Double> = minus(system.const(other))

operator fun FenjaSystem.Expr<Double>.times(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Double> = system.SimpleExpr(TimesDependency(this.dependency, other.dependency))

operator fun FenjaSystem.Expr<Double>.times(other: Double): FenjaSystem.UpdateExpr<Double> = times(system.const(other))

operator fun FenjaSystem.Expr<Double>.div(other: FenjaSystem.Expr<Double>): FenjaSystem.UpdateExpr<Double> = system.SimpleExpr(DivDependency(this.dependency, other.dependency))

operator fun FenjaSystem.Expr<Double>.div(other: Double) = div(system.const(other))

infix fun <T> Property<in T>.bind(expr: FenjaSystem.Expr<T>) {
    expr.system.SimpleExpr(PropertyDependency(expr.dependency, this))
}

infix fun <T> Property<in T>.bind(stream: FenjaSystem.EventStream<T>) {
    stream.system.SimpleEventStream(PropertyDependency(stream.dependency, this))
}
