package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.OutputExpr

/**
 * Created by sreinck on 25.07.18.
 */
class SilentFenjaSystemLogger : FenjaSystemLogger {

    override fun updateVariable(inputExpr: InputExpr<*>) {
        // empty
    }

    override fun evaluateRule(outputExpr: OutputExpr<*>) {
        // empty
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        // empty
    }

}
