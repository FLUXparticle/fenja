package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 04.06.18.
 */
/*
fun Expr<out Variable>.extractFactors(): Sequence<String> = when (this) {
    is NegateExpr -> argument.extractFactors()
    is PlusExpr -> left.extractFactors() + right.extractFactors()
    is MinusExpr -> left.extractFactors() + right.extractFactors()
    is TimesExpr -> left.extractFactors() + right.extractFactors()
    is DivExpr -> left.extractFactors() + right.extractFactors()
    is Factor -> sequenceOf(factor.name)
    is ConstExpr -> emptySequence()
    is MinExpr -> arguments.asSequence().map { it.name }
    is MaxExpr -> arguments.asSequence().map { it.name }
    is MapExpr<*, *> -> (argument as Expr<out Variable>).extractFactors()
    is CombineExpr<*, *, *> -> (left as Expr<out Variable>).extractFactors() + (right as Expr<out Variable>).extractFactors()
}
*/
