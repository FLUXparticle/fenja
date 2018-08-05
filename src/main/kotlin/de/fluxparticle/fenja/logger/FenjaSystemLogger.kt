package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
interface FenjaSystemLogger {

    fun updateSource(source: SourceDependency<*>, value: Any?)

    fun executeUpdate(update: UpdateDependency<*>, value: Any?)

    fun ruleLists(headline: String, map: Map<String, Collection<String>>)

}
