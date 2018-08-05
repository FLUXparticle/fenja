package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 31.07.18.
 */
interface DependencyVisitor<R> {

    fun visit(dependency: Dependency<*>, vararg children: Dependency<*>): R

}
