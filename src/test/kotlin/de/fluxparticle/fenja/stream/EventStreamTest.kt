package de.fluxparticle.fenja.stream

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import de.fluxparticle.fenja.FenjaSystem
import de.fluxparticle.fenja.logger.DelegateFenjaSystemLogger
import de.fluxparticle.fenja.logger.FenjaSystemLogger
import de.fluxparticle.fenja.logger.PrintFenjaSystemLogger
import de.fluxparticle.fenja.logger.TeeFenjaSystemLogger
import org.junit.Test

/**
 * Created by sreinck on 04.08.18.
 */
class EventStreamTest {

    private val logger = DelegateFenjaSystemLogger(PrintFenjaSystemLogger(System.out))

    private val system = FenjaSystem(logger)

    private val input by system.EventStreamSourceDelegate<Int>()

    private var output by system.EventStreamRelayDelegate<String>()

    @Test
    fun simple1() {
        output = input { it.toString() }

        system.finish()

        val mockLogger: FenjaSystemLogger = mock()
        val teeLogger = TeeFenjaSystemLogger(mockLogger, PrintFenjaSystemLogger(System.out))
        logger.setDelegate(teeLogger)

        input.sendValue(1, 42)

        verify(mockLogger).updateSource(any(), any())
        verify(mockLogger).executeUpdate(any(), any())
    }

}
