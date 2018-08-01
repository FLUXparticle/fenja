package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.expr.InputExpr
import de.fluxparticle.fenja.expr.OutputExpr
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Created by sreinck on 25.07.18.
 */
class PrintFenjaSystemLogger(private val out: PrintWriter) : FenjaSystemLogger {

    constructor(stream: OutputStream) : this(PrintWriter(stream, true))

    override fun updateVariable(inputExpr: InputExpr<*>) {
        out.println("===== ${inputExpr.name} = ${inputExpr.value} =====")
    }

    override fun evaluateRule(outputExpr: OutputExpr<*>) {
        out.println("${outputExpr.rule} -> ${outputExpr.name} = ${outputExpr.value}")
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        out.println("===== $headline ======")
        map.entries
                .sortedBy { it.key }
                .forEach { (key, value) -> println(key + ": " + value) }
    }

}
