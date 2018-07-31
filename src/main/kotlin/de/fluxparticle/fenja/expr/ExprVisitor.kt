package de.fluxparticle.fenja.expr

import javafx.beans.property.ObjectProperty

/**
 * Created by sreinck on 31.07.18.
 */
interface ExprVisitor<Type, Result, Data> {

    fun visitConstExpr(value: Type, data: Data): Result

    fun <T> visitMapExpr(argument: Expr<T>, func: (T) -> Type, data: Data): Result

    fun <T, S> visitCombineExpr(left: Expr<T>, right: Expr<S>, func: (T, S) -> Type, data: Data): Result

    fun visitOutputExpr(name: String, rule: Expr<Type>?, property: ObjectProperty<Type>, data: Data): Result

}
