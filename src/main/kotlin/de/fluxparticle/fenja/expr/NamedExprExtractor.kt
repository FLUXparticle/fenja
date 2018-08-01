package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
class NamedExprExtractor : ExprVisitor<Sequence<NamedExpr<*>>> {

    override fun visit(expr: Expr<*>, vararg children: Expr<*>): Sequence<NamedExpr<*>> {
        return when (expr) {
            is NamedExpr<*> -> sequenceOf(expr)
            else -> children.asSequence().flatMap { it.accept(this) }
        }
    }

}
