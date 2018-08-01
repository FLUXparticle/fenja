package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
interface ExprVisitor<R> {

    fun visit(expr: Expr<*>, vararg children: Expr<*>): R

}
