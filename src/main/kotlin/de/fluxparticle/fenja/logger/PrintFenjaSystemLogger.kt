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
        val sb = StringBuilder()
        sb.append(source.name)
        if (source.getTransaction() >= 0) {
            sb.append(" = ")
            sb.append(source.getValue())
        }
         out.println("===== $sb =====")
    }

    override fun executeUpdate(update: UpdateDependency<*>) {
        val sb = StringBuilder()

        sb.append(update.toUpdateString())
        if (update.name != null) {
            sb.append(" -> ")
            sb.append(update.name)
        }
        if (update.getTransaction() >= 0) {
            sb.append(" = ")
            sb.append(update.getValue())
        }

        out.println(sb.toString())
    }

    override fun ruleLists(headline: String, map: Map<Dependency<*>, List<UpdateDependency<*>>>) {
        out.println("===== $headline ======")
        map.entries
                .sortedBy { it.key.toString() }
                .forEach { (key, value) -> println("$key: $value") }
    }

}
