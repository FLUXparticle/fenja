package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
class DelegateFenjaSystemLogger(private var delegate: FenjaSystemLogger) : FenjaSystemLogger {

    fun setDelegate(delegate: FenjaSystemLogger) {
        this.delegate = delegate
    }

    override fun updateSource(source: SourceDependency<*>, value: Any?) {
        delegate.updateSource(source, value)
    }

    override fun executeUpdate(update: UpdateDependency<*>, value: Any?) {
        delegate.executeUpdate(update, value)
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        delegate.ruleLists(headline, map)
    }

}
