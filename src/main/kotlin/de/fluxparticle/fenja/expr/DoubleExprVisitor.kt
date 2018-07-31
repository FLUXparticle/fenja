package de.fluxparticle.fenja.expr

/**
 * Created by sreinck on 31.07.18.
 */
interface DoubleExprVisitor<Result, Data> : ExprVisitor<Double, Result, Data> {
    
    fun visitNegateExpr(argument: Expr<Double>, data: Data): Result
    
    fun visitPlusExpr(left: Expr<Double>, right: Expr<Double>, data: Data): Result
    
    fun visitMinusExpr(left: Expr<Double>, right: Expr<Double>, data: Data): Result
    
    fun visitTimesExpr(left: Expr<Double>, right: Expr<Double>, data: Data): Result
    
    fun visitDivExpr(left: Expr<Double>, right: Expr<Double>, data: Data): Result

}
