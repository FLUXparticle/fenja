package de.fluxparticle.fenja.logger

import de.fluxparticle.fenja.dependency.SourceDependency
import de.fluxparticle.fenja.dependency.UpdateDependency
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Created by sreinck on 25.07.18.
 */
class PrintFenjaSystemLogger(private val out: PrintWriter) : FenjaSystemLogger {

    constructor(stream: OutputStream) : this(PrintWriter(stream, true))

    override fun updateSource(source: SourceDependency<*>, value: Any?) {
         out.println("===== ${source.name} = $value =====")
    }

    override fun executeUpdate(update: UpdateDependency<*>, value: Any?) {
        out.println("${update.getDependency()} -> ${update.name} = $value")
    }

    override fun ruleLists(headline: String, map: Map<String, Collection<String>>) {
        out.println("===== $headline ======")
        map.entries
                .sortedBy { it.key }
                .forEach { (key, value) -> println("$key: ${value.sorted()}") }
    }

}
