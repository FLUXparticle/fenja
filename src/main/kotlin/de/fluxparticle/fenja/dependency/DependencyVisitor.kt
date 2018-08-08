package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 31.07.18.
 */
internal interface DependencyVisitor<R> {

    fun visit(dependency: Dependency<*>, vararg children: Dependency<*>): R

}
