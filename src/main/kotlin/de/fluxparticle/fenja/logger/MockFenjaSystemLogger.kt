package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency

/**
 * Created by sreinck on 15.08.18.
 */
class MockFenjaSystemLogger : FenjaSystemLogger() {

    private val privateUpdates = mutableListOf<String>()

    var sources: Int = 0
        private set

    val updates: List<String>
        get() = privateUpdates

    override fun updateSource(source: SourceDependency<*>) {
        sources++
    }

    override fun executeUpdate(update: UpdateDependency<*>) {
        update.name?.let { privateUpdates.add(it) }
    }

    override fun ruleLists(headline: String, map: Map<Dependency<*>, List<UpdateDependency<*>>>) {
        // empty
    }

}
