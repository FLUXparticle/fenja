package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 05.08.18.
 */
interface NamedDependency<T> : Dependency<T> {

    val name: String

}
