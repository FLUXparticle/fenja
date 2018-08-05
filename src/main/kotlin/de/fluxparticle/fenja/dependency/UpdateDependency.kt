package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 05.08.18.
 */
interface UpdateDependency<T> : NamedDependency<T> {

    fun getDependency(): Dependency<T>?

    fun update()

}
