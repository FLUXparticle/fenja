package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
class DelegateFenjaSystemLogger(private var delegate: FenjaSystemLogger) : FenjaSystemLogger() {

    fun setDelegate(delegate: FenjaSystemLogger) {
        this.delegate = delegate
    }

    override fun updateSource(source: SourceDependency<*>) {
        delegate.updateSource(source)
    }

    override fun executeUpdate(update: UpdateDependency<*>) {
        delegate.executeUpdate(update)
    }

    override fun ruleLists(headline: String, map: Map<Dependency<*>, List<UpdateDependency<*>>>) {
        delegate.ruleLists(headline, map)
    }

}
