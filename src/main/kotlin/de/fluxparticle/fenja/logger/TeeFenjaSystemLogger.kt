package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
class TeeFenjaSystemLogger(private val left: FenjaSystemLogger, private val right: FenjaSystemLogger) : FenjaSystemLogger {

    override fun updateSource(source: SourceDependency<*>) {
        left.updateSource(source)
        right.updateSource(source)
    }

    override fun executeUpdate(update: UpdateDependency<*>) {
        left.executeUpdate(update)
        right.executeUpdate(update)
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        left.ruleLists(headline, map)
        right.ruleLists(headline, map)
    }

}
