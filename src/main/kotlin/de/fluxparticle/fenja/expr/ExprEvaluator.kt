package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty

/**
 * Created by sreinck on 31.07.18.
 */
open class ExprEvaluator<Type> : ExprVisitor<Type, Type, Void?> {

    override fun visitConstExpr(value: Type, data: Void?): Type {
        return value
    }

    override fun <T> visitMapExpr(argument: Expr<T>, func: (T) -> Type, data: Void?): Type {
        val argumentResult = argument.accept(ExprEvaluator(), data)
        return func.invoke(argumentResult)
    }

    override fun <T, S> visitCombineExpr(left: Expr<T>, right: Expr<S>, func: (T, S) -> Type, data: Void?): Type {
        val leftResult = left.accept(ExprEvaluator(), data)
        val rightResult = right.accept(ExprEvaluator(), data)
        return func.invoke(leftResult, rightResult)
    }

    override fun visitOutputExpr(name: String, rule: Expr<Type>?, property: ObjectProperty<Type>, data: Void?): Type {
        return property.value
    }

}
