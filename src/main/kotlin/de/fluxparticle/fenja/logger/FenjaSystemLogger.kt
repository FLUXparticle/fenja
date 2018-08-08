package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 25.07.18.
 */
abstract class FenjaSystemLogger {

    internal abstract fun updateSource(source: SourceDependency<*>)

    internal abstract fun executeUpdate(update: UpdateDependency<*>)

    internal abstract fun ruleLists(headline: String, map: Map<Dependency<*>, List<UpdateDependency<*>>>)

}
