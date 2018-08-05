package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
class TeeFenjaSystemLogger(private val left: FenjaSystemLogger, private val right: FenjaSystemLogger) : FenjaSystemLogger {

    override fun updateSource(source: SourceDependency<*>, value: Any?) {
        left.updateSource(source, value)
        right.updateSource(source, value)
    }

    override fun executeUpdate(update: UpdateDependency<*>, value: Any?) {
        left.executeUpdate(update, value)
        right.executeUpdate(update, value)
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        left.ruleLists(headline, map)
        right.ruleLists(headline, map)
    }

}
