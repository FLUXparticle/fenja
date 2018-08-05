package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
class SilentFenjaSystemLogger : FenjaSystemLogger {

    override fun updateSource(source: SourceDependency<*>, value: Any?) {
        // empty
    }

    override fun executeUpdate(update: UpdateDependency<*>, value: Any?) {
        // empty
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        // empty
    }

}
