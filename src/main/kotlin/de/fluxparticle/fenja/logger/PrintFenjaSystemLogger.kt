package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.Dependency
import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Created by sreinck on 25.07.18.
 */
class PrintFenjaSystemLogger(private val out: PrintWriter) : FenjaSystemLogger() {

    constructor(stream: OutputStream) : this(PrintWriter(stream, true))

    override fun updateSource(source: SourceDependency<*>) {
         out.println("===== ${source.name} = ${source.getValue()} =====")
    }

    override fun executeUpdate(update: UpdateDependency<*>) {
        out.println("${update.toUpdateString()} -> ${update.name} = ${update.getValue()}")
    }

    override fun ruleLists(headline: String, map: Map<Dependency<*>, List<UpdateDependency<*>>>) {
        out.println("===== $headline ======")
        map.entries
                .sortedBy { it.key.toString() }
                .forEach { (key, value) -> println("$key: $value") }
    }

}
