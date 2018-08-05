package de.fluxparticle.fenja.dependency

/**
 * Created by sreinck on 31.07.18.
 */
class NamedDependencyExtractor : DependencyVisitor<Sequence<NamedDependency<*>>> {

    override fun visit(dependency: Dependency<*>, vararg children: Dependency<*>): Sequence<NamedDependency<*>> {
        return when (dependency) {
            is NamedDependency -> sequenceOf(dependency)
            else -> children.asSequence().flatMap { it.accept(this) }
        }
    }

}
