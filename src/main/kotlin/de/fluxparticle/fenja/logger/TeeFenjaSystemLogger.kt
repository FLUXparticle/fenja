package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.OutputExpr

/**
 * Created by sreinck on 25.07.18.
 */
class TeeFenjaSystemLogger(private val left: FenjaSystemLogger, private val right: FenjaSystemLogger) : FenjaSystemLogger {

    override fun updateVariable(inputExpr: InputExpr<*>) {
        left.updateVariable(inputExpr)
        right.updateVariable(inputExpr)
    }

    override fun evaluateRule(outputExpr: OutputExpr<*>) {
        left.evaluateRule(outputExpr)
        right.evaluateRule(outputExpr)
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        left.ruleLists(headline, map)
        right.ruleLists(headline, map)
    }

}
