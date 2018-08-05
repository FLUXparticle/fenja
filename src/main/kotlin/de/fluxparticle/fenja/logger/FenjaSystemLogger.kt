package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
interface FenjaSystemLogger {

    fun updateSource(source: SourceDependency<*>)

    fun executeUpdate(update: UpdateDependency<*>)

    fun ruleLists(headline: String, map: Map<String, Collection<String>>)

}
