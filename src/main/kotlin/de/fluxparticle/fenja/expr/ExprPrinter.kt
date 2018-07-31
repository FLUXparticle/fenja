package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty

/**
 * Created by sreinck on 28.07.18.
 */
open class ExprPrinter<Type> : ExprVisitor<Type, String, Boolean> {
    
    override fun visitConstExpr(value: Type, data: Boolean): String {
        return value.toString()
    }

    override fun <T> visitMapExpr(argument: Expr<T>, func: (T) -> Type, data: Boolean): String {
        val argumentResult = argument.accept(ExprPrinter(), true)
        return "$argumentResult {}".asFactor(data)
    }

    override fun <T, S> visitCombineExpr(left: Expr<T>, right: Expr<S>, func: (T, S) -> Type, data: Boolean): String {
        val leftResult = left.accept(ExprPrinter(), true)
        val rightResult = right.accept(ExprPrinter(), true)
        return "($leftResult..$rightResult) {}".asFactor(data)
    }

    override fun visitOutputExpr(name: String, rule: Expr<Type>?, property: ObjectProperty<Type>, data: Boolean): String {
        return rule?.accept(this, data) ?: name
    }

    companion object {

        @JvmStatic
        protected fun String.asFactor(factor: Boolean) = if (factor) "($this)" else this

    }

}
