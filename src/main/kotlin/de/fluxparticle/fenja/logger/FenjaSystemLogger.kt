package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.OutputExpr

/**
 * Created by sreinck on 25.07.18.
 */
interface FenjaSystemLogger {

    fun updateVariable(inputExpr: InputExpr<*>)

    fun evaluateRule(outputExpr: OutputExpr<*>)

    fun ruleLists(headline: String, map: Map<String, Collection<String>>)

}
