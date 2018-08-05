package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 05.08.18.
 */
interface Dependency<T> {

    fun eval(): T

    fun <R> accept(visitor: DependencyVisitor<R>): R

}
