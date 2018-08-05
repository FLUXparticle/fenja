package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 05.08.18.
 */
interface SourceDependency<T> : NamedDependency<T> {

    var updates: List<UpdateDependency<*>>?

}
