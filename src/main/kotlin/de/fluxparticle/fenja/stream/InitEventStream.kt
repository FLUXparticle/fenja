package de.fluxparticle.fenja.stream

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 06.08.18.
 */
internal class InitDependency<T>(private val source: Dependency<T>, initEvent: T) : UpdateDependency<T>() {

    init {
        buffer.setValue(0, initEvent)
    }

    override fun update() {
        val transaction = source.getTransaction()
        if (transaction > buffer.getTransaction()) {
            val value = source.getValue()
            buffer.setValue(transaction, value)
        }
    }

    override fun getDependencies(): Sequence<Dependency<*>> {
        return sequenceOf(source)
    }

    override fun toUpdateString(): String {
        return "init $source"
    }

}
